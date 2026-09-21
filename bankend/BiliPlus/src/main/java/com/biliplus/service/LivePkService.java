package com.biliplus.service;

import com.biliplus.pojo.entity.LivePk;

public interface LivePkService {

    LivePk invite(Long hostId, Long opponentRoomId);

    LivePk response(Long hostId, Long pkId, boolean agree);

    LivePk getActive(Long roomId);

    LivePk end(Long hostId, Long pkId);

    /** 送礼加分，由 GiftService 调用 */
    LivePk addScore(Long pkId, Long roomWhich, int score);

    void forceCloseByRoom(Long roomId);
}
