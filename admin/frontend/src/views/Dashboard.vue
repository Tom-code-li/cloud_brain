<template>
  <div class="dashboard">
    <!-- 欢迎卡片 -->
    <div class="welcome-card">
      <div class="welcome-left">
        <div class="welcome-icon">
          <el-icon><Sunny /></el-icon>
        </div>
        <div>
          <div class="welcome-title">
            {{ greeting }}，{{ userStore.realName || userStore.username }}
          </div>
          <div class="welcome-sub">
            {{ roleLabel }} · {{ todayStr }} · {{ weekday }}
          </div>
        </div>
      </div>
      <div class="welcome-right">
        <div class="welcome-stat">
          <div class="welcome-stat-num">{{ stats.doctorCount || 0 }}</div>
          <div class="welcome-stat-label">医生总数</div>
        </div>
        <div class="welcome-sep"></div>
        <div class="welcome-stat">
          <div class="welcome-stat-num">{{ stats.departmentCount || 0 }}</div>
          <div class="welcome-stat-label">科室总数</div>
        </div>
        <div class="welcome-sep"></div>
        <div class="welcome-stat">
          <div class="welcome-stat-num">{{ stats.todayScheduleCount || 0 }}</div>
          <div class="welcome-stat-label">今日排班</div>
        </div>
        <div class="welcome-sep"></div>
        <div class="welcome-stat">
          <div class="welcome-stat-num">{{ stats.monthScheduleCount || 0 }}</div>
          <div class="welcome-stat-label">本月排班</div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="section-title">
      <el-icon><Menu /></el-icon>
      <span>快捷入口</span>
    </div>
    <div class="quick-grid">
      <div class="quick-card quick-blue" @click="go('/department')">
        <div class="quick-icon">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="quick-text">
          <div class="quick-title">科室管理</div>
          <div class="quick-desc">管理科室层级、类型与信息</div>
        </div>
        <el-icon class="quick-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="quick-card quick-green" @click="go('/doctor')">
        <div class="quick-icon">
          <el-icon><UserFilled /></el-icon>
        </div>
        <div class="quick-text">
          <div class="quick-title">医生管理</div>
          <div class="quick-desc">医生信息、职称、排班设置</div>
        </div>
        <el-icon class="quick-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="quick-card quick-orange" @click="go('/schedule')">
        <div class="quick-icon">
          <el-icon><Calendar /></el-icon>
        </div>
        <div class="quick-text">
          <div class="quick-title">排班管理</div>
          <div class="quick-desc">排班安排、调班与查看</div>
        </div>
        <el-icon class="quick-arrow"><ArrowRight /></el-icon>
      </div>
      <div class="quick-card quick-purple" @click="go('/role')">
        <div class="quick-icon">
          <el-icon><Lock /></el-icon>
        </div>
        <div class="quick-text">
          <div class="quick-title">角色管理</div>
          <div class="quick-desc">系统角色与权限管理</div>
        </div>
        <el-icon class="quick-arrow"><ArrowRight /></el-icon>
      </div>
    </div>

    <el-row :gutter="16" class="row">
      <!-- 今日排班 -->
      <el-col :span="16">
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Calendar /></el-icon>
              <span>今日排班</span>
              <el-tag size="small" type="info" effect="plain">{{ todayStr }}</el-tag>
            </div>
            <el-button size="small" type="primary" link @click="go('/schedule')">
              查看全部 <el-icon><ArrowRight /></el-icon>
            </el-button>
          </div>
          <div v-if="todayList.length === 0" class="empty-tip">
            <el-empty description="今日暂无排班" />
          </div>
          <div v-else class="schedule-list">
            <div v-for="(item, idx) in todayList" :key="idx" class="schedule-item">
              <div class="schedule-shift" :class="getShiftClass(item.timeSlot)">
                {{ item.timeSlot || '-' }}
              </div>
              <div class="schedule-info">
                <div class="schedule-name">{{ item.doctorName || '-' }}</div>
                <div class="schedule-sub">
                  <el-tag size="small" effect="plain">{{ item.departmentName || '-' }}</el-tag>
                  <el-tag size="small" effect="plain">{{ item.title || '-' }}</el-tag>
                </div>
              </div>
              <div class="schedule-time">
                {{ Number(item.currentAppointments) || 0 }} / {{ Number(item.maxAppointments) || 0 }} 号
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧信息 -->
      <el-col :span="8">
        <div class="panel-card">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><DataBoard /></el-icon>
              <span>系统概览</span>
            </div>
          </div>
          <div class="overview-list">
            <div class="overview-item">
              <el-icon class="overview-icon blue"><User /></el-icon>
              <div class="overview-info">
                <div class="overview-label">在线用户</div>
                <div class="overview-value">1</div>
              </div>
            </div>
            <div class="overview-item">
              <el-icon class="overview-icon green"><Check /></el-icon>
              <div class="overview-info">
                <div class="overview-label">在岗医生</div>
                <div class="overview-value">{{ stats.todayDoctorOnDuty || 0 }}</div>
              </div>
            </div>
            <div class="overview-item">
              <el-icon class="overview-icon orange"><Tickets /></el-icon>
              <div class="overview-info">
                <div class="overview-label">科室数量</div>
                <div class="overview-value">{{ stats.departmentCount || 0 }}</div>
              </div>
            </div>
            <div class="overview-item">
              <el-icon class="overview-icon purple"><Finished /></el-icon>
              <div class="overview-info">
                <div class="overview-label">本月排班</div>
                <div class="overview-value">{{ stats.monthScheduleCount || 0 }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel-card" style="margin-top:16px">
          <div class="panel-header">
            <div class="panel-title">
              <el-icon><Bell /></el-icon>
              <span>温馨提示</span>
            </div>
          </div>
          <div class="tip-list">
            <div class="tip-item">
              <el-icon class="tip-icon blue"><InfoFilled /></el-icon>
              <span>请及时处理本周排班调整，避免影响正常运营</span>
            </div>
            <div class="tip-item">
              <el-icon class="tip-icon orange"><WarningFilled /></el-icon>
              <span>医生信息变更后，请同步更新其排班数据</span>
            </div>
            <div class="tip-item">
              <el-icon class="tip-icon green"><CircleCheck /></el-icon>
              <span>系统数据每日自动同步，请保持信息准确</span>
            </div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 科室分布 -->
    <div class="section-title" style="margin-top:0">
      <el-icon><OfficeBuilding /></el-icon>
      <span>科室分布</span>
    </div>
    <div class="dept-grid">
      <div v-for="(d, idx) in departmentList.slice(0, 8)" :key="idx" class="dept-card">
        <div class="dept-avatar" :style="{ background: deptColors[idx % deptColors.length] }">
          <el-icon><OfficeBuilding /></el-icon>
        </div>
        <div class="dept-info">
          <div class="dept-name">{{ d.name }}</div>
          <div class="dept-type">{{ d.type || '科室' }}</div>
        </div>
      </div>
      <div v-if="departmentList.length === 0" class="empty-tip" style="grid-column:1/-1">
        <el-empty description="暂无科室数据" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  Calendar,
  UserFilled,
  OfficeBuilding,
  DataAnalysis,
  ArrowRight,
  Menu,
  Sunny,
  Lock,
  DataBoard,
  User,
  Check,
  Tickets,
  Finished,
  Bell,
  InfoFilled,
  WarningFilled,
  CircleCheck
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { listDoctors } from '@/api/doctor'
import { listDepartments } from '@/api/department'
import { listSchedules } from '@/api/schedule'

const router = useRouter()
const userStore = useUserStore()

const stats = reactive({
  doctorCount: 0,
  departmentCount: 0,
  todayScheduleCount: 0,
  todayDoctorOnDuty: 0,
  monthScheduleCount: 0
})

const todayList = ref([])
const departmentList = ref([])

const deptColors = [
  '#409eff', '#67c23a', '#e6a23c', '#f56c6c',
  '#909399', '#722ed1', '#13c2c2', '#faad14'
]

const roleLabel = computed(() => userStore.roleName || '系统管理员')

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 11) return '早上好'
  if (h < 13) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const todayStr = computed(() => {
  const d = new Date()
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
})

