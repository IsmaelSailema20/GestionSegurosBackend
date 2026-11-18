+# GestionSegurosBackend
+
+Backend para la gestión de seguros médicos, clientes, contratos, pagos, documentos y reembolsos. Expone una API REST con Spring Boot y autenticación stateless mediante JWT.
+
+> Proyecto en desarrollo (`0.0.1-SNAPSHOT`). La documentación corresponde a la rama `develop`.
+
+## Características
+
+- Autenticación y autorización con JWT.
+- Roles: `Administrador`, `Agente` y `Cliente`.
+- Gestión de usuarios, clientes, agentes y condiciones médicas.
+- Administración de seguros, beneficios, contratos y beneficiarios.
+- Registro de pagos y comprobantes.
+- Gestión de tarjetas de clientes y tarjeta principal.
+- Carga, descarga, aprobación y rechazo de documentos.
+- Solicitud, seguimiento y resolución de reembolsos.
+- Generación de contratos y reportes en PDF.
+- Notificaciones por correo mediante Mailgun.
+- Pruebas unitarias y de integración con JUnit, Spring Boot Test, Mockito, H2 y MockMvc.
+
+## Tecnologías
+
+Java 17 · Spring Boot 3.4.4 · Spring Web · Spring Data JPA/Hibernate · PostgreSQL · H2 · Spring Security · JJWT 0.12.6 · Thymeleaf · Flying Saucer PDF · Lombok · Maven · JaCoCo.
+
+## Arquitectura
+
+El código está organizado por capas:
+
+- `Controllers`: endpoints HTTP, validación y respuestas REST.
+- `Services`: reglas de negocio.
+- `Repositories`: acceso a datos con Spring Data JPA.
+- `Entities`: entidades persistentes y relaciones.
+- `Dto`: objetos de entrada y salida.
+- `Auth` y `Config`: JWT, filtros y configuración de seguridad.
+- `Exceptions`: tratamiento global de errores.
+
+El proyecto se empaqueta como WAR y también puede ejecutarse directamente con Spring Boot.
+
+## Requisitos
+
+- JDK 17 o superior.
+- PostgreSQL para la ejecución normal.
+- Git.
+- Maven opcional: se incluye Maven Wrapper (`mvnw` y `mvnw.cmd`).
+
+## Configuración
+
+La configuración principal está en `src/main/resources/application.properties`. Ajusta las credenciales de PostgreSQL antes de ejecutar:
+
+```properties
+spring.datasource.url=jdbc:postgresql://localhost:5432/AplicacionSeguros
+spring.datasource.username=TU_USUARIO
+spring.datasource.password=TU_CONTRASEÑA
+spring.jpa.hibernate.ddl-auto=update
+documentos.directorio=./uploads
+spring.servlet.multipart.max-file-size=10MB
+```
+
+Puedes crear la base de datos con:
+
+```sql
+CREATE DATABASE "AplicacionSeguros";
+```
+
+El perfil de pruebas utiliza H2 en memoria y está definido en `application-test.properties`. El directorio `uploads/` está excluido del repositorio.
+
+El envío de correo requiere configurar Mailgun. Usa variables de entorno o un gestor de secretos para la API key, dominio y correo remitente.
+
+## Ejecución
+
+```bash
+git clone https://github.com/IsmaelSailema20/GestionSegurosBackend.git
+cd GestionSegurosBackend
+git checkout develop
+```
+
+Linux/macOS:
+
+```bash
+./mvnw spring-boot:run
+```
+
+Windows:
+
+```bat
+mvnw.cmd spring-boot:run
+```
+
+La API queda disponible por defecto en `http://localhost:8080`.
+
+Para generar el WAR:
+
+```bash
+./mvnw clean package
+```
+
+## Pruebas y cobertura
+
+```bash
+./mvnw clean test
+```
+
+El reporte de JaCoCo se genera en `target/site/jacoco/index.html`.
+
+## Autenticación
+
+El inicio de sesión no requiere token:
+
+```http
+POST /login
+Content-Type: application/json
+```
+
+```json
+{
+  "email": "usuario@ejemplo.com",
+  "password": "tu contraseña"
+}
+```
+
+Respuesta:
+
+```json
+{
+  "token": "<JWT>"
+}
+```
+
+Usa el token en las rutas protegidas:
+
+```http
+Authorization: Bearer <JWT>
+```
+
+La autorización se configura en `SecurityConfig`. Algunas rutas de pagos, reembolsos y reportes tienen actualmente reglas permisivas; deben revisarse antes de desplegar en producción.
+
+## API
+
+Las rutas respetan las mayúsculas y minúsculas definidas en los controladores.
+
+### Usuarios y autenticación
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| POST | `/login` | Autentica y devuelve un JWT. |
+| GET | `/Usuarios` | Lista usuarios. |
+| GET | `/Usuarios/{id}` | Consulta un usuario. |
+| POST | `/Usuarios/guardar` | Registra un usuario. |
+| POST | `/Usuarios/{id}` | Actualiza un usuario. |
+| PUT | `/Usuarios/credencialesCliente` | Actualiza credenciales de cliente. |
+| DELETE | `/Usuarios/{id}` | Elimina un usuario. |
+
+### Clientes, seguros y catálogos
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| GET/POST | `/Clientes` | Lista o registra clientes. |
+| GET/PUT | `/Clientes/{id}` | Consulta o edita un cliente. |
+| GET | `/Clientes/agente` | Clientes del agente autenticado. |
+| PUT | `/Clientes/estado/{id}` | Actualiza el estado del cliente. |
+| GET/POST | `/Seguros` | Lista o crea seguros. |
+| GET/PUT/DELETE | `/Seguros/{id}` | Consulta, edita o elimina un seguro. |
+| POST | `/Seguros/{id}` | Activa un seguro. |
+| POST | `/Seguros/editable/{id}` | Comprueba si es editable. |
+| GET | `/Beneficios` | Lista beneficios. |
+| GET/POST | `/Condiciones` | Lista o crea condiciones médicas. |
+| GET/PUT/DELETE | `/Condiciones/{id}` | Consulta, edita o elimina una condición. |
+
+### Contratos
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| GET | `/contratos` | Lista contratos. |
+| GET | `/contratos/{id}` | Consulta un contrato. |
+| GET | `/contratos/cliente/{cedula}` | Contratos por cédula. |
+| GET | `/contratos/agente/{idAgente}` | Contratos por agente. |
+| POST | `/contratos` | Registra un contrato. |
+| PUT | `/contratos/{id}` | Edita un contrato. |
+| PUT | `/contratos/estado/{id}` | Cambia su estado. |
+| PUT | `/contratos/activar/{id}` | Activa un contrato. |
+| DELETE | `/contratos/{id}` | Elimina un contrato. |
+
+### Pagos y tarjetas
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| POST | `/Pagos/registrar` | Registra un pago en JSON. |
+| POST | `/Pagos` | Registra un pago con comprobante multipart. |
+| GET | `/Pagos/{pagoId}/documentos` | Comprobantes de un pago. |
+| GET | `/Pagos/contrato/{contratoId}` | Pagos de un contrato. |
+| GET | `/Pagos/contrato/{contratoId}/pedientes` | Pagos pendientes. |
+| GET | `/Pagos/estado/{contratoId}` | Estado del último pago. |
+| PUT | `/Pagos/aceptarPago/{idDocumento}` | Aprueba un comprobante. |
+| PUT | `/Pagos/rechazarPago/{idDocumento}` | Rechaza un comprobante. |
+| GET/POST | `/Tarjetas` | Lista o registra tarjetas. |
+| GET/PUT/DELETE | `/Tarjetas/{id}` | Consulta, edita o elimina una tarjeta. |
+| GET | `/Tarjetas/cliente/{idCliente}` | Tarjetas de un cliente. |
+| GET | `/Tarjetas/cliente/{idCliente}/principal` | Tarjeta principal. |
+| PUT | `/Tarjetas/{id}/inactivar` | Inactiva una tarjeta. |
+| PUT | `/Tarjetas/cambiarAPrincipal/{id}` | Marca una tarjeta como principal. |
+
+### Documentos
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| POST | `/documentos/subir` | Sube un archivo con `multipart/form-data`. |
+| GET | `/documentos/cliente/{idContrato}` | Lista documentos de un contrato. |
+| GET | `/documentos/descargar/{idDocumento}` | Descarga un documento. |
+| PUT | `/documentos/{idDocumento}/aprobar` | Aprueba y notifica al cliente. |
+| PUT | `/documentos/{idDocumento}/rechazar` | Rechaza y notifica al cliente. |
+
+### Reembolsos
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| POST | `/Reembolsos/crearConDocumentos` | Crea un reembolso con documentos. |
+| GET | `/Reembolsos` | Lista reembolsos. |
+| GET | `/Reembolsos/{id}` | Consulta un reembolso. |
+| GET | `/Reembolsos/pendientes` | Lista pendientes. |
+| GET | `/Reembolsos/documentos/{reembolsoId}` | Documentos del reembolso. |
+| GET | `/Reembolsos/porContrato/{contratoId}` | Reembolsos por contrato. |
+| GET | `/Reembolsos/cliente/{clienteId}` | Reembolsos por cliente. |
+| GET | `/Reembolsos/agente/{agenteId}` | Reembolsos por agente. |
+| POST | `/Reembolsos/SubirDocumento` | Sube un documento. |
+| POST | `/Reembolsos/subirDocumento/{reembolsoId}` | Añade un documento. |
+| PUT | `/Reembolsos/{id}` | Actualiza un reembolso. |
+| PUT | `/Reembolsos/aceptar/{id}` | Acepta un reembolso. |
+| PUT | `/Reembolsos/rechazar/{id}` | Rechaza un reembolso. |
+| PUT | `/Reembolsos/rechazarDocumento/{idDocumento}` | Rechaza un documento. |
+
+### PDFs y reportes
+
+| Método | Ruta | Descripción |
+|---|---|---|
+| GET | `/pdf/contrato/{id}` | Obtiene o genera el PDF del contrato. |
+| GET | `/pdf/Reportes` | Reporte general de contratos. |
+| GET | `/pdf/Reportes/{id}` | Reporte filtrado por agente. |
+| GET | `/pdf/Reportes/documentos` | Reporte de documentos pendientes. |
+| GET | `/pdf/Reportes/documentos/{idAgente}` | Reporte por agente. |
+
+## Archivos y plantillas
+
+Los archivos se reciben con `multipart/form-data` y tienen un límite de 10 MB. Las plantillas se encuentran en `src/main/resources/templates/`:
+
+- `ContratoPlantilla.html`
+- `ReportesPlantilla.html`
+- `ReportesDocumentosPlantilla.html`
+
+Los tipos documentales contemplan identificación, historial médico, formularios, comprobantes de pago y respaldos de consultas, exámenes e internaciones.
+
+## Estructura
+
+```text
+src/main/java/com/app/seguros/AplicacionSeguros/
+├── Auth
+├── Config
+├── Controllers
+├── Dto
+├── Entities
+├── Exceptions
+├── Repositories
+└── Services
+```
+
+## Consideraciones de seguridad
+
+- Reemplaza cualquier credencial o secreto expuesto previamente en archivos de configuración.
+- Mueve la contraseña de PostgreSQL, la API key de Mailgun y la clave JWT a variables de entorno o un gestor de secretos.
+- No subas `uploads/` ni documentos reales al repositorio.
+- Restringe CORS en producción; actualmente permite cualquier origen.
+- Revisa las rutas con `permitAll()`, especialmente pagos, reembolsos y reportes.
+- Usa HTTPS fuera del entorno local.
+- Valida autorización por recurso, tipo MIME y tamaño real de los archivos.
+
+## Licencia
+
+El repositorio no define una licencia de código abierto. Confirma las condiciones de uso con sus propietarios antes de redistribuirlo.
