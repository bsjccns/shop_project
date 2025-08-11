package cn.wolfcode.mq;

public class RabbitMQConstant {

    // 交换机名称
    public static final String ORDER_PENDING_EXCHANGE = "order.pending.exchange";
    public static final String ORDER_RESULT_EXCHANGE = "order.result.exchange";
    public static final String ORDER_PAY_TIMEOUT_DLX_EXCHANGE = "order.pay.timeout.dlx.exchange";
    public static final String CANCEL_SECKILL_OVER_EXCHANGE = "seckill.cancel.over.exchange";

    // 队列名称
    public static final String ORDER_PENDING_QUEUE = "order.pending.queue";
    public static final String ORDER_RESULT_QUEUE = "order.result.queue";

    public static final String ORDER_PAY_TIMEOUT_QUEUE_NO_EXCHANGE = "order.pay.timeout.queue.no.exchange";
    // 接收订单超时消息的死信队列
    public static final String ORDER_PAY_TIMEOUT_DLX_QUEUE = "order.pay.timeout.dlx.queue";
    public static final String CANCEL_SECKILL_OVER_QUEUE = "seckill.cancel.over.queue";

    // 路由键
    public static final String ORDER_PENDING_ROUTING_KEY = "order.pending.routing.key";
    public static final String ORDER_RESULT_ROUTING_KEY = "order.result.routing.key";
    public static final String ORDER_PAY_TIMEOUT_ROUTING_DLX_KEY = "order.pay.timeout.routing.dlx.key";
    public static final String CANCEL_SECKILL_OVER_ROUTING_KEY = "seckill.cancel.over.routing.key";
}