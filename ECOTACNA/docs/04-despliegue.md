# Despliegue — EcoTacna

Este documento describe los requisitos, configuración y pasos necesarios para desplegar EcoTacna en un entorno local o de producción.

---

## Requisitos previos

| Herramienta | Versión mínima | Uso |
|-------------|---------------|-----|
| Java JDK | 17 | Backend Spring Boot |
| Node.js | 18+ | Frontend React/Vite |
| npm | 9+ | Gestión de dependencias frontend |
| PostgreSQL | 15+ | Base de datos (o Supabase) |
| Git | 2.x | Control de versiones |

---

## Estructura del proyecto

```text
ECOTACNA/
├── EcoTacnaSpringBootJPA/    ← Backend Spring Boot
│   ├── src/
│   ├── pom.xml
│   └── mvnw.cmd
├── EcoTacnaFrontend/         ← Frontend React/Vite
│   ├── src/
│   ├── package.json
│   └── vite.config.ts
├── docs/                     ← Documentación técnica
├── README.md                 ← Documentación principal
└── .gitignore
```

---

## Configuración del Backend

### Variables de entorno y properties

El backend lee su configuración desde `application.properties` y `application-supabase.properties`. Las variables de entorno tienen prioridad sobre los valores por defecto.

| Variable | Descripción | Valor por defecto | Obligatoria |
|----------|-------------|-------------------|-------------|
| `JWT_SECRET` | Clave HMAC-256 para firmar tokens | Valor de desarrollo | Sí (cambiar en prod) |
| `JWT_EXPIRATION` | Expiración del token (ms) | `86400000` (24h) | No |
| `CAPTCHA_ENABLED` | Activar validación PuzzleCaptcha | `true` | No |
| `PAYMENTS_MODE` | Modo de pagos | `simulated` | No |
| `PAYMENTS_ENABLED` | Habilitar módulo de pagos | `true` | No |
| `RUC_PROVIDER` | Proveedor de consulta RUC | `apiperudev` | No |
| `APIPERUDEV_API_TOKEN` | Token de ApiPeruDev | (vacío) | Sí, para consultas RUC reales |
| `APIPERUDEV_API_BASE_URL` | URL base de ApiPeruDev | `https://apiperu.dev/api` | No |
| `ALLOWED_ORIGINS` | Orígenes permitidos para CORS | `http://localhost:8081` | Sí (en prod) |

### Base de datos con Supabase

El perfil `supabase` configura la conexión a PostgreSQL en la nube:

```properties
spring.datasource.url=jdbc:postgresql://<host>:6543/postgres?sslmode=require&prepareThreshold=0
spring.datasource.username=postgres.<project-ref>
spring.datasource.password=<contraseña>
```

**Notas importantes:**
- Usar el **Transaction Pooler** de Supabase (puerto `6543`), no la conexión directa.
- El parámetro `prepareThreshold=0` es necesario para compatibilidad con el pooler.
- SSL es requerido (`sslmode=require`).
- Hibernate gestiona el esquema automáticamente con `ddl-auto=update`.

### Compilar y ejecutar

```powershell
cd EcoTacnaSpringBootJPA

# Compilar
.\mvnw.cmd clean compile

# Empaquetar (sin tests)
.\mvnw.cmd package -DskipTests

# Iniciar servidor (puerto 8082)
.\mvnw.cmd spring-boot:run
```

Para activar el perfil Supabase:
```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=supabase
```

El backend estará disponible en: `http://localhost:8082/ecotacna/api`

---

## Configuración del Frontend

### Variables de entorno

Crear un archivo `.env` en `EcoTacnaFrontend/` (o usar las variables directamente):

| Variable | Descripción | Valor por defecto |
|----------|-------------|-------------------|
| `VITE_API_BASE_URL` | URL base del backend | `http://localhost:8082/ecotacna/api` |
| `VITE_RUC_PROVIDER` | Proveedor de consulta RUC | `apiperudev` |

### Instalar y ejecutar

```powershell
cd EcoTacnaFrontend

# Instalar dependencias
npm install

# Modo desarrollo (puerto 8081)
npm run dev

# Verificar tipos TypeScript
npx tsc --noEmit

# Ejecutar linter
npm run lint

# Build de producción
npm run build
```

El frontend estará disponible en: `http://localhost:8081`

---

## Validación local completa

Una vez que backend y frontend están corriendo:

### Backend
```powershell
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile
.\mvnw.cmd package -DskipTests
# Esperado: compilación y empaquetado exitosos
```

> La entrega final no incluye tests automatizados. La validación operativa se realiza mediante compilación, empaquetado y prueba manual de los flujos.

### Frontend
```powershell
cd EcoTacnaFrontend
npm run lint       # Esperado: 0 errores
npx tsc --noEmit   # Esperado: sin errores
npm run build      # Esperado: build exitoso
```

---

## CORS

El backend controla qué dominios pueden hacer peticiones mediante la variable `ALLOWED_ORIGINS`.

- **Desarrollo:** `http://localhost:8081`
- **Producción:** Configurar con el dominio real del frontend desplegado.

Si el frontend y backend se despliegan en dominios diferentes, asegurarse de que `ALLOWED_ORIGINS` incluya el dominio del frontend.

---

## Advertencias de producción

> ⚠️ **JWT_SECRET:** El valor por defecto es solo para desarrollo. Generar una clave segura y única para producción.

> ⚠️ **APIPERUDEV_API_TOKEN:** Obtener un token válido en [https://apiperu.dev](https://apiperu.dev) para que las consultas de RUC funcionen en producción.

> ⚠️ **Archivo .env:** No subir archivos `.env` con secretos reales al repositorio. Usar variables de entorno del servidor o un gestor de secretos.

> ⚠️ **HTTPS:** En producción, configurar HTTPS obligatorio. Los tokens JWT viajan en headers HTTP y deben estar cifrados en tránsito.

> ⚠️ **Pagos:** Mientras `PAYMENTS_MODE=simulated`, los pagos no son reales. Si se integra una pasarela real en el futuro, cambiar esta configuración y nunca exponer claves de pasarela en el código fuente.

> ⚠️ **Base de datos:** No exponer credenciales de Supabase en el repositorio. Usar variables de entorno.

---

## Resumen de puertos

| Servicio | Puerto | URL |
|----------|--------|-----|
| Backend Spring Boot | 8082 | `http://localhost:8082/ecotacna/api` |
| Frontend React/Vite | 8081 | `http://localhost:8081` |
| PostgreSQL (Supabase Pooler) | 6543 | Configurado en properties |
