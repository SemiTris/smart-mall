<template>
  <div class="chat-page">
    <!-- ==================== 左侧：会话列表 ==================== -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <div class="user-box">
          <el-avatar :size="32">{{ (userStore.user?.nickname || 'U').charAt(0) }}</el-avatar>
          <span class="nickname">{{ userStore.user?.nickname }}</span>
        </div>
        <el-button type="primary" size="small" @click="newSession">+ 新建会话</el-button>
      </div>

      <ul class="session-list">
        <li v-for="s in sessions" :key="s.sessionId"
            :class="['session-item', { active: s.sessionId === currentSessionId }]"
            @click="switchSession(s)">
          <span class="session-title">{{ s.title }}</span>
          <el-icon class="session-del" title="删除会话" @click.stop="removeSession(s)">
            <Delete/>
          </el-icon>
        </li>
        <li v-if="sessions.length === 0" class="empty-tip">还没有会话，点上面新建一个</li>
      </ul>

      <div class="sidebar-footer">
        <el-button text size="small" @click="logout">退出登录</el-button>
      </div>
    </aside>

    <!-- ==================== 右侧：对话区 ==================== -->
    <main class="main">
      <div ref="msgBox" class="messages">
        <div v-if="messages.length === 0" class="welcome">
          <h3>你好，我是智选商城智能客服 👋</h3>
          <p>可以试着问我：</p>
          <p class="examples">
            · 你们支持七天无理由退货吗？<br>
            · 帮我查一下我的订单<br>
            · 保修期是多久？
          </p>
        </div>

        <div v-for="(m, i) in messages" :key="i" :class="['msg', m.role]">
          <div class="bubble">
            <!--
              等首字期间显示"思考中"，避免出现一个空白气泡干等着。
              条件带上 !m.content 是兜底：只要拿到内容就立刻切成正文，
              哪怕 pending 因为意外没被清掉也不会挡住回答。
            -->
            <span v-if="m.pending && !m.content" class="thinking">
              <i class="dot"></i><i class="dot"></i><i class="dot"></i>
              <em>{{ m.hint }}</em>
            </span>
            <template v-else>{{ m.content }}</template>
          </div>
        </div>
      </div>

      <div class="input-bar">
        <el-input v-model="input" placeholder="请输入你的问题…（Enter 发送）"
                  :disabled="sending" @keyup.enter="send"/>
        <el-button type="primary" :loading="sending" @click="send">发送</el-button>
      </div>
    </main>
  </div>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Delete } from '@element-plus/icons-vue'
import http from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const sessions = ref([])
const currentSessionId = ref('')
const messages = ref([])
const input = ref('')
const sending = ref(false)
const msgBox = ref()

/** 当前用户 id */
const userId = () => userStore.user?.id

// ==================== 会话相关 ====================

