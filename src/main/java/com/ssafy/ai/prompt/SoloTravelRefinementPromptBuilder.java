package com.ssafy.ai.prompt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.travel.itinerary.dto.ItineraryCandidateResponseDto;
import org.springframework.stereotype.Component;

@Component
public class SoloTravelRefinementPromptBuilder {

   private final ObjectMapper objectMapper = new ObjectMapper();

   public String build(ItineraryCandidateResponseDto baseItinerary, int tripDays) {
      String itineraryJson = "";
      try {
         itineraryJson = objectMapper.writeValueAsString(baseItinerary);
      } catch (JsonProcessingException e) {
         itineraryJson = "{}";
      }

      return """
            사용자가 혼자 여행할 예정이며, 알고리즘을 통해 생성된 기본 여행 경로는 다음과 같다:
            %s

                너의 임무는 이 기본 경로를 바탕으로 '혼자 여행' 컨셉을 강화하여 사용자에게 2가지 다른 버전의 완성된 코스를 제안하는 것이다.

                ---------------------------------------------------------
                IMPORTANT RULES (반드시 지켜야 할 핵심 규칙)
                ---------------------------------------------------------
                0. **초고속 응답:**
                   - 오직 2가지 차별화된 코스(`routeType`: 1, 2)만 생성하라.
                   - 모든 설명은 1~2문장 내외로 짧고 간결하게 작성하라.

                1. **추천 보강 및 지리적 정확성:**
                   - 하루에 **딱 1개**의 새로운 장소만 추가하라.
                   - **매우 중요**: 새로운 장소는 구체적인 상호명(예: '블루보틀 성수점', '대성갈비')이어야 하며, 단순히 '멋진 카페' 같은 추상적인 명칭은 검색이 불가능하므로 지양하라.
                   - 새로운 장소는 반드시 기존 방문지의 위도/경도에서 **반경 3km 이내**에 실제로 존재해야 한다.
                   - 응답 JSON의 `places` 리스트에는 **기존 장소들과 새로운 장소가 순서에 맞게 모두 포함**되어야 한다.
                   - 새로운 장소의 `placeId`는 `null`로, `newPlace`는 `true`로 설정하라.

                2. **차별화된 2가지 코스:**
                   - `routeType` 1: 효율 및 미식 중심 (가장 합리적인 동선과 평점 높은 혼밥 맛집 추가)
                   - `routeType` 2: 힐링 및 특별 체험 (분위기 좋은 카페나 혼자 즐기기 좋은 체험 시설 추가)

                3. **평가 및 요약:**
                   - 각 코스의 특징을 `summary`에 요약하고, `recommendation`에 혼자 여행 조언을 작성하라.
                   - `soloScore`를 통해 안전성, 교통 편의성, 동선 간결성, 관광지 접근성, 혼밥 난이도를 산출하라.
                   - 점수 산출 이유는 `scoreJustification`에 짧게 적어라.

                3. **데이터 형식 유지:**
                   - 반드시 제공된 JSON 구조와 동일한 형태의 응답을 반환하라.
                ---------------------------------------------------------
                """.formatted(itineraryJson);
   }
}
