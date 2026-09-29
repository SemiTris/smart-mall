package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.ChatMessageEntity;

import java.util.List;

/**
 * 会话消息 Mapper
 * <p>
 * 这张表有两个用途：
 * <ol>
 *     <li>作为 ChatMemoryStore 的落地表 —— 让 AI 的记忆重启不丢</li>
 *     <li>作为会话历史的数据源 —— 用户切换会话、管理员查看记录都用它</li>
 * </ol>
 * 因为写入是"全量覆盖"（框架语义），所以本 Mapper 只有"查 / 删 / 批量插"三种操作。
 * <p>
 * SQL 全部写在 resources/mapper/ChatMessageMapper.xml 里。
 *
 * @author hs
 */
public interface ChatMessageMapper {

    /**
     * 查询某会话的全部消息（含 system，按 seq 升序）
     * <p>
     * 供 ChatMemoryStore 重建记忆使用，必须带 system，否则对话顺序会错乱。
     *
     * @param sessionId 会话 UUID
     * @return 消息列表
     */
    List<ChatMessageEntity> selectBySessionId(String sessionId);

    /**
     * 查询某会话的可见消息（剔除 system / tool，供前端与管理员展示）
     *
     * @param sessionId 会话 UUID
     * @return 消息列表
     */
    List<ChatMessageEntity> selectVisibleBySessionId(String sessionId);

    /**
     * 删除某会话的全部消息（全量覆盖的第一步）
     *
     * @param sessionId 会话 UUID
     * @return 影响行数
     */
    int deleteBySessionId(String sessionId);

    /**
     * 批量插入消息
     *
     * @param list 消息列表
     * @return 影响行数
     */
    int batchInsert(List<ChatMessageEntity> list);

    /**
     * 统计某会话的可见消息条数
     *
     * @param sessionId 会话 UUID
     * @return 条数
     */
    int countVisible(String sessionId);
}
