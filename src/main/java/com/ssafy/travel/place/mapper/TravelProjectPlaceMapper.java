package com.ssafy.travel.place.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Mapper
public interface TravelProjectPlaceMapper {

    void insertPlace(TravelProjectPlace place);

    void deletePlace(@Param("placeId") Long placeId,
                     @Param("projectId") Long projectId);
    
    TravelProjectPlace findByPlaceId(Long placeId);
    List<TravelProjectPlace> findByProjectId(Long projectId);
    List<TravelProjectPlace> findSortedPlacesByProjectId(Map<String, Object> params);

    boolean isPlaceExist(@Param("projectId") Long projectId, @Param("googlePlaceId") String googlePlaceId);
    
    void deleteAllByProjectId(Long projectId);
    
    List<TravelProjectPlace> findByProjectIdAndStatus(Long projectId, String status);
    void updateStatus(Long placeId, String status);

    TravelProjectPlace findByPlaceIdAndProjectId(@Param("placeId") Long placeId, @Param("projectId") Long projectId);

    TravelProjectPlace findByGooglePlaceIdAndProjectId(@Param("googlePlaceId") String googlePlaceId, @Param("projectId") Long projectId);
}
