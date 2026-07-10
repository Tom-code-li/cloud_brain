<script setup>
import { computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';

const route = useRoute();
const router = useRouter();

const tabs = [
  { key: 'medical-home', label: '病历首页', route: '/medical-home' },
  { key: 'exam-req', label: '检查申请', route: '/exam-request' },
  { key: 'lab-req', label: '检验申请', route: '/lab-request' },
  { key: 'ai-report-detail', label: '报告回阅', route: '/ai-report-detail' },
  { key: 'diagnosis-detail', label: '门诊确诊', route: '/diagnosis-detail' },
  { key: 'prescription-detail', label: '开设处方', route: '/prescription-detail' }
];

const active = computed(() => route.name);

function go(tab) {
  if (route.path !== tab.route) {
    router.push(tab.route);
  }
}
</script>

<template>
  <div class="encounter-tabs">
    <button
      v-for="tab in tabs"
      :key="tab.key"
      class="encounter-tab"
      :class="{ active: active === tab.key }"
      @click="go(tab)"
    >
      {{ tab.label }}
    </button>
  </div>
</template>
