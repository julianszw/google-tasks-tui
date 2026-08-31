# Unified Task View

## Purpose
Define la vista única de la aplicación: una lista de tareas siempre visible que se muestra al iniciar la aplicación y sobre la que se navega y se ejecutan acciones (completar, reabrir, eliminar, purgar y crear) mediante atajos de teclado. Reemplaza al menú principal (`main-menu`): ya no existe una pantalla separada de opciones.

## Requirements

### Requirement: Vista única al iniciar
La aplicación DEBE abrir directamente en la vista de lista de tareas, sin un menú principal separado.

#### Scenario: Apertura de la aplicación
- **GIVEN** la aplicación iniciada
- **WHEN** se muestra la ventana principal
- **THEN** se muestra la lista de tareas
- **AND** no se muestra un menú principal de opciones

#### Scenario: Sin tareas al iniciar
- **GIVEN** que no hay tareas
- **WHEN** se muestra la vista
- **THEN** se muestra un mensaje indicando que no hay tareas
- **AND** el usuario puede crear una tarea o salir de la aplicación

### Requirement: Requerir autenticación previa
La aplicación DEBE requerir autenticación contra Google Tasks antes de mostrar y
operar sobre las tareas; sin autenticación, DEBE solicitar el acceso.

#### Scenario: Usuario no autenticado
- **GIVEN** un usuario que no está autenticado
- **WHEN** se inicia la aplicación
- **THEN** el sistema solicita autenticación (según `task-provider`)
- **AND** no se muestra la lista de tareas hasta completar la autenticación

#### Scenario: Usuario autenticado
- **GIVEN** un usuario autenticado
- **WHEN** se inicia la aplicación
- **THEN** el sistema carga las listas y tareas desde Google Tasks
- **AND** se muestra la vista de tareas de la lista activa

### Requirement: Fallo al iniciar
Ante un fallo de autenticación o de red al iniciar, el sistema DEBE mostrar un
mensaje legible y terminar con un código de salida de error, sin exponer un stack
trace al usuario.

#### Scenario: Fallo de autenticación o de red al iniciar
- **GIVEN** un fallo de autenticación o de red al arrancar la aplicación
- **WHEN** se inicia la aplicación
- **THEN** el sistema muestra un mensaje de error legible al usuario
- **AND** termina con un código de salida distinto de cero
- **AND** no imprime un stack trace crudo

#### Scenario: Arranque exitoso
- **GIVEN** la aplicación puede autenticarse y conectarse
- **WHEN** se inicia la aplicación
- **THEN** la aplicación continúa normalmente mostrando la vista de tareas

### Requirement: Navegación por teclado
El sistema DEBE permitir moverse por la lista de tareas con las teclas `↑`/`k` (arriba) y `↓`/`j` (abajo), manteniendo visible la tarea seleccionada.

#### Scenario: Mover selección
- **GIVEN** la vista activa con una o más tareas
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección se mueve a la siguiente tarea
- **AND** la selección se resalta visualmente

#### Scenario: Límites de la lista
- **GIVEN** la selección en la primera o última tarea
- **WHEN** el usuario intenta moverse más allá del límite
- **THEN** la selección se mantiene sin salir de la lista

### Requirement: Redibujado de la vista al cambiar el estado
El sistema DEBE redibujar la vista cada vez que cambia el estado (selección, completado, borrado, purga o creación), sin acumular contenido residual en pantalla.

#### Scenario: Navegar entre tareas
- **GIVEN** la vista activa con una o más tareas
- **WHEN** el usuario cambia la selección con `↑`/`k` o `↓`/`j`
- **THEN** la vista se redibuja con la nueva tarea seleccionada
- **AND** no queda contenido residual en pantalla

### Requirement: Completar tarea seleccionada
La tecla `c` DEBE marcar como completada la tarea seleccionada.

#### Scenario: Completar selección pendiente
- **GIVEN** una tarea seleccionada en estado `PENDING`
- **WHEN** el usuario presiona `c`
- **THEN** la tarea pasa a `COMPLETED`
- **AND** la vista se actualiza aplicando atenuado y color según `output-formatting`

#### Scenario: Completar selección ya completada
- **GIVEN** una tarea seleccionada en estado `COMPLETED`
- **WHEN** el usuario presiona `c`
- **THEN** no se produce ningún cambio ni error

