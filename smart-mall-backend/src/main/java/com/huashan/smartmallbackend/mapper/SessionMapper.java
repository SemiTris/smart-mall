package com.huashan.smartmallbackend.mapper;

import com.huashan.smartmallbackend.entity.Session;

import java.util.List;

/**
 * 会话 Mapper
 * <p>
 * SQL 全部写在 resources/mapper/SessionMapper.xml 里。
 *
 * @author hs
 */
public interface SessionMapper {

    /**
     * 按会话 UUID 查询
     *
     * @param sessionId 会话 UUID
     * @return 会话，不存在返回 null
     */
    Session selectBySessionId(String sessionId);

    /**
     * 查询某用户的全部会话（左侧列表用，按最后活跃时间倒序）
     *
     * @param userId 用户 id
     * @return 会话列表
     */
    List<Session> selectByUserId(Long userId);

    /**
     * 管理端分页查询（联查用户名与消息条数）
     *
     * @param username 用户名（模糊，可空）
     * @return 会话列表
     */
    List<Session> selectPage(String username);

    /**
     * 新建会话
     *
     * @param session 会话
     * @return 影响行数
     */
    int insert(Session session);

    /**
     * 刷新会话标题（取首问前若干字）
     *
     * @param sessionId 会话 UUID
     * @param title     标题
     * @return 影响行数
     */
    int updateTitle(String sessionId, String title);

    /**
     * 刷新会话最后活跃时间
     *
     * @param sessionId 会话 UUID
     * @return 影响行数
     */
    int touch(String sessionId);

    /**
     * 逻辑删除会话
     *
     * @param sessionId 会话 UUID
     * @return 影响行数
     */
    int deleteBySessionId(String sessionId);
}
