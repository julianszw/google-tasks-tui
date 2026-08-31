package tasktracker.cli;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Direction;
import com.googlecode.lanterna.gui2.LinearLayout;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextGUI;
import com.googlecode.lanterna.gui2.Window;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import java.io.IOException;
import java.util.List;
import tasktracker.model.Settings;
import tasktracker.provider.AccountProvider;
import tasktracker.provider.ProviderException;
import tasktracker.service.SettingsStore;

public class SettingsWindow extends BasicWindow {

    private static final String TITLE = "⚙ Ajustes";
    private static final String ON = "Sí";
    private static final String OFF = "No";

    private final Settings settings;
    private final SettingsStore store;
    private final AccountProvider account;
    private final ActionListBox list = new ActionListBox();
    private int selected;
    private boolean accountChanged;

    public SettingsWindow(Settings settings, SettingsStore store, AccountProvider account) {
        super(TITLE);
        this.settings = settings;
        this.store = store;
        this.account = account;

        setHints(List.of(Window.Hint.CENTERED));
        rebuild();

        Panel content = new Panel(new LinearLayout(Direction.VERTICAL));
        content.addComponent(list);
        setComponent(content);
        setFocusedInteractable(list);
    }

    private void rebuild() {
        list.clearItems();
        list.addItem("Ocultar listas vacías: " + (settings.isHideEmptyLists() ? ON : OFF), () -> {
        });
        list.addItem("Ocultar tareas completadas: " + (settings.isHideCompletedTasks() ? ON : OFF), () -> {
        });
        list.addItem(accountLabel(), () -> {
        });
        list.addItem("Tema: claro · oscuro (próximamente)", () -> {
        });
        if (selected >= list.getItemCount()) {
            selected = list.getItemCount() - 1;
        }
        list.setSelectedIndex(selected);
    }

    int selectedIndex() {
        return list.getSelectedIndex();
    }

    boolean isAccountChanged() {
        return accountChanged;
    }

    String accountLabel() {
        if (account == null) {
            return "Cuenta: —";
        }
        String email = currentEmail();
        return email == null ? "Cuenta: configurar" : "Cuenta: " + email;
    }

    @Override
    public boolean handleInput(KeyStroke key) {
        if (key.getKeyType() == KeyType.Escape) {
            close();
            return true;
        }
        if (key.getKeyType() == KeyType.Enter) {
            activateSelected();
            return true;
        }
        if (key.getKeyType() == KeyType.ArrowUp) {
            moveSelection(-1);
            return true;
        }
        if (key.getKeyType() == KeyType.ArrowDown) {
            moveSelection(1);
            return true;
        }
        if (key.getKeyType() == KeyType.Character) {
            Character c = key.getCharacter();
            if (c != null) {
                switch (c) {
                    case 'k' -> {
                        moveSelection(-1);
                        return true;
                    }
                    case 'j' -> {
                        moveSelection(1);
                        return true;
                    }
                    default -> {
                    }
                }
            }
        }
        return super.handleInput(key);
    }

    private void activateSelected() {
        switch (list.getSelectedIndex()) {
            case 0 -> toggleHideEmptyLists();
            case 1 -> toggleHideCompletedTasks();
            case 2 -> activateAccount();
            default -> {
                // "Tema": botón muerto, sin comportamiento.
            }
        }
    }

    private void toggleHideEmptyLists() {
        settings.setHideEmptyLists(!settings.isHideEmptyLists());
        store.save(settings);
        selected = list.getSelectedIndex();
        rebuild();
    }

    private void toggleHideCompletedTasks() {
        settings.setHideCompletedTasks(!settings.isHideCompletedTasks());
        store.save(settings);
        selected = list.getSelectedIndex();
        rebuild();
    }

    private void activateAccount() {
        if (account == null) {
            return;
        }
        if (currentEmail() == null) {
            try {
                runAuthorize();
                accountChanged = true;
            } catch (ProviderException e) {
                // La autenticación falló; no se marca el cambio de cuenta.
            }
            close();
            return;
        }
        WindowBasedTextGUI gui = windowBasedGui();
        if (gui == null) {
            return;
        }
        OptionMenuWindow menu = new OptionMenuWindow("Cuenta: " + currentEmail(),
                List.of("Cambiar de cuenta", "Cerrar sesión"));
        gui.addWindowAndWait(menu);
        if (menu.selectedIndex() == 0) {
            account.signOut();
            try {
                runAuthorize();
            } catch (ProviderException e) {
                // La autenticación falló; la cuenta queda sin sesión.
            }
            accountChanged = true;
            close();
        } else if (menu.selectedIndex() == 1) {
            account.signOut();
            accountChanged = true;
            close();
        }
    }

    private String currentEmail() {
        try {
            return account.accountEmail();
        } catch (ProviderException e) {
            return null;
        }
    }

    private void runAuthorize() {
        WindowBasedTextGUI gui = windowBasedGui();
        if (gui == null) {
            account.authorize();
            return;
        }
        try {
            gui.getScreen().stopScreen();
        } catch (IOException e) {
            account.authorize();
            return;
        }
        try {
            account.authorize();
        } finally {
            try {
                gui.getScreen().startScreen();
            } catch (IOException ignored) {
                // No se pudo reanudar la pantalla; la sesión continúa sin autorizar.
            }
        }
    }

    private WindowBasedTextGUI windowBasedGui() {
        TextGUI gui = getTextGUI();
        return gui instanceof WindowBasedTextGUI wbg ? wbg : null;
    }

    private void moveSelection(int delta) {
        int count = list.getItemCount();
        if (count == 0) {
            return;
        }
        list.setSelectedIndex(MenuNavigation.cycle(list.getSelectedIndex(), delta, count));
    }
}
