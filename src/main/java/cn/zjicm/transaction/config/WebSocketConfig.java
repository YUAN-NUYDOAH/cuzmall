package cn.zjicm.transaction.config;

import cn.zjicm.transaction.websocket.SubstituteChatWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SubstituteChatWebSocketHandler substituteChatWebSocketHandler;

    public WebSocketConfig(SubstituteChatWebSocketHandler substituteChatWebSocketHandler) {
        this.substituteChatWebSocketHandler = substituteChatWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(substituteChatWebSocketHandler, "/ws/substitutes/{id}")
                .setAllowedOrigins("*");
    }
}
