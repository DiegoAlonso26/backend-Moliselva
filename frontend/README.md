# Moliselva · Recepción de maíz y kardex

Frontend de la Tarea 11 para el proceso **PO-01 Recepción y acondicionamiento de maíz**. La aplicación concentra la demostración en tres pantallas operativas y consume la API real del backend de la Tarea 10.

## Alcance

- **Acceso:** autenticación con JWT, sesión en `sessionStorage` y redirección según rol.
- **Recepción de maíz:** registro de proveedor, peso bruto, tara, sacos, humedad y merma; cálculo de peso neto y entrada al kardex; bandeja diaria con filtros y decisión del jefe de planta.
- **Kardex de maíz:** stock actual, libro de movimientos, saldo acumulado y exportación CSV.
- **Responsive:** diseño operativo para escritorio, tablet y móvil.
- **PWA:** build instalable con service worker y navegación por hash.

Producción por lote, pedidos y despachos no forman parte de esta entrega. El backend conserva esos módulos, pero la interfaz se limitó al escenario PO-01 para que la revisión sea verificable de punta a punta.

## Stack

| Componente | Versión |
| --- | --- |
| React | 18.3 |
| TypeScript | 5.7 |
| Vite | 6.0 |
| React Router | 6.30 |
| Vitest | 2.1 |
| vite-plugin-pwa | 0.21 |

## Inicio rápido

Requisitos: Node.js 20 o superior, npm y, para el modo integrado, el backend de Moliselva con PostgreSQL en `http://localhost:8080`.

```bash
npm install
npm run dev
```

La aplicación se abre en `http://localhost:5173`. El archivo `.env.development` apunta a la API local:

```env
VITE_API_URL=http://localhost:8080/api
```

El backend debe permitir el origen `http://localhost:5173`. El CORS local está configurado para ese origen; use `localhost`, no `127.0.0.1`.

### Modo demo sin backend

El modo demo usa `localStorage` y datos ficticios. Se activa cuando no existe `VITE_API_URL` o cuando se fuerza con `VITE_USAR_MOCKS`:

```bash
VITE_USAR_MOCKS=true npm run dev
```

Para revisar el build de producción en modo demo:

```bash
npm run build
npm run preview
```

La vista previa se abre en `http://localhost:4173`.

## Usuarios de prueba

Los nombres de usuario se mantienen estables para la integración. Los nombres visibles corresponden a los datos de demostración.

| Usuario | Nombre | Rol | Clave API local | Clave demo |
| --- | --- | --- | --- | --- |
| `admin_demo` | Lucía Mendoza | Administrador | `MoliselvaDemo2026!` | `demo123` |
| `jefe_demo` | María Quispe | Jefe de planta | `MoliselvaDemo2026!` | `demo123` |
| `operario_demo` | Carlos Ríos | Operario | `MoliselvaDemo2026!` | `demo123` |
| `contadora_demo` | Ana Torres | Contadora | `MoliselvaDemo2026!` | `demo123` |

Las claves corresponden únicamente al entorno local de demostración. No se reutilizan en producción.

## Roles y permisos

| Acción | Administrador | Jefe de planta | Operario | Contadora |
| --- | --- | --- | --- | --- |
| Iniciar sesión | Sí | Sí | Sí | Sí |
| Registrar recepción | No | Sí | Sí | No |
| Decidir recepción pendiente | No | Sí | No | No |
| Consultar bandeja del día | Sí | Sí | Sí | Sí |
| Consultar kardex y stock | Sí | Sí | Sí | Sí |
| Exportar kardex | Sí | Sí | No | Sí |

Al ingresar, el operario y el jefe de planta abren Recepción; el administrador y la contadora abren Kardex. La interfaz oculta las acciones no permitidas y el backend valida el rol en cada operación.

## Reglas de negocio

