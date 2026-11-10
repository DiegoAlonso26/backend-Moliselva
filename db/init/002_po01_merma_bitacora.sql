-- Moliselva - actualización PO-01 (26-10-2026)
-- Para bases creadas antes de incorporar merma de limpieza y bitácora.
-- Es idempotente: puede ejecutarse más de una vez.

SET search_path TO moliselva, public;

ALTER TABLE recepcion
    ADD COLUMN IF NOT EXISTS merma_limpieza_kg NUMERIC(12,2) NOT NULL DEFAULT 0;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'ck_recepcion_merma'
          AND conrelid = 'moliselva.recepcion'::regclass
    ) THEN
        ALTER TABLE recepcion
            ADD CONSTRAINT ck_recepcion_merma CHECK (
                merma_limpieza_kg >= 0
                AND merma_limpieza_kg < (peso_bruto_kg - tara_kg - peso_sacos_kg)
            );
    END IF;
END $$;

CREATE TABLE IF NOT EXISTS bitacora (
    id_bitacora     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT NOT NULL REFERENCES usuario(id_usuario),
    fecha_hora      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion          VARCHAR(160) NOT NULL,
    motivo          VARCHAR(300)
);

CREATE INDEX IF NOT EXISTS ix_bitacora_fecha ON bitacora(fecha_hora);

-- Una recepción aceptada debe generar una sola ENTRADA al kardex.
-- La creación del movimiento la realiza el backend en la misma transacción
-- que cambia la recepción a ACEPTADA.
CREATE UNIQUE INDEX IF NOT EXISTS ux_mov_entrada_recepcion
ON movimiento_inventario(id_recepcion)
WHERE id_recepcion IS NOT NULL AND tipo_movimiento='ENTRADA';

-- Verificación rápida
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_schema='moliselva'
  AND table_name='recepcion'
  AND column_name='merma_limpieza_kg';

SELECT to_regclass('moliselva.bitacora') AS tabla_bitacora;
