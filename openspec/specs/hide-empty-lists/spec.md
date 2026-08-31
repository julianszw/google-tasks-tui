# Hide Empty Lists

## Purpose
Define el ajuste de ocultar de la navegación las listas de tareas que no tienen
tareas, manteniéndolas disponibles como destino al mover tareas. El ajuste se
activa y desactiva desde la sección de Settings (`settings`) y, por defecto, todas
las listas están visibles.

## Requirements

### Requirement: Alternar ocultado desde Settings
El ajuste DEBE activarse y desactivarse desde la sección de Settings (según
`settings`).

#### Scenario: Activar o desactivar
- **GIVEN** la sección de Settings abierta con el ajuste "ocultar listas vacías" seleccionado
- **WHEN** el usuario alterna el ajuste con `Enter`
- **THEN** el ajuste de ocultar listas vacías se activa o desactiva (alterna)
- **AND** la vista se redibuja reflejando el nuevo estado

#### Scenario: Estado por defecto
- **GIVEN** la aplicación iniciada sin ajustes guardados
- **WHEN** se muestra la vista
- **THEN** todas las listas están visibles (el ocultado está desactivado)

### Requirement: Ocultar listas vacías de la navegación
Cuando la opción está activa, el sistema DEBE excluir de la navegación con
`Tab`/`Shift+Tab` las listas que no tienen tareas.

#### Scenario: Navegación solo por listas con tareas
- **GIVEN** la opción de ocultado activa y listas con y sin tareas
- **WHEN** el usuario navega con `Tab` o `Shift+Tab`
- **THEN** la navegación recorre únicamente las listas con tareas
- **AND** las listas vacías no aparecen en la navegación

#### Scenario: Ocultado desactivado
- **GIVEN** la opción de ocultado desactivada
- **WHEN** el usuario navega con `Tab` o `Shift+Tab`
- **THEN** la navegación recorre todas las listas, incluidas las vacías

### Requirement: Listas vacías disponibles para mover tareas
El sistema DEBE mantener las listas vacías ocultas como destinos disponibles al
mover una tarea a otra lista.

#### Scenario: Mover a una lista vacía oculta
- **GIVEN** la opción de ocultado activa y una lista vacía oculta
- **WHEN** el usuario mueve una tarea a otra lista
- **THEN** la lista vacía oculta aparece como destino disponible en el selector de listas (según `task-action-menu`)
