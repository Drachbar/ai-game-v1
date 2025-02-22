package se.drachbar.aigamev1.service;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import se.drachbar.aigamev1.chat.MyStreamingResponseHandler;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final Map<String, OpenAiStreamingChatModel> models;

    public void processQuery(String query, WebSocketSession session) {
        processQuery(query, session, "gpt4oMiniStreamingModel");
    }

    public void processQuery(String query, WebSocketSession session, String modelName) {
        OpenAiStreamingChatModel model = models.getOrDefault(modelName, models.get("gpt4oMiniStreamingModel"));

        List<ChatMessage> messages = List.of(
                new SystemMessage("You are a helpful AI agent, you answer short and concise."),
                new SystemMessage("You are an expert in Java and Angular."),
                new UserMessage(query)
        );

        MyStreamingResponseHandler responseHandler = new MyStreamingResponseHandler(session);
        model.chat(ChatRequest.builder().messages(messages).build(), responseHandler);
    }

}
