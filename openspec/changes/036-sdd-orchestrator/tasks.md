# Tareas

- [x] Crear agente primario `sdd-orchestrator` (delega a subagentes vía `task`)
- [x] Crear subagente `sdd-specs` (especificar, `openspec/specs/**`)
- [x] Crear subagente `sdd-change` (planificar, `openspec/changes/**`)
- [x] Crear subagente `sdd-implement` (implementar, `src/` + tests + `tasks.md`)
- [x] Crear subagente `sdd-review` (revisar, `reviews/**`)
- [x] Crear subagente `sdd-git` (commitear, git + `REGISTER.md`)
- [x] Eliminar agentes primarios `specs-generator`, `codereviewer`, `git-manager`
- [x] Conservar `ask` como agente primario
- [x] Establecer `default_agent: sdd-orchestrator` en `.opencode/opencode.json`
- [x] Actualizar `AGENTS.md` con el flujo orquestador/subagentes
- [x] Registrar este change en `openspec/changes/036-sdd-orchestrator/`
