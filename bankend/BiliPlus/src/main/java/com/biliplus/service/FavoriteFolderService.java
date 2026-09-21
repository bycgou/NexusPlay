package com.biliplus.service;

import com.biliplus.pojo.vo.FavoriteFolderVO;
import com.biliplus.result.PageResult;

import java.util.List;

public interface FavoriteFolderService {

    /** 我的收藏夹列表；尚无默认夹时自动补建 */
    List<FavoriteFolderVO> list(Long userId);

    FavoriteFolderVO create(Long userId, String name, Boolean isPrivate);

    FavoriteFolderVO update(Long userId, Long id, String name, Boolean isPrivate);

    void delete(Long userId, Long id);

    /** 夹内视频分页 */
    PageResult videos(Long userId, Long folderId, Integer page, Integer size);

    /** 取默认收藏夹，不存在则创建 */
    Long ensureDefaultFolder(Long userId);

    /** 校验 folderId 归属并返回；folderId 为空时返回默认夹 ID */
    Long resolveFolderId(Long userId, Long folderId);
}
