package com.ssafy.ai.prompt;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;

@Component
public class PlaceAnalysisPromptBuilder {

    public String build(JsonNode placeDetails) {
        String name = placeDetails.path("name").asText();
        String address = placeDetails.path("formatted_address").asText();
        double rating = placeDetails.path("rating").asDouble(0.0);
        int reviewCount = placeDetails.path("user_ratings_total").asInt(0);
        int priceLevel = placeDetails.path("price_level").asInt(-1); // 1~4, -1은 정보 없음

        List<String> types = new ArrayList<>();
        if (placeDetails.has("types") && placeDetails.path("types").isArray()) {
            for (JsonNode typeNode : placeDetails.path("types")) {
                types.add(typeNode.asText());
            }
        }
        String typesString = String.join(", ", types);

        StringBuilder reviewsText = new StringBuilder();
        if (placeDetails.has("reviews") && placeDetails.path("reviews").isArray()) {
            int count = 0;
            for (JsonNode reviewNode : placeDetails.path("reviews")) {
                if (count < 5) {
                    reviewsText.append("- \"").append(reviewNode.path("text").asText().replace("\n", " "))
                            .append("\"\n");
                    count++;
                }
            }
        }

        return """
                당신은 특정 장소에 대해 '혼자 여행하기 좋은 정도'를 분석하는 노련한 분석가입니다.
                제공된 모든 정보를 종합적으로 고려하여, 아래의 핵심 원칙에 따라 분석을 수행하고 결과를 JSON으로 반환하세요.

                [분석 대상 장소 정보]
                - 이름: %s
                - 주소: %s
                - 장소 유형: %s
                - 평점: %.1f/5.0
                - 리뷰 수: %d개
                - 가격대: %d (1: 저렴, 2: 보통, 3: 비쌈, 4: 매우 비쌈, -1: 정보 없음)
                - 최근 리뷰 (최대 5개):
                %s

                ---------------------------------------------------------
                핵심 분석 원칙 (Scoring Principles)
                ---------------------------------------------------------
                1. 장소 유형(types) 분석:
                   - 장소의 본질적인 목적을 파악합니다. 카페, 바, 1인 식당 특화(라멘, 스시) 등은 혼자 방문하기에 본질적으로 유리합니다. 반면, 가족 레스토랑, 연회장 등은 불리합니다. 당신의 지식을 활용하여 각 유형의 일반적인 특성을 고려하세요.

                2. 리뷰(reviews) 심층 분석:
                   - 리뷰는 사용자의 실제 경험이 담긴 가장 중요한 정보입니다. 텍스트의 문맥과 뉘앙스를 깊이 파악해야 합니다.
                   - '혼자'라는 키워드가 긍정적 경험("혼자 가기 좋아요")과 연결되는지, 부정적 경험("혼자 가니 좀 그랬어요")과 연결되는지 파악하세요.
                   - '웨이팅', '붐빔', '시끄러움' 등의 키워드가 얼마나 자주 등장하며, 이것이 혼자 방문객에게 치명적인 단점인지 분석하세요.
                   - '조용함', '아늑함', '카운터석', '바 좌석' 등의 키워드는 혼자 방문하기에 긍정적인 신호입니다.

                3. 가격대(priceLevel)와 분위기 연관 분석:
                   - 가격대는 장소의 분위기를 추론하는 중요한 단서입니다. 가격이 높을수록(3, 4) 격식 있는 분위기일 가능성이 높아 혼자 방문하기 부담스러울 수 있습니다.
                   - 가격 정보가 없다면(-1), 리뷰에 포함된 '가성비', '비싸다', '기념일' 등의 단서나 장소 유형(예: 'fine_dining')을 통해 분위기를 추론하세요.

                4. 종합 판단 및 근거 제시:
                   - 위 세 가지 핵심 원칙을 종합적으로 고려하여 최종 'soloDifficultyScore'를 0점에서 100점 사이로 결정합니다. 점수가 높을수록 혼자 방문하기 좋음을 의미합니다.
                   - 'scoreJustification' 필드에 어떤 원칙(유형, 리뷰, 가격)에 근거하여 최종 점수를 어떻게 매겼는지 구체적이고 논리적인 설명을 한두 문장으로 작성해야 합니다. (예: "카페 유형이라 기본적으로 점수가 높지만, 리뷰에 웨이팅이 길다는 언급이 많아 점수를 일부 조정했습니다.")

                5. 어조 및 스타일 (Tone and Style):
                   - 모든 설명과 태그는 반드시 한국어로 작성합니다.
                   - 'scoreJustification'을 포함한 모든 설명은 전문가적이고 객관적인 어조를 유지하며, 반드시 정중한 존댓말(~했습니다, ~합니다)로 통일해야 합니다.

                [결과물 (Output)]
                - soloDifficultyScore: 0~100 사이의 정수.
                - tags: 분석 내용을 요약하는 핵심 태그 3~5개 (한국어).
                - scoreJustification: 점수 산출에 대한 구체적인 근거 설명 (한국어, 존댓말).

                [JSON 출력 규칙]
                - 분석 결과는 반드시 아래와 같은 JSON 형식으로만 반환해야 합니다.
                - JSON 외에 어떤 설명이나 추가적인 텍스트도 포함해서는 안 됩니다.
                ---------------------------------------------------------
                """
                .formatted(name, address, typesString, rating, reviewCount, priceLevel, reviewsText.toString());
    }
}
