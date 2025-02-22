package se.drachbar.aigamev1.aiAgents;

import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChoiceAgent {
    private final Map<String, OpenAiChatModel> chatModels;
    private final ChatLanguageModel gpt4oMiniModel;

    public String[] generateChoices(List<ChatMessage> storyHistory) {
        List<ChatMessage> messages = new ArrayList<>(storyHistory);
        messages.add(new SystemMessage("""
                Du är en kreativ assistent som genererar valalternativ för ett onlinespel.
                Baserat på den aktuella historien, ge exakt 4 realistiska och varierade valmöjligheter
                för nästa steg i berättelsen. Returnera alternativen som en array av strängar i formatet:
                ["val 1", "val 2", "val 3", "val 4"].
                Om spelet har avslutats (t.ex. innehåller '[GAME OVER]' eller alla spelare är döda), returnera en tom array: [].
                """));

        String response = gpt4oMiniModel.generate(messages).content().text();
        try {
            // Förväntar sig att AI:n returnerar en array som en sträng, t.ex. '["Gå", "Stanna", "Spring", "Ropa"]'
            return parseChoices(response);
        } catch (Exception e) {
            // Fallback om parsing misslyckas
            return new String[0];
        }
    }

    private String[] parseChoices(String response) {
        // Enkel parsing av sträng till array (förutsätter att AI:n följer formatet)
        String trimmed = response.trim().replaceAll("[\\[\\]\"]", "").trim();
        if (trimmed.isEmpty()) {
            return new String[0];
        }
        String[] choices = trimmed.split(",\\s*");
        return choices.length == 4 ? choices : new String[0]; // Säkerställ att vi får exakt 4 val
    }
}
