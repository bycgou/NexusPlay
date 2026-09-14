package com.biliplus.controller.user;

import com.biliplus.constant.AllConstant;

import com.biliplus.pojo.dto.userdto.UserDTO;
import com.biliplus.pojo.dto.userdto.UserRegisterDTO;
import com.biliplus.pojo.dto.userdto.VideoUploadDTO;
import com.biliplus.pojo.entity.User;
import com.biliplus.pojo.entity.Video;
import com.biliplus.pojo.vo.PoepleLoginVO;
import com.biliplus.pojo.vo.VideoUploadVO;
import com.biliplus.properties.JwtPeopleProperties;
import com.biliplus.result.Result;
import com.biliplus.service.EmailCodeService;
import com.biliplus.service.PeopleUserService;
import com.biliplus.service.ValidateCodeService;
import com.biliplus.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/pp/people")
public class PeopleUserController {

    @Autowired
    private ValidateCodeService validateCodeService;
    @Autowired
    private EmailCodeService emailCodeService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    PeopleUserService peopleUserService;

    @Autowired
    private JwtPeopleProperties jwtPeopleProperties;

    /*
    生成并发送图片验证码
     */
    @GetMapping("/captcha/image")
    public Result<Map<String, String>> generateImageCaptcha(
            @RequestParam(required = false, defaultValue = "0") Integer type
    ) {
        try {
            // 调用服务生成验证码（返回captchaId和imageBase64）
            Map<String, String> captchaInfo = validateCodeService.generateValidateCode();

            // 日志记录（仅记录标识，不暴露验证码内容）
            log.info("生成验证码成功，captchaId：{}，类型：{}",
                    captchaInfo.get("captchaId"), type);

            // 用success方法包装数据返回（符合Result类规范）
            return Result.success(captchaInfo);

        } catch (Exception e) {
            // 异常时返回错误信息
            log.error("生成验证码失败", e);
            return Result.error("验证码生成失败，请重试");
        }
    }

