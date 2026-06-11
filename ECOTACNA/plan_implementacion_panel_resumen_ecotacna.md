# Plan de implementación — Rediseño del Panel Resumen EcoTacna

## 1. Propósito

Este documento define un plan ordenado para reconstruir el módulo **Resumen / Panel de control institucional** de EcoTacna, tomando como referencia la imagen visual esperada que se entregará a Antigravity.

El objetivo es reemplazar el panel actual, que todavía contiene apartados vacíos, textos genéricos o secciones pendientes, por un dashboard real enfocado en:

- ingresos mensuales de la plataforma;
- suscripciones activas y próximas renovaciones;
- cobros próximos y monto por cobrar;
- movimiento bruto por pagos de aceite entre usuarios;
- litros de aceite comercializados;
- estado real de suscripciones;
- resumen ejecutivo financiero y operativo.

El trabajo debe hacerse por bloques pequeños para evitar errores, reducir consumo de tokens y mantener control sobre cada cambio.

---

## 2. Regla principal del trabajo

Antes de modificar código, Antigravity debe leer y respetar los documentos del proyecto ubicados en:

```txt
C:\Users\MILTONHFLORESCHINO\Desktop\ECOTACNA\ECOTACNA\docs
```

Especialmente:

- `01-arquitectura`
- `02-flujo-funcional`
- `03-seguridad`
- `04-despliegue`

Todo cambio debe respetar la arquitectura existente: backend Spring Boot, frontend React/Vite/Tailwind, DTOs limpios, servicios separados, repositorios controlados, seguridad por roles y consumo de datos reales desde Supabase/PostgreSQL.

---

## 3. Alcance funcional del nuevo resumen

El nuevo Panel Resumen debe mostrar información real que ya existe o puede calcularse desde los módulos trabajados:

### 3.1 Métricas superiores

1. **Ingresos del mes**  
   Suma de pagos confirmados del mes actual. Debe incluir ingresos reales de la plataforma por suscripciones y cobros reales si existen.

2. **Suscripciones activas**  
   Cantidad de empresas/generadoras y recolectoras con suscripción activa o estado equivalente.

3. **Cobros próximos**  
   Cantidad de suscripciones que vencen dentro de los próximos 7 días.

4. **Monto por cobrar**  
   Suma del valor de esas renovaciones próximas.

5. **Pagos por aceite registrados**  
   Movimiento bruto registrado entre generadores y recolectores por compra de aceite.  
   Importante: este dato representa movimiento económico dentro de la plataforma. Si no todo ese monto es ingreso directo de EcoTacna, debe nombrarse como **movimiento bruto por aceite**, no como ingreso neto de la empresa.

6. **Litros de aceite comercializados**  
   Suma real de litros confirmados o completados en solicitudes/recojos pagados.

---

## 4. Reglas económicas vigentes

Estas reglas deben quedar centralizadas y no duplicadas en varios archivos.

```txt
GENERADORA / restaurante: S/ 29.90 mensual
GENERADORA / restaurante: 7 días de prueba gratuita al registrarse por primera vez
RECOLECTORA: S/ 299.90 mensual
```

No deben quedar activos valores antiguos como:

```txt
S/ 20.00
S/ 45.00
Gratis como plan mensual definitivo
```

La palabra **Gratis** solo puede usarse para indicar la prueba inicial de 7 días de generadoras/restaurantes.

---

## 5. Información que NO debe incluirse

El nuevo panel no debe conservar secciones que no tengan datos reales o que no aporten a la administración financiera/operativa actual.

Eliminar del módulo Resumen:

- `Pendiente de integración backend`
- mapas operativos sin GPS real;
- zonas o distritos;
- litros por distrito;
- placeholders visuales;
- datos mock;
- paneles enormes vacíos;
- accesos rápidos genéricos si no son realmente necesarios;
- listas inferiores inventadas;
- datos hardcodeados solo para verse bonitos.

El dashboard debe mostrar menos secciones, pero más útiles.

---

## 6. Diferencia entre ingreso de EcoTacna y movimiento bruto

Para evitar confusiones de negocio, el dashboard debe distinguir:

### Ingresos de EcoTacna

Dinero que entra a la empresa EcoTacna, por ejemplo:

- suscripciones de generadoras/restaurantes;
- suscripciones de recolectoras;
- cobros administrativos o certificados, solo si existen datos reales;
- comisiones, solo si existen en el modelo.

### Movimiento bruto por aceite

Dinero registrado en la plataforma entre usuarios, por ejemplo:

