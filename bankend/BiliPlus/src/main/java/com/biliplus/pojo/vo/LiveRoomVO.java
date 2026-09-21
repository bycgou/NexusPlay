package com.biliplus.pojo.vo;

import com.biliplus.pojo.entity.LiveRoom;
import lombok.Data;

import java.time.LocalDateTime;

/** 直播间 VO（含主播昵称与在线数） */
@Data
public class LiveRoomVO {
    private Long id;
    private String title;
    private String coverUrl;
    private Long userId;
    private String hostNickname;
    private String hostAvatar;
    private Integer categoryId;
    private Integer status;
    private Integer viewCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String streamKey;
    private String playUrl;
    private String pushUrl;
    private Integer onlineCount;

    public static LiveRoomVO from(LiveRoom room) {
        if (room == null) {
            return null;
        }
        LiveRoomVO vo = new LiveRoomVO();
        vo.setId(room.getId());
        vo.setTitle(room.getTitle());
        vo.setCoverUrl(room.getCoverUrl());
        vo.setUserId(room.getUserId());
        vo.setCategoryId(room.getCategoryId());
        vo.setStatus(room.getStatus());
        vo.setViewCount(room.getViewCount());
        vo.setStartTime(room.getStartTime());
        vo.setEndTime(room.getEndTime());
        vo.setStreamKey(room.getStreamKey());
        vo.setPlayUrl(room.getPlayUrl());
        vo.setPushUrl(room.getPushUrl());
        return vo;
    }
}
