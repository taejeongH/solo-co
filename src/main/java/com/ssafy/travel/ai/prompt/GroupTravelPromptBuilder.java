package com.ssafy.travel.ai.prompt;

import java.util.List;

import org.springframework.stereotype.Component;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Component
public class GroupTravelPromptBuilder {

    public String build(List<TravelProjectPlace> places, int tripDays) {

        StringBuilder list = new StringBuilder();
        for (TravelProjectPlace p : places) {
            list.append(String.format(
                "- placeId: %d, placeName: %s, latitude: %f, longitude: %f%n",
                p.getPlaceId(),
                p.getPlaceName(),
                p.getLatitude(),
                p.getLongitude()
            ));
        }

        return """
        사용자가 그룹 여행을 할 예정이며, 방문할 장소 목록(위도, 경도 포함)과 여행 기간은 다음과 같다:
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
            -   **`routeType` 1, 2, 3 (3개):**
                -   `routeType` 필드에 각각 1, 2, 3을 순서대로 할당한다.
                -   반드시 입력으로 제공된 장소만 사용해야 한다. (새로운 장소 추천 금지)
                -   여행 기간(%d일)을 모두 활용하여 하루에 2~4개의 장소로 일정을 구성한다.
            -   **`routeType` 4 (1개):**
                -   `routeType` 필드에 4를 할당한다.
                -   이 코스는 다음 두 가지 조건을 **모두** 만족해야 한다:
                    1. 입력으로 제공된 기존 장소들을 활용할 것.
                    2. 그룹 여행에 더 적합한 **최소 1개 이상의 새로운 장소를 반드시 추가**할 것.
                -   `reason` 필드에 왜 새로운 장소를 추가했는지 등 최적화 이유를 명확하게 작성한다.

        3.  **필수 값 규칙:**
            -   `summary` 필드는 각 코스의 특징을 1~2문장으로 요약하여 반드시 작성한다.
        ---------------------------------------------------------
        """.formatted(list, tripDays, tripDays);
    }


}
