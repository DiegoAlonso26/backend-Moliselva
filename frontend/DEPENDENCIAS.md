# Actualización de dependencias del frontend

Revisión del 3 de noviembre de 2026. Se conservaron React 18, TypeScript y el
funcionamiento de las pantallas de PO-01. Se verificó con Node 20 en Linux.

| Dependencia directa | Anterior | Actual |
|---|---|---|
| react-router-dom | 6.30.0 | 7.18.5 |
| vite | 6.0.11 | 6.4.5 |
| vitest | 2.1.8 | 4.1.12 |
| happy-dom | ^15.11.7 | 21.0.4 |
| vite-plugin-pwa | 0.21.1 | 2.0.0 |

El mínimo declarado de Node es `^20.19.0 || >=22.12.0`. CI sigue usando la rama
20 y obtiene su última revisión disponible mediante `actions/setup-node`.
Se eligió Vitest 4 para conservar compatibilidad con Node 20; Vitest 5 exige
Node 22.12 o posterior. Se revisó la
[guía oficial de migración de Vitest 4](https://v4.vitest.dev/guide/migration.html).

## Dependencias transitivas de la PWA

Workbox 7.4.1 todavía solicita versiones anteriores de dos herramientas. Se
fijaron estas sustituciones, limitadas a `workbox-build`:

- `@trickfilm400/rollup-plugin-off-main-thread`: 4.0.0, que actualiza EJS y elimina
  la cadena antigua de Jake/Filelist. Consultar la
  [publicación oficial](https://github.com/Trickfilm400/rollup-plugin-off-main-thread/releases/tag/v4.0.0).
- `glob`: 14.0.3, que incorpora la corrección de cancelación y Minimatch 11.

Estas sustituciones se comprobaron mediante instalación limpia, compilación y
generación del service worker con precaché. No se usó `npm audit fix --force`,
cuya propuesta para la cadena restante implica regresar a un plugin PWA antiguo.
Revisar los overrides cuando Workbox actualice sus rangos para poder retirarlos.

## Resultado de la auditoría

| Auditoría completa | Inicial | Tras actualizar |
|---|---:|---:|
| Críticas | 3 | 0 |
| Altas | 4 | 0 |
| Moderadas | 10 | 0 |
| Bajas | 2 | 4 |
| Total de paquetes señalados | 19 | 4 |

Los cuatro avisos bajos corresponden a **un único aviso de seguridad** en la
cadena `vite-plugin-pwa → workbox-build → @rollup/plugin-node-resolve → deepmerge`.
La base de avisos declara afectadas las versiones 4.2.0 a 4.3.1 de `deepmerge` y
no identifica una versión corregida. Referencia:
[GHSA-gcqx-w48m-w2h4](https://github.com/advisories/GHSA-gcqx-w48m-w2h4).

Esta cadena pertenece a la compilación de la PWA; no es una dependencia de
ejecución de la API ni se usa para procesar solicitudes del usuario. El riesgo
pendiente consiste en fusionar objetos manipulados durante esa compilación.
Workbox utiliza el resolver en su
[generador de bundles](https://github.com/GoogleChrome/workbox/blob/v7.4.1/packages/workbox-build/src/lib/bundle.ts).
La revisión debe reabrirse cuando exista un parche de Deepmerge o una versión de
Workbox que sustituya esa dependencia. No se declara resuelto este aviso.

`npm audit --omit=dev` devuelve cero vulnerabilidades conocidas. El comando
`npm run audit:security` ejecuta `npm audit --audit-level=moderate`: mantiene los
avisos bajos visibles y devuelve error ante cualquiera moderado, alto o crítico.
Ese mismo comando forma parte del pipeline.

## Comprobaciones

- `npm ci`: instalación reproducible con el lockfile actualizado.
- `npm test`: las 17 pruebas originales y tres nuevas de navegación de la app.
- `npm run build`: comprobación de TypeScript, bundles y generación de la PWA.
- `npm run audit:security` y `npm audit --omit=dev`: revisión completa y de
  dependencias de producción, respectivamente.

Las nuevas pruebas montan `App` y comprueban el acceso sin sesión, la navegación
del jefe entre recepción y kardex, el cierre de sesión y la página inicial de la
contadora. Conservan simulada la API; el recorrido HTTP con PostgreSQL real se
registra por separado en las evidencias de los smoke tests.
