package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.DynamicMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.entity.Dynamic;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.DynamicVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.DynamicService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DynamicServiceImpl implements DynamicService {

    public static final int TYPE_TEXT = 1;
    public static final int TYPE_VIDEO = 2;

    private static final int MAX_CONTENT_LENGTH = 1000;

    @Autowired
    private DynamicMapper dynamicMapper;

    @Autowired
    private VideoMapper videoMapper;

    @Override
    @Transactional
    public Dynamic publishText(Long userId, String content) {
        requireLogin(userId);
        if (!StringUtils.hasText(content)) {
            throw new BusinessException("请输入动态内容");
        }
        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException("动态内容不能超过" + MAX_CONTENT_LENGTH + "字");
        }

        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(userId);
        dynamic.setType(TYPE_TEXT);
        dynamic.setContent(trimmed);
        dynamic.setStatus(1);
        dynamic.setCreateTime(LocalDateTime.now());
        dynamicMapper.insert(dynamic);
        return dynamic;
    }

    @Override
    public void publishVideoDynamic(Long videoId) {
        if (videoId == null) {
            return;
        }
        if (dynamicMapper.countVideoDynamic(videoId) > 0) {
            return;
        }
        Video video = videoMapper.getVideo(videoId);
        if (video == null || video.getUserId() == null) {
            return;
        }
        Dynamic dynamic = new Dynamic();
        dynamic.setUserId(video.getUserId());
        dynamic.setType(TYPE_VIDEO);
        dynamic.setContent(video.getTitle());
        dynamic.setVideoId(videoId);
        dynamic.setStatus(1);
        dynamic.setCreateTime(LocalDateTime.now());
        dynamicMapper.insert(dynamic);
        log.info("自动发布投稿动态 videoId={}, dynamicId={}", videoId, dynamic.getId());
    }

    @Override
    public PageResult feed(Long userId, Integer page, Integer size) {
        requireLogin(userId);
        int[] range = range(page, size);
        List<DynamicVO> records = dynamicMapper.selectFeed(userId, range[0], range[1]);
        long total = dynamicMapper.countFeed(userId);
        decorate(records, userId);
        return new PageResult(total, records);
    }

    @Override
    public PageResult hot(Long viewerId, Integer page, Integer size) {
        int[] range = range(page, size);
        List<DynamicVO> records = dynamicMapper.selectHot(range[0], range[1]);
        long total = dynamicMapper.countHot();
        decorate(records, viewerId);
        return new PageResult(total, records);
    }

    @Override
    public PageResult byUser(Long viewerId, Long userId, Integer page, Integer size) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        int[] range = range(page, size);
        List<DynamicVO> records = dynamicMapper.selectByUser(userId, range[0], range[1]);
        long total = dynamicMapper.countByUser(userId);
        decorate(records, viewerId);
        return new PageResult(total, records);
    }

    @Override
    @Transactional
    public Map<String, Object> toggleLike(Long userId, Long dynamicId) {
        requireLogin(userId);
        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        if (dynamic == null || dynamic.getStatus() == null || dynamic.getStatus() != 1) {
            throw new BusinessException("动态不存在");
        }

        Map<String, Object> result = new HashMap<>();
        if (dynamicMapper.countLike(userId, dynamicId) > 0) {
            dynamicMapper.deleteLike(userId, dynamicId);
            result.put("liked", false);
        } else {
            dynamicMapper.insertLike(userId, dynamicId, LocalDateTime.now());
            result.put("liked", true);
        }
        result.put("likeCount", dynamicMapper.countLikes(dynamicId));
        return result;
    }

    @Override
    @Transactional
    public void delete(Long userId, Long dynamicId) {
        requireLogin(userId);
        Dynamic dynamic = dynamicMapper.selectById(dynamicId);
        if (dynamic == null || dynamic.getStatus() == null || dynamic.getStatus() != 1) {
            throw new BusinessException("动态不存在");
        }
        if (!Objects.equals(dynamic.getUserId(), userId)) {
            throw new BusinessException("只能删除自己的动态");
        }
        dynamicMapper.softDelete(dynamicId, userId);
    }

    /** 一次查询补齐当前用户对该页所有动态的点赞态，避免逐条查库 */
    private void decorate(List<DynamicVO> records, Long viewerId) {
        if (records == null || records.isEmpty() || viewerId == null) {
            if (records != null) {
                records.forEach(r -> r.setLiked(false));
            }
            return;
        }
        List<Long> ids = records.stream().map(DynamicVO::getId).collect(Collectors.toList());
        Set<Long> likedIds = new HashSet<>(dynamicMapper.selectLikedIds(viewerId, ids));
        for (DynamicVO vo : records) {
            vo.setLiked(likedIds.contains(vo.getId()));
        }
    }

    private int[] range(Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 50);
        return new int[]{(p - 1) * s, s};
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
    }
}
