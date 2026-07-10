<template>
  <div class="login-container">
    <!-- 左侧品牌展示 -->
    <div class="brand-panel">
      <div class="brand-panel-inner">
        <div class="brand-logo-box">
          <div class="brand-logo">
            <span class="brand-logo-text">HIS</span>
          </div>
        </div>
        <div class="brand-title">东软云医院 HIS 系统</div>
        <div class="brand-subtitle">统一工作台 · 管理员后台</div>

        <div class="feature-list">
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-name">医生管理</div>
              <div class="feature-desc">管理医生信息与职称</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><OfficeBuilding /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-name">科室管理</div>
              <div class="feature-desc">科室层级与类型管理</div>
            </div>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="feature-text">
              <div class="feature-name">排班管理</div>
              <div class="feature-desc">灵活安排医生值班</div>
            </div>
          </div>
        </div>

        <div class="brand-footer">
          © {{ currentYear }} 东软云医院 HIS 系统 · All Rights Reserved
        </div>
      </div>
    </div>

    <!-- 右侧登录表单 -->
    <div class="form-panel">
      <div class="form-card">
        <div class="form-header">
          <div class="form-header-tag">欢迎使用</div>
          <h2 class="form-header-title">管理员登录</h2>
          <p class="form-header-desc">请输入您的账号信息以继续使用系统</p>
        </div>

        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="rules"
          size="large"
          label-position="top"
          class="login-form"
          @submit.prevent="handleSubmit"
        >
          <el-form-item prop="username">
            <div class="form-label">
              <el-icon><User /></el-icon>
              <span>用户名</span>
            </div>
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              autocomplete="username"
              class="form-input"
            />
          </el-form-item>

          <el-form-item prop="password">
            <div class="form-label">
              <el-icon><Lock /></el-icon>
              <span>密码</span>
            </div>
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="请输入密码"
              show-password
              autocomplete="current-password"
              class="form-input"
              @keyup.enter="handleSubmit"
            />
          </el-form-item>

          <div class="form-options">
            <el-checkbox v-model="rememberMe" class="remember-check">记住我</el-checkbox>
            <span class="forget-link">忘记密码？</span>
          </div>

          <el-form-item class="submit-item">
            <el-button
              type="primary"
              class="login-btn"
              :loading="loading"
              @click="handleSubmit"
            >
              <template v-if="!loading">
                <el-icon class="btn-icon"><Right /></el-icon>
                <span>立即登录</span>
              </template>
              <span v-else>登录中...</span>
            </el-button>
          </el-form-item>
        </el-form>

      </div>

      <div class="mobile-copyright">
        © {{ currentYear }} 东软云医院 HIS 系统
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, reactive, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  User,
  Lock,
  Right,
  InfoFilled,
  Calendar,
  OfficeBuilding,
  UserFilled
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 使用自定义图标替代（Element Plus 内置无 MedicalIcon，用替代方案）
const MedicalIcon = UserFilled

const loginFormRef = ref(null)
const loading = ref(false)
const rememberMe = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, message: '用户名长度至少为 2', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 4, message: '密码长度至少为 4', trigger: 'blur' }
  ]
}

const currentYear = computed(() => new Date().getFullYear())

async function handleSubmit() {
  if (!loginFormRef.value) return
  try {
    await loginFormRef.value.validate()
  } catch {
    return
  }

  loading.value = true
  try {
    await userStore.login({
      username: loginForm.username.trim(),
      password: loginForm.password
    })
    ElMessage.success(`欢迎，${userStore.realName || userStore.username}`)
    const redirect = route.query.redirect
    if (redirect && typeof redirect === 'string') {
      router.replace(redirect)
    } else {
      router.replace('/')
    }
  } catch (err) {
    // 错误消息已由 request 拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  background: #f5f7fb;
}

/* ============ 左侧品牌展示 ============ */
.brand-panel {
  flex: 1.2;
  background: linear-gradient(135deg, #1e4fd6 0%, #2e6fe8 55%, #4080ff 100%);
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.brand-panel::before,
.brand-panel::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  pointer-events: none;
}

.brand-panel::before {
  width: 420px;
  height: 420px;
  top: -120px;
  right: -120px;
}

.brand-panel::after {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -80px;
}

.brand-panel-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 460px;
  padding: 60px 50px;
  color: #ffffff;
  display: flex;
  flex-direction: column;
}

.brand-logo-box {
  margin-bottom: 28px;
}

.brand-logo {
  width: 72px;
  height: 72px;
  background: rgba(255, 255, 255, 0.18);
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.brand-logo-text {
  font-size: 26px;
  font-weight: 700;
  color: #ffffff;
  letter-spacing: 2px;
}

.brand-title {
  font-size: 30px;
  font-weight: 700;
  line-height: 1.3;
  margin-bottom: 10px;
  letter-spacing: 1px;
}

.brand-subtitle {
  font-size: 15px;
  opacity: 0.88;
  letter-spacing: 2px;
  margin-bottom: 50px;
}

/* 特性列表 */
.feature-list {
  display: flex;
  flex-direction: column;
  gap: 18px;
  margin-bottom: 50px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 14px;
  background: rgba(255, 255, 255, 0.08);
  padding: 14px 18px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  transition: all 0.25s;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.14);
  transform: translateX(4px);
}

.feature-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;
}

