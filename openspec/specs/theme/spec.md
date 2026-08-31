# Theme

## Purpose
Define la capacidad de alternar el tema de la interfaz entre un tema claro y un tema
oscuro. Hoy la aplicación muestra un único tema oscuro permanente (según
`visual-style`, que reemplazó a `dark-theme`); esta capacidad define el
comportamiento a implementar para reintroducir la alternancia, con el tema oscuro
como por defecto. Se expone desde la sección de Settings (`settings`), donde por
ahora aparece como un marcador de posición (botón muerto). El tema elegido se
conserva entre sesiones.

## Requirements

### Requirement: Dos temas disponibles
El sistema DEBE ofrecer dos temas: "claro" y "oscuro".

#### Scenario: Temas disponibles
- **GIVEN** la aplicación en funcionamiento
- **WHEN** se consultan los temas disponibles
- **THEN** los temas disponibles son "claro" y "oscuro"

### Requirement: Tema oscuro por defecto
El sistema DEBE usar el tema oscuro como tema por defecto.

#### Scenario: Primer inicio
- **GIVEN** que no hay un tema guardado
- **WHEN** se inicia la aplicación
- **THEN** la interfaz se muestra en tema oscuro

### Requirement: Alternar tema desde Settings
El sistema DEBE permitir alternar entre tema claro y oscuro desde la sección de
Settings (según `settings`).

#### Scenario: Cambiar a tema claro
- **GIVEN** la aplicación en tema oscuro
- **WHEN** el usuario elige "claro" en Settings
- **THEN** la interfaz cambia al tema claro

#### Scenario: Cambiar a tema oscuro
- **GIVEN** la aplicación en tema claro
- **WHEN** el usuario elige "oscuro" en Settings
- **THEN** la interfaz cambia al tema oscuro

### Requirement: Aplicación global del tema
El cambio de tema DEBE aplicarse a toda la interfaz de la vista única de tareas
(lista, barra de estado, mensajes y elementos de interacción), redibujando la vista
sin acumular contenido residual.

#### Scenario: Aplicación global
- **GIVEN** un cambio de tema
- **WHEN** se aplica el cambio
- **THEN** todo el contenido visible usa el nuevo tema
- **AND** la vista se redibuja sin contenido residual

### Requirement: Persistencia del tema
El tema DEBE conservarse entre sesiones: al reiniciar la aplicación se recupera el
tema elegido previamente.

#### Scenario: Reinicio con tema claro
- **GIVEN** la aplicación con el tema claro elegido
- **WHEN** se cierra y se vuelve a iniciar
- **THEN** la interfaz se muestra en tema claro

### Requirement: Paletas de cada tema
Cada tema DEBE definir una paleta propia y consistente: el tema oscuro reutiliza la
paleta definida en `visual-style`; el tema claro DEBE definir su propia paleta
(fondo claro, texto oscuro y acentos legibles) manteniendo los mismos roles
semánticos (texto principal, secundario, completado, advertencia y error).

#### Scenario: Paleta consistente por tema
- **GIVEN** un tema activo (claro u oscuro)
- **WHEN** se muestra la interfaz
- **THEN** se aplica la paleta de ese tema de forma consistente en toda la vista
- **AND** los roles semánticos (completado, advertencia, error) se distinguen en ambos temas

### Requirement: Coherencia e implementación iterativa
Al implementar esta capacidad, el sistema DEBE mantener la coherencia con las
capacidades afectadas: `visual-style` (que hoy define un único tema oscuro
permanente) DEBE dejar de declarar la ausencia de alternancia y definir (o
referenciar) las paletas de ambos temas, y `dark-theme` (marcada como removida) DEBE
apuntar a esta capacidad como la que reintroduce la alternancia.

#### Scenario: Actualización de visual-style
- **GIVEN** la implementación de la alternancia de tema
- **WHEN** se incorpora esta capacidad
- **THEN** `visual-style` deja de declarar un único tema oscuro permanente
- **AND** define o referencia las paletas de ambos temas

#### Scenario: Actualización de dark-theme
- **GIVEN** la implementación de la alternancia de tema
- **WHEN** se incorpora esta capacidad
- **THEN** `dark-theme` deja de estar marcada como removida
- **AND** referencia la alternancia de tema de esta capacidad
