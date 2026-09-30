package com.huashan.smartmallbackend.memory;

import com.huashan.smartmallbackend.entity.ChatMessageEntity;
import com.huashan.smartmallbackend.mapper.ChatMessageMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessage;
import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.ToolExecutionResultMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * MySQL 版会话记忆存储
 * <p>
 * 挂到 {@code MessageWindowChatMemory} 上之后，AI 的记忆就落到了 t_chat_message 表：
 * <ul>
 *     <li>重启服务历史不丢</li>
 *     <li>用户看到的「历史消息」和 AI 的「上下文」是同一份数据</li>
 *     <li>管理端的「会话记录」直接查这张表</li>
 * </ul>
 * <p>
 * <b>为什么是「全量覆盖」</b>：框架每次调用 {@code updateMessages} 时传进来的都是
 * 该会话的<b>整个窗口</b>（含被淘汰后的结果），所以实现上就是「先删后批量插」。
 * 这不是我们选的策略，而是框架语义。
 *
 * <h3>⚠️ 为什么必须同时存 message_json（不能只靠 role + content）</h3>
 * {@code AiMessage} 除了 text 之外还有一个 {@code thinking} 字段（思考内容），
 * 而 DeepSeek 的思考模式<b>要求把上一轮的 thinking 原样回传</b>，否则下一轮直接 400：
 * <pre>
 *   InvalidRequestException:
 *   The `reasoning_content` in the thinking mode must be passed back to the API.
 * </pre>
 * 只存 role + content 会把这个字段丢掉，<b>表现就是「模型一调工具就报服务异常」</b>
 * （因为工具调用恰好是"一轮 AI 消息 + 一轮工具结果"的两段式，第二轮就炸）。
 * 用 {@code ChatMessageSerializer.messageToJson()} 整体序列化才能把它保住。
 *
 * @author hs
 */
@Component
public class MySqlChatMemoryStore implements ChatMemoryStore {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    /**
     * 读取某会话的全部消息（含 system）
     * <p>
     * <b>system 必须读出来</b>：框架每轮都会 add(SystemMessage)，
     * 并先调用本方法判断"是否已存在等值的 system 消息"。
     * 如果这里不返回 system，框架会以为没有，于是把它追加到列表尾部，
     * 导致 system 跑到对话后面、顺序错乱。
     *
     * @param memoryId 记忆 id（= 会话 UUID）
     * @return 消息列表（按 seq 升序）
     */
    @Override
    public List<ChatMessage> getMessages(Object memoryId) {
        String sessionId = String.valueOf(memoryId);
        List<ChatMessageEntity> rows = chatMessageMapper.selectBySessionId(sessionId);
        List<ChatMessage> messages = new ArrayList<>(rows.size());
        for (ChatMessageEntity row : rows) {
            try {
                // 优先用 JSON 精确还原（能保住 thinking、工具调用等结构化信息）
                messages.add(ChatMessageDeserializer.messageFromJson(row.getMessageJson()));
            } catch (Exception e) {
                // 兜底：JSON 损坏时按 role + content 重建，绝不让"读记忆"抛异常
                messages.add(rebuild(row.getRole(), row.getContent()));
            }
        }
        return messages;
    }

    /**
     * 全量覆盖写入某会话的消息
     *
     * @param memoryId 记忆 id（= 会话 UUID）
     * @param messages 框架传进来的完整窗口
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMessages(Object memoryId, List<ChatMessage> messages) {
        String sessionId = String.valueOf(memoryId);

        // 1、先清空旧数据（全量覆盖）
        chatMessageMapper.deleteBySessionId(sessionId);

        if (messages == null || messages.isEmpty()) {
            return;
        }

        // 2、按当前窗口顺序重排 seq，并同时保存"纯文本"和"完整 JSON"
        List<ChatMessageEntity> rows = new ArrayList<>(messages.size());
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage message = messages.get(i);
            ChatMessageEntity row = new ChatMessageEntity();
            row.setSessionId(sessionId);
            row.setSeq(i);
            row.setRole(roleOf(message));
            row.setContent(textOf(message));
            row.setMessageJson(ChatMessageSerializer.messageToJson(message));
            rows.add(row);
        }

        // 3、批量插入
        chatMessageMapper.batchInsert(rows);
    }

    /**
     * 删除某会话的全部消息（清空 AI 记忆）
     *
     * @param memoryId 记忆 id（= 会话 UUID）
     */
    @Override
    public void deleteMessages(Object memoryId) {
        chatMessageMapper.deleteBySessionId(String.valueOf(memoryId));
    }

    /**
     * 判断消息角色，用于落库与前端展示过滤
     *
     * @param message 消息
     * @return system / user / ai / tool / other
     */
    private String roleOf(ChatMessage message) {
        if (message instanceof SystemMessage) {
            return "system";
        }
        if (message instanceof UserMessage) {
            return "user";
        }
        if (message instanceof AiMessage) {
            return "ai";
        }
        if (message instanceof ToolExecutionResultMessage) {
            return "tool";
        }
        return "other";
    }

    /**
     * 取消息的纯文本（供前端 / 管理端直接展示）
     *
     * @param message 消息
     * @return 纯文本；纯工具调用的 AiMessage 可能返回空串
     */
    private String textOf(ChatMessage message) {
        if (message instanceof SystemMessage sm) {
            return sm.text();
        }
        if (message instanceof UserMessage um) {
            // 普通文本消息取 singleText，多模态消息退回 toString
            return um.hasSingleText() ? um.singleText() : um.toString();
        }
        if (message instanceof AiMessage am) {
            // 纯工具调用轮次时 text() 可能为 null
            return am.text() == null ? "" : am.text();
        }
        if (message instanceof ToolExecutionResultMessage tm) {
            return tm.text();
        }
        return message.toString();
    }

    /**
     * JSON 损坏时的兜底重建
     *
     * @param role    角色
     * @param content 纯文本
     * @return 消息对象
     */
    private ChatMessage rebuild(String role, String content) {
        String text = content == null ? "" : content;
        return switch (role) {
            case "system" -> SystemMessage.from(text);
            case "user" -> UserMessage.from(text);
            case "ai" -> AiMessage.from(text);
            default -> UserMessage.from(text);
        };
    }
}