.feature-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 2px;
}

.feature-desc {
  font-size: 12px;
  opacity: 0.8;
}

.brand-footer {
  font-size: 12px;
  opacity: 0.7;
  letter-spacing: 0.5px;
}

/* ============ 右侧登录表单 ============ */
.form-panel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  position: relative;
}

.form-card {
  width: 100%;
  max-width: 440px;
  background: #ffffff;
  border-radius: 20px;
  padding: 48px 44px 40px;
  box-shadow: 0 8px 32px rgba(30, 79, 214, 0.08),
    0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid rgba(30, 79, 214, 0.06);
}

.form-header {
  margin-bottom: 36px;
}

.form-header-tag {
  display: inline-block;
  background: rgba(30, 79, 214, 0.1);
  color: #1e4fd6;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 6px;
  margin-bottom: 14px;
  letter-spacing: 1px;
}

.form-header-title {
  margin: 0 0 8px;
  font-size: 26px;
  font-weight: 700;
  color: #1f2d3d;
}

.form-header-desc {
  margin: 0;
  color: #909399;
  font-size: 13px;
}

.login-form {
  margin: 0;
}

.form-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #606266;
  margin-bottom: 8px;
}

.form-label .el-icon {
  color: #1e4fd6;
  font-size: 14px;
}

/* 输入框样式增强 */
.form-input :deep(.el-input__wrapper) {
  border-radius: 10px;
  padding: 0 16px;
  height: 48px;
  box-shadow: 0 0 0 1px #e4e7eb inset;
  background: #fafbfc;
  transition: all 0.2s;
}

.form-input :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #c0c4cc inset;
  background: #ffffff;
}

.form-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(30, 79, 214, 0.25) inset;
  background: #ffffff;
}

.form-input :deep(.el-input__inner) {
  font-size: 14px;
}

.form-input :deep(.el-input__prefix-inner) {
  color: #909399;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -4px 0 20px;
}

.remember-check {
  font-size: 13px;
  color: #606266;
}

.forget-link {
  font-size: 13px;
  color: #1e4fd6;
  cursor: pointer;
  font-weight: 500;
  transition: color 0.15s;
}

.forget-link:hover {
  color: #2e6fe8;
  text-decoration: underline;
}

/* 登录按钮 */
.submit-item {
  margin-bottom: 0;
}

.login-btn {
  width: 100%;
  height: 50px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  letter-spacing: 2px;
  background: linear-gradient(135deg, #1e4fd6 0%, #2e6fe8 100%);
  border: none;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s;
  box-shadow: 0 4px 12px rgba(30, 79, 214, 0.25);
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(30, 79, 214, 0.35);
}

.login-btn:active {
  transform: translateY(0);
}

.btn-icon {
  margin-right: 6px;
  font-size: 16px;
}

/* 底部提示 */
.form-tip {
  margin-top: 24px;
  padding: 12px 16px;
  background: #f5f7fb;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #606266;
  border: 1px dashed #e4e7eb;
}

.tip-icon {
  color: #1e4fd6;
  font-size: 14px;
  flex-shrink: 0;
}

.mobile-copyright {
  position: absolute;
  bottom: 16px;
  font-size: 12px;
  color: #909399;
}

/* ============ 响应式 ============ */
@media (max-width: 960px) {
  .brand-panel {
    display: none;
  }

  .form-panel {
    background: linear-gradient(135deg, #1e4fd6 0%, #2e6fe8 100%);
    padding: 24px 16px;
  }

  .form-card {
    padding: 36px 28px 28px;
  }

  .mobile-copyright {
    color: rgba(255, 255, 255, 0.85);
    position: static;
    margin-top: 20px;
    text-align: center;
  }
}
</style>