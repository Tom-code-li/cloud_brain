<script setup>
import { computed } from 'vue';

const props = defineProps({
  status: {
    type: String,
    default: ''
  }
});

const tagType = computed(() => {
  const value = props.status || '';

  if (['已支付', '已确认', '候诊中', '接诊中', '可退费'].some((item) => value.includes(item))) {
    return 'success';
  }

  if (['待支付', '待确认', '未执行'].some((item) => value.includes(item))) {
    return 'warning';
  }

  if (['不可退', '已退费', '已执行'].some((item) => value.includes(item))) {
    return 'danger';
  }

  return 'info';
});
</script>

<template>
  <el-tag :type="tagType" effect="plain" round>{{ status || '-' }}</el-tag>
</template>
