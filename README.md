# SW E3 Aplicación Empresarial Moliselva — backend

Backend de la demo de **PO-01 Recepción y acondicionamiento de maíz** y su kardex, según `CONTEXTO_MOLISELVA.md`, sección 18. Corresponde a la tarea 10 (CE0231–CE0233). También expone API para maestros, fórmulas, lotes, pedidos y despachos; esos módulos son extensiones que no forman parte del escenario mínimo de la demo.

## Arquitectura y datos

Java 17, Spring Boot 3, API REST, Spring Security con JWT, JDBC y PostgreSQL 16. El código está dividido en `auth`, `proveedor`, `maestros`, `recepcion`, `operaciones`, `inventario` y `comun`. La recepción y su entrada al kardex, la finalización de un lote y la confirmación de un despacho se ejecutan cada una en **una transacción**.

### Recepción: estructura hexagonal

`recepcion/domain` contiene las reglas de peso, merma y humedad y los datos del proceso. `recepcion/application` divide el flujo en cinco casos de uso: registrar, decidir, corregir, eliminar y consultar. Sus interfaces en `port/in` son las operaciones disponibles para la API; las de `port/out` expresan qué necesitan esos casos de uso de recepción, proveedores, inventario y bitácora. `adapter/in/web` convierte HTTP y DTO a esos datos; `adapter/out/jdbc` implementa los puertos de salida con las consultas SQL y los repositorios existentes.

La dirección de dependencias es `web → port/in → application → port/out ← jdbc`. Registrar y decidir mantienen en una sola transacción la recepción, su movimiento de inventario cuando corresponde y la bitácora. Las rutas y los DTO públicos de recepción no cambiaron.

### Producción por lote: estructura hexagonal

`produccion/domain` define el lote, sus insumos, estados y reglas. `produccion/application` contiene los casos de uso para consultar, planificar, gestionar insumos, cambiar estado y finalizar. Los puertos de salida expresan las operaciones necesarias de lotes, catálogo, inventario y bitácora; `adapter/out/jdbc` las implementa. `adapter/in/web` conserva `/api/lotes` y los DTO de Swagger. `OperacionService` continúa atendiendo fórmulas, pedidos y despachos.

La finalización bloquea el lote y, en una transacción, valida que tenga todos los insumos de la fórmula, descuenta la cantidad real (o la calculada si falta), registra el producto terminado, cambia el estado y escribe la bitácora. Las modificaciones de lotes e insumos bloquean el mismo registro para serializarse con la finalización. Las rutas y los campos JSON públicos no cambiaron.

La única definición de tablas activa es `../SW E3 Base de datos Moliselva/init/001_moliselva.sql` de Rómulo, con el esquema `moliselva`. La actualización `002_po01_merma_bitacora.sql` sirve para bases creadas antes del 26 de octubre; la instalación nueva ya contiene esos cambios. El backend no ejecuta Flyway ni crea tablas paralelas. Se conecta a `jdbc:postgresql://db:5432/moliselva_demo?currentSchema=moliselva` en Compose.

El `Dockerfile` construye la aplicación y ejecuta las pruebas de Maven. El Compose principal está en `../SW E3 Base de datos Moliselva/docker-compose.yml`; incluye `db` con healthcheck y `api` con dependencia a la base saludable. `backend/compose.yaml` usa los mismos servicios y volumen para el puente local de compilación.

## Arranque local

1. En `SW E3 Base de datos Moliselva/`, configurar `.env` con `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD`, `POSTGRES_PORT`, `JWT_SECRET` y `DEMO_PASSWORD`. Deben mantenerse fuera del repositorio. Este entorno de trabajo ya tiene un `.env` local con claves aleatorias de prueba; para consultar la contraseña demo, abrir ese archivo localmente. No usarla en producción.
2. Mantener `backend/` y `SW E3 Base de datos Moliselva/` como carpetas hermanas.
3. Desde `SW E3 Base de datos Moliselva/`, ejecutar `docker compose up -d --build`.
4. La API queda en `http://localhost:8080/api`. En una base nueva, PostgreSQL ejecuta los scripts `init/`. No usar `docker compose down -v` si se desean conservar datos.

Al iniciar, `DemoPasswordInitializer` reemplaza `HASH_DEMO_REEMPLAZAR` de los cuatro usuarios de prueba por un hash BCrypt de `DEMO_PASSWORD`. Solo modifica esas filas con marcador; no restablece contraseñas ya configuradas.

## Contrato API

### Probar con Swagger UI

Con los contenedores iniciados, abrir <http://localhost:8080/swagger-ui/index.html>.
Alli se pueden ejecutar las peticiones sin Postman:

1. Abrir `POST /api/auth/login`, pulsar **Try it out**, escribir `operario_demo` y la clave `DEMO_PASSWORD` de `SW E3 Base de datos Moliselva/.env`, y pulsar **Execute**.
2. Copiar solamente el valor `token` de la respuesta.
3. Pulsar **Authorize** (arriba a la derecha), pegar el token y confirmar. Swagger agrega el prefijo `Bearer` automaticamente.
4. Ejecutar las rutas protegidas con **Try it out**. Para las rutas de jefe o administrador, iniciar sesion con `jefe_demo` o `admin_demo` y actualizar el token en **Authorize**.

