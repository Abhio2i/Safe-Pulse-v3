// Web App - Authority access via the Backend

//package com.example.jwt.config;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class CorsConfig implements WebMvcConfigurer {
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://68.183.89.215:5000")
//                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
//                .allowedHeaders("*")
//                .allowCredentials(true)
//                .maxAge(3600);
//    }
//}
package com.maarco.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
//         registry       .addMapping("/dashboardDemo/**")
                .allowedOrigins("http://68.183.89.215:5000",
                                "http://www.nutrifyindianow.in/",
                                "https://www.nutrifyindianow.in/",
                                "http://localhost:3000/",
                                "http://localhost:5500/",
                                "http://127.0.0.1:5500",
                                "localhost:3000/")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
