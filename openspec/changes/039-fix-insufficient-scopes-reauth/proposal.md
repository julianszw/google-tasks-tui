# 039 - Fix: re-autenticación ante scopes insuficientes

## Why

Cuando el token OAuth tiene scopes insuficientes (ej. autenticado con una versión anterior de la app), Google Tasks devuelve un HTTP 403 con `reason: "insufficientAuthenticationScopes"`. Actualmente, `GoogleTasksProvider.toProviderException()` solo detecta `invalid_grant` como caso re-autentiqueable. El error de scopes insuficientes no se detecta, por lo que `ReauthenticatingTaskProvider` no relanza el flujo OAuth y el usuario ve un error críptico.

## What Changes

- Se agrega el método `isInsufficientScopes()` en `GoogleTasksProvider` que detecta:
  - `GoogleJsonResponseException` con código 403 y `errors[].reason == "insufficientAuthenticationScopes"`
  - Fallback: mensaje que contiene `"insufficient authentication scopes"`
- Se modifica `toProviderException()` para incluir `isInsufficientScopes(e)` junto a `isInvalidGrant(e)` en la condición de re-autenticación

## Impact

- El usuario ya no verá el error críptico de scopes insuficientes
- `ReauthenticatingTaskProvider` relanzará automáticamente el flujo OAuth por navegador
- El usuario solo necesitará re-autenticarse una vez para obtener un token con los scopes correctos
- Sin cambios en la interfaz pública ni en la persistencia