El contrato OpenAPI en JSON esta en <http://localhost:8080/v3/api-docs>. El login y la documentacion son publicos; los datos de la API mantienen sus permisos por rol.

Todos los JSON usan camelCase. Las fechas son locales ISO, los pesos están en kg y la humedad en %. Todas las rutas, salvo login, exigen `Authorization: Bearer <token>`. **El frontend debe enviar siempre `pesoSacosKg` y `mermaLimpiezaKg`; usar `0` cuando no apliquen.** Si se omiten, la API responde `400 DATOS_INVALIDOS`.

| Ruta | Roles | Resultado |
|---|---|---|
| `POST /api/auth/login` | Público | `{token, usuario}` |
| `GET /api/auth/me` | Cualquiera autenticado | Usuario actual |
| `GET /api/proveedores` | Los cuatro roles | Proveedores activos |
| `GET /api/proveedores/{id}` | Los cuatro roles | Detalle de proveedor activo |
| `POST /api/proveedores` | ADMINISTRADOR, JEFE_PLANTA | Crear proveedor, HTTP 201 |
| `PUT /api/proveedores/{id}` | ADMINISTRADOR, JEFE_PLANTA | Actualizar proveedor |
| `DELETE /api/proveedores/{id}` | ADMINISTRADOR, JEFE_PLANTA | Desactivar proveedor, HTTP 204 |
| `GET /api/clientes` y `/{id}` | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | Clientes activos y detalle |
| `GET /api/insumos`, `/api/productos` y `/{id}` | Los cuatro roles | Catálogos activos y detalle |
| `POST`, `PUT /{id}`, `DELETE /{id}` en esos catálogos | ADMINISTRADOR, JEFE_PLANTA | Crear, actualizar y desactivar |
| `POST /api/recepciones` | OPERARIO, JEFE_PLANTA | Recepción creada, HTTP 201 |
| `PATCH /api/recepciones/{id}/decision` | JEFE_PLANTA | Acepta o rechaza una PENDIENTE |
| `GET /api/recepciones?fecha=AAAA-MM-DD` | Los cuatro roles | Recepciones de la fecha; sin fecha usa hoy en Lima |
| `GET /api/recepciones/{id}` | Los cuatro roles | Detalle de recepción |
| `PUT`, `DELETE /api/recepciones/{id}` | OPERARIO, JEFE_PLANTA | Corregir o eliminar recepción sin decisión ni entrada al kardex |
| `GET /api/inventario/maiz/kardex` | Los cuatro roles | Movimientos y saldo |
| `GET /api/inventario/maiz/stock` | Los cuatro roles | `{stockKg}` |
| `GET /api/inventario/maiz/kardex/exportar` | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | CSV UTF-8 para Excel |
| `GET /api/inventario/stock`, `/stock/{tipo}/{id}` | Los cuatro roles | Stock de todos los insumos y productos |
| `GET /api/inventario/movimientos`, `/movimientos/{id}` | Los cuatro roles | Movimientos y detalle |
| `POST /api/inventario/movimientos/ajustes` | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | Ajuste de entrada o salida con motivo |
| `GET /api/formulas` y `/{id}`; detalles `/{id}/detalles` y `/{insumoId}` | JEFE_PLANTA | Fórmulas y proporciones confidenciales |
| `POST`, `PUT`, `DELETE` en fórmulas y detalles | JEFE_PLANTA | CRUD antes de usar la fórmula en un lote |
| `PATCH /api/formulas/{id}/vigencia` | JEFE_PLANTA | Retirar o restablecer vigencia, incluso si ya se usó |
| `GET /api/lotes` y `/{id}`; insumos `/{id}/insumos` y `/{insumoId}` | JEFE_PLANTA, OPERARIO | Lotes y materiales |
| `POST`, `PUT`, `DELETE` en lotes e insumos | JEFE_PLANTA | CRUD de lotes planificados |
| `PATCH /api/lotes/{id}/estado`, `POST /{id}/finalizar` | JEFE_PLANTA | Iniciar, anular o finalizar; finalizar mueve el kardex |
| `GET /api/pedidos`, `/api/despachos` y sus detalles | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | Consultas y detalle |
| `POST`, `PUT`, `DELETE` en pedidos, despachos y detalles | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | CRUD antes de atender o confirmar |
| `PATCH /api/pedidos/{id}/estado`, `POST /api/despachos/{id}/confirmar` | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | Preparar/cancelar pedido y confirmar salida |
| `GET /api/bitacora` y `/{id}` | ADMINISTRADOR, JEFE_PLANTA | Eventos recientes e individual |
| `GET /api/roles` y `/{id}` | ADMINISTRADOR | Roles fijos del sistema |
| `GET`, `POST /api/usuarios`; `GET`, `PUT`, `DELETE /{id}` | ADMINISTRADOR | CRUD de usuarios, con desactivación lógica |

