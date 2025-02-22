package se.drachbar.aigamev1.websocket;

import org.springframework.lang.NonNull;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import se.drachbar.aigamev1.service.ChatService;

public class MyWebSocketHandler implements WebSocketHandler {
    private final ChatService chatService;

    public MyWebSocketHandler(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    @NonNull
    public Mono<Void> handle(WebSocketSession session) {
        Flux<String> inputMessages = session.receive()
                .map(WebSocketMessage::getPayloadAsText);

        return inputMessages
                .doOnNext(message -> chatService.processQuery(message, session))
                .then();
    }
}
