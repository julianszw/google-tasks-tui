package tasktracker.provider;

import java.time.LocalDate;
import java.util.List;
import java.util.function.Supplier;
import tasktracker.model.Task;
import tasktracker.model.TaskList;

/**
 * Envuelve un {@link TaskProvider} para re-autenticar automáticamente cuando la
 * credencial expira o es revocada durante la sesión. Ante una
 * {@link AuthenticationExpiredException}, cierra la sesión, relanza el flujo OAuth
 * y reconstruye el proveedor delegado con la credencial recién autorizada,
 * reintentando la operación un número acotado de veces.
 */
public final class ReauthenticatingTaskProvider implements TaskProvider {

    private static final int MAX_REAUTH_ATTEMPTS = 1;

    private final AccountProvider account;
    private final Supplier<TaskProvider> providerFactory;
    private TaskProvider delegate;

    public ReauthenticatingTaskProvider(AccountProvider account, Supplier<TaskProvider> providerFactory) {
        this.account = account;
        this.providerFactory = providerFactory;
        this.delegate = providerFactory.get();
    }

    @Override
    public List<TaskList> listTaskLists() {
        return withReauth(() -> delegate.listTaskLists());
    }

    @Override
    public TaskList createTaskList(String title) {
        return withReauth(() -> delegate.createTaskList(title));
    }

    @Override
    public void deleteTaskList(String listId) {
        withReauth(() -> delegate.deleteTaskList(listId));
    }

    @Override
    public List<Task> listTasks(String listId) {
        return withReauth(() -> delegate.listTasks(listId));
    }

    @Override
    public Task createTask(String listId, String title, LocalDate due) {
        return withReauth(() -> delegate.createTask(listId, title, due));
    }

    @Override
    public Task updateTask(Task task) {
        return withReauth(() -> delegate.updateTask(task));
    }

    @Override
    public void deleteTask(String listId, String taskId) {
        withReauth(() -> delegate.deleteTask(listId, taskId));
    }

    @Override
    public Task moveTask(String taskListId, String taskId, String destinationListId) {
        return withReauth(() -> delegate.moveTask(taskListId, taskId, destinationListId));
    }

    @Override
    public String providerName() {
        return delegate.providerName();
    }

    private <T> T withReauth(Supplier<T> operation) {
        int reauths = 0;
        while (true) {
            try {
                return operation.get();
            } catch (AuthenticationExpiredException e) {
                if (reauths >= MAX_REAUTH_ATTEMPTS) {
                    throw new AuthenticationExpiredException(
                            "La re-autenticación no resolvió el problema: " + e.getMessage(), e);
                }
                reauths++;
                account.signOut();
                account.authorize();
                delegate = providerFactory.get();
            }
        }
    }

    private void withReauth(Runnable operation) {
        withReauth(() -> {
            operation.run();
            return null;
        });
    }
}
