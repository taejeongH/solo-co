package com.ssafy.travel.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.project.entity.TravelProject;

@Mapper
public interface TravelProjectMapper {

	TravelProject findById(@Param("projectId") Long projectId);
    List<TravelProject> findProjectsByUserId(Long userId);
    void createProject(TravelProject project);
}
