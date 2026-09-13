# Tareas

- [x] Agregar `AuthenticationExpiredException` (subclase de `ProviderException`) en `tasktracker.provider` para señalizar credencial inválida/revocada y la necesidad de re-autenticación.
- [x] Actualizar `GoogleTasksProvider.toProviderException()` (antes `handleException`) para detectar `invalid_grant` y lanzar `AuthenticationExpiredException`:
  - [x] Caso `TokenResponseException` con `getDetails().getError() == "invalid_grant"` (camino real del endpoint de token).
  - [x] Caso defensivo `GoogleJsonResponseException` con `getDetails().getErrors()[].reason == "invalid_grant"`.
  - [x] Mantener el mensaje genérico para el resto de excepciones.
- [x] Crear el decorador `ReauthenticatingTaskProvider` (capa `provider`) que envuelve el `TaskProvider`:
  - [x] Ante `AuthenticationExpiredException`, llamar `auth.signOut()` + `auth.authorize()` y reconstruir el proveedor delegado con la credencial fresca.
  - [x] Limitar los intentos de re-autenticación para evitar bucles infinitos.
  - [x] Cubrir tanto operaciones que devuelven valor como operaciones `void`.
- [x] Ajustar `App.run()` para construir el `TaskService` sobre `ReauthenticatingTaskProvider` (cubriendo arranque y sesión interactiva).
- [x] Asegurar que `GoogleTasksProvider`/`Tasks` se reconstruyan con la credencial recién autorizada (no reutilizar la instancia previa).
- [x] Agregar/ajustar tests:
  - [x] Test de detección de `invalid_grant` vía `TokenResponseException`.
  - [x] Test de detección de `invalid_grant` vía `GoogleJsonResponseException`.
  - [x] Test del flujo de re-autenticación y reconstrucción del delegado con credencial fresca.
  - [x] Test del overload `void` (operaciones como `deleteTaskList`).
  - [x] Test de operación con argumentos (`createTask`) que verifica el paso de argumentos al delegado fresco.
- [x] Verificar que los tests existentes siguen pasando y ejecutar la suite (`mvn test`).
