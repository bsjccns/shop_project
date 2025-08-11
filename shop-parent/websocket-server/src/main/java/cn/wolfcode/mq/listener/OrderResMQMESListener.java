package cn.wolfcode.mq.listener;


import cn.wolfcode.core.WebSocketServer;
import cn.wolfcode.mq.OrderMQResult;
import cn.wolfcode.mq.RabbitMQConstant;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.websocket.Session;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class OrderResMQMESListener {

    @RabbitListener(queues = RabbitMQConstant.ORDER_RESULT_QUEUE)
    public void onMessage(OrderMQResult result) {
        log.info("[订单结果] 收到订单结果消息：{}", JSON.toJSONString(result));
        try {
            int count = 3;
            Session session = null;
            do {
                // 将收到的消息发送给客户端
                session = WebSocketServer.SESSION_MAP.get(result.getToken());
                if (session != null) {
                    session.getBasicRemote().sendText(JSON.toJSONString(result));
                    log.info("[订单结果] 消息成功通知到用户：{}", result.getToken());
                    break;
                }

                log.warn("[订单结果] 无法获取用户连接信息：{}，count：{}", result.getToken(), count);
                // 拿不到睡 300ms
                TimeUnit.MILLISECONDS.sleep(300);
                count--;
            } while (count > 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

