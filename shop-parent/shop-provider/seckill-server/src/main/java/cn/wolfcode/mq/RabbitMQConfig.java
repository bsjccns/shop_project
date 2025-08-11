package cn.wolfcode.mq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    // 订单待处理交换机
    @Bean
    public DirectExchange orderPendingExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_PENDING_EXCHANGE, true, false);
    }

    // 订单待处理队列
    @Bean
    public Queue orderPendingQueue() {
        return new Queue(RabbitMQConstant.ORDER_PENDING_QUEUE, true);
    }

    // 订单待处理队列绑定
    @Bean
    public Binding orderPendingBinding(DirectExchange orderPendingExchange, Queue orderPendingQueue) {
        return BindingBuilder.bind(orderPendingQueue).to(orderPendingExchange).with(RabbitMQConstant.ORDER_PENDING_ROUTING_KEY);
    }

    // 订单结果交换机
    @Bean
    public DirectExchange orderResultExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_RESULT_EXCHANGE, true, false);
    }

    // 订单结果队列
    @Bean
    public Queue orderResultQueue() {
        return new Queue(RabbitMQConstant.ORDER_RESULT_QUEUE, true);
    }

    // 订单结果队列绑定
    @Bean
    public Binding orderResultBinding(DirectExchange orderResultExchange, Queue orderResultQueue) {
        return BindingBuilder.bind(orderResultQueue).to(orderResultExchange).with(RabbitMQConstant.ORDER_RESULT_ROUTING_KEY);
    }

    // 普通队列
    @Bean
    public Queue orderPayTimeoutQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-message-ttl", 1000*60*30); // TTL 设置为 5000 毫秒（5秒）
        args.put("x-dead-letter-exchange", RabbitMQConstant.ORDER_PAY_TIMEOUT_DLX_EXCHANGE); // 死信交换机
        args.put("x-dead-letter-routing-key", RabbitMQConstant.ORDER_PAY_TIMEOUT_ROUTING_DLX_KEY); // 死信路由键
        return new Queue(RabbitMQConstant.ORDER_PAY_TIMEOUT_QUEUE_NO_EXCHANGE, true, false, false, args);
    }

    // 死信交换机
    @Bean
    public DirectExchange dlxExchange() {
        return new DirectExchange(RabbitMQConstant.ORDER_PAY_TIMEOUT_DLX_EXCHANGE, true, false);
    }

    // 死信队列
    @Bean
    public Queue dlxQueue() {
        return new Queue(RabbitMQConstant.ORDER_PAY_TIMEOUT_DLX_QUEUE, true, false, false);
    }

    // 绑定死信交换机和死信队列
    @Bean
    public Binding dlxBinding() {
        return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(RabbitMQConstant.ORDER_PAY_TIMEOUT_ROUTING_DLX_KEY);
    }

    // 取消秒杀过期交换机
    @Bean
    public DirectExchange cancelSeckillOverExchange() {
        return new DirectExchange(RabbitMQConstant.CANCEL_SECKILL_OVER_EXCHANGE, true, false);
    }

    // 取消秒杀过期队列
    @Bean
    public Queue cancelSeckillOverQueue() {
        return new Queue(RabbitMQConstant.CANCEL_SECKILL_OVER_QUEUE, true);
    }

    // 取消秒杀过期队列绑定
    @Bean
    public Binding cancelSeckillOverBinding(DirectExchange cancelSeckillOverExchange, Queue cancelSeckillOverQueue) {
        return BindingBuilder.bind(cancelSeckillOverQueue).to(cancelSeckillOverExchange).with(RabbitMQConstant.CANCEL_SECKILL_OVER_ROUTING_KEY);
    }
}