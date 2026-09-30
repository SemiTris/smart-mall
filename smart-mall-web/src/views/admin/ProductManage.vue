<template>
  <el-card shadow="never">
    <!-- 查询条件 -->
    <el-form inline>
      <el-form-item label="关键词">
        <el-input v-model="query.keyword" placeholder="名称 / 品牌" clearable style="width: 180px"
                  @keyup.enter="search"/>
      </el-form-item>
      <el-form-item label="类别">
        <el-input v-model="query.category" placeholder="如：手机" clearable style="width: 120px"
                  @keyup.enter="search"/>
      </el-form-item>
      <el-form-item label="状态">
        <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
          <el-option label="上架" :value="1"/>
          <el-option label="下架" :value="0"/>
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="search">查询</el-button>
        <el-button @click="reset">重置</el-button>
        <el-button type="success" @click="openDialog()">+ 新增商品</el-button>
      </el-form-item>
    </el-form>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="70"/>
      <el-table-column prop="name" label="商品名称" min-width="200"/>
      <el-table-column prop="price" label="价格" width="110">
        <template #default="{ row }">¥{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="category" label="类别" width="100"/>
      <el-table-column prop="brand" label="品牌" width="100"/>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
          <el-button link type="warning" @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
          <el-button link type="danger" @click="remove(row)">删除</el-button>
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

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑商品' : '新增商品'" width="520px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="商品名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入商品名称"/>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" :step="100" style="width: 200px"/>
        </el-form-item>
        <el-form-item label="类别" prop="category">
          <el-input v-model="form.category" placeholder="如：手机 / 耳机 / 笔记本"/>
        </el-form-item>
        <el-form-item label="品牌">
          <el-input v-model="form.brand" placeholder="如：华为"/>
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="商品描述"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import http from '@/utils/request'

const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  pageNum: 1,
  pageSize: 10,
  keyword: '',
  category: '',
  status: null
})

// ---------- 弹窗 ----------
const dialogVisible = ref(false)
const formRef = ref()
const form = reactive({
  id: null, name: '', price: 0, category: '', brand: '', description: '', status: 1
})
const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  category: [{ required: true, message: '请输入类别', trigger: 'blur' }]
}

/** 加载商品分页 */
function loadData() {
  loading.value = true
  http.get('admin/product/page', { params: query })
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
  query.keyword = ''
  query.category = ''
  query.status = null
  search()
}

/** 打开新增 / 编辑弹窗 */
function openDialog(row) {
  if (row) {
    Object.assign(form, row)
  } else {
    Object.assign(form, { id: null, name: '', price: 0, category: '', brand: '', description: '', status: 1 })
  }
  dialogVisible.value = true
}

/** 保存（id 为空=新增，非空=修改） */
function save() {
  formRef.value.validate(valid => {
    if (!valid) return
    saving.value = true
    http.post('admin/product', form)
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('保存成功')
            dialogVisible.value = false
            loadData()
          } else {
            ElMessage.error(res.msg)
          }
        })
        .catch(() => ElMessage.error('保存失败'))
        .finally(() => {
          saving.value = false
        })
  })
}

/** 上下架 */
function toggleStatus(row) {
  const next = row.status === 1 ? 0 : 1
  http.put(`admin/product/${row.id}/status?status=${next}`)
      .then(res => {
        if (res.code === 200) {
          ElMessage.success('操作成功')
          loadData()
        } else {
          ElMessage.error(res.msg)
        }
      })
      .catch(() => ElMessage.error('操作失败'))
}

/** 删除（逻辑删除） */
function remove(row) {
  ElMessageBox.confirm(`确定删除商品「${row.name}」吗？`, '提示', { type: 'warning' })
      .then(() => {
        http.delete(`admin/product/${row.id}`)
            .then(res => {
              if (res.code === 200) {
                ElMessage.success('删除成功')
                loadData()
              } else {
                ElMessage.error(res.msg)
              }
            })
      })
      .catch(() => {
      })
}

onMounted(loadData)
</script>

<style scoped>
.pager {
  margin-top: 16px;
  justify-content: flex-end;
}
</style>
