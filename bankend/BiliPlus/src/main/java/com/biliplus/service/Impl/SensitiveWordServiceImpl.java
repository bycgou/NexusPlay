package com.biliplus.service.Impl;

import com.biliplus.exception.BusinessException;
import com.biliplus.mapper.SensitiveHitLogMapper;
import com.biliplus.mapper.SensitiveWordMapper;
import com.biliplus.pojo.entity.SensitiveHitLog;
import com.biliplus.pojo.entity.SensitiveWord;
import com.biliplus.pojo.vo.SensitiveHitVO;
import com.biliplus.result.PageResult;
import com.biliplus.service.SensitiveWordService;
import com.biliplus.service.UserCreditService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Service
public class SensitiveWordServiceImpl implements SensitiveWordService {

    public static final int LEVEL_BLOCK = 1;
    public static final int LEVEL_REVIEW = 2;
    public static final int LEVEL_MARK = 3;

    @Autowired
    private SensitiveWordMapper sensitiveWordMapper;

    @Autowired
    private SensitiveHitLogMapper sensitiveHitLogMapper;

    @Autowired
    private UserCreditService userCreditService;

    @Autowired
    private com.biliplus.service.AdminOperationLogService adminOperationLogService;

    /** 启用中的词库，写操作后刷新；读用 CopyOnWriteArrayList 保证遍历期无锁 */
    private volatile List<SensitiveWord> enabledWords = new CopyOnWriteArrayList<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    private void refreshCache() {
        try {
            List<SensitiveWord> loaded = sensitiveWordMapper.selectAllEnabled();
            enabledWords = loaded == null ? new CopyOnWriteArrayList<>()
                    : new CopyOnWriteArrayList<>(loaded);
        } catch (Exception e) {
            // 词库不可用时降级为「不过滤」，不能阻断发布主流程
            log.warn("加载敏感词库失败，本轮发布将不过滤", e);
            enabledWords = new CopyOnWriteArrayList<>();
        }
    }

    @Override
    public void enforce(String text, Long userId, String targetType, Long targetId) {
        if (!StringUtils.hasText(text)) {
            return;
        }
        List<SensitiveHitVO> hits = match(text);
        if (hits.isEmpty()) {
            return;
        }
        boolean blocking = false;
        for (SensitiveHitVO hit : hits) {
            recordHit(hit, userId, targetType, targetId, text);
            if (hit.isBlocking()) {
                blocking = true;
            }
            // 拦截与转人工都按违规计一次分，仅标记不扣分
            if (hit.getLevel() != null && hit.getLevel() != LEVEL_MARK) {
                userCreditService.applyViolation(userId, -5, "内容命中敏感词：" + hit.getWord());
            }
        }
        if (blocking) {
            throw new BusinessException("内容包含违规词，请修改后重试");
        }
    }

    /** 纯内存匹配，大小写不敏感；同一文本命中多个词时全部返回 */
    private List<SensitiveHitVO> match(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        List<SensitiveHitVO> hits = new ArrayList<>();
        for (SensitiveWord word : enabledWords) {
            if (word.getWord() == null) {
                continue;
            }
            if (lower.contains(word.getWord().toLowerCase(Locale.ROOT))) {
                SensitiveHitVO vo = new SensitiveHitVO();
                vo.setWordId(word.getId());
                vo.setWord(word.getWord());
                vo.setLevel(word.getLevel());
                hits.add(vo);
            }
        }
        return hits;
    }

    private void recordHit(SensitiveHitVO hit, Long userId, String targetType, Long targetId, String text) {
        try {
            SensitiveHitLog record = new SensitiveHitLog();
            record.setWordId(hit.getWordId());
            record.setWord(hit.getWord());
            record.setLevel(hit.getLevel());
            record.setUserId(userId);
            record.setTargetType(targetType);
            record.setTargetId(targetId);
            record.setContent(abbreviate(text, 200));
            record.setAction(hit.isBlocking() ? "block" : "mark");
            record.setCreateTime(LocalDateTime.now());
            sensitiveHitLogMapper.insert(record);
        } catch (Exception e) {
            log.warn("写敏感词命中日志失败", e);
        }
    }

    // ===== 词库维护 =====

    @Override
    public PageResult adminList(String keyword, Integer status, Integer page, Integer size) {
        String kw = com.biliplus.utils.LikeEscape.prepareKeyword(keyword);
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<SensitiveWord> records = sensitiveWordMapper.adminList(kw, status, (p - 1) * s, s);
        return new PageResult(sensitiveWordMapper.adminCount(kw, status), records);
    }

