package se.drachbar.aigamev1.aiAgents;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChoiceAgent {
    private final Map<String, OpenAiChatModel> chatModels;
    private final ChatLanguageModel gpt4oMiniModel;

    public String[] generateChoices(List<ChatMessage> storyHistory, String currentPlayer) {
        List<ChatMessage> messages = new ArrayList<>(storyHistory);
        messages.add(new SystemMessage("""
                Du är en kreativ assistent som genererar valalternativ för ett onlinespel.
                Baserat på den aktuella historien, ge exakt 4 realistiska och varierade valmöjligheter
                för nästa steg i berättelsen. Det ska vara minst ett smart val och ett ganska dumt val.
                Returnera valen exakt som en JSON-array av strängar, t.ex.:
                ["val 1", "val 2", "val 3", "val 4"]
                Om spelet har avslutats (t.ex. innehåller '[GAME OVER]' eller alla spelare är döda), returnera en tom array: []
                """));
        messages.add(new SystemMessage("""
                Nuvarande spelare är: %s
                """.formatted(currentPlayer)));
        final String response = gpt4oMiniModel.generate(messages).content().text();
        final String[] choices = parseChoices(response);
        if (choices.length == 0) {
            log.error("Inga val fanns");
        }
        return choices;
    }

    private String[] parseChoices(String response) {
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(trimMarkdown(response), String[].class);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.error("Det gick inte att parsea meddelandet: {}", response);
            return new String[0];
        }
    }

    private String trimMarkdown(String input) {
        int start = input.indexOf('[');
        int end = input.lastIndexOf(']');

        if (start == -1 || end == -1 || end <= start) {
            return "";
        }

        return input.substring(start, end + 1);
    }
}
