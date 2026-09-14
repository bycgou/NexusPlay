package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AdminVideoMapper;
import com.biliplus.pojo.entity.Video;
import com.biliplus.result.PageResult;
import com.biliplus.service.AdminVideoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class AdminVideoServiceImpl implements AdminVideoService {

    /** 视频状态：0待审 1正常 2下架 3审核不通过 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_NORMAL = 1;
    public static final int STATUS_OFFLINE = 2;
    public static final int STATUS_REJECTED = 3;

    @Autowired
    private AdminVideoMapper adminVideoMapper;

    @Override
    public PageResult pageByStatus(Integer status, Integer page, Integer pageSize) {
        int p = page == null || page < 1 ? 1 : page;
        int ps = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 100);
        PageHelper.startPage(p, ps);
        Page<Video> result = adminVideoMapper.selectByStatus(status);
        return new PageResult(result.getTotal(), result.getResult());
    }

    @Override
    public Video getById(Long videoId) {
        if (videoId == null) {
            return null;
        }
        return adminVideoMapper.selectById(videoId);
    }

    @Override
    @Transactional
    public void approve(Long videoId) {
        changeStatus(videoId, STATUS_PENDING, STATUS_NORMAL, "审核通过失败：视频不存在或状态不正确");
        // 通过时清空历史拒绝原因
        adminVideoMapper.clearRejectReason(videoId);
    }

    @Override
    @Transactional
    public void reject(Long videoId, String reason) {
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("请填写不通过原因");
        }
        String trimmed = reason.trim();
        if (trimmed.length() > 500) {
            throw new BusinessException("原因不能超过500字");
        }
        int rows = adminVideoMapper.reject(videoId, STATUS_PENDING, STATUS_REJECTED, trimmed);
        if (rows <= 0) {
            throw new BusinessException("审核不通过失败：视频不存在或状态不正确");
        }
        log.info("视频审核不通过 id={} reason={}", videoId, trimmed);
    }

    @Override
    public void offline(Long videoId) {
        changeStatus(videoId, STATUS_NORMAL, STATUS_OFFLINE, "下架失败：视频不存在或状态不正确");
    }

    private void changeStatus(Long videoId, int fromStatus, int toStatus, String errMsg) {
        if (videoId == null) {
            throw new BusinessException("视频ID不能为空");
        }
        int rows = adminVideoMapper.updateStatus(videoId, fromStatus, toStatus);
        if (rows <= 0) {
            throw new BusinessException(errMsg);
        }
        log.info("视频状态变更 id={} {} -> {}", videoId, fromStatus, toStatus);
    }
}
