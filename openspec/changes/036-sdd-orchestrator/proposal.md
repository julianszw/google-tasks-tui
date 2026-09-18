# Rediseño del flujo SDD: orquestador + subagentes

## Why

El flujo Spec-Driven Development estaba repartido entre varios agentes primarios
independientes (`specs-generator`, `codereviewer`, `git-manager`) más los agentes
`plan`/`build` integrados, sin un coordinador único. Esto hacía que el ciclo
completo (especificar → planificar → implementar → revisar → commitear) dependiera
de que el usuario invocara cada agente en el orden correcto y pasara el contexto a
mano.

## What Changes

- Se introduce un agente primario `sdd-orchestrator` (por defecto) que coordina el
  pipeline SDD y delega cada etapa a un subagente vía `task`.
- Se crean cinco subagentes `mode: subagent`: `sdd-specs`, `sdd-change`,
  `sdd-implement`, `sdd-review` y `sdd-git`.
- Se eliminan los agentes primarios `specs-generator`, `codereviewer` y
  `git-manager` (su contenido se reubica en los subagentes correspondientes).
- Se conserva `ask` como agente primario de consulta.
- Se corrige el modelo de permisos de `sdd-specs` (antes `specs-generator` declaraba
  `edit/write: "**": allow`, contradiciendo su ámbito de solo specs).
- Se establece `default_agent: sdd-orchestrator` en `.opencode/opencode.json`.
- Se actualiza `AGENTS.md` para documentar el flujo orquestador/subagentes.

## Impact

- Configuración de opencode (`.opencode/opencode.json`, `.opencode/agent/**`).
- Convenciones documentadas en `AGENTS.md`.
- El ciclo SDD pasa a ser dirigido por un único agente por defecto, con las
  aprobaciones del usuario en los gates (implementar, aplicar review, commitear).
- Sin cambios en `src/`, `pom.xml` ni specs de producto.
