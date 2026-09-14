package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.BannerMapper;
import com.biliplus.pojo.entity.Banner;
import com.biliplus.service.BannerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class BannerServiceImpl implements BannerService {

    @Autowired
    private BannerMapper bannerMapper;

    @Override
    public List<Banner> listOnline() {
        return bannerMapper.listOnline();
    }

    @Override
    public List<Banner> listAll() {
        return bannerMapper.listAll();
    }

    @Override
    public Banner create(Banner banner) {
        validate(banner);
        fillDefaults(banner);
        banner.setCreateTime(LocalDateTime.now());
        banner.setUpdateTime(LocalDateTime.now());
        bannerMapper.insert(banner);
        return banner;
    }

    @Override
    public Banner update(Long id, Banner banner) {
        if (id == null) {
            throw new BusinessException("轮播图ID不能为空");
        }
        Banner existing = bannerMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("轮播图不存在");
        }
        validate(banner);
        fillDefaults(banner);
        banner.setId(id);
        banner.setUpdateTime(LocalDateTime.now());
        int rows = bannerMapper.update(banner);
        if (rows <= 0) {
            throw new BusinessException("更新失败");
        }
        return bannerMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BusinessException("轮播图ID不能为空");
        }
        if (bannerMapper.selectById(id) == null) {
            throw new BusinessException("轮播图不存在");
        }
        bannerMapper.deleteById(id);
    }

    private void validate(Banner banner) {
        if (banner == null || !StringUtils.hasText(banner.getTitle())) {
            throw new BusinessException("标题不能为空");
        }
        if (!StringUtils.hasText(banner.getImageUrl())) {
            throw new BusinessException("图片地址不能为空");
        }
        Integer linkType = banner.getLinkType();
        if (linkType != null && linkType == 1 && banner.getVideoId() == null) {
            throw new BusinessException("跳转视频时必须填写视频ID");
        }
    }

    private void fillDefaults(Banner banner) {
        if (banner.getLinkType() == null) {
            banner.setLinkType(3);
        }
        if (banner.getSortOrder() == null) {
            banner.setSortOrder(0);
        }
        if (banner.getStatus() == null) {
            banner.setStatus(1);
        }
        if (banner.getLinkType() != 1) {
            banner.setVideoId(null);
        }
        if (banner.getLinkType() != 2) {
            banner.setLinkUrl(null);
        }
    }
}
