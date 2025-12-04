package com.ssafy.travel.project.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.ssafy.travel.project.entity.TravelProjectInvite;

@Mapper
public interface TravelProjectInviteMapper {

    void insert(TravelProjectInvite invite);
    TravelProjectInvite findByCode(String inviteCode);
    void deleteById(Long inviteId);
    TravelProjectInvite findByProjectId(Long projectId);
}
