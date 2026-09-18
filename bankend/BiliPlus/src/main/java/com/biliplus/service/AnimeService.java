package com.biliplus.service;

import com.biliplus.pojo.entity.Anime;
import com.biliplus.result.PageResult;

public interface AnimeService {

    /** 番剧分页列表 */
    PageResult pageQuery(Integer page, Integer pageSize, Integer status, String keyword);

    /** 番剧详情 */
    Anime getById(Long id);
}
