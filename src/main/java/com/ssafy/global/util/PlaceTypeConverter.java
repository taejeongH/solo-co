package com.ssafy.global.util;

import java.util.List;
import java.util.Map;

public class PlaceTypeConverter {

    private static final List<String> priorityOrder = List.of(
            "restaurant", "cafe", "lodging", "bank", "atm", "store",
            "bakery", "convenience_store", "pharmacy", "hospital",
            "movie_theater", "museum", "art_gallery", "library",
            "tourist_attraction", "park", "subway_station", "bus_station", "airport",
            "department_store", "shopping_mall", "bar",
            "point_of_interest", "establishment"
    );

    private static final Map<String, String> typeMap = Map.ofEntries(
            Map.entry("restaurant", "음식점"),
            Map.entry("cafe", "카페"),
            Map.entry("bar", "바"),
            Map.entry("lodging", "숙소"),
            Map.entry("bank", "은행"),
            Map.entry("atm", "ATM"),
            Map.entry("store", "상점"),
            Map.entry("tourist_attraction", "관광 명소"),
            Map.entry("park", "공원"),
            Map.entry("subway_station", "지하철역"),
            Map.entry("bus_station", "버스 정류장"),
            Map.entry("airport", "공항"),
            Map.entry("department_store", "백화점"),
            Map.entry("shopping_mall", "쇼핑몰"),
            Map.entry("bakery", "베이커리"),
            Map.entry("convenience_store", "편의점"),
            Map.entry("pharmacy", "약국"),
            Map.entry("hospital", "병원"),
            Map.entry("movie_theater", "영화관"),
            Map.entry("museum", "박물관"),
            Map.entry("art_gallery", "미술관"),
            Map.entry("library", "도서관"),
            Map.entry("point_of_interest", "관심 장소"),
            Map.entry("establishment", "시설")
    );

    public static String translatePlaceTypeToKorean(List<String> types) {
        if (types == null || types.isEmpty()) {
            return "기타";
        }

        for (String type : priorityOrder) {
            if (types.contains(type) && typeMap.containsKey(type)) {
                return typeMap.get(type);
            }
        }

        return "기타";
    }
}