     // 发送邮箱验证码
    @PostMapping ("/email")
    public Result sendEmailCode(@RequestBody UserRegisterDTO userRegisterDTO){
        // 验证码校验及发送邮箱验证码
        String captchaId = userRegisterDTO.getCaptchaId();
        String ImageCaptcha = userRegisterDTO.getImageCaptcha();
        String email = userRegisterDTO.getEmail();
        log.info("用户输入的邮箱地址{},验证码key{},验证码值{}",email,captchaId,ImageCaptcha);


        // 1. 定义Redis中图片验证码的key（与验证码生成时的key规则一致）
        String imageCaptchaRedisKey = AllConstant.CAPTCHA_REDIS_PREFIX + captchaId;

        try {
            // 2. 从Redis获取图片验证码（JWT架构无Session，完全依赖Redis）
            String storedImageCode = stringRedisTemplate.opsForValue().get(imageCaptchaRedisKey);
            log.info("图片验证码校验 - 用户输入：{}，Redis存储：{}，邮箱：{}",
                    ImageCaptcha, storedImageCode, email);

            // 3. 图片验证码校验逻辑
            if (storedImageCode == null) {
                return Result.error("图片验证码已过期，请重新输入");
            }
            // 忽略大小写校验，提升用户体验
            if (!ImageCaptcha.equalsIgnoreCase(storedImageCode)) {
                return Result.error("图片验证码错误");
            }

            // 4. 校验通过：发送邮箱验证码（调用服务层逻辑）
            emailCodeService.sendEmailCode(email);

            // 5. 关键修正：删除Redis中的图片验证码（防止重复使用，原代码删错了storedImageCode值，需删key）
            Boolean deleteSuccess = stringRedisTemplate.delete(imageCaptchaRedisKey);
            if (deleteSuccess) {
                log.info("图片验证码已从Redis删除，key：{}", imageCaptchaRedisKey);
            }

            // 6. 返回成功响应（无数据返回，按ResponseVO规范封装）
            return Result.success("邮箱验证码已发送，请查收");

        } catch (Exception e) {
            // 业务异常：返回具体错误信息（如验证码过期、不正确）
            log.warn("发送邮箱验证码失败（业务异常）：{}", e.getMessage());
            return Result.error(e.getMessage());
        }

    }
    /**
     * 注册用户
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        String password = userRegisterDTO.getPassword();
        String emailCaptcha = userRegisterDTO.getEmailCaptcha();
        // 日志脱敏：不输出密码明文
        log.info("用户注册请求: email={}, passwordLength={}", email, password == null ? 0 : password.length());
        try {
            peopleUserService.PeopleRegister(email, password, emailCaptcha);
            return Result.success();
        } catch (Exception e) {
            log.warn("用户注册失败: email={}, error={}", email, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result<PoepleLoginVO> login(@RequestBody UserRegisterDTO userRegisterDTO) {
        String email = userRegisterDTO.getEmail();
        // 日志脱敏：不输出密码
        log.info("用户登录请求: email={}", email);
        // 调用login方法
        User user = peopleUserService.userlogin(userRegisterDTO);
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        // 生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        String token = JwtUtil.createJWT(
                jwtPeopleProperties.getPeopleSecretKey(),
                jwtPeopleProperties.getPeopleTtl(),
                claims
        );
        // 向VO写入数据
        PoepleLoginVO poepleLoginVO = new PoepleLoginVO();
        poepleLoginVO.setId(user.getId());
        poepleLoginVO.setUsername(user.getUsername());
        poepleLoginVO.setEmail(user.getEmail());
        poepleLoginVO.setPhone(user.getPhone());
        poepleLoginVO.setAvatar(user.getAvatar());
        poepleLoginVO.setNickname(user.getNickname());
        poepleLoginVO.setSignature(user.getSignature());
        poepleLoginVO.setRole(user.getRole());
        poepleLoginVO.setStatus(user.getStatus());
        poepleLoginVO.setToken(token);
        log.info("用户登录成功: id={}, email={}", user.getId(), email);

        return Result.success(poepleLoginVO);
    }

    // 投稿视频
    @PostMapping ("/contribution")
    public Result<VideoUploadVO> uploadVideo(@RequestBody VideoUploadDTO videoUploadDTO){

        log.info("投稿视频:{}", videoUploadDTO);
        Video video = peopleUserService.uploadVideo(videoUploadDTO);
        VideoUploadVO videoUploadVO = new VideoUploadVO();
        BeanUtils.copyProperties(video, videoUploadVO);

        log.info("响应前端:{}", Result.success(videoUploadVO));
        return Result.success(videoUploadVO);

    }

    // 修改用户信息
   @PostMapping ("/updateUserInfo")
    public Result updateUser(@RequestBody UserDTO userDTO){
        log.info("修改用户信息:{}", userDTO);
        peopleUserService.updateUser(userDTO);
        return Result.success();
   }

   // 根据id查询用户信息
    @GetMapping("/user/{userId}")
    public Result<UserDTO> getUserById(@PathVariable Long userId){
        log.info("根据id查询用户信息:{}", userId);
        UserDTO userDTO = peopleUserService.getUserById(userId);
        if(userDTO == null){
            return Result.error("用户不存在");
        }
        return Result.success(userDTO);
    }


    // 根据name查询用户信息
    @GetMapping("/username")
    public Result<UserDTO> getUserByName(@RequestParam String nickname){
        log.info("根据name查询用户信息:{}", nickname);
        UserDTO userDTO = peopleUserService.getUserByName(nickname);
        if(userDTO == null){
            return Result.error("用户不存在");
        }
        return Result.success(userDTO);
    }

    /** 搜索用户（昵称/用户名模糊，公开） */
    @GetMapping("/search")
    public Result<java.util.List<UserDTO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        log.info("搜索用户 keyword={}", keyword);
        try {
            return Result.success(peopleUserService.searchUsers(keyword, limit));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /** 我的投稿列表（含 status / rejectReason，供用户查看审核结果） */
    @GetMapping("/my/videos")
    public Result<java.util.List<Video>> myVideos() {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("查询我的投稿 userId={}", userId);
        try {
            return Result.success(peopleUserService.listMyVideos(userId));
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }






}



