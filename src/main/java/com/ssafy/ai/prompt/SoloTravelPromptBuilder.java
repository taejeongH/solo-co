package com.ssafy.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Component
public class SoloTravelPromptBuilder {

    public String build(List<TravelProjectPlace> places, int tripDays) {

        StringBuilder list = new StringBuilder();
        for (TravelProjectPlace p : places) {
            list.append(String.format(
                    "- placeId: %d, placeName: %s, latitude: %f, longitude: %f%n",
                    p.getPlaceId(),
                    p.getPlaceName(),
                    p.getLatitude(),
                    p.getLongitude()));
        }

        return """
                사용자가 혼자 여행할 예정이며, 방문할 장소 목록(위도, 경도 포함)과 여행 기간은 다음과 같다:
                - 방문 장소:
                %s
                - 여행 기간: 총 %d일

                너의 임무는 이 정보를 바탕으로 총 4개의 여행 코스 제안을 생성하는 것이다.

                ---------------------------------------------------------
                IMPORTANT RULES (반드시 지켜야 할 핵심 규칙)
                ---------------------------------------------------------
                0.  **경로 최적화 규칙:**
                    -   제공된 장소들의 위도, 경도(`latitude`, `longitude`) 정보를 반드시 사용하여 이동 거리가 최소화되도록 경로를 최적화해야 한다.
                    -   지리적으로 가까운 장소들은 같은 날 방문하도록 일정을 구성하라.

                1.  **장소(Place) 규칙:**
                    -   입력으로 제공된 장소의 `placeId`는 절대 변경하거나 새로 만들지 마라.
                    -   새로운 장소를 추천할 경우, `placeId`는 반드시 `null`로, `newPlace`는 `true`로 설정해야 한다.
                    -   새롭게 추천하는 장소는 '홍대 거리'나 '분위기 좋은 카페'처럼 모호한 장소가 아닌, Google 지도에서 검색 가능한 실제 장소의 정확한 명칭이어야 한다.

                2.  **코스 생성 규칙 (총 4개):**
                    -   너의 목표는 사용자가 제공한 장소를 기반으로, %d일의 여행 기간을 모두 채우는 완전한 여행 코스 4개를 만드는 것이다.
                    -   하루에 방문하는 장소는 최소 2개, 최대 4개로 구성해야 한다.
                    -   **장소 보충 규칙:** 만약 사용자가 제공한 장소의 수가 여행 기간을 채우기에 부족하다면, **모든 `routeType`(1, 2, 3, 4)에서** 혼자 여행에 적합한 새로운 장소를 추가하여 일정을 채워야 한다. 사용자가 제공한 장소는 모두 활용하는 것을 우선으로 하되, 일정이 비지 않도록 새로운 장소를 적극적으로 추천하라.
                    -   `routeType` 4에서는 다른 코스들과 차별화되도록, 특히 더 창의적이거나 특별한 새로운 장소를 1개 이상 포함하도록 노력하라.
                    -   `routeMeta.reason` 필드에 왜 새로운 장소를 추가했는지 등 최적화 이유를 명확하게 작성한다.

                3.  **필수 값 규칙:**
                    -   `summary` 필드는 각 코스의 특징을 1~2문장으로 요약하여 반드시 작성한다.
                    -   `recommendation` 필드는 혼자 여행하는 사람에게 도움이 될 구체적인 조언을 반드시 작성한다.

                4.  **soloScore 점수 계산 규칙 (매우 중요):**
                    -   **임의의 숫자를 절대 사용하지 마라.** 모든 점수는 아래의 기준에 따라 논리적으로 계산되어야 한다.
                    -   너의 방대한 지식을 활용하여 각 장소의 주소, 종류, 일반적인 특징을 바탕으로 점수를 산출하라.

                    -   **`totalScore` (종합 점수):**
                        -   `totalScore`는 아래 5개 항목 점수의 산술 평균으로 계산한다.
                        -   `safety`, `transportAccessibility`, `routeSimplicity`, `landmarkAccessibility`
                        -   **주의:** `soloDiningDifficulty`는 점수가 낮을수록 좋은 항목이므로, 평균 계산 시에는 `(100 - soloDiningDifficulty)` 값으로 변환하여 사용한다.

                    -   **`safety` (안전성):**
                        -   해당 장소의 주소와 지역의 일반적인 치안 수준을 고려하라.
                        -   주변에 유흥업소가 많거나, 밤늦게 혼자 다니기 위험한 곳은 낮은 점수를 부여한다.
                        -   관광객이 많고 잘 알려진 공공장소는 높은 점수를 부여한다.

                    -   **`transportAccessibility` (교통 편의성):**
                        -   장소의 위치(위도/경도)를 기반으로 대중교통(지하철, 버스) 접근성을 평가하라.
                        -   주요 역에서 가깝거나 버스 노선이 많으면 높은 점수를 부여한다.
                        -   도보 이동이 길거나 교통편이 불편한 외진 곳은 낮은 점수를 부여한다.

                    -   **`routeSimplicity` (경로 단순성):**
                        -   하루 동안의 코스 전체를 평가하는 점수이다.
                        -   방문할 장소들이 지리적으로 가깝고, 이동 경로가 단순하고 효율적일수록 높은 점수를 부여한다.
                        -   하루에 방문하는 장소 수가 적절하고(2~3개) 동선이 깔끔하면 점수를 높게 책정한다.

                    -   **`landmarkAccessibility` (관광지 접근성):**
                        -   혼자서도 그 장소를 온전히 즐길 수 있는지를 평가한다.
                        -   예약 없이 방문 가능하거나, 1인용 티켓 구매가 용이한 곳은 높은 점수를 부여한다.
                        -   그룹 단위 예약만 가능하거나, 혼자 즐기기 어색한 곳(예: 대규모 그룹 활동 장소)은 낮은 점수를 부여한다.

                    -   **`soloDiningDifficulty` (혼밥 난이도 - 낮을수록 좋음):**
                        -   해당 장소 또는 그 주변의 식사 환경을 평가한다.
                        -   점수가 낮을수록 혼자 식사하기 쉽다는 의미이다.
                        -   카페, 푸드코트, 패스트푸드, 1인 식당이 많은 지역은 낮은 점수(10~30점)를 부여한다.
                        -   주로 2인 이상 테이블만 있거나, 격식 있는 레스토랑, 단체 손님 위주의 식당이 많은 곳은 높은 점수(70~90점)를 부여한다.

                    -   **`scoreJustification` (점수 산출 이유):**
                        -   `safety`, `transportAccessibility`, `routeSimplicity`, `landmarkAccessibility`, `soloDiningDifficulty` 각각의 점수를 어떻게 산출했는지에 대한 구체적인 이유를 한 문장으로 작성해야 한다.
                        -   `scoreJustification` 필드에 각 점수 항목의 이름(`safety` 등)을 key로, 해당 이유를 value로 하는 Map 형태로 채워야 한다.
                ---------------------------------------------------------
                """
                .formatted(list, tripDays, tripDays);
    }

}
