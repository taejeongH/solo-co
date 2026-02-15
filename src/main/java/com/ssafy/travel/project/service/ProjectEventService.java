package com.ssafy.travel.project.service;

import com.ssafy.travel.project.dto.ProjectEventDto;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectEventService {

    private final SimpMessageSendingOperations messagingTemplate;

    public void notifyProjectUpdate(Long projectId, String type) {
        notifyProjectUpdate(projectId, type, null);
    }

    public void notifyProjectUpdate(Long projectId, String type, Object data) {
        ProjectEventDto event = new ProjectEventDto(projectId, type, data);
        messagingTemplate.convertAndSend("/topic/projects/" + projectId + "/updates", event);
    }
}
