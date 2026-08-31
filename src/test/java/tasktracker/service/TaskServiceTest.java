package tasktracker.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasktracker.FakeTaskProvider;
import tasktracker.exception.TaskListNotFoundException;
import tasktracker.exception.TaskNotFoundException;
import tasktracker.model.Task;
import tasktracker.model.TaskList;
import tasktracker.model.TaskStatus;
import tasktracker.provider.TaskProvider;

class TaskServiceTest {

    private TaskService service;

    @BeforeEach
    void setUp() {
        service = new TaskService(new FakeTaskProvider());
    }

    private String inboxId() {
        return service.createList("Inbox").getId();
    }

    @Test
    void createListCreatesListWithUniqueId() {
        TaskList first = service.createList("Inbox");
        TaskList second = service.createList("Trabajo");

        assertNotEquals(first.getId(), second.getId());
        assertEquals("Inbox", first.getTitle());
        assertEquals("Trabajo", second.getTitle());
    }

    @Test
    void createListRejectsEmptyName() {
        assertThrows(IllegalArgumentException.class, () -> service.createList(""));
    }

    @Test
    void createListRejectsBlankName() {
        assertThrows(IllegalArgumentException.class, () -> service.createList("   "));
    }

    @Test
    void createListRejectsNullName() {
        assertThrows(IllegalArgumentException.class, () -> service.createList(null));
    }

    @Test
    void deleteEmptyListsRemovesOnlyEmptyLists() {
        service.createList("Inbox");
        String work = service.createList("Trabajo").getId();
        service.addTask(work, "A");

        List<TaskList> removed = service.deleteEmptyLists();

        assertEquals(1, removed.size());
        assertEquals("Inbox", removed.get(0).getTitle());
        assertEquals(List.of(work), service.listLists().stream().map(TaskList::getId).toList());
    }

    @Test
    void deleteEmptyListsReturnsEmptyWhenNoEmptyLists() {
        String inbox = service.createList("Inbox").getId();
        service.addTask(inbox, "A");

        assertTrue(service.deleteEmptyLists().isEmpty());
        assertEquals(1, service.listLists().size());
    }

    @Test
    void deleteEmptyListsCanLeaveZeroLists() {
        service.createList("Inbox");

        List<TaskList> removed = service.deleteEmptyLists();

        assertEquals(1, removed.size());
        assertTrue(service.listLists().isEmpty());
    }

    @Test
    void listListsReturnsEmptyWhenNoLists() {
        assertTrue(service.listLists().isEmpty());
    }

    @Test
    void addTaskCreatesPendingTaskWithUniqueId() {
        String listId = inboxId();

        Task first = service.addTask(listId, "Comprar leche");
        Task second = service.addTask(listId, "Pagar facturas");

        assertNotEquals(first.getId(), second.getId());
        assertEquals(TaskStatus.PENDING, first.getStatus());
        assertEquals("Comprar leche", first.getTitle());
    }

    @Test
    void addTaskAssociatesTaskToList() {
        String inbox = inboxId();
        String work = service.createList("Trabajo").getId();

        Task task = service.addTask(inbox, "A");

        assertEquals(inbox, task.getListId());
        assertEquals(List.of(task), service.listTasks(inbox));
        assertTrue(service.listTasks(work).isEmpty());
    }

    @Test
    void addTaskThrowsWhenListDoesNotExist() {
        assertThrows(TaskListNotFoundException.class, () -> service.addTask("99", "A"));
    }

    @Test
    void addTaskRejectsEmptyTitle() {
        String listId = inboxId();
        assertThrows(IllegalArgumentException.class, () -> service.addTask(listId, ""));
    }

    @Test
    void addTaskRejectsBlankTitle() {
        String listId = inboxId();
        assertThrows(IllegalArgumentException.class, () -> service.addTask(listId, "   "));
    }

    @Test
    void addTaskRejectsNullTitle() {
        String listId = inboxId();
        assertThrows(IllegalArgumentException.class, () -> service.addTask(listId, null));
    }

    @Test
    void addTaskRejectsEmptyTitleAndDoesNotCreateTask() {
        String listId = inboxId();
        service.addTask(listId, "Única");

        assertThrows(IllegalArgumentException.class, () -> service.addTask(listId, " "));
        assertEquals(1, service.listTasks(listId).size());
    }

    @Test
    void addTaskWithDueDateStoresDate() {
        String listId = inboxId();

        Task task = service.addTask(listId, "Entregar", "2026-08-28");

        assertEquals(LocalDate.of(2026, 8, 28), task.getDue());
    }

