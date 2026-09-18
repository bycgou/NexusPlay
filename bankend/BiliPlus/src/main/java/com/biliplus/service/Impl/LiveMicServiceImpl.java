package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LiveMicSessionMapper;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.pojo.entity.LiveMicSession;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.service.LiveMicService;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class LiveMicServiceImpl implements LiveMicService {

    @Autowired
    private LiveMicSessionMapper micSessionMapper;

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    @Autowired
    private LiveWebSocketHandler liveWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public LiveMicSession apply(Long guestId, Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null || room.getStatus() == null || room.getStatus() != 1) {
            throw new BusinessException("直播间未开播");
        }
        if (room.getUserId().equals(guestId)) {
            throw new BusinessException("主播无需申请连麦");
        }
        LiveMicSession active = micSessionMapper.selectActive(roomId);
        if (active != null) {
            throw new BusinessException("当前已有连麦进行中");
        }
        List<LiveMicSession> pending = micSessionMapper.listPending(roomId, 5);
        boolean already = pending.stream().anyMatch(s -> s.getGuestUserId().equals(guestId));
        if (already) {
            throw new BusinessException("已提交连麦申请，请等待主播处理");
        }

        LiveMicSession session = new LiveMicSession();
        session.setLiveRoomId(roomId);
        session.setHostUserId(room.getUserId());
        session.setGuestUserId(guestId);
        session.setStatus(0);
        session.setCreateTime(LocalDateTime.now());
        micSessionMapper.insert(session);
        return session;
    }

    @Override
    public Map<String, Object> accept(Long hostId, Long sessionId, boolean agree) {
        LiveMicSession session = micSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException("连麦申请不存在");
        }
        if (!session.getHostUserId().equals(hostId)) {
            throw new BusinessException("无权处理该申请");
        }
        if (session.getStatus() != null && session.getStatus() != 0) {
            throw new BusinessException("该申请已处理");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("sessionId", sessionId);
        result.put("agree", agree);
        result.put("guestUserId", session.getGuestUserId());
        result.put("roomId", session.getLiveRoomId());

        if (!agree) {
            session.setStatus(3);
            session.setEndTime(LocalDateTime.now());
            micSessionMapper.updateStatus(session);
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("type", "mic_result");
                node.put("roomId", session.getLiveRoomId());
                node.put("sessionId", sessionId);
                node.put("guestUserId", session.getGuestUserId());
                node.put("agree", false);
                liveWebSocketHandler.broadcast(session.getLiveRoomId(), node.toString());
            } catch (Exception e) {
                log.warn("广播 mic 拒绝失败", e);
            }
            return result;
        }

        session.setStatus(1);
        session.setStartTime(LocalDateTime.now());
        micSessionMapper.updateStatus(session);

        String rtcRoom = "live-" + session.getLiveRoomId();
        result.put("rtcRoom", rtcRoom);
        result.put("token", "mic-" + sessionId + "-" + System.currentTimeMillis());

        try {
            // 给双方
            ObjectNode hostNode = objectMapper.createObjectNode();
            hostNode.put("type", "mic_ready");
            hostNode.put("roomId", session.getLiveRoomId());
            hostNode.put("sessionId", sessionId);
            hostNode.put("role", "host");
            hostNode.put("guestUserId", session.getGuestUserId());
            hostNode.put("rtcRoom", rtcRoom);
            hostNode.put("token", (String) result.get("token"));

            ObjectNode guestNode = objectMapper.createObjectNode();
            guestNode.put("type", "mic_ready");
            guestNode.put("roomId", session.getLiveRoomId());
            guestNode.put("sessionId", sessionId);
            guestNode.put("role", "guest");
            guestNode.put("guestUserId", session.getGuestUserId());
            guestNode.put("rtcRoom", rtcRoom);
            guestNode.put("token", (String) result.get("token"));

            // 房间广播（主播在房间）；guest 可能也在
            liveWebSocketHandler.broadcast(session.getLiveRoomId(), hostNode.toString());
            liveWebSocketHandler.broadcast(session.getLiveRoomId(), guestNode.toString());
        } catch (Exception e) {
            log.warn("广播 mic_ready 失败", e);
        }
        return result;
    }

    @Override
    public void leave(Long userId, Long roomId) {
        LiveMicSession active = micSessionMapper.selectActive(roomId);
        if (active == null) {
            return;
        }
        if (!active.getHostUserId().equals(userId) && !active.getGuestUserId().equals(userId)) {
            return;
        }
        active.setStatus(2);
        active.setEndTime(LocalDateTime.now());
        micSessionMapper.updateStatus(active);
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "mic_end");
            node.put("roomId", roomId);
            node.put("sessionId", active.getId());
            liveWebSocketHandler.broadcast(roomId, node.toString());
        } catch (Exception e) {
            log.warn("广播 mic_end 失败", e);
        }
    }

    @Override
    public List<LiveMicSession> listPending(Long roomId) {
        return micSessionMapper.listPending(roomId, 20);
    }

    @Override
    public void forceCloseByRoom(Long roomId) {
        micSessionMapper.closeByRoom(roomId);
    }
}
