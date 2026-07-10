export const outpatientMenuItems = [
  { key: 'patient-view', label: '患者查看', route: '/patients' },
  { key: 'medical-home', label: '医生诊疗：病历首页', route: '/medical-home' },
  { key: 'exam-req', label: '医生诊疗：检查申请', route: '/exam-request' },
  { key: 'lab-req', label: '医生诊疗：检验申请', route: '/lab-request' },
  { key: 'consult-record', label: '看诊记录', route: '/consult-record' },
  { key: 'ai-report', label: '检查/检验结果', route: '/ai-report' },
  { key: 'diagnosis', label: '医生诊疗：门诊确诊', route: '/diagnosis' },
  { key: 'prescription', label: '医生诊疗：开设处方', route: '/prescription' },
  { key: 'fee-query', label: '医生诊疗：费用查询', route: '/fee-query' }
];

export const registrationMenuItems = [
  { key: 'registration-dashboard', label: '挂号工作台', route: '/registration/dashboard' },
  { key: 'offline-registration', label: '线下挂号', route: '/registration/offline' },
  { key: 'online-registration-confirm', label: '线上挂号确认', route: '/registration/online-confirm' },
  { key: 'registration-fee-management', label: '收费管理', route: '/registration/fee-management' },
  { key: 'registration-fee-query', label: '费用查询', route: '/registration/fee-query' },
  { key: 'registration-refund-management', label: '退号退费', route: '/registration/refund' }
];

export const examMenuItems = [
  { key: 'exam-workbench', label: '检查管理', route: '/exam' }
];

export const labMenuItems = [
  { key: 'lab-workbench', label: '检验管理', route: '/lab' }
];

export const pharmacyMenuItems = [
  { key: 'pharmacy-dispatch', label: '药房发药管理', route: '/pharmacy/dispatch' },
  { key: 'pharmacy-refund', label: '药房退药管理', route: '/pharmacy/refund' },
  { key: 'pharmacy-stock', label: '药房库存管理', route: '/pharmacy/stock' }
];

export const roleMenus = {
  outpatient: outpatientMenuItems,
  registration: registrationMenuItems,
  exam: examMenuItems,
  lab: labMenuItems,
  pharmacy: pharmacyMenuItems
};

const doctorTypeModules = {
  OUTPATIENT: 'outpatient',
  REGISTRATION: 'registration',
  EXAM: 'exam',
  LAB: 'lab',
  PHARMACY: 'pharmacy'
};

const knownWorkbenchRoutes = new Set([
  '/patients',
  '/medical-home',
  '/exam-request',
  '/lab-request',
  '/consult-record',
  '/ai-report',
  '/diagnosis',
  '/prescription',
  '/fee-query',
  '/registration',
  '/registration/dashboard',
  '/registration/offline',
  '/registration/online-confirm',
  '/registration/fee-management',
  '/registration/fee-query',
  '/registration/refund',
  '/exam',
  '/lab',
  '/pharmacy/dispatch',
  '/pharmacy/refund',
  '/pharmacy/stock'
]);

function resolveDoctorTypeModule(source = {}) {
  const normalizedDoctorType = String(source.doctorType || '').trim().toUpperCase();
  return doctorTypeModules[normalizedDoctorType] || '';
}

export function resolveRoleModule(source = {}) {
  if (source.moduleKey === 'outpatient' || source.moduleKey === 'registration' || source.moduleKey === 'exam' || source.moduleKey === 'lab' || source.moduleKey === 'pharmacy') {
    return source.moduleKey;
  }

  const doctorTypeModule = resolveDoctorTypeModule(source);
  if (doctorTypeModule) {
    return doctorTypeModule;
  }

  const roleCode = String(source.roleCode || '');
  const roleName = String(source.roleName || source.role || '');
  const workbenchRoute = String(source.workbenchRoute || '');

  if (workbenchRoute.startsWith('/exam')) {
    return 'exam';
  }

  if (workbenchRoute.startsWith('/lab')) {
    return 'lab';
  }

  if (workbenchRoute.startsWith('/registration')) {
    return 'registration';
  }

  if (workbenchRoute.startsWith('/pharmacy')) {
    return 'pharmacy';
  }

  if (/exam|check|inspection/i.test(roleCode)) {
    return 'exam';
  }

  if (/lab|inspect|laboratory/i.test(roleCode)) {
    return 'lab';
  }

  if (/检查/.test(roleName)) {
    return 'exam';
  }

  if (/检验/.test(roleName)) {
    return 'lab';
  }

  if (/registration|register|guahao/i.test(roleCode)) {
    return 'registration';
  }

  if (/挂号|收费|窗口/.test(roleName)) {
    return 'registration';
  }

  if (/pharmacy|drug|dispense/i.test(roleCode)) {
    return 'pharmacy';
  }

  if (/药房|发药|药品/.test(roleName)) {
    return 'pharmacy';
  }

  return 'outpatient';
}

export function resolveWorkbenchRoute(source = {}) {
  if (source.workbenchRoute && knownWorkbenchRoutes.has(source.workbenchRoute)) {
    return source.workbenchRoute;
  }

  const moduleKey = resolveRoleModule(source);
  return roleMenus[moduleKey]?.[0]?.route || outpatientMenuItems[0].route;
}

export function getMenuItemsByRole(source = {}) {
  return roleMenus[resolveRoleModule(source)] || outpatientMenuItems;
}

export function getRoleLabel(source = {}) {
  const moduleKey = resolveRoleModule(source);
  if (moduleKey === 'registration') return '挂号医生';
  if (moduleKey === 'exam') return '检查医生';
  if (moduleKey === 'lab') return '检验医生';
  if (moduleKey === 'pharmacy') return '药房医生';
  return '门诊医生';
}
