import http from './medicalExamHttp.js';

export const getWorkbench = (status = 'all', keyword = '', itemName = '') =>
  http.get('/workbench', { params: { status, keyword, patientName: keyword, itemName } });

export const executeOrder = (orderItemId) =>
  http.post('/order/execute', { orderItemId });

export const confirmSample = (orderItemId, sampleId) =>
  http.post('/order/sample', { orderItemId, sampleId });

export const getItemDetail = (orderItemId) =>
  http.get('/order/item-detail', { params: { orderItemId } });
