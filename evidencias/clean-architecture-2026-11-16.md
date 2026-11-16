# Migración de PO-01 a Clean Architecture (fase 1)

Fecha: 16 de noviembre de 2026, hora de Lima. Rama: `refactor/clean-architecture`, desde `feat/frontend-completo` (`3c0aae6`). Guía de la arquitectura: [`docs/ARQUITECTURA.md`](../docs/ARQUITECTURA.md).

Las salidas no contienen `DEMO_PASSWORD`, `POSTGRES_PASSWORD`, `JWT_SECRET` ni tokens: se buscaron los valores del `.env` local y el prefijo de los JWT, sin coincidencias. El `.env` fue local y no se sube.

## 1. Pruebas automáticas

```powershell
docker run --rm -e TZ=America/Lima -v "${PWD}:/app" -v moliselva-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B verify
```

Resultado de la primera etapa: `BUILD SUCCESS`, **124 pruebas, 0 fallos y 0 errores** (antes eran 106). Con la segunda etapa (sección 4) son 157.

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

**Nota sobre `smoke-crud.ps1`.** Su línea 118 espera que el operario consulte un insumo del lote (`GET /api/lotes/{id}/insumos/{insumoId}` → 200). El commit `3c0aae6` de `feat/frontend-completo`, anterior a esta rama, restringió esa ruta a JEFE_PLANTA, así que el script ya fallaba sobre esa rama. Esta rama no cambia esa regla: el `@PreAuthorize("hasRole('JEFE_PLANTA')")` de esas rutas sigue idéntico en `presentation/rest/produccion/LoteController`. Para completar la verificación se ejecutó una copia del script con esa línea reemplazada por dos comprobaciones: operario → 403 y jefe → 200. El script del repositorio no se modificó aquí; su ajuste corresponde al commit `3c0aae6`.

## 4. Segunda etapa: producción y ventas

Mismo día, misma rama. `produccion` y `ventas` pasaron de hexagonal a las cuatro capas: `domain/{produccion,ventas}`, `application/{produccion,ventas}/{usecase,gateway}`, `presentation/rest/{produccion,ventas}` e `infrastructure/persistence/jdbc/{produccion,ventas}`. Los composition roots son `ProduccionUseCaseConfig` y `VentasUseCaseConfig`.

**Reglas de negocio sin cambios.** `LotePolicy`, `DespachoPolicy`, `LoteChecks`, `VentaChecks` y los casos de uso conservan su lógica línea por línea. Se comparó cada archivo contra `3c0aae6` descontando paquete, imports, `@Service`, `@Transactional` y el renombre de puertos: **51 de 51 archivos de dominio, casos de uso y gateways son idénticos**. Las rutas, `@PreAuthorize` y `@ResponseStatus` de `LoteController`, `PedidoController` y `DespachoController` también son idénticos (18 anotaciones en cada uno); el controlador ahora obtiene el usuario con `ActorActual` en lugar de leer el JWT. Solo se quitaron `@Service` y `@Transactional` y se renombraron los puertos (`*Port` → `*Gateway`). `ProduccionFailure` y `VentaFailure` heredan de `FallaNegocio` y responden con el mismo HTTP y el mismo `codigo` (antes los traducía `comun.ApiErrors`).

**Transacciones sin cambios.** `infrastructure/config/Transacciones` envuelve cada caso de uso en un proxy con `TransactionInterceptor`: las interfaces `Consultar*` son de solo lectura y las demás de escritura, con reversión ante cualquier excepción, igual que las anotaciones que reemplaza. PO-01 también usa este envoltorio.

| Verificación | Resultado |
|---|---|
| `mvn -B verify` | BUILD SUCCESS, **157 pruebas** (124 + 33 nuevas), 0 fallos |
| Nuevas: `LotePolicyTest` (23), `DespachoPolicyTest` (7), `TransaccionesTest` (3) | Tamaños de lote, cálculo de kg por porcentaje, estados editables, pedido que admite despacho o cancelación, destino del reparto; commit, reversión y solo lectura |
| `FinalizarLoteTest`, `ConfirmarDespachoTest` | Solo cambian paquete, imports y nombres de tipo; aserciones idénticas |
| `/v3/api-docs` contra la línea base de `3c0aae6` | 48 rutas, 33 esquemas, **0 diferencias** |
| `smoke-po01.ps1` (base nueva) | 51 OK |
| `smoke-crud.ps1` (base nueva) | 116 OK, con el mismo ajuste de la línea 118 |

`smoke-crud.ps1` incluye la comprobación transaccional más fuerte contra PostgreSQL real: «finalización fallida no deja movimientos ni cambia estado». Un lote con consumo mayor al stock responde 409 `STOCK_INSUFICIENTE`, no deja movimientos en el kardex y sigue EN_PROCESO; después se finaliza con una salida y una entrada. También cubre el CRUD completo de lotes, pedidos, despachos y sus detalles, los cambios de estado, la confirmación del despacho con salida de stock y los rechazos (despacho que excede el pedido, lote finalizado dos veces, eliminación de registros cerrados).

