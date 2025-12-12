package com.ssafy.travel.ai.service;

import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.ai.prompt.GroupTravelPromptBuilder;
import com.ssafy.travel.ai.prompt.SoloTravelPromptBuilder;
import com.ssafy.travel.itinerary.dto.GroupItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;

@Service
public class AIService {

    private final ChatClient openAiChatClient;
    private final GroupTravelPromptBuilder groupPromptBuilder;
    private final SoloTravelPromptBuilder soloPromptBuilder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AIService(@Qualifier("openAiChatClient") ChatClient openAiChatClient, GroupTravelPromptBuilder groupPromptBuilder, SoloTravelPromptBuilder soloPromptBuilder) {
    	this.openAiChatClient = openAiChatClient;
    	this.groupPromptBuilder = groupPromptBuilder;
    	this.soloPromptBuilder = soloPromptBuilder;
	}

    public List<GroupItineraryCandidateResponseDto> generateGroupItinerary(int tripDays, List<TravelProjectPlace> places) {
    	try {
            String prompt = groupPromptBuilder.build(places, tripDays);
            String raw = openAiChatClient.prompt().user(prompt).call().content();
            String json = extractJson(raw); // JSON만 뽑아내는 함수

            return objectMapper.readValue(
                    json,
                    new TypeReference<List<GroupItineraryCandidateResponseDto>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Group itinerary AI generation failed", e);
        }
    }

    public List<SoloItineraryCandidateResponseDto> generateSoloItinerary(int tripDays, List<TravelProjectPlace> places) {
        try {
            String prompt = soloPromptBuilder.build(places, tripDays);
            String raw = openAiChatClient.prompt().user(prompt).call().content();
            String json = extractJson(raw); // JSON만 뽑아내는 함수
            
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<SoloItineraryCandidateResponseDto>>() {}
            );

        } catch (Exception e) {
            throw new RuntimeException("Solo itinerary AI generation failed", e);
        }
    }

    public String extractJson(String text) {
        if (text == null || text.isBlank()) {
            throw new RuntimeException("응답이 비어 있음");
        }

        int start = text.indexOf('[');
        int endRaw = text.lastIndexOf(']');

        if (start == -1 || endRaw == -1 || endRaw < start) {
            throw new RuntimeException("JSON 배열 구간을 찾을 수 없음");
        }

        return text.substring(start, endRaw + 1);
    }
    
    public void validate(List<? extends ItineraryCandidateResponseDto> candidates, Set<Long> validPlaceIds) {
        for (var candidate : candidates) {
            for (var day : candidate.getDays()) {
                for (var place : day.getPlaces()) {

                    // 기존 장소
                    if (!place.isNewPlace()) {
                        if (place.getPlaceId() == null || !validPlaceIds.contains(place.getPlaceId())) {
                            throw new CustomException(ErrorCode.INVALID_AI_RESULT);
                        }
                    }
                }
            }
        }
    }

}
