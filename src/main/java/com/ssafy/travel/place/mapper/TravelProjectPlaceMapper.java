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

    List<TravelProjectPlace> findByProjectId(Long projectId);
    List<TravelProjectPlace> findSortedPlacesByProjectId(Map<String, Object> params);
    
    void deleteAllByProjectId(Long projectId);

}
