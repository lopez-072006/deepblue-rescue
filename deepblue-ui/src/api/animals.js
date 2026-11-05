import { request } from './client'
export const getAnimal = code => request(`/api/animals/${encodeURIComponent(code)}`)
export const getAnimalTreatments = code => request(`/api/animals/${encodeURIComponent(code)}/treatments`)
export const getTreatmentEligibility = code => request(`/api/animals/${encodeURIComponent(code)}/treatment-eligibility`)
