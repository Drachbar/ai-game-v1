package se.drachbar.aigamev1.chat;

import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.CompletableFuture;

public class GameStreamingResponseHandler implements StreamingChatResponseHandler {
    private final WebSocketSession session;
    private final CompletableFuture<String> future;
    private final StringBuilder fullResponse;

    public GameStreamingResponseHandler(WebSocketSession session) {
        this.session = session;
        this.future = new CompletableFuture<>();
        this.fullResponse = new StringBuilder();
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        fullResponse.append(partialResponse);
        session.send(Mono.just(session.textMessage(partialResponse))).subscribe();
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        session.send(Mono.just(session.textMessage("<complete-response>" + completeResponse.aiMessage().text() + "</complete-response>")))
                .subscribe();
        future.complete(fullResponse.toString());
    }

    @Override
    public void onError(Throwable error) {
        session.send(Mono.just(session.textMessage("Error: " + error.getMessage()))).subscribe();
        future.completeExceptionally(error);
    }

    public Mono<String> getResponse() {
        return Mono.fromFuture(future);
    }
}
