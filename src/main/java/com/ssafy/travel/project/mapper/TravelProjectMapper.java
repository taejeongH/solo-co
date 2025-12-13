package com.ssafy.travel.project.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.project.entity.TravelProject;

@Mapper
public interface TravelProjectMapper {

	TravelProject findById(@Param("projectId") Long projectId);
    List<TravelProject> findProjectsByUserIdAndType(Long userId, @Param("projectType") String projectType);
    void createProject(TravelProject project);
    void update(TravelProject project);
    void delete(Long projectId);
}
