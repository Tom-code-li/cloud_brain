<template>
  <div class="department-page">
    <!-- 顶部：与排班管理一致的 el-card 风格 -->
    <el-card shadow="never" class="department-card">
      <template #header>
        <div class="page-header">
          <span>科室管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">
              新增科室
            </el-button>
            <el-button type="success" :icon="Refresh" @click="refreshAll">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 主体：左栏目录 + 右栏内容 -->
      <div class="main-layout">
        <!-- 左栏：科室目录 -->
        <div class="sidebar">
          <div class="sidebar-inner">
            <div class="sidebar-head">
              <span class="sidebar-title">科室目录</span>
              <el-tag size="small" type="info" effect="plain">{{ totalTreeCount }} 个</el-tag>
            </div>

            <el-input
              v-model="treeKeyword"
              placeholder="搜索科室"
              clearable
              :prefix-icon="Search"
              size="small"
              class="tree-search"
            />

            <div class="tree-scroll">
            <el-tree
              ref="treeRef"
              class="dept-tree"
              :data="filteredTreeData"
              node-key="id"
              :default-expand-all="true"
              :highlight-current="true"
              :expand-on-click-node="false"
              :show-icon="false"
              @node-click="handleTreeNodeClick"
            >
            <template #default="{ data, node }">
              <div class="dept-tree-node" :class="{ active: selectedId === data.id }">
                <div class="dept-tree-main">
                  <span
                    v-if="(data.children || []).length > 0"
                    class="dept-tree-icon dept-tree-icon-toggle"
                    @click.stop="toggleNodeExpand(node)"
                  >
                    <el-icon v-if="node.expanded"><CaretBottom /></el-icon>
                    <el-icon v-else><CaretRight /></el-icon>
                  </span>
                  <span v-else class="dept-tree-icon">
                    <el-icon><Document /></el-icon>
                  </span>
                  <span class="dept-tree-name">{{ data.name }}</span>
                  <span v-if="data.code" class="dept-tree-code">{{ data.code }}</span>
                </div>
              </div>
            </template>
            </el-tree>
            </div>
          </div>
        </div>

        <!-- 右栏：列表/详情 -->
        <div class="content-area">
          <!-- 列表视图 -->
          <transition name="fade-slide" mode="out-in">
            <div v-if="!selectedId || !currentDetail" key="list" class="right-pane">
              <div class="list-toolbar">
                <div class="list-toolbar-left">
                  <el-input
                    v-model="keyword"
                    placeholder="搜索科室名称或编号"
                    clearable
                    :prefix-icon="Search"
                    style="width: 260px"
                    @input="loadList"
                  />
                </div>
                <div class="list-toolbar-right" style="display: flex; align-items: center; gap: 8px;">
                  <span style="font-size: 13px; color: #606266; white-space: nowrap;">类型筛选：</span>
                  <el-dropdown @command="handleTypeFilter">
                  <el-button>
                      {{ typeFilter ? deptTypeCodeToCn(typeFilter) : '全部' }}
                      <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                    </el-button>
                    <template #dropdown>
                      <el-dropdown-menu>
                        <el-dropdown-item command="">全部</el-dropdown-item>
                        <el-dropdown-item
                          v-for="opt in departmentTypeOptions"
                          :key="opt.code"
                          :command="opt.code"
                        >{{ opt.label }}</el-dropdown-item>
                      </el-dropdown-menu>
                    </template>
                  </el-dropdown>
                </div>
              </div>

              <div class="table-scroll">
              <el-table
                :data="filteredTableData"
                stripe
                style="width: 100%"
                height="100%"
                empty-text="暂无科室数据"
                :header-cell-style="{ background: '#fafafa' }"
                @row-click="viewDetail"
                highlight-current-row
              >
                <el-table-column label="科室" min-width="240">
                  <template #default="{ row }">
                    <div class="table-dept-cell">
                      <div class="table-dept-icon" :class="row.departmentType === '住院' ? 'icon-purple' : 'icon-blue'">
                        <el-icon><OfficeBuilding /></el-icon>
                      </div>
                      <div>
                        <div class="table-dept-name">{{ row.name }}</div>
                        <div class="table-dept-code">{{ row.code || '未分配编号' }}</div>
                      </div>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column label="类型" width="80" align="center">
                  <template #default="{ row }">
                    <el-tag
                      size="small"
                      :type="row.departmentType === '住院' ? 'warning' : 'primary'"
                      effect="plain"
                    >
                      {{ row.departmentType || '-' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="上级科室" width="120" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span class="table-parent">{{ parentName(row.parentId) || '顶级科室' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="楼层" width="120" align="center">
                  <template #default="{ row }">
                    <span>{{ row.floor || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="联系电话" width="130" show-overflow-tooltip>
                  <template #default="{ row }">
                    <span>{{ row.phone || '-' }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="排序" width="70" align="center">
                  <template #default="{ row }">
                    <span class="table-sort-num">{{ row.sortOrder }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="状态" width="70" align="center">
                  <template #default="{ row }">
                    <el-tag
                      size="small"
                      :type="row.status === 1 ? 'success' : 'info'"
                      effect="light"
                    >
                      {{ row.status === 1 ? '启用' : '停用' }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="80" fixed="right" align="center">
                  <template #default="{ row }">
                    <el-tooltip content="编辑" placement="top">
                      <el-button type="primary" link :icon="Edit" @click.stop="openEditDialog(row)" />
                    </el-tooltip>
                    <el-tooltip content="删除" placement="top">
                      <el-button type="danger" link :icon="Delete" @click.stop="handleDelete(row)" />
                    </el-tooltip>
                  </template>
                </el-table-column>
              </el-table>
              </div>
            </div>

            <!-- 详情视图 -->
            <div v-else :key="'detail-' + selectedId" class="right-pane detail-scroll">
              <div class="detail-wrapper">
                <div class="detail-header">
                  <div class="detail-head-left">
                    <div class="detail-avatar">
                      <el-icon><OfficeBuilding /></el-icon>
                    </div>
                    <div>
                      <div class="detail-name">{{ currentDetail.name }}</div>
                      <div class="detail-meta">
                        <el-tag size="small" type="info" effect="plain">
                          {{ currentDetail.code || '未分配编号' }}
                        </el-tag>
                        <el-tag
                          size="small"
                          :type="currentDetail.status === 1 ? 'success' : 'info'"
                          effect="light"
                        >
                          {{ currentDetail.status === 1 ? '启用中' : '已停用' }}
                        </el-tag>
                        <span class="detail-meta-item" v-if="currentDetail.departmentType">
                          <el-icon><Menu /></el-icon>
                          {{ currentDetail.departmentType }}
                        </span>
                      </div>
                    </div>
                  </div>
                  <div class="detail-head-right">
                    <el-button plain :icon="Back" @click="switchToList">返回列表</el-button>
                    <el-button type="primary" :icon="Edit" @click="openEditDialog(currentDetail)">编辑</el-button>
                    <el-button type="danger" plain :icon="Delete" @click="handleDelete(currentDetail)">删除</el-button>
                  </div>
                </div>

                <el-row :gutter="16" class="info-cards">
                  <el-col :span="8">
                    <div class="info-card">
                      <div class="info-card-title">
                        <el-icon><Location /></el-icon>
                        <span>位置信息</span>
                      </div>
                      <div class="info-card-row">
                        <span class="info-card-label">楼层</span>
                        <span class="info-card-value">{{ currentDetail.floor || '-' }}</span>
                      </div>
                      <div class="info-card-row">
                        <span class="info-card-label">联系电话</span>
                        <span class="info-card-value">{{ currentDetail.phone || '-' }}</span>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-card">
                      <div class="info-card-title">
                        <el-icon><Setting /></el-icon>
                        <span>组织信息</span>
                      </div>
                      <div class="info-card-row">
                        <span class="info-card-label">上级科室</span>
                        <span class="info-card-value">{{ parentName(currentDetail.parentId) || '无' }}</span>
                      </div>
                      <div class="info-card-row">
                        <span class="info-card-label">科室类型</span>
                        <span class="info-card-value">{{ currentDetail.departmentType || '-' }}</span>
                      </div>
                      <div class="info-card-row">
                        <span class="info-card-label">排序号</span>
                        <span class="info-card-value">{{ currentDetail.sortOrder }}</span>
                      </div>
                    </div>
                  </el-col>
                  <el-col :span="8">
                    <div class="info-card">
                      <div class="info-card-title">
                        <el-icon><Document /></el-icon>
                        <span>科室简介</span>
                      </div>
                      <div class="info-card-desc">{{ currentDetail.description || '暂无简介' }}</div>
                    </div>
                  </el-col>
                </el-row>

                <!-- 科室医生 -->
                <div class="dept-doctors-card">
                  <div class="dept-doctors-header">
                    <div class="dept-doctors-title">
                      <el-icon><User /></el-icon>
                      <span>科室医生</span>
                      <el-tag size="small" type="primary" effect="plain">{{ deptDoctors.length }} 人</el-tag>
                    </div>
                  </div>

                  <el-table
                    :data="deptDoctors"
                    stripe
                    style="width: 100%"
                    empty-text="暂无医生数据"
                    v-loading="loadingDoctors"
                    max-height="420"
                    @row-click="openDoctorEditDialog"
                  >
                    <el-table-column prop="doctorNo" label="工号" width="110" />
                    <el-table-column prop="doctorName" label="姓名" width="100" />
                    <el-table-column prop="title" label="职称" width="100" />
                    <el-table-column prop="doctorType" label="医生类型" width="90" />
                    <el-table-column prop="phone" label="手机" width="130" />
                    <el-table-column prop="email" label="邮箱" show-overflow-tooltip min-width="170" />
                    <el-table-column prop="specialty" label="专长" show-overflow-tooltip min-width="150" />
                    <el-table-column prop="hireDate" label="入职日期" width="110" />
                    <el-table-column label="状态" width="80" align="center">
                      <template #default="{ row }">
                        <el-tag
                          size="small"
                          :type="row.status === 1 ? 'success' : 'info'"
                          effect="light"
                        >
                          {{ row.status === 1 ? '启用' : '停用' }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </div>

                <!-- 编辑医生弹窗 -->
                <el-dialog
                  v-model="doctorEditVisible"
                  title="编辑医生"
                  width="640px"
                  destroy-on-close
                  top="8vh"
                >
                  <el-form
                    ref="doctorEditFormRef"
                    :model="doctorEditData"
                    label-width="90px"
                  >
                    <el-row :gutter="16">
                      <el-col :span="12">
                        <el-form-item label="姓名">
                          <el-input v-model="doctorEditData.name" maxlength="50" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="职称">
                          <el-select v-model="doctorEditData.title" placeholder="请选择" style="width: 100%" filterable>
                            <el-option v-for="t in doctorTitleOptions" :key="t" :label="t" :value="t" />
                          </el-select>
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="医生类型">
                          <el-select v-model="doctorEditData.doctorType" placeholder="请选择" style="width: 100%" filterable>
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
                        <el-form-item label="手机">
                          <el-input v-model="doctorEditData.phone" maxlength="20" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="邮箱">
                          <el-input v-model="doctorEditData.email" maxlength="100" />
                        </el-form-item>
                      </el-col>
                      <el-col :span="12">
                        <el-form-item label="状态">
                          <el-switch
                            v-model="doctorEditData.status"
                            :active-value="1"
                            :inactive-value="0"
                            active-text="启用"
                            inactive-text="停用"
                          />
                        </el-form-item>
                      </el-col>
                      <el-col :span="24">
                        <el-form-item label="专长">
                          <el-input v-model="doctorEditData.specialty" type="textarea" :rows="2" maxlength="255" />
                        </el-form-item>
                      </el-col>
                    </el-row>
                  </el-form>
                  <template #footer>
                    <el-button @click="doctorEditVisible = false">取消</el-button>
                    <el-button type="primary" :loading="doctorEditSubmitting" @click="submitDoctorEdit">确定</el-button>
                  </template>
                </el-dialog>
              </div>
            </div>
          </transition>
        </div>
      </div>
    </el-card>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增科室' : '编辑科室'"
      width="620px"
      destroy-on-close
      top="8vh"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="科室名称" prop="name">
          <el-input v-model="formData.name" maxlength="50" show-word-limit placeholder="例如：神经内科" />
        </el-form-item>
        <el-form-item label="科室编号" prop="code">
          <el-input
            v-model="formData.code"
            placeholder="不填则自动生成，例如：NEUROLOGY"
            maxlength="50"
          />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级科室" prop="parentId">
              <el-tree-select
                v-model="formData.parentId"
                :data="selectTreeData"
                node-key="id"
                :props="{ label: 'name', children: 'children' }"
                check-strictly
                render-after-expand
                placeholder="不选则为顶级科室"
                clearable
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="科室类型" prop="departmentType">
              <el-select
                v-model="formData.departmentType"
                placeholder="请选择"
                style="width: 100%"
              >
                <el-option
                  v-for="opt in departmentTypeOptions"
                  :key="opt.code"
                  :label="opt.label"
                  :value="opt.code"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="楼层" prop="floor">
              <el-input v-model="formData.floor" placeholder="例如：门诊三楼" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="联系电话" prop="phone">
              <el-input v-model="formData.phone" placeholder="例如：010-12345678" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="排序号" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" :max="9999" controls-position="right" />
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            placeholder="简要描述科室的诊疗方向或特色"
          />
        </el-form-item>
        <el-form-item v-if="dialogMode === 'edit'" label="状态" prop="status">
          <el-switch
            v-model="formData.status"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="停用"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="submitForm">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowDown,
  Back,
  CaretBottom,
  CaretRight,
  Delete,
  Document,
  Edit,
  Location,
  Menu,
  OfficeBuilding,
  Plus,
  Refresh,
  Search,
  User
} from '@element-plus/icons-vue'
import {
  createDepartment,
  deleteDepartment,
  getDepartmentDetail,
  getDepartmentTree,
  listDepartments,
  toggleDepartmentStatus,
  updateDepartment
} from '@/api/department'
import { listDoctors, updateDoctor } from '@/api/doctor'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// ---------- 科室类型字典（英文 code ↔ 中文展示）----------
// 数据库存英文，后端返回时转换为中文
const DEPARTMENT_TYPE_MAP = {
  OUTPATIENT: '门诊',
  LAB: '检验',
  EXAM: '检查',
  PHARMACY: '药房',
  BILLING: '收费'
}
const DEPARTMENT_TYPE_REVERSE_MAP = Object.fromEntries(
  Object.entries(DEPARTMENT_TYPE_MAP).map(([code, label]) => [label, code])
)
const departmentTypeOptions = Object.keys(DEPARTMENT_TYPE_MAP).map((code) => ({
  code,
  label: DEPARTMENT_TYPE_MAP[code]
}))

// 工具函数：中文 → 英文 code
function deptTypeCnToCode(cn) { return DEPARTMENT_TYPE_REVERSE_MAP[cn] || '' }
// 工具函数：英文 code → 中文
function deptTypeCodeToCn(code) { return DEPARTMENT_TYPE_MAP[code] || code || '' }

// 数据 ----------
const treeData = ref([])
const tableData = ref([])
const currentDetail = ref(null)
const selectedId = ref(null)
const keyword = ref('')
const treeKeyword = ref('')
const typeFilter = ref('')
const deptDoctors = ref([])
const loadingDoctors = ref(false)
const doctorEditVisible = ref(false)
const doctorEditFormRef = ref(null)
const doctorEditSubmitting = ref(false)
const doctorTitleOptions = ['住院医师', '主治医师', '副主任医师', '主任医师']
// 医生类型字典（英文 code ↔ 中文展示）
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
// 工具函数：中文 → 英文 code
function doctorTypeCnToCode(cn) { return DOCTOR_TYPE_REVERSE_MAP[cn] || '' }
const doctorEditData = reactive({
  id: null,
  name: '',
  title: '',
  doctorType: '',
  phone: '',
  email: '',
  specialty: '',
  status: 1
})

// ---------- 弹窗 ----------
const dialogVisible = ref(false)
const dialogMode = ref('create')
const submitLoading = ref(false)
const formRef = ref(null)
const formData = reactive({
  name: '',
  code: '',
  parentId: null,
  departmentType: 'OUTPATIENT',
  floor: '',
  phone: '',
  sortOrder: 0,
  description: '',
  status: 1
})

const formRules = {
  name: [{ required: true, message: '请输入科室名称', trigger: 'blur' }]
}

// ---------- 计算 ----------
const enabledCount = computed(() =>
  (tableData.value || []).filter((d) => d.status === 1).length
)
const disabledCount = computed(() =>
  (tableData.value || []).filter((d) => d.status !== 1).length
)
const typeCount = computed(() => {
  const set = new Set()
  for (const d of tableData.value || []) {
    if (d.departmentType) set.add(d.departmentType)
  }
  return set.size
})

const selectTreeData = computed(() => treeData.value)

// 计算完整树的节点总数（包括子科室）
const totalTreeCount = computed(() => {
  let count = 0
  const walk = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      count++
      walk(n.children)
    }
  }
  walk(treeData.value)
  return count
})

// 按关键字过滤树节点（保留父节点）
function filterTreeNodes(nodes, kw) {
  const lower = (kw || '').toLowerCase()
  const walk = (list) => {
    const result = []
    for (const n of list) {
      const nameMatch = !lower || (n.name || '').toLowerCase().includes(lower) || (n.code || '').toLowerCase().includes(lower)
      const children = n.children && n.children.length > 0 ? walk(n.children) : []
      if (nameMatch || children.length > 0) {
        result.push({ ...n, children })
      }
    }
    return result
  }
  return walk(nodes || [])
}
const filteredTreeData = computed(() => filterTreeNodes(treeData.value, treeKeyword.value))

// 获取选中顶级节点及其所有子科室ID集合（含递归）
const selectedChildIds = computed(() => {
  if (!selectedId.value) return null
  // 在树中查找选中节点
  const findNode = (nodes) => {
    if (!nodes) return null
    for (const n of nodes) {
      if (n.id === selectedId.value) return n
      const found = findNode(n.children)
      if (found) return found
    }
    return null
  }
  const node = findNode(treeData.value)
  if (!node) return null
  // 没有子节点的叶子节点不触发过滤
  if (!node.children || node.children.length === 0) return null
  // 收集选中节点自身 + 所有子节点ID
  const ids = new Set()
  ids.add(selectedId.value)
  const collect = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      ids.add(n.id)
      collect(n.children)
    }
  }
  collect(node.children)
  return ids
})

// 列表筛选：永远不显示有子科室的顶级科室
const parentDeptIds = computed(() => {
  const ids = new Set()
  const walk = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      if (n.children && n.children.length > 0) {
        ids.add(n.id)
        walk(n.children)
      }
    }
  }
  walk(treeData.value)
  return ids
})

const filteredTableData = computed(() => {
  const kw = (keyword.value || '').toLowerCase()
  const parentIds = parentDeptIds.value
  const childIds = selectedChildIds.value
  return (tableData.value || []).filter((d) => {
    if (parentIds.has(d.id)) return false
    if (childIds && !childIds.has(d.id)) return false
    const nameMatch = !kw || (d.name || '').toLowerCase().includes(kw) || (d.code || '').toLowerCase().includes(kw)
    // typeFilter 存英文 code，d.departmentType 是中文，需要转换后比对
    const effectiveCnType = typeFilter.value ? deptTypeCodeToCn(typeFilter.value) : ''
    const typeMatch = !effectiveCnType || d.departmentType === effectiveCnType
    return nameMatch && typeMatch
  })
})

function handleTypeFilter(cmd) {
  typeFilter.value = cmd
}

const allDepartmentMap = computed(() => {
  const map = new Map()
  const walk = (nodes) => {
    if (!nodes) return
    for (const n of nodes) {
      map.set(n.id, n.name)
      walk(n.children)
    }
  }
  walk(treeData.value)
  return map
})

function parentName(parentId) {
  if (parentId == null) return ''
  return allDepartmentMap.value.get(parentId) || '-'
}

// ---------- 方法 ----------
async function loadTree() {
  try {
    const data = await getDepartmentTree()
    treeData.value = data
  } catch (e) {
    // request interceptor 已提示
  }
}

async function loadList() {
  try {
    const data = await listDepartments(keyword.value || undefined)
    tableData.value = data
  } catch (e) {
    // ignore
  }
}

function refreshAll() {
  loadTree()
  if (!selectedId.value) {
    loadList()
  } else {
    loadDetail(selectedId.value)
  }
}

function handleTreeNodeClick(data) {
  selectedId.value = data.id
  // 顶级科室（有子科室）：不加载详情，显示子科室列表
  if (data.children && data.children.length > 0) {
    currentDetail.value = null
    return
  }
  // 叶子科室：加载详情
  loadDetail(data.id)
}

function toggleNodeExpand(node) {
  node.expanded = !node.expanded
}

async function loadDetail(id) {
  try {
    const detail = await getDepartmentDetail(id)
    currentDetail.value = detail
    loadDeptDoctors(id)
  } catch (e) {
    currentDetail.value = null
    selectedId.value = null
    deptDoctors.value = []
  }
}

async function loadDeptDoctors(deptId) {
  try {
    loadingDoctors.value = true
    const data = await listDoctors({ departmentId: deptId })
    if (Array.isArray(data)) {
      deptDoctors.value = data
    } else if (data && Array.isArray(data.rows)) {
      deptDoctors.value = data.rows
    } else if (data && Array.isArray(data.list)) {
      deptDoctors.value = data.list
    } else {
      deptDoctors.value = []
    }
  } catch (e) {
    deptDoctors.value = []
  } finally {
    loadingDoctors.value = false
  }
}

function viewDetail(row) {
  selectedId.value = row.id
  loadDetail(row.id)
}

function openDoctorEditDialog(row) {
  if (!row) return
  doctorEditData.id = row.id
  doctorEditData.name = row.doctorName || row.name || ''
  doctorEditData.title = row.title || ''
  doctorEditData.doctorType = doctorTypeCnToCode(row.doctorType) || ''
  doctorEditData.phone = row.phone || ''
  doctorEditData.email = row.email || ''
  doctorEditData.specialty = row.specialty || ''
  doctorEditData.status = row.status ?? 1
  doctorEditVisible.value = true
}

async function submitDoctorEdit() {
  if (!doctorEditData.id) return
  try {
    doctorEditSubmitting.value = true
    await updateDoctor(doctorEditData.id, {
      name: doctorEditData.name.trim(),
      title: doctorEditData.title,
      doctorType: doctorEditData.doctorType,
      phone: doctorEditData.phone,
      email: doctorEditData.email,
      specialty: doctorEditData.specialty,
      status: doctorEditData.status
    })
    ElMessage.success('保存成功')
    doctorEditVisible.value = false
    loadDeptDoctors(selectedId.value)
  } catch (e) {
    // interceptor 已提示
  } finally {
    doctorEditSubmitting.value = false
  }
}

function switchToList() {
  selectedId.value = null
  currentDetail.value = null
  deptDoctors.value = []
  loadList()
}

function openCreateDialog() {
  dialogMode.value = 'create'
  formData.name = ''
  formData.code = ''
  formData.parentId = null
  formData.departmentType = 'OUTPATIENT'
  formData.floor = ''
  formData.phone = ''
  formData.sortOrder = 0
  formData.description = ''
  formData.status = 1
  dialogVisible.value = true
}

let editingId = null
function openEditDialog(row) {
  dialogMode.value = 'edit'
  editingId = row.id
  formData.name = row.name
  formData.code = row.code || ''
  formData.parentId = row.parentId ?? null
  // row.departmentType 是后端返回的中文（如 '门诊'），需转成英文 code
  formData.departmentType = deptTypeCnToCode(row.departmentType) || 'OUTPATIENT'
  formData.floor = row.floor || ''
  formData.phone = row.phone || ''
  formData.sortOrder = row.sortOrder ?? 0
  formData.description = row.description || ''
  formData.status = row.status ?? 1
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }

  submitLoading.value = true
  try {
    if (dialogMode.value === 'create') {
      await createDepartment({
        name: formData.name.trim(),
        code: formData.code || undefined,
        parentId: formData.parentId ?? undefined,
        departmentType: formData.departmentType,
        floor: formData.floor || undefined,
        phone: formData.phone || undefined,
        sortOrder: formData.sortOrder,
        description: formData.description || undefined
      })
      ElMessage.success('新增科室成功')
    } else {
      await updateDepartment(editingId, {
        name: formData.name.trim(),
        code: formData.code || undefined,
        parentId: formData.parentId ?? undefined,
        departmentType: formData.departmentType,
        floor: formData.floor,
        phone: formData.phone,
        sortOrder: formData.sortOrder,
        description: formData.description,
        status: formData.status
      })
      ElMessage.success('编辑科室成功')
    }
    dialogVisible.value = false
    refreshAll()
  } catch (e) {
    // 已在 interceptor 中提示
  } finally {
    submitLoading.value = false
  }
}

async function handleToggleStatus(row) {
  try {
    await toggleDepartmentStatus(row.id)
    ElMessage.success('状态已切换')
    refreshAll()
  } catch (e) {
    // ignore
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(
      `确认删除科室"${row.name}"？该操作不可恢复，且若科室下有关联医生或排班记录将无法删除。`,
      '删除科室',
      {
        confirmButtonText: '确认删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
  } catch (e) {
    return
  }
  try {
    await deleteDepartment(row.id)
    ElMessage.success('删除科室成功')
    if (selectedId.value === row.id) {
      selectedId.value = null
      currentDetail.value = null
    }
    refreshAll()
  } catch (e) {
    // ignore
  }
}

// ---------- 组件通信 ----------
const props = defineProps({
  initialDepartmentId: {
    type: Number,
    default: null
  }
})
const emit = defineEmits(['select'])

onMounted(() => {
  loadTree()
  loadList()
  if (props.initialDepartmentId) {
    selectedId.value = props.initialDepartmentId
    loadDetail(props.initialDepartmentId)
    emit('select', props.initialDepartmentId)
  }
})
</script>

<style scoped>
.department-page {
  height: 100%;
  padding: 0;
  box-sizing: border-box;
  display: flex;
  flex-direction: column;
}

/* ========== 顶部：与排班管理页面一致 ========== */
.department-card {
  border-radius: 12px;
  height: 100%;
  display: flex;
  flex-direction: column;
  margin: 0;
  overflow: hidden;
}

.department-card :deep(.el-card__header) {
  padding: 14px 20px;
  flex-shrink: 0;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.department-card :deep(.el-card__body) {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  padding: 12px 20px;
  min-height: 0;
  overflow: hidden;
}

/* ========== 主体：左栏目录 + 右栏列表/详情 ========== */
.main-layout {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  gap: 16px;
}

/* ========== 左栏：科室目录 ========== */
.sidebar {
  flex-shrink: 0;
  width: 280px;
  background: #fafbfc;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.sidebar-inner {
  display: flex;
  flex-direction: column;
  padding: 16px;
  box-sizing: border-box;
  min-height: 0;
  flex: 1;
}

.tree-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  margin-top: 4px;
}

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
}

.sidebar-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.tree-search {
  margin-bottom: 6px;
}

.dept-tree {
  background: transparent;
}

/* 隐藏 el-tree 默认的展开箭头图标（避免与自定义图标重复） */
.dept-tree :deep(.el-tree-node__expand-icon) {
  display: none;
}

/* 隐藏 leaf 节点的占位图标（leaf 节点没有 expand-icon 但有 leaf-icon 占位） */
.dept-tree :deep(.el-tree-node.is-leaf > .el-tree-node__content > .el-tree-node__expand-icon) {
  display: none;
}

/* 增强子科室缩进层次：每一级更深一点的缩进 + 视觉区分 */
.dept-tree :deep(.el-tree-node__content) {
  height: 38px;
  border-radius: 6px;
  transition: background 0.15s;
}

.dept-tree :deep(.el-tree-node__content:hover) {
  background: #ecf5ff;
}

/* 父节点选中：浅色高亮背景 */
.dept-tree :deep(.el-tree-node.is-current:not(.is-leaf) > .el-tree-node__content) {
  background: #ecf5ff;
}

/* 子科室（叶子节点）选中：深蓝色背景 + 左侧蓝色色条 + 文字加粗 */
.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content) {
  background: #409eff;
  color: #fff;
  box-shadow: 0 2px 6px rgba(64, 158, 255, 0.25);
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-name) {
  color: #fff;
  font-weight: 600;
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-code) {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  border-color: rgba(255, 255, 255, 0.35);
}

.dept-tree :deep(.el-tree-node.is-current.is-leaf > .el-tree-node__content .dept-tree-icon) {
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
}

/* 子科室左侧连接线 + 背景层次 */
.dept-tree :deep(.el-tree-node.is-leaf .el-tree-node__content) {
  background: transparent;
}

/* 调整每级缩进视觉：使用 padding-left 区分层级（在 el-tree 基础上叠加） */
.dept-tree :deep(.el-tree-node .el-tree-node__children .el-tree-node__content) {
  background: #fff;
}

.dept-tree :deep(.el-tree-node .el-tree-node__children .el-tree-node__children .el-tree-node__content) {
  background: #fafbfc;
}

.dept-tree-node {
  display: flex;
  align-items: center;
  width: 100%;
  padding-right: 10px;
  box-sizing: border-box;
}

.dept-tree-main {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
  flex: 1;
}

.dept-tree-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 18px;
  height: 18px;
  border-radius: 4px;
  font-size: 13px;
  flex-shrink: 0;
  color: #909399;
}

.dept-tree-icon-toggle {
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}

.dept-tree-icon-toggle:hover {
  color: #409eff;
  background: #ecf5ff;
}

.dept-tree-name {
  font-size: 14px;
  color: #303133;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dept-tree-code {
  font-size: 11px;
  color: #c0c4cc;
  background: #fff;
  border: 1px solid #ebeef5;
  padding: 1px 6px;
  border-radius: 8px;
  margin-left: 4px;
  flex-shrink: 0;
}

/* ========== 右栏：列表 + 详情 ========== */
.content-area {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.right-pane {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.list-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  flex-shrink: 0;
}

.table-scroll {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

:deep(.table-scroll .el-table) {
  flex: 1 1 auto;
  min-height: 0;
}

.detail-scroll {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
}

/* ========== 列表 / 详情 切换动画 ========== */
.fade-slide-enter-active,
.fade-slide-leave-active {
  transition:
    opacity 0.24s cubic-bezier(0.4, 0, 0.2, 1),
    transform 0.24s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
  opacity: 0;
  transform: translateY(10px) scale(0.985);
}

.fade-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.99);
}

/* ========== 列表表格单元格 ========== */
.table-dept-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 2px 0;
}

