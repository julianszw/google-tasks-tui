package tasktracker.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import tasktracker.model.Task;
import tasktracker.model.TaskList;

class ReauthenticatingTaskProviderTest {

    @Test
    void rebuildsProviderWithFreshCredentialAfterReauth() {
        FakeAccountProvider account = new FakeAccountProvider();
        AtomicBoolean expired = new AtomicBoolean(true);
        List<FailFirstListProvider> built = new ArrayList<>();

        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(account, () -> {
            FailFirstListProvider p = new FailFirstListProvider(expired);
            built.add(p);
            return p;
        });

        provider.listTaskLists();

        assertEquals(2, built.size());
        assertEquals(1, built.get(0).calls());
        assertEquals(1, built.get(1).calls());
        assertTrue(account.isSignedOut());
        assertEquals(1, account.authorizeCalls());
    }

    @Test
    void reauthsAndRetriesVoidOperation() {
        FakeAccountProvider account = new FakeAccountProvider();
        AtomicBoolean expired = new AtomicBoolean(true);
        List<FailFirstDeleteProvider> built = new ArrayList<>();

        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(account, () -> {
            FailFirstDeleteProvider p = new FailFirstDeleteProvider(expired);
            built.add(p);
            return p;
        });

        provider.deleteTaskList("list-1");

        assertEquals(2, built.size());
        assertEquals(1, built.get(0).calls());
        assertEquals(1, built.get(1).calls());
        assertTrue(account.isSignedOut());
        assertEquals(1, account.authorizeCalls());
    }

    @Test
    void passesArgumentsToFreshDelegateAfterReauth() {
        FakeAccountProvider account = new FakeAccountProvider();
        AtomicBoolean expired = new AtomicBoolean(true);
        List<FailFirstCreateTaskProvider> built = new ArrayList<>();
        LocalDate due = LocalDate.of(2026, 9, 13);

        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(account, () -> {
            FailFirstCreateTaskProvider p = new FailFirstCreateTaskProvider(expired);
            built.add(p);
            return p;
        });

        Task task = provider.createTask("list-1", "Mi tarea", due);

        assertNotNull(task);
        assertEquals(2, built.size());
        assertEquals(1, built.get(0).calls());
        assertEquals(1, built.get(1).calls());
        assertEquals("list-1", built.get(1).lastListId());
        assertEquals("Mi tarea", built.get(1).lastTitle());
        assertEquals(due, built.get(1).lastDue());
        assertTrue(account.isSignedOut());
        assertEquals(1, account.authorizeCalls());
    }

    @Test
    void stopsAfterMaxReauthAttempts() {
        FakeAccountProvider account = new FakeAccountProvider();
        AtomicInteger builds = new AtomicInteger();
        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(account, () -> {
            builds.incrementAndGet();
            return new AlwaysFailProvider();
        });

        AuthenticationExpiredException ex = assertThrows(
                AuthenticationExpiredException.class, provider::listTaskLists);

        assertEquals(2, builds.get());
        assertEquals(1, account.authorizeCalls());
        assertTrue(ex.getMessage().contains("re-autenticación no resolvió"));
    }

    @Test
    void doesNotReauthForOtherProviderExceptions() {
        FakeAccountProvider account = new FakeAccountProvider();
        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(account,
                () -> new ThrowingProvider(new ProviderException("otro error")));

        assertThrows(ProviderException.class, provider::listTaskLists);

        assertFalse(account.isSignedOut());
        assertEquals(0, account.authorizeCalls());
    }

    @Test
    void delegatesProviderNameToCurrentDelegate() {
        ReauthenticatingTaskProvider provider = new ReauthenticatingTaskProvider(
                new FakeAccountProvider(), () -> new NamedProvider("Google Tasks"));

        assertEquals("Google Tasks", provider.providerName());
    }

    /** Base que lanza {@link UnsupportedOperationException} en todo método no sobrescrito. */
    private abstract static class StubTaskProvider implements TaskProvider {

        @Override
        public List<TaskList> listTaskLists() {
            throw new UnsupportedOperationException();
        }

        @Override
        public TaskList createTaskList(String title) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteTaskList(String listId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Task> listTasks(String listId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Task createTask(String listId, String title, LocalDate due) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Task updateTask(Task task) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteTask(String listId, String taskId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Task moveTask(String taskListId, String taskId, String destinationListId) {
            throw new UnsupportedOperationException();
        }
    }

    /** Falla la primera invocación de {@code listTaskLists()} (credencial expirada) y luego responde. */
    private static final class FailFirstListProvider extends StubTaskProvider {

        private final AtomicBoolean expired;
        private int calls;

        FailFirstListProvider(AtomicBoolean expired) {
            this.expired = expired;
        }

        @Override
        public List<TaskList> listTaskLists() {
            calls++;
            if (expired.compareAndSet(true, false)) {
                throw new AuthenticationExpiredException("sesión expirada");
            }
            return List.of();
        }

        int calls() {
            return calls;
        }
    }

    /** Falla la primera invocación de {@code deleteTaskList()} y luego completa el borrado. */
    private static final class FailFirstDeleteProvider extends StubTaskProvider {

        private final AtomicBoolean expired;
        private int calls;

        FailFirstDeleteProvider(AtomicBoolean expired) {
            this.expired = expired;
        }

        @Override
        public void deleteTaskList(String listId) {
            calls++;
            if (expired.compareAndSet(true, false)) {
                throw new AuthenticationExpiredException("sesión expirada");
            }
        }

        int calls() {
            return calls;
        }
    }

    /** Falla la primera invocación de {@code createTask()} y luego registra los argumentos. */
    private static final class FailFirstCreateTaskProvider extends StubTaskProvider {

        private final AtomicBoolean expired;
        private int calls;
        private String lastListId;
        private String lastTitle;
        private LocalDate lastDue;

        FailFirstCreateTaskProvider(AtomicBoolean expired) {
            this.expired = expired;
        }

        @Override
        public Task createTask(String listId, String title, LocalDate due) {
            calls++;
            if (expired.compareAndSet(true, false)) {
                throw new AuthenticationExpiredException("sesión expirada");
            }
            this.lastListId = listId;
            this.lastTitle = title;
            this.lastDue = due;
            Task task = new Task(title);
            task.setId("t-1");
            task.setListId(listId);
            return task;
        }

        int calls() {
            return calls;
        }

        String lastListId() {
            return lastListId;
        }

        String lastTitle() {
            return lastTitle;
        }

        LocalDate lastDue() {
            return lastDue;
        }
    }

    /** Siempre lanza {@link AuthenticationExpiredException}. */
    private static final class AlwaysFailProvider extends StubTaskProvider {

        @Override
        public List<TaskList> listTaskLists() {
            throw new AuthenticationExpiredException("sesión expirada");
        }
    }

    /** Lanza la excepción indicada en {@code listTaskLists()}. */
    private static final class ThrowingProvider extends StubTaskProvider {

        private final ProviderException exception;

        ThrowingProvider(ProviderException exception) {
            this.exception = exception;
        }

        @Override
        public List<TaskList> listTaskLists() {
            throw exception;
        }
    }

    /** Proveedor con nombre personalizado para probar la delegación. */
    private static final class NamedProvider extends StubTaskProvider {

        private final String name;

        NamedProvider(String name) {
            this.name = name;
        }

        @Override
        public String providerName() {
            return name;
        }
    }
}
