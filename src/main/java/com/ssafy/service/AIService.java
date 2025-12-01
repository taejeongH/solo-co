package com.ssafy.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.dto.response.ItineraryCandidateResponse;
import com.ssafy.entity.TravelProject;
import com.ssafy.entity.TravelProjectPlace;
import com.ssafy.mapper.TravelProjectMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AIService {

    @Value("${upstage.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private final TravelProjectMapper projectMapper;
    private final TravelProjectPlaceService placeService;

    // ======================================================
    // 1) JSON ONLY 출력하도록 강제된 프롬프트
    // ======================================================
    private String buildPrompt(List<TravelProjectPlace> places, int tripDays) {

        StringBuilder sb = new StringBuilder();
        sb.append("여행 기간은 " + tripDays + "일이다.\n");
        sb.append("각 일정 후보는 반드시 " + tripDays + "일치를 생성해야 한다.\n\n");
        sb.append("응답 JSON 형식:\n");
        sb.append("{ \"candidates\": [ { \"candidateId\": 1, \"days\": [ { \"day\": 1, \"places\": [\"장소1\"] } ] } ] }\n\n");

        sb.append("장소 목록:\n");
        for (TravelProjectPlace p : places) {
            sb.append("- " + p.getPlaceName());
            if (p.getPlaceAddress() != null) sb.append(" (" + p.getPlaceAddress() + ")");
            sb.append("\n");
        }

        sb.append("\n각 day에는 적절한 2~4개의 장소를 넣고, 반드시 1일부터 ")
          .append(tripDays).append("일까지 모든 day를 포함해줘.");

        return sb.toString();
    }

    // ======================================================
    // 2) Upstage API 호출
    // ======================================================
    private String callUpstageLLM(String prompt) throws Exception {

        String url = "https://api.upstage.ai/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("model", "solar-1-mini-chat");

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of(
                "role", "user",
                "content", prompt
        ));

        body.put("messages", messages);

        String jsonBody = objectMapper.writeValueAsString(body);
        HttpEntity<String> request = new HttpEntity<>(jsonBody, headers);

        return restTemplate.postForObject(url, request, String.class);
    }

    // ======================================================
    // 3) 응답에서 JSON만 추출하는 정규식
    // ======================================================
    private String extractJson(String content) {

        // JSON 객체만 추출: { ... }
        Pattern pattern = Pattern.compile("\\{[\\s\\S]*\\}", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(0);  // JSON만 반환
        }

        throw new RuntimeException("LLM output does not contain valid JSON");
    }

    // ======================================================
    // 4) Upstage 응답 파싱
    // ======================================================
    private List<ItineraryCandidateResponse> parseCandidates(String llmResponse) throws Exception {

        JsonNode root = objectMapper.readTree(llmResponse);
        String content = root.get("choices").get(0).get("message").get("content").asText();

        // JSON만 추출
        String jsonOnly = extractJson(content);

        // JSON 파싱
        JsonNode parsed = objectMapper.readTree(jsonOnly);

        JsonNode candidatesArr = parsed.get("candidates");
        if (candidatesArr == null) {
            throw new RuntimeException("No 'candidates' field found in JSON");
        }

        return objectMapper.readerForListOf(ItineraryCandidateResponse.class)
                .readValue(candidatesArr);
    }

    // ======================================================
    // 5) 외부에서 호출하는 메서드
    // ======================================================
    public List<ItineraryCandidateResponse> generateItineraryCandidates(int tripDays, List<TravelProjectPlace> places) {
        try {
            String prompt = buildPrompt(places, tripDays);
            String rawResponse = callUpstageLLM(prompt);
            return parseCandidates(rawResponse);
        } catch (Exception e) {
            throw new RuntimeException("AI itinerary generation failed", e);
        }
    }
}
