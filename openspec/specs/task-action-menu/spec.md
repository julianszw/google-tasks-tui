# Task Action Menu

## Purpose
Define el menú contextual de acciones que se abre al presionar `Enter` sobre la tarea seleccionada en la vista única (`interactive-cli`). Permite desplazarse entre las acciones aplicables a la tarea (completar, reabrir, eliminar, editar, fecha y mover) y ejecutarlas con `Enter`. La acción editar abre directamente el campo de título, la acción fecha permite establecer o quitar la fecha de vencimiento y la acción mover abre el selector de lista destino. Complementa los atajos de teclado existentes, que quedan deshabilitados mientras el menú está abierto.

## Requirements

### Requirement: Abrir menú de acciones con Enter
La tecla `Enter` DEBE abrir un menú contextual de acciones para la tarea seleccionada.

#### Scenario: Abrir el menú
- **GIVEN** la vista única visible con una tarea seleccionada
- **WHEN** el usuario presiona `Enter`
- **THEN** se abre un menú contextual junto a la tarea seleccionada (overlay)
- **AND** el menú lista las acciones de la tarea: completar, reabrir, eliminar, editar, fecha y mover

#### Scenario: Enter sin tareas
- **GIVEN** que no hay tareas en la lista
- **WHEN** el usuario presiona `Enter`
- **THEN** no se abre ningún menú
- **AND** no se produce ningún error

### Requirement: Acciones disponibles del menú
El menú DEBE listar las acciones de la tarea seleccionada — completar, reabrir,
eliminar, editar, fecha y mover — en orden alfabético (según `menu-ordering`) y cada
una con su atajo en gris (según `menu-shortcuts`).

#### Scenario: Lista de acciones
- **GIVEN** el menú de acciones abierto
- **WHEN** se muestra el menú
- **THEN** se listan las acciones completar, editar, eliminar, fecha, mover y reabrir (orden alfabético)
- **AND** cada acción muestra su atajo en gris (según `menu-shortcuts`)

#### Scenario: Completar desde el menú
- **GIVEN** el menú abierto con la acción "completar" seleccionada
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** la tarea se marca como completada según `task-management`
- **AND** la vista se redibuja con el resultado

#### Scenario: Reabrir desde el menú
- **GIVEN** el menú abierto con la acción "reabrir" seleccionada
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** la tarea se reabre según `task-management`
- **AND** la vista se redibuja con el resultado

#### Scenario: Eliminar desde el menú
- **GIVEN** el menú abierto con la acción "eliminar" seleccionada
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** la tarea se elimina según `task-management`
- **AND** la vista se redibuja sin mostrar la tarea eliminada

### Requirement: Navegación circular entre acciones
El sistema DEBE permitir desplazarse entre las acciones del menú con las teclas `↑`/`k` (arriba) y `↓`/`j` (abajo), de forma circular, resaltando la acción seleccionada.

#### Scenario: Mover selección de acción
- **GIVEN** el menú de acciones abierto
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección se mueve a la siguiente acción
- **AND** la acción seleccionada se resalta visualmente

#### Scenario: Ciclo al llegar al final
- **GIVEN** la selección en la última acción
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección vuelve a la primera acción

#### Scenario: Ciclo al llegar al principio
- **GIVEN** la selección en la primera acción
- **WHEN** el usuario presiona `↑` o `k`
- **THEN** la selección vuelve a la última acción

### Requirement: Ejecutar acción con Enter
La tecla `Enter` DEBE ejecutar la acción seleccionada del menú sobre la tarea y cerrar el menú.

#### Scenario: Ejecutar acción seleccionada
- **GIVEN** el menú abierto con una acción seleccionada
- **WHEN** el usuario presiona `Enter`
- **THEN** se ejecuta la acción seleccionada sobre la tarea
- **AND** el menú se cierra
- **AND** la vista se redibuja reflejando el resultado

### Requirement: Cancelar el menú
La tecla `Esc` DEBE cerrar el menú sin ejecutar ninguna acción, devolviendo el foco a la lista de tareas.

#### Scenario: Cancelar sin ejecutar
- **GIVEN** el menú de acciones abierto
- **WHEN** el usuario presiona `Esc`
- **THEN** el menú se cierra
- **AND** no se ejecuta ninguna acción
- **AND** la lista de tareas vuelve a estar activa

### Requirement: Atajos activos y deshabilitados con el menú abierto
Mientras el menú de acciones está abierto, los atajos del menú (`c`, `r`, `d`, `e`,
`f`, `m` — según `menu-shortcuts`) ejecutan las acciones del menú, y los atajos de la
vista (`a`, `n`, `s`, `x`, `p`, `q`) DEBEN quedar deshabilitados.

