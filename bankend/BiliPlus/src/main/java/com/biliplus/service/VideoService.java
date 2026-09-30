package com.biliplus.service;

import com.biliplus.pojo.dto.userdto.VideoPageQueryDTO;
import com.biliplus.pojo.vo.HotVideoVO;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.result.PageResult;

import java.util.List;

public interface VideoService {
    PageResult pageQuery(VideoPageQueryDTO videoPageQueryDTO);

    VideoUploadVO getVideo(Long videoId);

    PageResult recommend(Integer page, Integer size);

    /** 热搜/热榜 TopN，rank 从 1 开始 */
    List<HotVideoVO> hot(Integer size);

    /** 分享计数：返回该视频最新分享数；匿名调用同样计数 */
    Long share(Long videoId);
}
