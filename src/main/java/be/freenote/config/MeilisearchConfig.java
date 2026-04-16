package be.freenote.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MeilisearchConfig {

    @Value("${app.meilisearch.host}")
    private String host;

    @Value("${app.meilisearch.api-key}")
    private String apiKey;

    public String getHost() {
        return host;
    }

    public String getApiKey() {
        return apiKey;
    }
}
