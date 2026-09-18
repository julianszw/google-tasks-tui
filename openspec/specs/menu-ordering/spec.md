# Menu Ordering

## Purpose
Define que las opciones de todos los menús de la aplicación se muestran en orden
alfabético.

## Requirements

### Requirement: Opciones ordenadas alfabéticamente
Las opciones de todos los menús (menú de tareas, selectores de listas, diálogos
"Sí/No" y menú de ajustes) DEBEN mostrarse en orden alfabético.

#### Scenario: Orden alfabético
- **GIVEN** un menú con dos o más opciones
- **WHEN** se muestra el menú
- **THEN** las opciones aparecen en orden alfabético
- **AND** el orden se mantiene estable en cada apertura

#### Scenario: Diálogo Sí/No
- **GIVEN** un diálogo con las opciones "Sí" y "No"
- **WHEN** se muestra el diálogo
- **THEN** "No" aparece antes que "Sí" (orden alfabético)