### Requirement: Reabrir tarea seleccionada
La tecla `r` DEBE volver a estado `PENDING` (reabrir) la tarea seleccionada.

#### Scenario: Reabrir selección completada
- **GIVEN** una tarea seleccionada en estado `COMPLETED`
- **WHEN** el usuario presiona `r`
- **THEN** la tarea pasa a `PENDING`
- **AND** la vista se actualiza aplicando el formato de tarea pendiente según `output-formatting`

#### Scenario: Reabrir selección ya pendiente
- **GIVEN** una tarea seleccionada en estado `PENDING`
- **WHEN** el usuario presiona `r`
- **THEN** no se produce ningún cambio ni error

### Requirement: Eliminar tarea seleccionada
La tecla `d` DEBE eliminar la tarea seleccionada.

#### Scenario: Eliminar selección
- **GIVEN** una tarea seleccionada
- **WHEN** el usuario presiona `d`
- **THEN** la tarea se elimina
- **AND** la vista se actualiza sin mostrar la tarea eliminada

### Requirement: Editar tarea seleccionada con atajo
La tecla `e` DEBE abrir el campo de edición del título para la tarea seleccionada,
sin necesidad de abrir el menú de acciones.

#### Scenario: Editar con e
- **GIVEN** una tarea seleccionada
- **WHEN** el usuario presiona `e`
- **THEN** se abre un campo de entrada precargado con el título actual (según `task-action-menu`)
- **AND** al confirmar un título no vacío, el título de la tarea se actualiza según `task-management`
- **AND** la vista se redibuja mostrando el nuevo título

#### Scenario: e sin tareas
- **GIVEN** que no hay tareas en la lista
- **WHEN** el usuario presiona `e`
- **THEN** no se abre ningún campo de edición
- **AND** no se produce ningún error

### Requirement: Purgar tareas completadas
La tecla `p` DEBE eliminar todas las tareas en estado `COMPLETED` de la lista activa.

#### Scenario: Con tareas completadas
- **GIVEN** una o más tareas en estado `COMPLETED` en la lista activa
- **WHEN** el usuario presiona `p`
- **THEN** se eliminan todas las tareas completadas de la lista activa
- **AND** la vista se actualiza mostrando solo las tareas pendientes

#### Scenario: Sin tareas completadas
- **GIVEN** que no hay tareas en estado `COMPLETED` en la lista activa
- **WHEN** el usuario presiona `p`
- **THEN** no se elimina ninguna tarea
- **AND** se muestra un mensaje indicando que no hay tareas completadas

### Requirement: Eliminar listas vacías con confirmación
La tecla `x` DEBE solicitar confirmación antes de eliminar las listas vacías.

#### Scenario: Solicitar confirmación
- **GIVEN** la vista visible
- **WHEN** el usuario presiona `x`
- **THEN** se muestra un diálogo de confirmación ("¿estás seguro?") con las opciones sí/no
- **AND** no se elimina ninguna lista aún

#### Scenario: Selección por defecto
- **GIVEN** el diálogo de confirmación abierto
- **WHEN** se muestra el diálogo
- **THEN** la opción "no" está seleccionada por defecto

#### Scenario: Confirmar
- **GIVEN** el diálogo de confirmación abierto con la opción "sí" seleccionada
- **WHEN** el usuario confirma con `Enter`
- **THEN** se eliminan las listas vacías según `task-lists`
- **AND** la vista se redibuja reflejando el resultado

#### Scenario: Cancelar con "no"
- **GIVEN** el diálogo de confirmación abierto con la opción "no" seleccionada
- **WHEN** el usuario confirma con `Enter`
- **THEN** el diálogo se cierra sin eliminar ninguna lista
- **AND** la aplicación permanece abierta

#### Scenario: Cancelar con Esc
- **GIVEN** el diálogo de confirmación abierto
- **WHEN** el usuario presiona `Esc`
- **THEN** el diálogo se cierra sin eliminar ninguna lista
- **AND** la aplicación permanece abierta

### Requirement: Crear tarea desde la vista
La tecla `a` DEBE abrir un campo de entrada para crear una tarea.

