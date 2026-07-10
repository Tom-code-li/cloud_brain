export const WORKBENCH_STATUS = {
  pending: { label: '待处理', badgeClass: 'badge-pending', dataStatus: 'pending' },
  progress: { label: '进行中', badgeClass: 'badge-progress', dataStatus: 'in_progress' },
  published: { label: '已发布', badgeClass: 'badge-published', dataStatus: 'published' }
};

const STATUS_LABELS = {
  '待缴费': '未支付',
  '待支付': '未支付',
  '已支付': '已支付',
  '待执行': '待执行',
  '执行中': '进行中',
  '已执行': '已执行',
  '已完成': '已完成',
  '草稿': '草稿',
  '已发布': '已发布',
  pending: '待处理',
  progress: '进行中',
  published: '已发布',
  in_progress: '进行中',
  sampling: '样本采集中',
  result_entered: '结果已录入',
  NORMAL: '正常',
  LOW: '偏低',
  HIGH: '偏高',
  ABNORMAL: '异常',
  normal: '正常',
  low: '偏低',
  high: '偏高',
  abnormal: '异常'
};

const ABNORMAL_FLAG_CLASS = {
  NORMAL: 'flag-normal',
  LOW: 'flag-low',
  HIGH: 'flag-high',
  ABNORMAL: 'flag-high'
};

const ABNORMAL_FLAG_LABEL = {
  NORMAL: '正常',
  LOW: '偏低',
  HIGH: '偏高',
  ABNORMAL: '异常'
};

export function statusLabel(status) {
  return STATUS_LABELS[status] ?? status ?? '—';
}

export function workbenchStatusLabel(status) {
  return WORKBENCH_STATUS[status]?.label ?? '待处理';
}

export function workbenchStatusBadge(status) {
  return WORKBENCH_STATUS[status]?.badgeClass ?? 'badge-pending';
}

export function abnormalFlagClass(flag) {
  return ABNORMAL_FLAG_CLASS[flag] ?? 'flag-na';
}

export function abnormalFlagLabel(flag) {
  return ABNORMAL_FLAG_LABEL[flag] ?? '—';
}

function normalizedReportStatus(item) {
  const status = item?.reportStatus ?? item?.status ?? '';
  return typeof status === 'string' ? status.trim() : status;
}

export function resolveStage(item) {
  if (!item) return 0;
  const reportStatus = normalizedReportStatus(item);
  if (item.feeStatus !== '已支付' || item.orderStatus === '待缴费') return 1;
  if (item.orderStatus === '待执行') return 2;
  if (reportStatus === '已发布' || reportStatus === '已回阅') return 5;
  if (reportStatus === '草稿') return 4;
  return 3;
}

export function resolveOrderProgressStage(item) {
  if (!item) return 0;
  if (item.feeStatus !== '已支付' || item.orderStatus === '待缴费') return 1;
  if (item.orderStatus === '待执行') return 2;
  if (item.orderStatus === '已完成') return 5;
  if (item.orderStatus === '执行中') {
    return resolveStage(item) >= 4 ? 4 : 3;
  }
  return 1;
}

export function actionLabel(status) {
  if (status === 'published') return '查看报告';
  if (status === 'progress') return '继续处理';
  return '进入检查';
}
