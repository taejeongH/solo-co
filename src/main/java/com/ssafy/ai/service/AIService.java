package com.ssafy.ai.service;

import java.util.List;
import java.util.Set;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.ai.prompt.GroupTravelPromptBuilder;
import com.ssafy.ai.prompt.SoloTravelPromptBuilder;
import com.ssafy.travel.itinerary.dto.GroupItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import org.springframework.cache.annotation.Cacheable;

import com.fasterxml.jackson.databind.JsonNode;
import com.ssafy.ai.dto.SoloPlaceAnalysisDto;
import com.ssafy.ai.prompt.PlaceAnalysisPromptBuilder;
import com.ssafy.tour.dto.TourApiResponseDto;
import com.ssafy.tour.service.TourApiService;
import com.ssafy.travel.itinerary.dto.SoloItineraryCandidateResponseDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap; // Using LinkedHashMap to maintain insertion order if needed, though sorting keys provides the robust solution
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.DoubleSummaryStatistics;


@Service
public class AIService {

    private final ChatClient openAiChatClient;
    private final GroupTravelPromptBuilder groupPromptBuilder;
    private final SoloTravelPromptBuilder soloPromptBuilder;
    private final PlaceAnalysisPromptBuilder placeAnalysisPromptBuilder;
    private final TourApiService tourApiService;

    public AIService(@Qualifier("openAiChatClient") ChatClient openAiChatClient, GroupTravelPromptBuilder groupPromptBuilder, SoloTravelPromptBuilder soloPromptBuilder, PlaceAnalysisPromptBuilder placeAnalysisPromptBuilder, TourApiService tourApiService) {
        this.openAiChatClient = openAiChatClient;
        this.groupPromptBuilder = groupPromptBuilder;
        this.soloPromptBuilder = soloPromptBuilder;
        this.placeAnalysisPromptBuilder = placeAnalysisPromptBuilder;
        this.tourApiService = tourApiService;
    }

    @Cacheable(value = "soloPlaceAnalysis", key = "#placeDetails.path('place_id').asText()")
    public SoloPlaceAnalysisDto analyzePlaceForSoloTravel(JsonNode placeDetails) {
        try {
            var outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<SoloPlaceAnalysisDto>() {});
            String prompt = placeAnalysisPromptBuilder.build(placeDetails) + "\n" + outputConverter.getFormat();

            return openAiChatClient.prompt()
                    .user(prompt)
                    .call()
                    .entity(outputConverter);

        } catch (Exception e) {
            throw new RuntimeException("Solo place analysis AI generation failed", e);
        }
    }

    public List<GroupItineraryCandidateResponseDto> generateGroupItinerary(int tripDays, List<TravelProjectPlace> places) {
        List<TourApiResponseDto.Item> newPlaces = Collections.emptyList();

        if (places != null && !places.isEmpty()) {
            DoubleSummaryStatistics latStats = places.stream()
                    .filter(p -> p.getLatitude() != null)
                    .mapToDouble(TravelProjectPlace::getLatitude)
                    .summaryStatistics();

            DoubleSummaryStatistics lonStats = places.stream()
                    .filter(p -> p.getLongitude() != null)
                    .mapToDouble(TravelProjectPlace::getLongitude)
                    .summaryStatistics();

            if (latStats.getCount() > 0 && lonStats.getCount() > 0) {
                double centerLat = latStats.getAverage();
                double centerLon = lonStats.getAverage();
                int radius = 10000;
                int count = 10;
                newPlaces = tourApiService.searchTouristAttractionsByLocation(centerLon, centerLat, radius, count);
            }
        }
        
    	try {
    		var outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<GroupItineraryCandidateResponseDto>>() {});
            String prompt = groupPromptBuilder.build(places, tripDays, newPlaces) + "\n" + outputConverter.getFormat();
            
            return openAiChatClient.prompt()
            		.user(prompt)
            		.call()
            		.entity(outputConverter);

        } catch (Exception e) {
            throw new RuntimeException("Group itinerary AI generation failed", e);
        }
    }

    public List<SoloItineraryCandidateResponseDto> generateSoloItinerary(int tripDays, List<TravelProjectPlace> places) {
        List<TourApiResponseDto.Item> newPlaces = Collections.emptyList();

        if (places != null && !places.isEmpty()) {
            DoubleSummaryStatistics latStats = places.stream()
                    .filter(p -> p.getLatitude() != null)
                    .mapToDouble(TravelProjectPlace::getLatitude)
                    .summaryStatistics();

            DoubleSummaryStatistics lonStats = places.stream()
                    .filter(p -> p.getLongitude() != null)
                    .mapToDouble(TravelProjectPlace::getLongitude)
                    .summaryStatistics();

            if (latStats.getCount() > 0 && lonStats.getCount() > 0) {
                double centerLat = latStats.getAverage();
                double centerLon = lonStats.getAverage();
                int radius = 10000;
                int count = 10;
                
                System.out.println(centerLat + " " + centerLon);
                newPlaces = tourApiService.searchTouristAttractionsByLocation(centerLon, centerLat, radius, count);
            }
        }


        try {
        	var outputConverter = new BeanOutputConverter<>(new ParameterizedTypeReference<List<SoloItineraryCandidateResponseDto>>() {});
            String prompt = soloPromptBuilder.build(places, tripDays, newPlaces) + "\n" + outputConverter.getFormat();
            
            return openAiChatClient.prompt()
            		.user(prompt)
            		.call()
            		.entity(outputConverter);

        } catch (Exception e) {
            throw new RuntimeException("Solo itinerary AI generation failed", e);
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
