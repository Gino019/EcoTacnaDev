# Flujo Funcional — EcoTacna

Este documento describe paso a paso el flujo operativo completo de la plataforma EcoTacna, desde el registro de una empresa hasta la generación de reportes.

---

## 1. Registro de empresa generadora

1. El representante de la empresa accede a la página de registro.
2. Selecciona tipo de empresa: **Generadora**.
3. Ingresa el RUC de su empresa.
4. El sistema consulta automáticamente la información del RUC vía **ApiPeruDev**.
5. Resuelve el **PuzzleCaptcha** para demostrar que no es un bot.
6. Completa datos adicionales (nombre del usuario, email, contraseña).
7. Se registra la empresa y el usuario con estado **PENDIENTE**.

## 2. Registro de empresa recolectora

1. El representante de la empresa accede a la página de registro.
2. Selecciona tipo de empresa: **Recolectora**.
3. Ingresa el RUC de su empresa.
4. El sistema consulta automáticamente la información del RUC vía **ApiPeruDev**.
5. Resuelve el **PuzzleCaptcha**.
6. Completa datos adicionales.
7. Se registra la empresa y el usuario con estado **PENDIENTE**.

## 3. Consulta RUC con ApiPeruDev

- Al ingresar un RUC válido (11 dígitos), el frontend envía la consulta al backend.
- El backend invoca la API de ApiPeruDev con el token configurado.
- Si la consulta es exitosa, se autocompletan: razón social, dirección, departamento, provincia y distrito.
- Si `RUC_PROVIDER=mock`, se omite la llamada externa y se permite ingreso manual.

## 4. Panel Resumen Institucional (Administrador)

1. El administrador accede al panel de control (Dashboard).
2. El sistema carga los KPIs institucionales desde el backend (`/api/admin/resumen-institucional`).
3. Se visualizan las métricas reales calculadas:
   - **Ingresos del mes**: Suma de pagos aprobados en el mes actual.
   - **Suscripciones activas**: Conteo de empresas en estado `ACTIVA`.
   - **Monto por cobrar** y **Cobros próximos**: Suscripciones que vencen en los siguientes 7 días, mostrando montos exactos según el tipo de empresa (Generadora: S/ 29.90, Recolectora: S/ 299.90).
   - **Pagos aceite**: Suma total en dinero procesado de los recojos completados.
   - **Litros comercializados**: Sumatoria de todos los litros confirmados en recojos exitosos.
4. Gráficos dinámicos visualizan la evolución histórica de ingresos y su composición.

## 5. Aprobación administrativa y visualización de detalles

1. El administrador accede al listado de empresas / recolectores.
2. Ve la lista de empresas registradas con estado **PENDIENTE** o **ACTIVA**.
3. Revisa los datos básicos en la tabla. Al hacer clic en el **botón del ojo (Ver detalles)**, se abre una ficha detallada con:
   - Datos reales de la empresa y ubicación.
   - Datos de contacto y fecha de inscripción.
   - Estado de la licencia mensual y últimos pagos.
   - Resumen de actividad (litros acumulados y recojos).
4. En esta ficha detallada, el administrador puede descargar una **Ficha de empresa en PDF**.
5. Desde el panel de pendientes, aprueba o rechaza la empresa.
6. Al aprobar, la empresa cambia a estado **ACTIVA** (o **PENDIENTE_PAGO** según configuración).

## 5. Pago simulado de suscripción

1. La empresa aprobada accede a la sección de suscripción.
2. Visualiza el plan de suscripción y su precio mensual:
   - **Empresa Generadora/Restaurante**: S/ 29.90 / mes (con 7 días de prueba gratis).
   - **Empresa Recolectora**: S/ 299.90 / mes.
3. Realiza el "pago" de forma **simulada** (sin pasarela real).
4. El sistema registra la suscripción como **ACTIVA** (o **PRUEBA_ACTIVA**). El inicio de la licencia se marca a partir del momento de este pago simulado exitoso y su registro en base de datos.

> ⚠️ **Importante:** Los pagos son 100% simulados para fines académicos. No se debe ingresar datos de tarjetas reales.

## 6. Registro de unidad recolectora

1. El usuario recolector accede a la sección de unidades de transporte.
2. Registra una nueva unidad con: placa, tipo, marca, modelo, capacidad en litros.
3. El sistema valida que la placa sea única (case-insensitive).
4. La unidad queda disponible para asignar a recojos.

## 7. Solicitud de recojo

