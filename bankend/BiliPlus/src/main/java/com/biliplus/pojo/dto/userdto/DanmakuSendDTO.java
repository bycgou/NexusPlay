package com.biliplus.pojo.dto.userdto;

import lombok.Data;

@Data
public class DanmakuSendDTO {
    private Long videoId; // 视频id
    private Long userId; // 用户id
    private String text; // 弹幕内容
    private Integer time; // 弹幕时间
    private String color;   // 弹幕颜色
    private String type;  // 弹幕类型 1:滚动 2:顶部 3:底部
}
