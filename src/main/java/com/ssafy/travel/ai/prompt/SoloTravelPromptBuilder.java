package com.ssafy.travel.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Component
public class SoloTravelPromptBuilder {

    public String build(List<TravelProjectPlace> places, int tripDays) {

        StringBuilder list = new StringBuilder();
        for (TravelProjectPlace p : places) {
            list.append(String.format(
                "- placeId: %d, placeName: %s",
                p.getPlaceId(),
                p.getPlaceName()
            ));
        }

        return """
        사용자가 혼자 여행할 때 방문할 예정인 장소 목록은 다음과 같다:

        %s

        여행 기간은 총 %d일이다.

        ---------------------------------------------------------
        IMPORTANT RULES (반드시 지켜야 함)
        ---------------------------------------------------------
        1) placeId는 서버 DB의 고유 식별자(Long)이다.
        2) 입력으로 제공된 placeId는 절대 수정/변경/재생성/재번호매기기 하지 마라.
        3) 기존 장소(newPlace=false)는 반드시 "입력으로 받은 placeId"를 그대로 사용해야 한다.
        4) 새로운 장소(newPlace=true)를 추천하는 경우:
           - placeId는 반드시 JSON null 로 설정한다. (문자열 "null" 금지)
           - newPlace 는 반드시 true
        5) 숫자 placeId를 새로 생성하는 행위는 절대 금지한다.
        6) 아래 PLACE OBJECT 규칙을 하나라도 어기면 결과는 무효이다.
           - days 배열의 구조는 이미 완성되어 있다.
           - 너는 days 배열 안의 "places" 배열의 값만 채운다.
           - days 배열 자체를 늘리거나 줄이거나 새로 만들면 안 된다.
        7) summary 필드는 절대 빈 문자열로 두지 말고, 각 코스의 특징을 1~2문장으로 반드시 작성하라.
        8) recommendation 필드는 절대 빈 문자열로 두지 말고, 혼자 여행하는 사람에게
           도움이 되는 구체적인 조언을 반드시 작성하라.
        9) soloScore의 모든 항목은 반드시 1~100 사이의 정수로 채워야 한다.

        ---------------------------------------------------------
        PLACE OBJECT 규칙 (가장 중요)
        ---------------------------------------------------------
        - days[].places[] 안의 각 place 객체는 반드시 아래 5개 필드를 "모두 포함"해야 한다.
        - 기존 장소(newPlace=false)도 placeName를 절대 생략하면 안 된다.
        - 어떤 경우에도 필드 누락, 축약, 일부만 출력은 금지한다.

        각 place 객체 형식 (필드명/필드수 절대 변경 금지):
        {
          "placeId": number | null,
          "placeName": string,
          "newPlace": boolean
        }

        ---------------------------------------------------------
        NEW PLACE (실존 장소 강제 규칙)
        ---------------------------------------------------------
        새로운 장소(placeId=null, newPlace=true)는 반드시
        Google Places에서 실제로 검색 가능한
        구체적인 단일 장소명 이어야 한다.
        
        이 서비스에서 장소(place)란 다음 조건을 모두 만족해야 합니다.

		- 하나의 명확한 지점(Point of Interest)이어야 합니다.
		- 공식 명칭이 존재하며 Google 지도에서 검색 가능한 장소여야 합니다.
		- Google Places API (Text Search)로 조회 가능한 장소여야 합니다.
		- 거리, 동네, 지역, 구역과 같은 포괄적인 개념은 장소로 취급하지 않습니다.

        ❌ 금지 예시:
        - 새로운 카페
        - 감성 카페
        - 힐링 스팟
        - 전망 좋은 장소
        - 유명한 공원
        - 핫플
        - 거리, 골목, 산책로 (예: 익선동 한옥거리, 명동 거리)
		- 동네, 지역, 구역 (예: 홍대, 성수동, 북촌)
		- 하나의 지도 핀으로 표시할 수 없는 장소
		- Google Places API에서 검색되지 않는 장소


        이 규칙을 위반하면 결과는 즉시 폐기된다.

        ---------------------------------------------------------
        출력 목적
        ---------------------------------------------------------
        총 4개의 코스를 JSON 배열로 생성하라.
        routeType은 아래 4개로 고정한다:

        - "standard_1"
        - "standard_2"
        - "standard_3"
        - "creative_optimized"

        규칙:
        1) standard_1~3
            - 반드시 입력으로 제공된 장소만 사용
            - placeId는 그대로 유지
            - day별 2~4개 장소
            - 여행 기간 %d일을 모두 포함

        2) creative_optimized
            - 반드시 최소 1개 이상의 새로운 장소를 포함해야 한다.
            - 새로운 장소는 입력에 없는 장소여야 한다.
            - 새로운 장소는 placeId=null, newPlace=true로 표현한다.
            - 혼자 여행 기준으로 안전성, 이동 편의성, 혼밥 난이도 개선에 초점을 둔다.
            - routeMeta.reason에 변경 이유를 명확히 작성한다.

        ---------------------------------------------------------
        JSON 템플릿 (절대 구조 수정 금지)
        ---------------------------------------------------------
        [
          {
            "routeType": "standard_1",
            "days": %s,
            "soloScore": {
              "totalScore": 0,
              "safety": 0,
              "transportAccessibility": 0,
              "routeSimplicity": 0,
              "landmarkAccessibility": 0,
              "soloDiningDifficulty": 0
            },
            "summary": "",
            "recommendation": "",
            "routeMeta": {
              "reason": ""
            }
          },
          {
            "routeType": "standard_2",
            "days": %s,
            "soloScore": {
              "totalScore": 0,
              "safety": 0,
              "transportAccessibility": 0,
              "routeSimplicity": 0,
              "landmarkAccessibility": 0,
              "soloDiningDifficulty": 0
            },
            "summary": "",
            "recommendation": "",
            "routeMeta": {
              "reason": ""
            }
          },
          {
            "routeType": "standard_3",
            "days": %s,
            "soloScore": {
              "totalScore": 0,
              "safety": 0,
              "transportAccessibility": 0,
              "routeSimplicity": 0,
              "landmarkAccessibility": 0,
              "soloDiningDifficulty": 0
            },
            "summary": "",
            "recommendation": "",
            "routeMeta": {
              "reason": ""
            }
          },
          {
            "routeType": "creative_optimized",
            "days": %s,
            "soloScore": {
              "totalScore": 0,
              "safety": 0,
              "transportAccessibility": 0,
              "routeSimplicity": 0,
              "landmarkAccessibility": 0,
              "soloDiningDifficulty": 0
            },
            "summary": "",
            "recommendation": "",
            "routeMeta": {
              "reason": ""
            }
          }
        ]

        ---------------------------------------------------------
        출력 규칙
        ---------------------------------------------------------
        1) JSON 외의 다른 텍스트 출력 금지
        2) 반드시 한 줄(minified) JSON으로 출력
        3) null은 문자열이 아닌 JSON null로 출력
        4) summary, recommendation, soloScore, routeMeta.reason은 절대 빈 값 금지

        ---------------------------------------------------------
        최종 출력: 한 줄 JSON only
        ---------------------------------------------------------
        """.formatted(
            list,
            tripDays,
            tripDays,
            buildDaysTemplate(tripDays),
            buildDaysTemplate(tripDays),
            buildDaysTemplate(tripDays),
            buildDaysTemplate(tripDays)
        );
    }

    private String buildDaysTemplate(int tripDays) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 1; i <= tripDays; i++) {
            sb.append("""
                {"day":%d,"places":[]}
            """.formatted(i));
            if (i < tripDays) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
