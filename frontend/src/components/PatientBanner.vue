<script setup>
defineProps({
  patient: {
    type: Object,
    required: true
  }
});
</script>

<template>
  <div class="patient-banner">
    <span class="label">患者信息：</span>
    <span class="chip">姓名：{{ patient.name }}</span>
    <span class="chip">病历号：{{ patient.id }}</span>
    <span class="chip">年龄：{{ patient.age }}</span>
    <span class="chip">性别：{{ patient.gender }}</span>
  </div>
</template>
