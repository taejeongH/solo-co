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
import com.ssafy.place.domain.PlaceContext;
import com.ssafy.place.dto.PersonalPlaceDetailDto;
import com.ssafy.place.dto.PersonalPlaceDto;
import com.ssafy.place.dto.PlaceDetailDto;
import com.ssafy.place.dto.PlaceDetailDto.OpeningHoursDto;
import com.ssafy.place.dto.PlaceDetailDto.ReviewDto;
import com.ssafy.place.dto.PlaceDto;
import com.ssafy.place.dto.PlaceSearchItemDto;
import com.ssafy.place.dto.PlaceSearchResponseDto;
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

	@Cacheable("places")
	public PlaceSearchResponseDto searchPlaces(String query, String location, String type, String nextPageToken) {
		UriComponentsBuilder uriBuilder = UriComponentsBuilder
				.fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/textsearch/json")
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
                .fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/details/json")
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
			
			int soloScore = calculateSoloScore(
				    result.path("rating").asDouble(0.0),
				    result.path("user_ratings_total").asInt(0),
				    result.path("price_level").asInt(-1),
				    result.path("opening_hours").path("open_now").asBoolean(false),
				    types
				);


            List<String> tags = extractSoloTags(
                    result.path("user_ratings_total").asInt(0),
                    result.path("price_level").asInt(2),
                    result.path("rating").asDouble(0.0),
                    types
            );

            return PersonalPlaceDto.builder()
                    .placeId(result.path("place_id").asText())
                    .name(result.path("name").asText())
                    .formattedAddress(result.path("formatted_address").asText())
                    .rating(result.path("rating").asDouble(0.0))
                    .soloDifficulty(soloScore)
                    .tags(tags)
                    .types(types)
                    .photoUrls(photoUrls)
                    .build();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.INTERNAL_ERROR, "Personal place brief error: " + e.getMessage());
        }
    }


	private PlaceDto getGroupPlaceBriefDetails(String placeId) {
		String fields = "place_id,name,formatted_address,formatted_phone_number,type,photos"; // Request 'photos' field
		String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/details/json")
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

			return PlaceDto.builder()
					.placeId(result.path("place_id").asText())
					.name(result.path("name").asText())
					.formattedAddress(result.path("formatted_address").asText())
					.formattedPhoneNumber(result.path("formatted_phone_number").asText(null))
					.types(types)
					.photoUrls(photoUrls) // Use the list of URLs
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
	            .fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/details/json")
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
	        int priceLevel = result.path("price_level").asInt(-1);
	        boolean isOpenNow = result.path("opening_hours").path("open_now").asBoolean(false);

	        int difficulty = calculateSoloScore(rating, reviewCount, priceLevel, isOpenNow, types);
	        List<String> tags = extractSoloTags(reviewCount, priceLevel, rating, types);
	        
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
	                .soloScore(difficulty)
	                .tags(tags)
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
		String url = UriComponentsBuilder.fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/details/json")
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
		return UriComponentsBuilder.fromHttpUrl(GOOGLE_PLACES_API_BASE_URL + "/photo")
				.queryParam("maxwidth", maxWidth)
				.queryParam("photoreference", photoReference)
				.queryParam("key", googlePlacesApiKey)
				.queryParam("language", "ko").build(false)
				.toUriString();
	}
	
	// ✅ 혼밥 난이도 계산 (현실 기준)
	private int calculateSoloScore(double rating, int reviewCount, int priceLevel, boolean isOpenNow, List<String> types) {
		if (types == null) types = List.of();
	    PlaceContext ctx = classifyPlaceContext(types, priceLevel);

	    int score;
	    // 1️⃣ 컨텍스트별 베이스 점수
	    switch (ctx) {
	        case FAST_FOOD:
	            score = 88; // 노브랜드, 맥날, 버거킹 등
	            break;
	        case CAFE:
	            score = 82;
	            break;
	        case BAR_PUB:
	            score = 72;
	            break;
	        case CASUAL_RESTAURANT:
	            score = 60;
	            break;
	        case FINE_DINING:
	            score = 18;
	            break;
	        default: // UNKNOWN
	            score = 30;
	    }
	    
	    System.out.println(ctx);
	    System.out.println(priceLevel);

	    // 2️⃣ 가격대 (싸면 혼밥 편함, 비쌀수록 부담)
	    // 0: 모름, 1: 싸다, 2: 보통, 3: 비쌈, 4: 매우 비쌈
	    if (priceLevel == 1) {
	        score += 6;
	    } else if (priceLevel == 3) {
	        score -= 8;
	    } else if (priceLevel >= 4) {
	        score -= 12;
	    }

	    // 3️⃣ 리뷰 수 (붐비는 맛집/핫플 패널티)
	    // 컨텍스트에 따라 다르게 적용
	    if (reviewCount > 0) {
	        if (ctx == PlaceContext.CASUAL_RESTAURANT || ctx == PlaceContext.FINE_DINING) {
	            // 일반/고급 레스토랑은 붐비면 혼밥 부담 ↑
	            if (reviewCount <= 30)       score += 5;   // 사람 적은 곳 → 오히려 혼밥 편함
	            else if (reviewCount <= 100) /* 변화 없음 */ ;
	            else if (reviewCount <= 300) score -= 6;
	            else                         score -= 10;
	        } else if (ctx == PlaceContext.FAST_FOOD || ctx == PlaceContext.CAFE) {
	            // 패스트푸드/카페는 사람 좀 있어도 괜찮음
	            if (reviewCount <= 10)       score -= 4;   // 너무 휑하면 오히려 어색
	            else if (reviewCount >= 200) score += 2;   // 적당히 사람 많은 게 자연스러움
	        }
	    }

	    // 4️⃣ 평점
	    // 너무 높고(4.6↑) 리뷰 많으면 "핫플/맛집" 느낌 → 레스토랑 계열에서만살짝 깎음
	    if (rating > 0) {
	        if (rating <= 3.0) {
	            score -= 6; // 이상한 가게일 수 있음
	        } else if (rating >= 4.6 && reviewCount >= 200 &&
	                (ctx == PlaceContext.CASUAL_RESTAURANT || ctx == PlaceContext.FINE_DINING)) {
	            score -= 6;
	        }
	    }

	    // 5️⃣ 영업 중 여부 (isOpenNow)
	    // 지금 열려 있고, 레스토랑 + 리뷰 많으면 피크 타임일 확률 → 살짝 패널티
	    if (isOpenNow && reviewCount >= 200 &&
	            (ctx == PlaceContext.CASUAL_RESTAURANT || ctx == PlaceContext.FINE_DINING)) {
	        score -= 3;
	    }
	    
	    boolean isFastTurnoverRestaurant =
	            types.contains("restaurant") &&
	            reviewCount >= 200 &&
	            rating >= 4.2 &&
	            priceLevel <= 2;

	    if (isFastTurnoverRestaurant) {
	        score += 12;
	    }
	    
	    if (ctx == PlaceContext.FINE_DINING) {
	        score = Math.min(score, 25);
	    }

	    // 6️⃣ 보정: 0 ~ 100 사이로 클램핑
	    score = Math.max(0, Math.min(100, score));

	    return score;
	}


	// ✅ 태그 추출 (AI 없이)
	private List<String> extractSoloTags(int reviewCount, int priceLevel, double rating, List<String> types) {

	    List<String> tags = new java.util.ArrayList<>();

	    // ✅ 키오스크
	    if (types.contains("fast_food") || priceLevel == 1) {
	        tags.add("키오스크 주문");
	    }

	    // ✅ 1인석
	    if (types.contains("bar") || types.contains("cafe") || types.contains("fast_food")) {
	        tags.add("1인석 존재");
	    }

	    // ✅ 빠른 주문
	    if (types.contains("fast_food") || (reviewCount > 150 && priceLevel <= 2)) {
	        tags.add("빠른 주문");
	    }

	    // ✅ 조용한 분위기
	    if (types.contains("cafe") || (reviewCount < 80 && rating >= 4.3)) {
	        tags.add("조용한 분위기");
	    }

	    // ✅ 혼자 방문 편함
	    if (
	        types.contains("fast_food") ||
	        types.contains("bar") ||
	        (reviewCount >= 50 && priceLevel <= 2)
	    ) {
	        tags.add("혼자 방문 편함");
	    }

	    return tags;
	}
	

	private PlaceContext classifyPlaceContext(List<String> types, int priceLevel) {
	    if (types == null) types = List.of();

	    // 전부 소문자로 맞추기
	    List<String> lower = types.stream()
	            .map(String::toLowerCase)
	            .collect(Collectors.toList());
	    
	    if (priceLevel<=0) {
	        return PlaceContext.UNKNOWN;
	    }
	    
	    // 패스트푸드 계열
	    if (lower.contains("fast_food") || lower.contains("meal_takeaway")) {
	        return PlaceContext.FAST_FOOD;
	    }

	    // 카페
	    if (lower.contains("cafe")) {
	        return PlaceContext.CAFE;
	    }

	    // 바/펍
	    if (lower.contains("bar") || lower.contains("pub") || lower.contains("night_club")) {
	        return PlaceContext.BAR_PUB;
	    }

	    // 파인다이닝 느낌 (비싼 restaurant)
	    if (lower.contains("restaurant") && priceLevel >= 3) {
	        return PlaceContext.FINE_DINING;
	    }
	    
	    // 그냥 일반 식당
	    if (lower.contains("restaurant")) {
	        return PlaceContext.CASUAL_RESTAURANT;
	    }
	    
	    

	    return PlaceContext.UNKNOWN;
	}


}