const weekday = computed(() => {
  const days = ['周日', '周一', '周二', '周三', '周四', '周五', '周六']
  return days[new Date().getDay()]
})

function fmtDate(d) {
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

function extractArray(res) {
  if (res === null || res === undefined) return []
  if (Array.isArray(res)) return res
  if (Array.isArray(res.list)) return res.list
  if (Array.isArray(res.data)) return res.data
  if (res.data && Array.isArray(res.data.list)) return res.data.list
  if (Array.isArray(res.rows)) return res.rows
  if (res.data && Array.isArray(res.data.rows)) return res.data.rows
  return []
}

function extractNumber(res, fallbackKey) {
  if (res === null || res === undefined) return 0
  if (typeof res === 'number') return res
  if (Array.isArray(res)) return res.length
  if (typeof res === 'object') {
    if (typeof res.total === 'number') return res.total
    if (typeof res[fallbackKey] === 'number') return res[fallbackKey]
    if (Array.isArray(res.list)) return res.list.length
    if (Array.isArray(res.rows)) return res.rows.length
    if (Array.isArray(res.data?.list)) return res.data.list.length
    if (Array.isArray(res.data?.rows)) return res.data.rows.length
  }
  return 0
}

function getShiftClass(slot) {
  if (!slot) return 'shift-day'
  const s = String(slot)
  if (s.includes('上午') || s.includes('早')) return 'shift-morning'
  if (s.includes('下午') || s.includes('中')) return 'shift-afternoon'
  if (s.includes('夜间') || s.includes('晚') || s.includes('夜')) return 'shift-night'
  return 'shift-day'
}

async function loadStats() {
  const today = fmtDate(new Date())
  const now = new Date()
  const monthStart = fmtDate(new Date(now.getFullYear(), now.getMonth(), 1))
  const monthEnd = fmtDate(new Date(now.getFullYear(), now.getMonth() + 1, 0))

  // 医生总数
  try {
    const res = await listDoctors({ pageNum: 1, pageSize: 1 })
    stats.doctorCount = extractNumber(res, 'total')
  } catch { /* ignore */ }

  // 科室总数 + 科室分布
  try {
    const res = await listDepartments()
    const arr = extractArray(res)
    departmentList.value = arr.map((d) => ({ name: d.name || '-', type: d.departmentType || d.type || '科室' }))
    stats.departmentCount = arr.length || extractNumber(res, 'total')
  } catch { /* ignore */ }

  // 今日排班
  try {
    const arr = await listSchedules({ startDate: today, endDate: today })
    let list = extractArray(arr)
    todayList.value = list
    stats.todayScheduleCount = list.length
    const uniqueDoctors = new Set(list.map((i) => i.doctorId || i.doctorName).filter(Boolean))
    stats.todayDoctorOnDuty = uniqueDoctors.size
  } catch { /* ignore */ }

  // 本月排班
  try {
    const arr = await listSchedules({ startDate: monthStart, endDate: monthEnd })
    let list = extractArray(arr)
    stats.monthScheduleCount = list.length
  } catch { /* ignore */ }
}

function go(path) { router.push(path) }

onMounted(loadStats)
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 欢迎卡片 */
.welcome-card {
  background: linear-gradient(135deg, #1e4fd6 0%, #2e6fe8 100%);
  border-radius: 16px;
  padding: 24px 28px;
  color: #ffffff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 20px;
  box-shadow: 0 4px 16px rgba(30, 79, 214, 0.2);
}

.welcome-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.welcome-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.18);
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
  margin-bottom: 4px;
}

