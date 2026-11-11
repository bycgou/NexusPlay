package com.biliplus.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MediaUrlUtilTest {

    @Test
    void rewrite_shouldReplaceLocalOriginWithExternalBase() {
        assertEquals(
                "https://example.com/avatar/1.png",
                MediaUrlUtil.rewrite("http://localhost:21160/avatar/1.png", "https://example.com"));
        assertEquals(
                "https://example.com/avatar/1.png",
                MediaUrlUtil.rewrite("http://127.0.0.1:8081/avatar/1.png", "https://example.com"));
    }

    @Test
    void rewrite_shouldKeepPathAndDropOrigin() {
        assertEquals(
                "https://example.com/video-files/a.mp4",
                MediaUrlUtil.rewrite("http://localhost:21160/video-files/a.mp4", "https://example.com/"));
    }

    @Test
    void rewrite_shouldLeaveRemoteUrlUntouched() {
        assertEquals(
                "https://cdn.example.com/a.png",
                MediaUrlUtil.rewrite("https://cdn.example.com/a.png", "https://example.com"));
    }

    @Test
    void rewrite_shouldReturnEmptyInputAsIs() {
        assertEquals(null, MediaUrlUtil.rewrite(null, "https://example.com"));
        assertEquals("", MediaUrlUtil.rewrite("", "https://example.com"));
        assertEquals("http://localhost:21160/a.png", MediaUrlUtil.rewrite("http://localhost:21160/a.png", ""));
    }
}
