import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { AlertTriangle, CheckCircle2, Info, X } from 'lucide-react'

type ToastTone = 'success' | 'error' | 'info'

interface ToastItem {
  id: number
  message: string
  tone: ToastTone
}

interface ToastContextValue {
  notify: (message: string, tone?: ToastTone) => void
  notifyError: (error: unknown) => void
}

const ToastContext = createContext<ToastContextValue | null>(null)

let nextToastId = 1

export function ToastProvider({ children }: { children: ReactNode }) {
  const [items, setItems] = useState<ToastItem[]>([])

  const remove = useCallback((id: number) => {
    setItems((current) => current.filter((item) => item.id !== id))
  }, [])

  const notify = useCallback(
    (message: string, tone: ToastTone = 'success') => {
      const id = nextToastId++
      setItems((current) => [...current, { id, message, tone }])
      window.setTimeout(() => remove(id), 4800)
    },
    [remove],
  )

  const notifyError = useCallback(
    (error: unknown) => {
      const message =
        error instanceof Error ? error.message : 'Ocurrió un error inesperado.'
      notify(message, 'error')
    },
    [notify],
  )

  const value = useMemo(() => ({ notify, notifyError }), [notify, notifyError])

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite" aria-atomic="false">
        {items.map((item) => {
          const Icon =
            item.tone === 'success'
              ? CheckCircle2
              : item.tone === 'error'
                ? AlertTriangle
                : Info
          return (
            <div key={item.id} className={`toast toast-${item.tone}`}>
              <Icon size={20} aria-hidden="true" />
              <p>{item.message}</p>
              <button
                type="button"
                className="toast-close"
                onClick={() => remove(item.id)}
                aria-label="Cerrar notificación"
              >
                <X size={16} />
              </button>
            </div>
          )
        })}
      </div>
    </ToastContext.Provider>
  )
}

export function useToast(): ToastContextValue {
  const context = useContext(ToastContext)
  if (!context) throw new Error('useToast debe usarse dentro de ToastProvider.')
  return context
}
