package com.huashan.smartmallbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 会话消息实体（ChatMemoryStore 的落地表 t_chat_message）
 * <p>
 * 同时存两份内容：
 * <ul>
 *     <li>content      —— 纯文本，供前端 / 管理端直接展示</li>
 *     <li>messageJson  —— 完整 ChatMessage 序列化，供记忆精确重建。
 *                          <b>这个字段不能省</b>：AiMessage 除了 text 还有 thinking
 *                          （DeepSeek 思考模式要求把上一轮的 thinking 原样回传，
 *                            只存 role + content 会丢，导致工具调用第二轮 400）</li>
 * </ul>
 *
 * @author hs
 */
@Data
public class ChatMessageEntity {

    /** 主键 */
    private Long id;

    /** 会话 UUID */
    private String sessionId;

    /** 窗口内顺序（每次全量覆盖时重排 0..n-1） */
    private Integer seq;

    /** 角色：system / user / ai / tool */
    private String role;

    /** 纯文本内容（供展示） */
    private String content;

    /** 完整 ChatMessage JSON（供记忆重建） */
    private String messageJson;

    /** 创建时间（@JsonFormat 管 JSON 出入参，@DateTimeFormat 管表单 / URL 参数） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}
