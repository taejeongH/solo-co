package com.ssafy.mapper;

import com.ssafy.dto.response.TravelProjectResponseDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TravelProjectMapper {

    List<TravelProjectResponseDto> findProjectsByUserId(Long userId);
}
