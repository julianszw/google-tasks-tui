# Menu Shortcuts

## Purpose
Define que todos los menús de la aplicación ofrecen atajos de teclado por opción,
sugeridos en gris (atenuado) junto al nombre de cada opción (según `visual-style`).
Aplica al menú de acciones de tarea (`task-action-menu`), a los diálogos de
confirmación "Sí/No" (`interactive-cli`) y a los demás menús con opciones fijas
(`settings`).

## Requirements

### Requirement: Atajo por opción
Cada opción de un menú DEBE tener una tecla de atajo; al presionarla se ejecuta la
opción directamente, sin necesidad de navegar hasta ella y presionar `Enter`.

#### Scenario: Ejecutar con atajo
- **GIVEN** un menú abierto con opciones
- **WHEN** el usuario presiona la tecla de atajo de una opción
- **THEN** se ejecuta esa opción inmediatamente
- **AND** el menú se cierra (salvo que la opción abra otra pantalla)

### Requirement: Sugerencia en gris
El atajo de cada opción DEBE mostrarse junto a su nombre en color gris/atenuado.

#### Scenario: Atajo visible atenuado
- **GIVEN** un menú abierto
- **WHEN** se muestran las opciones
- **THEN** cada opción muestra su atajo en gris/atenuado (según `visual-style`)

### Requirement: Atajos del menú de tareas
El menú de acciones de tarea DEBE asignar los atajos `c` (completar), `r` (reabrir),
`d` (eliminar), `e` (editar), `f` (fecha) y `m` (mover).

#### Scenario: Atajos disponibles
- **GIVEN** el menú de acciones de tarea abierto
- **WHEN** se muestran las acciones
- **THEN** se muestran los atajos `c`, `r`, `d`, `e`, `f` y `m` junto a sus acciones

### Requirement: Atajos de diálogos de confirmación
Los diálogos con opciones "Sí"/"No" DEBEN asignar la tecla `s` a "Sí" y la tecla `n`
a "No".

#### Scenario: Confirmar con s o n
- **GIVEN** un diálogo "Sí/No" abierto
- **WHEN** el usuario presiona `s` o `n`
- **THEN** se ejecuta la opción "Sí" (con `s`) o "No" (con `n`)

### Requirement: Atajos de otros menús fijos
Los demás menús con opciones fijas (por ejemplo, "cambiar de cuenta"/"cerrar sesión"
en `settings`) DEBEN asignar una tecla de atajo distintiva por opción, evitando
colisiones.

#### Scenario: Atajos presentes
- **GIVEN** un menú con opciones fijas
- **WHEN** se muestran las opciones
- **THEN** cada opción muestra su atajo en gris