#### Scenario: Crear tarea
- **GIVEN** la vista única visible con una lista activa
- **WHEN** el usuario presiona `a`
- **THEN** se abre un campo de entrada para ingresar el título
- **AND** al confirmar un título no vacío, se crea la tarea en la lista activa (según `task-management`)
- **AND** la vista se actualiza mostrando la tarea creada

#### Scenario: Título vacío
- **GIVEN** el campo de entrada abierto
- **WHEN** el usuario confirma un título vacío o compuesto solo por espacios
- **THEN** no se crea ninguna tarea
- **AND** se muestra un mensaje de error

#### Scenario: Crear tarea con fecha de vencimiento
- **GIVEN** la vista única visible con una lista activa
- **WHEN** el usuario crea una tarea indicando además una fecha de vencimiento
- **THEN** la tarea se crea con esa fecha (según `task-management`)
- **AND** la vista muestra la tarea con su fecha de vencimiento

### Requirement: Ayuda permanente de teclas
El sistema DEBE mostrar siempre una ayuda visible con las teclas disponibles, en la fila de atajos de la barra de estado (según `visual-style`).

#### Scenario: Ayuda siempre visible
- **GIVEN** la vista única visible
- **WHEN** se muestra la vista
- **THEN** se muestra la fila de atajos con las teclas disponibles: `↑`/`k` (subir), `↓`/`j` (bajar), `Enter` (acciones), `Tab` (lista), `a` (crear), `e` (editar), `n` (nueva lista), `s` (ajustes), `x` (eliminar listas vacías), `c` (completar), `r` (reabrir), `d` (eliminar), `p` (purgar), `q`/`Esc` (salir)
- **AND** la ayuda permanece visible en todo momento, sin necesidad de presionar una tecla para mostrarla

### Requirement: Salir de la aplicación con confirmación
Las teclas `q` y `Esc` DEBEN mostrar un diálogo de confirmación ("¿estás seguro?") con opciones sí/no antes de cerrar la aplicación.

#### Scenario: Solicitar salida
- **GIVEN** la vista única visible sin ningún menú ni diálogo abierto
- **WHEN** el usuario presiona `q` o `Esc`
- **THEN** se muestra un diálogo de confirmación "¿estás seguro?" con las opciones sí/no
- **AND** la aplicación no se cierra aún

#### Scenario: Selección por defecto
- **GIVEN** el diálogo de confirmación abierto
- **WHEN** se muestra el diálogo
- **THEN** la opción "no" está seleccionada por defecto

#### Scenario: Confirmar salida
- **GIVEN** el diálogo de confirmación abierto con la opción "sí" seleccionada
- **WHEN** el usuario confirma con `Enter`
- **THEN** se cierra la aplicación

#### Scenario: Cancelar salida
- **GIVEN** el diálogo de confirmación abierto con la opción "no" seleccionada
- **WHEN** el usuario confirma con `Enter`
- **THEN** el diálogo se cierra
- **AND** la aplicación permanece abierta en la lista de tareas

#### Scenario: Cancelar con Esc
- **GIVEN** el diálogo de confirmación abierto
- **WHEN** el usuario presiona `Esc`
- **THEN** el diálogo se cierra
- **AND** la aplicación permanece abierta en la lista de tareas

#### Scenario: Esc con menú de acciones abierto
- **GIVEN** el menú de acciones abierto (ver `task-action-menu`)
- **WHEN** el usuario presiona `Esc`
- **THEN** se cancela el menú de acciones en lugar de mostrar el diálogo de salida
- **AND** la aplicación permanece abierta en la lista de tareas

### Requirement: Navegación cíclica en el diálogo de confirmación
El diálogo de confirmación de salida DEBE permitir alternar entre las opciones "sí" y "no" con las teclas `↑`/`k` (arriba) y `↓`/`j` (abajo), de forma cíclica.

#### Scenario: Alternar entre opciones
- **GIVEN** el diálogo de confirmación abierto con "no" seleccionado por defecto
- **WHEN** el usuario presiona `↑` o `k` (o `↓` o `j`)
- **THEN** la selección cambia a la opción "sí"

#### Scenario: Ciclo en los extremos
- **GIVEN** la opción "sí" seleccionada
- **WHEN** el usuario presiona `↑` o `k`
- **THEN** la selección vuelve a la opción "no"
- **AND** al presionar `↓` o `j` desde "no", la selección vuelve a "sí"
