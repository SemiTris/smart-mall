package com.huashan.smartmallbackend.common;

import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.ContentFilteredException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理：把 LangChain4j 的类型化异常翻译成人话
 * <p>
 * 原则：<b>日志里记全，返回里说少</b>。
 * 日志要有堆栈方便排查，返回给用户的绝不能带内部细节。
 * <p>
 * 注意流式接口（返回 Flux）走不到这里，那类接口用 {@link Lc4jErrors} 手工翻译。
 *
 * @author hs
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 401：API Key 错误 / 过期
     *
     * @param e 鉴权异常
     * @return 友好提示
     */
    @ExceptionHandler(AuthenticationException.class)
    public R<Void> handleAuth(AuthenticationException e) {
        log.error("【鉴权失败】检查 DASHSCOPE_API_KEY / DEEPSEEK_API_KEY 是否正确、是否过期", e);
        return R.fail(500, "服务配置异常，请联系管理员");
    }

    /**
     * 429：限流 / 免费额度用完
     *
     * @param e 限流异常
     * @return 友好提示
     */
    @ExceptionHandler(RateLimitException.class)
    public R<Void> handleRateLimit(RateLimitException e) {
        log.warn("【限流/额度不足】{}", e.getMessage());
        return R.fail(429, "当前咨询人数较多，请稍后重试");
    }

    /**
     * 超时：模型响应太慢或网络抖动
     *
     * @param e 超时异常
     * @return 友好提示
     */
    @ExceptionHandler(TimeoutException.class)
    public R<Void> handleTimeout(TimeoutException e) {
        log.warn("【调用超时】{}", e.getMessage());
        return R.fail(504, "回答生成超时，请稍后重试");
    }

    /**
     * 内容审核拦截：输入或输出不合规
     *
     * @param e 内容过滤异常
     * @return 友好提示
     */
    @ExceptionHandler(ContentFilteredException.class)
    public R<Void> handleContentFiltered(ContentFilteredException e) {
        log.warn("【内容审核拦截】{}", e.getMessage());
        return R.fail(400, "抱歉，该问题我无法回答");
    }

    /**
     * 业务参数问题（Service 里主动抛的）
     *
     * @param e 参数异常
     * @return 友好提示
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public R<Void> handleBiz(IllegalArgumentException e) {
        log.warn("【业务校验失败】{}", e.getMessage());
        return R.fail(400, e.getMessage());
    }

    /**
     * 兜底：其余所有异常
     *
     * @param e 任意异常
     * @return 友好提示
     */
    @ExceptionHandler(Exception.class)
    public R<Void> handleOthers(Exception e) {
        // 完整异常打进日志，方便排查
        log.error("【系统异常】", e);
        // 返回给用户的绝不能带内部细节
        return R.fail(500, "服务异常，请稍后重试");
    }
}
