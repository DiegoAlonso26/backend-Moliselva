# Contrato de API · Moliselva

Contrato consumido por el frontend de la Tarea 11 para el proceso **PO-01 Recepción y acondicionamiento de maíz**. El cliente está implementado en `src/po01/http.ts` y el modo demo replica el mismo contrato en `src/po01/mock.ts`.

## 1. Configuración

- Variable del frontend: `VITE_API_URL`.
- Valor local: `http://localhost:8080/api`.
- Sin la variable, o con `VITE_USAR_MOCKS=true`, la aplicación usa el modo demo con `localStorage`.
- Formato: JSON UTF-8.
- Fechas: ISO 8601 con hora local de Lima, por ejemplo `2026-10-27T16:15:00-05:00`.
- Pesos: kilogramos con decimales.
- Origen permitido por CORS: `http://localhost:5173`.

## 2. Autenticación

### POST `/auth/login`

Solicitud:

```json
{
  "usuario": "jefe_demo",
  "password": "MoliselvaDemo2026!"
}
```

Respuesta `200`:

```json
{
  "token": "jwt-firmado",
  "usuario": {
    "id": 2,
    "nombres": "María Quispe",
    "usuario": "jefe_demo",
    "rol": "JEFE_PLANTA"
  }
}
```

El token se envía en todas las rutas protegidas:

```http
Authorization: Bearer <token>
```

La sesión se guarda en `sessionStorage` con la clave `moliselva_session`.

## 3. Proveedores

### GET `/proveedores`

Respuesta `200`:

```json
[
  { "idProveedor": 1, "razonSocial": "Agropecuaria del Huallaga S.A.C." },
  { "idProveedor": 2, "razonSocial": "Cooperativa Agraria San Martín" }
]
```

## 4. Recepciones

### GET `/recepciones?fecha=AAAA-MM-DD`

Sin `fecha`, el backend usa la fecha actual de Lima. Respuesta `200`:

```json
[
  {
    "idRecepcion": 5,
    "fechaHora": "2026-10-27T16:15:00-05:00",
    "proveedor": "Cooperativa Agraria San Martín",
    "pesoBrutoKg": 34200,
    "taraKg": 13000,
    "pesoSacosKg": 0,
    "pesoNetoKg": 21200,
    "humedadPct": 14.7,
    "mermaLimpiezaKg": 180,
    "estado": "PENDIENTE",
    "entradaKardexKg": null,
    "registradoPor": null,
    "autorizadoPor": null
  }
]
```

`registradoPor` puede ser `null` porque el esquema de la Tarea 9 aún no expone la columna de usuario que registró la recepción. La interfaz oculta esa columna cuando todos los registros vienen vacíos.

### POST `/recepciones`

Roles: `JEFE_PLANTA` y `OPERARIO`.

```json
{
  "idProveedor": 1,
  "pesoBrutoKg": 32500,
  "taraKg": 12500,
  "pesoSacosKg": 0,
  "humedadPct": 14.2,
  "mermaLimpiezaKg": 150
}
```

Respuesta `201`: recepción creada con `estado: "ACEPTADA"`, `pesoNetoKg: 20000` y `entradaKardexKg: 19850`.

La clasificación por humedad es:

| Humedad | Estado | Kardex |
| --- | --- | --- |
| `<= 14.5%` | `ACEPTADA` | Entrada inmediata |
| `> 14.5%` y `<= 15%` | `PENDIENTE` | Sin movimiento |
| `> 15%` | `RECHAZADA` | Sin movimiento |

### PATCH `/recepciones/{id}/decision`

Rol: `JEFE_PLANTA`.

```json
{
  "decision": "ACEPTAR",
  "motivo": "Falta de maíz en planta"
}
```

`decision` admite `ACEPTAR` o `RECHAZAR`. El motivo es obligatorio. Solo se puede decidir una recepción en estado `PENDIENTE`; un segundo intento devuelve `409`. Al aceptar se registra la entrada al kardex con `pesoNetoKg - mermaLimpiezaKg`.

## 5. Kardex e inventario de maíz

### GET `/inventario/maiz/kardex`

Respuesta `200`, ordenada de la más antigua a la más reciente:

```json
[
  {
    "idMovimiento": 16,
    "fechaHora": "2026-10-27T15:50:00-05:00",
    "tipoMovimiento": "ENTRADA",
    "cantidadKg": 19850,
    "saldoKg": 19850,
    "idRecepcion": 1,
    "usuario": "operario_demo"
  }
]
```

Tipos posibles: `ENTRADA`, `AJUSTE_ENTRADA`, `AJUSTE_SALIDA`.

### GET `/inventario/maiz/stock`

```json
{ "stockKg": 63560 }
```

### Exportación

El frontend genera el CSV en el navegador desde el libro cargado. El backend también expone `GET /inventario/maiz/kardex/exportar` para `ADMINISTRADOR`, `JEFE_PLANTA` y `CONTADORA`.

## 6. Errores

Formato:

```json
{
  "codigo": "DATOS_INVALIDOS",
  "mensaje": "El motivo de la decisión es obligatorio."
}
```

| Código HTTP | Uso |
| --- | --- |
| `400` | Datos inválidos o fecha mal formada |
| `401` | Token ausente o vencido |
| `403` | Rol sin permiso |
| `404` | Recurso inexistente |
| `409` | Conflicto de estado, por ejemplo decidir dos veces |
| `500` | Error interno sin detalles sensibles |

## 7. Permisos por rol

| Acción | ADMINISTRADOR | JEFE_PLANTA | OPERARIO | CONTADORA |
| --- | --- | --- | --- | --- |
| Consultar recepciones | Sí | Sí | Sí | Sí |
| Registrar recepción | No | Sí | Sí | No |
| Decidir recepción | No | Sí | No | No |
| Consultar kardex y stock | Sí | Sí | Sí | Sí |
| Exportar kardex | Sí | Sí | No | Sí |

El backend valida el rol en cada solicitud; la interfaz solo oculta controles para evitar acciones no permitidas.
