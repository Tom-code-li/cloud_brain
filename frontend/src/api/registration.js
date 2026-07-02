import registrationHttp from './registrationHttp.js';

export function syncPatient(payload) {
  return registrationHttp.post('/registration/patient/sync', payload);
}

export function getRegistrationDepartments() {
  return registrationHttp.get('/registration/departments');
}

export function getRegistrationDoctors(params = {}) {
  return registrationHttp.get('/registration/doctors', { params });
}

export function getRegistrationSchedules(params = {}) {
  return registrationHttp.get('/registration/schedules', { params });
}

export function submitOfflineRegistration(payload) {
  return registrationHttp.post('/registration/offline/submit', payload);
}

export function submitOnlineRegistration(payload) {
  return registrationHttp.post('/registration/online/submit', payload);
}

export function chargeRegistration(payload) {
  return registrationHttp.post('/registration/fee/charge', payload);
}

export function chargeFeeOrder(payload) {
  return registrationHttp.post('/registration/fee/order/charge', payload);
}

export function getRegistrationQueue(params = {}) {
  return registrationHttp.get('/registration/queue', { params });
}

export function getOnlinePendingRegistrations() {
  return registrationHttp.get('/registration/online/pending');
}

export function confirmOnlineRegistration(registrationId) {
  return registrationHttp.put('/registration/online/confirm', null, {
    params: { registrationId }
  });
}

export function getPendingFees(params = {}) {
  return registrationHttp.get('/registration/fee/pending', { params });
}

export function getFeeHistory(params = {}) {
  return registrationHttp.get('/registration/fee/history', { params });
}

export function checkRefund(feeOrderId) {
  return registrationHttp.get('/registration/fee/refund/check', {
    params: { feeOrderId }
  });
}

export function refundFee(payload) {
  return registrationHttp.post('/registration/fee/refund', payload);
}
