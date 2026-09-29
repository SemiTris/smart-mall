package com.huashan.smartmallbackend.controller.admin;

import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.common.R;
import com.huashan.smartmallbackend.entity.ChatMessageEntity;
import com.huashan.smartmallbackend.entity.Session;
import com.huashan.smartmallbackend.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理端 - 会话记录管理
 * <p>
 * 可以查看所有用户的会话，并点开看某会话的完整对话内容。
 * 数据来源就是 ChatMemoryStore 的那张表，所以看到的就是 AI 真正记住的内容。
 *
 * @author hs
 */
@RestController
@RequestMapping("/admin/session")
public class AdminSessionController {

    @Autowired
    private SessionService sessionService;

    /**
     * 所有用户的会话列表（分页）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊，可空）
     * @return 分页结果（含 messageCount）
     */
    @GetMapping("/page")
    public R<PageInfo<Session>> page(@RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                     @RequestParam(required = false) String username) {
        return R.ok(sessionService.page(pageNum, pageSize, username));
    }

    /**
     * 查看某会话的完整对话内容（剔除 system / tool）
     *
     * @param sessionId 会话 UUID
     * @return 消息列表
     */
    @GetMapping("/{sessionId}/messages")
    public R<List<ChatMessageEntity>> messages(@PathVariable String sessionId) {
        return R.ok(sessionService.messages(sessionId));
    }
}
