package com.ssafy.place.service;

import java.util.Collections;
import java.util.List;
import java.util.Map; // Added for PlaceDetailDto geometry
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.global.exception.CustomException;
import com.ssafy.global.exception.ErrorCode;
import com.ssafy.ai.dto.SoloPlaceAnalysisDto;
import com.ssafy.place.dto.PersonalPlaceDetailDto;
import com.ssafy.place.dto.PersonalPlaceDto;
import com.ssafy.place.dto.PlaceDetailDto;
import com.ssafy.place.dto.PlaceDetailDto.OpeningHoursDto;
import com.ssafy.place.dto.PlaceDetailDto.ReviewDto;
import com.ssafy.place.dto.PlaceDto;
import com.ssafy.place.dto.PlaceSearchItemDto;
import com.ssafy.place.dto.PlaceSearchResponseDto;
import com.ssafy.ai.service.AIService;
import com.ssafy.travel.project.entity.TravelProject;
import com.ssafy.travel.project.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PlaceService {

	@Value("${google.places.api.key}")
	private String googlePlacesApiKey;

	private final String GOOGLE_PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place";
	private final RestTemplate restTemplate;
	private final ObjectMapper objectMapper;
	private final TravelProjectMapper travelProjectMapper;
	private final AIService aiService;

	@Cacheable("places")
	public PlaceSearchResponseDto searchPlaces(String query, String location, String type, String nextPageToken) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(GOOGLE_PLACES_API_BASE_URL + "/textsearch/json")
				.queryParam("query", query)
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko");

		if (location != null && !location.isEmpty()) {
			uriBuilder.queryParam("location", location);
		}

		if (type != null && !type.isEmpty()) {
			uriBuilder.queryParam("type", type);
		}

		if (nextPageToken != null && !nextPageToken.isEmpty()) {
			uriBuilder.queryParam("pagetoken", nextPageToken);
		}

		String url = uriBuilder.build(false).toUriString();

		try {
			String response = restTemplate.getForObject(url, String.class);
			JsonNode root = objectMapper.readTree(response);
			String status = root.path("status").asText();

			if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST,
						"Google Places API error: " + status + " - " + root.path("error_message").asText());
			}

			List<PlaceSearchItemDto> places = mapGooglePlaceResultsToDto(root.path("results"));
			String nextToken = root.path("next_page_token").asText(null);
			return PlaceSearchResponseDto.builder()
					.places(places)
					.nextPageToken(nextToken)
					.build();
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_ERROR, "Error calling Google Places API: " + e.getMessage());
		}

	}

	private List<PlaceSearchItemDto> mapGooglePlaceResultsToDto(JsonNode results) {
		if (results == null || !results.isArray()) {
			return Collections.emptyList();
		}

		List<PlaceSearchItemDto> placeSearchItemDtos = new java.util.ArrayList<>();
		for (JsonNode result : results) {
			placeSearchItemDtos.add(PlaceSearchItemDto.builder()
					.placeId(result.path("place_id").asText())
					.name(result.path("name").asText())
					.formattedAddress(result.path("formatted_address").asText())
					.build());
		}

		return placeSearchItemDtos;

	}

    @Cacheable("places")
	public Object getPlaceBriefDetails(String placeId, Long projectId) {
    	if (projectId==-1) return getGroupPlaceBriefDetails(placeId);
		TravelProject project = travelProjectMapper.findById(projectId);
		if (project == null) {
			throw new CustomException(ErrorCode.PROJECT_NOT_FOUND, "Project not found with id: " + projectId);
		}
		String projectType = project.getProjectType();
		

		if ("PERSONAL".equals(projectType)) {
			return getPersonalPlaceBriefDetails(placeId);
		} else {
			return getGroupPlaceBriefDetails(placeId);
		}
	}

    private PersonalPlaceDto getPersonalPlaceBriefDetails(String placeId) {
        String fields = "place_id,name,formatted_address,rating,user_ratings_total,price_level,types,opening_hours,photos";

        String url = UriComponentsBuilder
                .fromUriString(GOOGLE_PLACES_API_BASE_URL + "/details/json")
                .queryParam("place_id", placeId)
                .queryParam("fields", fields)
                .queryParam("key", googlePlacesApiKey)
                .queryParam("language", "ko")
                .build(false)
                .toUriString();

        try {
            String response = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(response);

            if (!"OK".equals(root.path("status").asText())) {
                throw new CustomException(ErrorCode.INVALID_REQUEST, "Google Places API error");
            }

            JsonNode result = root.path("result");

            List<String> types = objectMapper.convertValue(
                    result.path("types"),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );

            List<String> photoUrls = Collections.emptyList();
			if (result.has("photos") && result.path("photos").isArray()) {
				photoUrls = new java.util.ArrayList<>();
				for (JsonNode photoNode : result.path("photos")) {
					String photoReference = photoNode.path("photo_reference").asText();
					photoUrls.add(getGooglePhotoUrl(photoReference, 1000)); // Max width for full details
				}
			}
			
            SoloPlaceAnalysisDto analysis = aiService.analyzePlaceForSoloTravel(result);

            return PersonalPlaceDto.builder()
                    .placeId(result.path("place_id").asText())
                    .name(result.path("name").asText())
                    .formattedAddress(result.path("formatted_address").asText())
                    .rating(result.path("rating").asDouble(0.0))
                    .soloDifficulty(analysis.getSoloDifficultyScore())
                    .scoreJustification(analysis.getScoreJustification())
                    .tags(analysis.getTags())
                    .types(types)
                    .photoUrls(photoUrls)
                    .build();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "Personal place brief error: " + e.getMessage());
        }
    }


	private PlaceDto getGroupPlaceBriefDetails(String placeId) {
		String fields = "place_id,name,formatted_address,formatted_phone_number,type,photos,geometry"; // Request 'photos' and 'geometry' fields
		String url = UriComponentsBuilder.fromUriString(GOOGLE_PLACES_API_BASE_URL + "/details/json")
				.queryParam("place_id", placeId)
				.queryParam("fields", fields)
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko")
				.build(false)
				.toUriString();

		try {
			String response = restTemplate.getForObject(url, String.class);
			JsonNode root = objectMapper.readTree(response);
			String status = root.path("status").asText();

			if (!"OK".equals(status)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST,
						"Google Places API error: " + status + " - " + root.path("error_message").asText());
			}

			JsonNode result = root.path("result");

			if (result.isMissingNode()) {
				throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "Place details not found for placeId: " + placeId);
			}

			List<String> photoUrls = Collections.emptyList();
			if (result.has("photos") && result.path("photos").isArray()) {
				photoUrls = new java.util.ArrayList<>();
				for (JsonNode photoNode : result.path("photos")) {
					String photoReference = photoNode.path("photo_reference").asText();
					photoUrls.add(getGooglePhotoUrl(photoReference, 400)); // Max width for brief display
				}
			}
			
			List<String> types = Collections.emptyList();
			if (result.has("types") && result.path("types").isArray()) {
				types = objectMapper.convertValue(
						result.path("types"),
						objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
				);
			}

			// Map geometry (location)
			JsonNode geometryNode = result.path("geometry");
			JsonNode locationNode = geometryNode.path("location");
			Map<String, Object> geometryMap = null;
			if (!locationNode.isMissingNode()) {
				geometryMap = Map.of("lat", locationNode.path("lat").asDouble(), "lng",
						locationNode.path("lng").asDouble());
			}

			return PlaceDto.builder()
					.placeId(result.path("place_id").asText())
					.name(result.path("name").asText())
					.formattedAddress(result.path("formatted_address").asText())
					.formattedPhoneNumber(result.path("formatted_phone_number").asText(null))
					.types(types)
					.photoUrls(photoUrls) // Use the list of URLs
					.geometry(geometryMap)
					.build();

		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_ERROR,
					"Error calling Google Places API for brief details: " + e.getMessage());
		}
	}

	@Cacheable("places")
	public Object getPlaceFullDetails(String placeId, Long projectId) {
		TravelProject project = travelProjectMapper.findById(projectId);
		if (project == null) {
			throw new CustomException(ErrorCode.PROJECT_NOT_FOUND, "Project not found with id: " + projectId);
		}
		String projectType = project.getProjectType();
		System.out.println(projectType);
		
		if ("PERSONAL".equals(projectType)) {
			return getPersonalPlaceFullDetails(placeId);
		} else {
			return getGroupPlaceFullDetails(placeId);
		}
	}

	private PersonalPlaceDetailDto getPersonalPlaceFullDetails(String placeId) {

	    String fields = "place_id,name,formatted_address,formatted_phone_number,website,url," +
	            "rating,user_ratings_total,opening_hours,reviews,photos,geometry,business_status,types,price_level";

	    String url = UriComponentsBuilder
	            .fromUriString(GOOGLE_PLACES_API_BASE_URL + "/details/json")
	            .queryParam("place_id", placeId)
	            .queryParam("fields", fields)
	            .queryParam("key", googlePlacesApiKey)
	            .queryParam("language", "ko")
	            .build(false)
	            .toUriString();

	    try {
	        String response = restTemplate.getForObject(url, String.class);
	        JsonNode root = objectMapper.readTree(response);

	        if (!"OK".equals(root.path("status").asText())) {
	            throw new CustomException(ErrorCode.INVALID_REQUEST, "Google Places API error");
	        }

	        JsonNode result = root.path("result");

	        List<String> types = objectMapper.convertValue(
	                result.path("types"),
	                objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)
	        );

	        int reviewCount = result.path("user_ratings_total").asInt(0);
	        double rating = result.path("rating").asDouble(0.0);

	        SoloPlaceAnalysisDto analysis = aiService.analyzePlaceForSoloTravel(result);
	        
	        List<String> photoUrls = Collections.emptyList();
			if (result.has("photos") && result.path("photos").isArray()) {
				photoUrls = new java.util.ArrayList<>();
				for (JsonNode photoNode : result.path("photos")) {
					String photoReference = photoNode.path("photo_reference").asText();
					photoUrls.add(getGooglePhotoUrl(photoReference, 1000)); // Max width for full details
				}
			}
			
			//위치 정보
			JsonNode geometryNode = result.path("geometry");
			JsonNode locationNode = geometryNode.path("location");
			Map<String, Object> geometryMap = null;
			if (!locationNode.isMissingNode()) {
				geometryMap = Map.of("lat", locationNode.path("lat").asDouble(), "lng",
						locationNode.path("lng").asDouble());
			}
			
			// 오픈 시간
			OpeningHoursDto openingHoursDto = null;
			if (result.has("opening_hours")) {
				JsonNode openingHoursNode = result.path("opening_hours");
				openingHoursDto = OpeningHoursDto.builder().openNow(openingHoursNode.path("open_now").asBoolean(false))
						.weekdayText(objectMapper.convertValue(openingHoursNode.path("weekday_text"),
								objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)))
						.build();
			}
			
	        return PersonalPlaceDetailDto.builder()
	                .placeId(result.path("place_id").asText())
	                .name(result.path("name").asText())
	                .formattedAddress(result.path("formatted_address").asText())
	                .rating(rating)
	                .userRatingsTotal(reviewCount)
	                .photoUrls(photoUrls)
	                .types(types)
	                .soloScore(analysis.getSoloDifficultyScore())
	                .scoreJustification(analysis.getScoreJustification())
	                .tags(analysis.getTags())
	                .businessStatus(result.path("business_status").asText(null))
	                .geometry(geometryMap)
	                .website(result.path("website").asText(null)).url(result.path("url").asText(null))
	                .openingHours(openingHoursDto != null ? Collections.singletonList(openingHoursDto)
							: Collections.emptyList())
	                .build();

	    } catch (Exception e) {
	        throw new CustomException(ErrorCode.INTERNAL_ERROR, "Personal place full error: " + e.getMessage());
	    }
	}


	private PlaceDetailDto getGroupPlaceFullDetails(String placeId) {
		String fields = "place_id,name,formatted_address,formatted_phone_number,website,url,rating,user_ratings_total,opening_hours,reviews,photos,geometry,business_status,vicinity";
		String url = UriComponentsBuilder.fromUriString(GOOGLE_PLACES_API_BASE_URL + "/details/json")
				.queryParam("place_id", placeId)
				.queryParam("fields", fields)
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko")
				.build(false).toUriString();

		System.out.println("Google Places API Full Details Request URL: " + url);

		try {
			String response = restTemplate.getForObject(url, String.class);
			System.out.println(response);
			JsonNode root = objectMapper.readTree(response);

			String status = root.path("status").asText();
			if (!"OK".equals(status)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST,
						"Google Places API error: " + status + " - " + root.path("error_message").asText());
			}

			JsonNode result = root.path("result");
			if (result.isMissingNode()) {
				throw new CustomException(ErrorCode.PLACE_NOT_FOUND, "Place details not found for placeId: " + placeId);
			}

			// Map photos
			List<String> photoUrls = Collections.emptyList();
			if (result.has("photos") && result.path("photos").isArray()) {
				photoUrls = new java.util.ArrayList<>();
				for (JsonNode photoNode : result.path("photos")) {
					String photoReference = photoNode.path("photo_reference").asText();
					photoUrls.add(getGooglePhotoUrl(photoReference, 1000)); // Max width for full details
				}
			}

			// Map types
			List<String> types = Collections.emptyList();
			if (result.has("types") && result.path("types").isArray()) {
				types = objectMapper.convertValue(result.path("types"),
						objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
			}

			// Map opening hours
			OpeningHoursDto openingHoursDto = null;
			if (result.has("opening_hours")) {
				JsonNode openingHoursNode = result.path("opening_hours");
				openingHoursDto = OpeningHoursDto.builder().openNow(openingHoursNode.path("open_now").asBoolean(false))
						.weekdayText(objectMapper.convertValue(openingHoursNode.path("weekday_text"),
								objectMapper.getTypeFactory().constructCollectionType(List.class, String.class)))
						.build();
			}

			// Map reviews
			List<ReviewDto> reviews = Collections.emptyList();
			if (result.has("reviews") && result.path("reviews").isArray()) {
				reviews = new java.util.ArrayList<>();
				for (JsonNode reviewNode : result.path("reviews")) {
					reviews.add(ReviewDto.builder().authorName(reviewNode.path("author_name").asText())
							.authorUrl(reviewNode.path("author_url").asText(null))
							.profilePhotoUrl(reviewNode.path("profile_photo_url").asText(null))
							.rating(reviewNode.path("rating").asInt())
							.relativeTimeDescription(reviewNode.path("relative_time_description").asText())
							.text(reviewNode.path("text").asText()).time(reviewNode.path("time").asLong()).build());
				}
			}

			// Map geometry (location)
			JsonNode geometryNode = result.path("geometry");
			JsonNode locationNode = geometryNode.path("location");
			Map<String, Object> geometryMap = null;
			if (!locationNode.isMissingNode()) {
				geometryMap = Map.of("lat", locationNode.path("lat").asDouble(), "lng",
						locationNode.path("lng").asDouble());
			}

			return PlaceDetailDto.builder().placeId(result.path("place_id").asText()).name(result.path("name").asText())
					.formattedAddress(result.path("formatted_address").asText())
					.formattedPhoneNumber(result.path("formatted_phone_number").asText(null)).types(types)
					.website(result.path("website").asText(null)).url(result.path("url").asText(null))
					.rating(result.path("rating").asDouble(0.0))
					.userRatingsTotal(result.path("user_ratings_total").asInt(0))
					.openingHours(openingHoursDto != null ? Collections.singletonList(openingHoursDto)
							: Collections.emptyList())
					.reviews(reviews).photoUrls(photoUrls) // Corrected from photoReferences
					.geometry(geometryMap).businessStatus(result.path("business_status").asText(null)).build();

		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_ERROR,
					"Error calling Google Places API for full details: " + e.getMessage());
		}
	}

	private String getGooglePhotoUrl(String photoReference, int maxWidth) {
		if (photoReference == null || photoReference.isEmpty()) {
			return null;
		}
		return UriComponentsBuilder.fromUriString(GOOGLE_PLACES_API_BASE_URL + "/photo")
				.queryParam("maxwidth", maxWidth)
				.queryParam("photoreference", photoReference)
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko").build(false)
				.toUriString();
	}

	@Cacheable("solo-dining-recommendations")
	public List<PersonalPlaceDto> recommendSoloDining(double latitude, double longitude, int radius) {
		List<PlaceSearchItemDto> nearbyPlaces = searchNearbySoloDining(latitude, longitude, radius);

		List<PersonalPlaceDto> detailedPlaces = nearbyPlaces.parallelStream()
				.map(place -> {
					try {
						// -1 to indicate it's not for a specific project, just general details
						return getPersonalPlaceBriefDetails(place.getPlaceId());
					} catch (Exception e) {
						// Log error or handle it as needed
						System.err.println("Error fetching details for place " + place.getPlaceId() + ": " + e.getMessage());
						return null;
					}
				})
				.filter(place -> place != null)
				.collect(Collectors.toList());

		System.out.println("--- AI Analysis Results (Before Filtering) ---");
		detailedPlaces.forEach(place -> {
			System.out.println("Place: " + place.getName() + ", Solo Difficulty Score: " + place.getSoloDifficulty());
		});
		System.out.println("-------------------------------------------");

		// Filter places based on solo-dining friendliness (e.g., soloDifficulty >= 65) and sort by score
		return detailedPlaces.stream()
				.filter(place -> place.getSoloDifficulty() >= 65)
				.sorted(java.util.Comparator.comparing(PersonalPlaceDto::getSoloDifficulty).reversed())
				.collect(Collectors.toList());
	}

	private List<PlaceSearchItemDto> searchNearbySoloDining(double latitude, double longitude, int radius) {
		String location = latitude + "," + longitude;
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromUriString(GOOGLE_PLACES_API_BASE_URL + "/nearbysearch/json")
				.queryParam("location", location)
				.queryParam("radius", radius)
				.queryParam("type", "restaurant")
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko");

		String url = uriBuilder.build(false).toUriString();

		try {
			String response = restTemplate.getForObject(url, String.class);
			JsonNode root = objectMapper.readTree(response);
			String status = root.path("status").asText();

			if (!"OK".equals(status) && !"ZERO_RESULTS".equals(status)) {
				throw new CustomException(ErrorCode.INVALID_REQUEST,
						"Google Places API error: " + status + " - " + root.path("error_message").asText());
			}

			return mapGooglePlaceResultsToDto(root.path("results"));
		} catch (Exception e) {
			throw new CustomException(ErrorCode.INTERNAL_ERROR, "Error calling Google Places API: " + e.getMessage());
		}
	}
}
