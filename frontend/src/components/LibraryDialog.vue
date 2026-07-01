<script setup>
defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    required: true
  },
  keyword: {
    type: String,
    default: ''
  },
  items: {
    type: Array,
    default: () => []
  },
  codeLabel: {
    type: String,
    default: '编码'
  },
  nameLabel: {
    type: String,
    default: '名称'
  }
});

const emit = defineEmits(['update:modelValue', 'update:keyword', 'search', 'select']);

function closeDialog(value) {
  emit('update:modelValue', value);
}

function changeKeyword(value) {
  emit('update:keyword', value);
  emit('search');
}
</script>

<template>
  <el-dialog :model-value="modelValue" :title="title" width="860px" @update:model-value="closeDialog">
    <el-input
      :model-value="keyword"
      placeholder="请输入编码或名称搜索"
      clearable
      class="dialog-search"
      @update:model-value="changeKeyword"
    />
    <el-table :data="items" border height="380" class="dialog-table">
      <el-table-column prop="code" :label="codeLabel" min-width="120" />
      <el-table-column prop="name" :label="nameLabel" min-width="160" />
      <el-table-column v-if="items.some((item) => item.icd)" prop="icd" label="国际ICD编码" min-width="140" />
      <el-table-column v-if="items.some((item) => item.type)" prop="type" label="疾病类型" min-width="120" />
      <el-table-column v-if="items.some((item) => item.spec)" prop="spec" label="规格" min-width="120" />
      <el-table-column v-if="items.some((item) => item.price !== undefined)" prop="price" label="单价" width="100">
        <template #default="{ row }">
          {{ Number(row.price).toFixed(2) }}
        </template>
      </el-table-column>
      <el-table-column v-if="items.some((item) => item.feeType)" prop="feeType" label="费用分类" min-width="120" />
      <el-table-column label="操作" width="96">
        <template #default="{ row }">
          <el-button link type="primary" @click="emit('select', row)">选择</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-hint">暂无数据</div>
      </template>
    </el-table>
  </el-dialog>
</template>
