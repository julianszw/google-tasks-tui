# Tareas — 035 account-settings

## Cuenta
- [x] `AccountProvider` (`accountEmail`, `authorize`, `signOut`)
- [x] `GoogleAuth` implementa `AccountProvider`: scope `userinfo.email`, `accountEmail()`, `signOut()`
- [x] `authorize()` pasa a `void`

## Interfaz
- [x] `SettingsWindow`: item "cuenta" (email / "configurar cuenta") + submenú cambiar/cerrar sesión
- [x] Suspender `Screen` durante OAuth desde Settings; cerrar app al cambiar sesión
- [x] Inyectar `AccountProvider` en `App`/`LanternaTaskTrackerView`/`TaskListWindow`

## Verificación
- [x] `mvn test`