1. La empresa generadora (con suscripción activa) accede a "Solicitar Recojo".
2. Completa el formulario:
   - **Volumen aproximado** (litros estimados).
   - **Precio ofertado por litro** (cuánto ofrece pagar por litro recogido).
   - **Fecha programada**.
   - **Dirección**.
   - **Observaciones** (opcional).
3. El sistema calcula y muestra el **monto estimado**.
4. La solicitud se crea con estado **PENDIENTE**.

## 8. Precio ofertado por litro

- Es el valor en soles (S/) que la empresa generadora ofrece pagar por cada litro de aceite recogido.
- Es **obligatorio** al crear la solicitud.
- Se almacena en el campo `precioOfertadoPorLitro` de la entidad `PickupRequest`.
- Este precio se usa tanto para el cálculo del monto estimado como para el monto final.

## 9. Monto estimado

```text
Monto estimado = volumen aproximado × precio ofertado por litro
```

- Se muestra al momento de crear la solicitud como referencia.
- No es vinculante; el monto final depende de los litros realmente confirmados.

## 10. Visualización de la oferta por el recolector

1. El recolector accede a la sección de "Solicitudes Disponibles".
2. Ve las solicitudes pendientes con su información:
   - Empresa solicitante.
   - Volumen aproximado.
   - **Precio ofertado por litro**.
   - **Monto estimado**.
   - Dirección y fecha.
3. El recolector evalúa si la oferta es conveniente.

## 11. Aceptación / Rechazo

1. El recolector decide aceptar o rechazar la solicitud.
2. Al **aceptar**, la solicitud cambia a estado **ACEPTADA** y se asigna al recolector.
3. Al **rechazar**, la solicitud permanece disponible para otros recolectores.

## 12. Seguimiento del recojo

El flujo de estados de una solicitud es:

```text
PENDIENTE → ACEPTADA → EN_CAMINO → RECOGIDA → COMPLETADA
```

- **PENDIENTE:** Creada por la empresa, visible para todos los recolectores.
- **ACEPTADA:** Asignada a un recolector específico.
- **EN_CAMINO:** El recolector indica que está en tránsito.
- **RECOGIDA:** El recolector indica que ha recogido el aceite.
- **COMPLETADA:** Se han confirmado los litros y se ha calculado el pago.

La empresa generadora puede ver el estado en tiempo real desde su panel de seguimiento.

## 13. Confirmación de litros

1. Tras la recogida, el recolector ingresa los **litros realmente recogidos** (`litrosConfirmados`).
2. El backend calcula el **monto final** usando la fórmula:

```text
Monto final = litros confirmados × precio ofertado por litro
```

3. La solicitud pasa a estado **COMPLETADA** con el campo `estadoPago` actualizado.

## 14. Cálculo backend del monto final

- El monto es calculado **exclusivamente por el backend** para evitar manipulación.
- Se usa siempre el `precioOfertadoPorLitro` **original** de la solicitud (no puede cambiarse después de crear la solicitud).
- El campo `montoTotal` almacena el resultado.

## 15. Historial — Empresa generadora

- La empresa generadora puede ver el historial de **todas sus solicitudes** (completadas, pendientes, canceladas).
- Para cada solicitud se muestra:
  - Estado, fecha, volumen solicitado, precio ofertado, litros confirmados, monto final.
- Puede filtrar por rango de fechas.

## 16. Historial — Recolector

- El recolector puede ver el historial de **todos los recojos que ha atendido**.
- Para cada recojo se muestra:
  - Empresa, estado, fecha, litros confirmados, precio ofertado, monto final.
- Puede filtrar por rango de fechas.

## 17. Constancia PDF

- Disponible para solicitudes con estado **COMPLETADA** y pago confirmado.
- Genera un documento PDF con los datos de la operación:
  - Datos de la empresa generadora.
  - Datos del recolector.
  - Litros confirmados.
  - Precio ofertado por litro.
  - Monto total.
  - Fecha de la operación.
- Accesible tanto desde el panel de la empresa como del recolector.
- Cada rol solo puede descargar las constancias de sus propias operaciones.

> ⚠️ La constancia PDF es un documento referencial/operativo. No constituye un comprobante tributario ni factura electrónica.

## 18. Exportación Excel

- Descarga un archivo `.xlsx` con el historial completo de operaciones.
- Diferenciado por rol:
  - La **empresa generadora** descarga sus solicitudes con columnas: ID, estado, fecha, volumen, precio ofertado, litros confirmados, monto.
  - El **recolector** descarga sus recojos atendidos con columnas similares más la empresa asociada.
- Generado por el backend usando Apache POI.
