<template>
  <el-card shadow="never">
    <!-- 查询条件 -->
    <el-form inline>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="模糊搜索" clearable style="width: 180px"
                  @keyup.enter="search"/>
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="query.role" placeholder="全部" clearable style="width: 120px">
          <el-option label="管理员" :value="1"/>
          <el-option label="普通用户" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="username" label="用户名" width="140"/>
      <el-table-column prop="nickname" label="昵称" width="140"/>
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'danger' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'warning'" size="small">
            {{ row.status === 1 ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="170"/>
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
  username: '',
  role: null
})

/** 加载用户分页数据 */
function loadData() {
  loading.value = true
  http.get('admin/user/page', { params: query })
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

/** 条件变化时回到第一页再查 */
function search() {
  query.pageNum = 1
  loadData()
}

/** 重置查询条件 */
function reset() {
  query.username = ''
  query.role = null
  search()
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
