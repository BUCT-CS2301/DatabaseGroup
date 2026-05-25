<template>
  <div class="settings-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>系统配置</h2>
    </div>

    <!-- 配置选项卡 -->
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 基本设置 -->
      <el-tab-pane label="基本设置" name="basic">
        <el-card shadow="never">
          <el-form :model="basicForm" label-width="120px">
            <el-form-item label="系统名称">
              <el-input v-model="basicForm.systemName" placeholder="请输入系统名称" />
            </el-form-item>
            <el-form-item label="系统Logo">
              <el-upload
                class="avatar-uploader"
                action="#"
                :show-file-list="false"
                :auto-upload="false"
              >
                <img v-if="basicForm.logoUrl" :src="basicForm.logoUrl" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
            </el-form-item>
            <el-form-item label="系统描述">
              <el-input v-model="basicForm.description" type="textarea" :rows="3" placeholder="请输入系统描述" />
            </el-form-item>
            <el-form-item label="版权信息">
              <el-input v-model="basicForm.copyright" placeholder="请输入版权信息" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveBasic">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 安全设置 -->
      <el-tab-pane label="安全设置" name="security">
        <el-card shadow="never">
          <el-form :model="securityForm" label-width="150px">
            <el-form-item label="登录失败锁定">
              <el-input-number v-model="securityForm.maxLoginAttempts" :min="3" :max="10" />
              <span class="form-tip">次后锁定账户</span>
            </el-form-item>
            <el-form-item label="密码最小长度">
              <el-input-number v-model="securityForm.minPasswordLength" :min="6" :max="20" />
              <span class="form-tip">个字符</span>
            </el-form-item>
            <el-form-item label="密码复杂度要求">
              <el-checkbox v-model="securityForm.requireUppercase">包含大写字母</el-checkbox>
              <el-checkbox v-model="securityForm.requireLowercase">包含小写字母</el-checkbox>
              <el-checkbox v-model="securityForm.requireNumbers">包含数字</el-checkbox>
              <el-checkbox v-model="securityForm.requireSpecialChars">包含特殊字符</el-checkbox>
            </el-form-item>
            <el-form-item label="会话超时时间">
              <el-input-number v-model="securityForm.sessionTimeout" :min="5" :max="120" />
              <span class="form-tip">分钟</span>
            </el-form-item>
            <el-form-item label="启用双因素认证">
              <el-switch v-model="securityForm.enable2FA" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveSecurity">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 邮件设置 -->
      <el-tab-pane label="邮件设置" name="email">
        <el-card shadow="never">
          <el-form :model="emailForm" label-width="120px">
            <el-form-item label="SMTP服务器">
              <el-input v-model="emailForm.smtpHost" placeholder="请输入SMTP服务器地址" />
            </el-form-item>
            <el-form-item label="SMTP端口">
              <el-input-number v-model="emailForm.smtpPort" :min="1" :max="65535" />
            </el-form-item>
            <el-form-item label="用户名">
              <el-input v-model="emailForm.username" placeholder="请输入SMTP用户名" />
            </el-form-item>
            <el-form-item label="密码">
              <el-input v-model="emailForm.password" type="password" placeholder="请输入SMTP密码" show-password />
            </el-form-item>
            <el-form-item label="发件人邮箱">
              <el-input v-model="emailForm.fromEmail" placeholder="请输入发件人邮箱" />
            </el-form-item>
            <el-form-item label="启用SSL">
              <el-switch v-model="emailForm.enableSSL" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleTestEmail">发送测试邮件</el-button>
              <el-button @click="handleSaveEmail">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>

      <!-- 文件存储设置 -->
      <el-tab-pane label="文件存储" name="storage">
        <el-card shadow="never">
          <el-form :model="storageForm" label-width="150px">
            <el-form-item label="存储类型">
              <el-radio-group v-model="storageForm.type">
                <el-radio label="local">本地存储</el-radio>
                <el-radio label="oss">对象存储</el-radio>
                <el-radio label="minio">MinIO存储</el-radio>
              </el-radio-group>
            </el-form-item>
            <template v-if="storageForm.type === 'oss'">
              <el-form-item label="Bucket名称">
                <el-input v-model="storageForm.ossBucket" placeholder="请输入Bucket名称" />
              </el-form-item>
              <el-form-item label="Endpoint">
                <el-input v-model="storageForm.ossEndpoint" placeholder="请输入Endpoint" />
              </el-form-item>
              <el-form-item label="AccessKey">
                <el-input v-model="storageForm.ossAccessKey" placeholder="请输入AccessKey" />
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input v-model="storageForm.ossSecretKey" type="password" placeholder="请输入SecretKey" show-password />
              </el-form-item>
            </template>
            <template v-if="storageForm.type === 'minio'">
              <el-form-item label="Endpoint">
                <el-input v-model="storageForm.minioEndpoint" placeholder="请输入Endpoint" />
              </el-form-item>
              <el-form-item label="AccessKey">
                <el-input v-model="storageForm.minioAccessKey" placeholder="请输入AccessKey" />
              </el-form-item>
              <el-form-item label="SecretKey">
                <el-input v-model="storageForm.minioSecretKey" type="password" placeholder="请输入SecretKey" show-password />
              </el-form-item>
              <el-form-item label="Bucket名称">
                <el-input v-model="storageForm.minioBucket" placeholder="请输入Bucket名称" />
              </el-form-item>
            </template>
            <el-form-item label="文件大小限制">
              <el-input-number v-model="storageForm.maxFileSize" :min="1" :max="100" />
              <span class="form-tip">MB</span>
            </el-form-item>
            <el-form-item label="允许的文件类型">
              <el-checkbox-group v-model="storageForm.allowedTypes">
                <el-checkbox label="jpg">JPG</el-checkbox>
                <el-checkbox label="png">PNG</el-checkbox>
                <el-checkbox label="gif">GIF</el-checkbox>
                <el-checkbox label="pdf">PDF</el-checkbox>
                <el-checkbox label="doc">DOC</el-checkbox>
                <el-checkbox label="docx">DOCX</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleSaveStorage">保存设置</el-button>
            </el-form-item>
          </el-form>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

