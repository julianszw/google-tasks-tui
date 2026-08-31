package tasktracker.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tasktracker.model.Settings;

class SettingsStoreTest {

    @TempDir
    Path tempDir;

    @Test
    void loadWithNoFileReturnsDefaults() {
        SettingsStore store = new SettingsStore(tempDir);

        Settings settings = store.load();

        assertFalse(settings.isHideEmptyLists());
        assertFalse(settings.isHideCompletedTasks());
    }

    @Test
    void saveAndLoadRoundTrip() {
        SettingsStore store = new SettingsStore(tempDir);
        Settings settings = new Settings();
        settings.setHideEmptyLists(true);
        settings.setHideCompletedTasks(true);

        store.save(settings);

        Settings loaded = store.load();
        assertTrue(loaded.isHideEmptyLists());
        assertTrue(loaded.isHideCompletedTasks());
    }

    @Test
    void saveIsBestEffortOnMissingDirectory() {
        SettingsStore store = new SettingsStore(tempDir.resolve("sub").resolve("dir"));
        Settings settings = new Settings();
        settings.setHideEmptyLists(true);

        store.save(settings);

        assertTrue(settings.isHideEmptyLists());
    }
}
