package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.FavoriteFolderMapper;
import com.biliplus.mapper.VideoFavoriteMapper;
import com.biliplus.pojo.entity.FavoriteFolder;
import com.biliplus.service.Impl.FavoriteFolderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FavoriteFolderServiceImplTest {

    @Mock
    private FavoriteFolderMapper favoriteFolderMapper;

    @Mock
    private VideoFavoriteMapper videoFavoriteMapper;

    @InjectMocks
    private FavoriteFolderServiceImpl favoriteFolderService;

    private FavoriteFolder folder(long id, long userId, int isDefault) {
        FavoriteFolder f = new FavoriteFolder();
        f.setId(id);
        f.setUserId(userId);
        f.setName(isDefault == 1 ? "默认收藏" : "我的收藏");
        f.setIsDefault(isDefault);
        f.setIsPrivate(0);
        return f;
    }

    @Test
    void ensureDefaultFolder_whenExists_shouldReuse() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(folder(1L, 9L, 1));

        assertEquals(1L, favoriteFolderService.ensureDefaultFolder(9L));
        verify(favoriteFolderMapper, never()).insert(any());
    }

    @Test
    void ensureDefaultFolder_whenMissing_shouldCreate() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(null);
        doAnswer(inv -> {
            ((FavoriteFolder) inv.getArgument(0)).setId(2L);
            return null;
        }).when(favoriteFolderMapper).insert(any(FavoriteFolder.class));

        assertEquals(2L, favoriteFolderService.ensureDefaultFolder(9L));

        ArgumentCaptor<FavoriteFolder> captor = ArgumentCaptor.forClass(FavoriteFolder.class);
        verify(favoriteFolderMapper).insert(captor.capture());
        assertEquals(1, captor.getValue().getIsDefault());
        assertEquals(9L, captor.getValue().getUserId());
    }

    @Test
    void ensureDefaultFolder_whenNotLogin_shouldThrow() {
        assertThrows(BusinessException.class, () -> favoriteFolderService.ensureDefaultFolder(null));
    }

    @Test
    void create_whenDuplicateName_shouldThrow() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(folder(1L, 9L, 1));
        when(favoriteFolderMapper.countByUserAndName(9L, "游戏")).thenReturn(1);

        assertThrows(BusinessException.class, () -> favoriteFolderService.create(9L, "游戏", false));
        verify(favoriteFolderMapper, never()).insert(any());
    }

    @Test
    void create_whenNameBlank_shouldThrow() {
        assertThrows(BusinessException.class, () -> favoriteFolderService.create(9L, "  ", false));
    }

    @Test
    void create_shouldInsertNonDefaultFolder() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(folder(1L, 9L, 1));
        when(favoriteFolderMapper.countByUserAndName(9L, "游戏")).thenReturn(0);

        favoriteFolderService.create(9L, " 游戏 ", true);

        ArgumentCaptor<FavoriteFolder> captor = ArgumentCaptor.forClass(FavoriteFolder.class);
        verify(favoriteFolderMapper).insert(captor.capture());
        assertEquals("游戏", captor.getValue().getName());
        assertEquals(0, captor.getValue().getIsDefault());
        assertEquals(1, captor.getValue().getIsPrivate());
    }

    @Test
    void delete_whenDefaultFolder_shouldThrow() {
        when(favoriteFolderMapper.selectById(1L)).thenReturn(folder(1L, 9L, 1));

        assertThrows(BusinessException.class, () -> favoriteFolderService.delete(9L, 1L));
        verify(favoriteFolderMapper, never()).delete(anyLong(), anyLong());
    }

    @Test
    void delete_whenNotOwner_shouldThrow() {
        when(favoriteFolderMapper.selectById(2L)).thenReturn(folder(2L, 100L, 0));

        assertThrows(BusinessException.class, () -> favoriteFolderService.delete(9L, 2L));
        verify(favoriteFolderMapper, never()).delete(anyLong(), anyLong());
    }

    @Test
    void delete_shouldRemoveOwnNonDefaultFolder() {
        when(favoriteFolderMapper.selectById(2L)).thenReturn(folder(2L, 9L, 0));
        when(favoriteFolderMapper.delete(2L, 9L)).thenReturn(1);

        favoriteFolderService.delete(9L, 2L);

        verify(favoriteFolderMapper).delete(2L, 9L);
    }

    @Test
    void resolveFolderId_whenNull_shouldFallbackToDefault() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(folder(1L, 9L, 1));

        assertEquals(1L, favoriteFolderService.resolveFolderId(9L, null));
    }

    @Test
    void resolveFolderId_whenOtherUsersFolder_shouldThrow() {
        when(favoriteFolderMapper.selectById(2L)).thenReturn(folder(2L, 100L, 0));

        assertThrows(BusinessException.class, () -> favoriteFolderService.resolveFolderId(9L, 2L));
    }

    @Test
    void resolveFolderId_whenMissing_shouldThrow() {
        when(favoriteFolderMapper.selectById(99L)).thenReturn(null);

        assertThrows(BusinessException.class, () -> favoriteFolderService.resolveFolderId(9L, 99L));
    }

    @Test
    void videos_shouldTreatNullFolderIdAsDefaultFolder() {
        when(favoriteFolderMapper.selectDefault(9L)).thenReturn(folder(1L, 9L, 1));
        when(favoriteFolderMapper.selectById(1L)).thenReturn(folder(1L, 9L, 1));
        when(videoFavoriteMapper.pageFolderVideos(eq(9L), eq(1L), eq(1)))
                .thenReturn(new com.github.pagehelper.Page<>(1, 20));

        favoriteFolderService.videos(9L, null, 1, 20);

        // isDefault=1 时才能把 folder_id 为 NULL 的历史收藏一起查出来
        verify(videoFavoriteMapper).pageFolderVideos(9L, 1L, 1);
    }
}
