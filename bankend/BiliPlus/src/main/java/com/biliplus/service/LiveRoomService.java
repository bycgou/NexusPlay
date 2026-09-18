package com.biliplus.service;

import com.biliplus.pojo.dto.LiveStartDTO;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.pojo.vo.LiveRoomVO;
import com.biliplus.result.PageResult;

import java.util.Map;

public interface LiveRoomService {

    LiveRoomVO startLive(Long userId, LiveStartDTO dto);

    void stopLive(Long roomId, Long userId);

    PageResult listLive(Integer page, Integer size);

    LiveRoomVO getRoomDetail(Long roomId);

    LiveRoomVO enterRoom(Long roomId, Long userId);

    void leaveRoom(Long roomId, Long userId);

    PageResult adminList(Integer status, Integer page, Integer size);

    void forceStop(Long roomId);

    /** 违规封禁：强制下播 + 禁用主播账号 */
    void banHost(Long roomId);

    /** 解除封禁主播账号 */
    void unbanHost(Long userId);

    LiveRoom getLiveRoom(Long roomId);

    /** 当前用户进行中的直播间（无则 null） */
    LiveRoomVO getMyLiveRoom(Long userId);

    /** 查询 SRS 推流状态 */
    Map<String, Object> getStreamStatus(Long roomId);
}
