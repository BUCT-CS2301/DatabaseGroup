<template>
  <div class="login">
    <el-card class="box" shadow="never">
      <h2>后台管理系统</h2>
      <el-tabs v-model="activeTab" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" label-width="70px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                show-password
                placeholder="请输入密码"
                @keyup.enter="onLogin"
              />
            </el-form-item>
            <el-button class="submit-btn" type="primary" :loading="loading" @click="onLogin">
              登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form ref="registerFormRef" :model="registerForm" :rules="registerRules" label-width="70px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="registerForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="registerForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="registerForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="registerForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="registerForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-button class="submit-btn" type="primary" :loading="registering" @click="onRegister">
              注册
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { createUser } from '@/api/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const activeTab = ref('login')
const loading = ref(false)
const registering = ref(false)
const loginFormRef = ref<FormInstance>()
const registerFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const registerForm = reactive({
  username: '',
  password: '',
  nickname: '',
  email: '',
  phone: ''
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function onLogin() {
  await loginFormRef.value?.validate()
  loading.value = true
  try {
    await userStore.login(loginForm.username, loginForm.password)
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? decodeURIComponent(route.query.redirect) : ''
    router.push(redirect || userStore.defaultRoute)
  } catch (error) {
    ElMessage.error('登录失败，请检查用户名或密码')
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  await registerFormRef.value?.validate()
  registering.value = true
  try {
    await createUser(registerForm)
    ElMessage.success('注册成功，请登录')
    loginForm.username = registerForm.username
    loginForm.password = ''
    activeTab.value = 'login'
  } finally {
    registering.value = false
  }
}
</script>

<style scoped>
.login {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f7fa;
}

.box {
  width: 430px;
  padding: 18px 20px 24px;
  border-radius: 8px;
}

.box h2 {
  margin: 0 0 18px;
  text-align: center;
  font-size: 22px;
  font-weight: 600;
}

.submit-btn {
  width: 100%;
}
</style>