    @Override
    @Transactional
    public SensitiveWord create(String word, Integer level) {
        if (!StringUtils.hasText(word)) {
            throw new BusinessException("敏感词不能为空");
        }
        String trimmed = word.trim();
        if (trimmed.length() > 50) {
            throw new BusinessException("敏感词不能超过50个字");
        }
        if (sensitiveWordMapper.selectByWord(trimmed) != null) {
            throw new BusinessException("该敏感词已存在");
        }
        SensitiveWord entity = new SensitiveWord();
        entity.setWord(trimmed);
        entity.setLevel(normalizeLevel(level));
        entity.setStatus(1);
        entity.setCreateTime(LocalDateTime.now());
        sensitiveWordMapper.insert(entity);
        refreshCache();
        adminOperationLogService.record("sensitive.create", "sensitive_word", entity.getId(),
                trimmed + " level=" + entity.getLevel());
        return entity;
    }

    @Override
    @Transactional
    public SensitiveWord update(Long id, Integer level, Integer status) {
        SensitiveWord existing = sensitiveWordMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("敏感词不存在");
        }
        int rows = sensitiveWordMapper.update(id, normalizeLevel(level),
                status == null ? existing.getStatus() : status);
        if (rows <= 0) {
            throw new BusinessException("更新失败");
        }
        refreshCache();
        adminOperationLogService.record("sensitive.update", "sensitive_word", id,
                "level=" + level + ", status=" + status);
        return sensitiveWordMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (sensitiveWordMapper.selectById(id) == null) {
            throw new BusinessException("敏感词不存在");
        }
        sensitiveWordMapper.delete(id);
        refreshCache();
        adminOperationLogService.record("sensitive.delete", "sensitive_word", id, null);
    }

    // ===== 命中复核 =====

    @Override
    public PageResult hitList(Integer reviewStatus, String action, Integer page, Integer size) {
        int p = page == null || page < 1 ? 1 : page;
        int s = size == null || size < 1 ? 20 : Math.min(size, 100);
        List<SensitiveHitLog> records = sensitiveHitLogMapper.adminList(reviewStatus, action, (p - 1) * s, s);
        return new PageResult(sensitiveHitLogMapper.adminCount(reviewStatus, action), records);
    }

    @Override
    public void reviewHit(Long adminId, Long hitId, Integer reviewStatus) {
        if (hitId == null) {
            throw new BusinessException("命中记录ID不能为空");
        }
        // 1 确认违规，2 误伤
        if (reviewStatus == null || (reviewStatus != 1 && reviewStatus != 2)) {
            throw new BusinessException("复核结果只能是「确认违规」或「误伤」");
        }
        int rows = sensitiveHitLogMapper.review(hitId, reviewStatus, adminId, LocalDateTime.now());
        if (rows <= 0) {
            throw new BusinessException("该记录已复核或不存在");
        }
        log.info("敏感词命中复核 hitId={}, reviewStatus={}, adminId={}", hitId, reviewStatus, adminId);
        adminOperationLogService.record("sensitive.review", "sensitive_word", hitId,
                "reviewStatus=" + reviewStatus);
    }

    @Override
    public Map<String, Object> hitSummary() {
        Map<String, Object> summary = new HashMap<>();
        Map<String, Object> review = sensitiveHitLogMapper.reviewSummary();
        long total = toLong(review == null ? null : review.get("total"));
        long confirmed = toLong(review == null ? null : review.get("confirmed"));
        long falsePositive = toLong(review == null ? null : review.get("falsePositive"));
        long reviewed = confirmed + falsePositive;

        summary.put("total", total);
        summary.put("confirmed", confirmed);
        summary.put("falsePositive", falsePositive);
        summary.put("reviewed", reviewed);
        // 误伤率 = 误伤 / 已复核；无复核记录时为 null，避免展示 0% 造成误解
        summary.put("falsePositiveRate", reviewed == 0 ? null : (double) falsePositive / reviewed);
        summary.put("byTargetType", sensitiveHitLogMapper.countByTargetType());
        summary.put("topWords", sensitiveHitLogMapper.topWords(10));
        return summary;
    }

    private int normalizeLevel(Integer level) {
        if (level == null) {
            return LEVEL_BLOCK;
        }
        return (level == LEVEL_BLOCK || level == LEVEL_REVIEW || level == LEVEL_MARK) ? level : LEVEL_BLOCK;
    }

    private String abbreviate(String text, int max) {
        String flat = text.replaceAll("\\s+", " ").trim();
        return flat.length() <= max ? flat : flat.substring(0, max) + "…";
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return value instanceof Number ? ((Number) value).longValue() : 0L;
    }
}
