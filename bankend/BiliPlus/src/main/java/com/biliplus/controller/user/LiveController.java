package com.biliplus.controller.user;

import com.biliplus.pojo.dto.GiftSendDTO;
import com.biliplus.pojo.dto.LiveStartDTO;
import com.biliplus.pojo.dto.PkInviteDTO;
import com.biliplus.pojo.dto.PkResponseDTO;
import com.biliplus.pojo.entity.LiveMicSession;
import com.biliplus.pojo.entity.LivePk;
import com.biliplus.pojo.vo.LiveRoomVO;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.GiftService;
import com.biliplus.service.LiveMicService;
import com.biliplus.service.LivePkService;
import com.biliplus.service.LiveReplayService;
import com.biliplus.service.LiveRoomService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/pp/live")
public class LiveController {

    @Autowired
    private LiveRoomService liveRoomService;

    @Autowired
    private GiftService giftService;

    @Autowired
    private LiveMicService liveMicService;

    @Autowired
    private LivePkService livePkService;

    @Autowired
    private LiveReplayService liveReplayService;

    // ========== 开播 / 观看 ==========

    @PostMapping("/rooms")
    public Result<LiveRoomVO> startLive(@RequestBody LiveStartDTO dto) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(liveRoomService.startLive(userId, dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rooms/{id}/stop")
    public Result<String> stopLive(@PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            liveRoomService.stopLive(id, userId);
            return Result.success("下播成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/rooms")
    public Result<PageResult> listLive(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(liveRoomService.listLive(page, size));
    }

    @GetMapping("/rooms/{id}")
    public Result<LiveRoomVO> getRoom(@PathVariable Long id) {
        try {
            return Result.success(liveRoomService.getRoomDetail(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 当前用户进行中的直播间（主播台恢复用，需登录） */
    @GetMapping("/my-room")
    public Result<LiveRoomVO> getMyRoom() {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(liveRoomService.getMyLiveRoom(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 推流状态（是否已推到 SRS） */
    @GetMapping("/rooms/{id}/stream-status")
    public Result<java.util.Map<String, Object>> streamStatus(@PathVariable Long id) {
        try {
            return Result.success(liveRoomService.getStreamStatus(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rooms/{id}/enter")
    public Result<LiveRoomVO> enterRoom(@PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(liveRoomService.enterRoom(id, userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rooms/{id}/leave")
    public Result<String> leaveRoom(@PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            liveRoomService.leaveRoom(id, userId);
            return Result.success("ok");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 礼物 / 钱包 ==========

    @GetMapping("/gifts")
    public Result<List<?>> listGifts() {
        return Result.success(giftService.listOnline());
    }

    @GetMapping("/wallet")
    public Result<?> wallet() {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(giftService.getWallet(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/wallet/recharge")
    public Result<?> recharge(@RequestBody java.util.Map<String, Long> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            Long amount = body == null ? null : body.get("amount");
            if (amount == null) {
                return Result.error("金额不能为空");
            }
            return Result.success(giftService.recharge(userId, amount));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/rooms/{id}/gifts")
    public Result<?> sendGift(@PathVariable Long id, @RequestBody GiftSendDTO dto) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(giftService.sendGift(userId, id, dto));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 连麦 ==========

    @GetMapping("/rooms/{id}/mic/history")
    public Result<List<LiveMicSession>> micPending(@PathVariable Long id) {
        return Result.success(liveMicService.listPending(id));
    }

    @PostMapping("/rooms/{id}/mic/leave")
    public Result<String> micLeave(@PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            liveMicService.leave(userId, id);
            return Result.success("ok");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== 回放 ==========

    /** 回放列表；userId 为空表示全站 */
    @GetMapping("/replays")
    public Result<PageResult> replays(@RequestParam(required = false) Long userId,
                                      @RequestParam(defaultValue = "1") Integer page,
                                      @RequestParam(defaultValue = "12") Integer size) {
        return Result.success(liveReplayService.list(userId, page, size));
    }

    @GetMapping("/replays/{id}")
    public Result<com.biliplus.pojo.entity.LiveReplay> replay(@PathVariable Long id) {
        try {
            return Result.success(liveReplayService.getById(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    // ========== PK ==========

    @PostMapping("/pk/invite")
    public Result<LivePk> pkInvite(@RequestBody PkInviteDTO dto) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(livePkService.invite(userId, dto.getOpponentRoomId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/pk/{id}/response")
    public Result<LivePk> pkResponse(@PathVariable Long id, @RequestBody PkResponseDTO dto) {
        try {
            Long userId = UserContext.getCurrentUserId();
            boolean agree = dto != null && Boolean.TRUE.equals(dto.getAgree());
            return Result.success(livePkService.response(userId, id, agree));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/pk/active")
    public Result<LivePk> pkActive(@RequestParam Long roomId) {
        return Result.success(livePkService.getActive(roomId));
    }

    @PostMapping("/pk/{id}/end")
    public Result<LivePk> pkEnd(@PathVariable Long id) {
        try {
            Long userId = UserContext.getCurrentUserId();
            return Result.success(livePkService.end(userId, id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
