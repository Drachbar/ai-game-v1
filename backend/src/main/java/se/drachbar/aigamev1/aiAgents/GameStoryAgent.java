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

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GameStoryAgent {
    private final Map<String, OpenAiStreamingChatModel> streamingModels; // Injicera en Map av alla streaming-modeller

    public Mono<String> processQuery(List<ChatMessage> history, String query, int round, String modelName, WebSocketSession session) {
        OpenAiStreamingChatModel model = streamingModels.getOrDefault(modelName, streamingModels.get("gpt4oMiniStreamingModel"));
        List<ChatMessage> messages = List.of(
                new SystemMessage("""
                        Du är en kreativ berättare som fortsätter en pågående historia för ett onlinespel.
                        Fortsätt historien baserat på spelarnas val. Historien ska pågå i cirka 10 rundor.
                        Du får in information om vilken runda spelet är på (just nu runda %d), så försök att ha klimax nära slutet av historien.
                        Spelarna kommer få göra olika val, om någon spelare gör något uppenbart dumt så kan den
                        spelaren få dö/förlora tidigt i spelet. Du avgör om spelarens val lyckas eller inte.
                        När historien når sitt naturliga slut, inkludera '[GAME OVER]' i svaret.
                        """.formatted(round)),
                new UserMessage(query)
        );

        GameStreamingResponseHandler responseHandler = new GameStreamingResponseHandler(session);
        model.chat(ChatRequest.builder().messages(messages).build(), responseHandler);
        return responseHandler.getResponse()
                .map(response -> {
                    history.add(new UserMessage(query)); // Lägg till spelarens val i historien
                    history.add(new AiMessage(response)); // Lägg till AI:ns svar i historien
                    return response;
                });
    }

    public Mono<String> processQuery(List<ChatMessage> history, String query, int round, WebSocketSession session) {
        return processQuery(history, query, 1, "gpt4oMiniStreamingModel", session); // Default till gpt-4o-mini
    }
}
