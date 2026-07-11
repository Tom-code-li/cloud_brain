<template>
  <div class="schedule-page">
    <el-card shadow="never" class="schedule-card">
      <template #header>
        <div class="page-header">
          <span>排班管理</span>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="openCreateDialog">
              新增排班
            </el-button>
            <el-button type="warning" :icon="MagicStick" @click="openAIDialog">
              AI 排班
            </el-button>
            <el-button type="success" :icon="Refresh" @click="loadList">刷新</el-button>
          </div>
        </div>
      </template>

      <div class="schedule-body">
      <el-form :inline="true" :model="queryForm" class="query-bar" @submit.prevent>
        <el-form-item label="科室">
          <el-select
            v-model="queryForm.departmentId"
            placeholder="请选择科室"
            clearable
            filterable
            :filter-method="filterDepartment"
            style="width: 180px"
          >
            <!-- 多类型时分组展示；单类型时扁平化 -->
            <template v-if="hasMultipleDeptTypes">
              <el-option-group
                v-for="group in filteredDepartmentsByType"
                :key="group.type"
                :label="group.type"
              >
                <el-option
                  v-for="dept in group.items"
                  :key="dept.id"
                  :label="formatDeptLabel(dept)"
                  :value="dept.id"
                >
                  <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                  <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                </el-option>
              </el-option-group>
            </template>
            <template v-else>
              <el-option
                v-for="dept in filteredDepartments"
                :key="dept.id"
                :label="formatDeptLabel(dept)"
                :value="dept.id"
              >
                <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
              </el-option>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select
            v-model="queryForm.title"
            placeholder="请选择"
            clearable
            style="width: 130px"
          >
            <el-option
              v-for="t in titleOptions"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="医生类型">
          <el-select
            v-model="queryForm.doctorType"
            placeholder="请选择"
            clearable
            style="width: 120px"
          >
            <el-option
              v-for="t in doctorTypeOptions"
              :key="t"
              :label="t"
              :value="t"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="周次">
          <el-select
            v-model="queryWeekOffset"
            placeholder="选择周次"
            style="width: 200px"
          >
            <el-option
              v-for="opt in weekOptions"
              :key="opt.label"
              :label="opt.label"
              :value="opt.offset"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="warning" :icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <div class="matrix-wrap" v-loading="loading">
        <el-table
          v-if="weekDates.length > 0"
          :data="matrixRows"
          border
          height="100%"
          style="width: 100%"
          :empty-text="loading ? '加载中...' : '暂无数据'"
          :header-cell-style="{ background: '#fafafa' }"
        >
          <el-table-column label="医生信息" width="260" fixed="left">
            <template #default="{ row }">
              <div class="doctor-cell">
                <el-avatar :size="40" style="background: #409eff; flex-shrink: 0">
                  {{ (row.doctorName || '医').slice(0, 1) }}
                </el-avatar>
                <div class="doctor-cell-info">
                  <div class="doctor-cell-name">
                    {{ row.doctorName || '-' }}
                    <span
                      v-if="row.title"
                      class="title-chip"
                      :class="titleChipClass(row.title)"
                    >
                      {{ row.title }}
                    </span>
                    <span
                      v-if="row.doctorType"
                      class="doctor-type-chip"
                      :class="doctorTypeChipClass(row.doctorType)"
                    >
                      {{ row.doctorType }}
                    </span>
                  </div>
                  <div class="doctor-cell-sub">
                    <span v-if="row.specialty">{{ row.specialty }}</span>
                    <span v-if="!row.specialty">-</span>
                  </div>
                  <div class="doctor-cell-dept">{{ row.departmentName || '-' }}</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column
            v-for="week in weekGroups"
            :key="'g-' + week.key"
            :label="week.label"
            align="center"
          >
            <el-table-column
              v-for="d in week.dates"
              :key="d.dateStr"
              :label="d.label"
              :prop="'d-' + d.dateStr"
              align="center"
              min-width="130"
            >
              <template #default="{ row }">
                <div class="slot-cell">
                  <template
                    v-for="slot in ['上午', '下午', '夜间']"
                    :key="slot"
                  >
                    <div
                      v-if="getScheduleItem(row, d.dateStr, slot)"
                      class="slot-item"
                      :class="slotClass(row, d.dateStr, slot)"
                      @click="openDetailDialog(getScheduleItem(row, d.dateStr, slot))"
                    >
                      <span class="slot-label">{{ displayTimeSlot(slot) }}</span>
                      <span class="slot-info">{{ slotInfo(row, d.dateStr, slot) }}</span>
                    </div>
                    <div
                      v-else
                      class="slot-item slot-empty"
                      @click="openCreateForSlot(row, d.dateStr, slot)"
                    >
                      <span class="slot-label">{{ displayTimeSlot(slot) }}</span>
                      <span class="slot-info">+ 新增</span>
                    </div>
                  </template>
                </div>
              </template>
            </el-table-column>
          </el-table-column>
          <el-table-column label="操作" width="120" align="center" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" link size="default" @click="openCreateForDoctor(row)">
                新增
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="!loading" description="暂无排班数据" />
      </div>
      </div>
    </el-card>

    <!-- 排班详情 + 挂号列表 弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="'排班详情 · ' + (detailSchedule?.doctorName || '')"
      width="760px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="schedule-detail">
        <div class="detail-meta">
          <div class="meta-item">
            <span class="meta-label">日期</span>
            <span class="meta-value">{{ detailSchedule?.scheduleDate }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">时段</span>
            <span class="meta-value">{{ timeSlotLabelMap[detailSchedule?.timeSlot] || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">科室</span>
            <span class="meta-value">{{ detailSchedule?.departmentName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">医生</span>
            <span class="meta-value">{{ detailSchedule?.doctorName || '-' }}</span>
          </div>
          <div class="meta-item">
            <span class="meta-label">号源</span>
            <span class="meta-value">
              <el-tag
                :type="detailSchedule && Number(detailSchedule.currentAppointments) >= Number(detailSchedule.maxAppointments) ? 'danger' : 'success'"
                size="small"
              >
                {{ detailSchedule?.currentAppointments || 0 }} / {{ detailSchedule?.maxAppointments || 0 }}
              </el-tag>
            </span>
          </div>
          <div class="meta-item">
            <span class="meta-label">状态</span>
            <span class="meta-value">{{ statusLabel(detailSchedule?.status) }}</span>
          </div>
        </div>

        <div class="detail-divider"></div>

        <div class="detail-section-title">挂号患者列表</div>
        <el-table
          :data="detailRegistrations"
          border
          stripe
          v-loading="registrationsLoading"
          style="width: 100%"
          :empty-text="registrationsLoading ? '加载中...' : '该排班暂无挂号记录'"
        >
          <el-table-column prop="registrationNo" label="挂号单号" min-width="160" />
          <el-table-column prop="patientName" label="患者名" width="120">
            <template #default="{ row }">
              <el-tag size="small" type="primary" effect="plain">{{ row.patientName || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="queueNo" label="排队号" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.queueNo ? '' : 'info'">{{ row.queueNo || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="source" label="来源" width="100" align="center" />
          <el-table-column prop="feeStatus" label="缴费状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                size="small"
:type="row.feeStatus === '已支付' ? 'success' : row.feeStatus === '待支付' ? 'warning' : 'info'"
              >{{ row.feeStatus || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-tag
                size="small"
:type="row.status === '已完成' ? 'success' : row.status === '已取消' ? 'danger' : row.status === '爽约' ? 'warning' : row.status === '已过期' ? 'warning' : 'info'"
              >{{ row.status || '-' }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="registrationFee" label="挂号费" width="100" align="right">
            <template #default="{ row }">¥{{ row.registrationFee }}</template>
          </el-table-column>
          <el-table-column prop="registeredAt" label="挂号时间" min-width="180">
            <template #default="{ row }">{{ formatDateTime(row.registeredAt) }}</template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="detailDialogVisible = false">关闭</el-button>
        <el-button
          type="primary"
          @click="handleOpenEditFromDetail"
        >编辑排班</el-button>
      </template>
    </el-dialog>

    <!-- 新增/编辑 弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑排班' : '新增排班'"
      width="520px"
      :close-on-click-modal="false"
      @close="resetForm"
    >
      <el-form
        ref="scheduleFormRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="科室" prop="departmentId">
          <el-select
            v-model="formData.departmentId"
            placeholder="请选择科室"
            filterable
            :filter-method="filterDepartment"
            style="width: 100%"
            @change="onDepartmentChange"
            :disabled="isEdit"
          >
            <template v-if="hasMultipleDeptTypes">
              <el-option-group
                v-for="group in filteredDepartmentsByType"
                :key="group.type"
                :label="group.type"
              >
                <el-option
                  v-for="dept in group.items"
                  :key="dept.id"
                  :label="formatDeptLabel(dept)"
                  :value="dept.id"
                >
                  <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                  <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                </el-option>
              </el-option-group>
            </template>
            <template v-else>
              <el-option
                v-for="dept in filteredDepartments"
                :key="dept.id"
                :label="formatDeptLabel(dept)"
                :value="dept.id"
              >
                <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
              </el-option>
            </template>
          </el-select>
        </el-form-item>

        <el-form-item label="医生" prop="doctorId">
          <el-select
            v-model="formData.doctorId"
            placeholder="请选择医生"
            filterable
            style="width: 100%"
            :disabled="isEdit"
          >
            <el-option
              v-for="doc in doctors"
              :key="doc.doctorId"
              :label="formatDoctorLabel(doc)"
              :value="doc.doctorId"
            />
          </el-select>
          <div v-if="formData.doctorId" class="doctor-card">
            <div class="doctor-card-inner">
              <el-avatar :size="56" class="doctor-avatar" style="background: #409eff">
                {{ (selectedDoctor?.doctorName || selectedDoctor?.doctorNo || '医').slice(0, 1) }}
              </el-avatar>
              <div class="doctor-info">
                <div class="doctor-info-row main">
                  <span class="doctor-name">{{ selectedDoctor?.doctorName || '-' }}</span>
                  <el-tag size="small" :type="doctorTypeTag(selectedDoctor?.doctorType)">
                    {{ selectedDoctor?.doctorType || '-' }}
                  </el-tag>
                </div>
                <div class="doctor-info-row">
                  <span class="label">职称：</span>
                  <span>{{ selectedDoctor?.title || '-' }}</span>
                </div>
                <div class="doctor-info-row">
                  <span class="label">专业：</span>
                  <span>{{ selectedDoctor?.specialty || '-' }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-form-item>

        <el-form-item v-if="!isEdit" label="排班日期" prop="scheduleDates">
          <el-date-picker
            v-model="formData.scheduleDates"
            type="dates"
            placeholder="可选择多个日期"
            value-format="YYYY-MM-DD"
            popper-class="schedule-date-popper"
            :cell-class-name="dateCellClass"
            @change="onScheduleDateChange"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item v-else label="排班日期" prop="scheduleDate">
          <el-date-picker
            v-model="formData.scheduleDate"
            type="date"
            disabled
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="!isEdit" label="时段" prop="timeSlots">
          <el-select
            v-model="formData.timeSlots"
            multiple
            placeholder="可选择多个时段"
            popper-class="schedule-time-popper"
            @change="onTimeSlotChange"
            style="width: 100%"
          >
            <el-option label="上午" value="上午" :class="{ 'option-conflict': isSlotAllOccupied('上午') }" />
            <el-option label="下午" value="下午" :class="{ 'option-conflict': isSlotAllOccupied('下午') }" />
            <el-option label="夜间" value="夜间" :class="{ 'option-conflict': isSlotAllOccupied('夜间') }" />
          </el-select>
        </el-form-item>
        <el-form-item v-else label="时段" prop="timeSlot">
          <el-select v-model="formData.timeSlot" placeholder="请选择时段" style="width: 100%">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="夜间" value="夜间" />
          </el-select>
        </el-form-item>

        <el-form-item label="挂号费（元）">
          <el-input-number
            v-model.number="formData.registrationFee"
            :min="0"
            :max="9999"
            :step="5"
            :precision="2"
            controls-position="right"
            style="width: 100%"
          />
          <div v-if="!isEdit && selectedDoctor" style="color: #909399; font-size: 12px; margin-top: 4px;">
            默认挂号费（{{ selectedDoctor.title || '普通' }}）：¥{{ defaultFeeByTitle(selectedDoctor.title) }}
          </div>
        </el-form-item>

        <el-form-item label="最大挂号数" prop="maxAppointments">
          <el-input-number
            v-model.number="formData.maxAppointments"
            :min="1"
            :max="500"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="isEdit" label="已挂号数" prop="currentAppointments">
          <el-input-number
            v-model.number="formData.currentAppointments"
            :min="0"
            :max="999"
            :step="1"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item v-if="isEdit" label="状态" prop="status">
          <el-select v-model="formData.status" placeholder="请选择状态" style="width: 100%" @change="onStatusChange">
            <el-option label="可预约" value="可预约" />
            <el-option label="约满" value="约满" />
            <el-option label="停诊" value="停诊" />
            <el-option label="已过期" value="已过期" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button
          v-if="isEdit"
          type="danger"
          :loading="deleting"
          @click="handleDelete"
        >
          删除
        </el-button>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确认
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 排班参数弹窗 -->
    <el-dialog
      v-model="aiDialogVisible"
      title="AI 排班"
      width="480px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <el-form :model="aiForm" label-width="100px">
        <el-form-item label="科室" required>
          <el-select
            v-model="aiForm.departmentId"
            placeholder="请选择科室"
            filterable
            :filter-method="filterDepartment"
            style="width: 100%"
            @change="onAIDepartmentChange"
          >
            <template v-if="hasMultipleDeptTypes">
              <el-option-group
                v-for="group in filteredDepartmentsByType"
                :key="group.type"
                :label="group.type"
              >
                <el-option
                  v-for="dept in group.items"
                  :key="dept.id"
                  :label="formatDeptLabel(dept)"
                  :value="dept.id"
                >
                  <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                  <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
                </el-option>
              </el-option-group>
            </template>
            <template v-else>
              <el-option
                v-for="dept in filteredDepartments"
                :key="dept.id"
                :label="formatDeptLabel(dept)"
                :value="dept.id"
              >
                <div class="dept-option-main">{{ formatDeptLabel(dept) }}</div>
                <div v-if="formatDeptSubLabel(dept)" class="dept-option-sub">{{ formatDeptSubLabel(dept) }}</div>
              </el-option>
            </template>
          </el-select>
        </el-form-item>
        <el-form-item label="日期范围" required>
          <el-date-picker
            v-model="aiForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item>
          <span style="color: #909399; font-size: 13px">
            AI 将基于科室历史数据，自动推荐每个医生的排班日期、时段和最大挂号数。
          </span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="aiDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="aiSuggestLoading" @click="onGenerateAI">
          {{ aiSuggestLoading ? '正在排班...' : '生成排班' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- AI 排班结果弹窗 -->
    <el-dialog
      v-if="aiSuggestion"
      v-model="_aiResultVisible"
      title="AI 排班结果"
      width="900px"
      :close-on-click-modal="false"
      destroy-on-close
    >
      <div class="ai-summary">
        <span>共生成 <b>{{ (aiSuggestion.details || []).length }}</b> 条排班</span>
        <span v-if="aiSuggestion.status">状态：{{ aiSuggestion.status }}</span>
      </div>
      <el-table :data="aiSuggestion.details || []" border style="width: 100%">
        <el-table-column label="医生" width="140">
          <template #default="{ row }">
            <span>{{ row.doctorName || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scheduleDate" label="日期" width="120" />
        <el-table-column label="时段" width="100">
          <template #default="{ row }">
            <span>{{ timeSlotLabelMap[row.timeSlot] || row.timeSlot }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="maxAppointments" label="最大挂号数" width="110" align="right" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'PENDING'" type="info">待处理</el-tag>
            <el-tag v-else-if="row.status === 'ACCEPTED'" type="success">已采纳</el-tag>
            <el-tag v-else-if="row.status === 'REJECTED'" type="danger">已拒绝</el-tag>
            <el-tag v-else type="info">{{ row.status || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="推荐理由">
          <template #default="{ row }">
            <span style="color: #606266">{{ row.reason || '-' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              :disabled="row.status !== 'PENDING'"
              @click="onAcceptDetail(row)"
            >采纳</el-button>
            <el-button
              size="small"
              type="danger"
              :disabled="row.status !== 'PENDING'"
              @click="onRejectDetail(row)"
            >拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="_aiResultVisible = false">关闭</el-button>
        <el-button type="danger" @click="onRejectAll">全部拒绝</el-button>
        <el-button type="primary" @click="onAcceptAll">全部采纳</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, MagicStick, Refresh } from '@element-plus/icons-vue'
import {
  acceptSuggestion,
  acceptSuggestionDetail,
  batchCreateSchedule,
  cancelSchedule,
  generateScheduleSuggestion,
  listDepartments,
  listDoctors,
  listSchedules,
  listScheduleRegistrations,
  rejectSuggestion,
  rejectSuggestionDetail,
  updateSchedule
} from '@/api/schedule'

const loading = ref(false)
const submitting = ref(false)
const deleting = ref(false)
const departments = ref([])
const deptKeyword = ref('')
const doctors = ref([])
const rawList = ref([])
const titleOptions = ref([])
const doctorTypeOptions = ref([])

// 详情弹窗状态
const detailDialogVisible = ref(false)
const detailSchedule = ref(null)
const detailRegistrations = ref([])
const registrationsLoading = ref(false)

const queryForm = reactive({
  departmentId: null,
  title: null,
  doctorType: null
})
const dateRange = ref(null)
const queryWeekOffset = ref(0) // 默认本周

const dialogVisible = ref(false)
const isEdit = ref(false)
const scheduleFormRef = ref(null)
const formData = reactive({
  id: null,
  doctorId: null,
  departmentId: null,
  scheduleDates: [],
  scheduleDate: '',
  timeSlots: [],
  timeSlot: '',
  maxAppointments: 20,
  currentAppointments: 0,
  registrationFee: 0,
  status: '可预约'
})

const formRules = {
  departmentId: [{ required: true, message: '请选择科室', trigger: 'change' }],
  doctorId: [{ required: true, message: '请选择医生', trigger: 'change' }],
  scheduleDates: [{
    required: true,
    validator: (_rule, value, callback) => {
      if (!value || !value.length) {
        callback(new Error('请选择排班日期'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  timeSlots: [{
    required: true,
    validator: (_rule, value, callback) => {
      if (!value || !value.length) {
        callback(new Error('请选择时段'))
      } else {
        callback()
      }
    },
    trigger: 'change'
  }],
  maxAppointments: [{ required: true, message: '请输入最大挂号数', trigger: 'blur' }]
}

// ========== AI 排班 ==========
const aiDialogVisible = ref(false)
const aiSuggestLoading = ref(false)
const aiForm = reactive({
  departmentId: null,
  dateRange: []
})
const aiSuggestion = ref(null) // { suggestionId, details: [...] }

const editingDoctorInfo = ref(null)

// 选择医生时自动填充默认挂号费
watch(() => formData.doctorId, (newDoctorId) => {
  if (!newDoctorId || isEdit.value) return
  const doc = doctors.value.find((d) => d.doctorId === newDoctorId)
  if (doc) {
    formData.registrationFee = defaultFeeByTitle(doc.title)
  }
})

const selectedDoctor = computed(() => {
  if (!formData.doctorId) return null
  const fromList = doctors.value.find((d) => d.doctorId === formData.doctorId) || null
  if (fromList) return fromList
  if (isEdit.value && editingDoctorInfo.value) return editingDoctorInfo.value
  return null
})

const weekOptions = computed(() => {
  const today = new Date()
  const weekday = today.getDay()
  const mondayOffset = weekday === 0 ? -6 : 1 - weekday
  const options = []
  for (let i = -2; i <= 4; i++) {
    const monday = new Date(today.getFullYear(), today.getMonth(), today.getDate() + mondayOffset + i * 7)
    const sunday = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + 6)
    let prefix = '本周'
    if (i < 0) prefix = '前' + Math.abs(i) + '周'
    if (i > 0) prefix = '后' + i + '周'
    const label = prefix + '（' + formatShort(monday) + ' ~ ' + formatShort(sunday) + '）'
    options.push({ label, offset: i, monday: formatDate(monday), sunday: formatDate(sunday) })
  }
  return options
})

const effectiveRange = computed(() => {
  if (dateRange.value && dateRange.value.length === 2) {
    return [dateRange.value[0], dateRange.value[1]]
  }
  const opt = weekOptions.value.find((o) => o.offset === queryWeekOffset.value)
  if (opt) return [opt.monday, opt.sunday]
  const today = new Date()
  const weekday = today.getDay()
  const mondayOffset = weekday === 0 ? -6 : 1 - weekday
  const monday = new Date(today.getFullYear(), today.getMonth(), today.getDate() + mondayOffset)
  const sunday = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + 6)
  return [formatDate(monday), formatDate(sunday)]
})

const weekDates = computed(() => {
  const [start, end] = effectiveRange.value
  const dates = []
  const sd = parseDate(start)
  const ed = parseDate(end)
  for (let d = new Date(sd); d <= ed; d.setDate(d.getDate() + 1)) {
    dates.push({
      dateStr: formatDate(d),
      weekday: d.getDay() === 0 ? 7 : d.getDay(),
      label: `${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}（周${weekdayText(d.getDay())}）`,
      isWeekend: d.getDay() === 0 || d.getDay() === 6
    })
  }
  return dates
})

const weekGroups = computed(() => {
  const groups = []
  let currentGroup = null
  for (const d of weekDates.value) {
    const mondayOfWeek = getMondayOfDate(parseDate(d.dateStr))
    const sundayOfWeek = new Date(mondayOfWeek.getFullYear(), mondayOfWeek.getMonth(), mondayOfWeek.getDate() + 6)
    const key = formatDate(mondayOfWeek) + '_' + formatDate(sundayOfWeek)
    const label = formatShort(mondayOfWeek) + ' ~ ' + formatShort(sundayOfWeek)
    if (!currentGroup || currentGroup.key !== key) {
      currentGroup = { key, label, dates: [] }
      groups.push(currentGroup)
    }
    currentGroup.dates.push(d)
  }
  return groups
})

const matrixRows = computed(() => {
  const rowsMap = new Map()
  const schedulesByDoctor = new Map()

  // 前端筛选：按职称、医生类型
  let filtered = rawList.value
  if (queryForm.title) {
    filtered = filtered.filter((item) => item.title === queryForm.title)
  }
  if (queryForm.doctorType) {
    filtered = filtered.filter((item) => item.doctorType === queryForm.doctorType)
  }

  for (const item of filtered) {
    const key = String(item.doctorId)
    if (!rowsMap.has(key)) {
      rowsMap.set(key, {
        doctorId: item.doctorId,
        doctorName: item.doctorName,
        doctorType: item.doctorType,
        title: item.title,
        specialty: item.specialty,
        departmentId: item.departmentId,
        departmentName: item.departmentName,
        schedules: {}
      })
    }
    if (!schedulesByDoctor.has(key)) schedulesByDoctor.set(key, {})
    const dateMap = schedulesByDoctor.get(key)
    const dateKey = item.scheduleDate
    if (!dateMap[dateKey]) dateMap[dateKey] = []
    dateMap[dateKey].push(item)
  }

  const rows = []
  for (const [key, row] of rowsMap.entries()) {
    const scheduleMap = schedulesByDoctor.get(key) || {}
    const mergedRow = { ...row, schedules: scheduleMap }
    rows.push(mergedRow)
  }
  return rows
})

function weekdayText(day) {
  const map = ['日', '一', '二', '三', '四', '五', '六']
  return map[day] || ''
}

function getMondayOfDate(d) {
  const day = d.getDay()
  const diff = d.getDate() - day + (day === 0 ? -6 : 1)
  return new Date(d.setDate(diff))
}

function parseDate(str) {
  const parts = str.split('-')
  return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]))
}

function formatDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return y + '-' + m + '-' + day
}

function formatShort(d) {
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return m + '-' + day
}

const doctorTypeTag = (type) => {
  if (!type) return 'info'
  if (type.includes('门诊')) return 'success'
  if (type.includes('检验') || type.includes('检查')) return 'warning'
  if (type.includes('药房')) return 'danger'
  if (type.includes('挂号')) return ''
  return 'info'
}

const doctorTypeChipClass = (type) => {
  if (!type) return 'doctor-type-other'
  if (type.includes('门诊')) return 'doctor-type-chief'
  if (type.includes('检验') || type.includes('检查')) return 'doctor-type-other'
  if (type.includes('药房')) return 'doctor-type-other'
  if (type.includes('挂号')) return 'doctor-type-other'
  return 'doctor-type-other'
}

const titleChipClass = (title) => {
  if (!title) return ''
  if (title.includes('主任')) return 'title-chip-chief'
  if (title.includes('主治')) return 'title-chip-senior'
  return 'title-chip-other'
}

const displayTimeSlot = (slot) => {
  // 时段值已是中文，直接返回
  return slot || '-'
}

// UI 提示用：后端为费用权威来源，此处仅用于新增排班时的输入框预填
// 必须与后端 ScheduleTimeSlotUtils.defaultFeeByTitle() 保持一致
const defaultFeeByTitle = (title) => {
  if (!title) return 15
  if (title.includes('主任医师')) return 50
  if (title.includes('副主任医师')) return 25
  if (title.includes('主治医师')) return 15
  if (title.includes('住院医师')) return 10
  return 15
}

const formatDoctorLabel = (doc) => {
  const parts = []
  if (doc.doctorNo) parts.push('[' + doc.doctorNo + ']')
  parts.push(doc.doctorName || '')
  parts.push(doc.title || '')
  return parts.join(' ').trim()
}

// -------- 科室下拉展示工具 --------

// 主标签：[code] 科室名称
const formatDeptLabel = (dept) => {
  if (!dept) return ''
  const parts = []
  if (dept.code) parts.push('[' + dept.code + ']')
  parts.push(dept.name || '')
  return parts.join(' ').trim()
}

// 副信息：楼层 / 类型
const formatDeptSubLabel = (dept) => {
  if (!dept) return ''
  const parts = []
  if (dept.departmentType) parts.push(dept.departmentType)
  if (dept.floor) parts.push(dept.floor)
  return parts.join(' · ')
}

// 将科室按 departmentType 分组（扁平 list → { type: [dept1, dept2] }）
const departmentsByType = computed(() => {
  const list = departments.value || []
  if (!list.length) return []

  // 先按 departmentType 分组
  const groups = new Map()
  for (const d of list) {
    const type = d.departmentType || '其他'
    if (!groups.has(type)) groups.set(type, [])
    groups.get(type).push(d)
  }

  // 转为数组（类型名按字典序，便于展示时稳定）
  const types = Array.from(groups.keys()).sort()
  return types.map((type) => ({
    type,
    items: groups.get(type)
  }))
})

// 是否有多个科室类型（决定是否用分组展示）
const hasMultipleDeptTypes = computed(() => {
  return departmentsByType.value.length > 1
})

// 科室模糊搜索：匹配 code、name、departmentType、floor
const matchesDeptKeyword = (dept) => {
  const kw = (deptKeyword.value || '').trim().toLowerCase()
  if (!kw) return true
  const haystack = [
    dept.name || '',
    dept.code || '',
    dept.departmentType || '',
    dept.floor || ''
  ]
    .join(' ')
    .toLowerCase()
  return haystack.includes(kw)
}

// 过滤后的科室列表（扁平化）
const filteredDepartments = computed(() => {
  const list = (departments.value || []).filter(matchesDeptKeyword)
  return list
})

// 过滤后的科室分组
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
  return types.map((type) => ({
    type,
    items: groups.get(type)
  }))
})

// el-select 过滤回调：更新关键字（返回值决定是否隐藏选项）
const filterDepartment = (keyword) => {
  deptKeyword.value = keyword || ''
}

function slotClass(row, dateStr, slot) {
  const item = getScheduleItem(row, dateStr, slot)
  if (!item) return ''
  if (item.status === '停诊') return 'slot-cancelled'
  if (item.status === '已过期') return 'slot-expired'
  const total = Number(item.maxAppointments) || 0
  const cur = Number(item.currentAppointments) || 0
  if (cur >= total) return 'slot-full'
  return 'slot-available'
}

function getScheduleItem(row, dateStr, slot) {
  const list = (row.schedules || {})[dateStr] || []
  return list.find((s) => s.timeSlot === slot) || null
}

function slotInfo(row, dateStr, slot) {
  const item = getScheduleItem(row, dateStr, slot)
  if (!item) return ''
  if (item.status === '停诊') return '停诊'
  const total = Number(item.maxAppointments) || 0
  const cur = Number(item.currentAppointments) || 0
  return cur + '/' + total
}

const timeSlotLabelMap = { '上午': '上午', '下午': '下午', '夜间': '夜间' } // AI 结果弹窗使用

function padZero(n) {
  return n < 10 ? '0' + n : '' + n
}

function formatDateKey(date) {
  return date.getFullYear() + '-' + padZero(date.getMonth() + 1) + '-' + padZero(date.getDate())
}

function isSlotOccupied(dateStr, slot) {
  if (!formData.doctorId) return false
  return rawList.value.some(
    (item) =>
      item.doctorId === formData.doctorId &&
      item.scheduleDate === dateStr &&
      item.timeSlot === slot &&
      item.status !== '停诊'
  )
}

// 该日期早中晚全已排班 → 完全不可选
function isDateConflict(dateStr) {
  return ['上午', '下午', '夜间'].every((slot) =>
    isSlotOccupied(dateStr, slot)
  )
}

// 该时段在所有已选日期都已占用 → 完全不可选
// 若还没选日期，则只要医生在某一天存在该时段排班，就提示有冲突
function isSlotAllOccupied(slot) {
  const dates = formData.scheduleDates || []
  if (dates.length === 0) {
    return rawList.value.some(
      (item) =>
        item.doctorId === formData.doctorId &&
        item.timeSlot === slot &&
        item.status !== '停诊'
    )
  }
  return dates.every((date) => isSlotOccupied(date, slot))
}

function dateCellClass(date) {
  if (!formData.doctorId) return ''
  const dateStr = formatDateKey(date)
  if (isDateConflict(dateStr)) return 'cell-conflict-full'
  return ''
}

function onScheduleDateChange(newVal) {
  if (!newVal || !Array.isArray(newVal)) return
  if (!formData.doctorId) return
  const conflictDates = newVal.filter((date) => isDateConflict(date))
  const keepDates = newVal.filter((date) => !isDateConflict(date))
  if (conflictDates.length > 0) {
    formData.scheduleDates = keepDates
    ElMessage.warning(
      '以下日期早中晚均已排班，无法添加：\n  • ' + conflictDates.join('\n  • ')
    )
  }
}

function onTimeSlotChange(newVal) {
  if (!newVal || !Array.isArray(newVal)) return
  if (!formData.doctorId) return
  const dates = formData.scheduleDates || []
  const conflictSlots = []
  const conflictDetails = []
  const keepSlots = []
  for (const slot of newVal) {
    if (isSlotAllOccupied(slot)) {
      conflictSlots.push(slot)
      const occupiedDates = dates.filter((date) => isSlotOccupied(date, slot))
      if (occupiedDates.length > 0) {
        conflictDetails.push(`  • ${timeSlotLabelMap[slot]}（${occupiedDates.join('、')}）`)
      } else {
        conflictDetails.push(`  • ${timeSlotLabelMap[slot]}（已有排班）`)
      }
    } else {
      keepSlots.push(slot)
    }
  }
  if (conflictSlots.length > 0) {
    formData.timeSlots = keepSlots
    ElMessage.warning(
      '以下时段在所选日期已有排班，无法添加：\n' + conflictDetails.join('\n')
    )
  }
}

function openCreateForDoctor(row) {
  resetForm()
  isEdit.value = false
  formData.departmentId = row.departmentId
  loadDoctors(row.departmentId)
  formData.doctorId = row.doctorId
  formData.registrationFee = defaultFeeByTitle(row.title)
  dialogVisible.value = true
}

function openCreateForSlot(row, dateStr, slot) {
  resetForm()
  isEdit.value = false
  formData.departmentId = row.departmentId
  loadDoctors(row.departmentId)
  formData.doctorId = row.doctorId
  formData.registrationFee = defaultFeeByTitle(row.title)
  formData.scheduleDates = [dateStr]
  formData.timeSlots = [slot]
  dialogVisible.value = true
}

async function loadDepartments() {
  try {
    departments.value = await listDepartments()
  } catch (e) {}
}

function extractOptions(list) {
  // 从医生列表提取去重的 title 和 doctorType，作为筛选下拉选项
  const titleSet = new Set()
  const typeSet = new Set()
  for (const d of list || []) {
    if (d.title) titleSet.add(d.title)
    if (d.doctorType) typeSet.add(d.doctorType)
  }
  titleOptions.value = Array.from(titleSet)
  doctorTypeOptions.value = Array.from(typeSet)
}

async function loadDoctors(departmentId) {
  if (!departmentId) {
    doctors.value = []
    return
  }
  try {
    doctors.value = await listDoctors(departmentId)
  } catch (e) {
    doctors.value = []
  }
}

// 加载所有医生（用于初始化时提取完整的职称/类型选项）
async function loadAllDoctorsForOptions() {
  try {
    const list = await listDoctors(null)
    extractOptions(list)
  } catch (e) {}
}

async function loadList() {
  loading.value = true
  try {
    const [start, end] = effectiveRange.value
    const params = { startDate: start, endDate: end }
    if (queryForm.departmentId) params.departmentId = queryForm.departmentId
    rawList.value = await listSchedules(params)
  } catch (e) {
    rawList.value = []
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  queryForm.departmentId = null
  queryForm.title = null
  queryForm.doctorType = null
  dateRange.value = null
  queryWeekOffset.value = 0
  loadList()
}

function resetForm() {
  formData.id = null
  formData.doctorId = null
  formData.departmentId = null
  formData.scheduleDates = []
  formData.scheduleDate = ''
  formData.timeSlots = []
  formData.timeSlot = ''
  formData.maxAppointments = 20
  formData.currentAppointments = 0
  formData.registrationFee = 0
  formData.status = '可预约'
  editingDoctorInfo.value = null
  if (scheduleFormRef.value) scheduleFormRef.value.clearValidate()
}

function onDepartmentChange(val) {
  if (!isEdit.value) {
    formData.doctorId = null
    loadDoctors(val)
  }
}

function openCreateDialog() {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

function formatDateTime(val) {
  if (!val) return '-'
  const str = String(val).replace('T', ' ')
  return str.slice(0, 19)
}

function statusLabel(status) {
  const map = { '可预约': '可预约', '约满': '约满', '停诊': '停诊', '已过期': '已过期' }
  return map[status] || (status || '-')
}

async function openDetailDialog(item) {
  if (!item) return
  detailSchedule.value = item
  detailRegistrations.value = []
  detailDialogVisible.value = true
  registrationsLoading.value = true
  try {
    detailRegistrations.value = await listScheduleRegistrations(item.id)
  } catch (e) {
    detailRegistrations.value = []
  } finally {
    registrationsLoading.value = false
  }
}

function handleOpenEditFromDetail() {
  if (!detailSchedule.value) return
  const item = detailSchedule.value
  detailDialogVisible.value = false
  openEditDialog(item)
}

function openEditDialog(item) {
  resetForm()
  isEdit.value = true
  editingDoctorInfo.value = {
    doctorId: item.doctorId,
    doctorName: item.doctorName,
    doctorNo: item.doctorNo,
    doctorType: item.doctorType,
    title: item.title,
    specialty: item.specialty
  }
  formData.id = item.id
  formData.doctorId = item.doctorId
  formData.departmentId = item.departmentId
  formData.scheduleDate = item.scheduleDate
  formData.timeSlot = item.timeSlot
  formData.maxAppointments = item.maxAppointments
  formData.currentAppointments = Number(item.currentAppointments) || 0
  formData.registrationFee = item.registrationFee || defaultFeeByTitle(item.title)
  formData.status = item.status || '可预约'
  loadDoctors(item.departmentId)
  dialogVisible.value = true
}

function onStatusChange(val) {
  if (val === '约满') {
    formData.currentAppointments = formData.maxAppointments
  }
}

async function handleDelete() {
  if (!formData.id) return
  try {
    await ElMessageBox.confirm('确定要删除该排班吗？', '提示', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  deleting.value = true
  try {
    await cancelSchedule(formData.id)
    ElMessage.success('删除成功')
    dialogVisible.value = false
    loadList()
  } finally {
    deleting.value = false
  }
}

async function handleSubmit() {
  if (!scheduleFormRef.value) return
  try {
    await scheduleFormRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    if (isEdit.value) {
      await updateSchedule(formData.id, {
        timeSlot: formData.timeSlot,
        maxAppointments: formData.maxAppointments,
        currentAppointments: formData.currentAppointments,
        registrationFee: formData.registrationFee,
        status: formData.status
      })
      ElMessage.success('编辑成功')
    } else {
      const dates = formData.scheduleDates || []
      const slots = formData.timeSlots || []
      const list = []
      for (const date of dates) {
        for (const slot of slots) {
          list.push({
            doctorId: formData.doctorId,
            departmentId: formData.departmentId,
            scheduleDate: date,
            timeSlot: slot,
            maxAppointments: formData.maxAppointments
          })
        }
      }
      await batchCreateSchedule({ schedules: list })
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // ignore
  } finally {
    submitting.value = false
  }
}

watch(
  () => [queryForm.departmentId, dateRange.value, queryWeekOffset.value],
  () => {
    loadList()
  }
)

onMounted(async () => {
  loadDepartments()
  await loadAllDoctorsForOptions() // 先加载所有医生以提取职称/类型选项
  loadList()
})

// ========== AI 排班方法 ==========
const _aiResultVisible = ref(false)

function openAIDialog() {
  aiForm.departmentId = queryForm.departmentId || null
  aiForm.dateRange = (dateRange.value && dateRange.value.length === 2)
    ? [...dateRange.value]
    : []
  aiSuggestion.value = null
  aiDialogVisible.value = true
}

function onAIDepartmentChange(_deptId) {
  // 科室变更时不做特殊处理，保留用户已选日期范围
}

async function onGenerateAI() {
  if (!aiForm.departmentId) {
    ElMessage.warning('请选择科室')
    return
  }
  if (!aiForm.dateRange || aiForm.dateRange.length !== 2) {
    ElMessage.warning('请选择日期范围')
    return
  }
  aiSuggestLoading.value = true
  try {
    const data = await generateScheduleSuggestion({
      departmentId: aiForm.departmentId,
      startDate: aiForm.dateRange[0],
      endDate: aiForm.dateRange[1]
    })
    aiSuggestion.value = {
      suggestionId: data.suggestionId,
      departmentId: data.departmentId,
      status: data.status,
      details: (data.details || []).map((d) => ({
        ...d,
        status: d.status || 'PENDING'
      }))
    }
    aiDialogVisible.value = false
    _aiResultVisible.value = true
    if (!aiSuggestion.value.details.length) {
      ElMessage.info('AI 暂未生成任何排班')
    }
  } catch (e) {
    // 错误由 request 拦截器提示
  } finally {
    aiSuggestLoading.value = false
  }
}

async function onAcceptAll() {
  if (!aiSuggestion.value) return
  try {
    aiSuggestLoading.value = true
    await acceptSuggestion(aiSuggestion.value.suggestionId)
    aiSuggestion.value.details.forEach((d) => {
      if (d.status !== 'REJECTED') d.status = 'ACCEPTED'
    })
    ElMessage.success('已采纳所有待处理排班')
    loadList()
  } finally {
    aiSuggestLoading.value = false
  }
}

async function onRejectAll() {
  if (!aiSuggestion.value) return
  try {
    aiSuggestLoading.value = true
    await rejectSuggestion(aiSuggestion.value.suggestionId)
    aiSuggestion.value.details.forEach((d) => {
      if (d.status !== 'ACCEPTED') d.status = 'REJECTED'
    })
    ElMessage.success('已拒绝所有待处理排班')
  } finally {
    aiSuggestLoading.value = false
  }
}

async function onAcceptDetail(row) {
  if (!aiSuggestion.value) return
  try {
    await acceptSuggestionDetail(aiSuggestion.value.suggestionId, row.detailId)
    row.status = 'ACCEPTED'
    ElMessage.success('已采纳该排班')
    loadList()
  } catch (e) {
    // 错误由 request 拦截器提示
  }
}

async function onRejectDetail(row) {
  if (!aiSuggestion.value) return
  try {
    await rejectSuggestionDetail(aiSuggestion.value.suggestionId, row.detailId)
    row.status = 'REJECTED'
    ElMessage.success('已拒绝该排班')
  } catch (e) {
    // 错误由 request 拦截器提示
  }
}
</script>

<style scoped>
/* 科室下拉选项的样式 */
.dept-option-main {
  font-size: 14px;
  color: #303133;
  line-height: 1.4;
}
.dept-option-sub {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
  margin-top: 2px;
}
.dept-option-sub:empty {
  display: none;
}

/* 关键修复：让 el-option 高度随内容自适应（原本固定 34px，双行会被截断产生"黑点"） */
:deep(.el-select-dropdown__item) {
  height: auto;
  min-height: 34px;
  line-height: 1.4;
  padding-top: 6px;
  padding-bottom: 6px;
  border-bottom: none;
}
:deep(.el-select-dropdown__item.hover),
:deep(.el-select-dropdown__item:hover) {
  background-color: #f5f7fa;
}

/* option-group 的分组标签也要加一点垂直间距，避免和上方内容挤在一起 */
:deep(.el-select-dropdown__group) {
  padding-top: 6px;
  padding-bottom: 4px;
}

.schedule-page {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 12px;
  box-sizing: border-box;
  min-height: 0;
}

.schedule-card {
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  margin: 0;
  overflow: hidden;
}

.schedule-card :deep(.el-card__body) {
  padding: 16px 20px;
  flex: 1 1 auto;
  display: flex;
  flex-direction: column;
  min-height: 0;
  overflow: hidden;
}

.schedule-card :deep(.el-card__header) {
  flex: 0 0 auto;
}

.schedule-page .page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #1f2d3d;
}

.schedule-body {
  display: flex;
  flex-direction: column;
  flex: 1 1 auto;
  min-height: 0;
  overflow: hidden;
}

.query-bar {
  flex: 0 0 auto;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 12px;
  position: sticky;
  top: 0;
  z-index: 5;
  background: #ffffff;
  white-space: nowrap;
}

.query-bar :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 8px;
}

.matrix-wrap {
  flex: 1 1 auto;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.doctor-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 0;
}

.doctor-cell-info {
  flex: 1;
  min-width: 0;
}

.doctor-cell-name {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
  white-space: nowrap;
}

.doctor-type-chip {
  display: inline-block;
  padding: 1px 6px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.5;
  border-radius: 4px;
  border: 1px solid transparent;
}

.doctor-type-chip.doctor-type-chief {
  background: #e6a23c;
  color: #ffffff;
  border-color: #cf8c2f;
}

.doctor-type-chip.doctor-type-other {
  background: #409eff;
  color: #ffffff;
  border-color: #2d81dd;
}

.doctor-cell-sub {
  display: flex;
  align-items: center;
  flex-wrap: nowrap;
  gap: 4px;
  font-size: 12px;
  color: #606266;
  margin-bottom: 2px;
}

.title-chip {
  display: inline-block;
  padding: 1px 6px;
  font-size: 11px;
  font-weight: 500;
  line-height: 1.5;
  border-radius: 4px;
  border: 1px solid transparent;
}

.title-chip.title-chip-chief {
  background: #f56c6c;
  color: #ffffff;
  border-color: #e85858;
}

.title-chip.title-chip-senior {
  background: #e6a23c;
  color: #ffffff;
  border-color: #cf8c2f;
}

.title-chip.title-chip-other {
  background: #909399;
  color: #ffffff;
  border-color: #7a7d82;
}

.doctor-cell-dept {
  font-size: 12px;
  color: #909399;
}

.slot-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 4px 0;
}

.slot-item {
  font-size: 13px;
  padding: 5px 8px;
  border-radius: 3px;
  line-height: 1.5;
  text-align: center;
  cursor: pointer;
  transition: opacity 0.2s;
}

.slot-item:hover {
  opacity: 0.85;
}

.slot-item .slot-label {
  font-weight: 600;
  margin-right: 6px;
  font-size: 13px;
}

.slot-item .slot-info {
  font-size: 12px;
  opacity: 0.9;
}

.slot-item.slot-empty {
  color: #909399;
  background: transparent;
  border: 1px dashed #dcdfe6;
  cursor: pointer;
}

.slot-item.slot-empty:hover {
  color: #409eff;
  border-color: #409eff;
  background: #ecf5ff;
  opacity: 1;
}

.slot-item.slot-available {
  color: #67c23a;
  background: #f0f9eb;
}

.slot-item.slot-full {
  color: #e6a23c;
  background: #fdf6ec;
}

.slot-item.slot-cancelled {
  color: #f56c6c;
  background: #fef0f0;
}

.slot-item.slot-expired {
  color: #909399;
  background: #f5f7fa;
  opacity: 0.6;
}

.slot-empty {
  color: #c0c4cc;
  font-size: 14px;
  text-align: center;
}

.doctor-card {
  margin-top: 8px;
  width: 100%;
  box-sizing: border-box;
}

.doctor-card-inner {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  padding: 14px 16px;
  background: #f5f7fa;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  box-sizing: border-box;
}

.doctor-avatar {
  flex-shrink: 0;
}

.doctor-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.doctor-info-row {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.doctor-info-row.main {
  font-size: 15px;
  color: #1f2d3d;
  font-weight: 600;
  margin-bottom: 2px;
}

.doctor-info-row .label {
  color: #909399;
  white-space: nowrap;
}

.doctor-name {
  font-size: 15px;
}

.ai-summary {
  margin-bottom: 12px;
  display: flex;
  gap: 24px;
  color: #606266;
  font-size: 14px;
}
.ai-summary b {
  color: #409eff;
  margin: 0 2px;
}

/* 排班详情弹窗 */
.schedule-detail .detail-meta {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px 18px;
}
.schedule-detail .meta-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  background: #f5f7fa;
  border-radius: 6px;
}
.schedule-detail .meta-label {
  font-size: 12px;
  color: #909399;
}
.schedule-detail .meta-value {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}
.schedule-detail .detail-divider {
  margin: 16px 0;
  border-top: 1px dashed #e4e7ed;
}
.schedule-detail .detail-section-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}
</style>

<style>
/* 时段下拉冲突项（全局样式，命中 teleport 到 body 的 el-select-dropdown） */
.schedule-time-popper .el-select-dropdown__item.option-conflict,
.schedule-time-popper .el-select-dropdown__item.option-conflict:hover {
  color: #c0c4cc !important;
  background-color: #f5f7fa !important;
  cursor: not-allowed !important;
  opacity: 0.7;
}

/* 日期面板冲突单元格（全局样式，命中 teleport 到 body 的 el-date-picker 面板） */
.schedule-date-popper .el-date-table td.cell-conflict-full,
.schedule-date-popper td.cell-conflict-full,
.el-picker-panel td.cell-conflict-full {
  color: #909399 !important;
  cursor: not-allowed !important;
  text-decoration: line-through !important;
  background-color: #f5f7fa !important;
  opacity: 0.7 !important;
}

.schedule-date-popper td.cell-conflict-full span,
.schedule-date-popper .el-date-table td.cell-conflict-full .cell,
.el-picker-panel td.cell-conflict-full span {
  color: #909399 !important;
  text-decoration: line-through !important;
  background-color: transparent !important;
}
</style>