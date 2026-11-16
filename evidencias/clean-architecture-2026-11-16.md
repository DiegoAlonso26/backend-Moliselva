# Migración de PO-01 a Clean Architecture (fase 1)

Fecha: 16 de noviembre de 2026, hora de Lima. Rama: `refactor/clean-architecture`, desde `feat/frontend-completo` (`3c0aae6`). Guía de la arquitectura: [`docs/ARQUITECTURA.md`](../docs/ARQUITECTURA.md).

Las salidas no contienen `DEMO_PASSWORD`, `POSTGRES_PASSWORD`, `JWT_SECRET` ni tokens: se buscaron los valores del `.env` local y el prefijo de los JWT, sin coincidencias. El `.env` fue local y no se sube.

## 1. Pruebas automáticas

```powershell
docker run --rm -e TZ=America/Lima -v "${PWD}:/app" -v moliselva-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B verify
```

Resultado: `BUILD SUCCESS`, **124 pruebas, 0 fallos y 0 errores** (antes eran 106).

| Clase | Pruebas | Cambio |
|---|---:|---|
| `RecepcionPolicyTest` | 28 | Solo cambió el paquete (`domain.recepcion`) |
| `RecepcionUseCaseTest` | 13 | Paquete e imports; `ActorRecepcion` → `Actor` y `*Port` → `*Gateway` (renombres). Aserciones sin cambios |
| `Po01SecurityTest` | 56 | Imports; `InventarioController` → `KardexMaizController`; los mocks de `InventarioService` pasan a `ConsultarKardexMaizUseCase` y `ExportarKardexMaizUseCase`; se importa `JwtActorActual`. Estados HTTP, códigos y roles sin cambios |
| `FinalizarLoteTest`, `ConfirmarDespachoTest`, `BusinessRulesTest` | 9 | Sin cambios |
| `ArquitecturaLimpiaTest` (nueva) | 8 | Reglas ArchUnit de dependencias entre capas |
| `ProveedorUseCaseTest` (nueva) | 7 | Normalización, permisos, bitácora y no encontrado |
| `KardexMaizUseCaseTest` (nueva) | 3 | Maíz no configurado, formato CSV y permiso de exportación |

`grep` confirma que `domain/` y `application/` no importan `org.springframework` ni `jakarta`.

## 2. Contrato de la API sin cambios

Se comparó `/v3/api-docs` de la versión anterior (demo con `3c0aae6`) con el de esta rama, levantada en el proyecto aislado `moliselva-clean`:

| Comparación | Antes | Después |
|---|---:|---:|
| Rutas | 48 | 48 |
| Esquemas | 33 | 33 |
| Diferencias en parámetros, cuerpos, respuestas, resúmenes, tags u `operationId` | — | **0** |

## 3. Pruebas de humo con base nueva

Proyecto `docker compose -p moliselva-clean` con base desde cero. El volumen de la demo no se tocó: se detuvo la demo durante la prueba y luego se reinició.

| Script | Resultado | Salida |
|---|---|---|
| `smoke-po01.ps1` | 51 OK | [smoke-po01-clean-2026-11-16.txt](smoke-po01-clean-2026-11-16.txt) |
| `smoke-crud.ps1` | 116 OK, con un ajuste (ver nota) | [smoke-crud-clean-2026-11-16.txt](smoke-crud-clean-2026-11-16.txt) |

**Nota sobre `smoke-crud.ps1`.** Su línea 118 espera que el operario consulte un insumo del lote (`GET /api/lotes/{id}/insumos/{insumoId}` → 200). El commit `3c0aae6` de `feat/frontend-completo`, anterior a esta rama, restringió esa ruta a JEFE_PLANTA, así que el script ya fallaba sobre esa rama. Esta rama no modifica `produccion` (`git diff 3c0aae6 -- produccion` está vacío). Para completar la verificación se ejecutó una copia del script con esa línea reemplazada por dos comprobaciones: operario → 403 y jefe → 200. El script del repositorio no se modificó aquí; su ajuste corresponde al commit `3c0aae6`.