Ejemplo de login:

```json
{"usuario":"operario_demo","password":"<clave local de DEMO_PASSWORD>"}
```

Para crear o actualizar un proveedor, enviar `{"razonSocial":"Proveedor de maíz SAC","documento":"20123456789","telefono":"987654321"}`. Solo `razonSocial` es obligatorio. El borrado es lógico: el proveedor deja de aparecer en las consultas y no se puede usar en nuevas recepciones, pero las recepciones históricas conservan su referencia. Las modificaciones quedan registradas en la bitácora.

Clientes, insumos, productos y usuarios también se desactivan lógicamente. No se puede desactivar el insumo `Maíz`, ni un insumo o producto con stock o referencias operativas activas, ni un cliente con pedidos abiertos. Una cuenta desactivada pierde acceso incluso si su JWT aún no venció; los permisos de un token usan el rol actual de la base de datos.

Ejemplo de recepción, **datos de prueba del contrato de la sección 18.4**:

```json
{"idProveedor":1,"pesoBrutoKg":32500,"taraKg":12500,"pesoSacosKg":0,"humedadPct":14.2,"mermaLimpiezaKg":150}
```

Devuelve `201`, `pesoNetoKg: 20000`, `estado: "ACEPTADA"` y `entradaKardexKg: 19850`. Entre 14.5% y 15% devuelve `PENDIENTE` sin entrada. Más de 15% devuelve `RECHAZADA` sin entrada. Para decidir una pendiente, el jefe envía `{"decision":"ACEPTAR","motivo":"Motivo de prueba"}` o `RECHAZAR` a la ruta PATCH. Repetir la decisión devuelve `409` y no crea otro movimiento.

Las correcciones de recepción solo son posibles antes de que exista una decisión del jefe o una entrada al kardex. El kardex y la bitácora son historiales inmutables por diseño: no tienen `PUT` ni `DELETE`. El stock es una vista calculada, no una tabla editable. Para corregir inventario se registra `AJUSTE_ENTRADA` o `AJUSTE_SALIDA` con `motivo`; la base impide stock negativo. Los nombres de los cuatro roles son fijos porque determinan los permisos del backend y no se crean ni eliminan mediante la API.

Para probar la extensión en Swagger, crear primero un producto e insumo, luego una fórmula y su detalle. Con la fórmula vigente, crear el lote de 500 o 1000 kg y sus insumos, cambiar su estado a `EN_PROCESO` y ejecutar `POST /api/lotes/{id}/finalizar`. El sistema descuenta insumos y registra el producto terminado. Para despacho, crear cliente, pedido y detalle; pasar el pedido a `EN_PREPARACION`, crear despacho y detalle, y ejecutar `POST /api/despachos/{id}/confirmar`. La confirmación valida las cantidades del pedido y el stock antes de registrar la salida. Los roles para estas extensiones son decisiones de acceso conservadoras; la matriz de 18.5 solo detalla PO-01.

Los errores usan `{"codigo":"...","mensaje":"..."}`. Un token ausente o vencido devuelve `401`; falta de rol, `403`. Una fecha mal escrita en el filtro devuelve `400 DATOS_INVALIDOS` con el formato esperado `AAAA-MM-DD`. Los errores inesperados devuelven `500 ERROR_INTERNO` sin detalles internos en el cuerpo; la excepción queda en el log del servidor. El operador no puede decidir ni exportar. La bitácora guarda usuario, acción, fecha y motivo, sin tokens ni contraseñas. CORS permite `http://localhost:5173` y expone `Content-Disposition` para que el frontend lea el nombre del CSV.

## Verificación

- `docker compose build` compila Java y ejecuta las pruebas unitarias de reglas y casos de uso.
- `backend/smoke-po01.ps1` prueba por HTTP los cuatro logins, recepción normal, pendiente y rechazada, autorización y duplicado, stock, kardex, exportación y permisos. Usa solo datos de prueba locales y crea registros en la base de la demo.
- `backend/smoke-crud.ps1` recorre los CRUD de usuarios, clientes, insumos, productos, fórmulas, lotes, pedidos, despachos y sus detalles; prueba corrección de recepción, ajustes de inventario, estados, permisos y revocación de token. Crea datos de prueba y deja los movimientos históricos inmutables.
- Desde `backend/`, ejecutar `pwsh -NoProfile -File .\smoke-crud.ps1` o `pwsh -NoProfile -File .\smoke-po01.ps1`. Ambos leen `DEMO_PASSWORD` del `.env` local, o del `.env` de la carpeta hermana de la base de datos.
- `backend/Moliselva.postman_collection.json` contiene los endpoints de la sección 18.4. Configurar la variable `demoPassword` en Postman con la clave local; no exportar esa variable con la colección si se comparte.

**Pendiente de la tarea 9:** Rómulo todavía no incorporó `recepcion.id_registra`. En el `POST`, `registradoPor` es el usuario autenticado; en las listas queda `null`. La bitácora registra quién creó cada recepción. Cuando se añada la columna, habrá que actualizar el repository y el modelo de datos de la tarea 6.
