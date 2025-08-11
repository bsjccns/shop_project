package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.mq.RabbitMQConstant;
import cn.wolfcode.service.IOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderPendingMessageListener2 {

    private final IOrderInfoService orderInfoService;

    private final RabbitTemplate rabbitTemplate;

    public OrderPendingMessageListener2(IOrderInfoService orderInfoService, RabbitTemplate rabbitTemplate) {
        this.orderInfoService = orderInfoService;
        this.rabbitTemplate = rabbitTemplate;
    }


    @RabbitListener(queues = RabbitMQConstant.ORDER_PENDING_QUEUE)
    public void onMessage(OrderMessage orderMessage){
        log.info("收到下单消息：{}",orderMessage);
        OrderMQResult result = new OrderMQResult();

        result.setSeckillId(orderMessage.getSeckillId());
        result.setToken(orderMessage.getToken());
        result.setTime(orderMessage.getTime());
        //创建订单
        try {
            String orderId = orderInfoService.doSeckill(orderMessage.getSeckillId(), orderMessage.getUserPhone(), orderMessage.getTime());
            log.info("创建订单成功，订单号：{}",orderId);
            result.setOrderNo(orderId);
            result.setCode(200);
            result.setMsg("下单成功，待支付");
            result.setUserPhone(orderMessage.getUserPhone()); // 发送给支付超时队列的时候，需要用户手机号
            //下单成功，发送支付延迟消息 直接向普通队列发消息，过一段时间消息会进入死信队列，并被消费掉
            rabbitTemplate.convertAndSend(RabbitMQConstant.ORDER_PAY_TIMEOUT_QUEUE_NO_EXCHANGE,result);
            result.setUserPhone(null);
        }catch (Exception e){
            log.error("创建订单失败",e);
            //TODO 回滚redis库存
            orderInfoService.syncStock(orderMessage.getSeckillId(), orderMessage.getUserPhone());
            result.setCode(500);
            result.setMsg("下单失败");
        }
        // 发送消息给websocket服务 ，websocket用于把订单id返回给前端
        rabbitTemplate.convertAndSend(RabbitMQConstant.ORDER_RESULT_EXCHANGE, RabbitMQConstant.ORDER_RESULT_ROUTING_KEY, result);
    }
}
