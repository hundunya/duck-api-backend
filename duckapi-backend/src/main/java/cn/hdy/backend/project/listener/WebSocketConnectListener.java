package cn.hdy.backend.project.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

/**
 * @author 滴滴鸭
 */
@Component
@RequiredArgsConstructor
public class WebSocketConnectListener {

    @EventListener
    public void handleConnectEvent(SessionConnectEvent event){
        System.out.println(event.getMessage());
        System.out.println(event.getSource());
        System.out.println(event.getUser());
        System.out.println("用户连接");
    }

    @EventListener
    public void handleDisconnectEvent(SessionDisconnectEvent event){
        System.out.println(event.getSessionId());
        System.out.println("用户断开连接");
    }

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event){
        System.out.println(event.getMessage());
        System.out.println(event.getSource());
        System.out.println(event.getUser());
        System.out.println("客户端订阅事件");
    }

    @EventListener
    public void handleUnSubscribeEvent(SessionUnsubscribeEvent event){
        System.out.println(event.getMessage());
        System.out.println(event.getSource());
        System.out.println(event.getUser());
        System.out.println("客户端取消订阅");
    }
}
