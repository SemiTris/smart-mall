<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-title">注册新账号</div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录用的账号" clearable/>
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="AI 会这样称呼你（可留空，默认用用户名）" clearable/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" show-password/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doRegister">注 册</el-button>
          <el-button @click="goLogin">返回登录</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/utils/request'

const router = useRouter()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', nickname: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ min: 6, required: true, message: '密码至少 6 位', trigger: 'blur' }]
}

/** 注册：成功后回登录页 */
function doRegister() {
  formRef.value.validate(valid => {
    if (!valid) return
    loading.value = true
    http.post('user/register', form)
        .then(res => {
          if (res.code === 200) {
            ElMessage.success('注册成功，请登录')
            router.push('/login')
          } else {
            ElMessage.error(res.msg)
          }
        })
        .catch(() => ElMessage.error('网络异常，请检查后端服务是否已启动'))
        .finally(() => {
          loading.value = false
        })
  })
}

/** 返回登录页 */
function goLogin() {
  router.push('/login')
}
</script>

<style scoped>
.auth-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.auth-card {
  width: 420px;
}

.auth-title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
  color: #303133;
}
</style>
