package tasktracker.cli;

import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import tasktracker.model.Settings;
import tasktracker.provider.AccountProvider;
import tasktracker.service.SettingsStore;
import tasktracker.service.TaskService;

public class LanternaTaskTrackerView {

    private final WindowBasedTextGUI gui;
    private final TaskService service;
    private final Settings settings;
    private final SettingsStore settingsStore;
    private final AccountProvider account;

    public LanternaTaskTrackerView(WindowBasedTextGUI gui, TaskService service, Settings settings,
            SettingsStore settingsStore, AccountProvider account) {
        this.gui = gui;
        this.service = service;
        this.settings = settings;
        this.settingsStore = settingsStore;
        this.account = account;
    }

    public void start() {
        TaskListWindow window = new TaskListWindow(service, gui, settings, settingsStore, account);
        gui.addWindowAndWait(window);
    }
}