- pagos realizados por recolectores a generadores por litros de aceite;
- montos totales de recojos completados/pagados.

Este dato debe ser visible porque es importante para medir el volumen económico del sistema, pero no debe mezclarse como ingreso neto de EcoTacna si el modelo no lo define así.

---

## 7. Imagen de referencia visual

Antigravity recibirá una imagen del resultado visual esperado.

Debe usarse como guía para:

- composición visual;
- tarjetas superiores;
- gráficos de dona;
- gráfico mensual;
- próximos cobros;
- resumen clave;
- estilo EcoTacna.

La imagen es una referencia de diseño, no una fuente de datos. Los valores finales deben venir del backend real.

---

# PLAN POR BLOQUES

No ejecutar todo en una sola tarea. Cada bloque debe terminar con validación y reporte antes de pasar al siguiente.

---

# BLOQUE 1 — Limpieza del módulo Resumen

## Objetivo

Eliminar del módulo Resumen todo lo que sea mock, placeholder o sección no útil para el nuevo dashboard.

## Prompt para Antigravity

```md
Necesito limpiar quirúrgicamente el módulo Resumen / Panel de control institucional de EcoTacna.

Antes de programar, lee y respeta las reglas ubicadas en:

C:\Users\MILTONHFLORESCHINO\Desktop\ECOTACNA\ECOTACNA\docs

Especialmente:
- 01-arquitectura
- 02-flujo-funcional
- 03-seguridad
- 04-despliegue

Objetivo:
Eliminar del módulo Resumen todos los textos, paneles y lógicas mock o placeholder que no aportan al nuevo dashboard financiero/operativo.

Buscar en todo el proyecto, excluyendo node_modules, dist, target y .git:

- mock
- Mock
- placeholder
- Pendiente de integración backend
- Mapa operativo territorial
- Litros recolectados por distrito
- Recolecciones por mes
- Solicitudes por estado
- Sin GPS
- datos de prueba
- demo
- fake
- hardcoded

Revisar principalmente:

Frontend:
- EcoTacnaFrontend/src/pages/admin
- EcoTacnaFrontend/src/components
- EcoTacnaFrontend/src/services/adminApi.ts
- rutas del panel administrador

Backend:
- AdminDashboardController.java
- AdminDashboardService.java
- DTOs de resumen o dashboard
- repositorios usados por dashboard

Eliminar del módulo Resumen:
- paneles de zonas/distritos;
- mapa territorial;
- textos “pendiente de integración backend”;
- datos hardcodeados;
- secciones inferiores que no aporten al resumen financiero;
- cards con datos ficticios.

No borrar:
- entidades reales;
- tabla users;
- empresas;
- recolectoras;
- pagos;
- suscripciones;
- solicitudes;
- configuraciones de login o seguridad.

Si hay datos basura en BD relacionados al resumen, solo reportarlos. No borrar datos de BD en este bloque.

Validación:

Frontend:
cd EcoTacnaFrontend
npm run lint
npx tsc --noEmit
npm run build

Backend, solo si se tocó código backend:
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile

Entregable:
- archivos limpiados;
- secciones eliminadas;
- confirmación de que no queda “Pendiente de integración backend”;
- confirmación de que no se tocaron datos reales;
- build frontend OK;
- compile backend OK si aplica.
```

---

# BLOQUE 2 — Auditoría de datos reales y contrato del endpoint

## Objetivo

Definir qué datos reales se pueden obtener desde la base de datos y crear el contrato del endpoint que alimentará el dashboard.

## Endpoint propuesto

```http
GET /api/admin/resumen-institucional
```

Ruta real con context path:

```http
GET http://localhost:8082/ecotacna/api/admin/resumen-institucional
```

## Prompt para Antigravity

