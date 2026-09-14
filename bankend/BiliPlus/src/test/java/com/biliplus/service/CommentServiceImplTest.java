package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.CommentMapper;
import com.biliplus.mapper.UserLikeMapper;
import com.biliplus.mapper.VideoMapper;
import com.biliplus.pojo.dto.userdto.CommentPostDTO;
import com.biliplus.pojo.entity.Comment;
import com.biliplus.pojo.entity.UserLike;
import com.biliplus.service.Impl.CommentServiceImpl;
import com.biliplus.utils.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private VideoMapper videoMapper;

    @Mock
    private UserLikeMapper userLikeMapper;

    @InjectMocks
    private CommentServiceImpl commentService;

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void saveComment_whenNotLogin_shouldThrow() {
        CommentPostDTO dto = new CommentPostDTO();
        dto.setVideoId(1L);
        dto.setContent("hello");
        assertThrows(BusinessException.class, () -> commentService.saveComment(dto));
        verify(commentMapper, never()).save(any());
    }

    @Test
    void saveComment_shouldUseJwtUserIdAndParentId() {
        UserContext.setCurrentUserId(42L);
        CommentPostDTO dto = new CommentPostDTO();
        dto.setVideoId(10L);
        dto.setContent("  nice  ");
        dto.setParentId(3L);

        commentService.saveComment(dto);

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);
        verify(commentMapper).save(captor.capture());
        Comment saved = captor.getValue();
        assertEquals(42L, saved.getUserId());
        assertEquals(10L, saved.getVideoId());
        assertEquals(3L, saved.getParentId());
        assertEquals("nice", saved.getContent());
        verify(videoMapper).increaseCommentCount(10L);
    }

    @Test
    void deleteComment_whenNotOwner_shouldThrow() {
        UserContext.setCurrentUserId(1L);
        Comment existing = new Comment();
        existing.setId(5L);
        existing.setUserId(99L);
        existing.setVideoId(1L);
        existing.setStatus((byte) 1);
        when(commentMapper.selectById(5L)).thenReturn(existing);

        CommentPostDTO dto = new CommentPostDTO();
        dto.setVideoId(1L);
        dto.setCommentId(5L);

        assertThrows(BusinessException.class, () -> commentService.deleteComment(dto));
        verify(commentMapper, never()).softDelete(anyLong(), anyLong());
    }

    @Test
    void deleteComment_whenOwner_shouldSoftDelete() {
        UserContext.setCurrentUserId(1L);
        Comment existing = new Comment();
        existing.setId(5L);
        existing.setUserId(1L);
        existing.setVideoId(2L);
        existing.setStatus((byte) 1);
        when(commentMapper.selectById(5L)).thenReturn(existing);

        CommentPostDTO dto = new CommentPostDTO();
        dto.setVideoId(2L);
        dto.setCommentId(5L);

        commentService.deleteComment(dto);
        verify(commentMapper).softDelete(5L, 1L);
        verify(videoMapper).decreaseCommentCount(2L);
    }

    @Test
    void toggleCommentLike_shouldInsertWhenNotLiked() {
        UserContext.setCurrentUserId(7L);
        Comment c = new Comment();
        c.setId(8L);
        c.setStatus((byte) 1);
        c.setLikeCount(2);
        when(commentMapper.selectById(8L)).thenReturn(c);
        when(userLikeMapper.countByUserAndTarget(7L, 8L, 2)).thenReturn(0);

        Map<String, Object> result = commentService.toggleCommentLike(8L, 7L);

        assertEquals(true, result.get("liked"));
        assertEquals(3, result.get("likeCount"));
        verify(userLikeMapper).insert(any(UserLike.class));
        verify(commentMapper).changeLikeCount(8L, 1);
    }

    @Test
    void toggleCommentLike_shouldDeleteWhenLiked() {
        UserContext.setCurrentUserId(7L);
        Comment c = new Comment();
        c.setId(8L);
        c.setStatus((byte) 1);
        c.setLikeCount(2);
        when(commentMapper.selectById(8L)).thenReturn(c);
        when(userLikeMapper.countByUserAndTarget(7L, 8L, 2)).thenReturn(1);

        Map<String, Object> result = commentService.toggleCommentLike(8L, 7L);

        assertEquals(false, result.get("liked"));
        assertEquals(1, result.get("likeCount"));
        verify(userLikeMapper).delete(7L, 8L, 2);
        verify(commentMapper).changeLikeCount(8L, -1);
    }
}
