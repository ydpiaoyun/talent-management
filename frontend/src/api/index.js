import http from './http'

export const authApi = {
  login: (data) => http.post('/auth/login', data),
}

export const attrApi = {
  list: () => http.get('/attribute/list'),
  groups: () => http.get('/attribute/groups'),
  listByGroup: (group) => http.get('/attribute/list-by-group', { params: { group } }),
  page: (page, size) => http.get('/attribute/page', { params: { pageNum: page, pageSize: size } }),
  getById: (id) => http.get(`/attribute/${id}`),
  save: (data) => http.post('/attribute', data),
  update: (data) => http.put('/attribute', data),
  delete: (id) => http.delete(`/attribute/${id}`),
  toggle: (id) => http.put(`/attribute/${id}/toggle`),
}

export const talentApi = {
  page: (page, size, keyword) => http.get('/talent/page', { params: { pageNum: page, pageSize: size, keyword } }),
  listWithAttrs: () => http.get('/talent/list-with-attrs'),
  getById: (id) => http.get(`/talent/${id}`),
  save: (data) => http.post('/talent', data),
  update: (data) => http.put('/talent', data),
  delete: (id) => http.delete(`/talent/${id}`),
}

export const screeningApi = {
  planList: () => http.get('/screening/plan/list'),
  planGet: (id) => http.get(`/screening/plan/${id}`),
  planSave: (data) => http.post('/screening/plan', data),
  planUpdate: (data) => http.put('/screening/plan', data),
  planDelete: (id) => http.delete(`/screening/plan/${id}`),
  condList: (planId) => http.get(`/screening/condition/list/${planId}`),
  condSave: (data) => http.post('/screening/condition', data),
  condUpdate: (data) => http.put('/screening/condition', data),
  condDelete: (id) => http.delete(`/screening/condition/${id}`),
  condBatchSave: (planId, data) => http.post(`/screening/condition/batch/${planId}`, data),
  execute: (planId) => http.post(`/screening/execute/${planId}`),
}

export const scoringApi = {
  planList: () => http.get('/scoring/plan/list'),
  planGet: (id) => http.get(`/scoring/plan/${id}`),
  planSave: (data) => http.post('/scoring/plan', data),
  planUpdate: (data) => http.put('/scoring/plan', data),
  planDelete: (id) => http.delete(`/scoring/plan/${id}`),
  calculate: (planId) => http.post(`/scoring/calculate/${planId}`),
  ranking: (planId) => http.get(`/scoring/ranking/${planId}`),
}

export const expressionApi = {
  validate: (expression) => http.post('/expression/validate', { expression }),
}
