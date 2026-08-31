# Hide Completed Tasks

## Purpose
Define el ajuste "ocultar tareas completadas": cuando está activo, la vista única de
tareas excluye las tareas en estado `COMPLETED`, mostrando solo las pendientes. El
ajuste se controla desde la sección de Settings (`settings`) y se conserva entre
sesiones.

## Requirements

### Requirement: Alternar ocultado desde Settings
El ajuste DEBE activarse y desactivarse desde la sección de Settings (según
`settings`).

#### Scenario: Activar o desactivar
- **GIVEN** la sección de Settings abierta con el ajuste "ocultar tareas completadas" seleccionado
- **WHEN** el usuario alterna el ajuste con `Enter`
- **THEN** el ajuste cambia de estado
- **AND** la vista de tareas se redibuja reflejando el nuevo estado

#### Scenario: Estado por defecto
- **GIVEN** la aplicación iniciada sin ajustes guardados
- **WHEN** se muestra la vista
- **THEN** el ajuste está desactivado (las tareas completadas se muestran)

### Requirement: Ocultar tareas completadas de la vista
Cuando el ajuste está activo, el sistema DEBE excluir de la lista las tareas en
estado `COMPLETED`.

#### Scenario: Ocultado activo
- **GIVEN** el ajuste activo y una lista con tareas pendientes y completadas
- **WHEN** se muestra la lista
- **THEN** solo se muestran las tareas pendientes
- **AND** las tareas completadas no aparecen en la lista

#### Scenario: Ocultado desactivado
- **GIVEN** el ajuste desactivado
- **WHEN** se muestra la lista
- **THEN** se muestran todas las tareas, incluidas las completadas

#### Scenario: Solo tareas completadas
- **GIVEN** el ajuste activo y una lista con únicamente tareas completadas
- **WHEN** se muestra la lista
- **THEN** la lista se muestra sin tareas
- **AND** no se produce ningún error