- Humedad menor o igual a **14.5%**: recepción **ACEPTADA** automáticamente y entrada inmediata al kardex.
- Humedad mayor a 14.5% y menor o igual a **15%**: recepción **PENDIENTE**; solo el jefe de planta acepta o rechaza con un motivo.
- Humedad mayor a **15%**: recepción **RECHAZADA**, sin movimiento de inventario.
- Peso neto = peso bruto − tara − peso de sacos.
- Entrada al kardex = peso neto − merma de limpieza.
- El kardex es inmutable. Las correcciones se registran como ajustes con motivo y usuario.
- Una recepción pendiente no modifica el stock hasta que el jefe de planta la acepta.
- El operario no puede decidir ni exportar el kardex.

## Integración con la API

| Método | Ruta | Uso |
| --- | --- | --- |
| `POST` | `/auth/login` | Inicio de sesión y token JWT |
| `GET` | `/proveedores` | Proveedores activos |
| `GET` | `/recepciones?fecha=AAAA-MM-DD` | Bandeja diaria |
| `POST` | `/recepciones` | Registrar recepción |
| `PATCH` | `/recepciones/{id}/decision` | Aceptar o rechazar una pendiente |
| `GET` | `/inventario/maiz/kardex` | Libro de movimientos |
| `GET` | `/inventario/maiz/stock` | Stock actual de maíz |

- El token se envía como `Authorization: Bearer <token>`.
- La sesión se guarda en `sessionStorage` con la clave `moliselva_session`.
- La URL base se configura con `VITE_API_URL` y debe incluir el prefijo `/api`.
- El cliente HTTP está en `src/po01/http.ts`; el modo demo replica el mismo contrato en `src/po01/mock.ts`.
- `GET /recepciones` devuelve `registradoPor: null` porque el esquema de la Tarea 9 aún no expone `recepcion.id_registra`. La interfaz oculta la columna cuando todos los registros vienen vacíos y usa `autorizadoPor` para mostrar quién decidió.
- El kardex se muestra en orden cronológico ascendente, con el último movimiento al final.

## Estructura

```text
src/
  components/       Marca, layout, tablas, modales, badges y estados
  pages/            Login y página de acceso restringido
  po01/
    api.ts          Selección entre API real y modo demo
    http.ts         Cliente HTTP con JWT
    mock.ts         Implementación localStorage
    RecepcionPage.tsx
    KardexPage.tsx
    permissions.ts  Reglas de acceso por rol
    types.ts        Contrato TypeScript
  lib/format.ts     Fechas, kilogramos, usuarios y CSV
  auth/             Sesión y rutas protegidas
  styles.css        Sistema visual responsive
docs/
  CONTRATO_API.md   Contrato resumido de integración
  PANTALLAS.md      Guía de las tres pantallas
```

## Scripts

| Comando | Descripción |
| --- | --- |
| `npm run dev` | Servidor de desarrollo con API real |
| `npm run build` | TypeScript y build de producción con PWA |
| `npm run preview` | Sirve el build de producción en modo demo |
| `npm test` | Ejecuta las pruebas con Vitest |

## Verificación

- `npm run build` compila sin errores de TypeScript.
- `npm test` ejecuta **17 pruebas aprobadas**: reglas de humedad, permisos, peso neto, merma, kardex, stock y flujos heredados del backend.
- El flujo E2E se ejecutó con Playwright contra el backend local y PostgreSQL:
  - inicio de sesión como `jefe_demo`;
  - registro de una recepción con 14.7% de humedad, que quedó pendiente;
  - aceptación de una recepción pendiente desde el modal de decisión;
  - verificación de la entrada y del saldo en el kardex.
- La interfaz se revisó en 1440 px, 820 px y 390 px. Las tablas se adaptan sin desbordamiento horizontal de la página.
- Las capturas finales están en `docs/capturas/`.

## Limitaciones

- No hay despliegue público; la integración se validó en el entorno local.
- `registradoPor` no está disponible en el listado de recepciones por una limitación del esquema de la Tarea 9.
- El modo demo persiste solo en el navegador actual y no reemplaza la validación del backend.
- Los datos de proveedores, usuarios y recepciones son ficticios.

## Documentación

- `Documento Pantallas.docx`: documento de entrega con capturas y guía funcional.
- `docs/CONTRATO_API.md`: contrato resumido de la API consumida.
- `docs/PANTALLAS.md`: recorrido por las tres pantallas.
