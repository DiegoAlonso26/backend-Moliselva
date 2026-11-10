# Tarea 13B: id_registra y Compose autocontenido

Fecha: 10 de noviembre de 2026, hora de Lima. Rama: `feat/id-registra` (desde `main` en `ebfdf3e`). Guía: `GUIA_TAREA13B_ID_REGISTRA_Y_COMPOSE.md`.

Ninguna salida de esta evidencia contiene `DEMO_PASSWORD`, `POSTGRES_PASSWORD`, `JWT_SECRET` ni tokens. El `.env` usado fue local, con valores aleatorios, y no se sube. Antes de guardar se buscaron en las salidas los valores de ese `.env` y el prefijo `eyJ` de los JWT: sin coincidencias.

## 1. Pruebas automáticas

Comando (Java 17 y Maven dentro de Docker, sin Maven local):

```powershell
docker run --rm -e TZ=America/Lima -v "${PWD}:/app" -v moliselva-m2:/root/.m2 -w /app maven:3.9-eclipse-temurin-17 mvn -B verify
```

Resultado: `BUILD SUCCESS`. Totales de `target/surefire-reports`:

| Clase | Ejecutadas | Fallidas | Errores | Omitidas |
|---|---:|---:|---:|---:|
| `RecepcionPolicyTest` | 28 | 0 | 0 | 0 |
| `RecepcionUseCaseTest` | 13 (10 + 3 nuevas) | 0 | 0 | 0 |
| `Po01SecurityTest` (sin cambios) | 47 | 0 | 0 | 0 |
| `BusinessRulesTest` | 3 | 0 | 0 | 0 |
| `FinalizarLoteTest` | 3 | 0 | 0 | 0 |
| `ConfirmarDespachoTest` | 3 | 0 | 0 | 0 |
| **Total** | **97** (94 + 3) | **0** | **0** | **0** |

Pruebas nuevas en `RecepcionUseCaseTest`:

- `registrarEnviaElIdDelOperarioComoIdRegistra`: `insertar` recibe el `id` del operario (3).
- `registrarEnviaElIdDelJefeDePlantaComoIdRegistra`: `insertar` recibe el `id` del jefe de planta (2).
- `rolSinPermisoNoRegistraNiLlamaAInsertar`: la contadora recibe `SIN_PERMISO` y no se llama a `insertar`.

En las 3 pruebas existentes que simulan `insertar` solo se agregó el cuarto argumento (`3`, el `id` del operario) por la nueva firma. Ninguna aserción cambió.

## 2. Base nueva desde cero

Proyecto Compose aislado para no tocar el volumen de la demo (`moliselva-demo_postgres_data`):

```powershell
docker compose -p moliselva-idregistra down -v
docker compose -p moliselva-idregistra up -d --build
```

PostgreSQL ejecutó `001_moliselva.sql`, `002_po01_merma_bitacora.sql` y `003_recepcion_id_registra.sql` desde `./db/init`, sin carpetas hermanas.

Comprobaciones con `psql`:

| Consulta | Resultado |
|---|---|
| `information_schema.columns` de `recepcion.id_registra` | `id_registra \| NO \| bigint` (NOT NULL) |
| Tablas base del esquema `moliselva` | 17 |
| Claves foráneas de `recepcion` | `recepcion_id_autoriza_fkey`, `recepcion_id_insumo_fkey`, `recepcion_id_proveedor_fkey`, `recepcion_id_registra_fkey`, `fk_recepcion_id_registra` |
| `docker port` del servicio `db` | `5432/tcp -> 127.0.0.1:5432` (H-04) |

Pruebas de humo:

| Script | Resultado | Salida |
|---|---|---|
| `smoke-po01.ps1` | 51 comprobaciones OK (49 anteriores + 2 nuevas de `registradoPor`) | [smoke-po01-2026-11-10.txt](smoke-po01-2026-11-10.txt) |
| `smoke-crud.ps1` | 115 comprobaciones OK | [smoke-crud-2026-11-10.txt](smoke-crud-2026-11-10.txt) |

Comprobaciones nuevas de `smoke-po01.ps1`:

- `listado muestra registradoPor = operario_demo (id_registra)`: la recepción registrada por `operario_demo` aparece en `GET /api/recepciones?fecha=hoy` con `registradoPor = operario_demo`.
- `detalle separa registradoPor y autorizadoPor`: la recepción pendiente registrada por `operario_demo` y aceptada por `jefe_demo` devuelve `registradoPor = operario_demo` y `autorizadoPor = jefe_demo` en `GET /api/recepciones/{id}`.

## 3. Volumen antiguo más 003

1. Se creó la base con el `001` y el `002` anteriores (versión del 26 de octubre, sin `id_registra`), montados con un archivo de Compose temporal fuera del repositorio. Proyecto `moliselva-antiguo`, solo el servicio `db`.
2. Se insertó una recepción histórica (id 1, PENDIENTE) directamente por SQL, como la habría dejado el backend anterior.
3. Se aplicó `db/init/003_recepcion_id_registra.sql` con `psql` dos veces: la segunda no falló (idempotente). `id_registra` quedó nullable (`is_nullable = YES`).
4. Se levantó la API de esta rama y se ejecutó una comprobación por HTTP:

| Comprobación | Resultado |
|---|---|
| `POST /api/recepciones` como `operario_demo` | HTTP 201, `registradoPor = operario_demo` |
| La nueva recepción en `GET /api/recepciones?fecha=hoy` | `registradoPor = operario_demo` |
| La recepción histórica (id 1, `id_registra` NULL) en el listado | Aparece, `registradoPor = null` |
| `GET /api/recepciones/1` | `registradoPor = null` |

Salida: [base-antigua-003-2026-11-10.txt](base-antigua-003-2026-11-10.txt). Además, `smoke-po01.ps1` completo pasó contra esta base: [smoke-po01-base-antigua-2026-11-10.txt](smoke-po01-base-antigua-2026-11-10.txt).

Al terminar se ejecutó `down -v` en los dos proyectos de verificación. El volumen de la demo no se modificó.

## 4. Observación para Rómulo

En una base nueva, `001` ya crea la clave foránea `recepcion_id_registra_fkey` y luego `003` agrega `fk_recepcion_id_registra`, porque solo busca la restricción por nombre. Quedan dos claves foráneas iguales sobre `id_registra`. No afecta el funcionamiento; los scripts se copiaron sin modificar. Se sugiere darle nombre `fk_recepcion_id_registra` a la clave en `001` o hacer que `003` busque cualquier clave foránea sobre esa columna.
