import type { Rol } from '../po01/types'

export type Row = Record<string, string | number | boolean | null>
export const ALL: Rol[] = ['ADMINISTRADOR', 'JEFE_PLANTA', 'OPERARIO', 'CONTADORA']
export const MANAGERS: Rol[] = ['ADMINISTRADOR', 'JEFE_PLANTA']
export const SALES: Rol[] = ['ADMINISTRADOR', 'JEFE_PLANTA', 'CONTADORA']
export const CHIEF: Rol[] = ['JEFE_PLANTA']
export interface Reference { path: string; id: string; label: string; filter?: (row: Row, values: Row) => boolean }
export interface Field {
  key: string; label: string; type?: 'text' | 'number' | 'password' | 'select' | 'textarea'
  required?: boolean; maxLength?: number; min?: number; max?: number; step?: number
  options?: string[]; reference?: Reference; default?: string | number; hint?: string
  immutable?: boolean
}
export interface Column { key: string; label: string; reference?: Reference }
export interface Action {
  roles?: Rol[]
  label: string; method: 'POST' | 'PATCH'; suffix: string; body?: Row
  fields?: Field[]; available?: (row: Row) => boolean; description: string
}
export interface Resource {
  title: string; singular: string; path: string; id: string; description: string
  read: Rol[]; write?: Rol[]; fields: Field[]; columns: Column[]
  createLabel?: string; createPath?: string; noEdit?: boolean; noDelete?: boolean; noCreate?: boolean
  deactivate?: boolean; limit?: number; filterDate?: boolean; warning?: string
  editable?: (row: Row) => boolean; deletable?: (row: Row, userId: number) => boolean
  actions?: Action[]; children?: (row: Row) => Resource[]
  map?: (row: Row) => Row; validate?: (values: Row) => string | null
  payload?: (values: Row, editing: boolean) => Row
  itemPath?: (row: Row) => string
  preview?: (values: Row) => string
}
export const ref = (path: string, id: string, label: string): Reference => ({ path, id, label })
export const nameField = (key: string, label: string, maxLength: number): Field => ({ key, label, required: true, maxLength })
export const optional = (key: string, label: string, maxLength: number): Field => ({ key, label, maxLength })
export const numberField = (key: string, label: string, step = 1, min = 1): Field => ({ key, label, type: 'number', step, min, required: true })
export const selectRef = (key: string, label: string, reference: Reference): Field => ({ key, label, type: 'select', required: true, reference })
