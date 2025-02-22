package se.drachbar.aigamev1.aiAgents;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
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
public class StartGameStoryAgent {

    private final Map<String, OpenAiStreamingChatModel> streamingModels;

    public Mono<String> startStory(List<ChatMessage> history, List<String> playerIds, String theme, String modelName, WebSocketSession session) {
        OpenAiStreamingChatModel model = streamingModels.getOrDefault(modelName, streamingModels.get("gpt4oMiniStreamingModel"));
        String playerList = String.join(", ", playerIds); // Konvertera till kommaseparerad sträng
        List<ChatMessage> messages = List.of(
                new SystemMessage("""
                        Du är en kreativ berättare som startar en ny historia för ett onlinespel.
                        Skapa en engagerande introduktion baserat på följande tema: '%s'.
                        Historien ska inkludera följande spelare: %s.
                        Historien ska pågå i cirka 10 rundor, med ett klimax nära slutet.
                        Ge en tydlig startpunkt där spelarna kan börja göra val.
                        """.formatted(theme, playerList))
        );

        GameStreamingResponseHandler responseHandler = new GameStreamingResponseHandler(session);
        model.chat(ChatRequest.builder().messages(messages).build(), responseHandler);
        return responseHandler.getResponse()
                .map(response -> {
                    history.add(new AiMessage(response)); // Lägg till introduktionen i historien
                    return response;
                });
    }

    public Mono<String> startStory(List<ChatMessage> history, List<String> playerIds, String theme, WebSocketSession session) {
        return startStory(history, playerIds, theme, "gpt4oMiniStreamingModel", session);
    }
}
