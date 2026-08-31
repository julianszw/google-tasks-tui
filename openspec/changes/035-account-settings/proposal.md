# 035 — account-settings

## Why
Settings debe poder configurar y consultar la cuenta de Google: mostrar el email
cuando hay una cuenta configurada, y permitir configurar una cuenta, cambiar de
cuenta o cerrar sesión.

## What Changes
- Nueva interfaz `AccountProvider` (`accountEmail`, `authorize`, `signOut`).
- `GoogleAuth` la implementa: agrega el scope `userinfo.email`, `accountEmail()`
  (consulta el endpoint `oauth2/v3/userinfo`, cacheado) y `signOut()` (borra
  `google-tokens/`); `authorize()` pasa a `void`.
- `SettingsWindow`: item "cuenta" que muestra el email o "configurar cuenta", con
  submenú "cambiar de cuenta"/"cerrar sesión". La autorización desde Settings
  suspende el `Screen` de Lanterna durante el flujo OAuth y cierra la app al
  cambiar la sesión para recargar con la nueva cuenta.
- `App`/`LanternaTaskTrackerView`/`TaskListWindow`: inyectan `AccountProvider`.

## Impact
- Specs: `settings` y `task-provider` (email de la cuenta) ya definen el
  comportamiento.
- Código: `provider/AccountProvider` (nueva), `google/GoogleAuth`,
  `cli/SettingsWindow`, `cli/TaskListWindow`, `cli/LanternaTaskTrackerView`, `App`.
- Tests: `FakeAccountProvider` (nuevo), `GoogleAuthTest` (nuevo), `SettingsWindowTest`.
