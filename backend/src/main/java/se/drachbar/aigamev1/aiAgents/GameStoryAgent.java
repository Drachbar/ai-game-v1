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
public class GameStoryAgent {
    private final Map<String, OpenAiStreamingChatModel> streamingModels; // Injicera en Map av alla streaming-modeller

    public Mono<List<ChatMessage>> processQuery(List<ChatMessage> history, String query, int round, String modelName, WebSocketSession session) {
        OpenAiStreamingChatModel model = streamingModels.getOrDefault(modelName, streamingModels.get("gpt4oMiniStreamingModel"));
        List<ChatMessage> messages = new ArrayList<>();
        messages.add(new SystemMessage("""
                    DU FÅR ABSOLUT INTE skriva några val, alternativ eller frågor till spelarna.
                    Ditt enda uppdrag är att fortsätta berättelsen i fri text, som en novell.
                    Du får INTE skriva fraser som "Vad vill du göra nu?" eller lista val som "1.", "2.", etc.
                    Det är en annan AI-agent som sköter valen. Om du bryter mot detta så förstörs spelets logik.
                """));
        messages.add(new SystemMessage("""
                Du är en kreativ berättare som fortsätter en pågående historia för ett onlinespel.
                Fortsätt historien baserat på spelarnas val. Historien ska pågå i cirka 10 rundor.
                Du får in information om vilken runda spelet är på (just nu runda %d), så försök att ha klimax nära slutet av historien.
                Spelarna kommer få göra olika val där valen skrivs av en annan ai-agent, om någon spelare gör något uppenbart dumt så kan den
                spelaren få dö/förlora tidigt i spelet. Du avgör om spelarens val lyckas eller inte.
                När en spelare förlorar eller dör inkludera orden "du dör" eller "du förlorar" i historien.
                När historien når sitt naturliga slut, inkludera '[GAME OVER]' i svaret.
                """.formatted(round)));
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

    public Mono<List<ChatMessage>> processQuery(List<ChatMessage> history, String query, int round, WebSocketSession session) {
        return processQuery(history, query, round, "gpt4oMiniStreamingModel", session); // Default till gpt-4o-mini
    }
}
