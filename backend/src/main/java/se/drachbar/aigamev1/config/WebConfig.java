package se.drachbar.aigamev1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import se.drachbar.aigamev1.service.GameService;
import se.drachbar.aigamev1.websocket.MyWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFlux
public class WebConfig {

    @Bean
    public HandlerMapping handlerMapping(GameService gameService) {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws/chat", new MyWebSocketHandler(gameService));
        int order = -1;
        return new SimpleUrlHandlerMapping(map, order);
    }
}
