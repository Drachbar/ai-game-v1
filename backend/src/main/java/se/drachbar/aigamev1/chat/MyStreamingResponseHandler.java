package se.drachbar.aigamev1.chat;


import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

public class MyStreamingResponseHandler implements StreamingChatResponseHandler {

    private final WebSocketSession session;

    public MyStreamingResponseHandler(WebSocketSession session) {
        this.session = session;
    }

    @Override
    public void onPartialResponse(String partialResponse) {
        session.send(Mono.just(session.textMessage(partialResponse))).subscribe();
    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {
        session.send(Mono.just(session.textMessage("<complete-response>" + completeResponse.aiMessage().text() + "</complete-response>"))).subscribe();
    }

    @Override
    public void onError(Throwable error) {
        session.send(Mono.just(session.textMessage("Error: " + error.getMessage()))).subscribe();
    }
}
