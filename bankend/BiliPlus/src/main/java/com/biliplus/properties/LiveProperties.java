package com.biliplus.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** 直播 / SRS / 礼物 / PK 配置 */
@Data
@Component
@ConfigurationProperties(prefix = "live")
public class LiveProperties {

    private Srs srs = new Srs();
    private Pk pk = new Pk();
    private Gift gift = new Gift();

    @Data
    public static class Srs {
        private String rtmpHost = "localhost";
        private Integer rtmpPort = 1935;
        private String httpFlvBase = "http://localhost:8080/live";
        private Boolean rtcEnabled = false;
    }

    @Data
    public static class Pk {
        private Integer defaultDurationSec = 300;
    }

    @Data
    public static class Gift {
        private Integer maxCountPerRequest = 100;
    }

    public String buildPushUrl(String streamKey) {
        return String.format("rtmp://%s:%d/live/%s",
                srs.getRtmpHost(), srs.getRtmpPort(), streamKey);
    }

    /** SRS 默认 HTTP-FLV：/live/{streamKey}.flv（不要写成 .live.flv） */
    public String buildPlayUrl(String streamKey) {
        return String.format("%s/%s.flv", srs.getHttpFlvBase(), streamKey);
    }
}
