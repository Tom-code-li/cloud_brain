<script setup>
import { computed, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '../stores/authStore.js';
import { resolveWorkbenchRoute } from '../config/menu.js';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const loading = ref(false);
const form = reactive({
  account: 'REG2025001',
  password: '123456'
});
const redirectPath = computed(() => (typeof route.query.redirect === 'string' ? route.query.redirect : ''));

async function submitLogin() {
  loading.value = true;
  try {
    const user = await authStore.login(form.account.trim(), form.password);
    ElMessage.success('登录成功');
    router.replace(redirectPath.value || user.workbenchRoute || resolveWorkbenchRoute(user));
  } catch (error) {
    ElMessage.error(error?.response?.data?.message || '账户或密码错误');
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="login-screen">
    <div class="login-shell">
      <div class="login-hero">
        <div class="login-brand">东软云医院 HIS 系统</div>
        <div class="login-note">统一登录后，系统将根据身份自动跳转到对应的门诊医生或挂号医生工作台。</div>
        <div class="login-role-row">
          <span class="login-role-chip">门诊医生</span>
          <span class="login-role-chip">挂号医生</span>
        </div>
      </div>

      <div class="login-panel">
        <div class="login-subtitle">统一登录</div>
        <div class="login-caption">请输入账户与密码</div>

        <el-form class="login-form" label-position="top" @submit.prevent>
          <el-form-item label="账户">
            <el-input v-model="form.account" placeholder="账户" @keyup.enter="submitLogin" />
          </el-form-item>
          <el-form-item label="密码">
            <el-input v-model="form.password" type="password" placeholder="密码" show-password @keyup.enter="submitLogin" />
          </el-form-item>
          <el-button type="primary" :loading="loading" class="login-button" @click="submitLogin">登录</el-button>
        </el-form>
      </div>
    </div>
  </div>
</template>
