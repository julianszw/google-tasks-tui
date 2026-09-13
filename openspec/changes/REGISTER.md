# Registro de commits por change

Fuente de verdad que mapea cada `openspec/changes/<nnn>-<id>/` al commit (o
commits) que lo implementó. El agente `git-manager` actualiza este archivo después de
cada commit.

> Formato de mensaje de commit: `openspec <nnn>-<id>: <resumen>`.

## Pendientes de commit

_(ninguno)_

## Commiteados

| Change | Commit (SHA) | Resumen |
|--------|--------------|---------|
| `001-implement-task-management-and-cli` | `58c144d` | implementar task-management y cli-interface |
| `002-purge-completed-tasks` | `efd7486` | agregar comando purge |
| `003-pretty-output-formatting` | `996fc9f` | formatear salida en tabla con color y tachado |
| `004-interactive-navigation` | `00a21ea` | agregar navegación interactiva para list |
| `005-package-runnable-jar` | `36ae571` | empaquetar jar ejecutable con shade |
| `006-interactive-help-and-back` | `43d760e` | agregar ayuda permanente y tecla atrás |
| `007-lanterna-tui` | `5e0e93d` | refactor de la capa de presentación a Lanterna |
| `008-json-persistence` | `f1adc60` | persistir tareas en archivo json |
| `009-navigable-main-menu` | `439d551` | agregar menú principal navegable |
| `010-launch-scripts` | `7f5559a` | agregar scripts de lanzamiento |
| `011-unified-task-view` | `3c0dc1a` | unificar vista de tareas y quitar menú principal |
| `012-dark-theme` | `bdbecaa` | agregar tema oscuro alternable |
| `013-remove-unused-code` | `1440fe4` | eliminar capa de comandos cli no usada |
| `014-visual-style` | `684ca4e` | implementar lenguaje visual agentty |
| `015-remove-unused-files-and-code` | `8cce7bd` | eliminar archivos y código no usados |
| `016-task-action-menu` | `f3dc5b8` | agregar menú de acciones y edición de tareas |
| `017-app-logo` | `55fdc34` | agregar logo ascii task manager |
| `018-task-lists` | `2980601` | agregar listas de tareas con navegación tab |
| `019-move-task-completion-format-exit-confirmation` | `5def5b6` | agregar mover tarea entre listas, tachado de completadas y confirmación de salida |
| `020-implement-ui-zoom` | `7ec5ce9` | agregar zoom visual global con atajos ctrl de solo sesión |
| `020-google-tasks-sync` | `8e35da8` | agregar sincronización bidireccional con google tasks |
| `021-circular-action-menu-navigation` | `f0ea4ef` | navegación circular con aritmética modular |
| `023-fix-google-auth-screen-suspend` | `79aec73` | suspender screen de lanterna durante flujo oauth |
| `024-fix-google-auth-browser-open` | `2211e08` | fix apertura de navegador en linux |
| `025-fix-google-tasks-400-bad-request` | `851dbb1` | fix error 400 al actualizar tareas y manejo de errores |
| `026-secure-google-credentials` | `8dd9d51` | aislar y proteger credenciales de google |
| `027-google-tasks-provider` | `e112984` | reemplazar persistencia local por task provider api |
| `028-taskmaster-date-selection-and-list-options` | `e112984`, `2e78182` | renombrar a taskmaster, agregar calendario y opciones de listas |
| `029-restore-date-field-calendar` | `29bd543` | abrir calendario al posicionarse en el campo de fecha |
| `030-add-task-list-field` | `c9b9190` | agregar campo de lista en nueva tarea |
| `031-cyclic-menu-navigation` | `85a3df7` | navegación cíclica en menúes y calendario |
| `032-code-review-hardening` | `34ac5c7` | aplicar hallazgos del code review (cacheo, paginación, dead code y docs) |
| `033-settings-section` | `5eab56c` | agregar sección de ajustes con persistencia |
| `034-edit-shortcut-and-delete-empty-lists` | `5eab56c` | agregar atajo editar y eliminar listas vacías |
| `035-account-settings` | `5eab56c` | agregar cuenta de google en ajustes |
| `037-fix-expired-oauth-token-reauth` | `74bb1f4` | re-autenticar automáticamente ante refresh token expirado/revocado (invalid_grant) |