Las salidas de los humos de esta sección reemplazan a las de la sección 3, porque corresponden al código final de la rama.

## 5. Tercera etapa: el resto del backend

Mismo día, misma rama. `auth`, `comun`, `maestros`, `operaciones` (fórmulas), `inventario` (stock general, movimientos y ajustes) y `service` dejaron de existir como paquetes legados. Sus reglas pasaron a casos de uso sin Spring y sus detalles técnicos a `infrastructure` y `presentation`:

| Antes | Ahora |
|---|---|
| `AuthService`, `AuthRepository`, `DemoPasswordInitializer` | `IniciarSesion`, `ConsultarUsuarios`, `GestionarUsuarios`, `ConsultarRoles`, `ActivarUsuariosDemo`; `JdbcUsuarioGateway`, `JdbcRolGateway`, `JwtEmisorToken`, `BCryptCifradorClaves`, `UsuariosDemoInicializador` |
| `MaestrosService`, `MaestrosRepository` | `ConsultarMaestros`, `GestionarClientes`, `GestionarInsumos`, `GestionarProductos`; la normalización (trim, vacíos a null, `kg`, «Saco de 40 kg») está en los `*Datos` del dominio |
| `OperacionService` | `ConsultarFormulas`, `GestionarFormulas`, `GestionarDetallesFormula`, `JdbcFormulaGateway` |
| `InventarioService` (general) | `ConsultarInventario`, `RegistrarAjuste`; la traducción de «Stock insuficiente» pasa a `JdbcInventarioGateway` |
| `comun`: `SecurityConfig`, `ApiErrors`, `OpenApiConfig`, bitácora | `infrastructure/security/SecurityConfig`, `presentation/rest/error/ManejadorErroresGenerales`, `presentation/rest/config/OpenApiConfig`, `ConsultarBitacora` y `JdbcBitacoraGateway` |
| `service/BusinessRules` | `domain/reglas/BusinessRules` |
| `ApiException` (HTTP dentro de los servicios) | Eliminada: las fallas son `FallaNegocio` (nuevo tipo `NO_AUTENTICADO` → 401) |

El SQL es el mismo y el orden de las validaciones también. Por ejemplo, desactivar un insumo comprueba, en este orden, Maíz protegido, stock en cero, uso activo y existencia. `ArquitecturaLimpiaTest` ahora exige que **toda clase esté en una de las cuatro capas**: en la raíz solo queda `MoliselvaApplication`.

| Verificación | Resultado |
|---|---|
| `mvn -B verify` | BUILD SUCCESS, **185 pruebas**, 0 fallos |
| Nuevas pruebas de casos de uso | `AuthUseCaseTest` (6), `MaestrosUseCaseTest` (7), `FormulasUseCaseTest` (6), `AjusteInventarioUseCaseTest` (8) y una regla ArchUnit más (9 en total) |
| `Po01SecurityTest` | Se movió a `presentation/rest`. Cambian imports y mocks (`UsuarioGateway` en lugar de `AuthRepository`, `IniciarSesionUseCase` en lugar de `AuthService`, `ConsultarBitacoraUseCase` en lugar de `BitacoraService`). Las 56 expectativas no cambian |
| `/v3/api-docs` contra `3c0aae6` | 48 rutas, 33 esquemas, **0 diferencias** |
| `smoke-po01.ps1` / `smoke-crud.ps1` (base nueva) | 51 OK / 116 OK (con el ajuste de la línea 118) |

### Comparación de respuestas reales: versión anterior y versión nueva

Se levantaron las dos API **contra la misma base**: la imagen anterior (código de `3c0aae6`) en el puerto 8081 y esta rama en el 8080. Después de los humos, que dejan datos en todos los módulos, se enviaron las mismas 65 peticiones a ambas y se compararon estado HTTP y cuerpo. Salida: [comparacion-respuestas-2026-11-16.txt](comparacion-respuestas-2026-11-16.txt).

- **65/65 respuestas con el mismo estado y el mismo contenido JSON.**
- **35 idénticas byte a byte:** todas las lecturas de todos los módulos (usuarios, roles, proveedores, clientes, insumos, productos, fórmulas y sus detalles, lotes e insumos, pedidos, despachos y sus detalles, stock, movimientos, kardex, recepciones y bitácora) y el CSV del kardex.
- **30 respuestas de error (400, 401, 403, 404 y 409) solo cambian el orden de `codigo` y `mensaje`.** Esos cuerpos se arman con `Map.of`, cuyo orden de iteración Java aleatoriza en cada arranque de la JVM. Pasa también en el 401 de `SecurityConfig`, que no cambió, y no tiene significado en JSON.
- Los casos de error ejercitan reglas sin modificar datos: Maíz protegido al editar o desactivar, el administrador que intenta quitarse el rol o desactivarse, rol desconocido, ajuste con dos ítems o sin ítem, insumo de la ruta distinto del cuerpo, fórmula o despacho inexistentes, lote de 750 kg, rutas sin permiso, login con clave incorrecta y petición sin token.
