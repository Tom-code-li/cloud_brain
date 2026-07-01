<script setup>
import { computed, onMounted, ref } from 'vue';
import PageHeader from '../components/PageHeader.vue';
import PatientBanner from '../components/PatientBanner.vue';
import { fetchFeeOrders } from '../api/outpatient.js';
import { usePatientStore } from '../stores/patientStore.js';

const patientStore = usePatientStore();
const patient = computed(() => patientStore.state.activePatient);
const feeItems = ref([]);
const total = computed(() => feeItems.value.reduce((sum, item) => sum + Number(item.amount || 0), 0));

async function loadFeeOrders() {
  const visitId = patientStore.state.medicalRecord?.visitId || patientStore.state.visit?.visitId;
  if (!visitId) {
    feeItems.value = [];
    return;
  }
  const res = await fetchFeeOrders({ visitId });
  feeItems.value = (res.data.list || []).flatMap((order) =>
    (order.items || []).map((item) => ({
      name: item.itemName,
      price: item.unitPrice,
      type: item.itemType,
      spec: item.itemSpec,
      count: item.quantity,
      amount: item.amount,
      time: order.createdAt,
      status: item.status
    }))
  );
}

onMounted(loadFeeOrders);
</script>

<template>
  <section>
    <PatientBanner v-if="patient" :patient="patient" />
    <PageHeader title="费用查询" description="查询患者检查、检验和处方费用记录。">
      <el-tag effect="light" type="primary">项目金额：{{ total.toFixed(2) }} 元</el-tag>
    </PageHeader>

    <el-card class="panel">
      <div class="search-grid">
        <el-input :model-value="patient?.id || ''" placeholder="请输入病历号" />
        <el-input :model-value="patient?.name || ''" placeholder="患者名" />
        <div class="search-actions"><el-button type="primary" @click="loadFeeOrders">搜索</el-button></div>
      </div>
    </el-card>

    <el-card class="panel">
      <el-table :data="feeItems" border stripe height="430">
        <el-table-column type="selection" width="48" />
        <el-table-column prop="name" label="项目名称" min-width="180" />
        <el-table-column prop="price" label="单价" width="100">
          <template #default="{ row }">{{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100" />
        <el-table-column prop="spec" label="规格" min-width="160" />
        <el-table-column prop="count" label="数量" width="80" />
        <el-table-column prop="time" label="开立时间" width="130" />
        <el-table-column prop="status" label="状态" width="120" />
      </el-table>
    </el-card>
  </section>
</template>
