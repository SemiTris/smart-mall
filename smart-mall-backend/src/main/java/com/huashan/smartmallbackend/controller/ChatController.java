package com.huashan.smartmallbackend.controller;

import com.huashan.smartmallbackend.ai.SmartService;
import com.huashan.smartmallbackend.common.Lc4jErrors;
import com.huashan.smartmallbackend.service.SessionService;
import com.huashan.smartmallbackend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话接口（流式）
 * <p>
 * 关于落库：<b>不用在这里写任何"收集完整回答再存库"的代码</b>。
 * 框架的流式处理器在流结束时（onCompleteResponse）会自动把 AI 的完整回答
 * 写进 ChatMemoryStore —— 也就是我们的 t_chat_message 表。
 * 这里自己去收集反而会双写，还会绕过 @MemoryId 记忆机制。
 *
 * @author hs
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    private SmartService smartService;

    @Autowired
    private UserService userService;

    @Autowired
    private SessionService sessionService;

    /**
     * 流式对话（SSE）
     *
     * @param userId    当前用户 id
     * @param sessionId 会话 UUID
     * @param message   用户问题
     * @return 流式回答
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(@RequestParam Long userId,
                               @RequestParam String sessionId,
                               @RequestParam String message) {
        // 1、把用户昵称查出来，作为 {{currentUser}} 注入系统提示词
        String nickname = userService.nicknameOf(userId);

        // 2、会话维护：首次提问补标题，之后刷活跃时间（左侧列表按它倒序）
        sessionService.onAsk(sessionId, message);

        try {
            // 3、调用 AI 服务：记忆 + RAG + 工具 + 护轨 全部由框架自动串起来
            return smartService.chat(sessionId, nickname, message)
                    // 流式接口不能返回 R 对象，异常只能翻译成一句话塞进流里
                    .onErrorResume(e -> {
                        // ★ 一定要记日志：流式接口走不到 GlobalExceptionHandler，
                        //   不记的话线上只看得到"服务异常"四个字，没法排查
                        log.error("【流式对话失败】sessionId={}, message={}", sessionId, message, e);
                        return Flux.just(Lc4jErrors.humanize(e));
                    });
        } catch (Exception e) {
            // 输入护轨 fatal 拦截是同步抛出的，走这里
            log.warn("【对话被拦截】sessionId={}, message={}，原因：{}", sessionId, message, e.getMessage());
            return Flux.just(Lc4jErrors.humanize(e));
        }
    }
}
