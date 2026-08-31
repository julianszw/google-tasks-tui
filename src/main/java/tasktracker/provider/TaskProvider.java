package tasktracker.provider;

import java.time.LocalDate;
import java.util.List;
import tasktracker.model.Task;
import tasktracker.model.TaskList;

public interface TaskProvider {

    List<TaskList> listTaskLists();

    TaskList createTaskList(String title);

    void deleteTaskList(String listId);

    List<Task> listTasks(String listId);

    Task createTask(String listId, String title, LocalDate due);

    Task updateTask(Task task);

    void deleteTask(String listId, String taskId);

    Task moveTask(String taskListId, String taskId, String destinationListId);

    default String providerName() {
        return "Local";
    }
}
