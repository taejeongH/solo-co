package com.ssafy.ai.prompt;

import java.util.List;

import com.ssafy.tour.dto.TourApiResponseDto;
import org.springframework.stereotype.Component;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Component
public class GroupTravelPromptBuilder {

    public String build(List<TravelProjectPlace> places, int tripDays, List<TourApiResponseDto.Item> newPlaces) {

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

        StringBuilder newPlacesList = new StringBuilder();
        if (newPlaces != null && !newPlaces.isEmpty()) {
            newPlacesList.append("""
            
            ---------------------------------------------------------
            추가 추천 장소 (한국관광공사 제공)
            ---------------------------------------------------------
            아래 목록은 사용자의 여행지에 맞춰 추천하는 새로운 관광 명소들이다.
            일정이 비어 보인다면, 이 장소들을 자연스럽게 코스에 포함시키는 것을 적극적으로 고려하라.
            """);
            for (TourApiResponseDto.Item item : newPlaces) {
                newPlacesList.append(String.format(
                    "- placeName: %s, address: %s, latitude: %f, longitude: %f%n",
                    item.getTitle(),
                    item.getAddress(),
                    item.getLatitude(),
                    item.getLongitude()
                ));
            }
        }

        return """
        당신은 전문 여행 플래너입니다. 사용자가 제공한 장소 목록과 여행 기간을 바탕으로 최적화된 여행 코스 4개를 제안하는 것이 당신의 임무입니다.

        [사용자 정보]
        - 방문 희망 장소:
        %s
        - 여행 기간: 총 %d일
        %s

        ---------------------------------------------------------
        IMPORTANT RULES (반드시 지켜야 할 핵심 규칙)
        ---------------------------------------------------------
        0.  **경로 최적화 규칙:**
            -   제공된 장소들의 위도, 경도(`latitude`, `longitude`) 정보를 반드시 사용하여 이동 거리가 최소화되도록 경로를 최적화해야 합니다.
            -   지리적으로 가까운 장소들은 같은 날 방문하도록 일정을 구성하세요.

        1.  **장소(Place) 규칙:**
            -   입력으로 제공된 장소의 `placeId`는 절대 변경하거나 새로 만들지 마세요.
            -   새로운 장소를 추천할 경우, `placeId`는 반드시 `null`로, `newPlace`는 `true`로 설정해야 합니다.
            -   새롭게 추천하는 장소는 '홍대 거리'나 '분위기 좋은 카페'처럼 모호한 장소가 아닌, Google 지도에서 검색 가능한 실제 장소의 정확한 명칭이어야 합니다.

        2.  **코스 생성 핵심 원칙 (총 4개):**
            -   먼저, 하루에 2~4개의 장소를 방문한다고 가정했을 때, 제공된 장소 목록이 %d일의 여행 기간을 채우기에 충분한지 평가하세요.
            -   하루에 방문하는 장소는 최소 2개, 최대 4개로 구성해야 합니다.

            -   **장소 목록이 충분할 경우:**
                -   `routeType` 1, 2, 3 (3개): 제공된 장소만을 사용하여 경로 최적화에 집중한 코스를 만드세요.
                -   `routeType` 4 (1개): 제공된 장소에 더해, 그룹 여행에 어울리는 새로운 장소를 1~2개 추가하여 창의적인 코스를 만드세요. `reason` 필드에 새로운 장소를 추가한 이유를 설명해야 합니다.

            -   **장소 목록이 부족하거나 비어있을 경우:**
                -   당신의 주된 임무는 여행 기간(%d일) 전체를 채울 수 있는 완전한 일정을 만드는 것입니다.
                -   `routeType` 1, 2, 3, 4 (4개 모두): 사용자가 제공한 장소가 있다면 그것을 포함하고, 부족한 일정은 그룹 여행의 특성과 기존 장소의 위치/테마를 고려하여 새로운 추천 장소로 채워넣으세요.
                -   모든 코스가 여행 기간 전체를 꽉 채우도록 충분한 수의 새로운 장소를 반드시 추가해야 합니다. `reason` 필드에 코스 구성에 대한 설명을 작성하세요.

        3.  **필수 값 규칙:**
            -   `summary` 필드는 각 코스의 특징을 1~2문장으로 요약하여 반드시 작성해야 합니다.
            -   `routeType` 필드는 각 코스에 1, 2, 3, 4를 순서대로 할당해야 합니다.
        ---------------------------------------------------------
        """.formatted(list, tripDays, newPlacesList, tripDays, tripDays);
    }


}
