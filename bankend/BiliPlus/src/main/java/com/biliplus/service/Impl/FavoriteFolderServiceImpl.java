package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.FavoriteFolderMapper;
import com.biliplus.mapper.VideoFavoriteMapper;
import com.biliplus.pojo.entity.FavoriteFolder;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.FavoriteFolderVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.FavoriteFolderService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class FavoriteFolderServiceImpl implements FavoriteFolderService {

    public static final String DEFAULT_FOLDER_NAME = "默认收藏";

    private static final int MAX_NAME_LENGTH = 50;

    @Autowired
    private FavoriteFolderMapper favoriteFolderMapper;

    @Autowired
    private VideoFavoriteMapper videoFavoriteMapper;

    @Override
    public List<FavoriteFolderVO> list(Long userId) {
        requireLogin(userId);
        ensureDefaultFolder(userId);
        List<FavoriteFolderVO> folders = favoriteFolderMapper.listByUser(userId);
        return folders == null ? new ArrayList<>() : folders;
    }

    @Override
    @Transactional
    public FavoriteFolderVO create(Long userId, String name, Boolean isPrivate) {
        requireLogin(userId);
        String trimmed = validateName(name);
        ensureDefaultFolder(userId);
        if (favoriteFolderMapper.countByUserAndName(userId, trimmed) > 0) {
            throw new BusinessException("已存在同名收藏夹");
        }

        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(trimmed);
        folder.setIsDefault(0);
        folder.setIsPrivate(Boolean.TRUE.equals(isPrivate) ? 1 : 0);
        folder.setCreateTime(LocalDateTime.now());
        favoriteFolderMapper.insert(folder);
        return toVO(folder, 0L);
    }

    @Override
    @Transactional
    public FavoriteFolderVO update(Long userId, Long id, String name, Boolean isPrivate) {
        requireLogin(userId);
        FavoriteFolder folder = requireOwned(userId, id);
        String trimmed = name == null ? folder.getName() : validateName(name);
        Integer priv = isPrivate == null ? folder.getIsPrivate() : (isPrivate ? 1 : 0);
        if (name != null && !trimmed.equals(folder.getName())
                && favoriteFolderMapper.countByUserAndName(userId, trimmed) > 0) {
            throw new BusinessException("已存在同名收藏夹");
        }
        int rows = favoriteFolderMapper.update(id, userId, trimmed, priv);
        if (rows <= 0) {
            throw new BusinessException("收藏夹不存在");
        }
        folder.setName(trimmed);
        folder.setIsPrivate(priv);
        return toVO(folder, null);
    }

    @Override
    @Transactional
    public void delete(Long userId, Long id) {
        requireLogin(userId);
        FavoriteFolder folder = requireOwned(userId, id);
        if (folder.getIsDefault() != null && folder.getIsDefault() == 1) {
            throw new BusinessException("默认收藏夹不可删除");
        }
        int rows = favoriteFolderMapper.delete(id, userId);
        if (rows <= 0) {
            throw new BusinessException("收藏夹不存在");
        }
    }

    @Override
    public PageResult videos(Long userId, Long folderId, Integer page, Integer size) {
        requireLogin(userId);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        Long resolved = resolveFolderId(userId, folderId);
        FavoriteFolder folder = favoriteFolderMapper.selectById(resolved);
        int isDefault = folder != null && folder.getIsDefault() != null && folder.getIsDefault() == 1 ? 1 : 0;

        PageHelper.startPage(p, s);
        Page<Video> videoPage = videoFavoriteMapper.pageFolderVideos(userId, resolved, isDefault);
        return new PageResult(videoPage.getTotal(), videoPage.getResult());
    }

    @Override
    public Long ensureDefaultFolder(Long userId) {
        requireLogin(userId);
        FavoriteFolder existing = favoriteFolderMapper.selectDefault(userId);
        if (existing != null) {
            return existing.getId();
        }
        FavoriteFolder folder = new FavoriteFolder();
        folder.setUserId(userId);
        folder.setName(DEFAULT_FOLDER_NAME);
        folder.setIsDefault(1);
        folder.setIsPrivate(0);
        folder.setCreateTime(LocalDateTime.now());
        favoriteFolderMapper.insert(folder);
        log.info("为用户 {} 创建默认收藏夹 id={}", userId, folder.getId());
        return folder.getId();
    }

    @Override
    public Long resolveFolderId(Long userId, Long folderId) {
        requireLogin(userId);
        if (folderId == null) {
            return ensureDefaultFolder(userId);
        }
        FavoriteFolder folder = favoriteFolderMapper.selectById(folderId);
        if (folder == null) {
            throw new BusinessException("收藏夹不存在");
        }
        if (!Objects.equals(folder.getUserId(), userId)) {
            throw new BusinessException("无权操作他人的收藏夹");
        }
        return folder.getId();
    }

    private FavoriteFolder requireOwned(Long userId, Long id) {
        if (id == null) {
            throw new BusinessException("收藏夹ID不能为空");
        }
        FavoriteFolder folder = favoriteFolderMapper.selectById(id);
        if (folder == null || !Objects.equals(folder.getUserId(), userId)) {
            throw new BusinessException("收藏夹不存在");
        }
        return folder;
    }

    private String validateName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new BusinessException("收藏夹名称不能为空");
        }
        String trimmed = name.trim();
        if (trimmed.length() > MAX_NAME_LENGTH) {
            throw new BusinessException("收藏夹名称不能超过" + MAX_NAME_LENGTH + "个字");
        }
        return trimmed;
    }

    private void requireLogin(Long userId) {
        if (userId == null) {
            throw new BusinessException("请先登录");
        }
    }

    private FavoriteFolderVO toVO(FavoriteFolder folder, Long videoCount) {
        FavoriteFolderVO vo = new FavoriteFolderVO();
        vo.setId(folder.getId());
        vo.setName(folder.getName());
        vo.setIsDefault(folder.getIsDefault());
        vo.setIsPrivate(folder.getIsPrivate());
        vo.setCreateTime(folder.getCreateTime());
        vo.setVideoCount(videoCount);
        return vo;
    }
}
