-- =============================================================
-- MOLISELVA - MIGRACIÓN 003
-- PO-01: usuario que registra la recepción
-- PostgreSQL 16
-- Fecha: 09-11-2026
--
-- Propósito:
--   Agregar recepcion.id_registra para identificar al usuario que
--   registró la carga. No reemplaza id_autoriza: ese campo se usa
--   únicamente cuando el jefe de planta autoriza una recepción pendiente.
--
-- Compatibilidad:
--   La columna se agrega nullable para no inventar el usuario de registros
--   históricos ya existentes. El backend debe enviar id_registra en todas
--   las recepciones nuevas. En instalaciones nuevas, 001_moliselva.sql
--   ya define id_registra como NOT NULL.
-- =============================================================

BEGIN;

SET search_path TO moliselva, public;

ALTER TABLE recepcion
    ADD COLUMN IF NOT EXISTS id_registra BIGINT;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_recepcion_id_registra'
          AND conrelid = 'moliselva.recepcion'::regclass
    ) THEN
        ALTER TABLE recepcion
            ADD CONSTRAINT fk_recepcion_id_registra
            FOREIGN KEY (id_registra)
            REFERENCES usuario(id_usuario);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS ix_recepcion_registra_fecha
    ON recepcion(id_registra, fecha_hora);

COMMENT ON COLUMN recepcion.id_registra IS
'Usuario que registró la recepción. Para registros nuevos debe ser informado por el backend; puede ser NULL solo en registros históricos anteriores a la migración 003.';

COMMIT;

-- Verificación sugerida:
-- SELECT column_name, is_nullable, data_type
-- FROM information_schema.columns
-- WHERE table_schema='moliselva'
--   AND table_name='recepcion'
--   AND column_name='id_registra';
