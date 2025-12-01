package com.ssafy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.ssafy.global.interceptor.RequestLogger;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private RequestLogger requestLogger;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestLogger);
    }
}