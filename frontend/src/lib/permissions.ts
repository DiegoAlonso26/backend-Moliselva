import type { Rol } from '../api/types'

export const roleLabels: Record<Rol, string> = {
  ADMINISTRADOR: 'Administrador',
  JEFE_PLANTA: 'Jefe de planta',
  OPERARIO: 'Operario',
  CONTADORA: 'Contadora',
}

export function roleLabel(rol: Rol): string {
  return roleLabels[rol]
}

export function canRegisterReception(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'].includes(rol)
}

export function canAuthorizeReception(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA'].includes(rol)
}

export function canCreateLote(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'].includes(rol)
}

export function canConfirmNucleo(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA'].includes(rol)
}

export function canFinishLote(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO'].includes(rol)
}

export function canSeeFormula(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA'].includes(rol)
}

export function canManageOrders(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA', 'CONTADORA'].includes(rol)
}

export function canConfirmDispatch(rol: Rol): boolean {
  return ['ADMINISTRADOR', 'JEFE_PLANTA', 'CONTADORA', 'OPERARIO'].includes(rol)
}
