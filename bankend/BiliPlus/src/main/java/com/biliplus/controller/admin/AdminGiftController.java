package com.biliplus.controller.admin;

import com.biliplus.pojo.entity.Gift;
import com.biliplus.result.Result;
import com.biliplus.service.GiftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin/gifts")
public class AdminGiftController {

    @Autowired
    private GiftService giftService;

    @GetMapping
    public Result<List<Gift>> list() {
        return Result.success(giftService.listAll());
    }

    @PostMapping
    public Result<Gift> create(@RequestBody Gift gift) {
        try {
            return Result.success(giftService.create(gift));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Result<Gift> update(@PathVariable Long id, @RequestBody Gift gift) {
        try {
            return Result.success(giftService.update(id, gift));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        try {
            giftService.delete(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
