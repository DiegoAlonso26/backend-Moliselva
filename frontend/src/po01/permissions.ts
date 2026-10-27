import type { Rol } from './types'

export const roleLabels: Record<Rol, string> = {
  ADMINISTRADOR: 'Administrador',
  JEFE_PLANTA: 'Jefe de planta',
  OPERARIO: 'Operario',
  CONTADORA: 'Contadora',
}

export function roleLabel(rol: Rol): string {
  return roleLabels[rol]
}

export function homeForRole(rol: Rol): string {
  return rol === 'OPERARIO' || rol === 'JEFE_PLANTA' ? '/recepcion' : '/kardex'
}

export function canRegisterReception(rol: Rol): boolean {
  return rol === 'JEFE_PLANTA' || rol === 'OPERARIO'
}

export function canDecideReception(rol: Rol): boolean {
  return rol === 'JEFE_PLANTA'
}

export function canExportKardex(rol: Rol): boolean {
  return rol === 'JEFE_PLANTA' || rol === 'CONTADORA' || rol === 'ADMINISTRADOR'
}
