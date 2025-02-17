package se.drachbar.aigamev1.chat;


import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler;

public class MyStreamingResponseHandler implements StreamingChatResponseHandler {

    @Override
    public void onPartialResponse(String partialResponse) {

    }

    @Override
    public void onCompleteResponse(ChatResponse completeResponse) {

    }

    @Override
    public void onError(Throwable error) {

    }
}
