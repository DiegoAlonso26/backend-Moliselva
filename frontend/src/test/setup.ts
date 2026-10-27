class MemoryStorage implements Storage {
  private data = new Map<string, string>()

  get length(): number {
    return this.data.size
  }

  clear(): void {
    this.data.clear()
  }

  getItem(key: string): string | null {
    return this.data.has(key) ? (this.data.get(key) ?? null) : null
  }

  key(index: number): string | null {
    return Array.from(this.data.keys())[index] ?? null
  }

  removeItem(key: string): void {
    this.data.delete(key)
  }

  setItem(key: string, value: string): void {
    this.data.set(key, String(value))
  }
}

function installStorage(target: object) {
  try {
    const current = (target as { localStorage?: Storage }).localStorage
    if (current) return
  } catch {
    return
  }
  Object.defineProperty(target, 'localStorage', {
    configurable: true,
    value: new MemoryStorage(),
  })
}

if (typeof window !== 'undefined') installStorage(window)
if (typeof globalThis !== 'undefined') installStorage(globalThis)
