package cn.wolfcode.web.controller;

import cn.wolfcode.common.constants.CommonConstants;
import cn.wolfcode.common.domain.UserInfo;
import cn.wolfcode.common.exception.BusinessException;
import cn.wolfcode.common.web.CodeMsg;
import cn.wolfcode.common.web.Result;
import cn.wolfcode.common.web.anno.RequireLogin;
import cn.wolfcode.domain.OrderInfo;
import cn.wolfcode.domain.SeckillProductVo;
import cn.wolfcode.mq.MQConstant;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.mq.RabbitMQConstant;
import cn.wolfcode.redis.CommonRedisKey;
import cn.wolfcode.redis.SeckillRedisKey;
import cn.wolfcode.service.IOrderInfoService;
import cn.wolfcode.service.ISeckillProductService;
import cn.wolfcode.util.DateUtil;
import cn.wolfcode.web.msg.SeckillCodeMsg;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@RestController
@RequestMapping("/order")
@Slf4j
public class OrderInfoController {

    /**
     * 本地库存售完标记缓存
     * key=秒杀id
     * value=是否已经售完
     */
    public static final Map<Long, Boolean> LOCAL_STOCK_COUNT_FLAG_CACHE = new ConcurrentHashMap<>();

    @Autowired
    private ISeckillProductService seckillProductService;
    @Autowired
    private StringRedisTemplate redisTemplate;
//    @Autowired
//    private RocketMQTemplate rocketMQTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private IOrderInfoService orderInfoService;

