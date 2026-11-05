import { request } from './client'
export const getCasesByStatus = status => request(`/api/rescue-cases?status=${encodeURIComponent(status)}`)
export const changeCaseStatus = (caseCode, status) => request(`/api/rescue-cases/${encodeURIComponent(caseCode)}/status`, { method: 'PATCH', body: JSON.stringify({ status }) })
