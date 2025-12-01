package com.ssafy.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.entity.TravelProject;

@Mapper
public interface TravelProjectMapper {

	TravelProject findById(@Param("projectId") Long projectId);
    List<TravelProject> findProjectsByUserId(Long userId);
    void createProject(TravelProject project);
}
