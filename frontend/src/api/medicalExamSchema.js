import http from './medicalExamHttp.js';

export const getItemSchema = (itemName) => http.get('/item/schema', { params: { itemName } });
