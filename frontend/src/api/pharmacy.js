import http from './pharmacyHttp.js';

export function getDispatchQueue(params = {}) {
  return http.get('/pharmacy/dispatch/queue', { params });
}

export function getDispenseDetail(dispenseId) {
  return http.get(`/pharmacy/dispense/${dispenseId}`);
}

export function markDispensed(payload) {
  return http.post('/pharmacy/dispense/mark', payload);
}

export function getRefundRecords(params = {}) {
  return http.get('/pharmacy/refund/list', { params });
}

export function submitRefund(payload) {
  return http.post('/pharmacy/refund/submit', payload);
}

export function getDrugStock(params = {}) {
  return http.get('/pharmacy/stock', { params });
}

export function updateStock(drugId, payload) {
  return http.post(`/pharmacy/stock/${drugId}`, payload);
}

export function getPurchaseRequests(params = {}) {
  return http.get('/pharmacy/purchase/list', { params });
}

export function submitPurchaseRequest(payload) {
  return http.post('/pharmacy/purchase/submit', payload);
}

export function getDispenseHistory(params = {}) {
  return http.get('/pharmacy/dispense/history', { params });
}

export function getDispenseStats() {
  return http.get('/pharmacy/stats');
}
