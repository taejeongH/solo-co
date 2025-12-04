package com.ssafy.travel.project.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.ssafy.travel.project.entity.TravelProjectInvite;

@Mapper
public interface TravelProjectInviteMapper {

    void insert(TravelProjectInvite invite);
    void deleteById(Long inviteId);
    TravelProjectInvite findByCode(String inviteCode);
    TravelProjectInvite findByProjectId(Long projectId);
    Integer existsMember(@Param("projectId") Long projectId,
            @Param("userId") Long userId);
    void addMember(@Param("projectId") Long projectId,
    		@Param("userId") Long userId);
    void deleteByProjectId(Long projectId);
}
