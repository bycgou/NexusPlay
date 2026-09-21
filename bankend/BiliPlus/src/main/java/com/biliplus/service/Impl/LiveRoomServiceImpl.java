package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.LiveRoomMapper;
import com.biliplus.mapper.LiveUserMapper;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.pojo.dto.LiveStartDTO;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.vo.LiveRoomVO;
import com.biliplus.properties.LiveProperties;
import com.biliplus.result.PageResult;
import com.biliplus.service.LiveMicService;
import com.biliplus.service.LivePkService;
import com.biliplus.service.LiveReplayService;
import com.biliplus.service.LiveRoomService;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiveRoomServiceImpl implements LiveRoomService {

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    @Autowired
    private LiveUserMapper liveUserMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private LiveProperties liveProperties;

    @Autowired
    private LiveWebSocketHandler liveWebSocketHandler;

    @Autowired
    private LiveMicService liveMicService;

    @Autowired
    private LivePkService livePkService;

    @Autowired
    private LiveReplayService liveReplayService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public LiveRoomVO startLive(Long userId, LiveStartDTO dto) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto == null || !StringUtils.hasText(dto.getTitle())) {
            throw new BusinessException("直播标题不能为空");
        }
        if (dto.getCategoryId() == null) {
            throw new BusinessException("请选择分区");
        }
        LiveRoom existing = liveRoomMapper.selectLiveByUserId(userId);
        if (existing != null) {
            throw new BusinessException("您已在直播中，请先下播");
        }

        LiveRoom room = new LiveRoom();
        room.setTitle(dto.getTitle().trim());
        room.setCoverUrl(dto.getCoverUrl());
        room.setUserId(userId);
        room.setCategoryId(dto.getCategoryId());
        room.setStatus(1);
        room.setViewCount(0);
        room.setStartTime(LocalDateTime.now());
        room.setCreateTime(LocalDateTime.now());
        room.setUpdateTime(LocalDateTime.now());

        String streamKey = UUID.randomUUID().toString().replace("-", "");
        room.setStreamKey(streamKey);
        room.setPushUrl(liveProperties.buildPushUrl(streamKey));
        room.setPlayUrl(liveProperties.buildPlayUrl(streamKey));

        liveRoomMapper.insert(room);
        return decorate(LiveRoomVO.from(room));
    }

    @Override
    public void stopLive(Long roomId, Long userId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        if (!room.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该直播间");
        }
        doStop(room);
    }

    @Override
    public PageResult listLive(Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 12 : Math.min(size, 50);
        int offset = (p - 1) * s;
        List<LiveRoom> rooms = liveRoomMapper.listLive(offset, s);
        long total = liveRoomMapper.countLive();
        List<LiveRoomVO> list = rooms.stream()
                .map(r -> decorate(LiveRoomVO.from(r)))
                .collect(Collectors.toList());
        return new PageResult(total, list);
    }

    @Override
    public LiveRoomVO getRoomDetail(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        return decorate(LiveRoomVO.from(room));
    }

    @Override
    public LiveRoomVO enterRoom(Long roomId, Long userId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        if (room.getStatus() != null && room.getStatus() != 1) {
            throw new BusinessException("该直播间未开播");
        }
        com.biliplus.pojo.entity.LiveUser liveUser = new com.biliplus.pojo.entity.LiveUser();
        liveUser.setLiveRoomId(roomId);
        liveUser.setUserId(userId);
        liveUser.setEnterTime(LocalDateTime.now());
        liveUserMapper.insert(liveUser);
        liveRoomMapper.incrViewCount(roomId, 1);
        return decorate(LiveRoomVO.from(liveRoomMapper.selectById(roomId)));
    }

    @Override
    public void leaveRoom(Long roomId, Long userId) {
        liveUserMapper.markLeave(roomId, userId);
    }

    @Override
    public PageResult adminList(Integer status, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 12 : Math.min(size, 50);
        int offset = (p - 1) * s;
        List<LiveRoom> rooms = liveRoomMapper.adminList(status, offset, s);
        long total = liveRoomMapper.adminCount(status);
        List<LiveRoomVO> list = rooms.stream()
                .map(r -> decorate(LiveRoomVO.from(r)))
                .collect(Collectors.toList());
        return new PageResult(total, list);
    }

    @Override
    public void forceStop(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        if (room.getStatus() != null && room.getStatus() == 1) {
            doStop(room);
        }
    }

    @Override
    public void banHost(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        if (room.getStatus() != null && room.getStatus() == 1) {
            doStop(room);
        }
        int rows = peopleUserMapper.updateUserStatus(room.getUserId(), 0);
        if (rows <= 0) {
            throw new BusinessException("封禁主播失败");
        }
        log.info("主播 {} 因直播间 {} 违规被封禁", room.getUserId(), roomId);
    }

    @Override
    public void unbanHost(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        int rows = peopleUserMapper.updateUserStatus(userId, 1);
        if (rows <= 0) {
            throw new BusinessException("解封失败，用户不存在");
        }
        log.info("主播 {} 已解除封禁", userId);
    }

    @Override
    public LiveRoom getLiveRoom(Long roomId) {
        return liveRoomMapper.selectById(roomId);
    }

    @Override
    public LiveRoomVO getMyLiveRoom(Long userId) {
        if (userId == null) {
            return null;
        }
        LiveRoom room = liveRoomMapper.selectLiveByUserId(userId);
        return decorate(LiveRoomVO.from(room));
    }

    @Override
    public Map<String, Object> getStreamStatus(Long roomId) {
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("roomId", roomId);
        result.put("roomStatus", room.getStatus());
        result.put("playUrl", room.getPlayUrl());
        result.put("streamKey", room.getStreamKey());
        boolean publishing = false;
        int clients = 0;
        if (StringUtils.hasText(room.getStreamKey())) {
            try {
                // SRS 5: GET /api/v1/streams 返回全部流
                String apiUrl = String.format("http://%s:1985/api/v1/streams",
                        liveProperties.getSrs().getRtmpHost());
                java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                        .connectTimeout(java.time.Duration.ofSeconds(2))
                        .build();
                java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                        .uri(java.net.URI.create(apiUrl))
                        .timeout(java.time.Duration.ofSeconds(2))
                        .GET()
                        .build();
                java.net.http.HttpResponse<String> resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                if (resp.statusCode() == 200 && StringUtils.hasText(resp.body())) {
                    String body = resp.body();
                    String key = room.getStreamKey();
                    int nameIdx = body.indexOf("\"name\":\"" + key + "\"");
                    if (nameIdx < 0) {
                        nameIdx = body.indexOf("\"name\":\"" + key + ".flv\"");
                    }
                    if (nameIdx >= 0) {
                        // 截取该流对象到下一个 "name" 或足够长窗口
                        int nextName = body.indexOf("\"name\":\"", nameIdx + 8);
                        int end = nextName > nameIdx ? nextName : Math.min(body.length(), nameIdx + 2000);
                        String window = body.substring(nameIdx, end);
                        boolean active = window.contains("\"active\":true")
                                || window.contains("\"active\": true");
                        // frames>0 说明确实在收推流数据（比 active 更稳）
                        boolean hasFrames = false;
                        int fi = window.indexOf("\"frames\":");
                        if (fi >= 0) {
                            int numStart = fi + 9;
                            int numEnd = numStart;
                            while (numEnd < window.length() && Character.isDigit(window.charAt(numEnd))) {
                                numEnd++;
                            }
                            if (numEnd > numStart) {
                                try {
                                    long frames = Long.parseLong(window.substring(numStart, numEnd));
                                    hasFrames = frames > 0;
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        publishing = active || hasFrames;
                        if (publishing) {
                            clients = 1;
                        }
                        result.put("srsActive", active);
                        result.put("srsFramesOk", hasFrames);
                    }
                }
            } catch (Exception e) {
                log.debug("查询 SRS 推流状态失败: {}", e.getMessage());
                result.put("srsError", e.getMessage());
            }
        }
        result.put("publishing", publishing);
        result.put("clients", clients);
        return result;
    }

    private void doStop(LiveRoom room) {
        room.setStatus(2);
        room.setEndTime(LocalDateTime.now());

        // 登记回放要在清空 streamKey 之前，回放地址依赖流密钥
        try {
            liveReplayService.registerOnStop(room);
        } catch (Exception e) {
            log.warn("登记直播回放失败 roomId={}", room.getId(), e);
        }

        room.setStreamKey(null);
        room.setPlayUrl(null);
        room.setPushUrl(null);

        liveMicService.forceCloseByRoom(room.getId());
        livePkService.forceCloseByRoom(room.getId());

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "room_close");
            node.put("roomId", room.getId());
            liveWebSocketHandler.broadcast(room.getId(), node.toString());
        } catch (Exception e) {
            log.warn("广播 room_close 失败 roomId={}", room.getId(), e);
        }

        liveWebSocketHandler.clearRoom(room.getId());
        liveRoomMapper.updateStatus(room);
        log.info("直播间 {} 已下播", room.getId());
    }

    private LiveRoomVO decorate(LiveRoomVO vo) {
        if (vo == null) {
            return null;
        }
        if (vo.getUserId() != null) {
            User user = peopleUserMapper.getUserById(vo.getUserId());
            if (user != null) {
                vo.setHostNickname(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
                vo.setHostAvatar(user.getAvatar());
            }
        }
        if (vo.getId() != null && vo.getStatus() != null && vo.getStatus() == 1) {
            vo.setOnlineCount(liveWebSocketHandler.onlineCount(vo.getId()));
        } else {
            vo.setOnlineCount(0);
        }
        return vo;
    }
}
