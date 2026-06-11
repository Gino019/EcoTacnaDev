# Seguridad — EcoTacna

Este documento describe los mecanismos de seguridad implementados en la plataforma EcoTacna.

---

## Autenticación JWT

- El sistema usa **JSON Web Tokens (JWT)** con algoritmo **HMAC-SHA256** para autenticar a los usuarios.
- Al hacer login exitoso, el backend genera un token JWT que contiene:
  - `sub`: email del usuario.
  - `role`: rol del usuario (ADMIN, GENERADORA, RECOLECTORA).
  - `exp`: fecha de expiración (configurable, por defecto 24 horas).
- El frontend almacena el token y lo envía en cada petición HTTP como header `Authorization: Bearer <token>`.
- El backend valida el token en cada request protegido mediante un filtro de Spring Security (`JwtAuthFilter`).

### Configuración
| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `JWT_SECRET` | Clave secreta HMAC-256 (mín. 256 bits) | Valor de desarrollo incluido |
| `JWT_EXPIRATION` | Expiración en milisegundos | `86400000` (24 horas) |

> ⚠️ **Producción:** Cambiar `JWT_SECRET` por una clave única y segura.

---

## Roles y permisos

El sistema define tres roles con acceso diferenciado:

### ADMIN
- Aprueba/rechaza empresas registradas.
- Gestiona usuarios.
- Visualiza todas las solicitudes y estadísticas.
- No puede crear solicitudes de recojo.

### GENERADORA
- Solicita recojos con precio ofertado.
- Confirma litros recogidos.
- Ve su historial de solicitudes.
- Descarga PDF y Excel de sus operaciones.

### RECOLECTORA
- Ve solicitudes disponibles.
- Acepta/rechaza solicitudes.
- Gestiona unidades de transporte.
- Ve su historial de recojos atendidos.
- Descarga PDF y Excel de sus operaciones.

### Validaciones de pertenencia
- Una empresa generadora **solo puede ver y operar sobre sus propias solicitudes**.
- Un recolector **solo puede ver los recojos que ha aceptado y atendido**.
- Los endpoints de PDF y Excel verifican que el usuario pertenezca a la empresa correspondiente antes de generar el documento.
- El backend valida `companyId` del usuario autenticado contra el `companyId` de la solicitud.

---

## Contraseñas — BCrypt

- Todas las contraseñas se almacenan hasheadas con **BCrypt** (factor de costo estándar).
- El backend nunca almacena ni transmite contraseñas en texto plano.
- Al registrarse, la contraseña se hashea antes de persistirla.
- Al hacer login, se compara el hash almacenado con la contraseña ingresada usando `BCryptPasswordEncoder`.

---

## PuzzleCaptcha

EcoTacna implementa un captcha propio tipo rompecabezas, sin depender de servicios externos (Google reCAPTCHA fue eliminado del proyecto).

### Flujo completo

```text
1. Frontend solicita un desafío  →  GET /api/public/captcha/challenge
2. Backend genera imagen + pieza recortada + posición correcta
3. Backend almacena el desafío en memoria (ConcurrentHashMap)
4. Frontend muestra la imagen y la pieza arrastrnable
5. Usuario arrastra la pieza a la posición correcta
6. Frontend envía la respuesta  →  POST /api/public/captcha/verify
   { challengeId, x, y }
7. Backend calcula la diferencia en píxeles
8. Si diff ≤ umbral → token verificado generado (UUID)
9. El desafío se marca como verificado y consumible una sola vez
10. Frontend envía el token verificado con el formulario de login/registro
11. Backend valida que el token existe, no está expirado (5 min) y no fue usado
12. Token se consume (elimina) → un solo uso garantizado
```

### Características de seguridad
- **Un solo uso:** El token verificado se consume al validarlo en el login/registro. No puede reutilizarse.
- **Expiración:** Los desafíos y tokens expiran a los 5 minutos.
- **Limpieza automática:** Un scheduled task elimina periódicamente los desafíos expirados.
- **Tolerancia configurable:** El umbral de diferencia en píxeles es configurable para balancear usabilidad vs. seguridad.
- **Sin dependencias externas:** No requiere claves de API de Google ni servicios de terceros.

### Configuración
| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `CAPTCHA_ENABLED` | Activa/desactiva la validación del captcha | `true` |

---

## Restricciones de PDF y Excel por rol

| Recurso | GENERADORA | RECOLECTORA | ADMIN |
|---------|-----------|-------------|-------|
| PDF de constancia (propia) | ✅ | ✅ | ❌ |
| PDF de constancia (ajena) | ❌ | ❌ | ❌ |
| Excel historial (propio) | ✅ | ✅ | ❌ |
| Excel historial (ajeno) | ❌ | ❌ | ❌ |

- Los endpoints verifican la pertenencia del usuario a la empresa antes de generar el documento.

---

## Pagos simulados

- El módulo de pagos de suscripción opera en modo **simulado** (`PAYMENTS_MODE=simulated`).
- No se conecta a ninguna pasarela de pago real (Culqi, Stripe, Niubiz, etc.).
- El flujo simula un checkout exitoso y activa la suscripción internamente.
- **No se debe ingresar datos de tarjetas de crédito/débito reales.**

---

## Auditoría

- Las acciones críticas se registran en la tabla `audit_logs`:
  - Acción realizada.
  - Usuario que la ejecutó.
  - Dirección IP.
  - Timestamp.
  - Detalles adicionales.
- Ejemplos de acciones auditadas: creación de solicitud, cambio de estado, confirmación de pago, registro de unidad.

---

## CORS

- El backend configura CORS mediante la variable `ALLOWED_ORIGINS`.
- Por defecto permite peticiones desde `http://localhost:8081` (frontend local).
- En producción, configurar con el dominio real del frontend.

---

## Limitaciones conocidas

1. **No hay refresh token:** Si el JWT expira, el usuario debe volver a hacer login.
2. **Sesión en memoria:** Los desafíos del PuzzleCaptcha se almacenan en memoria del servidor (no persisten entre reinicios).
3. **Sin rate limiting:** No hay limitación de intentos de login por IP (se recomienda agregar en producción).
4. **Sin HTTPS forzado:** En desarrollo se usa HTTP. En producción se debe configurar HTTPS obligatorio.
5. **Secreto JWT de desarrollo:** El secreto por defecto es solo para desarrollo; debe cambiarse en producción.
