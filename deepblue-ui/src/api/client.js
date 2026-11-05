export class ApiError extends Error { constructor({ status, message, details }) { super(message); this.status = status; this.details = details ?? {} } }
export async function request(path, options = {}) {
  let response
  try { response = await fetch(path, { headers: { 'Content-Type': 'application/json' }, ...options }) }
  catch { throw new ApiError({ status: 0, message: 'No se pudo contactar el servidor. Verifica que Spring Boot esté iniciado.' }) }
  if (!response.ok) { const body = await response.json().catch(() => null); throw new ApiError({ status: body?.status ?? response.status, message: body?.message ?? 'No se pudo completar la solicitud', details: body?.details }) }
  if (response.status === 204) return null
  return response.json()
}