.welcome-sub {
  font-size: 13px;
  opacity: 0.9;
}

.welcome-right {
  display: flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.12);
  padding: 12px 24px;
  border-radius: 12px;
}

.welcome-stat {
  text-align: center;
}

.welcome-stat-num {
  font-size: 22px;
  font-weight: 700;
  line-height: 1.2;
}

.welcome-stat-label {
  font-size: 12px;
  opacity: 0.9;
  margin-top: 2px;
}

.welcome-sep {
  width: 1px;
  height: 28px;
  background: rgba(255, 255, 255, 0.25);
}

/* 标题 */
.section-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  padding: 0 4px;
}

.section-title .el-icon {
  color: #1e4fd6;
  font-size: 16px;
}

/* 快捷入口 */
.quick-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.quick-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 18px;
  display: flex;
  align-items: center;
  gap: 14px;
  cursor: pointer;
  transition: all 0.2s;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.quick-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
}

.quick-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  color: #ffffff;
  flex-shrink: 0;
}

.quick-blue .quick-icon { background: linear-gradient(135deg, #1e4fd6, #409eff); }
.quick-green .quick-icon { background: linear-gradient(135deg, #52c41a, #67c23a); }
.quick-orange .quick-icon { background: linear-gradient(135deg, #d48806, #e6a23c); }
.quick-purple .quick-icon { background: linear-gradient(135deg, #722ed1, #9254de); }

.quick-text {
  flex: 1;
  min-width: 0;
}

.quick-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.quick-desc {
  font-size: 12px;
  color: #909399;
  line-height: 1.4;
}

.quick-arrow {
  color: #c0c4cc;
  font-size: 14px;
}

.quick-card:hover .quick-arrow {
  color: #1e4fd6;
}

/* 面板卡片 */
.row {
  margin-top: 0;
}

.panel-card {
  background: #ffffff;
  border-radius: 14px;
  padding: 18px 20px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.panel-title .el-icon {
  color: #1e4fd6;
  font-size: 16px;
}

.empty-tip {
  padding: 20px 0;
}

/* 排班列表 */
.schedule-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.schedule-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 14px;
  background: #f8fafc;
  border-radius: 10px;
  transition: background 0.15s;
}

.schedule-item:hover {
  background: #eef2f8;
}

.schedule-shift {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  min-width: 56px;
  text-align: center;
}

.shift-morning { background: #fff3e0; color: #d48806; }
.shift-afternoon { background: #e6f7ff; color: #1890ff; }
.shift-night { background: #f3e5f5; color: #722ed1; }
.shift-day { background: #e8f4e9; color: #52c41a; }

.schedule-info {
  flex: 1;
  min-width: 0;
}

.schedule-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.schedule-sub {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.schedule-time {
  font-size: 13px;
  color: #606266;
  font-family: 'Courier New', monospace;
}

/* 系统概览 */
.overview-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.overview-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 10px;
}

.overview-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #ffffff;
  flex-shrink: 0;
}

.overview-icon.blue { background: #409eff; }
.overview-icon.green { background: #67c23a; }
.overview-icon.orange { background: #e6a23c; }
.overview-icon.purple { background: #9254de; }

.overview-info {
  flex: 1;
}

.overview-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 2px;
}

.overview-value {
  font-size: 18px;
  font-weight: 700;
  color: #303133;
}

/* 温馨提示 */
.tip-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.tip-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 10px 12px;
  background: #f8fafc;
  border-radius: 10px;
  font-size: 13px;
  color: #606266;
  line-height: 1.6;
}

.tip-icon {
  font-size: 16px;
  margin-top: 2px;
  flex-shrink: 0;
}

.tip-icon.blue { color: #409eff; }
.tip-icon.orange { color: #e6a23c; }
.tip-icon.green { color: #67c23a; }

/* 科室分布 */
.dept-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.dept-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 14px 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);
  transition: all 0.2s;
}

.dept-card:hover {
  transform: translateY(-1px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.06);
}

.dept-avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 18px;
  flex-shrink: 0;
}

.dept-name {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 2px;
}

.dept-type {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 992px) {
  .quick-grid { grid-template-columns: repeat(2, 1fr); }
  .dept-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 640px) {
  .welcome-right {
    width: 100%;
    justify-content: space-between;
    gap: 10px;
    padding: 10px 16px;
  }
  .quick-grid { grid-template-columns: 1fr; }
  .dept-grid { grid-template-columns: 1fr; }
}
</style>