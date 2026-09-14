package com.biliplus.service;

import com.biliplus.pojo.entity.Banner;

import java.util.List;

public interface BannerService {

    List<Banner> listOnline();

    List<Banner> listAll();

    Banner create(Banner banner);

    Banner update(Long id, Banner banner);

    void delete(Long id);
}
