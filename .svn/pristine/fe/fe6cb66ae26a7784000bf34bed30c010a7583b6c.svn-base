package com.wolfking.jeesite.modules.ws.config;

import com.wolfking.jeesite.common.config.redis.RedisConstant;
import com.wolfking.jeesite.common.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

/**
 * WebSocket 配置
 */
@Configuration
@ComponentScan({"com.wolfking.jeesite.common"})
@EnableWebSocketMessageBroker
@Slf4j
//通过EnableWebSocketMessageBroker 开启使用STOMP协议来传输基于代理(message broker)的消息,此时浏览器支持使用@MessageMapping 就像支持@RequestMapping一样。
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer{
    
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) { //endPoint 注册协议节点,并映射指定的URl
        // 客户端与服务器端建立连接的点
        // 在网页上可以通过"/applicationName/hello"来和服务器的WebSocket连接
        // 如：http://localhost:8080/endpointWS
        //注册一个Stomp 协议的endpoint,并指定 SockJS协议
        // setAllowedOrigins("*")表示可以跨域
        registry.addEndpoint("/endpointWS")
                .setAllowedOrigins("*")
                .withSockJS();
    }


    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 配置客户端发送信息的路径的前缀
        // 客户端向服务器端发送时的主题上面需要加"/ws"作为前缀
        registry.setApplicationDestinationPrefixes("/ws");
        // 在topic和queue这两个域上服务端可以向客户端发消息
        //queue:点对点
        registry.enableSimpleBroker("/topic","/queue");
        // 服务端给客户端指定用户发送一对一的主题，前缀是"/user"
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureWebSocketTransport(final WebSocketTransportRegistration registration) {
        registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
            @Override
            public WebSocketHandler decorate(final WebSocketHandler handler) {
                return new WebSocketHandlerDecorator(handler) {
                    @Override
                    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
                        // 客户端与服务器端建立连接后，此处记录谁上线了
                        String userId = session.getPrincipal().getName();//id
                        log.info("online: " + userId);
                        //System.out.println("ws connect:" + userId);
                        redisUtils.hmSet(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,RedisConstant.WEBSOCKET_SESSION,userId,userId,0l);
//                        User user = UserUtils.getUser();
//                        if(user.isCustomer()){
//
//                        }
                        super.afterConnectionEstablished(session);
                    }

                    @Override
                    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
                        // 客户端与服务器端断开连接后，此处记录谁下线了
                        String userId = session.getPrincipal().getName();
                        log.info("offline: " + userId);
                        redisUtils.hdel(RedisConstant.RedisDBType.REDIS_CONSTANTS_DB,RedisConstant.WEBSOCKET_SESSION,userId);
                        super.afterConnectionClosed(session, closeStatus);
                    }
                };
            }
        });
        super.configureWebSocketTransport(registration);
    }

}
