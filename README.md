# SW E3 Aplicación Empresarial Moliselva — backend PO-01

Backend de la demo de **PO-01 Recepción y acondicionamiento de maíz** y su kardex, según `CONTEXTO_MOLISELVA.md`, sección 18. Corresponde a la tarea 10 (CE0231–CE0233). Los módulos de lote y despacho quedan fuera de la demo mínima acordada el 13 de octubre.

## Arquitectura y datos

Java 17, Spring Boot 3, API REST, Spring Security con JWT, JDBC y PostgreSQL 16. El código activo está dividido en `auth`, `proveedor`, `recepcion`, `inventario` y `comun`. Los módulos separan controller, service, repository y DTO. La validación de recepción y su entrada al kardex ocurren en **una transacción**.

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
| `POST /api/recepciones` | OPERARIO, JEFE_PLANTA | Recepción creada, HTTP 201 |
| `PATCH /api/recepciones/{id}/decision` | JEFE_PLANTA | Acepta o rechaza una PENDIENTE |
| `GET /api/recepciones?fecha=AAAA-MM-DD` | Los cuatro roles | Recepciones de la fecha; sin fecha usa hoy en Lima |
| `GET /api/inventario/maiz/kardex` | Los cuatro roles | Movimientos y saldo |
| `GET /api/inventario/maiz/stock` | Los cuatro roles | `{stockKg}` |
| `GET /api/inventario/maiz/kardex/exportar` | ADMINISTRADOR, JEFE_PLANTA, CONTADORA | CSV UTF-8 para Excel |
| `GET /api/bitacora` | ADMINISTRADOR, JEFE_PLANTA | Eventos recientes |
| `GET, POST /api/usuarios` | ADMINISTRADOR | Lista y alta de usuarios |

Ejemplo de login:

```json
{"usuario":"operario_demo","password":"<clave local de DEMO_PASSWORD>"}
```

Ejemplo de recepción, **datos de prueba del contrato de la sección 18.4**:

```json
{"idProveedor":1,"pesoBrutoKg":32500,"taraKg":12500,"pesoSacosKg":0,"humedadPct":14.2,"mermaLimpiezaKg":150}
```

Devuelve `201`, `pesoNetoKg: 20000`, `estado: "ACEPTADA"` y `entradaKardexKg: 19850`. Entre 14.5% y 15% devuelve `PENDIENTE` sin entrada. Más de 15% devuelve `RECHAZADA` sin entrada. Para decidir una pendiente, el jefe envía `{"decision":"ACEPTAR","motivo":"Motivo de prueba"}` o `RECHAZAR` a la ruta PATCH. Repetir la decisión devuelve `409` y no crea otro movimiento.

Los errores usan `{"codigo":"...","mensaje":"..."}`. Un token ausente o vencido devuelve `401`; falta de rol, `403`. Una fecha mal escrita en el filtro devuelve `400 DATOS_INVALIDOS` con el formato esperado `AAAA-MM-DD`. Los errores inesperados devuelven `500 ERROR_INTERNO` sin detalles internos en el cuerpo; la excepción queda en el log del servidor. El operador no puede decidir ni exportar. La bitácora guarda usuario, acción, fecha y motivo, sin tokens ni contraseñas. CORS permite `http://localhost:5173` y expone `Content-Disposition` para que el frontend lea el nombre del CSV.

## Verificación

- `docker compose build` compila Java y ejecuta `BusinessRulesTest`.
- `backend/smoke-po01.ps1` prueba por HTTP los cuatro logins, recepción normal, pendiente y rechazada, autorización y duplicado, stock, kardex, exportación y permisos. Usa solo datos de prueba locales y crea registros en la base de la demo.
- `backend/Moliselva.postman_collection.json` contiene los endpoints de la sección 18.4. Configurar la variable `demoPassword` en Postman con la clave local; no exportar esa variable con la colección si se comparte.

**Pendiente de la tarea 9:** Rómulo todavía no incorporó `recepcion.id_registra`. En el `POST`, `registradoPor` es el usuario autenticado; en las listas queda `null`. La bitácora registra quién creó cada recepción. Cuando se añada la columna, habrá que actualizar el repository y el modelo de datos de la tarea 6.
