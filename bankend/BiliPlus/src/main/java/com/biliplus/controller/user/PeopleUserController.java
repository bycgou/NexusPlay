package com.biliplus.controller.user;

import com.biliplus.constant.AllConstant;

import com.biliplus.pojo.dto.userdto.ChangePasswordDTO;
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
    private com.biliplus.utils.LoginRateLimiter loginRateLimiter;

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
        // 日志脱敏：不输出验证码明文
        log.info("发送邮箱验证码请求: email={}, captchaId={}", email, captchaId);

        if (email == null || email.trim().isEmpty()) {
            return Result.error("邮箱不能为空");
        }
        if (captchaId == null || captchaId.trim().isEmpty()) {
            return Result.error("图片验证码已过期，请重新输入");
        }

        try {
            // 先验码：一次性验证码，失败即作废，防止爆破
            if (ImageCaptcha == null || ImageCaptcha.trim().isEmpty()) {
                return Result.error("图片验证码已过期，请重新输入");
            }
            if (!validateCodeService.validateCode(captchaId.trim(), ImageCaptcha.trim())) {
                return Result.error("图片验证码错误");
            }

            // 验码通过后再扣发信额度，避免错误验证码占额度
            String mailKey = email.trim().toLowerCase();
            if (!loginRateLimiter.allow("mail-60s:" + mailKey, 1, java.time.Duration.ofSeconds(60))
                    || !loginRateLimiter.allow("mail-day:" + mailKey, 5, java.time.Duration.ofDays(1))) {
                return Result.error("发送过于频繁，请稍后再试");
            }

            emailCodeService.sendEmailCode(email);
            return Result.success("邮箱验证码已发送，请查收");

        } catch (Exception e) {
            log.warn("发送邮箱验证码失败: {}", e.getMessage());
            return Result.error("发送失败，请稍后再试");
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
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public org.springframework.http.ResponseEntity<Result<PoepleLoginVO>> login(
            @RequestBody UserRegisterDTO userRegisterDTO,
            jakarta.servlet.http.HttpServletRequest request) {
        String email = userRegisterDTO.getEmail();
        // 日志脱敏：不输出密码
        log.info("用户登录请求: email={}", email);

        String ip = com.biliplus.utils.ClientIpUtil.resolve(request);
        if (!loginRateLimiter.allowLoginIp(ip)) {
            log.warn("登录 IP 触发限速 ip={}", ip);
            return org.springframework.http.ResponseEntity
                    .status(429)
                    .body(Result.error("尝试次数过多，请稍后再试"));
        }

        // 连续失败后强制图片验证码，降低爆破成本
        if (email != null && !email.trim().isEmpty()) {
            String failKey = "login-fail:" + email.trim().toLowerCase();
            if (loginRateLimiter.count(failKey) >= 3) {
                String captchaId = userRegisterDTO.getCaptchaId();
                String imageCaptcha = userRegisterDTO.getImageCaptcha();
                if (captchaId == null || captchaId.trim().isEmpty()
                        || imageCaptcha == null || imageCaptcha.trim().isEmpty()) {
                    return org.springframework.http.ResponseEntity
                            .status(429)
                            .body(Result.error("请完成图片验证码后继续登录"));
                }
                if (!validateCodeService.validateCode(captchaId.trim(), imageCaptcha.trim())) {
                    return org.springframework.http.ResponseEntity
                            .status(429)
                            .body(Result.error("图片验证码错误"));
                }
            }
        }

        // 调用login方法
        User user = peopleUserService.userlogin(userRegisterDTO);
        if (user == null) {
            if (email != null && !email.trim().isEmpty()) {
                loginRateLimiter.allow("login-fail:" + email.trim().toLowerCase(), 50, java.time.Duration.ofMinutes(15));
            }
            return org.springframework.http.ResponseEntity
                    .status(401)
                    .body(Result.error("用户名或密码错误"));
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
        if (email != null && !email.trim().isEmpty()) {
            loginRateLimiter.reset("login-fail:" + email.trim().toLowerCase());
        }

        return org.springframework.http.ResponseEntity.ok(Result.success(poepleLoginVO));
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

    /** 修改当前登录用户密码 */
    @PostMapping("/changePassword")
    public Result changePassword(@RequestBody ChangePasswordDTO dto) {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("用户修改密码 userId={}", userId);
        try {
            peopleUserService.changePassword(dto.getOldPassword(), dto.getNewPassword());
            return Result.success();
        } catch (Exception e) {
            log.warn("修改密码失败 userId={}, msg={}", userId, e.getMessage());
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

   // 根据id查询用户信息
    @GetMapping("/user/{userId}")
    public org.springframework.http.ResponseEntity<Result<?>> getUserById(@PathVariable Long userId){
        log.info("根据id查询用户信息:{}", userId);
        Long currentUserId = com.biliplus.utils.UserContext.getCurrentUserId();
        // 本人可看完整资料，他人只返回公开字段
        if (currentUserId != null && currentUserId.equals(userId)) {
            UserDTO userDTO = peopleUserService.getUserById(userId);
            if(userDTO == null){
                return org.springframework.http.ResponseEntity.status(404).body(Result.error("用户不存在"));
            }
            return org.springframework.http.ResponseEntity.ok(Result.success(userDTO));
        }
        com.biliplus.pojo.vo.UserPublicVO pub = peopleUserService.getPublicById(userId);
        if(pub == null){
            return org.springframework.http.ResponseEntity.status(404).body(Result.error("用户不存在"));
        }
        return org.springframework.http.ResponseEntity.ok(Result.success(pub));
    }

    /** 当前登录用户完整资料（含 email/phone，仅本人） */
    @GetMapping("/me")
    public org.springframework.http.ResponseEntity<Result<UserDTO>> getMe() {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        if (userId == null) {
            return org.springframework.http.ResponseEntity
                    .status(401)
                    .body(Result.error("请先登录"));
        }
        UserDTO userDTO = peopleUserService.getUserById(userId);
        if (userDTO == null) {
            return org.springframework.http.ResponseEntity.status(404).body(Result.error("用户不存在"));
        }
        return org.springframework.http.ResponseEntity.ok(Result.success(userDTO));
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
    public Result<java.util.List<com.biliplus.pojo.vo.UserPublicVO>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) Integer limit) {
        log.info("搜索用户 keyword={}", keyword);
        try {
            return Result.success(peopleUserService.searchUsers(keyword, limit));
        } catch (Exception e) {
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

    /** 我的投稿列表（含 status / rejectReason / tags，供用户查看审核结果） */
    @GetMapping("/my/videos")
    public Result<java.util.List<com.biliplus.pojo.vo.MyVideoVO>> myVideos() {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("查询我的投稿 userId={}", userId);
        try {
            return Result.success(peopleUserService.listMyVideos(userId));
        } catch (Exception e) {
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

    /** 编辑自有稿件（标题/简介/分类/封面/标签） */
    @PutMapping("/my/videos/{id}")
    public Result updateMyVideo(@PathVariable Long id,
                                @RequestBody com.biliplus.pojo.dto.userdto.VideoEditDTO dto) {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("编辑稿件 userId={}, videoId={}", userId, id);
        try {
            peopleUserService.updateMyVideo(userId, id, dto);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

    /** 删除（软删）自有稿件 */
    @DeleteMapping("/my/videos/{id}")
    public Result deleteMyVideo(@PathVariable Long id) {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("删除稿件 userId={}, videoId={}", userId, id);
        try {
            peopleUserService.deleteMyVideo(userId, id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }

    /** 驳回/下架稿件修改后重新提交审核 */
    @PostMapping("/my/videos/{id}/resubmit")
    public Result resubmitMyVideo(@PathVariable Long id) {
        Long userId = com.biliplus.utils.UserContext.getCurrentUserId();
        log.info("重新提交稿件 userId={}, videoId={}", userId, id);
        try {
            peopleUserService.resubmitMyVideo(userId, id);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e instanceof com.biliplus.exception.BusinessException ? e.getMessage() : "请求处理失败，请稍后再试");
        }
    }






}



