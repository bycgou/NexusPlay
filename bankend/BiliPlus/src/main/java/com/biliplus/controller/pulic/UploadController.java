package com.biliplus.controller.pulic;

import com.biliplus.pojo.dto.ChunkUploadDTO;
import com.biliplus.pojo.dto.MergeDTO;
import com.biliplus.pojo.vo.VideoUploadResultVO;
import com.biliplus.result.Result;
import com.biliplus.utils.VideoMetadataUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/pp/upload")
public class UploadController {

    @Value("${video.upload.base-path}")
    private String basePath;

    @Value("${video.upload.temp-path}")
    private String tempPath;

    @Value("${video.upload.access-prefix}")
    private String videoAccessPrefix;

    // 图片相关
    @Value("${image.upload.base-path}")
    private String imageBasepath;

    @Value("${image.upload.access-prefix}")
    private String imageAccessPrefix;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    // 1. 接受分片
    @PostMapping("/chunk")
    public Result<Void> uploadChunk(
            @RequestParam("fileInfo") String fileInfoJson,
            @RequestParam("chunkData") MultipartFile chunkData) throws IOException {

        log.info("上传分片：{}", fileInfoJson);
        ChunkUploadDTO fileInfo = com.alibaba.fastjson2.JSON.parseObject(fileInfoJson, ChunkUploadDTO.class);
        String md5 = sanitizeMd5(fileInfo.getMd5());
        if (md5 == null) {
            return Result.error("非法的文件标识");
        }
        Integer chunkIndex = fileInfo.getChunkIndex();
        if (chunkIndex == null || chunkIndex < 0) {
            return Result.error("非法的分片序号");
        }

        String tempDir = tempPath + md5 + "/";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String chunkFileName = md5 + "_" + chunkIndex;
        File chunkFile = new File(tempDir + chunkFileName);
        chunkData.transferTo(chunkFile);

        return Result.success(null);
    }

    private static String sanitizeMd5(String md5) {
        if (md5 == null || md5.isEmpty() || md5.length() > 64) {
            return null;
        }
        // 仅允许十六进制，防止路径穿越
        if (!md5.matches("[0-9a-fA-F]+")) {
            return null;
        }
        return md5.toLowerCase();
    }

    // 2. 合并分片
    @PostMapping("/merge")
    public Result<VideoUploadResultVO> mergeChunks(@RequestBody MergeDTO mergeDTO) throws IOException {
        log.info("合并分片：{}", mergeDTO);
        String md5 = sanitizeMd5(mergeDTO.getMd5());
        if (md5 == null) {
            return Result.error("非法的文件标识");
        }
        String fileName = mergeDTO.getFileName();
        Integer totalChunks = mergeDTO.getTotalChunks();
        if (fileName == null || !fileName.contains(".")) {
            return Result.error("文件名不合法");
        }
        if (totalChunks == null || totalChunks <= 0) {
            return Result.error("分片数量不合法");
        }

        String tempDir = tempPath + md5 + "/";
        File dir = new File(tempDir);
        if (!dir.exists()) {
            return Result.error("分片文件不存在");
        }

        File[] chunkFiles = dir.listFiles();
        if (chunkFiles == null || chunkFiles.length != totalChunks) {
            return Result.error("分片数量不完整");
        }

        List<File> chunkList = new ArrayList<>(Arrays.asList(chunkFiles));
        chunkList.sort(Comparator.comparingInt(f -> Integer.parseInt(f.getName().split("_")[1])));

        String fileExt = fileName.substring(fileName.lastIndexOf("."));
        String newFileName = UUID.randomUUID() + fileExt;
        String finalFilePath = basePath + newFileName;
        File finalFile = new File(finalFilePath);
        if (!finalFile.getParentFile().exists()) {
            finalFile.getParentFile().mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(finalFile);
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {

            for (File chunk : chunkList) {
                try (FileInputStream fis = new FileInputStream(chunk);
                     BufferedInputStream bis = new BufferedInputStream(fis)) {

                    byte[] buffer = new byte[1024 * 1024];
                    int len;
                    while ((len = bis.read(buffer)) != -1) {
                        bos.write(buffer, 0, len);
                    }
                }
                chunk.delete(); // 删除分片
            }
        }

        double duration = VideoMetadataUtil.getVideoDuration(finalFile);
        String formattedDuration = VideoMetadataUtil.formatDuration(duration);
        log.info("视频合并完成,文件名:{},视频时长：{}秒({})", newFileName, duration, formattedDuration);

        Files.deleteIfExists(Paths.get(tempDir)); // 删除临时目录（分片已删完）

        // 存相对路径：局域网/多域名部署时由前端站点同源解析，再经 Vite/Nginx 转发到本服务，
        // 避免把 localhost 之类的主机名写进库，导致换一个访问域名就全部失效
        String fileUrl = videoAccessPrefix + newFileName;

        VideoUploadResultVO vo = new VideoUploadResultVO();
        vo.setFileUrl(fileUrl);
        vo.setDuration(duration);
        vo.setFormatDuration(formattedDuration);
        return Result.success(vo);
    }

    // 3. 图片上传
    @PostMapping("/cover")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            if (file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("文件大小不能超过5MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || !Arrays.asList(ALLOWED_TYPES).contains(contentType)) {
                return Result.error("仅支持 JPG/PNG/GIF/WebP 格式");
            }

            Path uploadPath = Paths.get(imageBasepath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            // 存相对路径，理由同 mergeChunks
            String fileUrl = imageAccessPrefix + filename;

            log.info("图片上传成功,文件名:{},访问地址:{}", filename, fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("上传封面失败", e);
            return Result.error("上传失败");
        }
    }
}