```md
Necesito auditar los datos reales disponibles en la base de datos y definir el contrato del nuevo endpoint del Panel Resumen.

No implementar todavía el diseño visual. Primero necesitamos un endpoint confiable con datos reales.

Endpoint objetivo:
GET /api/admin/resumen-institucional

Debe requerir rol ADMIN y devolver un DTO limpio, no entidades JPA.

Métricas necesarias:

1. ingresosDelMes
Suma real de pagos confirmados del mes actual. Separar, si es posible, ingresos de plataforma y movimiento bruto por aceite.

2. suscripcionesActivas
Cantidad de suscripciones activas, separando generadoras y recolectoras si el modelo lo permite.

3. cobrosProximos
Cantidad de suscripciones que vencen en los próximos 7 días.

4. montoPorCobrar
Suma de los cobros próximos.
Reglas:
- GENERADORA: S/ 29.90
- RECOLECTORA: S/ 299.90

5. pagosPorAceiteRegistrados
Suma real de pagos por aceite entre usuarios. Nombrar como movimiento bruto si no representa ingreso neto de EcoTacna.

6. litrosAceiteComercializados
Suma real de litros confirmados o completados.

7. ingresosMensuales
Lista enero-diciembre con ingresos agrupados por mes del año actual.

8. composicionIngresos
Debe separar:
- suscripciones generadoras;
- suscripciones recolectoras;
- pagos por aceite registrados;
- otros cobros, solo si existen datos reales.

9. estadoSuscripciones
Distribución por estado:
- activas;
- prueba activa;
- pendiente de pago;
- pendiente;
- canceladas si aplica.

10. proximosCobros
Lista ordenada por fecha de vencimiento ascendente, con:
- empresa;
- RUC;
- tipo;
- fecha de vencimiento;
- monto;
- estado.

11. resumenClave
- próximo ingreso estimado;
- mayor fuente de ingreso;
- ticket mensual por recolectora: S/ 299.90;
- ticket mensual por generadora: S/ 29.90;
- nota de prueba gratis de 7 días para generadoras.

Ejecutar consultas de diagnóstico, ajustando nombres de columnas reales:

SELECT subscription_status, company_type, COUNT(*)
FROM companies
GROUP BY subscription_status, company_type
ORDER BY company_type, subscription_status;

SELECT *
FROM subscriptions
ORDER BY id DESC
LIMIT 20;

SELECT *
FROM payments
ORDER BY id DESC
LIMIT 20;

SELECT *
FROM pickup_requests
ORDER BY id DESC
LIMIT 20;

Crear DTOs limpios, por ejemplo:
- AdminInstitutionalSummaryResponse
- AdminKpiResponse
- MonthlyRevenuePoint
- IncomeCompositionPoint
- UpcomingChargeResponse
- SubscriptionStatusPoint

El endpoint debe devolver wrapper estándar:

{
  "success": true,
  "message": "Resumen institucional",
  "data": { }
}

No inventar datos. Si una métrica no tiene datos reales, devolver 0 o lista vacía.
No usar zonas/distritos.
No usar mocks.
No usar valores S/ 20.00 ni S/ 45.00.

Validar con curl:

$TOKEN="TOKEN_ADMIN"
curl.exe -i -H "Authorization: Bearer $TOKEN" `
  http://localhost:8082/ecotacna/api/admin/resumen-institucional

Validación backend:
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile

Entregable:
- SQL usado para validar datos;
- DTOs creados;
- endpoint creado;
- JSON de ejemplo real devuelto;
- backend compile OK.
```

---

# BLOQUE 3 — Implementación backend de métricas reales

## Objetivo

Implementar los cálculos reales en backend para alimentar el dashboard.

## Prompt para Antigravity

```md
Necesito implementar la lógica real del endpoint:

GET /api/admin/resumen-institucional

Ya no debe existir lógica mock ni placeholder. Todo debe calcularse desde Supabase/PostgreSQL.

Reglas de negocio:
- GENERADORA / restaurante: S/ 29.90 mensual.
- GENERADORA / restaurante: 7 días de prueba gratis.
- RECOLECTORA: S/ 299.90 mensual.
- No usar S/ 20.00 ni S/ 45.00.

Métricas obligatorias:

1. Ingresos del mes
Pagos confirmados del mes actual.
Si hay tipos de pago, separar suscripciones y pagos por aceite.

2. Suscripciones activas
Contar suscripciones activas o empresas con estado equivalente.

3. Cobros próximos
Suscripciones que vencen en los próximos 7 días.

4. Monto por cobrar
Suma del monto mensual de esas suscripciones próximas.

5. Pagos por aceite registrados
Suma de pagos asociados a recojos completados/pagados.
Debe tratarse como movimiento bruto si no es ingreso directo de EcoTacna.

6. Litros comercializados
Suma de litros confirmados en recojos completados.

7. Ingresos mensuales
Agrupado por mes del año actual.

8. Composición de ingresos
- suscripciones generadoras;
- suscripciones recolectoras;
- pagos por aceite;
- otros reales si existen.

9. Estado de suscripciones
- activas;
- prueba activa;
- pendiente de pago;
- pendiente;
- canceladas si aplica.

10. Próximos cobros
Máximo 5 o 10 registros, ordenados por fecha de vencimiento ascendente.

Implementación:

1. Revisar repositorios existentes:
- PaymentRepository
- SubscriptionRepository
- PickupRequestRepository
- CompanyRepository

