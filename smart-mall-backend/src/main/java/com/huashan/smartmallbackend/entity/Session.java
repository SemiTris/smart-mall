package com.huashan.smartmallbackend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 会话实体
 * <p>
 * sessionId 是 UUID，同时作为 LangChain4j 的 @MemoryId 使用，
 * 因此「会话列表」和「AI 的记忆」共用同一个标识。
 *
 * @author hs
 */
@Data
public class Session {

    /** 主键 */
    private Long id;

    /** 会话 UUID（= @MemoryId） */
    private String sessionId;

    /** 所属用户 id */
    private Long userId;

    /** 会话标题（取首问前若干字） */
    private String title;

    /** 逻辑删除：0 正常，1 已删 */
    private Integer deleted;

    /** 创建时间（@JsonFormat 管 JSON 出入参，@DateTimeFormat 管表单 / URL 参数） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /** 最后活跃时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // ===== 以下字段不入库，仅用于管理端列表展示（由 SQL 联查填充） =====

    /** 所属用户名（管理端展示用） */
    private String username;

    /** 消息条数（管理端展示用） */
    private Integer messageCount;
}
