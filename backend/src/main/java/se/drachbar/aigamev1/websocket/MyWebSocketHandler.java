package se.drachbar.aigamev1.websocket;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class MyWebSocketHandler implements WebSocketHandler {
    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(session.receive()
                .map(msg -> session.textMessage("Echo: " + msg.getPayloadAsText())));
    }
}