2. Crear métodos de repositorio limpios.
Usar JPQL si es claro. Usar consulta nativa solo si es más seguro por nombres reales de columnas.

3. No meter lógica pesada en controller.
Usar AdminDashboardService o crear AdminInstitutionalSummaryService.

4. Si una métrica no tiene datos:
- devolver 0;
- no fallar;
- no inventar datos.

5. Si hay registros legacy con montos antiguos, no corregirlos en este bloque. Solo reportarlos.

Validación:
- comparar endpoint contra SQL directo;
- probar con token admin;
- confirmar que no hay 500 por datos nulos;
- confirmar que no se usan mocks.

Backend:
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile

Entregable:
- archivos modificados;
- queries o métodos de repositorio creados;
- JSON real de respuesta;
- confirmación de valores calculados;
- backend compile OK.
```

---

# BLOQUE 4 — Rediseño frontend del Panel Resumen

## Objetivo

Implementar el diseño visual final usando el endpoint real.

## Prompt para Antigravity

```md
Necesito rediseñar el frontend del módulo Resumen usando el endpoint real:

GET /api/admin/resumen-institucional

Usaré como referencia visual la imagen adjunta del resultado esperado. El diseño debe respetar esa intención visual, pero los datos deben venir del backend real.

Objetivo visual:

El panel debe mostrar:

1. Fila superior de KPIs:
- Ingresos del mes.
- Suscripciones activas.
- Cobros próximos.
- Monto por cobrar.
- Pagos por aceite registrados.
- Litros de aceite comercializados.

2. Gráfico de ingresos mensuales.
3. Dona de composición de ingresos.
4. Tabla compacta de próximos cobros.
5. Dona o gráfico compacto de estado de suscripciones.
6. Resumen clave inferior.

Archivos probables:
- EcoTacnaFrontend/src/pages/admin/AdminResumen.tsx
- EcoTacnaFrontend/src/services/adminApi.ts
- componentes reutilizables en src/components
- tipos en src/types.ts si aplica

Agregar en adminApi.ts:

getInstitutionalSummary()

Debe usar apiClient existente y token actual.

Componentes sugeridos:
- StatCard
- SimpleBarChart
- DonutChart
- UpcomingChargesCard
- SubscriptionStatusCard
- InsightCard

No instalar librerías nuevas si no es necesario.
Si ya existe una librería de gráficos, usarla.
Si no existe, usar SVG/CSS simple para barras y dona, para evitar dependencias.

Reglas visuales:
- mantener estilo EcoTacna;
- tarjetas blancas con sombra suave;
- bordes redondeados;
- verde principal;
- ámbar para pendientes/cobros próximos;
- azul para pagos o montos;
- rojo solo para vencidos/rechazados;
- nada de placeholders;
- nada de zonas/distritos;
- nada de “pendiente de backend”.

Estados frontend:
- loading;
- error real;
- empty state solo si de verdad no hay datos;
- no transformar errores en ceros falsos.

No mostrar valores hardcodeados de la imagen si el backend devuelve otros.
La imagen es referencia de diseño, no fuente de datos.

Validación manual:
1. Login admin.
2. Entrar a Resumen.
3. Ver KPIs con datos reales.
4. Ver gráficos cargados.
5. Ver próximos cobros.
6. Ver composición de ingresos.
7. Recargar página y confirmar persistencia.
8. Apagar backend y confirmar que aparece error real, no dashboard falso.

Validación técnica:
cd EcoTacnaFrontend
npm run lint
npx tsc --noEmit
npm run build

Backend si se toca:
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile

Restricciones:
- No tocar ApiPeruDev.
- No tocar registro.
- No tocar login.
- No tocar BCrypt.
- No tocar SecurityConfig.
- No borrar datos.
- No usar mocks.
- No crear datos ficticios.
- No mostrar zonas/distritos.

Entregable:
- captura del nuevo panel Resumen;
- archivos modificados;
- componentes creados;
- confirmación de consumo de endpoint real;
- frontend build OK.
```

---

# BLOQUE 5 — Validación cruzada contra base de datos

## Objetivo

Confirmar que el dashboard muestra valores reales y coherentes con SQL directo.

## Prompt para Antigravity

```md
Necesito validar el nuevo Panel Resumen contra la base de datos real y cerrar la implementación.

Tareas:

1. Validar métricas del dashboard con SQL directo.
Ajustar nombres de columnas reales según el esquema.

Ejemplos:

SELECT COUNT(*)
FROM subscriptions
WHERE status = 'ACTIVA';

