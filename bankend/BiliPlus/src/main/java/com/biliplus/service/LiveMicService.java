package com.biliplus.service;

import com.biliplus.pojo.entity.LiveMicSession;

import java.util.List;
import java.util.Map;

public interface LiveMicService {

    LiveMicSession apply(Long guestId, Long roomId);

    Map<String, Object> accept(Long hostId, Long sessionId, boolean agree);

    void leave(Long userId, Long roomId);

    List<LiveMicSession> listPending(Long roomId);

    void forceCloseByRoom(Long roomId);
}