.table-dept-icon {
  width: 34px;
  height: 34px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  flex-shrink: 0;
}

.table-dept-icon.icon-blue {
  background: #409eff;
}

.table-dept-icon.icon-purple {
  background: #8e44ad;
}

.table-dept-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
  margin-bottom: 2px;
}

.table-dept-code {
  font-size: 12px;
  color: #909399;
}

.table-parent {
  color: #606266;
  font-size: 13px;
}

.table-simple-cell {
  display: flex;
  align-items: center;
  gap: 6px;
  color: #606266;
  font-size: 14px;
}

.table-simple-cell .el-icon {
  color: #c0c4cc;
  font-size: 13px;
}

.table-sort-num {
  display: inline-block;
  min-width: 28px;
  padding: 2px 8px;
  background: #f5f7fa;
  border-radius: 6px;
  font-size: 12px;
  color: #909399;
}

/* ========== 详情视图 ========== */
.detail-wrapper {
  background: #fff;
  border-radius: 10px;
  padding: 20px;
  border: 1px solid #ebeef5;
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 20px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 20px;
}

.detail-head-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.detail-avatar {
  width: 56px;
  height: 56px;
  background: #409eff;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 28px;
  flex-shrink: 0;
}

.detail-name {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 6px;
}

.detail-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.detail-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #909399;
}

.detail-head-right {
  display: flex;
  gap: 10px;
}

.info-cards {
  margin: 0;
}

.info-card {
  background: #fafbfc;
  border: 1px solid #f0f2f5;
  border-radius: 10px;
  padding: 18px;
  height: 100%;
  box-sizing: border-box;
}

.info-card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 12px;
  padding-bottom: 10px;
  border-bottom: 1px dashed #ebeef5;
}

.info-card-title .el-icon {
  color: #409eff;
}

.info-card-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 0;
  font-size: 13px;
}

.info-card-label {
  color: #909399;
}

.info-card-value {
  color: #303133;
  font-weight: 500;
  text-align: right;
  max-width: 60%;
  overflow: hidden;
  text-overflow: ellipsis;
}

.info-card-desc {
  font-size: 13px;
  color: #606266;
  line-height: 1.8;
}

/* ========== 科室医生 ========== */
.dept-doctors-card {
  margin-top: 16px;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 10px;
  overflow: hidden;
}

.dept-doctors-header {
  padding: 14px 18px;
  border-bottom: 1px solid #f0f2f5;
}

.dept-doctors-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.dept-doctors-title .el-icon {
  color: #409eff;
}
</style>