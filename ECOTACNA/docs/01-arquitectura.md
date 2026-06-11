# Arquitectura Técnica — EcoTacna

## Visión general

EcoTacna es una aplicación web de arquitectura cliente-servidor que conecta empresas generadoras de aceite vegetal usado con empresas recolectoras en la región de Tacna.

```text
┌────────────────────┐         HTTP/REST         ┌─────────────────────────┐
│   Frontend React   │  ◄──────────────────────►  │  Backend Spring Boot    │
│   (Vite + TS)      │        JSON + JWT          │  (Java 17 + JPA)        │
│   Puerto 8081      │                            │  Puerto 8082            │
└────────────────────┘                            └────────┬────────────────┘
                                                           │
                                                           │ JDBC / Pooler
                                                           ▼
                                                  ┌────────────────────┐
                                                  │   PostgreSQL       │
                                                  │   (Supabase)       │
                                                  └────────────────────┘
```

## Separación por roles

El sistema maneja tres roles con paneles diferenciados:

| Rol | Descripción | Panel |
|-----|-------------|-------|
| `ADMIN` | Administrador de la plataforma | Gestión de empresas, recolectores y aprobaciones (la gestión de usuarios es implícita) |
| `GENERADORA` | Empresa que genera aceite vegetal usado | Solicitar recojos, confirmar litros, ver historial |
| `RECOLECTORA` | Empresa que recoge aceite | Aceptar solicitudes, registrar unidades, ver historial |

## Capas del Backend

El backend sigue la arquitectura estándar de Spring Boot por capas:

### Controller
Recibe las peticiones HTTP, valida permisos por rol y delega al Service.

Controladores principales:
- `AuthController` — Login, registro, autenticación JWT.
- `PickupRequestController` — Solicitudes de recojo (CRUD + flujo de estados).
- `AdminDashboardController` — Panel de administración.
- `TransportUnitController` — Unidades de transporte del recolector.
- `PaymentController` — Pagos de suscripción (simulados).
- `PublicCaptchaController` — Generación y verificación de PuzzleCaptcha.
- `RucLookupController` — Consulta RUC vía ApiPeruDev.

### Service
Contiene la lógica de negocio:
- `PickupRequestService` — Flujo completo de solicitudes y confirmación de pago.
- `CaptchaService` — Generación de desafíos, verificación de respuestas, tokens de un solo uso.
- `ConstanciaPdfService` — Generación de constancias PDF con iText.
- `HistorialExcelService` — Exportación de historial a Excel con Apache POI.
- `ApiPeruDevRucService` — Consulta de RUC contra la API de ApiPeruDev.
- `AdminDashboardService` — Estadísticas y gestión administrativa.
- `AuditLogService` — Registro de auditoría de acciones críticas.

### Repository
Interfaces JPA que extienden `JpaRepository` para acceso a datos:
- `UserRepository`
- `CompanyRepository`
- `PickupRequestRepository`
- `TransportUnitRepository`
- `SubscriptionRepository`
- `AuditLogRepository`

### DTO (Data Transfer Objects)
Objetos de transferencia para la comunicación frontend ↔ backend:
- `PickupRequestRequest` / `PickupRequestResponse`
- `PickupTrackingResponse`
- `CaptchaVerifyRequestDto`
- `AuthRequest` / `AuthResponse`
- `TransportUnitRequest`

### Entity (Modelo de datos)
Entidades JPA mapeadas a tablas PostgreSQL:
- `User` — Usuarios del sistema (email, password BCrypt, rol, empresa asociada).
- `Company` — Empresas registradas (RUC, razón social, tipo, estado de suscripción).
- `PickupRequest` — Solicitudes de recojo (volumen, precio ofertado, estado, litros confirmados, monto final).
- `TransportUnit` — Unidades vehiculares del recolector (placa, capacidad).
- `Subscription` — Registro de suscripciones y pagos simulados.
- `AuditLog` — Log de auditoría con acciones, usuario, IP y timestamp.

## Capas del Frontend

El frontend está organizado en:

### `pages/`
Páginas organizadas por rol:
- `pages/admin/` — Panel administrativo (AdminEmpresas, AdminRecolectores, AdminSolicitudes, etc. NOTA: Se retiró el módulo visual de Usuarios, su gestión es implícita en empresas/recolectores y la autenticación. La tabla users se conserva para login, roles y relación con empresas).
- `pages/empresa/` — Panel de la empresa generadora (EmpresaMisSolicitudes, EmpresaSeguimiento, EmpresaSolicitarRecojo, etc.).
- `pages/recolector/` — Panel del recolector (RecolectorSolicitudes, RecolectorRecojosDia, RecolectorMapaOperativo, etc.).
- `Login.tsx`, `Profile.tsx` — Páginas compartidas.

### `services/`
Módulos de comunicación con el backend:
- `authApi.ts` — Login y autenticación.
- `empresaApi.ts` — Operaciones de empresa generadora (solicitudes, historial, PDF, Excel).
- `recolectorApi.ts` — Operaciones de recolector (aceptar, confirmar, historial, PDF, Excel).
- `publicApi.ts` — Endpoints públicos (captcha).
- `adminApi.ts` — Operaciones administrativas.

### `components/`
Componentes reutilizables:
- `PuzzleCaptcha.tsx` — Componente visual de captcha tipo rompecabezas.
- `DashboardShell.tsx` — Layout compartido para paneles.
- `ui/` — Componentes base de interfaz (botones, inputs, dialogs, etc.).

### `types.ts`
Interfaces TypeScript que reflejan los DTOs del backend.

## Base de datos

PostgreSQL alojado en Supabase, conectado mediante Transaction Pooler (puerto 6543) con SSL requerido.

### Entidades principales y relaciones

```text
┌──────────┐    1:N    ┌──────────────┐    N:1    ┌──────────┐
│ Company  │◄──────────│    User      │──────────►│  Company │
│          │           │ (rol, email) │           │          │
└────┬─────┘           └──────────────┘           └──────────┘
     │ 1:N                                              │ 1:N
     ▼                                                  ▼
┌──────────────────┐                        ┌─────────────────┐
│  PickupRequest   │                        │  TransportUnit  │
│  (solicitudes)   │                        │  (unidades)     │
└──────────────────┘                        └─────────────────┘
```

- `ddl-auto=update`: Hibernate gestiona el esquema automáticamente.
- No se usan seeders ni datos demo.

## Integraciones externas

### ApiPeruDev (Consulta RUC)
- Servicio externo para validar RUC de empresas al momento del registro.
- Requiere un token válido (`APIPERUDEV_API_TOKEN`).
- Configurable: si `RUC_PROVIDER=mock`, se omite la llamada real.

### PuzzleCaptcha
- Implementación propia (sin dependencia de Google reCAPTCHA).
- El backend genera un desafío visual, el frontend lo resuelve, y el backend verifica la respuesta.
- Los tokens verificados son de un solo uso y expiran en 5 minutos.

## Generación de reportes

### Constancia PDF (iText)
- Genera un PDF por operación completada con pago confirmado.
- Incluye: datos de empresa, recolector, litros, precio ofertado, monto final, fecha.
- Endpoint protegido por rol (empresa ve sus propias, recolector ve las que atendió).

### Exportación Excel (Apache POI)
- Genera un archivo `.xlsx` con el historial completo de operaciones.
- Diferenciado por rol: la empresa ve su historial y el recolector ve el suyo.
