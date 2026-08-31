package tasktracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasktracker.FakeAccountProvider;
import tasktracker.model.Settings;
import tasktracker.service.SettingsStore;

class SettingsWindowTest {

    @TempDir
    Path tempDir;

    private final Settings settings = new Settings();
    private SettingsStore store;

    private SettingsWindow window() {
        store = new SettingsStore(tempDir);
        return new SettingsWindow(settings, store, new FakeAccountProvider());
    }

    @Test
    void enterTogglesHideEmptyLists() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke(KeyType.Enter));

        assertTrue(settings.isHideEmptyLists());
    }

    @Test
    void enterTogglesHideCompletedTasks() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke('j', false, false));
        window.handleInput(new KeyStroke(KeyType.Enter));

        assertTrue(settings.isHideCompletedTasks());
    }

    @Test
    void togglingPersistsToStore() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke(KeyType.Enter));

        assertTrue(store.load().isHideEmptyLists());
    }

    @Test
    void themeItemDoesNothing() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke('j', false, false));
        window.handleInput(new KeyStroke('j', false, false));
        window.handleInput(new KeyStroke('j', false, false));
        window.handleInput(new KeyStroke(KeyType.Enter));

        assertFalse(settings.isHideEmptyLists());
        assertFalse(settings.isHideCompletedTasks());
    }

    @Test
    void navigationWrapsAround() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke('k', false, false));

        assertEquals(3, window.selectedIndex());
    }

    @Test
    void accountLabelShowsEmailWhenConfigured() {
        FakeAccountProvider account = new FakeAccountProvider();
        account.setEmail("user@example.com");
        SettingsWindow window = new SettingsWindow(settings, new SettingsStore(tempDir), account);

        assertEquals("Cuenta: user@example.com", window.accountLabel());
    }

    @Test
    void accountLabelShowsConfigurarWhenNoAccount() {
        SettingsWindow window = window();

        assertEquals("Cuenta: configurar", window.accountLabel());
    }

    @Test
    void escapeClosesWindow() {
        SettingsWindow window = window();

        window.handleInput(new KeyStroke(KeyType.Escape));

        assertFalse(settings.isHideEmptyLists());
    }
}
