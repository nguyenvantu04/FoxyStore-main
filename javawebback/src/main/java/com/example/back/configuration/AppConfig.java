package com.example.back.configuration;

import io.github.cdimascio.dotenv.Dotenv;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
@Configuration
public class AppConfig {
    @PostConstruct
    public void loadEnv() {
        Dotenv dotenv = Dotenv.load();
        System.setProperty("GOOGLE_CLIENT_ID", dotenv.get("GOOGLE_CLIENT_ID"));
        System.setProperty("GOOGLE_CLIENT_SECRET", dotenv.get("GOOGLE_CLIENT_SECRET"));
        System.setProperty("OPENAI_API_KEY", dotenv.get("OPENAI_API_KEY"));
        System.setProperty("QDRANT_URL", dotenv.get("QDRANT_URL"));
        System.setProperty("QDRANT_API_KEY", dotenv.get("QDRANT_API_KEY"));
    }

}
