# Banco App

API REST en Java Spring Boot y aplicación web en Angular para gestionar clientes, cuentas y movimientos bancarios, con un reporte de estado de cuenta en JSON y PDF.

## Stack

| Capa | Tecnología |
| --- | --- |
| Backend | Java 21, Spring Boot 3.5, Spring Data JPA, Bean Validation, Maven, OpenPDF 3 |
| Base de datos | PostgreSQL 16 |
| Frontend | Angular 22 (standalone, signals, sin zone.js), SCSS propio sin frameworks de estilos ni componentes |
| Pruebas | JUnit 5, Mockito y MockMvc en el backend; Jest en el frontend; colección de Postman |
| Despliegue | Docker y Docker Compose (PostgreSQL, API y Nginx) |

## Levantar con Docker

Requisito: Docker con Docker Compose.

```bash
docker compose up -d --build
```

| Servicio | URL |
| --- | --- |
| Aplicación web | http://localhost:4200 |
| API | http://localhost:8080/api |
| PostgreSQL | localhost:5433 (base `banco`, usuario `banco_user`, clave `banco_clave`) |

La construcción de las imágenes corre todas las pruebas del backend y del frontend; si alguna falla, la imagen no se genera. La primera vez el contenedor de PostgreSQL ejecuta `BaseDatos.sql` y deja cargados los datos iniciales.

Para cambiar puertos, credenciales, el límite diario o la zona horaria, copia `.env.example` como `.env` y ajusta los valores. Si el puerto 8080 está ocupado en tu equipo:

```bash
BACKEND_PORT=8081 docker compose up -d --build
```

Para detener todo y borrar los datos (la próxima vez se vuelve a cargar `BaseDatos.sql`):

```bash
docker compose down -v
```

Nginx sirve la aplicación y reenvía `/api` al contenedor de la API, así el navegador ve un solo origen y no hace falta configurar CORS.

## Levantar sin Docker

Requisitos: Java 21, Maven 3.9, Node 24 y PostgreSQL 16 o superior.

1. Base de datos: ejecuta `CrearBaseDatos.sql` conectado a `postgres` y luego `BaseDatos.sql` conectado a `banco`.

   ```bash
   psql -d postgres -f CrearBaseDatos.sql
   psql -d banco -f BaseDatos.sql
   ```

2. API, en `backend`. Las credenciales se pasan por variables de entorno (por defecto `postgres`/`postgres`):

   ```bash
   DB_USER=mi_usuario DB_PASSWORD=mi_clave mvn spring-boot:run
   ```

   Variables disponibles: `DB_URL`, `DB_USER`, `DB_PASSWORD`, `SERVER_PORT` y `LIMITE_DIARIO_RETIRO`.

3. Frontend, en `frontend`. El servidor de desarrollo reenvía `/api` a `localhost:8080`:

   ```bash
   npm install
   npm start
   ```

   Abre http://localhost:4200.

## Pruebas

| Qué | Comando | Resultado |
| --- | --- | --- |
| Backend | `mvn test` en `backend` | 77 pruebas: estrategias, servicios y endpoints con MockMvc; no requieren base de datos |
| Frontend | `npm test` en `frontend` | 96 pruebas con Jest: utilidades, servicios HTTP, componentes y pantallas |
| Cobertura del frontend | `npm run test:coverage` en `frontend` | 90% de sentencias |
| API de punta a punta | Colección `docs/postman/banco-api.postman_collection.json` | 38 peticiones con 97 aserciones |

La colección de Postman (formato v2.1, compatible con Postman 9.13.2) se ejecuta en orden sobre una base recién cargada con `BaseDatos.sql`: crea un cliente y una cuenta de prueba, registra los movimientos del ejercicio, prueba los reportes y al final borra lo que creó. La variable `baseUrl` apunta a `http://localhost:8080/api`. También se puede correr desde la terminal:

```bash
npx newman run docs/postman/banco-api.postman_collection.json
```

## API

Todas las rutas cuelgan de `/api`.

| Recurso | Operaciones |
| --- | --- |
| `/clientes` | `GET` lista, `GET /{id}`, `POST`, `PUT /{id}`, `PATCH /{id}`, `DELETE /{id}` |
| `/cuentas` | Las mismas; `GET /cuentas?clienteId=jlema` filtra por cliente |
| `/movimientos` | Las mismas; `GET /movimientos?numeroCuenta=478758` filtra por cuenta |
| `/reportes` | `GET /reportes?fecha=2026-10-01,2026-10-31&clienteId=jlema&formato=json` (o `formato=pdf`) |

Ejemplos:

