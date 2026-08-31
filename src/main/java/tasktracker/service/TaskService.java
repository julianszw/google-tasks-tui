package tasktracker.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tasktracker.exception.TaskListNotFoundException;
import tasktracker.exception.TaskNotFoundException;
import tasktracker.model.Task;
import tasktracker.model.TaskList;
import tasktracker.provider.TaskProvider;

public class TaskService {

    public static final int MIN_ZOOM = -2;
    public static final int MAX_ZOOM = 2;
    public static final int DEFAULT_ZOOM = 0;

    private final TaskProvider provider;
    private final Map<String, TaskList> lists = new LinkedHashMap<>();
    private final Map<String, Task> tasks = new LinkedHashMap<>();
    private int zoom;

    public TaskService(TaskProvider provider) {
        this.provider = provider;
    }

    public void load() {
        lists.clear();
        tasks.clear();
        for (TaskList list : provider.listTaskLists()) {
            lists.put(list.getId(), list);
            for (Task task : provider.listTasks(list.getId())) {
                tasks.put(task.getId(), task);
            }
        }
    }

    public List<TaskList> listLists() {
        return List.copyOf(lists.values());
    }

    public TaskList createList(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la lista no puede estar vacío");
        }
        TaskList created = provider.createTaskList(name);
        lists.put(created.getId(), created);
        return created;
    }

    public List<TaskList> deleteEmptyLists() {
        List<TaskList> empty = lists.values().stream()
                .filter(list -> listTasks(list.getId()).isEmpty())
                .toList();
        for (TaskList list : empty) {
            provider.deleteTaskList(list.getId());
            lists.remove(list.getId());
        }
        return empty;
    }

    public Task addTask(String listId, String title) {
        return addTask(listId, title, null);
    }

    public Task addTask(String listId, String title, String due) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        requireList(listId);
        Task created = provider.createTask(listId, title, parseDue(due));
        tasks.put(created.getId(), created);
        return created;
    }

    public List<Task> listTasks(String listId) {
        return tasks.values().stream()
                .filter(task -> listId.equals(task.getListId()))
                .sorted(Comparator.comparing(Task::isCompleted))
                .toList();
    }

    public void completeTask(String id) {
        Task task = requireTask(id);
        task.markCompleted();
        provider.updateTask(task);
    }

    public void reopenTask(String id) {
        Task task = requireTask(id);
        task.markPending();
        provider.updateTask(task);
    }

    public void renameTask(String id, String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("El título no puede estar vacío");
        }
        Task task = requireTask(id);
        task.rename(title);
        provider.updateTask(task);
    }

    public void setTaskDue(String id, String due) {
        Task task = requireTask(id);
        task.setDue(parseDue(due));
        provider.updateTask(task);
    }

    public void deleteTask(String id) {
        Task task = requireTask(id);
        provider.deleteTask(task.getListId(), task.getId());
        tasks.remove(id);
    }

    public void moveTask(String taskId, String targetListId) {
        requireList(targetListId);
        Task task = requireTask(taskId);
        if (task.getListId().equals(targetListId)) {
            return;
        }
        provider.moveTask(task.getListId(), task.getId(), targetListId);
        task.setListId(targetListId);
    }

    public List<Task> purgeCompletedTasks(String listId) {
        List<Task> removed = listTasks(listId).stream()
                .filter(Task::isCompleted)
                .toList();
        for (Task task : removed) {
            provider.deleteTask(task.getListId(), task.getId());
            tasks.remove(task.getId());
        }
        return removed;
    }

    public int getZoomLevel() {
        return zoom;
    }

    public void setZoomLevel(int level) {
        this.zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, level));
    }

    public String providerName() {
        return provider.providerName();
    }

    private Task requireTask(String id) {
        Task task = tasks.get(id);
        if (task == null) {
            throw new TaskNotFoundException(id);
        }
        return task;
    }

    private void requireList(String id) {
        if (!lists.containsKey(id)) {
            throw new TaskListNotFoundException(id);
        }
    }

    private static LocalDate parseDue(String due) {
        if (due == null || due.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(due.trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("La fecha debe tener formato yyyy-MM-dd");
        }
    }
}
