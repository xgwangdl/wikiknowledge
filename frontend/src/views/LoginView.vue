<template>
  <div class="login-page">
    <el-card class="login-card">
      <h1 class="title">维基知识库</h1>
      <el-tabs v-model="mode">
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-button type="primary" class="submit" :loading="loading" @click="handleLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="注册" name="register">
          <el-form :model="registerForm" label-position="top">
            <el-form-item label="用户名">
              <el-input v-model="registerForm.username" placeholder="3-50 个字符" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="至少 6 位" />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="registerForm.displayName" placeholder="选填" />
            </el-form-item>
            <el-button type="primary" class="submit" :loading="loading" @click="handleRegister">
              注册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const mode = ref('login')
const loading = ref(false)
const loginForm = reactive({ username: '', password: '' })
const registerForm = reactive({ username: '', password: '', displayName: '' })

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    await auth.login(loginForm)
    router.push('/chat')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  loading.value = true
  try {
    await auth.register(registerForm)
    ElMessage.success('注册成功，请登录')
    mode.value = 'login'
    loginForm.username = registerForm.username
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '注册失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef4ff 0%, #f7f9fc 100%);
}

.login-card {
  width: 420px;
}

.title {
  margin: 0 0 16px;
  text-align: center;
}

.submit {
  width: 100%;
}
</style>
