package se.drachbar.aigamev1.aiAgents;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.output.Response;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChoiceAgentTest {

    @InjectMocks
    private ChoiceAgent sut;

    @Mock
    private ChatLanguageModel chatModel;

    @Test
    void shouldParseResponseToArray() {
        when(chatModel.generate(anyList())).thenReturn(new Response<>(new AiMessage("[\"Val1, med komma\", \"Val2\", \"Val3\", \"Val4\"]")));
        final String[] actual = sut.generateChoices(List.of(), "Current-player");
        final String[] expected = {"Val1, med komma", "Val2", "Val3", "Val4"};
        assertArrayEquals(expected, actual);
    }

    @Test
    void shouldParseMarkdownResponseToArray() {
        when(chatModel.generate(anyList())).thenReturn(new Response<>(new AiMessage("""
                ```json
                ["Val1", "Val2", "Val3", "Val4"]
                ```
                """)));
        final String[] actual = sut.generateChoices(List.of(), "Current-player");
        final String[] expected = {"Val1", "Val2", "Val3", "Val4"};
        assertArrayEquals(expected, actual);
    }
}