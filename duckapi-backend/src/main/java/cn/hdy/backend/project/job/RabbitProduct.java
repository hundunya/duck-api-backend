package cn.hdy.backend.project.job;

import cn.hdy.backend.project.config.RabbitConfig;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.UUID;

/**
 * RabbitMQ 消息提供者
 *
 * @author 滴滴鸭
 **/
@Slf4j
@Component
public class RabbitProduct {

    @Resource
    private RabbitTemplate rabbitTemplate;

    private final static Gson GSON = new Gson();

    /**
     * 构造方法注入rabbitTemplate
     */
    @Autowired
    public RabbitProduct(RabbitTemplate rabbitTemplate){
        this.rabbitTemplate = rabbitTemplate;
    }



    //发送消息 推送到websocket    参数自定义 转为String发送消息
    public void sendMessage(Map<String, Object> message){
        CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
        String json = GSON.toJson(message);
        //把消息对象放入路由对应的队列当中去
        rabbitTemplate.convertAndSend(RabbitConfig.MSG_EXCHANGE,RabbitConfig.MSG_ROUTING_KEY, json, correlationId);
    }

}
