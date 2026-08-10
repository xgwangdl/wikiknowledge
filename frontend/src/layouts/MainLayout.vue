<template>
  <el-container class="layout">
    <el-aside width="220px">
      <div class="logo">维基知识库</div>
      <el-menu router :default-active="$route.path">
        <el-menu-item index="/chat">AI 问答</el-menu-item>
        <el-menu-item index="/knowledge-bases">知识库管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span>{{ auth.user?.displayName || auth.user?.username }}</span>
        <el-button link type="danger" @click="handleLogout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()

function handleLogout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.layout {
  height: 100%;
}

.logo {
  height: 56px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  font-size: 18px;
  font-weight: 600;
  color: #fff;
  background: #1d4ed8;
}

.header {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}
</style>
