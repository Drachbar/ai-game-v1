package se.drachbar.aigamev1.config;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiChatModelName;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenAiModelConfig {
    private final ApiKeyConfig properties;

    private OpenAiStreamingChatModel createStreamingModel(OpenAiChatModelName modelName) {
        return OpenAiStreamingChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(modelName)
                .build();
    }

    private OpenAiChatModel createModel(OpenAiChatModelName modelName) {
        return OpenAiChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(modelName)
                .build();
    }

    @Bean
    public OpenAiStreamingChatModel gpt4oMiniStreamingModel() {
        return createStreamingModel(OpenAiChatModelName.GPT_4_O_MINI);
    }

    @Bean
    public OpenAiStreamingChatModel gpt4oStreamingModel() {
        return createStreamingModel(OpenAiChatModelName.GPT_4_O);
    }

    @Bean
    public OpenAiChatModel gpt4oMiniModel() {
        return createModel(OpenAiChatModelName.GPT_4_O_MINI);
    }

    @Bean
    public OpenAiChatModel gpt4oModel() {
        return createModel(OpenAiChatModelName.GPT_4_O);
    }
}