#### Scenario: Atajo del menú activo
- **GIVEN** el menú de acciones abierto
- **WHEN** el usuario presiona un atajo del menú (por ejemplo, `c` o `e`)
- **THEN** se ejecuta la acción correspondiente del menú

#### Scenario: Atajo de la vista deshabilitado
- **GIVEN** el menú de acciones abierto
- **WHEN** el usuario presiona un atajo de la vista (por ejemplo, `p` o `q`)
- **THEN** no se ejecuta la acción asociada al atajo de la vista
- **AND** no se cierra la aplicación
- **AND** el menú permanece abierto

#### Scenario: Atajos restaurados al cerrar el menú
- **GIVEN** el menú de acciones abierto
- **WHEN** el usuario lo cierra con `Esc` o ejecutando una acción
- **THEN** los atajos de teclado de la vista vuelven a estar habilitados

### Requirement: Editar tarea desde el menú
La acción "editar" DEBE abrir un campo de entrada precargado con el título actual para modificar la tarea seleccionada.

#### Scenario: Editar título
- **GIVEN** el menú abierto con la acción "editar" seleccionada
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** se abre un campo de entrada precargado con el título actual de la tarea
- **AND** al confirmar un título no vacío, el título de la tarea se actualiza según `task-management`
- **AND** la vista se redibuja mostrando el nuevo título

#### Scenario: Editar con título vacío
- **GIVEN** el campo de edición abierto
- **WHEN** el usuario confirma un título vacío o compuesto solo por espacios
- **THEN** no se modifica el título de la tarea
- **AND** se muestra un mensaje de error

#### Scenario: Cancelar edición
- **GIVEN** el campo de edición abierto
- **WHEN** el usuario cancela sin confirmar (con `Esc`)
- **THEN** no se modifica el título de la tarea
- **AND** la vista vuelve a la lista sin cambios

### Requirement: Editar fecha de vencimiento desde el menú
La acción "fecha" DEBE permitir establecer o quitar la fecha de vencimiento de la tarea seleccionada, abriendo el calendario interactivo (según `date-selection`).

#### Scenario: Establecer o modificar fecha
- **GIVEN** el menú abierto con la acción "fecha" seleccionada
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** se abre un calendario interactivo para elegir la fecha
- **AND** al elegir un día, la fecha de la tarea se actualiza según `task-management`
- **AND** la vista se redibuja mostrando la nueva fecha

#### Scenario: Quitar fecha
- **GIVEN** el menú abierto con la acción "fecha" seleccionada y la tarea con fecha de vencimiento
- **WHEN** el usuario elige quitar la fecha (tecla `d`)
- **THEN** la tarea queda sin fecha de vencimiento según `task-management`

#### Scenario: Cancelar edición de fecha
- **GIVEN** el calendario abierto
- **WHEN** el usuario cancela sin confirmar (con `Esc`)
- **THEN** no se modifica la fecha de la tarea
- **AND** la vista vuelve a la lista sin cambios

### Requirement: Mover tarea a otra lista desde el menú
La acción "mover" DEBE permitir elegir una lista destino y mover la tarea seleccionada a esa lista.

#### Scenario: Mover a otra lista
- **GIVEN** la acción "mover" seleccionada y al menos dos listas existentes
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** se muestra un selector con las listas existentes, excluyendo la lista actual de la tarea
- **AND** al elegir una lista destino y confirmar, la tarea se mueve a esa lista según `task-management`
- **AND** la vista se redibuja reflejando el resultado

#### Scenario: Sin listas destino disponibles
- **GIVEN** la acción "mover" seleccionada y solo existe una lista (la lista actual)
- **WHEN** el usuario ejecuta la acción con `Enter`
- **THEN** se muestra un mensaje indicando que no hay otra lista a la que mover la tarea
- **AND** no se mueve la tarea

#### Scenario: Cancelar el selector de lista
- **GIVEN** el selector de listas abierto
- **WHEN** el usuario presiona `Esc`
- **THEN** se cierra el selector sin mover la tarea
- **AND** la vista vuelve a la lista sin cambios

### Requirement: Navegación cíclica en el selector de listas
El selector de listas de la acción "mover" DEBE permitir desplazarse entre las listas destino con las teclas `↑`/`k` (arriba) y `↓`/`j` (abajo), de forma cíclica, resaltando la lista seleccionada.

#### Scenario: Mover selección en el selector
- **GIVEN** el selector de listas abierto con dos o más listas
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección se mueve a la siguiente lista
- **AND** la lista seleccionada se resalta visualmente

#### Scenario: Ciclo al llegar al final
- **GIVEN** la selección en la última lista del selector
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección vuelve a la primera lista

#### Scenario: Ciclo al llegar al principio
- **GIVEN** la selección en la primera lista del selector
- **WHEN** el usuario presiona `↑` o `k`
- **THEN** la selección vuelve a la última lista
