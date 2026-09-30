<template>
  <div class="auth-page">
    <el-card class="auth-card">
      <template #header>
        <div class="auth-title">智选商城 · 智能客服系统</div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="70px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable/>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码"
                    show-password @keyup.enter="doLogin"/>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="doLogin">登 录</el-button>
          <el-button @click="goRegister">注 册</el-button>
        </el-form-item>
      </el-form>

      <el-alert type="info" :closable="false" show-icon>
        <div>预置账号：</div>
        <div>管理员：<b>admin / admin123</b></div>
        <div>普通用户：<b>zhangsan / 123456</b></div>
      </el-alert>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import http from '@/utils/request'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref()
const loading = ref(false)
const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

/** 登录：成功后按 role 跳转（1=管理员 → 后台，0=普通用户 → 对话页） */
function doLogin() {
  formRef.value.validate(valid => {
    if (!valid) return
    loading.value = true
    http.post('user/login', form)
        .then(res => {
          if (res.code === 200) {
            userStore.setUser(res.data)
            ElMessage.success('登录成功')
            router.push(res.data.role === 1 ? '/admin' : '/chat')
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

/** 跳转注册页 */
function goRegister() {
  router.push('/register')
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
