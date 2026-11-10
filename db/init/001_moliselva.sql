-- =============================================================
-- MOLISELVA - PERFIL DE EGRESO 2026
-- SW E3 - Tarea 9: Base de datos de la demo
-- Motor objetivo: PostgreSQL 16
-- Actualización: 09-11-2026 (id_registra en recepcion)
-- Responsable: Rómulo
-- Fecha: 26-10-2026 (actualización PO-01)
--
-- IMPORTANTE:
-- 1) Este script crea un esquema limpio llamado "moliselva".
-- 2) Los datos iniciales de producto terminado se basan en los
--    registros NO AMBIGUOS del formato de inventario del 17-09-2026.
-- 3) No se incluyen porcentajes ni cantidades reales de fórmulas.
-- 4) Los usuarios, proveedor y cliente con sufijo DEMO son datos de
--    prueba y no representan personas o entidades reales de Moliselva.
-- =============================================================

BEGIN;

DROP SCHEMA IF EXISTS moliselva CASCADE;
CREATE SCHEMA moliselva;
SET search_path TO moliselva, public;

-- =============================================================
-- 1. SEGURIDAD Y MAESTROS
-- =============================================================

CREATE TABLE rol (
    id_rol          SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(40) NOT NULL UNIQUE,
    descripcion     VARCHAR(200)
);

