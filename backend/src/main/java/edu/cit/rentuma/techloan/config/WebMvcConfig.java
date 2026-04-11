package edu.cit.rentuma.techloan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves files from the /uploads/ directory as static resources.
 * e.g. GET /uploads/equipment/item-1-abc123.jpg
 * resolves to the file at uploads/equipment/item-1-abc123.jpg on disk.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}