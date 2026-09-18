package com.biliplus.controller.admin;

import com.biliplus.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

/** 管理端图片上传（轮播图 / 分区图标 / 礼物图标） */
@Slf4j
@RestController
@RequestMapping("/admin/upload")
public class AdminUploadController {

    @Value("${app.external-url}")
    private String externalUrl;

    @Value("${image.upload.base-path}")
    private String imageBasepath;

    @Value("${image.upload.access-prefix}")
    private String imageAccessPrefix;

    private static final String[] ALLOWED_TYPES = {"image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"};
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @PostMapping("/image")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return Result.error("文件不能为空");
            }
            if (file.getSize() > MAX_FILE_SIZE) {
                return Result.error("文件大小不能超过5MB");
            }
            String contentType = file.getContentType();
            if (contentType == null || !Arrays.asList(ALLOWED_TYPES).contains(contentType)) {
                return Result.error("仅支持 JPG/PNG/GIF/WebP/SVG 格式");
            }

            Path uploadPath = Paths.get(imageBasepath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
            }
            String filename = UUID.randomUUID().toString().replace("-", "") + extension;
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = externalUrl + imageAccessPrefix + filename;
            log.info("管理端图片上传成功: {}", fileUrl);
            return Result.success(fileUrl);
        } catch (Exception e) {
            log.error("管理端图片上传失败", e);
            return Result.error("上传失败");
        }
    }
}