CREATE TABLE usuario (
    id_usuario      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_rol          SMALLINT NOT NULL REFERENCES rol(id_rol),
    nombres         VARCHAR(120) NOT NULL,
    usuario         VARCHAR(60) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    creado_en       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Bitácora mínima para evidencias de seguridad y trazabilidad de acciones.
CREATE TABLE bitacora (
    id_bitacora     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_usuario      BIGINT NOT NULL REFERENCES usuario(id_usuario),
    fecha_hora      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accion          VARCHAR(160) NOT NULL,
    motivo          VARCHAR(300)
);

CREATE TABLE proveedor (
    id_proveedor    BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    razon_social    VARCHAR(160) NOT NULL,
    documento       VARCHAR(30),
    telefono        VARCHAR(30),
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE cliente (
    id_cliente              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tipo_cliente            VARCHAR(30) NOT NULL
                            CHECK (tipo_cliente IN ('REVENDEDOR','CRIADOR_TECNIFICADO','OTRO')),
    nombre_razon_social     VARCHAR(160) NOT NULL,
    documento               VARCHAR(30),
    telefono                VARCHAR(30),
    zona                    VARCHAR(120),
    activo                  BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE insumo (
    id_insumo       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre          VARCHAR(120) NOT NULL UNIQUE,
    unidad_medida   VARCHAR(20) NOT NULL DEFAULT 'kg',
    activo          BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE producto (
    id_producto         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nombre              VARCHAR(160) NOT NULL UNIQUE,
    especie             VARCHAR(60),
    fase                VARCHAR(60),
    linea_comercial     VARCHAR(30),
    presentacion        VARCHAR(80) NOT NULL DEFAULT 'Saco de 40 kg',
    activo              BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_producto_linea
        CHECK (linea_comercial IS NULL OR linea_comercial IN ('GJA','MCDO','OTRA'))
);

-- =============================================================
-- 2. RECEPCIÓN
-- =============================================================

CREATE TABLE recepcion (
    id_recepcion        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_proveedor        BIGINT NOT NULL REFERENCES proveedor(id_proveedor),
    id_insumo           BIGINT NOT NULL REFERENCES insumo(id_insumo),
    id_registra         BIGINT NOT NULL REFERENCES usuario(id_usuario),
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    peso_bruto_kg       NUMERIC(12,2) NOT NULL CHECK (peso_bruto_kg > 0),
    tara_kg             NUMERIC(12,2) NOT NULL CHECK (tara_kg >= 0),
    peso_sacos_kg       NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (peso_sacos_kg >= 0),
    peso_neto_kg        NUMERIC(12,2) GENERATED ALWAYS AS
                        (peso_bruto_kg - tara_kg - peso_sacos_kg) STORED,
    merma_limpieza_kg   NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (merma_limpieza_kg >= 0),
    humedad_pct         NUMERIC(5,2) NOT NULL CHECK (humedad_pct BETWEEN 0 AND 100),
    estado              VARCHAR(20) NOT NULL
                        CHECK (estado IN ('PENDIENTE','ACEPTADA','RECHAZADA')),
    id_autoriza         BIGINT REFERENCES usuario(id_usuario),
    CONSTRAINT ck_recepcion_peso_neto CHECK (peso_bruto_kg - tara_kg - peso_sacos_kg > 0),
    CONSTRAINT ck_recepcion_merma CHECK (
        merma_limpieza_kg < (peso_bruto_kg - tara_kg - peso_sacos_kg)
    ),
    CONSTRAINT ck_recepcion_humedad_aceptada CHECK (
        estado <> 'ACEPTADA'
        OR humedad_pct <= 14.50
        OR (humedad_pct <= 15.00 AND id_autoriza IS NOT NULL)
    )
);

-- =============================================================
-- 3. PRODUCCIÓN Y FÓRMULAS
-- =============================================================

CREATE TABLE formula (
    id_formula       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_producto      BIGINT NOT NULL REFERENCES producto(id_producto),
    version          VARCHAR(40) NOT NULL,
    vigente          BOOLEAN NOT NULL DEFAULT TRUE,
    id_creador       BIGINT NOT NULL REFERENCES usuario(id_usuario),
    fecha_creacion   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (id_producto, version)
);

CREATE TABLE formula_detalle (
    id_formula                  BIGINT NOT NULL REFERENCES formula(id_formula) ON DELETE CASCADE,
    id_insumo                   BIGINT NOT NULL REFERENCES insumo(id_insumo),
    proporcion_confidencial     NUMERIC(8,4) NOT NULL
                                CHECK (proporcion_confidencial > 0 AND proporcion_confidencial <= 100),
    PRIMARY KEY (id_formula, id_insumo)
);

CREATE TABLE lote (
    id_lote             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_producto         BIGINT NOT NULL REFERENCES producto(id_producto),
    id_formula          BIGINT NOT NULL REFERENCES formula(id_formula),
    fecha_hora          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tamano_kg           INTEGER NOT NULL CHECK (tamano_kg IN (500,1000)),
    estado              VARCHAR(20) NOT NULL DEFAULT 'PLANIFICADO'
                        CHECK (estado IN ('PLANIFICADO','EN_PROCESO','TERMINADO','ANULADO')),
    id_responsable      BIGINT NOT NULL REFERENCES usuario(id_usuario)
);

CREATE TABLE lote_insumo (
    id_lote                 BIGINT NOT NULL REFERENCES lote(id_lote) ON DELETE CASCADE,
    id_insumo               BIGINT NOT NULL REFERENCES insumo(id_insumo),
    cantidad_calculada_kg   NUMERIC(12,3) NOT NULL CHECK (cantidad_calculada_kg > 0),
    cantidad_real_kg        NUMERIC(12,3) CHECK (cantidad_real_kg IS NULL OR cantidad_real_kg > 0),
    PRIMARY KEY (id_lote, id_insumo)
);

-- =============================================================
-- 4. PEDIDOS Y DESPACHO
-- =============================================================

CREATE TABLE pedido (
    id_pedido        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_cliente       BIGINT NOT NULL REFERENCES cliente(id_cliente),
    fecha_hora       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    estado           VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE'
                     CHECK (estado IN ('PENDIENTE','EN_PREPARACION','ATENDIDO','CANCELADO')),
    observacion      VARCHAR(300)
);

CREATE TABLE pedido_detalle (
    id_pedido        BIGINT NOT NULL REFERENCES pedido(id_pedido) ON DELETE CASCADE,
    id_producto      BIGINT NOT NULL REFERENCES producto(id_producto),
    cantidad_sacos   INTEGER NOT NULL CHECK (cantidad_sacos > 0),
    cantidad_kg      NUMERIC(12,2) NOT NULL CHECK (cantidad_kg > 0),
    PRIMARY KEY (id_pedido, id_producto)
);

CREATE TABLE despacho (
    id_despacho      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    id_pedido        BIGINT NOT NULL REFERENCES pedido(id_pedido),
    fecha_hora       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_entrega     VARCHAR(20) NOT NULL CHECK (tipo_entrega IN ('RECOJO_PLANTA','REPARTO')),
    destino          VARCHAR(160),
    estado           VARCHAR(20) NOT NULL DEFAULT 'BORRADOR'
                     CHECK (estado IN ('BORRADOR','CONFIRMADO','ANULADO')),
    id_usuario       BIGINT NOT NULL REFERENCES usuario(id_usuario)
);

CREATE TABLE despacho_detalle (
    id_despacho      BIGINT NOT NULL REFERENCES despacho(id_despacho) ON DELETE CASCADE,
    id_producto      BIGINT NOT NULL REFERENCES producto(id_producto),
    cantidad_sacos   INTEGER NOT NULL CHECK (cantidad_sacos > 0),
    cantidad_kg      NUMERIC(12,2) NOT NULL CHECK (cantidad_kg > 0),
    PRIMARY KEY (id_despacho, id_producto)
);

-- =============================================================
-- 5. KARDEX / INVENTARIO
-- =============================================================

CREATE TABLE movimiento_inventario (
    id_movimiento        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fecha_hora           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_movimiento      VARCHAR(20) NOT NULL
                         CHECK (tipo_movimiento IN ('ENTRADA','SALIDA','AJUSTE_ENTRADA','AJUSTE_SALIDA')),
    id_insumo            BIGINT REFERENCES insumo(id_insumo),
    id_producto          BIGINT REFERENCES producto(id_producto),
    cantidad             NUMERIC(14,3) NOT NULL CHECK (cantidad > 0),
    unidad_medida        VARCHAR(20) NOT NULL DEFAULT 'kg',
    id_recepcion         BIGINT REFERENCES recepcion(id_recepcion),
    id_lote              BIGINT REFERENCES lote(id_lote),
    id_despacho          BIGINT REFERENCES despacho(id_despacho),
    id_usuario           BIGINT NOT NULL REFERENCES usuario(id_usuario),
    CONSTRAINT ck_mov_item_exclusivo CHECK (
        (id_insumo IS NOT NULL AND id_producto IS NULL)
        OR (id_insumo IS NULL AND id_producto IS NOT NULL)
    ),
    CONSTRAINT ck_mov_un_origen_max CHECK (
        (CASE WHEN id_recepcion IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN id_lote IS NOT NULL THEN 1 ELSE 0 END) +
        (CASE WHEN id_despacho IS NOT NULL THEN 1 ELSE 0 END) <= 1
    )
);

CREATE INDEX ix_recepcion_fecha ON recepcion(fecha_hora);
CREATE INDEX ix_recepcion_registra_fecha ON recepcion(id_registra, fecha_hora);
CREATE INDEX ix_bitacora_fecha ON bitacora(fecha_hora);
CREATE INDEX ix_lote_fecha ON lote(fecha_hora);
CREATE INDEX ix_pedido_fecha ON pedido(fecha_hora);
CREATE INDEX ix_despacho_fecha ON despacho(fecha_hora);
CREATE INDEX ix_mov_insumo_fecha ON movimiento_inventario(id_insumo, fecha_hora);
CREATE INDEX ix_mov_producto_fecha ON movimiento_inventario(id_producto, fecha_hora);
CREATE INDEX ix_mov_origen_recepcion ON movimiento_inventario(id_recepcion) WHERE id_recepcion IS NOT NULL;
-- Defensa adicional de idempotencia: una recepción aceptada genera una sola ENTRADA al kardex.
CREATE UNIQUE INDEX ux_mov_entrada_recepcion
ON movimiento_inventario(id_recepcion)
WHERE id_recepcion IS NOT NULL AND tipo_movimiento='ENTRADA';
CREATE INDEX ix_mov_origen_lote ON movimiento_inventario(id_lote) WHERE id_lote IS NOT NULL;
CREATE INDEX ix_mov_origen_despacho ON movimiento_inventario(id_despacho) WHERE id_despacho IS NOT NULL;

-- =============================================================
-- 6. VISTAS DE STOCK Y KARDEX
-- =============================================================

CREATE VIEW vw_stock_actual AS
SELECT
    'INSUMO'::VARCHAR(10) AS tipo_item,
    i.id_insumo AS id_item,
    i.nombre,
    i.unidad_medida,
    COALESCE(SUM(CASE
        WHEN m.tipo_movimiento IN ('ENTRADA','AJUSTE_ENTRADA') THEN m.cantidad
        WHEN m.tipo_movimiento IN ('SALIDA','AJUSTE_SALIDA') THEN -m.cantidad
        ELSE 0 END),0) AS stock_actual
FROM insumo i
LEFT JOIN movimiento_inventario m ON m.id_insumo = i.id_insumo
GROUP BY i.id_insumo, i.nombre, i.unidad_medida

UNION ALL

SELECT
    'PRODUCTO'::VARCHAR(10) AS tipo_item,
    p.id_producto AS id_item,
    p.nombre,
    'kg'::VARCHAR(20) AS unidad_medida,
    COALESCE(SUM(CASE
        WHEN m.tipo_movimiento IN ('ENTRADA','AJUSTE_ENTRADA') THEN m.cantidad
        WHEN m.tipo_movimiento IN ('SALIDA','AJUSTE_SALIDA') THEN -m.cantidad
        ELSE 0 END),0) AS stock_actual
FROM producto p
LEFT JOIN movimiento_inventario m ON m.id_producto = p.id_producto
GROUP BY p.id_producto, p.nombre;

CREATE VIEW vw_kardex AS
WITH base AS (
    SELECT
        m.*,
        CASE WHEN m.id_insumo IS NOT NULL THEN 'INSUMO' ELSE 'PRODUCTO' END AS tipo_item,
        COALESCE(m.id_insumo, m.id_producto) AS id_item,
        CASE
            WHEN m.tipo_movimiento IN ('ENTRADA','AJUSTE_ENTRADA') THEN m.cantidad
            ELSE -m.cantidad
        END AS cantidad_firmada
    FROM movimiento_inventario m
)
SELECT
    b.id_movimiento,
    b.fecha_hora,
    b.tipo_item,
    b.id_item,
    COALESCE(i.nombre,p.nombre) AS item,
    b.tipo_movimiento,
    b.cantidad,
    b.unidad_medida,
    b.cantidad_firmada,
    SUM(b.cantidad_firmada) OVER (
        PARTITION BY b.tipo_item, b.id_item
        ORDER BY b.fecha_hora, b.id_movimiento
        ROWS BETWEEN UNBOUNDED PRECEDING AND CURRENT ROW
    ) AS saldo,
    b.id_recepcion,
    b.id_lote,
    b.id_despacho,
    b.id_usuario
FROM base b
LEFT JOIN insumo i ON b.tipo_item='INSUMO' AND i.id_insumo=b.id_item
LEFT JOIN producto p ON b.tipo_item='PRODUCTO' AND p.id_producto=b.id_item;

-- =============================================================
-- 7. REGLA: NO PERMITIR STOCK NEGATIVO
--    El kardex es append-only: una corrección debe registrarse como
--    AJUSTE_ENTRADA o AJUSTE_SALIDA, no editando el historial.
-- =============================================================

CREATE OR REPLACE FUNCTION fn_validar_stock_no_negativo()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
DECLARE
    v_stock NUMERIC(14,3);
BEGIN
    IF NEW.tipo_movimiento IN ('SALIDA','AJUSTE_SALIDA') THEN
        IF NEW.id_insumo IS NOT NULL THEN
            SELECT COALESCE(SUM(CASE
                WHEN tipo_movimiento IN ('ENTRADA','AJUSTE_ENTRADA') THEN cantidad
                ELSE -cantidad END),0)
            INTO v_stock
            FROM movimiento_inventario
            WHERE id_insumo = NEW.id_insumo;
        ELSE
            SELECT COALESCE(SUM(CASE
                WHEN tipo_movimiento IN ('ENTRADA','AJUSTE_ENTRADA') THEN cantidad
                ELSE -cantidad END),0)
            INTO v_stock
            FROM movimiento_inventario
            WHERE id_producto = NEW.id_producto;
        END IF;

        IF v_stock < NEW.cantidad THEN
            RAISE EXCEPTION 'Stock insuficiente. Disponible: %, solicitado: %', v_stock, NEW.cantidad;
        END IF;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_validar_stock_no_negativo
BEFORE INSERT ON movimiento_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_validar_stock_no_negativo();

CREATE OR REPLACE FUNCTION fn_kardex_inmutable()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    RAISE EXCEPTION 'Los movimientos de inventario no se editan ni eliminan; registre un ajuste compensatorio.';
END;
$$;

CREATE TRIGGER trg_kardex_no_update
BEFORE UPDATE OR DELETE ON movimiento_inventario
FOR EACH ROW
EXECUTE FUNCTION fn_kardex_inmutable();

-- La entrada de una recepción aceptada al kardex NO se genera con trigger.
-- La crea el backend Spring Boot en la misma transacción que deja la recepción
-- en estado ACEPTADA. La base de datos impide duplicar esa ENTRADA mediante
-- ux_mov_entrada_recepcion y conserva el kardex como historial inmutable.

-- =============================================================
-- 8. PROCEDIMIENTO DE CONFIRMACIÓN DE DESPACHO
--    Valida stock y registra la salida en el kardex.
-- =============================================================

CREATE OR REPLACE FUNCTION confirmar_despacho(
    p_id_despacho BIGINT,
    p_id_usuario BIGINT
)
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    r RECORD;
    v_estado VARCHAR(20);
BEGIN
    SELECT estado INTO v_estado
    FROM despacho
    WHERE id_despacho = p_id_despacho
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Despacho % no existe', p_id_despacho;
    END IF;

    IF v_estado <> 'BORRADOR' THEN
        RAISE EXCEPTION 'El despacho % no está en estado BORRADOR', p_id_despacho;
    END IF;

    IF NOT EXISTS (SELECT 1 FROM despacho_detalle WHERE id_despacho=p_id_despacho) THEN
        RAISE EXCEPTION 'El despacho % no tiene detalle', p_id_despacho;
    END IF;

    FOR r IN
        SELECT id_producto, cantidad_kg
        FROM despacho_detalle
        WHERE id_despacho = p_id_despacho
        ORDER BY id_producto
    LOOP
        INSERT INTO movimiento_inventario(
            tipo_movimiento, id_producto, cantidad, unidad_medida,
            id_despacho, id_usuario
        ) VALUES (
            'SALIDA', r.id_producto, r.cantidad_kg, 'kg',
            p_id_despacho, p_id_usuario
        );
    END LOOP;

    UPDATE despacho
    SET estado='CONFIRMADO', id_usuario=p_id_usuario, fecha_hora=CURRENT_TIMESTAMP
    WHERE id_despacho=p_id_despacho;

    UPDATE pedido p
    SET estado='ATENDIDO'
    FROM despacho d
    WHERE d.id_despacho=p_id_despacho
      AND p.id_pedido=d.id_pedido;
END;
$$;

-- =============================================================
-- 9. DATOS DE PRUEBA
-- =============================================================

INSERT INTO rol(nombre,descripcion) VALUES
('ADMINISTRADOR','Consulta global y gestión de usuarios.'),
('JEFE_PLANTA','Gestión operativa y acceso restringido a fórmulas.'),
('OPERARIO','Registro operativo de recepción y producción.'),
('CONTADORA','Inventario, ventas, ajustes, reportes y exportaciones.');

INSERT INTO usuario(id_rol,nombres,usuario,password_hash)
SELECT id_rol,'Administrador DEMO','admin_demo','HASH_DEMO_REEMPLAZAR' FROM rol WHERE nombre='ADMINISTRADOR';
INSERT INTO usuario(id_rol,nombres,usuario,password_hash)
SELECT id_rol,'Jefe de planta DEMO','jefe_demo','HASH_DEMO_REEMPLAZAR' FROM rol WHERE nombre='JEFE_PLANTA';
INSERT INTO usuario(id_rol,nombres,usuario,password_hash)
SELECT id_rol,'Operario DEMO','operario_demo','HASH_DEMO_REEMPLAZAR' FROM rol WHERE nombre='OPERARIO';
INSERT INTO usuario(id_rol,nombres,usuario,password_hash)
SELECT id_rol,'Contadora DEMO','contadora_demo','HASH_DEMO_REEMPLAZAR' FROM rol WHERE nombre='CONTADORA';

INSERT INTO proveedor(razon_social,documento,telefono)
VALUES ('PROVEEDOR_DE_PRUEBA',NULL,NULL);

INSERT INTO cliente(tipo_cliente,nombre_razon_social,documento,telefono,zona)
VALUES ('REVENDEDOR','CLIENTE_DE_PRUEBA',NULL,NULL,'DATO_DE_PRUEBA');

INSERT INTO insumo(nombre,unidad_medida) VALUES
('Maíz','kg');

-- Productos con cantidades no ambiguas observadas en el formato del 17-09-2026.
INSERT INTO producto(nombre,especie,fase,linea_comercial,presentacion) VALUES
('Cerdo granja inicio','Cerdo','Inicio','GJA','Saco de 40 kg'),
('Cerdo granja crecimiento','Cerdo','Crecimiento','GJA','Saco de 40 kg'),
('Pollo granja inicio','Pollo','Inicio','GJA','Saco de 40 kg'),
('Pollo granja crecimiento','Pollo','Crecimiento','GJA','Saco de 40 kg'),
('Pollo granja engorde','Pollo','Engorde','GJA','Saco de 40 kg'),
('Cuy engorde','Cuy','Engorde',NULL,'Saco de 40 kg'),
('Conejo engorde','Conejo','Engorde',NULL,'Saco de 40 kg'),
('Cerdo mercado inicio','Cerdo','Inicio','MCDO','Saco de 40 kg'),
('Cerdo mercado crecimiento','Cerdo','Crecimiento','MCDO','Saco de 40 kg'),
('Cerdo mercado engorde','Cerdo','Engorde','MCDO','Saco de 40 kg'),
('Pollo mercado inicio','Pollo','Inicio','MCDO','Saco de 40 kg'),
('Pollo mercado crecimiento','Pollo','Crecimiento','MCDO','Saco de 40 kg'),
('Pollo mercado engorde','Pollo','Engorde','MCDO','Saco de 40 kg'),
('Cerdo gestación','Cerdo','Gestación',NULL,'Saco de 40 kg'),
('Engorde toro','Vacuno','Engorde',NULL,'Saco de 40 kg');

-- Carga inicial de inventario: 229 sacos no ambiguos x 40 kg = 9 160 kg.
-- Se omiten 16 sacos de dos registros ambiguos del formato original hasta validarlos.
WITH inventario(nombre_producto,sacos) AS (
    VALUES
    ('Cerdo granja inicio',22),
    ('Cerdo granja crecimiento',14),
    ('Pollo granja inicio',18),
    ('Pollo granja crecimiento',21),
    ('Pollo granja engorde',21),
    ('Cuy engorde',9),
    ('Conejo engorde',2),
    ('Cerdo mercado inicio',5),
    ('Cerdo mercado crecimiento',17),
    ('Cerdo mercado engorde',39),
    ('Pollo mercado inicio',9),
    ('Pollo mercado crecimiento',16),
    ('Pollo mercado engorde',14),
    ('Cerdo gestación',9),
    ('Engorde toro',13)
)
INSERT INTO movimiento_inventario(
    fecha_hora,tipo_movimiento,id_producto,cantidad,unidad_medida,id_usuario
)
SELECT
    TIMESTAMP '2026-09-17 08:00:00',
    'AJUSTE_ENTRADA',
    p.id_producto,
    i.sacos * 40.0,
    'kg',
    u.id_usuario
FROM inventario i
JOIN producto p ON p.nombre=i.nombre_producto
CROSS JOIN usuario u
WHERE u.usuario='admin_demo';

-- Cabecera de fórmula para demostrar la relación del modelo sin publicar composición.
INSERT INTO formula(id_producto,version,vigente,id_creador)
SELECT p.id_producto,'DEMO-SIN-COMPOSICION',FALSE,u.id_usuario
FROM producto p
CROSS JOIN usuario u
WHERE p.nombre='Cerdo granja inicio'
  AND u.usuario='jefe_demo';

-- Pedido y despacho BORRADOR de prueba. No descuenta stock mientras no se confirme.
INSERT INTO pedido(id_cliente,estado,observacion)
SELECT id_cliente,'PENDIENTE','DATO DE PRUEBA - no corresponde a una venta real'
FROM cliente WHERE nombre_razon_social='CLIENTE_DE_PRUEBA';

INSERT INTO pedido_detalle(id_pedido,id_producto,cantidad_sacos,cantidad_kg)
SELECT pe.id_pedido, pr.id_producto, 1, 40.0
FROM pedido pe
JOIN cliente c ON c.id_cliente=pe.id_cliente AND c.nombre_razon_social='CLIENTE_DE_PRUEBA'
JOIN producto pr ON pr.nombre='Cerdo granja inicio';

INSERT INTO despacho(id_pedido,tipo_entrega,destino,estado,id_usuario)
SELECT pe.id_pedido,'RECOJO_PLANTA','DATO_DE_PRUEBA','BORRADOR',u.id_usuario
FROM pedido pe
JOIN cliente c ON c.id_cliente=pe.id_cliente AND c.nombre_razon_social='CLIENTE_DE_PRUEBA'
CROSS JOIN usuario u
WHERE u.usuario='contadora_demo';

INSERT INTO despacho_detalle(id_despacho,id_producto,cantidad_sacos,cantidad_kg)
SELECT d.id_despacho,p.id_producto,1,40.0
FROM despacho d
JOIN pedido pe ON pe.id_pedido=d.id_pedido
JOIN cliente c ON c.id_cliente=pe.id_cliente AND c.nombre_razon_social='CLIENTE_DE_PRUEBA'
JOIN producto p ON p.nombre='Cerdo granja inicio';

COMMIT;

-- =============================================================
-- 10. CONSULTAS DE VERIFICACIÓN (ejecutar después del script)
-- =============================================================

-- Cantidad de tablas del esquema (debe devolver 17):
-- SELECT COUNT(*) FROM information_schema.tables
-- WHERE table_schema='moliselva' AND table_type='BASE TABLE';

-- Verificar columnas PO-01 y bitácora:
-- SELECT column_name FROM information_schema.columns
-- WHERE table_schema='moliselva' AND table_name='recepcion'
--   AND column_name='merma_limpieza_kg';
-- SELECT COUNT(*) FROM moliselva.bitacora;


-- Decisión acordada para PO-01:
-- el backend calcula cantidad_kardex = peso_neto_kg - merma_limpieza_kg y,
-- dentro de una misma transacción, actualiza la recepción a ACEPTADA e inserta
-- el movimiento ENTRADA con id_recepcion. Si se repite, el índice único evita duplicarlo.

-- Stock inicial de productos:
-- SELECT * FROM moliselva.vw_stock_actual
-- WHERE tipo_item='PRODUCTO'
-- ORDER BY nombre;

-- Total cargado desde registros no ambiguos del formato del 17-09-2026 (debe ser 9160 kg):
-- SELECT SUM(stock_actual) FROM moliselva.vw_stock_actual WHERE tipo_item='PRODUCTO';

-- Confirmar el despacho de prueba (descuenta 40 kg del producto):
-- SELECT moliselva.confirmar_despacho(
--   (SELECT id_despacho FROM moliselva.despacho ORDER BY id_despacho LIMIT 1),
--   (SELECT id_usuario FROM moliselva.usuario WHERE usuario='contadora_demo')
-- );

-- Ver el kardex del producto despachado:
-- SELECT * FROM moliselva.vw_kardex
-- WHERE item='Cerdo granja inicio'
-- ORDER BY fecha_hora,id_movimiento;

-- PRUEBA NEGATIVA: intentar una salida superior al stock debe fallar.
-- INSERT INTO moliselva.movimiento_inventario(
--   tipo_movimiento,id_producto,cantidad,unidad_medida,id_usuario
-- ) VALUES (
--   'SALIDA',
--   (SELECT id_producto FROM moliselva.producto WHERE nombre='Cerdo granja inicio'),
--   999999,'kg',
--   (SELECT id_usuario FROM moliselva.usuario WHERE usuario='admin_demo')
-- );

-- PRUEBA NEGATIVA: una recepción ACEPTADA con 14.7% y sin autorización debe fallar.
-- INSERT INTO moliselva.recepcion(
--   id_proveedor,id_insumo,peso_bruto_kg,tara_kg,peso_sacos_kg,humedad_pct,estado
-- ) VALUES (
--   (SELECT id_proveedor FROM moliselva.proveedor WHERE razon_social='PROVEEDOR_DE_PRUEBA'),
--   (SELECT id_insumo FROM moliselva.insumo WHERE nombre='Maíz'),
--   1000,200,0,14.70,'ACEPTADA'
-- );
