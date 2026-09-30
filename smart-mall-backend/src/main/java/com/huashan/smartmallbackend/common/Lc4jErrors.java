package com.huashan.smartmallbackend.common;

import dev.langchain4j.exception.AuthenticationException;
import dev.langchain4j.exception.ContentFilteredException;
import dev.langchain4j.exception.RateLimitException;
import dev.langchain4j.exception.TimeoutException;
import dev.langchain4j.guardrail.GuardrailException;

/**
 * LangChain4j 异常 → 人话 的统一映射
 * <p>
 * 为什么单独抽出来？因为流式接口（返回 {@code Flux<String>}）不能返回 R 对象，
 * 异常只能转成一段文字塞进流里。这样 REST 走 GlobalExceptionHandler、
 * SSE 走本类，两边共用同一张映射表，提示语不会不一致。
 *
 * @author hs
 */
public final class Lc4jErrors {

    /**
     * 私有构造，禁止实例化
     */
    private Lc4jErrors() {
    }

    /**
     * 把异常翻译成给用户看的一句话
     *
     * @param e 异常
     * @return 友好提示
     */
    public static String humanize(Throwable e) {
        if (e == null) {
            return "服务异常，请稍后重试";
        }
        // 顺序有讲究：先具体后笼统
        if (e instanceof GuardrailException) {
            // 护轨拦截：message 本身就是给用户看的（如"检测到异常指令，请正常提问"），
            // 但框架会把它包一层（"The guardrail ... failed with this message: xxx"），
            // 这里剥掉外层包装，只留护轨自己写的那句话
            return guardrailMessage(e.getMessage());
        }
        if (e instanceof RateLimitException) {
            return "当前咨询人数较多，请稍后重试";
        }
        if (e instanceof AuthenticationException) {
            return "服务配置异常，请联系管理员";
        }
        if (e instanceof TimeoutException) {
            return "回答生成超时，请稍后重试";
        }
        if (e instanceof ContentFilteredException) {
            return "抱歉，该问题我无法回答";
        }
        return "服务异常，请稍后重试";
    }

    /**
     * 从框架包装过的护轨异常信息里，取出护轨自己写的那句话
     *
     * @param raw 异常原始 message（可能形如 "The guardrail ... failed with this message: xxx"）
     * @return 干净的用户提示
     */
    private static String guardrailMessage(String raw) {
        if (raw == null || raw.isBlank()) {
            return "输入不符合要求，请调整后重试";
        }
        String marker = "failed with this message:";
        int idx = raw.indexOf(marker);
        if (idx >= 0) {
            String clean = raw.substring(idx + marker.length()).trim();
            return clean.isBlank() ? raw : clean;
        }
        return raw;
    }
}