SELECT SUM(amount)
FROM payments
WHERE status = 'PAGADO'
  AND created_at >= date_trunc('month', CURRENT_DATE);

SELECT SUM(monto_total)
FROM pickup_requests
WHERE estado_pago = 'PAGADO';

SELECT SUM(litros_confirmados)
FROM pickup_requests
WHERE estado = 'COMPLETADO';

2. Validar próximos cobros:
- fechas próximas;
- monto S/ 29.90 para generadora;
- monto S/ 299.90 para recolectora.

3. Validar que no existan strings mock:
Buscar en todo el proyecto, excluyendo node_modules, dist, target y .git:

- Pendiente de integración backend
- mock
- fake
- demo
- Mapa operativo
- Sin GPS
- S/ 20.00
- S/ 45.00

4. Confirmar que el dashboard distingue:
- ingresos directos de EcoTacna;
- movimiento bruto por aceite entre usuarios.

5. Confirmar que no hay zonas/distritos.

6. Ejecutar builds finales:

Backend:
cd EcoTacnaSpringBootJPA
.\mvnw.cmd clean compile

Frontend:
cd EcoTacnaFrontend
npm run lint
npx tsc --noEmit
npm run build

Entregable:
- captura del dashboard final;
- JSON real del endpoint;
- SQL de validación;
- lista de archivos modificados;
- confirmación de que no quedan mocks/placeholders;
- backend compile OK;
- frontend lint/tsc/build OK.
```

---

# BLOQUE 6 — Documentación final en `docs`

## Objetivo

Actualizar la documentación del proyecto para que el nuevo Resumen quede registrado en los archivos `.md` del sistema.

## Prompt para Antigravity

```md
Necesito documentar el nuevo Panel Resumen institucional en los archivos .md del proyecto.

Leer y actualizar según corresponda:

C:\Users\MILTONHFLORESCHINO\Desktop\ECOTACNA\ECOTACNA\docs

Principalmente:
- 01-arquitectura
- 02-flujo-funcional
- 04-despliegue

Documentar:

1. Nuevo endpoint:
GET /api/admin/resumen-institucional

2. Métricas principales:
- ingresos del mes;
- suscripciones activas;
- cobros próximos;
- monto por cobrar;
- pagos por aceite registrados como movimiento bruto;
- litros comercializados;
- ingresos mensuales;
- composición de ingresos;
- estado de suscripciones;
- próximos cobros.

3. Reglas de precios:
- GENERADORA/restaurante: S/ 29.90 mensual;
- 7 días de prueba gratuita para generadoras;
- RECOLECTORA: S/ 299.90 mensual.

4. Aclaración financiera:
- ingresos de EcoTacna y movimiento bruto por aceite no deben mezclarse si el modelo no define comisión.

5. Secciones eliminadas:
- zonas/distritos;
- mapa territorial;
- placeholders de backend pendiente.

6. Validación:
- backend compile OK;
- frontend build OK;
- datos reales desde Supabase.

No incluir tokens, claves, credenciales ni capturas con datos sensibles.

Entregable:
- archivos docs modificados;
- resumen breve de cambios documentados;
- confirmación de que no se expusieron credenciales.
```

---

# 8. Recomendación de ejecución para ahorrar tokens

No enviar todos los prompts juntos.

Orden recomendado:

1. Enviar **BLOQUE 1**.
2. Revisar reporte y confirmar que no se rompió nada.
3. Enviar **BLOQUE 2**.
4. Pedir JSON de ejemplo del endpoint.
5. Enviar **BLOQUE 3**.
6. Validar con curl.
7. Enviar **BLOQUE 4** junto con la imagen visual esperada.
8. Validar pantalla.
9. Enviar **BLOQUE 5**.
10. Enviar **BLOQUE 6**.

Cada bloque debe cerrarse con evidencia antes de pasar al siguiente.

---

# 9. Definición de terminado

El nuevo Panel Resumen se considera terminado solo si cumple todo esto:

- no quedan placeholders visibles;
- no queda “Pendiente de integración backend”;
- no hay zonas/distritos;
- los KPIs vienen del backend real;
- los gráficos consumen datos reales;
- se muestran próximos cobros reales;
- se distingue ingreso de EcoTacna vs movimiento bruto por aceite;
- precios correctos: S/ 29.90 y S/ 299.90;
- generadoras mantienen prueba gratis de 7 días;
- no aparecen S/ 20.00 ni S/ 45.00;
- el frontend maneja errores reales;
- backend compila;
- frontend lint/tsc/build pasa;
- docs actualizados.

