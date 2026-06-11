# EcoTacna — Plataforma de gestión de aceite vegetal usado

Sistema web para conectar empresas generadoras/restaurantes con empresas recolectoras de aceite vegetal usado en Tacna.

## 🛠️ Stack Tecnológico
* **Backend:** Spring Boot, Java 17, JPA/Hibernate.
* **Frontend:** React 18, Vite 5, TypeScript, Vanilla CSS/Tailwind.
* **Base de datos:** PostgreSQL / Supabase.
* **Seguridad:** JWT, roles (ADMIN, GENERADORA, RECOLECTORA), BCrypt, PuzzleCaptcha.
* **Reportes:** iText PDF y Apache POI Excel.
* **RUC:** ApiPeruDev.
* **Pagos:** simulados para entorno académico.

## 📦 Módulos principales
* Registro de empresas con validación de RUC (ApiPeruDev).
* Login con PuzzleCaptcha.
* Panel administrador.
* Panel empresa generadora.
* Panel recolector.
* Solicitudes de recojo.
* Precio ofertado por litro y confirmación de pago.
* Confirmación de litros recogidos.
* Historiales de operaciones.
* Constancia PDF (por operación).
* Exportación Excel (historial general).
* Pagos de suscripción simulados.

## 🔄 Flujo funcional resumido
```text
Registro empresa
→ consulta RUC ApiPeruDev
→ aprobación administrativa y pago simulado de suscripción
→ generador solicita recojo con precio ofertado
→ recolector acepta solicitud
→ recolector recoge y empresa confirma litros
→ backend calcula monto final (litros confirmados × precio ofertado)
→ historial
→ generación de PDF y Excel
```

## ⚙️ Variables de entorno principales
Para ejecutar la aplicación se requiere configurar ciertas variables de entorno o *properties*. (No incluya secretos reales en el repositorio).

**Backend (`EcoTacnaSpringBootJPA/src/main/resources/application.properties`):**
* `APIPERUDEV_API_TOKEN`: Token de acceso a API Perú Dev (requerido para consultar RUCs reales).
* `APIPERUDEV_API_BASE_URL`: URL base de la API (ej: `https://apiperu.dev/api`).
* `RUC_PROVIDER`: Proveedor de validación (`apiperudev` o `mock`).
* `JWT_SECRET`: Clave secreta (HS256) para firmar tokens. Cambiar en producción.
* `JWT_EXPIRATION`: Tiempo de expiración del token (milisegundos).
* `CAPTCHA_ENABLED`: Activa (`true`) la validación de PuzzleCaptcha en el backend.
* `PAYMENTS_MODE`: Modo de pagos (`simulated`).
* `ALLOWED_ORIGINS`: Dominios permitidos para CORS (ej: `http://localhost:8081`).
* Variables JDBC (`spring.datasource.url`, `username`, `password`): Credenciales de PostgreSQL o Supabase Transaction Pooler.

**Frontend (`EcoTacnaFrontend/.env`):**
* `VITE_API_BASE_URL`: URL base del backend (ej: `http://localhost:8082/ecotacna/api`).
* `VITE_RUC_PROVIDER`: Controla el flujo visual de consulta (`apiperudev`).

## 🚀 Cómo ejecutar localmente

**Backend:**
```powershell
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile
.\mvnw.cmd spring-boot:run
```

**Frontend:**
```powershell
cd EcoTacnaFrontend
npm install
npm run dev
```

## ✅ Cómo validar

**Backend (Compilación y empaquetado):**
```powershell
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile
.\mvnw.cmd package -DskipTests
```
*Estado actual: compilación y empaquetado correctos (106 archivos fuente).*

> La entrega final no incluye tests automatizados por decisión de limpieza académica. La validación operativa se realiza mediante compilación, empaquetado y prueba manual de los flujos.

**Frontend (Lint y Build):**
```powershell
cd EcoTacnaFrontend
npm run lint
npx tsc --noEmit
npm run build
```
*Estado actual: 0 errores de lint, warnings técnicos controlados.*

## ⚠️ Advertencias importantes
* **Pagos Simulados:** Los pagos de suscripción son 100% simulados para fines académicos. **No se debe usar tarjetas reales.**
* **API RUC:** La consulta RUC usa ApiPeruDev (requiere un token válido en `APIPERUDEV_API_TOKEN`).
* **Geolocalización:** El mapa operativo puede estar como placeholder ya que la implementación de geolocalización en tiempo real requiere API Keys de paga.
* **Comprobantes:** La constancia PDF generada por la aplicación es referencial y operativa, no es un comprobante tributario válido. El sistema no emite facturación electrónica oficial (SUNAT).

## 📚 Enlaces a documentación detallada
Consulte los siguientes archivos en la carpeta `docs/` para más información técnica y funcional:

* [docs/01-arquitectura.md](docs/01-arquitectura.md)
* [docs/02-flujo-funcional.md](docs/02-flujo-funcional.md)
* [docs/03-seguridad.md](docs/03-seguridad.md)
* [docs/04-despliegue.md](docs/04-despliegue.md)
