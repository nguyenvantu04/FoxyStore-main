package com.example.back.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Map /images/** to multiple locations to ensure we find the files
        // 1. file:src/... -> Standard dev run from module root
        // 2. file:javawebback/src/... -> Run from project root
        // 3. classpath:/static/images/ -> Fallback to built resources (where old images might be)
        registry.addResourceHandler("/images/**")
                .addResourceLocations(
                        "file:src/main/resources/static/images/",
                        "file:javawebback/src/main/resources/static/images/",
                        "classpath:/static/images/"
                )
                .setCachePeriod(0); // Disable caching to ensure immediate visibility
    }
}
