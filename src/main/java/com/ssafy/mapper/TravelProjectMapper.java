package com.ssafy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.entity.TravelProject;

@Mapper
public interface TravelProjectMapper {

    List<TravelProject> findProjectsByUserId(Long userId);
}
