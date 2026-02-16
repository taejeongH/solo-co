package com.ssafy.ai.service;

import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.ai.dto.SoloPlaceAnalysisDto;
import com.ssafy.ai.prompt.GroupTravelPromptBuilder;
import com.ssafy.ai.prompt.GroupTravelRefinementPromptBuilder;
import com.ssafy.ai.prompt.PlaceAnalysisPromptBuilder;
import com.ssafy.ai.prompt.SoloTravelPromptBuilder;
import com.ssafy.ai.prompt.SoloTravelRefinementPromptBuilder;
import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.travel.itinerary.dto.GroupItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;

@Service
public class AIService {

    private final ChatClient openAiChatClient;
    private final GroupTravelPromptBuilder groupPromptBuilder;
    private final SoloTravelPromptBuilder soloPromptBuilder;
    private final PlaceAnalysisPromptBuilder placeAnalysisPromptBuilder;
    private final SoloTravelRefinementPromptBuilder soloRefinementPromptBuilder;
    private final GroupTravelRefinementPromptBuilder groupRefinementPromptBuilder;

    public AIService(@Qualifier("openAiChatClient") ChatClient openAiChatClient,
            GroupTravelPromptBuilder groupPromptBuilder, SoloTravelPromptBuilder soloPromptBuilder,
            PlaceAnalysisPromptBuilder placeAnalysisPromptBuilder,
            SoloTravelRefinementPromptBuilder soloRefinementPromptBuilder,
            GroupTravelRefinementPromptBuilder groupRefinementPromptBuilder) {
        this.openAiChatClient = openAiChatClient;
        this.groupPromptBuilder = groupPromptBuilder;
        this.soloPromptBuilder = soloPromptBuilder;
        this.placeAnalysisPromptBuilder = placeAnalysisPromptBuilder;
        this.soloRefinementPromptBuilder = soloRefinementPromptBuilder;
        this.groupRefinementPromptBuilder = groupRefinementPromptBuilder;
    }

    @Cacheable(value = "soloPlaceAnalysis", key = "#placeDetails.path('place_id').asText()")
    public SoloPlaceAnalysisDto analyzePlaceForSoloTravel(JsonNode placeDetails) {
        try {
            var outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<SoloPlaceAnalysisDto>() {
            });
            String prompt = placeAnalysisPromptBuilder.build(placeDetails) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "혼자 여행 장소 분석 AI 생성에 실패했습니다.");
        }
    }

    public List<GroupItineraryCandidateResponseDto> generateGroupItinerary(int tripDays,
            List<TravelProjectPlace> places) {

        try {
            var outputConverter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<List<GroupItineraryCandidateResponseDto>>() {
                    });
            String prompt = groupPromptBuilder.build(places, tripDays) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "그룹 여행 일정 AI 생성에 실패했습니다.");
        }
    }

    public List<SoloItineraryCandidateResponseDto> generateSoloItinerary(int tripDays,
            List<TravelProjectPlace> places) {

        try {
            var outputConverter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<List<SoloItineraryCandidateResponseDto>>() {
                    });
            String prompt = soloPromptBuilder.build(places, tripDays) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "혼자 여행 일정 AI 생성에 실패했습니다.");
        }
    }

    public List<SoloItineraryCandidateResponseDto> refineSoloItinerary(int tripDays,
            ItineraryCandidateResponseDto base) {
        try {
            var outputConverter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<List<SoloItineraryCandidateResponseDto>>() {
                    });
            String prompt = soloRefinementPromptBuilder.build(base, tripDays) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "혼자 여행 일정 고도화에 실패했습니다.");
        }
    }

    public List<GroupItineraryCandidateResponseDto> refineGroupItinerary(int tripDays,
            ItineraryCandidateResponseDto base) {
        try {
            var outputConverter = new BeanOutputConverter<>(
                    new ParameterizedTypeReference<List<GroupItineraryCandidateResponseDto>>() {
                    });
            String prompt = groupRefinementPromptBuilder.build(base, tripDays) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "그룹 여행 일정 고도화에 실패했습니다.");
        }
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
