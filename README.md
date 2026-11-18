# GestionSegurosBackend

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-6DB33F?style=flat&logo=springboot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?style=flat&logo=postgresql&logoColor=white)
![JWT](https://img.shields.io/badge/Auth-JWT%200.12.6-000000?style=flat&logo=jsonwebtokens&logoColor=white)
![Version](https://img.shields.io/badge/Version-0.0.1--SNAPSHOT-blue?style=flat)

Backend para la gestión de seguros médicos, clientes, contratos, pagos, documentos y reembolsos. Expone una API REST con Spring Boot y autenticación *stateless* mediante JWT.

> [!NOTE]
> Proyecto en desarrollo (`0.0.1-SNAPSHOT`). La documentación corresponde a la rama `develop`.

## Tabla de contenidos

- [Características](#características)
- [Tecnologías](#tecnologías)
- [Arquitectura y estructura](#arquitectura-y-estructura)
- [Requisitos](#requisitos)
- [Configuración](#configuración)
- [Ejecución](#ejecución)
- [Pruebas y cobertura](#pruebas-y-cobertura)
- [Autenticación](#autenticación)
- [API](#api)
- [Archivos y plantillas](#archivos-y-plantillas)
- [Consideraciones de seguridad](#consideraciones-de-seguridad)
- [Licencia](#licencia)

---

## Características

- Autenticación y autorización *stateless* con JWT.
- Control de acceso basado en roles: `Administrador`, `Agente` y `Cliente`.
- Gestión de usuarios, clientes, agentes y condiciones médicas.
- Administración de seguros, beneficios, contratos y beneficiarios.
- Registro de pagos y validación de comprobantes.
- Gestión de tarjetas de clientes y asignación de tarjeta principal.
- Carga, descarga, aprobación y rechazo de documentos.
- Solicitud, seguimiento y resolución de reembolsos.
- Generación dinámica de contratos y reportes en PDF.
- Notificaciones por correo electrónico mediante Mailgun.
- Pruebas unitarias y de integración con JUnit, Spring Boot Test, Mockito, H2 y MockMvc.

## Tecnologías

| Categoría | Herramientas |
| :--- | :--- |
| **Lenguaje y Framework** | Java 17, Spring Boot 3.4.4, Spring Web |
| **Persistencia** | Spring Data JPA / Hibernate, PostgreSQL, H2 |
| **Seguridad** | Spring Security, JJWT 0.12.6 |
| **Documentos y Plantillas** | Thymeleaf, Flying Saucer PDF |
| **Utilidades y Build** | Lombok, Maven (Maven Wrapper), JaCoCo |

## Arquitectura y estructura

El código está organizado bajo una arquitectura por capas:

- **`Controllers`**: endpoints HTTP, validación de entrada y respuestas REST.
- **`Services`**: lógica y reglas de negocio.
- **`Repositories`**: acceso a datos mediante Spring Data JPA.
- **`Entities`**: entidades persistentes y relaciones de base de datos.
- **`Dto`**: objetos de transferencia de datos para entrada y salida.
- **`Auth` y `Config`**: generación/validación de JWT, filtros y configuración de seguridad.
- **`Exceptions`**: manejo global de errores y excepciones personalizadas.

El proyecto se empaqueta como **WAR** y también puede ejecutarse directamente mediante Spring Boot.

```text
src/main/java/com/app/seguros/AplicacionSeguros/
├── Auth
├── Config
├── Controllers
├── Dto
├── Entities
├── Exceptions
├── Repositories
└── Services
```

## Requisitos

- **JDK 17** o superior.
- **PostgreSQL** para ejecución en entorno normal.
- **Git**.
- **Maven** *(opcional: el repositorio incluye Maven Wrapper `mvnw` y `mvnw.cmd`)*.

## Configuración

1. **Crear la base de datos en PostgreSQL:**

```sql
CREATE DATABASE "AplicacionSeguros";
```

2. **Configurar propiedades de la aplicación:**

La configuración principal se encuentra en `src/main/resources/application.properties`. Ajusta las credenciales de PostgreSQL antes de iniciar el servidor:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/AplicacionSeguros
spring.datasource.username=TU_USUARIO
spring.datasource.password=TU_CONTRASEÑA
spring.jpa.hibernate.ddl-auto=update
documentos.directorio=./uploads
spring.servlet.multipart.max-file-size=10MB
```

3. **Servicios externos y perfiles:**

- **Correo (Mailgun):** el envío de notificaciones requiere configurar la API key, el dominio y el correo remitente mediante variables de entorno o un gestor de secretos.
- **Almacenamiento local:** el directorio `uploads/` está excluido del control de versiones.
- **Perfil de pruebas:** utiliza H2 en memoria y está configurado en `application-test.properties`.

## Ejecución

Clona el repositorio y cámbiate a la rama `develop`:

```bash
git clone [https://github.com/IsmaelSailema20/GestionSegurosBackend.git](https://github.com/IsmaelSailema20/GestionSegurosBackend.git)
cd GestionSegurosBackend
git checkout develop
```

**Iniciar en Linux / macOS:**

```bash
./mvnw spring-boot:run
```

**Iniciar en Windows:**

```bat
mvnw.cmd spring-boot:run
```

Una vez iniciado, la API estará disponible por defecto en `http://localhost:8080`.

**Empaquetar el archivo WAR:**

```bash
./mvnw clean package
```

## Pruebas y cobertura

Para ejecutar la suite de pruebas unitarias y de integración:

```bash
./mvnw clean test
```

El reporte de cobertura generado por JaCoCo queda disponible en:

```text
target/site/jacoco/index.html
```

## Autenticación

El endpoint de inicio de sesión es público y no requiere token:

```http
POST /login
Content-Type: application/json
```

**Cuerpo de la solicitud:**

```json
{
  "email": "usuario@ejemplo.com",
  "password": "tu contraseña"
}
```

**Respuesta:**

```json
{
  "token": "<JWT>"
}
```

Para consumir las rutas protegidas, incluye el token en la cabecera `Authorization`:

```http
Authorization: Bearer <JWT>
```

> [!WARNING]
> La autorización se gestiona en `SecurityConfig`. Algunas rutas de pagos, reembolsos y reportes tienen actualmente reglas permisivas; deben revisarse antes de desplegar en producción.

## API

> [!IMPORTANT]
> Las rutas respetan exactamente las mayúsculas y minúsculas definidas en los controladores.

### Usuarios y autenticación

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/login` | Autentica y devuelve un JWT. |
| `GET` | `/Usuarios` | Lista usuarios. |
| `GET` | `/Usuarios/{id}` | Consulta un usuario. |
| `POST` | `/Usuarios/guardar` | Registra un usuario. |
| `POST` | `/Usuarios/{id}` | Actualiza un usuario. |
| `PUT` | `/Usuarios/credencialesCliente` | Actualiza credenciales de cliente. |
| `DELETE` | `/Usuarios/{id}` | Elimina un usuario. |

### Clientes, seguros y catálogos

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` / `POST` | `/Clientes` | Lista o registra clientes. |
| `GET` / `PUT` | `/Clientes/{id}` | Consulta o edita un cliente. |
| `GET` | `/Clientes/agente` | Lista los clientes del agente autenticado. |
| `PUT` | `/Clientes/estado/{id}` | Actualiza el estado del cliente. |
| `GET` / `POST` | `/Seguros` | Lista o crea seguros. |
| `GET` / `PUT` / `DELETE` | `/Seguros/{id}` | Consulta, edita o elimina un seguro. |
| `POST` | `/Seguros/{id}` | Activa un seguro. |
| `POST` | `/Seguros/editable/{id}` | Comprueba si un seguro es editable. |
| `GET` | `/Beneficios` | Lista beneficios. |
| `GET` / `POST` | `/Condiciones` | Lista o crea condiciones médicas. |
| `GET` / `PUT` / `DELETE` | `/Condiciones/{id}` | Consulta, edita o elimina una condición. |

### Contratos

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/contratos` | Lista contratos. |
| `GET` | `/contratos/{id}` | Consulta un contrato. |
| `GET` | `/contratos/cliente/{cedula}` | Lista contratos por cédula del cliente. |
| `GET` | `/contratos/agente/{idAgente}` | Lista contratos por agente. |
| `POST` | `/contratos` | Registra un contrato. |
| `PUT` | `/contratos/{id}` | Edita un contrato. |
| `PUT` | `/contratos/estado/{id}` | Cambia el estado de un contrato. |
| `PUT` | `/contratos/activar/{id}` | Activa un contrato. |
| `DELETE` | `/contratos/{id}` | Elimina un contrato. |

### Pagos y tarjetas

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/Pagos/registrar` | Registra un pago en formato JSON. |
| `POST` | `/Pagos` | Registra un pago con comprobante `multipart`. |
| `GET` | `/Pagos/{pagoId}/documentos` | Obtiene los comprobantes de un pago. |
| `GET` | `/Pagos/contrato/{contratoId}` | Lista los pagos de un contrato. |
| `GET` | `/Pagos/contrato/{contratoId}/pedientes` | Lista pagos pendientes de un contrato. |
| `GET` | `/Pagos/estado/{contratoId}` | Consulta el estado del último pago. |
| `PUT` | `/Pagos/aceptarPago/{idDocumento}` | Aprueba un comprobante de pago. |
| `PUT` | `/Pagos/rechazarPago/{idDocumento}` | Rechaza un comprobante de pago. |
| `GET` / `POST` | `/Tarjetas` | Lista o registra tarjetas. |
| `GET` / `PUT` / `DELETE` | `/Tarjetas/{id}` | Consulta, edita o elimina una tarjeta. |
| `GET` | `/Tarjetas/cliente/{idCliente}` | Lista las tarjetas de un cliente. |
| `GET` | `/Tarjetas/cliente/{idCliente}/principal` | Obtiene la tarjeta principal del cliente. |
| `PUT` | `/Tarjetas/{id}/inactivar` | Inactiva una tarjeta. |
| `PUT` | `/Tarjetas/cambiarAPrincipal/{id}` | Marca una tarjeta como principal. |

### Documentos

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/documentos/subir` | Sube un archivo con `multipart/form-data`. |
| `GET` | `/documentos/cliente/{idContrato}` | Lista los documentos de un contrato. |
| `GET` | `/documentos/descargar/{idDocumento}` | Descarga un documento. |
| `PUT` | `/documentos/{idDocumento}/aprobar` | Aprueba un documento y notifica al cliente. |
| `PUT` | `/documentos/{idDocumento}/rechazar` | Rechaza un documento y notifica al cliente. |

### Reembolsos

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `POST` | `/Reembolsos/crearConDocumentos` | Crea un reembolso adjuntando documentos. |
| `GET` | `/Reembolsos` | Lista reembolsos. |
| `GET` | `/Reembolsos/{id}` | Consulta un reembolso. |
| `GET` | `/Reembolsos/pendientes` | Lista reembolsos pendientes. |
| `GET` | `/Reembolsos/documentos/{reembolsoId}` | Lista los documentos de un reembolso. |
| `GET` | `/Reembolsos/porContrato/{contratoId}` | Lista reembolsos por contrato. |
| `GET` | `/Reembolsos/cliente/{clienteId}` | Lista reembolsos por cliente. |
| `GET` | `/Reembolsos/agente/{agenteId}` | Lista reembolsos por agente. |
| `POST` | `/Reembolsos/SubirDocumento` | Sube un documento de reembolso. |
| `POST` | `/Reembolsos/subirDocumento/{reembolsoId}` | Añade un documento a un reembolso existente. |
| `PUT` | `/Reembolsos/{id}` | Actualiza un reembolso. |
| `PUT` | `/Reembolsos/aceptar/{id}` | Acepta un reembolso. |
| `PUT` | `/Reembolsos/rechazar/{id}` | Rechaza un reembolso. |
| `PUT` | `/Reembolsos/rechazarDocumento/{idDocumento}` | Rechaza un documento de reembolso. |

### PDFs y reportes

| Método | Ruta | Descripción |
| :--- | :--- | :--- |
| `GET` | `/pdf/contrato/{id}` | Obtiene o genera el PDF del contrato. |
| `GET` | `/pdf/Reportes` | Genera el reporte general de contratos. |
| `GET` | `/pdf/Reportes/{id}` | Genera el reporte filtrado por agente. |
| `GET` | `/pdf/Reportes/documentos` | Genera el reporte de documentos pendientes. |
| `GET` | `/pdf/Reportes/documentos/{idAgente}` | Genera el reporte de documentos por agente. |

## Archivos y plantillas

- **Carga de archivos:** se reciben mediante `multipart/form-data` con un límite máximo de **10 MB** por archivo.
- **Tipos documentales:** contemplan identificación, historial médico, formularios, comprobantes de pago y respaldos de consultas, exámenes e internaciones.
- **Plantillas HTML para PDF** (`src/main/resources/templates/`):
  - `ContratoPlantilla.html`
  - `ReportesPlantilla.html`
  - `ReportesDocumentosPlantilla.html`

## Consideraciones de seguridad

Antes de desplegar en un entorno productivo, ten en cuenta las siguientes recomendaciones:

- Reemplaza cualquier credencial o secreto expuesto previamente en archivos de configuración.
- Traslada la contraseña de PostgreSQL, la API key de Mailgun y la clave de firma JWT a variables de entorno o un gestor de secretos.
- No subas el directorio `uploads/` ni documentos reales al repositorio.
- Restringe la política de **CORS** en producción (actualmente permite cualquier origen).
- Revisa y restringe las rutas configuradas con `permitAll()`, especialmente en los módulos de pagos, reembolsos y reportes.
- Habilita **HTTPS** fuera del entorno de desarrollo local.
- Valida la autorización a nivel de recurso, así como el tipo MIME y tamaño real de los archivos cargados.

## Licencia

El repositorio no define una licencia de código abierto. Confirma las condiciones de uso con sus propietarios antes de redistribuirlo.
