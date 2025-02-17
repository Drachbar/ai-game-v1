package se.drachbar.aigamev1.service;


import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.springframework.stereotype.Service;
import se.drachbar.aigamev1.chat.MyStreamingResponseHandler;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatService {

    private final StreamingChatLanguageModel model;

    public ChatService() {
        this.model = OpenAiStreamingChatModel.builder()
                .apiKey("")
                .modelName(OpenAiChatModelName.GPT_4_O_MINI)
                .build();
    }

    public void processQuery(final String query, MyStreamingResponseHandler responseHandler) {
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("You are a helpful AI agent, you answer short and concise."));
        messages.add(new SystemMessage("You are an expert in java and Angular"));
        messages.add(new UserMessage(query));

//        model.chat(ChatRequest.builder().messages(messages).build(), );
    }

}
