import http from './http';

export function syncPatient(payload) {
  return http.post('/registration/patient/sync', payload);
}

export function getRegistrationDepartments() {
  return http.get('/registration/departments');
}

export function getRegistrationDoctors(params = {}) {
  return http.get('/registration/doctors', { params });
}

export function getRegistrationSchedules(params = {}) {
  return http.get('/registration/schedules', { params });
}

export function submitOfflineRegistration(payload) {
  return http.post('/registration/offline/submit', payload);
}

export function submitOnlineRegistration(payload) {
  return http.post('/registration/online/submit', payload);
}

export function chargeRegistration(payload) {
  return http.post('/registration/fee/charge', payload);
}

export function chargeFeeOrder(payload) {
  return http.post('/registration/fee/order/charge', payload);
}

export function getRegistrationQueue(params = {}) {
  return http.get('/registration/queue', { params });
}

export function getOnlinePendingRegistrations() {
  return http.get('/registration/online/pending');
}

export function confirmOnlineRegistration(registrationId) {
  return http.put('/registration/online/confirm', null, {
    params: { registrationId }
  });
}

export function getPendingFees(params = {}) {
  return http.get('/registration/fee/pending', { params });
}

export function getFeeHistory(params = {}) {
  return http.get('/registration/fee/history', { params });
}

export function checkRefund(feeOrderId) {
  return http.get('/registration/fee/refund/check', {
    params: { feeOrderId }
  });
}

export function refundFee(payload) {
  return http.post('/registration/fee/refund', payload);
}
