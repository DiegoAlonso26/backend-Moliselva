# Frontend Moliselva

Interfaz de Elías para la demo PO-01: acceso, recepción de maíz y kardex.
Integrada desde `origin/elias` para validar backend y frontend en el mismo PR.
Los documentos Word y las capturas de entrega se conservan en la rama original.

## Ejecutar

Requisitos: Node 20.19 o posterior de la rama 20, o Node 22.12 o posterior, y npm.
Desde `frontend/`:

```powershell
npm ci
npm run dev
```

Abrir `http://localhost:5173`. La configuración de desarrollo apunta a
`http://localhost:8080/api`; el backend y PostgreSQL deben estar iniciados.
La variable `VITE_API_URL` permite configurar la dirección de la API.

Los usuarios demo son `operario_demo`, `jefe_demo`, `contadora_demo` y
`admin_demo`. La contraseña inicial se consulta en `DEMO_PASSWORD` del `.env`
local de la base de datos; no se publica en el repositorio.
El modo simulado se habilita con `VITE_USAR_MOCKS=true` y usa datos de prueba.

## Verificar

```powershell
npm test
npm run build
npm run audit:security
```

GitHub Actions ejecuta las pruebas, la compilación y la auditoría después de
instalar las dependencias con `npm ci`. Las pruebas existentes de Elías se
conservan y se agregan tres comprobaciones de navegación para validar la
actualización de React Router.

La auditoría bloquea vulnerabilidades moderadas, altas y críticas. Los avisos
bajos pendientes y las versiones fijadas se explican en
[DEPENDENCIAS.md](DEPENDENCIAS.md).
