# Base de datos Moliselva (PostgreSQL 16)

Scripts de Rómulo copiados sin modificar desde `SW E3 Base de datos Moliselva.zip` (versión del 9 de noviembre de 2026). El servicio `db` de `compose.yaml` monta `db/init/` en `/docker-entrypoint-initdb.d`, por eso PostgreSQL los ejecuta en orden alfabético **solo cuando el volumen está vacío**.

| Script | Qué hace | Cuándo se usa |
|---|---|---|
| `init/001_moliselva.sql` | Crea el esquema `moliselva` completo (17 tablas), índices y datos iniciales. Ya incluye `recepcion.merma_limpieza_kg`, la bitácora y `recepcion.id_registra BIGINT NOT NULL` con clave foránea a `usuario`. | Base nueva desde cero. |
| `init/002_po01_merma_bitacora.sql` | Agrega la merma de limpieza y la bitácora de PO-01. Idempotente. | Bases creadas antes del 26 de octubre de 2026. En una base nueva no cambia nada. |
| `init/003_recepcion_id_registra.sql` | Agrega `recepcion.id_registra` como columna **nullable**, la clave `fk_recepcion_id_registra` y el índice `ix_recepcion_registra_fecha`. Idempotente. | Bases creadas antes del 9 de noviembre de 2026. Las recepciones históricas quedan con `id_registra` NULL; el backend lo informa en todas las nuevas. En una base nueva no cambia nada. |

`id_registra` es el usuario que registra la carga. `id_autoriza` sigue siendo solo el jefe de planta que decide una recepción PENDIENTE.

## Aplicar 003 a un volumen existente

Con los contenedores levantados, desde la raíz del repositorio:

```sh
docker compose exec db sh -lc 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -f /docker-entrypoint-initdb.d/003_recepcion_id_registra.sql'
```

## Empezar desde cero

`docker compose down -v` borra el volumen `postgres_data` y todos sus datos. Luego `docker compose up -d --build` vuelve a ejecutar los scripts.
