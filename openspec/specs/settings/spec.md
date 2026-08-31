# Settings

## Purpose
Define la sección de ajustes de la aplicación: una pantalla que se abre con la tecla
`s` desde la vista única de tareas, identificada con un ícono de engranaje (ruedita),
y que permite alternar los ajustes "ocultar listas vacías" (según `hide-empty-lists`)
y "ocultar tareas completadas" (según `hide-completed-tasks`), configurar y consultar
la cuenta de Google (según `task-provider`) y ver la opción de tema (claro/oscuro)
como marcador de posición (según `theme`). Los ajustes se conservan entre sesiones.

## Requirements

### Requirement: Abrir Settings con la tecla s
La tecla `s` DEBE abrir la sección de Settings desde la vista única de tareas.

#### Scenario: Abrir Settings
- **GIVEN** la vista única de tareas visible
- **WHEN** el usuario presiona `s`
- **THEN** se abre la sección de Settings

#### Scenario: Abrir sin tareas
- **GIVEN** la vista sin tareas
- **WHEN** el usuario presiona `s`
- **THEN** se abre la sección de Settings igualmente

### Requirement: Ícono de engranaje
La sección de Settings DEBE identificarse visualmente con un ícono de engranaje
(ruedita) en su cabecera o título.

#### Scenario: Ícono visible
- **GIVEN** la sección de Settings abierta
- **WHEN** se muestra la sección
- **THEN** se muestra un ícono de engranaje/ruedita (por ejemplo, `⚙` o su
  equivalente en caracteres) junto al título de la sección

### Requirement: Ajustes disponibles
La sección DEBE listar, en orden estable, los items: "ocultar listas vacías",
"ocultar tareas completadas", "cuenta" y "tema".

#### Scenario: Lista de ajustes
- **GIVEN** la sección de Settings abierta
- **WHEN** se muestra la sección
- **THEN** se listan los items "ocultar listas vacías", "ocultar tareas completadas", "cuenta" y "tema"
- **AND** el orden de los items es el mismo en cada apertura

#### Scenario: Estado de los toggles
- **GIVEN** la sección de Settings abierta
- **WHEN** se muestran los items "ocultar listas vacías" y "ocultar tareas completadas"
- **THEN** cada uno muestra si está activado o desactivado

### Requirement: Navegación entre ajustes
El sistema DEBE permitir moverse entre los ajustes con las teclas `↑`/`k` (arriba) y
`↓`/`j` (abajo), de forma cíclica, resaltando el ajuste seleccionado.

#### Scenario: Mover selección
- **GIVEN** la sección de Settings abierta
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección se mueve al siguiente ajuste
- **AND** el ajuste seleccionado se resalta visualmente

#### Scenario: Ciclo en los extremos
- **GIVEN** la selección en el último ajuste
- **WHEN** el usuario presiona `↓` o `j`
- **THEN** la selección vuelve al primer ajuste
- **AND** al presionar `↑` o `k` desde el primer ajuste, la selección vuelve al último

### Requirement: Alternar un ajuste
La tecla `Enter` DEBE alternar los ajustes de tipo toggle ("ocultar listas vacías" y
"ocultar tareas completadas") entre activado y desactivado.

#### Scenario: Alternar ajuste
- **GIVEN** la sección de Settings abierta con un toggle seleccionado
- **WHEN** el usuario presiona `Enter`
- **THEN** el toggle cambia de estado (activado ↔ desactivado)
- **AND** la sección se redibuja mostrando el nuevo estado
- **AND** el cambio se refleja en la vista de tareas (según `hide-empty-lists` y
  `hide-completed-tasks`)

### Requirement: Mostrar estado de la cuenta
La sección DEBE mostrar el estado de la cuenta de Google: "configurar cuenta" si no
hay ninguna configurada, o el email de la cuenta configurada.

#### Scenario: Sin cuenta
- **GIVEN** que no hay una cuenta de Google configurada
- **WHEN** se muestra la sección de Settings
- **THEN** se muestra "configurar cuenta"
- **AND** no se muestra ningún email

#### Scenario: Con cuenta
- **GIVEN** una cuenta de Google configurada
- **WHEN** se muestra la sección de Settings
- **THEN** se muestra el email de la cuenta configurada

### Requirement: Configurar cuenta
El sistema DEBE permitir configurar una cuenta de Google desde Settings, iniciando la
autenticación (según `task-provider`).

#### Scenario: Configurar cuenta
- **GIVEN** la sección de Settings sin cuenta configurada
- **WHEN** el usuario activa "configurar cuenta"
- **THEN** se inicia la autenticación con Google
- **AND** al autorizar, la cuenta queda configurada
- **AND** la sección muestra el email de la cuenta

### Requirement: Cambiar de cuenta
Con una cuenta configurada, el sistema DEBE permitir autenticarse nuevamente para
cambiar a otra cuenta de Google.

#### Scenario: Cambiar de cuenta
- **GIVEN** una cuenta configurada
- **WHEN** el usuario activa "cambiar de cuenta"
- **THEN** se inicia la autenticación con Google
- **AND** al autorizar con otra cuenta, la cuenta configurada se reemplaza
- **AND** la sección muestra el email de la nueva cuenta

### Requirement: Cerrar sesión
Con una cuenta configurada, el sistema DEBE permitir cerrar sesión, eliminando la
cuenta guardada.

#### Scenario: Cerrar sesión
- **GIVEN** una cuenta configurada
- **WHEN** el usuario activa "cerrar sesión"
- **THEN** se elimina la cuenta guardada
- **AND** la sección vuelve a mostrar "configurar cuenta"

### Requirement: Tema (botón muerto)
La sección DEBE mostrar el item "tema" con las opciones "claro" y "oscuro" como un
marcador de posición (botón muerto): por ahora NO cambia el tema de la aplicación.
El comportamiento real se define en la capacidad `theme`.

#### Scenario: Item de tema visible
- **GIVEN** la sección de Settings abierta
- **WHEN** se muestra el item "tema"
- **THEN** se muestran las opciones "claro" y "oscuro"

#### Scenario: Botón muerto
- **GIVEN** la sección de Settings abierta con el item "tema"
- **WHEN** el usuario activa el item "tema" o elige "claro"/"oscuro"
- **THEN** no se produce ningún cambio de tema
- **AND** la aplicación permanece en el tema oscuro (según `visual-style`)

### Requirement: Cerrar Settings
La tecla `Esc` DEBE cerrar la sección de Settings y devolver el foco a la vista de
tareas sin cambios adicionales.

#### Scenario: Cerrar con Esc
- **GIVEN** la sección de Settings abierta
- **WHEN** el usuario presiona `Esc`
- **THEN** la sección se cierra
- **AND** la vista de tareas vuelve a estar activa

### Requirement: Persistencia entre sesiones
Los ajustes DEBEN conservarse entre sesiones: al reiniciar la aplicación se
recuperan los valores elegidos previamente.

#### Scenario: Recuperar ajustes
- **GIVEN** un ajuste activado en una sesión anterior
- **WHEN** se inicia la aplicación
- **THEN** el ajuste permanece activado

#### Scenario: Estado por defecto
- **GIVEN** la primera ejecución (sin ajustes guardados)
- **WHEN** se muestra la vista
- **THEN** ambos ajustes están desactivados (todas las listas y tareas visibles)
