import http from './medicalExamHttp.js';

export const generateDraft = (payload) => http.post('/report/draft', payload);

export const publishReport = (payload) => http.put('/report/publish', payload);

export const rejectReport = (orderItemId) => http.post('/report/reject', { orderItemId });

export const getReportDetail = (reportId) => http.get('/report/detail', { params: { reportId } });
