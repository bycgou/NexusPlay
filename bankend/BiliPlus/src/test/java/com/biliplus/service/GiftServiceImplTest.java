package com.biliplus.service;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.*;
import com.biliplus.pojo.dto.GiftSendDTO;
import com.biliplus.pojo.entity.Gift;
import com.biliplus.pojo.entity.LiveRoom;
import com.biliplus.pojo.entity.UserWallet;
import com.biliplus.pojo.vo.GiftSendResultVO;
import com.biliplus.properties.LiveProperties;
import com.biliplus.service.Impl.GiftServiceImpl;
import com.biliplus.websocket.LiveWebSocketHandler;import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GiftServiceImplTest {

    @Mock
    private GiftMapper giftMapper;

    @Mock
    private LiveRoomMapper liveRoomMapper;

    @Mock
    private UserWalletMapper userWalletMapper;

    @Mock
    private GiftRecordMapper giftRecordMapper;

    @Mock
    private HostIncomeMapper hostIncomeMapper;

    @Mock
    private PeopleUserMapper peopleUserMapper;

    @Mock
    private LivePkService livePkService;

    @Mock
    private WalletService walletService;

    @Mock
    private LiveWebSocketHandler liveWebSocketHandler;

    @Spy
    private LiveProperties liveProperties = new LiveProperties();

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private GiftServiceImpl giftService;

    private LiveRoom liveRoom() {
        LiveRoom room = new LiveRoom();
        room.setId(1L);
        room.setUserId(100L);
        room.setStatus(1);
        return room;
    }

    private Gift gift() {
        Gift gift = new Gift();
        gift.setId(1L);
        gift.setName("小心心");
        gift.setIconUrl("/gifts/heart.png");
        gift.setPrice(10);
        gift.setEffectLevel(1);
        gift.setStatus(1);
        return gift;
    }

    @Test
    void sendGift_whenCountInvalid_shouldThrow() {
        GiftSendDTO dto = new GiftSendDTO();
        dto.setGiftId(1L);
        dto.setCount(0);
        assertThrows(BusinessException.class, () -> giftService.sendGift(9L, 1L, dto));
    }

    @Test
    void sendGift_whenRoomNotLive_shouldThrow() {
        LiveRoom room = liveRoom();
        room.setStatus(2);
        when(liveRoomMapper.selectById(1L)).thenReturn(room);

        GiftSendDTO dto = new GiftSendDTO();
        dto.setGiftId(1L);
        dto.setCount(1);
        assertThrows(BusinessException.class, () -> giftService.sendGift(9L, 1L, dto));
        verify(userWalletMapper, never()).deduct(anyLong(), anyLong());
    }

    @Test
    void sendGift_whenBalanceInsufficient_shouldThrow() {
        when(liveRoomMapper.selectById(1L)).thenReturn(liveRoom());
        when(giftMapper.selectById(1L)).thenReturn(gift());
        when(userWalletMapper.deduct(9L, 10)).thenReturn(0);

        GiftSendDTO dto = new GiftSendDTO();
        dto.setGiftId(1L);
        dto.setCount(1);
        assertThrows(BusinessException.class, () -> giftService.sendGift(9L, 1L, dto));
        verify(giftRecordMapper, never()).insert(any());
    }

    @Test
    void sendGift_shouldDeductAndWriteRecord() {
        when(liveRoomMapper.selectById(1L)).thenReturn(liveRoom());
        when(giftMapper.selectById(1L)).thenReturn(gift());
        when(userWalletMapper.deduct(9L, 10)).thenReturn(1);
        when(livePkService.getActive(1L)).thenReturn(null);
        when(userWalletMapper.selectByUserId(9L)).thenReturn(wallet(990));

        GiftSendDTO dto = new GiftSendDTO();
        dto.setGiftId(1L);
        dto.setCount(1);

        GiftSendResultVO result = giftService.sendGift(9L, 1L, dto);
        assertNotNull(result);
        assertEquals(1, result.getCombo());
        assertEquals(990, result.getBalance());
        verify(giftRecordMapper).insert(any());
        verify(hostIncomeMapper).addIncome(100L, 10L);
        // 送礼必须在同一事务里写两笔账变：支出方与主播收入方
        verify(walletService, times(2)).insertTransaction(anyLong(), anyInt(), anyLong(),
                anyLong(), anyString(), any(), anyString());
    }

    @Test
    void recharge_whenAmountIllegal_shouldThrow() {
        // 金额校验已下沉到充值订单链路，非法金额在创建订单时被拒
        when(walletService.createRechargeOrder(eq(9L), anyLong()))
                .thenThrow(new BusinessException("充值数量非法"));
        assertThrows(BusinessException.class, () -> giftService.recharge(9L, -1));
    }

    private UserWallet wallet(long balance) {
        UserWallet w = new UserWallet();
        w.setUserId(9L);
        w.setBalance(balance);
        return w;
    }
}
