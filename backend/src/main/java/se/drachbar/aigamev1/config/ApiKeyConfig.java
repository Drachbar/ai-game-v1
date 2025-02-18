package se.drachbar.aigamev1.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ApiKeyConfig {

    @Value("${api.key.open-ai}")
    private String apiKey;

}
