<template>
  <el-card shadow="never">
    <!-- 查询条件 -->
    <el-form inline>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="模糊搜索" clearable style="width: 160px"
                  @keyup.enter="search"/>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="username" label="用户" width="110"/>
      <el-table-column prop="title" label="会话标题" min-width="220"/>
      <el-table-column label="消息数" width="90">
        <template #default="{ row }">
          <el-tag size="small" type="info">{{ row.messageCount }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170"/>
      <el-table-column prop="updateTime" label="最后活跃" width="170"/>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="viewMessages(row)">查看对话</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination class="pager"
                   layout="total, prev, pager, next, sizes"
                   :total="total"
                   v-model:current-page="query.pageNum"
                   v-model:page-size="query.pageSize"
                   :page-sizes="[5, 10, 20, 50]"
                   @current-change="loadData"
                   @size-change="search"/>

    <!-- 对话内容抽屉 -->
    <el-drawer v-model="drawerVisible" :title="`会话对话内容：${currentTitle}`" size="560px">
      <div v-loading="msgLoading" class="msg-list">
        <div v-for="(m, i) in msgList" :key="i" :class="['msg', m.role]">
          <div class="bubble">{{ m.content }}</div>
        </div>
        <div v-if="!msgLoading && msgList.length === 0" class="empty-tip">该会话还没有对话内容</div>
      </div>
    </el-drawer>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import http from '@/utils/request'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  username: ''
})

// ---------- 抽屉 ----------
const drawerVisible = ref(false)
const msgLoading = ref(false)
const msgList = ref([])
const currentTitle = ref('')

/** 加载会话分页 */
function loadData() {
  loading.value = true
  http.get('admin/session/page', { params: query })
      .then(res => {
        if (res.code === 200) {
          list.value = res.data.list
          total.value = res.data.total
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('查询失败，请检查后端服务'))
      .finally(() => {
        loading.value = false
      })
}

/** 条件变化回到第一页 */
function search() {
  query.pageNum = 1
  loadData()
}

/** 重置条件 */
function reset() {
  query.username = ''
  search()
}

/** 查看某会话的完整对话（数据源就是 AI 的记忆表） */
function viewMessages(row) {
  currentTitle.value = row.title
  drawerVisible.value = true
  msgLoading.value = true
  msgList.value = []
  http.get(`admin/session/${row.sessionId}/messages`)
      .then(res => {
        if (res.code === 200) {
          msgList.value = res.data || []
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('加载对话内容失败'))
      .finally(() => {
        msgLoading.value = false
      })
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.msg-list {
  min-height: 200px;
}

.msg {
  display: flex;
  margin-bottom: 12px;
}

.msg.user {
  justify-content: flex-end;
}

.bubble {
  max-width: 80%;
  padding: 8px 12px;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
  font-size: 13px;
}

.msg.user .bubble {
  background: #409eff;
  color: #fff;
}

.msg.ai .bubble {
  background: #f4f4f5;
  color: #303133;
}

.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 13px;
  padding: 40px 0;
}
</style>
