package cn.wolfcode.mq.listener;

import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.OrderMessage;
import cn.wolfcode.mq.OrderTimeoutMessage;
import cn.wolfcode.mq.RabbitMQConstant;
import cn.wolfcode.service.IOrderInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderTimeOutCheckListener {

    private final IOrderInfoService orderInfoService;

    public OrderTimeOutCheckListener(IOrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    // 延迟队列，处理订单超时未支付问题
    @RabbitListener(queues = RabbitMQConstant.ORDER_PAY_TIMEOUT_DLX_QUEUE)
    public void onMessage(OrderMQResult orderMessage){
        log.info("订单超时检查，开始处理：{}", orderMessage.toString());

        OrderTimeoutMessage orderTimeoutMessage = new OrderTimeoutMessage();
        orderTimeoutMessage.setOrderNo(orderMessage.getOrderNo());
        orderTimeoutMessage.setSeckillId(orderMessage.getSeckillId());
        orderTimeoutMessage.setUserPhone(orderMessage.getUserPhone());
        orderInfoService.checkPyTimeout(orderTimeoutMessage);

    }
}
