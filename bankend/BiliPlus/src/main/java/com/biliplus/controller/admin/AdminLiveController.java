package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.Gift;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.GiftService;
import com.biliplus.service.LiveRoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/live")
public class AdminLiveController {

    @Autowired
    private LiveRoomService liveRoomService;

    @Autowired
    private GiftService giftService;

    @GetMapping("/rooms")
    public Result<PageResult> rooms(@RequestParam(required = false) Integer status,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(liveRoomService.adminList(status, page, size));
    }

    @PostMapping("/rooms/{id}/force-stop")
    public Result<String> forceStop(@PathVariable Long id) {
        try {
            liveRoomService.forceStop(id);
            return Result.success("已强制下播");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 直播间详情（含 playUrl，管理端预览用） */
    @GetMapping("/rooms/{id}")
    public Result<com.biliplus.pojo.vo.LiveRoomVO> roomDetail(@PathVariable Long id) {
        try {
            return Result.success(liveRoomService.getRoomDetail(id));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 违规封禁：强制下播 + 禁用主播 */
    @PostMapping("/rooms/{id}/ban")
    public Result<String> banHost(@PathVariable Long id) {
        try {
            liveRoomService.banHost(id);
            return Result.success("已强制下播并封禁主播");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 解除主播封禁 */
    @PostMapping("/users/{userId}/unban")
    public Result<String> unbanHost(@PathVariable Long userId) {
        try {
            liveRoomService.unbanHost(userId);
            return Result.success("已解除封禁");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/gift/records")
    public Result<PageResult> giftRecords(@RequestParam(required = false) Long roomId,
                                          @RequestParam(required = false) Long senderId,
                                          @RequestParam(required = false) Long hostUserId,
                                          @RequestParam(defaultValue = "1") Integer page,
                                          @RequestParam(defaultValue = "20") Integer size) {
        return Result.success(giftService.adminRecords(roomId, senderId, hostUserId, page, size));
    }
}
