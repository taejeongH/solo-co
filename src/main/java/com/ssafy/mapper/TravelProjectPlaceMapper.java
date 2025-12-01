package com.ssafy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.entity.TravelProjectPlace;

@Mapper
public interface TravelProjectPlaceMapper {

    void insertPlace(TravelProjectPlace place);

    void deletePlace(@Param("placeId") Long placeId,
                     @Param("projectId") Long projectId);

    List<TravelProjectPlace> findByProjectId(Long projectId);
}
