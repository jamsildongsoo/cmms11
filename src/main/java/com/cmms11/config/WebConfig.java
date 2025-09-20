package com.cmms11.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Serve plain HTML from templates as static pages
        registry.addResourceHandler("/auth/**")
                .addResourceLocations("classpath:/templates/auth/");
        registry.addResourceHandler("/plant/**")
                .addResourceLocations("classpath:/templates/plant/");
        registry.addResourceHandler("/domain/**")
                .addResourceLocations("classpath:/templates/domain/");
        registry.addResourceHandler("/layout/**")
                .addResourceLocations("classpath:/templates/layout/");
        registry.addResourceHandler("/inspection/**")
                .addResourceLocations("classpath:/templates/inspection/");
        registry.addResourceHandler("/workorder/**")
                .addResourceLocations("classpath:/templates/workorder/");
        registry.addResourceHandler("/workpermit/**")
                .addResourceLocations("classpath:/templates/workpermit/");
        registry.addResourceHandler("/inventoryTx/**")
                .addResourceLocations("classpath:/templates/inventoryTx/");
        registry.addResourceHandler("/approval/**")
                .addResourceLocations("classpath:/templates/approval/");
        registry.addResourceHandler("/memo/**")
                .addResourceLocations("classpath:/templates/memo/");
        // Static assets (already under resources/static/assets)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
        // Compatibility for templates that reference /static/**
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
