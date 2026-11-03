import { realPo01Api } from './http'
import { mockPo01Api, po01ApiMode, resetPo01State } from './mock'
import type { Po01ApiClient } from './types'

export const po01Api: Po01ApiClient =
  po01ApiMode === 'demo' ? mockPo01Api : realPo01Api

export { po01ApiMode, resetPo01State }
export * from './types'
