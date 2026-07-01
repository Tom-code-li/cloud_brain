import http from './medicalExamHttp.js';

export const saveExamResult = (payload) => http.post('/result/exam', payload);

export const saveLabResult = (payload) => http.post('/result/lab', payload);
