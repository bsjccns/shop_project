package cn.wolfcode.mq;

import cn.wolfcode.domain.LoginLog;
import cn.wolfcode.mapper.UserMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Service
public class MQLoginLogListener2 {

    @Autowired
    private UserMapper userMapper;

    @RabbitListener(queues = MQConstant.LOGIN_QUEUE)
    public void onMessage(LoginLog message) {
        // 通过 RabbitMQ 进行异步登录日志记录
        userMapper.insertLoginLong(message);
    }
}
