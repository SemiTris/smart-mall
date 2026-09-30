<template>
  <el-card shadow="never">
    <!-- 查询条件 -->
    <el-form inline>
      <el-form-item label="用户名">
        <el-input v-model="query.username" placeholder="模糊搜索" clearable style="width: 160px"
                  @keyup.enter="search"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
          <el-option label="已支付" value="已支付"/>
          <el-option label="已发货" value="已发货"/>
          <el-option label="已完成" value="已完成"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格：展开行显示订单明细（一对多） -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column type="expand">
        <template #default="{ row }">
          <div class="items-box">
            <div class="items-title">该订单包含 {{ (row.items || []).length }} 件商品：</div>
            <el-table :data="row.items" size="small" border>
              <el-table-column prop="productName" label="商品名称" min-width="200"/>
              <el-table-column prop="price" label="单价" width="120">
                <template #default="scope">¥{{ scope.row.price }}</template>
              </el-table-column>
              <el-table-column prop="quantity" label="数量" width="90"/>
              <el-table-column prop="subtotal" label="小计" width="120">
                <template #default="scope">¥{{ scope.row.subtotal }}</template>
              </el-table-column>
            </el-table>
          </div>
        </template>
      </el-table-column>

      <el-table-column prop="orderNo" label="订单号" width="180"/>
      <el-table-column prop="username" label="下单用户" width="110"/>
      <el-table-column prop="totalAmount" label="总金额" width="120">
        <template #default="{ row }">¥{{ row.totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="下单时间" min-width="170"/>
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
  status: ''
})

/** 状态对应的标签颜色 */
function statusType(status) {
  if (status === '已支付') return 'warning'
  if (status === '已发货') return 'primary'
  return 'success'
}

/** 加载订单分页（返回的每行都自带 items 明细） */
function loadData() {
  loading.value = true
  http.get('admin/order/page', { params: query })
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
  query.status = ''
  search()
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}

.items-box {
  padding: 8px 20px 12px 48px;
}

.items-title {
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
</style>
