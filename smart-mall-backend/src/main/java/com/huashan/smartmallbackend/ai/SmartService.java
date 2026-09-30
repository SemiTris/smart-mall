package com.huashan.smartmallbackend.ai;

import com.huashan.smartmallbackend.ai.guardrail.SafeInputGuardrail;
import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.spring.AiService;
import reactor.core.publisher.Flux;

/**
 * 智能客服统一 AI 服务（项目心脏）
 * <p>
 * 一个接口接齐三种能力：
 * <ul>
 *     <li>售后知识问答 —— 走 RAG（检索 D:/rag 下的售后文档）</li>
 *     <li>订单查询     —— 走 @Tool（OrderTools → MyBatis → t_order + t_order_item）</li>
 *     <li>多轮对话     —— 走 @MemoryId（ChatMemoryProvider + MySQL 持久化）</li>
 * </ul>
 * <p>
 * {@code @AiService} 不写任何参数：AUTOMATIC 模式会按类型从容器里
 * 自动装配 ChatModel、StreamingChatModel、ChatMemoryProvider、ContentRetriever，
 * 本项目这些组件各只有一个，所以全都能自动选中。
 * <p>
 * <b>为什么这里没有输出护轨</b>：
 * 输出护轨要拿到<b>完整回答</b>才能校验，所以框架会把整条流攒完再一次性吐出来，
 * 打字机效果直接失效。本接口是流式的，所以只保留输入护轨（输入护轨在请求发出前拦截，
 * 不影响流式）。需要输出护轨的场景请用同步接口 —— 两者在 LangChain4j 里目前无法兼得。
 *
 * @author hs
 */
@InputGuardrails({SafeInputGuardrail.class})
@AiService
public interface SmartService {

    /**
     * 流式对话
     *
     * @param sessionId   会话 UUID（同时是 @MemoryId）
     * @param currentUser 当前登录用户昵称，注入到系统提示词里
     * @param message     用户问题
     * @return 流式回答
     */
    @SystemMessage("""
            你是「智选商城」的智能客服助手，当前登录用户的昵称是：{{currentUser}}。

            【能力一：售后知识问答】
            当用户询问退换货政策、售后流程、发票、保修等常见问题时，
            严格依据检索到的资料回答，不要编造不存在的政策或条款。

            【能力二：订单查询】
            当用户询问订单、物流、发货情况时，必须调用 queryMyOrders 工具，
            并且把用户名参数固定传为「{{currentUser}}」。
            绝对不要询问用户姓名，也不要凭记忆编造订单信息。

            【能力三：多轮对话】
            你能记住本次会话之前的对话内容，要结合上文理解"刚才那个""它"等指代。

            通用规则：
            1. 使用中文回答，语气友好专业
            2. 资料中没有的信息，如实告知"我这边没有查到相关信息"
            3. 涉及退款、投诉等敏感诉求，引导用户联系人工客服
            """)
    Flux<String> chat(@MemoryId String sessionId,
                      @V("currentUser") String currentUser,
                      @UserMessage String message);
}
