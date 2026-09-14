package com.biliplus.utils;

import lombok.extern.slf4j.Slf4j;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FrameGrabber;

import java.io.File;

/*
*视频时长解析工具
* */
@Slf4j
public class VideoMetadataUtil {
    public static double getVideoDuration(File videoFile){
        if(!videoFile.exists() || !videoFile.isFile()){
            log.error("文件不存在{}",videoFile.getParentFile());
            return 0.0;
        }
        FFmpegFrameGrabber grabber = null;
        try {
            grabber = new FFmpegFrameGrabber(videoFile);
            grabber.start();

            // 时长;微秒->(1秒=1000000微秒)
            long durationUs = grabber.getLengthInTime();
            double duration = durationUs / 1000000.0;
            return Math.round(duration*10)/10.0;
        }catch (FrameGrabber.Exception e){
            log.error("解析视频时长异常{}",e.getMessage());
            return 0.0;
        }finally {
            // 关闭资源,避免内存泄漏
            if (grabber != null){
                try {
                    grabber.stop();
                    grabber.release();
                }catch (FrameGrabber.Exception e){
                    log.error("关闭资源异常{}",e.getMessage());
                }
            }
        }
    }


    /**
     * 格式化时长：秒 → 分:秒（如 123.4 秒 → "2:03"）
     * @param seconds 视频时长（秒）
     * @return 格式化后的字符串
     */
    public static String formatDuration(double seconds) {
        int minutes = (int) seconds / 60;
        int secs = (int) seconds % 60;
        return String.format("%d:%02d", minutes, secs); // 补零，如 1分3秒 → "1:03"
    }

}
