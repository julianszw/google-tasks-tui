package tasktracker.google;

import com.google.api.client.auth.oauth2.TokenErrorResponse;
import com.google.api.client.auth.oauth2.TokenResponseException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.tasks.Tasks;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import tasktracker.model.Task;
import tasktracker.model.TaskList;
import tasktracker.provider.AuthenticationExpiredException;
import tasktracker.provider.ProviderException;
import tasktracker.provider.TaskProvider;

public final class GoogleTasksProvider implements TaskProvider {

    private static final String APPLICATION_NAME = "cli-task-tracker";
    private static final String DUE_TIME_SUFFIX = "T00:00:00.000Z";
    private static final int MAX_RESULTS = 100;

    private final Tasks tasks;

    public GoogleTasksProvider(GoogleAuth auth) {
        try {
            this.tasks = new Tasks.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    auth.loadCredential())
                    .setApplicationName(APPLICATION_NAME)
                    .build();
        } catch (IOException | GeneralSecurityException e) {
            throw new ProviderException("No se pudo construir el cliente de Google Tasks: " + e.getMessage(), e);
        }
    }

    @Override
    public List<TaskList> listTaskLists() {
        return call("No se pudieron listar las listas", () -> {
            List<com.google.api.services.tasks.model.TaskList> all = new ArrayList<>();
            String pageToken = null;
            do {
                var request = tasks.tasklists().list().setMaxResults(MAX_RESULTS);
                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }
                var response = request.execute();
                if (response.getItems() != null) {
                    all.addAll(response.getItems());
                }
                pageToken = response.getNextPageToken();
            } while (pageToken != null);
            return all.stream().map(GoogleTasksProvider::toTaskList).toList();
        });
    }

    @Override
    public TaskList createTaskList(String title) {
        return call("No se pudo crear la lista", () -> {
            com.google.api.services.tasks.model.TaskList created = tasks.tasklists()
                    .insert(new com.google.api.services.tasks.model.TaskList().setTitle(title))
                    .execute();
            return toTaskList(created);
        });
    }

    @Override
    public void deleteTaskList(String listId) {
        call("No se pudo eliminar la lista", () -> {
            tasks.tasklists().delete(listId).execute();
            return null;
        });
    }

    @Override
    public List<Task> listTasks(String listId) {
        return call("No se pudieron listar las tareas", () -> {
            List<com.google.api.services.tasks.model.Task> all = new ArrayList<>();
            String pageToken = null;
            do {
                var request = tasks.tasks().list(listId).setMaxResults(MAX_RESULTS);
                if (pageToken != null) {
                    request.setPageToken(pageToken);
                }
                var response = request.execute();
                if (response.getItems() != null) {
                    all.addAll(response.getItems());
                }
                pageToken = response.getNextPageToken();
            } while (pageToken != null);
            return all.stream().map(task -> toTask(task, listId)).toList();
        });
    }

    @Override
    public Task createTask(String listId, String title, LocalDate due) {
        return call("No se pudo crear la tarea", () -> {
            com.google.api.services.tasks.model.Task googleTask =
                    new com.google.api.services.tasks.model.Task().setTitle(title);
            if (due != null) {
                googleTask.setDue(toGoogleDue(due));
            }
            return toTask(tasks.tasks().insert(listId, googleTask).execute(), listId);
        });
    }

    @Override
    public Task updateTask(Task task) {
        return call("No se pudo actualizar la tarea",
                () -> toTask(tasks.tasks().update(task.getListId(), task.getId(), toGoogleTask(task)).execute(),
                        task.getListId()));
    }

    @Override
    public void deleteTask(String listId, String taskId) {
        call("No se pudo eliminar la tarea", () -> {
            tasks.tasks().delete(listId, taskId).execute();
            return null;
        });
    }

    @Override
    public Task moveTask(String taskListId, String taskId, String destinationListId) {
        return call("No se pudo mover la tarea", () -> {
            com.google.api.services.tasks.model.Task moved = tasks.tasks().move(taskListId, taskId)
                    .setDestinationTasklist(destinationListId)
                    .execute();
            return toTask(moved, destinationListId);
        });
    }

    @Override
    public String providerName() {
        return "Google Tasks";
    }

    private <T> T call(String message, ThrowingSupplier<T> operation) {
        try {
            return operation.get();
        } catch (IOException e) {
            throw toProviderException(message, e);
        }
    }

    @FunctionalInterface
    private interface ThrowingSupplier<T> {
        T get() throws IOException;
    }

    private static Task toTask(com.google.api.services.tasks.model.Task googleTask, String listId) {
        Task task = new Task(googleTask.getTitle());
        task.setId(googleTask.getId());
        task.setListId(listId);
        task.setDue(parseDue(googleTask.getDue()));
        if ("completed".equals(googleTask.getStatus())) {
            task.markCompleted();
        }
        return task;
    }

    private static com.google.api.services.tasks.model.Task toGoogleTask(Task task) {
        com.google.api.services.tasks.model.Task googleTask = new com.google.api.services.tasks.model.Task()
                .setId(task.getId())
                .setTitle(task.getTitle())
                .setStatus(task.isCompleted() ? "completed" : "needsAction");
        if (task.getDue() != null) {
            googleTask.setDue(toGoogleDue(task.getDue()));
        }
        return googleTask;
    }

    private static TaskList toTaskList(com.google.api.services.tasks.model.TaskList list) {
        TaskList taskList = new TaskList(list.getTitle());
        taskList.setId(list.getId());
        return taskList;
    }

    private static String toGoogleDue(LocalDate due) {
        return due + DUE_TIME_SUFFIX;
    }

    private static LocalDate parseDue(String due) {
        if (due == null) {
            return null;
        }
        try {
            return OffsetDateTime.parse(due).toLocalDate();
        } catch (DateTimeParseException e) {
            return LocalDate.parse(due.substring(0, Math.min(10, due.length())));
        }
    }

    /**
     * Convierte la {@link IOException} de Google en una {@link ProviderException}.
     * Package-private para poder testear la detección de credencial inválida sin red.
     */
    static ProviderException toProviderException(String message, IOException e) {
        if (isInvalidGrant(e) || isInsufficientScopes(e)) {
            return new AuthenticationExpiredException(
                    message + ": la sesión de Google expiró o fue revocada", e);
        }
        if (e instanceof GoogleJsonResponseException ge && ge.getDetails() != null) {
            return new ProviderException(message + ": " + ge.getDetails().getMessage(), e);
        }
        return new ProviderException(message + ": " + e.getMessage(), e);
    }

    private static boolean isInvalidGrant(IOException e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof TokenResponseException tre && isInvalidGrantError(tre.getDetails())) {
                return true;
            }
            // Defensa: `invalid_grant` normalmente llega como `TokenResponseException` (endpoint
            // de token). Se cubre además el caso JSON por si la API lo reporta como
            // `GoogleJsonResponseException` con `errors[].reason == "invalid_grant"`.
            if (cause instanceof GoogleJsonResponseException gre && containsInvalidGrantReason(gre.getDetails())) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private static boolean isInvalidGrantError(TokenErrorResponse details) {
        return details != null && "invalid_grant".equals(details.getError());
    }

    private static boolean containsInvalidGrantReason(GoogleJsonError details) {
        if (details == null || details.getErrors() == null) {
            return false;
        }
        return details.getErrors().stream().anyMatch(info -> "invalid_grant".equals(info.getReason()));
    }

    private static boolean isInsufficientScopes(IOException e) {
        if (!(e instanceof GoogleJsonResponseException ge) || ge.getDetails() == null) {
            return false;
        }
        if (ge.getDetails().getCode() == 403
                && ge.getDetails().getErrors() != null
                && ge.getDetails().getErrors().stream()
                        .anyMatch(info -> "insufficientAuthenticationScopes".equals(info.getReason()))) {
            return true;
        }
        return ge.getDetails().getMessage() != null
                && ge.getDetails().getMessage().contains("insufficient authentication scopes");
    }
}
