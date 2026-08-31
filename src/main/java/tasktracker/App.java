package tasktracker;

import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import java.io.IOException;
import java.nio.file.Path;
import tasktracker.cli.LanternaTaskTrackerView;
import tasktracker.cli.VisualStyle;
import tasktracker.google.GoogleAuth;
import tasktracker.google.GoogleTasksProvider;
import tasktracker.model.Settings;
import tasktracker.provider.ProviderException;
import tasktracker.service.SettingsStore;
import tasktracker.service.TaskService;

public class App {

    public static void main(String[] args) {
        int exitCode = run();
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    private static int run() {
        try {
            Path workingDir = Path.of(".");
            GoogleAuth auth = new GoogleAuth(workingDir);
            if (!auth.hasStoredCredentials()) {
                auth.authorize();
            }

            TaskService service = new TaskService(new GoogleTasksProvider(auth));
            service.load();
            if (service.listLists().isEmpty()) {
                service.createList("Inbox");
            }

            SettingsStore settingsStore = new SettingsStore(workingDir);
            Settings settings = settingsStore.load();

            Terminal terminal = new DefaultTerminalFactory().setTerminalEmulatorTitle("TaskMaster").createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            try {
                WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
                gui.setTheme(VisualStyle.theme());

                LanternaTaskTrackerView view = new LanternaTaskTrackerView(gui, service, settings, settingsStore, auth);
                view.start();
            } finally {
                screen.stopScreen();
            }
            return 0;
        } catch (ProviderException e) {
            System.err.println("Error: " + e.getMessage());
            return 1;
        } catch (IOException e) {
            System.err.println("Error de terminal: " + e.getMessage());
            return 1;
        }
    }
}
