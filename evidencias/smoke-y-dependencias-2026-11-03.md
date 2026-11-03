# Validación con PostgreSQL y actualización del frontend

Fecha: 3 de noviembre de 2026. Trabajo posterior al merge de la tarea 13.

## Recorrido HTTP con la base real

Se reconstruyó la API desde el backend fusionado en `main` (`1bf2935`) usando
`docker compose -f "../SW E3 Base de datos Moliselva/docker-compose.yml" up -d --build`.
Se utilizó el volumen existente de PostgreSQL 16 de la demo. El arranque terminó
correctamente, con `db` saludable y la API disponible en `localhost:8080`.

Después de comprobar que la API respondía, se ejecutaron, en orden:

```powershell
pwsh -NoProfile -File .\smoke-po01.ps1
pwsh -NoProfile -File .\smoke-crud.ps1
```

| Prueba con PostgreSQL real | Comprobaciones aprobadas | Código de salida |
|---|---:|---:|
| PO-01 completo | 49 | 0 |
| CRUD ampliado | 115 | 0 |
| Total | 164 | 0 |

Salidas guardadas:
[PO-01](smoke-po01-2026-11-03.txt) y
[CRUD](smoke-crud-2026-11-03.txt).
Se verificó que no contienen contraseñas locales ni JWT.

Se comprobaron los cuatro logins, estados por humedad, decisión del jefe,
entrada única al kardex, stock, permisos, exportación CSV, CORS y bitácora.
El segundo script también verificó los CRUD y los flujos de las extensiones ya
existentes. Los scripts crean datos descartables y conservan los movimientos
históricos en la base de la demo; no representan datos reales del negocio.
La API y PostgreSQL quedaron ejecutándose.

## Actualización de dependencias

Se actualizaron React Router, Vite, Vitest, Happy DOM y el plugin PWA. Se fijaron
dos overrides transitivos, limitados a Workbox, para actualizar las herramientas
de generación del service worker. Las versiones, su motivo y el seguimiento del
aviso pendiente se detallan en
[DEPENDENCIAS.md](../frontend/DEPENDENCIAS.md).

| Auditoría completa | Antes | Después |
|---|---:|---:|
| Críticas | 3 | 0 |
| Altas | 4 | 0 |
| Moderadas | 10 | 0 |
| Bajas | 2 | 4 |
| Total de paquetes señalados | 19 | 4 |

Se conservan los reportes completos
[antes](npm-audit-antes-2026-11-03.json) y
[después](npm-audit-despues-2026-11-03.json).
`npm audit --omit=dev` devuelve **cero vulnerabilidades conocidas** en las
dependencias de producción.

Los cuatro avisos bajos derivan de un mismo problema de `deepmerge`, a través
de `vite-plugin-pwa`, Workbox y el resolver de Rollup. El
[aviso publicado](https://github.com/advisories/GHSA-gcqx-w48m-w2h4) no identifica
una versión corregida. La dependencia se utiliza durante la compilación de la
PWA; el aviso sigue abierto y documentado. No se declara una auditoría total de
cero alertas ni se oculta el aviso mediante exclusiones.

El pipeline incorpora `npm run audit:security`, que falla ante vulnerabilidades
moderadas, altas o críticas y mantiene los avisos bajos visibles.

## Regresión comprobada con Node 20

- Instalación limpia con `npm ci` y el lockfile actualizado: correcta.
- Pruebas del frontend: **20 aprobadas de 20**. Las 17 originales se conservan;
  las tres nuevas montan la aplicación para comprobar protección de rutas,
  navegación recepción/kardex, cierre de sesión y acceso inicial de la contadora.
- TypeScript y compilación de Vite 6.4.5: correctos.
- PWA 2.0.0: service worker generado, con 14 entradas en el precaché.
- Auditoría con umbral moderado y auditoría de producción: códigos de salida 0.
- `actionlint` y `git diff --check`: correctos.

Estas pruebas de navegación usan la API simulada para aislar la actualización
del router. La integración real API/PostgreSQL es la de los smoke tests de la
primera sección; no se presenta esa prueba de componentes como un recorrido E2E
del navegador con el servidor real.
