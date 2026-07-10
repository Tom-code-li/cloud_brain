<template>
  <div class="doctor-page">
    <el-card shadow="never" class="doctor-card">
      <template #header>
        <div class="page-header">
          <span>医生账号管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">新增医生</el-button>
            <el-button type="success" :icon="Refresh" @click="loadList">刷新</el-button>
            <el-button :icon="Download" @click="exportList">导出</el-button>
          </div>
        </div>
      </template>

      <el-form :inline="true" :model="queryForm" class="query-bar" @submit.prevent>
        <el-form-item label="科室">
          <el-select v-model="queryForm.departmentId" placeholder="请选择科室" clearable filterable :filter-method="filterDepartment" @change="onSearch" style="width: 180px">
            <template v-if="hasMultipleDeptTypes">
              <el-option-group v-for="group in filteredDepartmentsByType" :key="group.type" :label="group.type">
                <el-option v-for="dept in group.items" :key="dept.id" :label="formatDeptLabel(dept)" :value="dept.id">
                  <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                  <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                </el-option>
              </el-option-group>
            </template>
            <template v-else>
              <el-option v-for="dept in filteredDepartments" :key="dept.id" :label="formatDeptLabel(dept)" :value="dept.id">
                <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
              </el-option>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="姓名 / 工号">
          <el-input v-model="queryForm.keyword" placeholder="姓名 / 工号 / 专长" clearable style="width: 220px" @keyup.enter="onSearch" />
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="queryForm.title" placeholder="请选择" clearable style="width: 130px" @change="onSearch">
            <el-option v-for="t in titleOptions" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生类型">
          <el-select v-model="queryForm.doctorType" placeholder="请选择" clearable style="width: 130px" @change="onSearch">
            <el-option v-for="opt in doctorTypeOptions" :key="opt.code" :label="opt.label" :value="opt.code" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 120px" @change="onSearch">
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :icon="RefreshLeft" @click="onReset">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="table-wrap">
      <el-table ref="tableRef" :data="tableData" stripe border style="width: 100%" height="100%" empty-text="暂无医生数据" v-loading="loading" @row-click="viewDetail" highlight-current-row>
        <el-table-column prop="doctorNo" label="工号" width="110" />
        <el-table-column prop="doctorName" label="姓名" width="100" />
        <el-table-column prop="loginUsername" label="登录账号" width="120" />
        <el-table-column prop="roleName" label="权限" width="110" />
        <el-table-column label="科室" width="140">
          <template #default="{ row }">
            <el-tag size="small" effect="plain">{{ row.departmentName }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="职称" width="100" />
        <el-table-column prop="doctorType" label="医生类型" width="90" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" show-overflow-tooltip min-width="170" />
        <el-table-column prop="specialty" label="专长" show-overflow-tooltip min-width="150" />
        <el-table-column prop="hireDate" label="入职日期" width="110" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-switch :model-value="row.status === 1" active-color="#13ce66" inactive-color="#ff4949" @change="handleToggle(row)" />
          </template>
        </el-table-column>
        <el-table-column label="操作" fixed="right" width="120">
          <template #default="{ row }">
            <el-button link type="primary" class="action-btn action-edit" @click.stop="openEditDialog(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
      </div>

      <div class="pagination-bar">
        <el-pagination v-model:current-page="queryForm.pageNum" v-model:page-size="queryForm.pageSize" :page-sizes="[10, 20, 50, 100]" :total="total" layout="total, sizes, prev, pager, next, jumper" @size-change="loadList" @current-change="loadList" />
      </div>
    </el-card>

    <!-- 新增 / 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogMode === 'create' ? '新增医生' : '编辑医生'" width="640px" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="90px" class="doctor-form">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="formData.name" placeholder="请输入姓名" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12" v-if="dialogMode === 'edit'">
            <el-form-item label="工号">
              <el-input :model-value="formData.doctorNo" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="权限角色" prop="roleId">
              <el-select v-model="formData.roleId" placeholder="请选择权限角色" filterable style="width: 100%">
                <el-option v-for="r in doctorRoles" :key="r.roleId" :label="r.roleName" :value="r.roleId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录账号" :prop="dialogMode === 'create' ? '' : 'loginUsername'">
              <el-input v-model="formData.loginUsername" :disabled="dialogMode === 'create'" :placeholder="dialogMode === 'create' ? '选择角色后自动生成' : '请输入登录账号'" maxlength="50" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="登录密码" prop="loginPassword">
              <el-input v-model="formData.loginPassword" :placeholder="dialogMode === 'create' ? '留空则默认 123456' : '仅在需要修改时填写'" maxlength="50" show-password />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室" prop="departmentId">
              <el-select v-model="formData.departmentId" placeholder="请选择科室" filterable style="width: 100%">
                <template v-if="hasMultipleDeptTypes">
                  <el-option-group v-for="group in formDeptOptionsByType" :key="group.type" :label="group.type">
                    <el-option v-for="dept in group.items" :key="dept.id" :label="formatDeptLabel(dept)" :value="dept.id">
                      <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                      <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                    </el-option>
                  </el-option-group>
                </template>
                <template v-else>
                  <el-option v-for="dept in formDeptOptions" :key="dept.id" :label="formatDeptLabel(dept)" :value="dept.id">
                    <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                    <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                  </el-option>
                </template>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机" prop="phone">
              <el-input v-model="formData.phone" placeholder="1 开头的 11 位手机号" maxlength="20" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="可选" maxlength="100" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称" prop="title">
              <el-select v-model="formData.title" placeholder="请选择职称" filterable style="width: 100%">
                <el-option v-for="t in titleOptions" :key="t" :label="t" :value="t" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="医生类型">
              <el-select v-model="formData.doctorType" placeholder="请选择" filterable style="width: 100%">
                <el-option
                  v-for="opt in doctorTypeOptions"
                  :key="opt.code"
                  :label="opt.label"
                  :value="opt.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="入职日期">
              <el-date-picker v-model="formData.hireDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="专长">
              <el-input v-model="formData.specialty" placeholder="请输入专长" type="textarea" :rows="2" maxlength="255" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button v-if="dialogMode === 'edit'" type="danger" :loading="deleting" @click="handleDelete">删除</el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="医生详情" width="600px" destroy-on-close @close="onDetailClose">
      <el-descriptions :column="2" border v-if="currentDetail">
        <el-descriptions-item label="姓名">{{ currentDetail.doctorName }}</el-descriptions-item>
        <el-descriptions-item label="工号">{{ currentDetail.doctorNo }}</el-descriptions-item>
        <el-descriptions-item label="登录账号">{{ currentDetail.loginUsername || '-' }}</el-descriptions-item>
        <el-descriptions-item label="权限角色">{{ currentDetail.roleName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="科室">{{ currentDetail.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="职称">{{ currentDetail.title || '-' }}</el-descriptions-item>
        <el-descriptions-item label="医生类型">{{ currentDetail.doctorType || '-' }}</el-descriptions-item>
        <el-descriptions-item label="手机">{{ currentDetail.phone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="邮箱">{{ currentDetail.email || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入职日期">{{ currentDetail.hireDate || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDetail.status === 1 ? 'success' : 'info'" size="small">
            {{ currentDetail.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="专长" :span="2">{{ currentDetail.specialty || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Refresh, Download, RefreshLeft } from '@element-plus/icons-vue'
import { listDoctors, createDoctor, updateDoctor, toggleDoctorStatus, checkDoctorDisable, exportDoctors, listDepartments, listDoctorRoles, deleteDoctor } from '@/api/doctor'

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const total = ref(0)
const tableData = ref([])
const departments = ref([])
const doctorRoles = ref([])
const deptKeyword = ref('')
const titleOptions = ref(['住院医师', '主治医师', '副主任医师', '主任医师'])

// doctorType 英文 code ↔ 中文 label 映射（与后端 doctorTypeToChinese 保持一致）
// 数据库存英文，返回给前端的是中文，前端下拉框 value 用英文、label 用中文
const DOCTOR_TYPE_MAP = {
  OUTPATIENT: '门诊医生',
  LAB: '检验医生',
  EXAM: '检查医生',
  PHARMACY: '药房医生',
  REGISTRATION: '挂号医生'
}
const DOCTOR_TYPE_REVERSE_MAP = Object.fromEntries(
  Object.entries(DOCTOR_TYPE_MAP).map(([code, label]) => [label, code])
)
const doctorTypeOptions = Object.keys(DOCTOR_TYPE_MAP).map((code) => ({
  code,
  label: DOCTOR_TYPE_MAP[code]
}))

// 角色 code → 登录账号前缀映射
const rolePrefixMap = {
  'DOCTOR_CLINIC': 'DOC',
  'PHARMACY': 'PHAR',
  'LAB_DOCTOR': 'LAB',
  'EXAM_DOCTOR': 'EXAM',
  'REGISTRATION_DOCTOR': 'REG',
  'DOCTOR_CHIEF': 'DOC'
}

// -------- 科室下拉选项（按类型分组 + 自定义模糊搜索） --------

const formatDeptLabel = (dept) => {
  if (!dept) return ''
  const parts = []
  if (dept.code) parts.push('[' + dept.code + ']')
  parts.push(dept.name || '')
  return parts.join(' ').trim()
}

const formatDeptSubLabel = (dept) => {
  if (!dept) return ''
  const parts = []
  if (dept.departmentType) parts.push(dept.departmentType)
  if (dept.floor) parts.push(dept.floor)
  return parts.join(' · ')
}

const departmentsByType = computed(() => {
  const list = departments.value || []
  if (!list.length) return []
  const groups = new Map()
  for (const d of list) {
    const type = d.departmentType || '其他'
    if (!groups.has(type)) groups.set(type, [])
    groups.get(type).push(d)
  }
  const types = Array.from(groups.keys()).sort()
  return types.map((type) => ({ type, items: groups.get(type) }))
})

const hasMultipleDeptTypes = computed(() => departmentsByType.value.length > 1)

const matchesDeptKeyword = (dept) => {
  const kw = (deptKeyword.value || '').trim().toLowerCase()
  if (!kw) return true
  const haystack = [dept.name || '', dept.code || '', dept.departmentType || '', dept.floor || ''].join(' ').toLowerCase()
  return haystack.includes(kw)
}

const filteredDepartments = computed(() => {
  const list = (departments.value || []).filter(matchesDeptKeyword)
  return list
})

const filteredDepartmentsByType = computed(() => {
  const list = filteredDepartments.value
  if (!list.length) return []
  const groups = new Map()
  for (const d of list) {
    const type = d.departmentType || '其他'
    if (!groups.has(type)) groups.set(type, [])
    groups.get(type).push(d)
  }
  const types = Array.from(groups.keys()).sort()
  return types.map((type) => ({ type, items: groups.get(type) }))
})

const filterDepartment = (keyword) => { deptKeyword.value = keyword || '' }

// 判断选中角色是否为管理员（roleCode 包含 ADMIN）
const isSelectedRoleAdmin = computed(() => {
  if (!formData.roleId) return false
  const role = doctorRoles.value.find(r => r.roleId === formData.roleId)
  return !!role && role.roleCode && role.roleCode.toUpperCase().includes('ADMIN')
})

// 编辑弹窗：科室下拉选项
// - 若选中角色为管理员：只显示顶级科室（parentId == null）
// - 否则：只显示叶子科室（没有子科室的科室）
const formDeptOptions = computed(() => {
  let list = departments.value || []
  if (!list.length) return []
  if (isSelectedRoleAdmin.value) {
    // 管理员：只显示顶级科室
    return list.filter(d => d.parentId == null)
  } else {
    // 非管理员：只显示叶子科室（没有子科室的科室）
    const parentIds = new Set(list.filter(d => d.parentId != null).map(d => d.parentId))
    return list.filter(d => !parentIds.has(d.id))
  }
})

// 编辑弹窗：按类型分组的科室
const formDeptOptionsByType = computed(() => {
  const list = formDeptOptions.value
  if (!list.length) return []
  const groups = new Map()
  for (const d of list) {
    const type = d.departmentType || '其他'
    if (!groups.has(type)) groups.set(type, [])
    groups.get(type).push(d)
  }
  const types = Array.from(groups.keys()).sort()
  return types.map((type) => ({ type, items: groups.get(type) }))
})

const queryForm = reactive({ departmentId: undefined, keyword: '', title: '', doctorType: '', status: undefined, pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const dialogMode = ref('create')
const formRef = ref(null)
const formData = reactive({
  doctorId: undefined, name: '', doctorNo: '', phone: '', email: '',
  departmentId: undefined, title: '', doctorType: '', specialty: '', hireDate: '',
  loginUsername: '', loginPassword: '', roleId: undefined
})

// 监听角色变化
// - 新增模式：自动生成工号和登录账号提示
// - 角色切换（新增或编辑）：若原科室在新过滤条件下不可选，则清空科室
watch(() => formData.roleId, (newRoleId) => {
  // 新增模式：自动生成工号和登录账号提示
  if (dialogMode.value === 'create' && newRoleId) {
    const role = doctorRoles.value.find(r => r.roleId === newRoleId)
    if (role) {
      const prefix = rolePrefixMap[role.roleCode] || 'DOC'
      const now = new Date()
      const datePart = String(now.getFullYear()) +
        String(now.getMonth() + 1).padStart(2, '0') +
        String(now.getDate()).padStart(2, '0')
      formData.doctorNo = 'D' + datePart + '?'
      formData.loginUsername = prefix + datePart + '?'
    }
  }
  // 角色切换：若当前已选科室不在新过滤列表中，清空科室
  if (newRoleId && formData.departmentId != null) {
    const availableIds = new Set(formDeptOptions.value.map(d => d.id))
    if (!availableIds.has(formData.departmentId)) {
      formData.departmentId = undefined
    }
  }
})

const formRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  roleId: [{ required: true, message: '请选择权限角色', trigger: 'change' }],
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  phone: [{ validator: (_r, v, cb) => (!v || /^1[3-9]\d{9}$/.test(v)) ? cb() : cb(new Error('手机号格式不正确')), trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }]
}

const detailVisible = ref(false)
const currentDetail = ref(null)
const tableRef = ref(null)

// 关键字输入防抖搜索（避免每按一个键发一次请求）
let keywordDebounceTimer = null
watch(
  () => queryForm.keyword,
  () => {
    if (keywordDebounceTimer) clearTimeout(keywordDebounceTimer)
    keywordDebounceTimer = setTimeout(() => {
      queryForm.pageNum = 1
      loadList()
    }, 300)
  }
)

onMounted(async () => {
  try { departments.value = await listDepartments() } catch { departments.value = [] }
  try { doctorRoles.value = await listDoctorRoles() } catch { doctorRoles.value = [] }
  await loadList()
})

async function loadList() {
  loading.value = true
  try {
    const result = await listDoctors({
      departmentId: queryForm.departmentId || undefined,
      keyword: queryForm.keyword || undefined,
      title: queryForm.title || undefined,
      status: queryForm.status,
      pageNum: queryForm.pageNum,
      pageSize: queryForm.pageSize
    })
    tableData.value = result?.list || []
    total.value = result?.total || 0

    // doctorType 前端筛选（后端接口暂不支持，临时在前端过滤）
    if (queryForm.doctorType) {
      const cnType = DOCTOR_TYPE_MAP[queryForm.doctorType]
      if (cnType) {
        tableData.value = tableData.value.filter((d) => d.doctorType === cnType)
        total.value = tableData.value.length
      }
    }

    // 从返回结果动态提取 title 的去重集合（doctorType 已由前端字典维护，不需提取）
    const currentTitles = new Set(titleOptions.value)
    for (const d of (result?.list || [])) {
      if (d.title) currentTitles.add(d.title)
    }
    titleOptions.value = Array.from(currentTitles)
  } catch {
    tableData.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() { queryForm.pageNum = 1; loadList() }

function onReset() {
  Object.assign(queryForm, { departmentId: undefined, keyword: '', title: '', doctorType: '', status: undefined, pageNum: 1 })
  loadList()
}

function resetFormData() {
  Object.assign(formData, {
    doctorId: undefined, name: '', doctorNo: '', phone: '', email: '',
    departmentId: undefined, title: '', doctorType: '', specialty: '', hireDate: '',
    loginUsername: '', loginPassword: '', roleId: undefined
  })
}

function openCreateDialog() { dialogMode.value = 'create'; resetFormData(); dialogVisible.value = true }

function openEditDialog(row) {
  dialogMode.value = 'edit'
  resetFormData()
  Object.assign(formData, {
    doctorId: row.doctorId, name: row.doctorName || '', doctorNo: row.doctorNo || '',
    phone: row.phone || '', email: row.email || '',
    departmentId: row.departmentId, title: row.title || '',
    // 后端返回中文（如"门诊医生"），需反向映射为英文 code（如"OUTPATIENT"）
    doctorType: row.doctorType ? DOCTOR_TYPE_REVERSE_MAP[row.doctorType] || '' : '',
    specialty: row.specialty || '', hireDate: row.hireDate || '',
    loginUsername: row.loginUsername || '', loginPassword: '', roleId: row.roleId
  })
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }

  submitting.value = true
  try {
    const payload = {
      name: formData.name?.trim(),
      phone: formData.phone?.trim() || null, email: formData.email?.trim() || null,
      departmentId: formData.departmentId, title: formData.title,
      doctorType: formData.doctorType, specialty: formData.specialty?.trim() || null,
      hireDate: formData.hireDate || null,
      loginPassword: formData.loginPassword?.trim() || null,
      roleId: formData.roleId || null
    }

    if (dialogMode.value === 'create') {
      // 工号和登录账号由后端根据角色自动生成，无需传入
      await createDoctor(payload)
      ElMessage.success('医生创建成功，默认登录账号为工号，默认密码 123456')
    } else {
      const update = { ...payload }
      delete update.doctorNo
      // 只有非空的账号 / 密码才会在后端被更新
      await updateDoctor(formData.doctorId, update)
      ElMessage.success('医生信息已更新')
    }
    dialogVisible.value = false
    loadList()
  } catch { /* request.js 已处理 */ } finally { submitting.value = false }
}

async function handleToggle(row) {
  if (row.status === 1) {
    try {
      const check = await checkDoctorDisable(row.doctorId)
      if (check && (check.pendingSchedules > 0 || check.pendingRegistrations > 0)) {
        await ElMessageBox.confirm(
          '该医生仍有 ' + check.pendingSchedules + ' 个排班和 ' + check.pendingRegistrations + ' 个挂号未完成，是否确认禁用？',
          '禁用确认', { confirmButtonText: '强制禁用', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' }
        )
        await toggleDoctorStatus(row.doctorId, true)
        ElMessage.success('已禁用')
        loadList()
        return
      }
    } catch { /* 校验失败继续普通切换 */ }

    try {
      await ElMessageBox.confirm('确认禁用该医生账号？禁用后该医生将无法登录。', '禁用确认', {
        confirmButtonText: '确认禁用', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger'
      })
    } catch { return }
  }

  try {
    await toggleDoctorStatus(row.doctorId, false)
    ElMessage.success(row.status === 1 ? '已禁用' : '已启用')
    loadList()
  } catch { /* request.js 已处理 */ }
}

function viewDetail(row) { currentDetail.value = row; detailVisible.value = true }

async function handleDelete() {
  if (!formData.doctorId) return
  try {
    await ElMessageBox.confirm(
      '确认删除该医生账号？删除后将无法恢复，且关联的排班和挂号记录将受影响。',
      '删除医生',
      { confirmButtonText: '确认删除', cancelButtonText: '取消', type: 'warning', confirmButtonClass: 'el-button--danger' }
    )
  } catch { return }
  deleting.value = true
  try {
    await deleteDoctor(formData.doctorId)
    ElMessage.success('医生已删除')
    dialogVisible.value = false
    loadList()
  } catch { /* request.js 已处理 */ } finally { deleting.value = false }
}

// 详情弹窗关闭时清除行选中状态
function onDetailClose() {
  currentDetail.value = null
  if (tableRef.value) tableRef.value.setCurrentRow()
}

async function exportList() {
  try {
    const list = await exportDoctors(queryForm.departmentId)
    if (!list || list.length === 0) { ElMessage.warning('当前筛选条件下没有可导出的数据'); return }

    const headers = ['工号', '姓名', '登录账号', '权限', '科室', '职称', '医生类型', '专长', '手机', '邮箱', '入职日期', '状态']
    const rows = list.map(d => [d.doctorNo, d.doctorName, d.loginUsername || '', d.roleName || '', d.departmentName, d.title || '', d.doctorType || '', d.specialty || '', d.phone || '', d.email || '', d.hireDate || '', d.status === 1 ? '启用' : '停用'])
    const csv = '\uFEFF' + [headers, ...rows].map(r => r.map(c => '"' + String(c).replace(/"/g, '""') + '"').join(',')).join('\n')
    const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = 'doctors-' + new Date().toISOString().slice(0, 10) + '.csv'
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    URL.revokeObjectURL(url)
    ElMessage.success('已导出 ' + list.length + ' 条数据')
  } catch { /* request.js 已处理 */ }
}
</script>

<style scoped>
.doctor-page { height: 100%; padding: 0; display: flex; flex-direction: column; }
.doctor-card { background: #fff; height: 100%; display: flex; flex-direction: column; margin: 0; }
:deep(.doctor-card .el-card__header) { flex-shrink: 0; padding: 14px 20px; }
:deep(.doctor-card .el-card__body) { flex: 1 1 auto; display: flex; flex-direction: column; padding: 12px 20px; min-height: 0; overflow: hidden; }
.page-header { display: flex; align-items: center; justify-content: space-between; font-size: 16px; font-weight: 600; color: #303133; }
.header-actions { display: flex; gap: 8px; }
.query-bar { padding: 4px 0 12px; flex-shrink: 0; }
.table-wrap { flex: 1 1 auto; min-height: 0; display: flex; flex-direction: column; overflow: hidden; }
:deep(.table-wrap .el-table) { flex: 1 1 auto; min-height: 0; }
.pagination-bar { display: flex; justify-content: flex-end; padding-top: 12px; flex-shrink: 0; }
.doctor-form { padding: 4px 8px; }

.action-btn {
  font-size: 15px;
  font-weight: 500;
  padding: 0 6px;
  margin-right: 8px;
  letter-spacing: 1px;
}
.action-btn:last-child { margin-right: 0; }

:deep(.action-btn.action-edit .el-button__inner) { color: #409eff; font-weight: 600; }
:deep(.action-btn.action-edit:hover .el-button__inner) { color: #66b1ff; }

.dept-option-main { font-size: 14px; color: #303133; line-height: 1.4; }
.dept-option-sub { font-size: 12px; color: #909399; line-height: 1.4; margin-top: 2px; }
.dept-option-sub:empty { display: none; }

:deep(.el-select-dropdown__item) { height: auto; min-height: 34px; line-height: 1.4; padding-top: 6px; padding-bottom: 6px; border-bottom: none; }
:deep(.el-select-dropdown__item.hover), :deep(.el-select-dropdown__item:hover) { background-color: #f5f7fa; }
:deep(.el-select-dropdown__group) { padding-top: 6px; padding-bottom: 4px; }
</style>