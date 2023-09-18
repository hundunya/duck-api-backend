package cn.hdy.backend.project.job;

import cn.hdy.backend.project.common.ResultUtils;
import cn.hdy.backend.project.config.RabbitConfig;
import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;

/**
 * RabbitMQ 定时消息队列 消费监听回调
 *
 * @author 滴滴鸭
 **/
@Slf4j
@Component
public class RabbitConsumer {

    @Resource
    private SimpMessagingTemplate messagingTemplate;

    private final static Gson GSON = new Gson();

    @SuppressWarnings(value = {"rawtypes", "unchecked"})
    @RabbitListener(queues = RabbitConfig.MSG_QUEUE) //监听队列
    public void messageReceive(String json, Message message, Channel channel) throws IOException {
        Map<String, Object> map = (Map) GSON.fromJson(json, Map.class);
        log.info("----------------接收到消息--------------------"+ map);
        //发送给WebSocket 由WebSocket推送给前端
        String destination = "/pay/"+map.get("outTradeNo");
        messagingTemplate.convertAndSend(destination, ResultUtils.success(map.get("status")));
        // 确认消息已接收
        channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
    }
}