```bash
# Crear una cuenta para un cliente existente
curl -X POST http://localhost:8080/api/cuentas -H 'Content-Type: application/json' \
  -d '{"numeroCuenta":"585545","tipoCuenta":"CORRIENTE","saldoInicial":1000,"clienteId":"jlema"}'

# Retiro de 575: el valor se envía positivo y el tipo decide el signo
curl -X POST http://localhost:8080/api/movimientos -H 'Content-Type: application/json' \
  -d '{"numeroCuenta":"478758","tipoMovimiento":"DEBITO","valor":575}'

# Estado de cuenta en PDF (el PDF llega en base64 dentro del JSON)
curl 'http://localhost:8080/api/reportes?fecha=2026-10-01,2026-10-31&clienteId=jlema&formato=pdf'
```

Todos los errores responden con el mismo JSON:

```json
{
  "fecha": "2026-10-01T10:00:00",
  "estado": 422,
  "error": "Unprocessable Entity",
  "mensaje": "Saldo no disponible",
  "ruta": "/api/movimientos"
}
```

Cuando hay errores de validación se agrega `errores`, una lista con `campo` y `mensaje`. Códigos: 400 datos inválidos, 404 recurso inexistente, 409 duplicado u operación no permitida, 422 regla de negocio.

## Reglas de negocio

* Los créditos se guardan con valor positivo y los débitos con valor negativo; cada movimiento guarda el saldo disponible resultante.
* Un débito sin saldo suficiente responde `Saldo no disponible`.
* Los retiros de una cuenta no pueden superar el límite diario (1000 por defecto, variable `LIMITE_DIARIO_RETIRO`); si se supera responde `Cupo diario excedido`.
* El registro de movimientos es transaccional y bloquea la fila de la cuenta, así dos retiros simultáneos no pueden dejar el saldo negativo.
* Solo se puede modificar o eliminar el último movimiento de cada cuenta, porque cambiar uno intermedio dejaría mal los saldos de los siguientes. Por la misma razón no se puede cambiar el saldo inicial de una cuenta con movimientos.
* No se puede eliminar un cliente con cuentas ni una cuenta con movimientos.
* Las contraseñas se guardan cifradas con bcrypt y nunca aparecen en las respuestas.

## Datos iniciales

| Cliente | Usuario | Contraseña | Cuentas |
| --- | --- | --- | --- |
| Jose Lema | `jlema` | 1234 | 478758 ahorro 2000, 585545 corriente 1000 |
| Marianela Montalvo | `mmontalvo` | 5678 | 225487 corriente 100, 496825 ahorro 540 |
| Juan Osorio | `josorio` | 1245 | 495878 ahorro 0 |

Género, edad e identificación son valores de ejemplo porque el enunciado no los trae. Los movimientos del ejercicio no vienen en el script: se registran por la API (la carpeta 3 de la colección de Postman lo hace).

## Diseño

Backend (`backend/src/main/java/com/banco/api`):

* `entity`: `Persona` abstracta con herencia JOINED, `Cliente`, `Cuenta` y `Movimiento`, con validaciones de Bean Validation. Hibernate valida el esquema contra `BaseDatos.sql` al arrancar.
* `service`: interfaces e implementaciones; los servicios devuelven DTOs y nunca entidades.
* `strategy`: patrón Strategy para los movimientos. `CreditoStrategy` suma y `DebitoStrategy` resta y valida saldo y cupo; un resolvedor arma un `EnumMap` con todas las estrategias.
* `reporte`: el mismo patrón para el formato del reporte (JSON y PDF con OpenPDF).
* `exception`: excepciones propias y un `@RestControllerAdvice` que produce el JSON de error uniforme.
* Los reportes y los totales se calculan con streams y lambdas.

Frontend (`frontend/src/app`):

* `core`: modelos iguales a los DTOs, un servicio HTTP genérico por recurso y un interceptor que convierte cualquier error en un `ErrorApi` con el mensaje del backend.
* `shared`: componentes reutilizables (tabla, encabezado con buscador y botón Nuevo, modal, campo de formulario con sus mensajes de validación, avisos) y funciones puras de búsqueda, formato y validación.
* `features`: una carpeta por pantalla (clientes, cuentas, movimientos y reportes) con su listado y su formulario.
* El estado de la vista vive en signals; la búsqueda rápida no distingue mayúsculas ni tildes.

## Estructura

```
banco-app/
  backend/            API REST (Maven)
  frontend/           Aplicación Angular y configuración de Nginx
  docs/postman/       Colección de Postman
  docs/diseno-frontend.png
  BaseDatos.sql       Esquema y datos iniciales
  CrearBaseDatos.sql  Creación de la base banco (solo sin Docker)
  compose.yml
  .env.example
```
