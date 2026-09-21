package com.biliplus.service.Impl;

import com.biliplus.constant.WalletTx;
import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.*;
import com.biliplus.pojo.dto.GiftSendDTO;
import com.biliplus.pojo.entity.*;
import com.biliplus.pojo.vo.GiftSendResultVO;
import com.biliplus.pojo.vo.RechargeOrderVO;
import com.biliplus.properties.LiveProperties;
import com.biliplus.result.PageResult;
import com.biliplus.service.GiftService;
import com.biliplus.service.LivePkService;
import com.biliplus.service.WalletService;
import com.biliplus.websocket.LiveWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class GiftServiceImpl implements GiftService {

    @Autowired
    private GiftMapper giftMapper;

    @Autowired
    private LiveRoomMapper liveRoomMapper;

    @Autowired
    private UserWalletMapper userWalletMapper;

    @Autowired
    private GiftRecordMapper giftRecordMapper;

    @Autowired
    private HostIncomeMapper hostIncomeMapper;

    @Autowired
    private PeopleUserMapper peopleUserMapper;

    @Autowired
    private LivePkService livePkService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private LiveWebSocketHandler liveWebSocketHandler;

    @Autowired
    private LiveProperties liveProperties;

    @Autowired
    private ObjectMapper objectMapper;

    /** key = senderId:giftId -> ComboState */
    private static final Map<String, ComboState> COMBO = new ConcurrentHashMap<>();
    private static final long COMBO_WINDOW_MS = 5000L;

    private static class ComboState {
        int combo;
        long lastTs;
    }

    @Override
    public List<Gift> listOnline() {
        return giftMapper.listOnline();
    }

    @Override
    public List<Gift> listAll() {
        return giftMapper.listAll();
    }

    @Override
    public Gift create(Gift gift) {
        validateGift(gift);
        if (gift.getCreateTime() == null) {
            gift.setCreateTime(LocalDateTime.now());
        }
        if (gift.getStatus() == null) {
            gift.setStatus(1);
        }
        if (gift.getEffectLevel() == null) {
            gift.setEffectLevel(1);
        }
        if (gift.getSortOrder() == null) {
            gift.setSortOrder(0);
        }
        giftMapper.insert(gift);
        return gift;
    }

    @Override
    public Gift update(Long id, Gift gift) {
        if (id == null) {
            throw new BusinessException("礼物ID不能为空");
        }
        if (giftMapper.selectById(id) == null) {
            throw new BusinessException("礼物不存在");
        }
        validateGift(gift);
        gift.setId(id);
        if (gift.getStatus() == null) {
            gift.setStatus(1);
        }
        int rows = giftMapper.update(gift);
        if (rows <= 0) {
            throw new BusinessException("更新失败");
        }
        return giftMapper.selectById(id);
    }

    @Override
    public void delete(Long id) {
        if (giftMapper.selectById(id) == null) {
            throw new BusinessException("礼物不存在");
        }
        giftMapper.deleteById(id);
    }

    @Override
    public UserWallet getWallet(Long userId) {
        userWalletMapper.ensureWallet(userId);
        return userWalletMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public UserWallet recharge(Long userId, long amount) {
        // 兼容旧接口：内部走「创建订单 + 模拟支付」，保证与充值订单同一条账变链路
        RechargeOrderVO order = walletService.createRechargeOrder(userId, amount);
        walletService.payRechargeOrder(userId, order.getOrderNo());
        return userWalletMapper.selectByUserId(userId);
    }

    @Override
    @Transactional
    public GiftSendResultVO sendGift(Long senderId, Long roomId, GiftSendDTO dto) {
        if (senderId == null) {
            throw new BusinessException("请先登录");
        }
        if (dto == null || dto.getGiftId() == null) {
            throw new BusinessException("请选择礼物");
        }
        int count = dto.getCount() == null ? 1 : dto.getCount();
        int max = liveProperties.getGift().getMaxCountPerRequest();
        if (count < 1 || count > max) {
            throw new BusinessException("数量必须在 1-" + max + " 之间");
        }
        LiveRoom room = liveRoomMapper.selectById(roomId);
        if (room == null) {
            throw new BusinessException("直播间不存在");
        }
        if (room.getStatus() == null || room.getStatus() != 1) {
            throw new BusinessException("该直播间未开播");
        }
        Gift gift = giftMapper.selectById(dto.getGiftId());
        if (gift == null || gift.getStatus() == null || gift.getStatus() != 1) {
            throw new BusinessException("礼物不存在或已下架");
        }
        long total = (long) gift.getPrice() * count;
        if (total > Integer.MAX_VALUE) {
            throw new BusinessException("金额过大");
        }

        userWalletMapper.ensureWallet(senderId);
        int deducted = userWalletMapper.deduct(senderId, total);
        if (deducted <= 0) {
            throw new BusinessException("余额不足");
        }

        hostIncomeMapper.ensureIncome(room.getUserId());
        hostIncomeMapper.addIncome(room.getUserId(), total);

        LivePk activePk = livePkService.getActive(roomId);
        Long pkId = activePk == null ? null : activePk.getId();

        GiftRecord record = new GiftRecord();
        record.setLiveRoomId(roomId);
        record.setPkId(pkId);
        record.setGiftId(gift.getId());
        record.setSenderId(senderId);
        record.setHostUserId(room.getUserId());
        record.setUnitPrice(gift.getPrice());
        record.setCount(count);
        record.setTotalPrice((int) total);
        record.setCreateTime(LocalDateTime.now());
        giftRecordMapper.insert(record);

        int combo = nextCombo(senderId, gift.getId());

        if (activePk != null) {
            livePkService.addScore(activePk.getId(), roomId, (int) total);
        }

        User sender = peopleUserMapper.getUserById(senderId);
        String senderName = sender == null ? ("用户" + senderId)
                : (StringUtils.hasText(sender.getNickname()) ? sender.getNickname() : sender.getUsername());

        // ===== 账变：送礼扣款与主播收入都必须留痕，否则管理端无法对账 =====
        UserWallet wallet = userWalletMapper.selectByUserId(senderId);
        long senderBalanceAfter = wallet == null || wallet.getBalance() == null ? 0L : wallet.getBalance();
        walletService.insertTransaction(senderId, WalletTx.TYPE_GIFT, -total, senderBalanceAfter,
                WalletTx.BIZ_GIFT, record.getId(),
                "送出 " + gift.getName() + " x" + count);

        HostIncome hostIncome = hostIncomeMapper.selectByUserId(room.getUserId());
        long hostBalanceAfter = hostIncome == null || hostIncome.getTotalIncome() == null
                ? 0L : hostIncome.getTotalIncome();
        walletService.insertTransaction(room.getUserId(), WalletTx.TYPE_HOST_INCOME, total, hostBalanceAfter,
                WalletTx.BIZ_HOST_INCOME, record.getId(),
                "收到 " + senderName + " 的 " + gift.getName() + " x" + count);

        try {
            ObjectNode node = objectMapper.createObjectNode();
            node.put("type", "gift");
            node.put("roomId", roomId);
            node.put("senderId", senderId);
            node.put("senderName", senderName);
            node.put("giftId", gift.getId());
            node.put("giftName", gift.getName());
            node.put("icon", gift.getIconUrl());
            node.put("count", count);
            node.put("combo", combo);
            node.put("level", gift.getEffectLevel() == null ? 1 : gift.getEffectLevel());
            liveWebSocketHandler.broadcast(roomId, node.toString());
        } catch (Exception e) {
            log.warn("广播 gift 失败", e);
        }

        GiftSendResultVO result = new GiftSendResultVO();
        result.setRecordId(record.getId());
        result.setBalance(wallet == null ? null : wallet.getBalance());
        result.setCombo(combo);
        result.setEffectLevel(gift.getEffectLevel());
        return result;
    }

    @Override
    public PageResult adminRecords(Long roomId, Long senderId, Long hostUserId, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        int offset = (p - 1) * s;
        List<GiftRecord> records = giftRecordMapper.adminList(roomId, senderId, hostUserId, offset, s);
        long total = giftRecordMapper.adminCount(roomId, senderId, hostUserId);
        return new PageResult(total, records);
    }

    private void validateGift(Gift gift) {
        if (gift == null || !StringUtils.hasText(gift.getName())) {
            throw new BusinessException("礼物名称不能为空");
        }
        if (!StringUtils.hasText(gift.getIconUrl())) {
            throw new BusinessException("礼物图标不能为空");
        }
        if (gift.getPrice() == null || gift.getPrice() <= 0) {
            throw new BusinessException("礼物价格必须大于0");
        }
    }

    private int nextCombo(Long senderId, Long giftId) {
        long now = System.currentTimeMillis();
        String key = senderId + ":" + giftId;
        ComboState state = COMBO.compute(key, (k, old) -> {
            if (old == null || now - old.lastTs > COMBO_WINDOW_MS) {
                ComboState s = new ComboState();
                s.combo = 1;
                s.lastTs = now;
                return s;
            }
            old.combo++;
            old.lastTs = now;
            return old;
        });
        return state.combo;
    }
}