/** 加载会话列表 */
function loadSessions() {
  http.get('session/list', { params: { userId: userId() } })
      .then(res => {
        if (res.code === 200) {
          sessions.value = res.data || []
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('加载会话列表失败'))
}

/** 新建会话 */
function newSession() {
  http.post(`session?userId=${userId()}`)
      .then(res => {
        if (res.code === 200) {
          currentSessionId.value = res.data.sessionId
          messages.value = []
          loadSessions()
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('新建会话失败'))
}

/** 切换会话：加载该会话的历史消息 */
function switchSession(session) {
  currentSessionId.value = session.sessionId
  http.get(`session/${session.sessionId}/messages`)
      .then(res => {
        if (res.code === 200) {
          // 后端只返回 user / ai 两种角色（system、tool 已过滤）
          messages.value = (res.data || []).map(m => ({
            role: m.role === 'user' ? 'user' : 'ai',
            content: m.content
          }))
          scrollToBottom()
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('加载历史消息失败'))
}

/** 删除会话 */
function removeSession(session) {
  ElMessageBox.confirm(`确定删除会话「${session.title}」吗？`, '提示', { type: 'warning' })
      .then(() => {
        http.delete(`session/${session.sessionId}`)
            .then(res => {
              if (res.code === 200) {
                ElMessage.success('已删除')
                if (currentSessionId.value === session.sessionId) {
                  currentSessionId.value = ''
                  messages.value = []
                }
                loadSessions()
              } else {
                ElMessage.error(res.msg)
              }
            })
      })
      .catch(() => {
        // 取消删除，什么都不做
      })
}

// ==================== 发消息（流式） ====================

/**
 * 发送消息：axios 读不了流，这里用原生 fetch
 * 注意：后端返回的是 SSE 格式（data:内容\n\n），要按 \n\n 拆事件
 */
function send() {
  const question = input.value.trim()
  if (!question) return
  if (!currentSessionId.value) {
    ElMessage.warning('请先新建一个会话')
    return
  }

  // 1、先把用户消息和一条空的 AI 消息放进列表
  messages.value.push({ role: 'user', content: question })

  // ★★ 这里必须是 reactive 对象，不能是普通字面量 ★★
  //    踩过的坑：写成 `const aiMsg = { role: 'ai', content: '' }` 时，
  //    push 进数组的是【原始对象】，之后 `aiMsg.content += text` 改的是这个原始对象，
  //    绕过了 Vue 的响应式代理 —— 视图压根不会更新。
  //    表现就是：气泡一直空白，直到流结束（sending 变化触发重渲染）才一次性蹦出全部内容，
  //    打字机效果完全失效。
  const aiMsg = reactive({
    role: 'ai',
    content: '',
    pending: true,          // 还没拿到第一个字
    hint: '正在思考…'        // 等待期间显示在气泡里的提示语
  })
  messages.value.push(aiMsg)

  input.value = ''
  sending.value = true
  scrollToBottom()

  // 2、等待提示：等得越久提示越具体，让用户知道不是卡死了
  const startedAt = Date.now()
  const ticker = setInterval(() => {
    const sec = Math.floor((Date.now() - startedAt) / 1000)
    if (sec >= 8) {
      aiMsg.hint = `还在生成中，已等待 ${sec} 秒…`
    } else if (sec >= 3) {
      aiMsg.hint = `正在检索知识库 / 查询数据，已等待 ${sec} 秒…`
    } else {
      aiMsg.hint = '正在思考…'
    }
  }, 1000)

  /** 收尾：停掉计时器并收起"思考中" */
  const finish = () => {
    clearInterval(ticker)
    aiMsg.pending = false
  }

  // 3、发起流式请求
  const url = `http://localhost:8080/chat/stream?userId=${userId()}` +
      `&sessionId=${currentSessionId.value}&message=${encodeURIComponent(question)}`

  fetch(url)
      .then(resp => {
        const reader = resp.body.getReader()
        const decoder = new TextDecoder('utf-8')
        let buffer = ''

        const pump = () => reader.read().then(({ done, value }) => {
          if (done) {
            finish()
            sending.value = false
            // 回答结束后刷新左侧列表（会话标题 / 排序会变）
            loadSessions()
            return
          }
          buffer += decoder.decode(value, { stream: true })
          // SSE 用空行分隔事件
          const blocks = buffer.split('\n\n')
          // 最后一段可能不完整，留到下次
          buffer = blocks.pop()
          blocks.forEach(block => {
            const text = parseSseBlock(block)
            if (text) {
              aiMsg.pending = false     // 第一个字到了，收起"思考中"
              aiMsg.content += text     // 逐字追加 → 打字机效果
              scrollToBottom()
            }
          })
          return pump()
        })
        return pump()
      })
      .catch(() => {
        finish()
        aiMsg.content = '对话失败，请检查后端服务是否已启动'
        sending.value = false
      })
}

/**
 * 解析一个 SSE 事件块，取出里面的文字
 *
 * ⚠️ 关键点：Spring 遇到带换行的内容时，会把「一个事件」拆成「多个 data: 行」，例如
 *      内容「～\n」在流里长这样 →  data:～
 *                                  data:
 *                                  （空行结束）
 * 所以必须「逐行剥掉 data: 前缀，再用 \n 拼回去」。
 * 如果只用 replace(/^data:/, '') 剥第一个，后面那些 data: 就会原样显示在页面上。
 *
 * @param {string} block 一个完整事件（已按 \n\n 切开）
 * @returns {string} 事件里的文字
 */
function parseSseBlock(block) {
  return block
      .split('\n')
      .filter(line => line.startsWith('data:'))
      .map(line => line.slice(5))   // 剥掉 "data:" 这 5 个字符（Spring 不会再加空格）
      .join('\n')
}

/** 滚动到消息区底部 */
function scrollToBottom() {
  nextTick(() => {
    if (msgBox.value) {
      msgBox.value.scrollTop = msgBox.value.scrollHeight
    }
  })
}

/** 退出登录 */
function logout() {
  userStore.logout()
  router.push('/login')
}

// ==================== 初始化 ====================

onMounted(() => {
  loadSessions()
})
</script>

<style scoped>
.chat-page {
  display: flex;
  height: 100vh;
}

/* ---------- 左侧 ---------- */
.sidebar {
  width: 260px;
  background: #1f2d3d;
  color: #dcdfe6;
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 14px;
  border-bottom: 1px solid #2c3e50;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.user-box {
  display: flex;
  align-items: center;
  gap: 8px;
}

.nickname {
  font-size: 14px;
  font-weight: 600;
}

.session-list {
  flex: 1;
  overflow-y: auto;
  list-style: none;
  margin: 0;
  padding: 8px;
}

.session-item {
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.session-item:hover {
  background: #2c3e50;
}

.session-item.active {
  background: #409eff;
  color: #fff;
}

.session-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.session-del {
  opacity: 0;
  transition: opacity .2s;
}

.session-item:hover .session-del {
  opacity: .8;
}

.empty-tip {
  color: #8a919f;
  font-size: 12px;
  text-align: center;
  padding: 20px 0;
}

.sidebar-footer {
  padding: 10px;
  border-top: 1px solid #2c3e50;
  text-align: center;
}

/* ---------- 右侧 ---------- */
.main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
}

.welcome {
  color: #606266;
  text-align: center;
  margin-top: 80px;
}

.welcome .examples {
  color: #909399;
  font-size: 14px;
  line-height: 2;
}

.msg {
  display: flex;
  margin-bottom: 14px;
}

.msg.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 72%;
  padding: 10px 14px;
  border-radius: 10px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.7;
  font-size: 14px;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
}

.msg.ai .bubble {
  background: #fff;
  color: #303133;
  border: 1px solid #ebeef5;
}

/* ---------- "思考中"占位：三个跳动的点 + 提示文字 ---------- */
.thinking {
  display: inline-flex;
  align-items: center;
  color: #909399;
}

.thinking .dot {
  width: 6px;
  height: 6px;
  margin-right: 4px;
  border-radius: 50%;
  background: #c0c4cc;
  animation: dot-bounce 1.2s infinite ease-in-out;
}

.thinking .dot:nth-child(2) {
  animation-delay: .16s;
}

.thinking .dot:nth-child(3) {
  animation-delay: .32s;
}

.thinking em {
  font-style: normal;
  font-size: 13px;
  margin-left: 6px;
}

@keyframes dot-bounce {
  0%, 80%, 100% {
    opacity: .3;
    transform: translateY(0);
  }
  40% {
    opacity: 1;
    transform: translateY(-3px);
  }
}

.input-bar {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  background: #fff;
  border-top: 1px solid #ebeef5;
}
</style>
