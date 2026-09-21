package com.biliplus.controller.user;

import com.biliplus.pojo.vo.FavoriteFolderVO;
import com.biliplus.result.PageResult;
import com.biliplus.result.Result;
import com.biliplus.service.FavoriteFolderService;
import com.biliplus.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/favorite-folders")
public class FavoriteFolderController {

    @Autowired
    private FavoriteFolderService favoriteFolderService;

    @GetMapping
    public Result<List<FavoriteFolderVO>> list() {
        try {
            return Result.success(favoriteFolderService.list(UserContext.getCurrentUserId()));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<FavoriteFolderVO> create(@RequestBody Map<String, Object> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            String name = body == null ? null : (String) body.get("name");
            Boolean isPrivate = body == null || body.get("isPrivate") == null
                    ? null : Boolean.valueOf(String.valueOf(body.get("isPrivate")));
            return Result.success(favoriteFolderService.create(userId, name, isPrivate));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<FavoriteFolderVO> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            Long userId = UserContext.getCurrentUserId();
            String name = body == null ? null : (String) body.get("name");
            Boolean isPrivate = body == null || body.get("isPrivate") == null
                    ? null : Boolean.valueOf(String.valueOf(body.get("isPrivate")));
            return Result.success(favoriteFolderService.update(userId, id, name, isPrivate));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            favoriteFolderService.delete(UserContext.getCurrentUserId(), id);
            return Result.success("已删除");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 夹内视频分页 */
    @GetMapping("/{id}/videos")
    public Result<PageResult> videos(@PathVariable Long id,
                                    @RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "20") Integer size) {
        try {
            return Result.success(favoriteFolderService.videos(UserContext.getCurrentUserId(), id, page, size));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
