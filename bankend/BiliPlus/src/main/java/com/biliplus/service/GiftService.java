package com.biliplus.service;

import com.biliplus.pojo.dto.GiftSendDTO;
import com.biliplus.pojo.entity.Gift;
import com.biliplus.pojo.entity.UserWallet;
import com.biliplus.pojo.vo.GiftSendResultVO;
import com.biliplus.result.PageResult;

import java.util.List;

public interface GiftService {

    List<Gift> listOnline();

    List<Gift> listAll();

    Gift create(Gift gift);

    Gift update(Long id, Gift gift);

    void delete(Long id);

    UserWallet getWallet(Long userId);

    UserWallet recharge(Long userId, long amount);

    GiftSendResultVO sendGift(Long senderId, Long roomId, GiftSendDTO dto);

    PageResult adminRecords(Long roomId, Long senderId, Long hostUserId, Integer page, Integer size);
}
