# Code Review — fix-expired-oauth-token-reauth (re-revisión)

## Resumen
Re-revisión tras aplicar los hallazgos del informe anterior. El rediseño con el
decorador `ReauthenticatingTaskProvider` es correcto y superior al fix inicial:
mueve la re-autenticación a la capa `provider`, de modo que **todas** las
operaciones del `TaskService` (arranque y sesión interactiva) re-autentican ante
`AuthenticationExpiredException`. El reintento relee el delegado fresco (no el
viejo), está acotado (máx. 1 re-autenticación, sin bucles infinitos) y preserva la
causa. Los tests nuevos son sólidos y cubren el bug de captura-por-valor.

Veredicto: **APROBADO**.

Conteo: 0 críticos, 0 altos, 0 medios, 3 bajos (ninguno bloqueante).

## Verificación de los puntos pedidos

1. **Delegación de las 9 operaciones de `TaskProvider`** — Correcta. Las 8
   operaciones de negocio delegan vía `withReauth` y `providerName()` delega
   directo (sin red). Los `void` (`deleteTaskList`, `deleteTask`) usan el overload
   `withReauth(Runnable)`.
2. **El reintento relee el delegado fresco** — Correcto. Los lambdas referencian el
   campo `delegate` (no una variable local capturada), y tras `delegate =
   providerFactory.get()` el bucle vuelve a invocar `operation.get()`, que lee el
   campo actualizado. El test `rebuildsProviderWithFreshCredentialAfterReauth`
   verifica que se construyen 2 delegados y que el segundo es el que responde.
3. **Sin bucles infinitos** — Correcto. `reauths` se incrementa una vez por
   re-autenticación y al superar `MAX_REAUTH_ATTEMPTS` (1) se lanza, propagando la
   excepción envuelta.
4. **Preservación de la causa** — Correcta. El rethrow final envuelve el
   `AuthenticationExpiredException` interno como causa, que a su vez conserva la
   `IOException` original (verificado por `toProviderExceptionPreservesOriginalCause`).
5. **`App.java` coherente** — Sí. Elimina `executeWithReauth`/`MAX_REAUTH_ATTEMPTS`;
   `loadTaskService` queda trivial construyendo `TaskService` sobre el decorador.
6. **Tests nuevos** — `ReauthenticatingTaskProviderTest` cubre reconstrucción del
   delegado, tope de reintentos, no-reauth para otras excepciones y delegación de
   `providerName()`. `GoogleTasksProviderTest` cubre `details == null` y causa.

## Hallazgos remanentes

### 1. [baja] `tasks.md` desactualizado respecto al diseño final
- Archivo: `openspec/changes/037-fix-expired-oauth-token-reauth/tasks.md:4,8-11`
- Problema: la checklist aún referencia el método renombrado
  `GoogleTasksProvider.handleException()` (ahora `toProviderException()`) y describe
  el enfoque original de reintento en `App.run()`, que fue reemplazado por el
  decorador `ReauthenticatingTaskProvider`. `proposal.md` sí está alineado.
- Principio: Clean Code / documentación — la fuente de verdad debe reflejar lo
  implementado.
- Cambio sugerido: actualizar `tasks.md` para nombrar `toProviderException()` y el
  decorador `ReauthenticatingTaskProvider` (y sus tests), en lugar del flujo ad-hoc
  de `App`.

### 2. [baja] Test acoplado a un fixture de otro paquete
- Archivo: `src/test/java/tasktracker/provider/ReauthenticatingTaskProviderTest.java:14`
- Problema: importa `tasktracker.FakeAccountProvider` (paquete `tasktracker`),
  creando un acoplamiento entre paquetes de test. Funciona porque ambos viven en
  test sources, pero es una dependencia frágil si el fixture se mueve/renombra.
- Principio: Clean Code — cohesión de paquetes / localización de fixtures.
- Cambio sugerido: mover `FakeAccountProvider` a un paquete compartido de test
  (p. ej. `tasktracker.testutil`) o duplicar un fixture local mínimo en
  `tasktracker.provider`.

### 3. [baja] El re-autenticado solo se ejerce vía `listTaskLists()`
- Archivo: `src/test/java/tasktracker/provider/ReauthenticatingTaskProviderTest.java`
- Problema: los cuatro tests de re-autenticación pasan por `listTaskLists()`; el
  overload `withReauth(Runnable)` (usado por `deleteTaskList`/`deleteTask`) y otras
  operaciones con parámetros no se ejercitan directamente. La delegación es
  uniforme, por lo que el riesgo es bajo.
- Principio: Clean Code / FIRST — cobertura de los caminos representativos.
- Cambio sugerido: agregar un test que dispare re-auth desde una operación `void`
  (p. ej. `deleteTask`) o con argumentos (`createTask`), para fijar el contrato del
  overload `Runnable`.

## Nota (no bloqueante)
La re-autenticación durante la sesión invoca `auth.authorize()` (flujo OAuth por
navegador) mientras la TUI Lanterna está activa; el texto OAuth se imprime a stdout
con el `Screen` activo. Ya está documentado como "limitación conocida" en
`proposal.md` y no rompe la funcionalidad.
