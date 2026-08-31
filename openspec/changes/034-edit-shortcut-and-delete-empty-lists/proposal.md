# 034 — edit-shortcut-and-delete-empty-lists

## Why
Editar una tarea requería abrir el menú de acciones (`Enter`); se agrega el atajo
`e` para editar directamente. Además se incorpora la acción "eliminar listas
vacías" con confirmación, que requiere una operación de eliminar lista en el
proveedor (hoy inexistente).

## What Changes
- `TaskProvider`: nueva operación `deleteTaskList`; implementada en
  `GoogleTasksProvider` y `FakeTaskProvider`.
- `TaskService`: nuevo `deleteEmptyLists()` que elimina todas las listas sin tareas.
- `TaskListWindow`: tecla `e` abre el editor; tecla `x` solicita confirmación
  ("¿Estás seguro?", "No" por defecto) y elimina las listas vacías, tolerando
  quedar con cero listas.
- `TaskViewRenderer` (barra de atajos): se agregan `e` y `x`.

## Impact
- Specs: `task-lists`, `task-provider` e `interactive-cli` ya definen el
  comportamiento.
- Código: `provider/TaskProvider`, `google/GoogleTasksProvider`, `service/TaskService`,
  `cli/TaskListWindow`, `cli/TaskViewRenderer`.
- Tests: `TaskServiceTest`, `TaskListWindowTest`.
