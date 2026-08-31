# 033 — settings-section

## Why
Los ajustes de la vista (ocultar listas vacías y ocultar tareas completadas) no
tenían un lugar centralizado. "Ocultar listas vacías" se activaba con la tecla `h`
de forma efímera (solo sesión). Se introduce una sección de Settings accesible con
`s`, con ícono de engranaje, que centraliza estos toggles y los persiste entre
sesiones.

## What Changes
- Nuevo modelo `Settings` (`hideEmptyLists`, `hideCompletedTasks`).
- Nuevo `SettingsStore` que persiste en `settings.properties` (Properties, sin
  dependencias nuevas) con carga/guardado best-effort.
- Nueva `SettingsWindow`: título con engranaje `⚙`, lista de items (toggles, cuenta,
  tema muerto), navegación cíclica `↑`/`k`/`↓`/`j`, `Enter` alterna, `Esc` cierra.
- `TaskListWindow`: usa `Settings` en lugar del campo `hideEmptyLists`; filtra
  tareas completadas cuando el ajuste está activo; tecla `s` abre Settings; se quita
  la tecla `h`.
- `TaskViewRenderer` (barra de atajos): se quita `h`, se agrega `s`.
- `App`/`LanternaTaskTrackerView`: cargan e inyectan `Settings` + `SettingsStore`.

## Impact
- Specs: `settings` (nueva), `hide-empty-lists`, `hide-completed-tasks` (nueva) e
  `interactive-cli` ya definen el comportamiento.
- Código: `model/Settings`, `service/SettingsStore`, `cli/SettingsWindow`,
  `cli/TaskListWindow`, `cli/TaskViewRenderer`, `cli/LanternaTaskTrackerView`, `App`.
- Tests: `SettingsStoreTest`, `SettingsWindowTest`, `TaskListWindowTest`.
