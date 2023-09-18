package cn.hdy.backend.project.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;


/**
 * RabbitMQ 配置类
 *
 * @author 滴滴鸭
 **/
@Configuration
public class RabbitConfig {

    //websocket 消息队列
    public static final String MSG_QUEUE = "msg_queue";

    //消息交换机
    public static final String MSG_EXCHANGE = "msg_exchange";

    //消息路由键
    public static final String MSG_ROUTING_KEY = "msg_routing_key";

    /**
     * 消息队列
     */
    @Bean
    public Queue msgQueue(){
        return new Queue(MSG_QUEUE);
    }

    @Bean
    public DirectExchange directExchange(){
        return new DirectExchange(MSG_EXCHANGE);
    }

    /**
     * 消息队列绑定消息交换机
     */
    @Bean
    public Binding msgBinding(){
        return BindingBuilder.bind(msgQueue()).to(directExchange()).with(MSG_ROUTING_KEY);
    }
}

