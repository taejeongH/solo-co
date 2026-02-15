package com.ssafy.travel.itinerary.service;

import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto.ItineraryDayDto;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto.ItineraryPlaceDto;
import com.ssafy.travel.place.entity.TravelProjectPlace;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ItineraryAlgorithmService {

    /**
     * 지리적 군집화 및 최단 경로 로직을 사용하여 기본 경로를 생성합니다.
     */
    public ItineraryCandidateResponseDto generateBaseItinerary(String projectType, int tripDays,
            List<TravelProjectPlace> places) {
        if (places == null || places.isEmpty()) {
            return createEmptyItinerary(tripDays);
        }

        // 1. 초기화
        List<ItineraryDayDto> days = new ArrayList<>();
        for (int i = 1; i <= tripDays; i++) {
            ItineraryDayDto dayDto = new ItineraryDayDto();
            dayDto.setDay(i);
            dayDto.setPlaces(new ArrayList<>());
            days.add(dayDto);
        }

        // 2. 장소 간 상대 거리 기반 최근접 이웃(Nearest Neighbor) 분배
        // 현재 위치(GPS)를 사용하지 않고, 선택된 장소 중 첫 번째 장소를 기점으로 삼습니다.
        List<TravelProjectPlace> remainingPlaces = new ArrayList<>(places);
        TravelProjectPlace currentPlace = remainingPlaces.remove(0);
        int currentDay = 1;

        // 하루 최대 장소 개수
        int targetPlacesPerDay = Math.max(1, (int) Math.ceil((double) places.size() / tripDays));

        addPlaceToDay(days.get(0), currentPlace);

        while (!remainingPlaces.isEmpty()) {
            TravelProjectPlace nextPlace = findNearest(currentPlace, remainingPlaces);
            remainingPlaces.remove(nextPlace);

            // 현재 날짜에 장소가 너무 많으면 다음 날로 이동
            if (days.get(currentDay - 1).getPlaces().size() >= targetPlacesPerDay && currentDay < tripDays) {
                currentDay++;
            }

            addPlaceToDay(days.get(currentDay - 1), nextPlace);
            currentPlace = nextPlace;
        }

        ItineraryCandidateResponseDto response = new ItineraryCandidateResponseDto();
        response.setDays(days);
        response.setRouteType(1); // 기본 타입
        response.setReason("지리적 근접성을 최우선으로 고려하여 효율적인 동선을 구성했습니다.");

        return response;
    }

    private TravelProjectPlace findNearest(TravelProjectPlace origin, List<TravelProjectPlace> candidates) {
        return candidates.stream()
                .min(Comparator.comparingDouble(p -> calculateDistance(origin, p)))
                .orElse(candidates.get(0));
    }

    private double calculateDistance(TravelProjectPlace p1, TravelProjectPlace p2) {
        double dLat = p1.getLatitude() - p2.getLatitude();
        double dLon = p1.getLongitude() - p2.getLongitude();
        return Math.sqrt(dLat * dLat + dLon * dLon);
    }

    private void addPlaceToDay(ItineraryDayDto day, TravelProjectPlace place) {
        ItineraryPlaceDto dto = new ItineraryPlaceDto();
        dto.setPlaceId(place.getPlaceId());
        dto.setPlaceName(place.getPlaceName());
        dto.setPlaceAddress(place.getPlaceAddress());
        dto.setPlaceType(place.getPlaceType());
        dto.setLatitude(place.getLatitude());
        dto.setLongitude(place.getLongitude());
        dto.setThumbnail(place.getThumbnail());
        dto.setNewPlace(false);
        day.getPlaces().add(dto);
    }

    private ItineraryCandidateResponseDto createEmptyItinerary(int tripDays) {
        ItineraryCandidateResponseDto response = new ItineraryCandidateResponseDto();
        List<ItineraryDayDto> days = new ArrayList<>();
        for (int i = 1; i <= tripDays; i++) {
            ItineraryDayDto day = new ItineraryDayDto();
            day.setDay(i);
            day.setPlaces(new ArrayList<>());
            days.add(day);
        }
        response.setDays(days);
        return response;
    }
}
