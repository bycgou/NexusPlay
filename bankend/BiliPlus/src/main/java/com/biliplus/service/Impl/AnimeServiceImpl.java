package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.AnimeMapper;
import com.biliplus.pojo.entity.Anime;
import com.biliplus.result.PageResult;
import com.biliplus.service.AnimeService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnimeServiceImpl implements AnimeService {

    @Autowired
    private AnimeMapper animeMapper;

    @Override
    public PageResult pageQuery(Integer page, Integer pageSize, Integer status, String keyword) {
        PageHelper.startPage(page == null ? 1 : page, pageSize == null ? 20 : pageSize);
        Page<Anime> result = animeMapper.pageQuery(status, keyword);
        List<Anime> records = result.getResult();
        return new PageResult(result.getTotal(), records);
    }

    @Override
    public Anime getById(Long id) {
        Anime anime = animeMapper.selectById(id);
        if (anime == null) {
            throw new BusinessException("番剧不存在");
        }
        return anime;
    }
}
