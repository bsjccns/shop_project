//package cn.wolfcode.mq.listener;
//
//import cn.wolfcode.mq.MQConstant;
//import cn.wolfcode.mq.OrderMessage;
//import cn.wolfcode.service.IOrderInfoService;
//import com.alibaba.fastjson.JSON;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
//import org.apache.rocketmq.spring.core.RocketMQListener;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//@Slf4j
//@Component
//@RocketMQMessageListener(
//        consumerGroup = MQConstant.ORDER_PENDING_CONSUMER_GROUP2,
//        topic = MQConstant.ORDER_PENDING_TOPIC
//)
//public class CreateOrderListener implements RocketMQListener<OrderMessage>{
//
//    @Autowired
//    IOrderInfoService orderInfoService;
//    @Override
//    public void onMessage(OrderMessage orderMessage) {
//        log.info("接收到消息{}", JSON.toJSON(orderMessage));
//        //创建订单
//        String orderId = orderInfoService.doSeckill(orderMessage.getSeckillId(), orderMessage.getUserPhone(), orderMessage.getTime());
//
//
//    }
//}
