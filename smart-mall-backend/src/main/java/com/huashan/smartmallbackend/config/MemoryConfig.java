package com.huashan.smartmallbackend.config;

import com.huashan.smartmallbackend.memory.MySqlChatMemoryStore;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 会话记忆配置
 * <p>
 * 把默认的内存 store 换成 MySQL store（「记忆持久化」的落地）。
 *
 * @author hs
 */
@Configuration
public class MemoryConfig {

    @Autowired
    private MySqlChatMemoryStore mySqlChatMemoryStore;

    /**
     * 为每个 sessionId 创建独立记忆，最多保留最近 20 条消息
     * <p>
     * 注意这个 20 有两层含义：既是"给模型看的上下文窗口"，
     * 也是"给人看的历史长度"（因为落库的就是这个窗口）。
     *
     * @return 记忆提供者
     */
    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                // 记忆 id = 会话 UUID
                .id(memoryId)
                // 每个会话保留最近 20 条
                .maxMessages(20)
                // 关键：换成 MySQL 存储，重启不丢
                .chatMemoryStore(mySqlChatMemoryStore)
                .build();
    }
}
