# Task Lists

## Purpose
Define el agrupamiento de tareas en listas y la navegación entre ellas con `Tab`/`Shift+Tab`, junto con la creación de nuevas listas mediante un botón en la barra inferior activado con la tecla `n`. La aplicación abre directamente sobre la lista inicial, sin una pantalla separada de selección de listas.

## Requirements

### Requirement: Tareas agrupadas en listas
El sistema DEBE agrupar las tareas en listas, de modo que cada tarea pertenezca a exactamente una lista.

#### Scenario: Tarea asociada a la lista activa
- **GIVEN** una lista activa
- **WHEN** se crea una tarea
- **THEN** la tarea queda asociada a la lista activa
- **AND** no pertenece a ninguna otra lista

#### Scenario: Cada lista muestra sus propias tareas
- **GIVEN** varias listas, cada una con sus tareas
- **WHEN** se muestra la lista activa
- **THEN** solo se muestran las tareas de esa lista

### Requirement: Lista inicial por defecto
El sistema DEBE crear automáticamente una lista inicial en el proveedor (Google
Tasks) la primera vez que no exista ninguna y abrir directamente sobre ella.

#### Scenario: Primera ejecución sin listas
- **GIVEN** que no existen listas en el proveedor
- **WHEN** se inicia la aplicación
- **THEN** el sistema crea una lista inicial (por ejemplo, "Inbox") en el proveedor
- **AND** la aplicación abre mostrando esa lista como activa

#### Scenario: Con listas existentes
- **GIVEN** una o más listas existentes en el proveedor
- **WHEN** se inicia la aplicación
- **THEN** no se crea una lista nueva
- **AND** la aplicación abre sobre la primera lista

### Requirement: Apertura directa sobre la lista activa
El sistema DEBE abrir directamente sobre la lista de tareas activa, sin una pantalla separada de selección de listas.

#### Scenario: Apertura de la aplicación
- **GIVEN** la aplicación iniciada
- **WHEN** se muestra la vista
- **THEN** se muestra la lista de tareas de la lista activa
- **AND** no se muestra una pantalla separada de selección de listas

### Requirement: Navegación entre listas con Tab
La tecla `Tab` DEBE avanzar a la siguiente lista de forma cíclica y `Shift+Tab` DEBE retroceder a la anterior.

#### Scenario: Avanzar con Tab
- **GIVEN** dos o más listas
- **WHEN** el usuario presiona `Tab`
- **THEN** la lista activa cambia a la siguiente lista
- **AND** la vista muestra las tareas de la nueva lista activa

#### Scenario: Ciclo al llegar al final
- **GIVEN** la lista activa es la última
- **WHEN** el usuario presiona `Tab`
- **THEN** la lista activa vuelve a la primera lista

#### Scenario: Retroceder con Shift+Tab
- **GIVEN** dos o más listas
- **WHEN** el usuario presiona `Shift+Tab`
- **THEN** la lista activa cambia a la lista anterior
- **AND** si la lista activa es la primera, vuelve a la última

#### Scenario: Una sola lista
- **GIVEN** una única lista
- **WHEN** el usuario presiona `Tab` o `Shift+Tab`
- **THEN** la lista activa no cambia

### Requirement: Indicador de la lista activa
El sistema DEBE mostrar el nombre de la lista activa de forma visible.

#### Scenario: Lista activa visible
- **GIVEN** la vista visible
- **WHEN** se muestra la vista
- **THEN** se muestra el nombre de la lista activa
- **AND** se muestra la posición de la lista respecto del total (por ejemplo, "1/3")

### Requirement: Botón para crear listas
El sistema DEBE mostrar en la barra inferior un botón para crear una nueva lista, activado con la tecla `n`.

#### Scenario: Botón visible
- **GIVEN** la vista visible
- **WHEN** se muestra la vista
- **THEN** se muestra un botón de crear lista (por ejemplo, "+ Nueva lista") en la barra inferior

#### Scenario: Activar con la tecla n
- **GIVEN** la vista visible
- **WHEN** el usuario presiona `n`
- **THEN** se abre un campo de entrada para ingresar el nombre de la nueva lista

### Requirement: Crear lista
El sistema DEBE crear una nueva lista a partir de un nombre no vacío y dejarla disponible para la navegación.

#### Scenario: Crear lista con nombre válido
- **GIVEN** un nombre de lista no vacío
- **WHEN** se confirma el nombre
- **THEN** se crea una nueva lista en el proveedor con ese nombre
- **AND** la nueva lista queda disponible para navegar con `Tab`/`Shift+Tab`
- **AND** la nueva lista se convierte en la lista activa

#### Scenario: Crear lista con nombre vacío
- **GIVEN** el campo de entrada abierto
- **WHEN** el usuario confirma un nombre vacío o compuesto solo por espacios
- **THEN** no se crea ninguna lista
- **AND** se muestra un mensaje de error

### Requirement: Cancelar creación de lista
La tecla `Esc` DEBE cancelar la creación de una lista sin crear nada.

#### Scenario: Cancelar
- **GIVEN** el campo de entrada abierto
- **WHEN** el usuario presiona `Esc`
- **THEN** no se crea ninguna lista
- **AND** la vista vuelve a la lista activa sin cambios

### Requirement: Eliminar listas vacías
El sistema DEBE permitir eliminar todas las listas que no tienen tareas.

#### Scenario: Eliminar listas vacías
- **GIVEN** una o más listas sin tareas
- **WHEN** se solicita eliminar las listas vacías
- **THEN** se eliminan todas las listas que no tienen tareas
- **AND** las listas con tareas permanecen intactas

#### Scenario: Sin listas vacías
- **GIVEN** que todas las listas tienen al menos una tarea
- **WHEN** se solicita eliminar las listas vacías
- **THEN** no se elimina ninguna lista
- **AND** se muestra un mensaje indicando que no hay listas vacías

#### Scenario: Quedar sin listas
- **GIVEN** todas las listas están vacías
- **WHEN** se solicita eliminar las listas vacías
- **THEN** se eliminan todas las listas
- **AND** la aplicación permite quedar sin listas, mostrando una vista vacía

#### Scenario: Vista sin listas
- **GIVEN** que no quedan listas
- **WHEN** se muestra la vista
- **THEN** la vista se muestra sin listas ni tareas
- **AND** el usuario puede crear una lista nueva (según esta capacidad)
