package com.huashan.smartmallbackend.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.huashan.smartmallbackend.entity.ChatMessageEntity;
import com.huashan.smartmallbackend.entity.Session;
import com.huashan.smartmallbackend.mapper.ChatMessageMapper;
import com.huashan.smartmallbackend.mapper.SessionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * 会话服务
 * <p>
 * 会话 id 是 UUID，同时作为 LangChain4j 的 @MemoryId，
 * 所以「左侧会话列表」「用户看的历史」「管理员看的记录」用的是同一个标识。
 *
 * @author hs
 */
@Service
public class SessionService {

    @Autowired
    private SessionMapper sessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 新建会话
     *
     * @param userId 用户 id
     * @return 新会话
     */
    public Session create(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("用户 id 不能为空");
        }
        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setUserId(userId);
        session.setTitle("新会话");
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 查询某用户的会话列表（左侧列表用）
     *
     * @param userId 用户 id
     * @return 会话列表
     */
    public List<Session> listByUser(Long userId) {
        return sessionMapper.selectByUserId(userId);
    }

    /**
     * 查询某会话的可见消息（剔除 system / tool）
     * <p>
     * 注意：用户消息在入库时已经被 RAG 增强过了（后面会附加
     * "Answer using the following information: ..." 这段检索到的资料），
     * 那是给模型看的，不该展示给用户，所以这里切掉。
     *
     * @param sessionId 会话 UUID
     * @return 消息列表
     */
    public List<ChatMessageEntity> messages(String sessionId) {
        List<ChatMessageEntity> list = chatMessageMapper.selectVisibleBySessionId(sessionId);
        list.forEach(m -> {
            if ("user".equals(m.getRole())) {
                m.setContent(stripAugmentation(m.getContent()));
            }
        });
        return list;
    }

    /**
     * 切掉 RAG 增强附加的检索资料，只保留用户原始问题
     *
     * @param content 入库的原始内容
     * @return 用户真正问的那句话
     */
    private String stripAugmentation(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }
        // LangChain4j 的 DefaultRetrievalAugmentor 用这个前缀拼接检索结果
        int idx = content.indexOf("\n\nAnswer using the following information");
        return idx > 0 ? content.substring(0, idx).trim() : content;
    }

    /**
     * 删除会话（逻辑删会话 + 物理删消息 + 清空 AI 记忆）
     * <p>
     * 这里直接删消息表，等于同时清空了 ChatMemoryStore，
     * 下次再问同一个 sessionId 时 AI 就是全新记忆。
     *
     * @param sessionId 会话 UUID
     */
    public void delete(String sessionId) {
        chatMessageMapper.deleteBySessionId(sessionId);
        sessionMapper.deleteBySessionId(sessionId);
    }

    /**
     * 用户提问时的会话维护：首次提问补标题，之后只刷活跃时间
     * <p>
     * 两条 UPDATE 互斥，不会重复更新 —— updateTitle 的 SQL 里本来就带
     * update_time = NOW()，所以首次提问不必再 touch 一次。
     *
     * @param sessionId 会话 UUID
     * @param message   用户本轮问题
     */
    public void onAsk(String sessionId, String message) {
        Session session = sessionMapper.selectBySessionId(sessionId);
        if (session == null) {
            return;
        }

        // 首次提问（标题还是默认值）：用问题内容当标题
        if ("新会话".equals(session.getTitle()) && message != null && !message.isBlank()) {
            String title = message.strip();
            if (title.length() > 20) {
                title = title.substring(0, 20) + "…";
            }
            sessionMapper.updateTitle(sessionId, title);
            return;
        }

        // 后续提问：只刷最后活跃时间（左侧会话列表按它倒序）
        sessionMapper.touch(sessionId);
    }

    /**
     * 管理端分页查询（展示所有用户的会话）
     *
     * @param pageNum  页码
     * @param pageSize 每页条数
     * @param username 用户名（模糊，可空）
     * @return 分页结果
     */
    public PageInfo<Session> page(Integer pageNum, Integer pageSize, String username) {
        PageHelper.startPage(pageNum, pageSize);
        List<Session> list = sessionMapper.selectPage(username);
        return new PageInfo<>(list);
    }
}
