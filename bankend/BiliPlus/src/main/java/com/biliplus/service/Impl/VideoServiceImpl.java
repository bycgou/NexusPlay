package com.biliplus.service.Impl;

import cn.hutool.core.bean.BeanUtil;
import com.biliplus.mapper.PeopleUserMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.GetListVideoVO;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.VideoService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class VideoServiceImpl implements VideoService {
    @Autowired
    private VideoMapper videoMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;
    /**
     * 分页查询
     * @param videoPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(VideoPageQueryDTO videoPageQueryDTO) {
        // 开始分页查询
        PageHelper.startPage(videoPageQueryDTO.getPage(), videoPageQueryDTO.getPageSize());

        Page<Video> page = videoMapper.pageQuery(videoPageQueryDTO);

        long total = page.getTotal();
        List<Video> records = page.getResult();

        log.info("分页查询结果：{}", page);

        List<GetListVideoVO> voList = new ArrayList<>();
        if (!records.isEmpty()) {
            // 提取所有 userId 并去重
            Set<Long> userIds = records.stream()
                    .map(Video::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                // 批量查询用户（返回的是 com.biliplus.pojo.entity.User）
                List<User> users = peopleUserMapper.selectByIds(new ArrayList<>(userIds));
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, user -> user));
            }

            // 转换为 VO 并填充用户信息
            for (Video video : records) {
                GetListVideoVO vo = new GetListVideoVO();
                // 复制视频字段
                vo.setId(video.getId());
                vo.setTitle(video.getTitle());
                vo.setDescription(video.getDescription());
                vo.setCoverUrl(video.getCoverUrl());
                vo.setVideoUrl(video.getVideoUrl());
                vo.setDuration(video.getDuration());
                vo.setUserId(video.getUserId());
                vo.setCategoryId(video.getCategoryId());
                vo.setStatus(video.getStatus());
                vo.setViewCount(video.getViewCount());
                vo.setLikeCount(video.getLikeCount());
                vo.setCommentCount(video.getCommentCount());
                vo.setShareCount(video.getShareCount());
                vo.setCreateTime(video.getCreateTime());
                vo.setUpdateTime(video.getUpdateTime());

                // 填充用户信息
                User user = userMap.get(video.getUserId());
                if (user != null) {
                    vo.setNickname(user.getNickname());
                    vo.setAvatar(user.getAvatar());
                }

                voList.add(vo);
            }
        }
        log.info("分页查询结果：{}", voList);
        return new PageResult(total, voList);
    }

    // 根据id查询视频
    @Override
    public VideoUploadVO getVideo(Long videoId) {

        Video video = videoMapper.getVideo(videoId);
        if (video == null) {
            throw new com.biliplus.exception.BusinessException("视频不存在或已下架");
        }
        videoMapper.increaseViewCount(videoId);
        if (video.getViewCount() != null) {
            video.setViewCount(video.getViewCount() + 1);
        }

        VideoUploadVO videoUploadVO = new VideoUploadVO();
        Long userId = video.getUserId();
        User user = peopleUserMapper.getUserById(userId);

        BeanUtil.copyProperties(video, videoUploadVO);
        if (user != null) {
            videoUploadVO.setNickname(user.getNickname());
            videoUploadVO.setAvatar(user.getAvatar());
        }
        return videoUploadVO;
    }

    // 3.加载推荐视频
    @Override
    public PageResult recommend() {
        // PageHelper 会自动追加 LIMIT，SQL 里不要再写 limit
        PageHelper.startPage(1, 10);
        Page<GetListVideoVO> page = videoMapper.recommend();
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public Long share(Long videoId) {
        if (videoId == null) {
            throw new com.biliplus.exception.BusinessException("视频ID不能为空");
        }
        // 未通过审核或已下架的稿件不计分享数（影响行数为 0）
        videoMapper.increaseShareCount(videoId);
        Integer count = videoMapper.selectShareCount(videoId);
        return count == null ? null : count.longValue();
    }
}
