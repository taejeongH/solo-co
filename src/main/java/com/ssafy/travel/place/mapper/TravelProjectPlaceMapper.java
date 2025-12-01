package com.ssafy.travel.place.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.place.entity.TravelProjectPlace;

@Mapper
public interface TravelProjectPlaceMapper {

    void insertPlace(TravelProjectPlace place);

    void deletePlace(@Param("placeId") Long placeId,
                     @Param("projectId") Long projectId);

    List<TravelProjectPlace> findByProjectId(Long projectId);
}
