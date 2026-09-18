package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LivePkMapper;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.pojo.entity.LivePk;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.properties.LiveProperties;
import com.biliplus.service.LivePkService;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
public class LivePkServiceImpl implements LivePkService {

    @Autowired
    private LivePkMapper livePkMapper;

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    @Autowired
    private LiveProperties liveProperties;

    @Autowired
    private LiveWebSocketHandler liveWebSocketHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public LivePk invite(Long hostId, Long opponentRoomId) {
        LiveRoom myRoom = liveRoomMapper.selectLiveByUserId(hostId);
        if (myRoom == null) {
            throw new BusinessException("您尚未开播");
        }
        LiveRoom opponent = liveRoomMapper.selectById(opponentRoomId);
        if (opponent == null || opponent.getStatus() == null || opponent.getStatus() != 1) {
            throw new BusinessException("对方直播间未开播");
        }
        if (opponent.getUserId().equals(hostId)) {
            throw new BusinessException("不能与自己 PK");
        }
        if (livePkMapper.selectActiveByRoom(myRoom.getId()) != null
                || livePkMapper.selectActiveByRoom(opponentRoomId) != null) {
            throw new BusinessException("已有进行中的 PK");
        }

        LivePk pk = new LivePk();
        pk.setRoomAId(myRoom.getId());
        pk.setRoomBId(opponentRoomId);
        pk.setHostAId(hostId);
        pk.setHostBId(opponent.getUserId());
        pk.setStatus(0);
        pk.setScoreA(0);
        pk.setScoreB(0);
        pk.setDurationSec(liveProperties.getPk().getDefaultDurationSec());
        pk.setCreateTime(LocalDateTime.now());
        livePkMapper.insert(pk);

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "pk_invite");
            node.put("pkId", pk.getId());
            node.put("roomId", opponentRoomId);
            node.put("fromRoomId", myRoom.getId());
            node.put("fromHostId", hostId);
            liveWebSocketHandler.broadcast(opponentRoomId, node.toString());
        } catch (Exception e) {
            log.warn("广播 pk_invite 失败", e);
        }
        return pk;
    }

    @Override
    public LivePk response(Long hostId, Long pkId, boolean agree) {
        LivePk pk = livePkMapper.selectById(pkId);
        if (pk == null) {
            throw new BusinessException("PK 记录不存在");
        }
        if (!pk.getHostBId().equals(hostId)) {
            throw new BusinessException("无权响应该邀请");
        }
        if (pk.getStatus() == null || pk.getStatus() != 0) {
            throw new BusinessException("邀请已处理");
        }

        if (!agree) {
            pk.setStatus(3);
            livePkMapper.update(pk);
            try {
                ObjectNode node = objectMapper.createObjectNode();
                node.put("type", "pk_cancel");
                node.put("pkId", pkId);
                liveWebSocketHandler.broadcastToRooms(pk.getRoomAId(), pk.getRoomBId(), node.toString());
            } catch (Exception ignored) {
            }
            return pk;
        }

        pk.setStatus(1);
        pk.setStartTime(LocalDateTime.now());
        livePkMapper.update(pk);

        LiveRoom roomA = liveRoomMapper.selectById(pk.getRoomAId());
        LiveRoom roomB = liveRoomMapper.selectById(pk.getRoomBId());

        try {
            ObjectNode a = objectMapper.createObjectNode();
            a.put("type", "pk_start");
            a.put("roomId", pk.getRoomAId());
            a.put("pkId", pk.getId());
            a.put("opponentRoomId", pk.getRoomBId());
            a.put("opponentName", roomB == null ? "" : String.valueOf(roomB.getTitle()));
            a.put("opponentPlayUrl", roomB == null ? "" : roomB.getPlayUrl());
            a.put("durationSec", pk.getDurationSec() == null ? 300 : pk.getDurationSec());

            ObjectNode b = objectMapper.createObjectNode();
            b.put("type", "pk_start");
            b.put("roomId", pk.getRoomBId());
            b.put("pkId", pk.getId());
            b.put("opponentRoomId", pk.getRoomAId());
            b.put("opponentName", roomA == null ? "" : roomA.getTitle());
            b.put("opponentPlayUrl", roomA == null ? "" : roomA.getPlayUrl());
            b.put("durationSec", pk.getDurationSec() == null ? 300 : pk.getDurationSec());

            liveWebSocketHandler.broadcast(pk.getRoomAId(), a.toString());
            liveWebSocketHandler.broadcast(pk.getRoomBId(), b.toString());
        } catch (Exception e) {
            log.warn("广播 pk_start 失败", e);
        }
        return pk;
    }

    @Override
    public LivePk getActive(Long roomId) {
        return livePkMapper.selectActiveByRoom(roomId);
    }

    @Override
    public LivePk end(Long hostId, Long pkId) {
        LivePk pk = livePkMapper.selectById(pkId);
        if (pk == null) {
            throw new BusinessException("PK 记录不存在");
        }
        if (!pk.getHostAId().equals(hostId) && !pk.getHostBId().equals(hostId)) {
            throw new BusinessException("无权结束该 PK");
        }
        if (pk.getStatus() == null || pk.getStatus() != 1) {
            throw new BusinessException("PK 未进行中");
        }
        return doEnd(pk);
    }

    @Override
    public LivePk addScore(Long pkId, Long roomWhich, int score) {
        LivePk pk = livePkMapper.selectById(pkId);
        if (pk == null || pk.getStatus() == null || pk.getStatus() != 1) {
            return pk;
        }
        if (pk.getRoomAId().equals(roomWhich)) {
            livePkMapper.addScoreA(pkId, roomWhich, score);
        } else if (pk.getRoomBId().equals(roomWhich)) {
            livePkMapper.addScoreB(pkId, roomWhich, score);
        }
        LivePk updated = livePkMapper.selectById(pkId);
        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "pk_score");
            node.put("pkId", pkId);
            node.put("scoreA", updated.getScoreA() == null ? 0 : updated.getScoreA());
            node.put("scoreB", updated.getScoreB() == null ? 0 : updated.getScoreB());
            liveWebSocketHandler.broadcastToRooms(updated.getRoomAId(), updated.getRoomBId(), node.toString());
        } catch (Exception e) {
            log.warn("广播 pk_score 失败", e);
        }
        return updated;
    }

    @Override
    public void forceCloseByRoom(Long roomId) {
        LivePk active = livePkMapper.selectActiveByRoom(roomId);
        if (active != null) {
            doEnd(active);
        }
        livePkMapper.closeByRoom(roomId);
    }

    private LivePk doEnd(LivePk pk) {
        pk.setStatus(2);
        pk.setEndTime(LocalDateTime.now());
        livePkMapper.update(pk);

        int sa = pk.getScoreA() == null ? 0 : pk.getScoreA();
        int sb = pk.getScoreB() == null ? 0 : pk.getScoreB();
        Long winner = sa == sb ? null : (sa > sb ? pk.getRoomAId() : pk.getRoomBId());

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "pk_end");
            node.put("pkId", pk.getId());
            if (winner != null) {
                node.put("winnerRoomId", winner);
            } else {
                node.putNull("winnerRoomId");
            }
            node.put("scoreA", sa);
            node.put("scoreB", sb);
            liveWebSocketHandler.broadcastToRooms(pk.getRoomAId(), pk.getRoomBId(), node.toString());
        } catch (Exception e) {
            log.warn("广播 pk_end 失败", e);
        }
        return pk;
    }
}
