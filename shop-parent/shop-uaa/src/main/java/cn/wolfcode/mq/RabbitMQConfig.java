package cn.wolfcode.mq;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // 登录日志交换机
    @Bean
    public DirectExchange loginExchange() {
        return new DirectExchange(MQConstant.LOGIN_EXCHANGE, true, false);
    }

    // 登录日志队列
    @Bean
    public Queue loginQueue() {
        return new Queue(MQConstant.LOGIN_QUEUE, true);
    }

    // 登录日志队列绑定
    @Bean
    public Binding loginBinding(DirectExchange loginExchange, Queue loginQueue) {
        return BindingBuilder.bind(loginQueue).to(loginExchange).with(MQConstant.LOGIN_ROUTING_KEY);
    }


    @Autowired
    public void configureRabbitTemplate(RabbitTemplate rabbitTemplate) {
        // 设置消息确认回调
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("Message sent successfully: " + correlationData.getId());
            } else {
                System.out.println("Message send failed: " + correlationData.getId() + ", cause: " + cause);
                // 可以在这里进行重试或记录日志
            }
        });

        // 设置消息返回回调
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            System.out.println("Message returned: " + new String(message.getBody()) + ", replyCode: " + replyCode + ", replyText: " + replyText + ", exchange: " + exchange + ", routingKey: " + routingKey);
            // 可以在这里进行重试或记录日志
        });
    }


}