// 当前激活的选项卡
const activeTab = ref('basic')

// 基本设置表单
const basicForm = reactive({
  systemName: '博物馆后台管理系统',
  logoUrl: '',
  description: '一个功能完善的博物馆后台管理系统，用于管理文物数据、用户信息、内容审核等功能。',
  copyright: '© 2026 DatabaseGroup. All rights reserved.'
})

// 安全设置表单
const securityForm = reactive({
  maxLoginAttempts: 5,
  minPasswordLength: 8,
  requireUppercase: true,
  requireLowercase: true,
  requireNumbers: true,
  requireSpecialChars: false,
  sessionTimeout: 30,
  enable2FA: false
})

// 邮件设置表单
const emailForm = reactive({
  smtpHost: 'smtp.example.com',
  smtpPort: 465,
  username: 'noreply@example.com',
  password: '',
  fromEmail: 'noreply@example.com',
  enableSSL: true
})

// 文件存储设置表单
const storageForm = reactive({
  type: 'local',
  ossBucket: '',
  ossEndpoint: '',
  ossAccessKey: '',
  ossSecretKey: '',
  minioEndpoint: 'localhost:9000',
  minioAccessKey: 'minioadmin',
  minioSecretKey: 'minioadmin123',
  minioBucket: 'admin-files',
  maxFileSize: 10,
  allowedTypes: ['jpg', 'png', 'gif', 'pdf']
})

// 保存基本设置
const handleSaveBasic = () => {
  ElMessage.success('基本设置保存成功')
}

// 保存安全设置
const handleSaveSecurity = () => {
  ElMessage.success('安全设置保存成功')
}

// 发送测试邮件
const handleTestEmail = () => {
  ElMessage.success('测试邮件已发送')
}

// 保存邮件设置
const handleSaveEmail = () => {
  ElMessage.success('邮件设置保存成功')
}

// 保存存储设置
const handleSaveStorage = () => {
  ElMessage.success('存储设置保存成功')
}
</script>

<style scoped>
.settings-container {
  padding: 0;
}

.page-header {
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.form-tip {
  margin-left: 12px;
  color: #909399;
  font-size: 14px;
}

.avatar-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader:hover {
  border-color: #409EFF;
}

.avatar {
  width: 100px;
  height: 100px;
  object-fit: cover;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
}
</style>