    /**
     * 优化前：
     * 测试数据：500 个用户，100 线程，执行 50 次
     * 测试情况：330 QPS
     * 优化后：
     * 测试数据：500 个用户，500 线程，执行 100 次
     * 测试情况：2700 QPS
     */
    @RequireLogin
    @RequestMapping("/doSeckill")
    public Result<String> doSeckill(Integer time, Long seckillId, @RequestHeader(CommonConstants.TOKEN_NAME) String token) {
//        // 1. 基于 token 获取到用户信息(必须登录)
//        UserInfo userInfo = this.getUserByToken(token);
//        // 2. 基于场次+秒杀id获取到秒杀商品对象
//        SeckillProductVo vo = seckillProductService.selectByIdAndTime(seckillId, time);
//        if (vo == null) {
//            throw new BusinessException(SeckillCodeMsg.REMOTE_DATA_ERROR);
//        }
//        // 3. 判断时间是否大于开始时间 && 小于 开始时间+2小时
//        /*if (!DateUtil.isLegalTime(vo.getStartDate(), time)) {
//            throw new BusinessException(SeckillCodeMsg.OUT_OF_SECKILL_TIME_ERROR);
//        }*/
//        // 增加本地缓存判断
//        Boolean flag = LOCAL_STOCK_COUNT_FLAG_CACHE.get(seckillId);
//        if (flag != null && flag) {
//            // 如果最终结果返回 true，直接抛出库存不足异常
//            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
//        }
//
//        // 4. 判断用户是否重复下单
//        // 基于用户 + 秒杀 id + 场次查询订单, 如果存在订单, 说明用户已经下过单
//        int max = 1;
//        String userOrderCountKey = SeckillRedisKey.SECKILL_ORDER_HASH.join(seckillId + "");
//        Long increment = redisTemplate.opsForHash().increment(userOrderCountKey, userInfo.getPhone() + "", 1);
//        if (increment > max) {
//            throw new BusinessException(SeckillCodeMsg.REPEAT_SECKILL);
//        }
//
//        // 5. 通过 redis 库存预减控制访问人数
//        String stockCountKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.join(time + "");
//        // 对库存自减以后，会返回剩余库存数量
//        long remain = redisTemplate.opsForHash().increment(stockCountKey, seckillId + "", -1);
//        if (remain < 0) {
//            // 标记当前库存已经售完
//            LOCAL_STOCK_COUNT_FLAG_CACHE.put(seckillId, true);
//            throw new BusinessException(SeckillCodeMsg.SECKILL_STOCK_OVER);
//        }
//        // 6. 执行下单操作(减少库存, 创建订单)
//        // 修改为利用 RocketMQ 发送消息，实现异步下单
//        rocketMQTemplate.asyncSend(MQConstant.ORDER_PENDING_TOPIC, new OrderMessage(time, seckillId, token, userInfo.getPhone()), new DefaultMQMessageCallback());
//        return Result.success("成功加入下单队列，正在排队中...");

        //1. 判断登录
        UserInfo userInfo = getUserByToken(token);
        // 2.秒杀商品是否存在
        SeckillProductVo seckillProductVo = seckillProductService.selectByIdAndTime(seckillId, time);
        // 2.1异常请求
        if (seckillProductVo==null) throw new BusinessException(SeckillCodeMsg.REMOTE_DATA_ERROR);
        // 3. 判断是否在秒杀时间内  为了方便测试 不加
//        if (!DateUtil.isLegalTime(seckillProductVo.getStartDate(), time)) {
//            throw new BusinessException(SeckillCodeMsg.OUT_OF_SECKILL_TIME_ERROR);
//        }
        // 4. 一人一单(优化成 redis查找下单记录)
//        OrderInfo orderInfo = orderInfoService.selectByUserIdAndSeckillId(userInfo.getPhone(), seckillId, time);
//        if (orderInfo!=null) throw new BusinessException(SeckillCodeMsg.REPEAT_PAY_ERROR);
        Long incremented = redisTemplate.opsForHash().increment(SeckillRedisKey.SECKILL_ORDER_HASH.join(seckillId + ""), String.valueOf(userInfo.getPhone()), 1);
        if (incremented>1) return Result.error("已经下过单");
        String stockCountKey = SeckillRedisKey.SECKILL_STOCK_COUNT_HASH.join(time + "");

        String orderID =null;
        try {

            // 5.库存是否充足 （优化成redis预扣库存）
            Long increment = redisTemplate.opsForHash().increment(stockCountKey, String.valueOf(seckillId), -1);

            if (increment<0) {
                redisTemplate.opsForHash().delete(SeckillRedisKey.SECKILL_ORDER_HASH.join(seckillId + ""), String.valueOf(userInfo.getPhone()));
                return Result.error(SeckillCodeMsg.SECKILL_STOCK_OVER);
            };

            // 6. 执行下单操作 返回订单编号
            OrderMessage orderMessage = new OrderMessage();
            orderMessage.setTime(time);
            orderMessage.setSeckillId(seckillId);
            orderMessage.setToken(token);
            orderMessage.setUserPhone(userInfo.getPhone());
//            rocketMQTemplate.asyncSend(MQConstant.ORDER_PENDING_TOPIC,orderMessage,new DefaultMQMessageCallback());
            rabbitTemplate.convertAndSend(RabbitMQConstant.ORDER_PENDING_EXCHANGE, RabbitMQConstant.ORDER_PENDING_ROUTING_KEY, orderMessage);
        }catch (Exception e){
            // 去除redis下单记录,redis库存回滚
//            redisTemplate.opsForHash().delete(String.valueOf(seckillId), String.valueOf(userInfo.getPhone()));
//            redisTemplate.opsForHash().increment(stockCountKey, String.valueOf(seckillId), 1);
            throw e;
        }

        return Result.success("成功加入下单队列，正在排队中...");

    }

    private UserInfo getUserByToken(String token) {
        return JSON.parseObject(redisTemplate.opsForValue().get(CommonRedisKey.USER_TOKEN.getRealKey(token)), UserInfo.class);
    }

    @RequireLogin
    @GetMapping("/find")
    public Result<OrderInfo> findById(String orderNo, @RequestHeader(CommonConstants.TOKEN_NAME) String token) {
        UserInfo userInfo = getUserByToken(token);
        OrderInfo orderInfo = orderInfoService.findByOrderNo(orderNo, userInfo.getPhone());
        return Result.success(orderInfo);
    }
}
