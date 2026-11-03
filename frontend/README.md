# Frontend Moliselva

Interfaz de Elías para la demo PO-01: acceso, recepción de maíz y kardex.
Integrada desde `origin/elias` para validar backend y frontend en el mismo PR.
Los documentos Word y las capturas de entrega se conservan en la rama original.

## Ejecutar

Requisitos: Node 20 y npm. Desde `frontend/`:

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
```

GitHub Actions ejecuta ambos comandos después de instalar las dependencias con
`npm ci`. Las pruebas existentes de Elías se conservan sin modificaciones.
