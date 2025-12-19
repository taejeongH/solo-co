package com.ssafy.ai.config;

import java.io.IOException;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class AiConfiguration {
    @Bean
    RestClient.Builder restClientBuilder() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
    }
    
    @Bean
    ChatClient openAiChatClient(@Qualifier("openAiChatModel") ChatModel chatModel) throws IOException {
        return ChatClient.builder(chatModel)
                .defaultOptions(ChatOptions.builder().temperature(0.7).maxTokens(5000).build())
                .build();
    }
}
