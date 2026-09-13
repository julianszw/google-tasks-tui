# Re-autenticación automática ante refresh token expirado/revocado

## Why
Al ejecutar `taskmaster`, si el refresh token de Google guardado ya expiró o fue revocado, al listar listas la app falla con un error genérico:

```
Error: No se pudieron listar las listas: 400 Bad Request
POST https://oauth2.googleapis.com/token
{ "error": "invalid_grant", "error_description": "Token has been expired or revoked." }
```

Causa raíz:
- `App.run()` confía en `auth.hasStoredCredentials()` (devuelve `true` porque el `StoredCredential` sigue en disco) y no re-autentica.
- `GoogleTasksProvider.listTaskLists()` intenta refrescar el access token con el refresh token inválido y Google responde `400 invalid_grant`.
- `GoogleTasksProvider.toProviderException()` solo detectaba `GoogleJsonResponseException`; el refresh inválido llega como `TokenResponseException` (subclase de `IOException`) y caía al mensaje genérico "400 Bad Request".
- `App.run()` captura `ProviderException` y termina la app.

## What Changes
- Nueva excepción de dominio (subclase de `ProviderException`, `AuthenticationExpiredException`) para señalizar que la credencial ya no es válida y se requiere re-autenticación.
- `GoogleTasksProvider.toProviderException()` detecta `invalid_grant` y lanza la excepción de re-autenticación en lugar del error genérico. El camino real es `TokenResponseException.getDetails().getError() == "invalid_grant"` (error del endpoint de token); se mantiene una defensa adicional para `GoogleJsonResponseException` con `getDetails().getErrors()[].reason == "invalid_grant"`, que no rompe nada y cubre respuestas inusuales o causas anidadas.
- Nuevo decorador `ReauthenticatingTaskProvider` (capa `provider`) que envuelve el `TaskProvider`: ante una `AuthenticationExpiredException` borra la credencial (`signOut()`), relanza el flujo OAuth (`authorize()`) y reconstruye el proveedor delegado con la credencial fresca, reintentando la operación con un límite de intentos (máx. 1 re-autenticación). De este modo la re-autenticación cubre tanto el arranque (`TaskService.load()`) como las operaciones durante la sesión (`completeTask`, `createTask`, etc.).
- `App.run()` construye el `TaskService` sobre `ReauthenticatingTaskProvider`, eliminando la lógica de reintento ad-hoc del arranque.
- Tests para la detección de `invalid_grant` (JSON y `TokenResponseException`), para la reconstrucción del proveedor con credencial fresca y para el flujo de re-autenticación.

## Impact
- No se modifican specs: `openspec/specs/task-provider/spec.md` ya cubre el requisito "Autenticación con Google" → escenario "Token expirado o revocado".
- Capas afectadas: `tasktracker.provider` (nueva excepción y decorador de re-autenticación), `tasktracker.google` (`GoogleTasksProvider`, `GoogleAuth`), `tasktracker.App` (flujo de arranque).
- Tests: `src/test/java/tasktracker/google/GoogleTasksProviderTest.java` y `src/test/java/tasktracker/provider/ReauthenticatingTaskProviderTest.java`.

## Limitación conocida
La re-autenticación durante la sesión se dispara desde el decorador de `provider`, que invoca `auth.authorize()` (flujo OAuth por navegador). Como ocurre con el cambio de cuenta en `SettingsWindow`, el flujo OAuth imprime a stdout mientras la TUI Lanterna está activa; es un defecto cosmético transitorio, no rompe la funcionalidad. La suspensión del `Screen` durante la re-autenticación queda pendiente para una iteración futura.