    @Test
    void addTaskWithoutDueDateHasNullDue() {
        String listId = inboxId();

        Task task = service.addTask(listId, "Sin fecha");

        assertNull(task.getDue());
    }

    @Test
    void addTaskRejectsInvalidDueDate() {
        String listId = inboxId();

        assertThrows(IllegalArgumentException.class, () -> service.addTask(listId, "A", "28/08/2026"));
    }

    @Test
    void listTasksReturnsEmptyWhenNoTasks() {
        String listId = inboxId();
        assertTrue(service.listTasks(listId).isEmpty());
    }

    @Test
    void listTasksReturnsOnlyTasksOfGivenList() {
        String inbox = inboxId();
        String work = service.createList("Trabajo").getId();
        service.addTask(inbox, "A");
        service.addTask(inbox, "B");
        service.addTask(work, "C");

        List<Task> inboxTasks = service.listTasks(inbox);
        List<Task> workTasks = service.listTasks(work);

        assertEquals(2, inboxTasks.size());
        assertEquals(1, workTasks.size());
        assertEquals("C", workTasks.get(0).getTitle());
    }

    @Test
    void completeTaskMarksExistingTaskCompleted() {
        Task task = service.addTask(inboxId(), "Comprar leche");

        service.completeTask(task.getId());

        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void completeTaskThrowsWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> service.completeTask("99"));
    }

    @Test
    void completeTaskIsIdempotentForAlreadyCompletedTask() {
        Task task = service.addTask(inboxId(), "Comprar leche");
        service.completeTask(task.getId());

        assertDoesNotThrow(() -> service.completeTask(task.getId()));
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void reopenTaskMarksCompletedTaskPending() {
        Task task = service.addTask(inboxId(), "Comprar leche");
        service.completeTask(task.getId());

        service.reopenTask(task.getId());

        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    void reopenTaskThrowsWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> service.reopenTask("99"));
    }

    @Test
    void reopenTaskIsIdempotentForPendingTask() {
        Task task = service.addTask(inboxId(), "Comprar leche");

        assertDoesNotThrow(() -> service.reopenTask(task.getId()));
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    void deleteTaskRemovesExistingTask() {
        String listId = inboxId();
        Task task = service.addTask(listId, "Comprar leche");

        service.deleteTask(task.getId());

        assertTrue(service.listTasks(listId).isEmpty());
    }

    @Test
    void deleteTaskThrowsWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> service.deleteTask("99"));
    }

    @Test
    void purgeCompletedTasksRemovesCompletedAndKeepsPending() {
        String listId = inboxId();
        Task pending = service.addTask(listId, "Pendiente");
        Task completed = service.addTask(listId, "Completada");
        service.completeTask(completed.getId());

        List<Task> removed = service.purgeCompletedTasks(listId);

        assertEquals(List.of(completed), removed);
        assertEquals(List.of(pending), service.listTasks(listId));
    }

    @Test
    void purgeCompletedTasksOnlyAffectsGivenList() {
        String inbox = inboxId();
        String work = service.createList("Trabajo").getId();
        Task inboxDone = service.addTask(inbox, "A");
        Task workDone = service.addTask(work, "B");
        service.completeTask(inboxDone.getId());
        service.completeTask(workDone.getId());

        service.purgeCompletedTasks(inbox);

        assertTrue(service.listTasks(inbox).isEmpty());
        assertEquals(List.of(workDone), service.listTasks(work));
    }

    @Test
    void purgeCompletedTasksReturnsEmptyWhenNoCompletedTasks() {
        String listId = inboxId();
        service.addTask(listId, "Pendiente");

        assertTrue(service.purgeCompletedTasks(listId).isEmpty());
        assertEquals(1, service.listTasks(listId).size());
    }

    @Test
    void renameTaskUpdatesTitleAndKeepsIdAndStatus() {
        String listId = inboxId();
        Task task = service.addTask(listId, "Original");
        String id = task.getId();
        service.completeTask(task.getId());

        service.renameTask(id, "Nuevo título");

        assertEquals("Nuevo título", task.getTitle());
        assertEquals(id, task.getId());
        assertEquals(TaskStatus.COMPLETED, task.getStatus());
    }

    @Test
    void renameTaskRejectsBlankTitle() {
        Task task = service.addTask(inboxId(), "Original");

        assertThrows(IllegalArgumentException.class, () -> service.renameTask(task.getId(), "   "));
        assertEquals("Original", task.getTitle());
    }

    @Test
    void renameTaskRejectsNullTitle() {
        Task task = service.addTask(inboxId(), "Original");

        assertThrows(IllegalArgumentException.class, () -> service.renameTask(task.getId(), null));
        assertEquals("Original", task.getTitle());
    }

    @Test
    void renameTaskThrowsWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> service.renameTask("99", "Título"));
    }

    @Test
    void setTaskDueStoresAndClearsDate() {
        Task task = service.addTask(inboxId(), "Original");

        service.setTaskDue(task.getId(), "2026-08-28");
        assertEquals(LocalDate.of(2026, 8, 28), task.getDue());

        service.setTaskDue(task.getId(), "");
        assertNull(task.getDue());
    }

    @Test
    void setTaskDueRejectsInvalidDate() {
        Task task = service.addTask(inboxId(), "Original");

        assertThrows(IllegalArgumentException.class, () -> service.setTaskDue(task.getId(), "nope"));
        assertNull(task.getDue());
    }

    @Test
    void setTaskDueThrowsWhenTaskDoesNotExist() {
        assertThrows(TaskNotFoundException.class, () -> service.setTaskDue("99", "2026-08-28"));
    }

    @Test
    void moveTaskMovesTaskToTargetList() {
        String inbox = inboxId();
        String work = service.createList("Trabajo").getId();
        Task task = service.addTask(inbox, "A");

        service.moveTask(task.getId(), work);

        assertEquals(work, task.getListId());
        assertTrue(service.listTasks(inbox).isEmpty());
        assertEquals(List.of(task), service.listTasks(work));
    }

    @Test
    void moveTaskThrowsWhenTaskDoesNotExist() {
        String work = service.createList("Trabajo").getId();

        assertThrows(TaskNotFoundException.class, () -> service.moveTask("99", work));
    }

    @Test
    void moveTaskThrowsWhenTargetListDoesNotExist() {
        String inbox = inboxId();
        Task task = service.addTask(inbox, "A");

        assertThrows(TaskListNotFoundException.class, () -> service.moveTask(task.getId(), "99"));
        assertEquals(inbox, task.getListId());
    }

    @Test
    void moveTaskToSameListKeepsTaskUnchanged() {
        String inbox = inboxId();
        Task task = service.addTask(inbox, "A");

        assertDoesNotThrow(() -> service.moveTask(task.getId(), inbox));
        assertEquals(inbox, task.getListId());
        assertEquals(List.of(task), service.listTasks(inbox));
    }

    @Test
    void listTasksOrdersPendingBeforeCompleted() {
        String listId = inboxId();
        Task a = service.addTask(listId, "A");
        Task b = service.addTask(listId, "B");
        Task c = service.addTask(listId, "C");
        service.completeTask(b.getId());

        assertEquals(List.of(a, c, b), service.listTasks(listId));
    }

    @Test
    void listTasksKeepsRelativeOrderWithinGroups() {
        String listId = inboxId();
        Task a = service.addTask(listId, "A");
        Task b = service.addTask(listId, "B");
        Task c = service.addTask(listId, "C");
        Task d = service.addTask(listId, "D");
        service.completeTask(a.getId());
        service.completeTask(c.getId());

        assertEquals(List.of(b, d, a, c), service.listTasks(listId));
    }

    @Test
    void reopenedTaskReturnsToPendingGroup() {
        String listId = inboxId();
        Task a = service.addTask(listId, "A");
        Task b = service.addTask(listId, "B");
        service.completeTask(b.getId());

        assertEquals(List.of(a, b), service.listTasks(listId));

        service.reopenTask(b.getId());

        assertEquals(List.of(a, b), service.listTasks(listId));
    }

    @Test
    void loadPopulatesListsAndTasksFromProvider() {
        TaskProvider provider = new FakeTaskProvider();
        TaskList list = provider.createTaskList("Inbox");
        provider.createTask(list.getId(), "A", null);
        provider.createTask(list.getId(), "B", null);
        TaskService loaded = new TaskService(provider);

        loaded.load();

        assertEquals(1, loaded.listLists().size());
        assertEquals(2, loaded.listTasks(list.getId()).size());
    }

    @Test
    void getZoomLevelDefaultsToZero() {
        assertEquals(0, service.getZoomLevel());
    }

    @Test
    void setZoomLevelStoresValueWithinRange() {
        service.setZoomLevel(2);

        assertEquals(2, service.getZoomLevel());
    }

    @Test
    void setZoomLevelClampsAboveMax() {
        service.setZoomLevel(99);

        assertEquals(TaskService.MAX_ZOOM, service.getZoomLevel());
    }

    @Test
    void setZoomLevelClampsBelowMin() {
        service.setZoomLevel(-99);

        assertEquals(TaskService.MIN_ZOOM, service.getZoomLevel());
    }
}
