package com.ssafy.tour.service;

import com.ssafy.tour.dto.TourApiResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
public class TourApiService {

    private final RestTemplate restTemplate;
    private final String tourApiKey;
    private static final String TOUR_API_BASE_URL = "http://apis.data.go.kr/B551011/KorService2";

    public TourApiService(RestTemplate restTemplate, @Value("${TOUR_API_KEY}") String tourApiKey) {
        this.restTemplate = restTemplate;
        this.tourApiKey = tourApiKey;
    }

    public List<TourApiResponseDto.Item> searchTouristAttractions(String areaCode, String sigunguCode, int count) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(TOUR_API_BASE_URL + "/areaBasedList2")
                    .queryParam("serviceKey", tourApiKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "SoloCo")
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", count)
                    .queryParam("pageNo", 1)
                    .queryParam("arrange", "B") // Sort by popularity
                    .queryParam("contentTypeId", "12"); // Tourist attraction

            if (areaCode != null && !areaCode.isEmpty()) {
                builder.queryParam("areaCode", areaCode);
            }
            if (sigunguCode != null && !sigunguCode.isEmpty()) {
                builder.queryParam("sigunguCode", sigunguCode);
            }

            URI uri = builder.build(true).toUri();
            log.info("Requesting TourAPI with URI: {}", uri);

            TourApiResponseDto responseDto = restTemplate.getForObject(uri, TourApiResponseDto.class);
            log.info("TourAPI Response: {}", responseDto);

            if (responseDto != null && responseDto.getResponse() != null && responseDto.getResponse().getBody() != null
                    && responseDto.getResponse().getBody().getItems() != null && responseDto.getResponse().getBody().getItems().getItemList() != null) {
                return responseDto.getResponse().getBody().getItems().getItemList();
            }

        } catch (HttpClientErrorException e) {
            log.error("Failed to call TourAPI - HTTP Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("An unexpected error occurred while calling TourAPI", e);
        }
        return Collections.emptyList();
    }

    public List<TourApiResponseDto.Item> searchTouristAttractionsByLocation(double longitude, double latitude, int radius, int count) {
        try {
            URI uri = UriComponentsBuilder.fromUriString(TOUR_API_BASE_URL + "/locationBasedList2")
                    .queryParam("serviceKey", tourApiKey)
                    .queryParam("MobileOS", "ETC")
                    .queryParam("MobileApp", "SoloCo")
                    .queryParam("_type", "json")
                    .queryParam("numOfRows", count)
                    .queryParam("pageNo", 1)
                    .queryParam("arrange", "B")
                    .queryParam("contentTypeId", "12")
                    .queryParam("mapX", longitude)
                    .queryParam("mapY", latitude)
                    .queryParam("radius", radius)
                    .build(true).toUri();

            log.info("Requesting TourAPI with location-based search: {}", uri);

            TourApiResponseDto responseDto = restTemplate.getForObject(uri, TourApiResponseDto.class);
            log.info("TourAPI Location-Based Response: {}", responseDto);
            
            if (responseDto != null && responseDto.getResponse() != null && responseDto.getResponse().getBody() != null
                    && responseDto.getResponse().getBody().getItems() != null && responseDto.getResponse().getBody().getItems().getItemList() != null) {
                return responseDto.getResponse().getBody().getItems().getItemList();
            }

        } catch (HttpClientErrorException e) {
            log.error("Failed to call location-based TourAPI - HTTP Status: {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            log.error("An unexpected error occurred while calling location-based TourAPI", e);
        }
        return Collections.emptyList();
    }
}
