package com.huashan.smartmallbackend.controller;

import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.ChatMessageEntity;
import com.huashan.smartmallbackend.entity.Session;
import com.huashan.smartmallbackend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 会话接口（用户端）：新建 / 列表 / 删除 / 查历史消息
 *
 * @author hs
 */
@RestController
@RequestMapping("/session")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 新建会话
     *
     * @param userId 用户 id
     * @return 新会话（含 sessionId）
     */
    @PostMapping
    public R<Session> create(@RequestParam Long userId) {
        return R.ok("新建成功", sessionService.create(userId));
    }

    /**
     * 我的会话列表（左侧列表用）
     *
     * @param userId 用户 id
     * @return 会话列表
     */
    @GetMapping("/list")
    public R<List<Session>> list(@RequestParam Long userId) {
        return R.ok(sessionService.listByUser(userId));
    }

    /**
     * 切换会话时加载历史消息（已剔除 system / tool）
     *
     * @param sessionId 会话 UUID
     * @return 消息列表
     */
    @GetMapping("/{sessionId}/messages")
    public R<List<ChatMessageEntity>> messages(@PathVariable String sessionId) {
        return R.ok(sessionService.messages(sessionId));
    }

    /**
     * 删除会话（会话、消息、AI 记忆一起清掉）
     *
     * @param sessionId 会话 UUID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}")
    public R<Void> delete(@PathVariable String sessionId) {
        sessionService.delete(sessionId);
        return R.ok("删除成功", null);
    }
}
