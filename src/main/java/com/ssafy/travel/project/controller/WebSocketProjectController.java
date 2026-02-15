package com.ssafy.travel.project.controller;

import com.ssafy.travel.project.dto.ProjectEventDto;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Controller
public class WebSocketProjectController {

    @MessageMapping("/projects/{projectId}/chat")
    @SendTo("/topic/projects/{projectId}/chat")
    public ChatMessage handleChat(@DestinationVariable Long projectId, @Payload ChatMessage message) {
        return message;
    }

    @MessageMapping("/projects/{projectId}/events")
    @SendTo("/topic/projects/{projectId}/updates")
    public ProjectEventDto handleEvent(@DestinationVariable Long projectId, @Payload ProjectEventDto event) {
        return event;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatMessage {
        private String sender;
        private String content;
        private String timestamp;
        private String type; // CHAT, JOIN, LEAVE
    }
}
