import { useMutation, useQueryClient } from '@tanstack/react-query'
import { registerTreatment } from '../api/treatments'
export function useRegisterTreatment() { const qc = useQueryClient(); return useMutation({ mutationFn: registerTreatment, onSuccess: created => qc.invalidateQueries({ queryKey: ['animal', created.animalCode] }) }) }
