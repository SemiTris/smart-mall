<template>
  <el-container class="admin-layout">
    <!-- 左侧菜单 -->
    <el-aside width="200px" class="aside">
      <div class="logo">智选商城 · 后台</div>
      <el-menu :default-active="$route.path" router
               background-color="#1f2d3d" text-color="#dcdfe6"
               active-text-color="#409eff">
        <el-menu-item index="/admin/dashboard">
          <el-icon><DataLine/></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/admin/user">
          <el-icon><User/></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/product">
          <el-icon><Goods/></el-icon>
          <span>商品管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/order">
          <el-icon><List/></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/admin/session">
          <el-icon><ChatDotRound/></el-icon>
          <span>会话记录</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="header">
        <span class="page-title">{{ $route.meta.title }}</span>
        <div class="header-right">
          <span class="nickname">{{ userStore.user?.nickname }}</span>
          <el-button text size="small" @click="logout">退出登录</el-button>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ChatDotRound, DataLine, Goods, List, User } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

/** 退出登录 */
function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.aside {
  background: #1f2d3d;
}

.logo {
  height: 56px;
  line-height: 56px;
  text-align: center;
  color: #fff;
  font-weight: 600;
  border-bottom: 1px solid #2c3e50;
}

.aside :deep(.el-menu) {
  border-right: none;
}

.header {
  background: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-weight: 600;
  font-size: 15px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.nickname {
  color: #606266;
  font-size: 13px;
}

.main {
  background: #f5f7fa;
  padding: 16px;
}
</style>
