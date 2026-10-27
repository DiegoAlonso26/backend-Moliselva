import { realApi } from './http'
import { mockApi, resetDemoState } from './mock'
import type { ApiClient } from './types'

export const apiMode = import.meta.env.VITE_API_URL ? 'api' : 'demo'

export const api: ApiClient = apiMode === 'demo' ? mockApi : realApi

export { resetDemoState }
export * from './types'
