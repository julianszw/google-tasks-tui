# Tareas — 033 settings-section

## Modelo y persistencia
- [x] `Settings` con `hideEmptyLists` y `hideCompletedTasks` (default `false`)
- [x] `SettingsStore` con Properties → `settings.properties` (load/save best-effort)

## Interfaz
- [x] `SettingsWindow` (engranaje `⚙`, toggles, navegación cíclica, `Enter` alterna, `Esc` cierra)
- [x] `TaskListWindow`: usar `Settings`, filtrar completadas, tecla `s`, quitar `h`
- [x] `TaskViewRenderer`: barra de atajos sin `h`, con `s`
- [x] `App`/`LanternaTaskTrackerView`: inyectar `Settings` + `SettingsStore`

## Configuración
- [x] Añadir `settings.properties` a `.gitignore`

## Verificación
- [x] `mvn test`
