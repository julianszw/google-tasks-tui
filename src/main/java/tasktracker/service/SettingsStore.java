package tasktracker.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import tasktracker.model.Settings;

public class SettingsStore {

    private static final String FILE_NAME = "settings.properties";
    private static final String KEY_HIDE_EMPTY_LISTS = "hideEmptyLists";
    private static final String KEY_HIDE_COMPLETED_TASKS = "hideCompletedTasks";

    private final Path path;

    public SettingsStore(Path workingDir) {
        this.path = workingDir.resolve(FILE_NAME);
    }

    public Settings load() {
        Settings settings = new Settings();
        if (!Files.exists(path)) {
            return settings;
        }
        Properties properties = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            properties.load(in);
        } catch (IOException e) {
            return settings;
        }
        settings.setHideEmptyLists(Boolean.parseBoolean(properties.getProperty(KEY_HIDE_EMPTY_LISTS, "false")));
        settings.setHideCompletedTasks(Boolean.parseBoolean(properties.getProperty(KEY_HIDE_COMPLETED_TASKS, "false")));
        return settings;
    }

    public void save(Settings settings) {
        Properties properties = new Properties();
        properties.setProperty(KEY_HIDE_EMPTY_LISTS, Boolean.toString(settings.isHideEmptyLists()));
        properties.setProperty(KEY_HIDE_COMPLETED_TASKS, Boolean.toString(settings.isHideCompletedTasks()));
        try (OutputStream out = Files.newOutputStream(path)) {
            properties.store(out, null);
        } catch (IOException e) {
            // Persistencia best-effort: no interrumpe la sesión si falla el guardado.
        }
    }
}
