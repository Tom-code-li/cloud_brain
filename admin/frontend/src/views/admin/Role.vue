<template>
  <div class="role-page">
    <el-card shadow="never" class="role-card">
      <template #header>
        <div class="page-header">
          <span>角色管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增角色</el-button>
            <el-button type="success" :icon="Refresh" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <div class="table-wrap">
        <el-table :data="tableData" stripe border style="width: 100%" height="100%" empty-text="暂无角色数据" v-loading="loading" @row-click="viewDetail" highlight-current-row>
          <el-table-column prop="roleId" label="ID" width="80" />
          <el-table-column prop="roleCode" label="角色编码" width="200" />
          <el-table-column prop="roleName" label="角色名称" width="150" />
          <el-table-column prop="description" label="描述" min-width="250" show-overflow-tooltip />
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch :model-value="row.status === 1" active-color="#13ce66" inactive-color="#ff4949" @change.stop="handleToggle(row)" />
            </template>
          </el-table-column>
          <el-table-column label="操作" fixed="right" width="140" align="center">
            <template #default="{ row }">
              <el-button link type="primary" @click.stop="openEditDialog(row)">编辑</el-button>
              <el-button link type="danger" @click.stop="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增角色' : '编辑角色'" width="480px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="100px">
        <el-form-item label="角色编码" prop="roleCode">
          <el-input v-model="formData.roleCode" placeholder="例如：DOCTOR_CLINIC" maxlength="50" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="例如：门诊医生" maxlength="50" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="formData.description" type="textarea" :rows="3" maxlength="255" placeholder="角色描述" />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'edit'" label="状态">
          <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" active-text="启用" inactive-text="停用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { listRoles, createRole, updateRole, deleteRole, toggleRoleStatus } from '@/api/role'

const loading = ref(false)
const submitting = ref(false)
const tableData = ref([])

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const editingId = ref(null)
const formData = reactive({ roleCode: '', roleName: '', description: '', status: 1 })

const formRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }]
}

onMounted(() => loadList())

async function loadList() {
  loading.value = true
  try { tableData.value = await listRoles() || [] }
  catch { tableData.value = [] }
  finally { loading.value = false }
}

function resetForm() {
  formData.roleCode = ''
  formData.roleName = ''
  formData.description = ''
  formData.status = 1
  editingId.value = null
}

function openCreateDialog() {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId.value = row.roleId
  formData.roleCode = row.roleCode
  formData.roleName = row.roleName
  formData.description = row.description || ''
  formData.status = row.status ?? 1
  dialogVisible.value = true
}

function viewDetail(row) { openEditDialog(row) }

async function submitForm() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  submitting.value = true
  try {
    const payload = { roleCode: formData.roleCode.trim(), roleName: formData.roleName.trim(), description: formData.description?.trim() || null, status: formData.status }
    if (dialogMode.value === 'create') {
      await createRole(payload)
      ElMessage.success('新增角色成功')
    } else {
      await updateRole(editingId.value, payload)
      ElMessage.success('编辑角色成功')
    }
    dialogVisible.value = false
    loadList()
  } catch { /* interceptor */ } finally { submitting.value = false }
}

async function handleToggle(row) {
  try {
    await toggleRoleStatus(row.roleId)
    ElMessage.success('状态已切换')
    loadList()
  } catch { /* ignore */ }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除角色"${row.roleName}"？`, '删除角色', { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' })
  } catch { return }
  try {
    await deleteRole(row.roleId)
    ElMessage.success('删除角色成功')
    loadList()
  } catch { /* ignore */ }
}
</script>

<style scoped>
.role-page { height: 100%; padding: 12px; box-sizing: border-box; display: flex; flex-direction: column; }
.role-card { flex: 1; display: flex; flex-direction: column; min-height: 0; }
:deep(.role-card .el-card__header) { flex-shrink: 0; padding: 14px 20px; }
:deep(.role-card .el-card__body) { flex: 1 1 auto; display: flex; flex-direction: column; padding: 12px 20px; min-height: 0; overflow: hidden; }
.page-header { display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.table-wrap { flex: 1 1 auto; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }
:deep(.table-wrap .el-table) { flex: 1 1 auto; min-height: 0; }
</style>