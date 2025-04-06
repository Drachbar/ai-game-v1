package se.drachbar.aigamev1.aiAgents;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import se.drachbar.aigamev1.chat.GameStreamingResponseHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameStoryFinishAgent {
    private final Map<String, OpenAiStreamingChatModel> streamingModels; // Injicera en Map av alla streaming-modeller

    public Mono<List<ChatMessage>> processQuery(List<ChatMessage> history, String query, String modelName, WebSocketSession session) {
        OpenAiStreamingChatModel model = streamingModels.getOrDefault(modelName, streamingModels.get("gpt4oMiniStreamingModel"));
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("""
                Du är en kreativ berättare som fortsätter en pågående historia för ett onlinespel.
                Fortsätt historien baserat på spelarnas val. Denna text som du skriver nu ska vara runt 500 ord.
                Detta är den sista berättelsen, så se till att avsluta historien på ett episkt sätt.
                Spelarna har fått göra olika val där valen skrivs av en annan ai-agent,
                om någon spelare gör något uppenbart dumt så kan den spelaren få dö/förlora tidigt i spelet.
                Du avgör om spelarens val lyckas eller inte. När en spelare förlorar eller dör inkludera orden
                "du dör" eller "du förlorar" i historien. Avsluta med att tydligt skriva för spelarna om de vann eller förlorade.
                """));
        messages.addAll(history);
        messages.add(new UserMessage(query));

        GameStreamingResponseHandler responseHandler = new GameStreamingResponseHandler(session);
        model.chat(ChatRequest.builder().messages(messages).build(), responseHandler);
        return responseHandler.getResponse()
                .map(response -> {
                    List<ChatMessage> newMessages = new ArrayList<>();
                    newMessages.add(new UserMessage(query));  // Spelarens val
                    newMessages.add(new AiMessage(response)); // AI:ns svar
                    return newMessages;
                });
    }

    public Mono<List<ChatMessage>> processQuery(List<ChatMessage> history, String query, WebSocketSession session) {
        return processQuery(history, query, "gpt4oMiniStreamingModel", session); // Default till gpt-4o-mini
    }
}
