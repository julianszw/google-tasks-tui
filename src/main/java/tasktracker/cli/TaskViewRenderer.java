package tasktracker.cli;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import java.time.LocalDate;
import java.util.List;
import tasktracker.model.Task;

final class TaskViewRenderer implements ComponentRenderer<TaskViewComponent> {

    private static final String APP_TITLE = "TaskMaster";
    private static final String NO_TASKS = "No hay tareas cargadas";

    private static final int STATUS_BAR_HEIGHT = 5;

    private static final char BOX_H = '─';
    private static final char BOX_V = '│';
    private static final char BOX_TL = '╭';
    private static final char BOX_TR = '╮';
    private static final char BOX_BL = '╰';
    private static final char BOX_BR = '╯';
    private static final char ACCENT_BAR = '▎';
    private static final char STRIPE_TOP = '▔';
    private static final char STRIPE_BOTTOM = '▁';
    private static final char SEPARATOR = '─';
    private static final String ICON_PENDING = "○";
    private static final String ICON_DONE = "✓";

    private final ShortcutBar shortcutBar = new ShortcutBar(List.of(
            new ShortcutBar.Shortcut("↑/k", "subir"),
            new ShortcutBar.Shortcut("↓/j", "bajar"),
            new ShortcutBar.Shortcut("Enter", "acciones"),
            new ShortcutBar.Shortcut("Tab", "lista"),
            new ShortcutBar.Shortcut("a", "crear"),
            new ShortcutBar.Shortcut("e", "editar"),
            new ShortcutBar.Shortcut("n", "nueva lista"),
            new ShortcutBar.Shortcut("s", "ajustes"),
            new ShortcutBar.Shortcut("c", "completar"),
            new ShortcutBar.Shortcut("r", "reabrir"),
            new ShortcutBar.Shortcut("d", "eliminar"),
            new ShortcutBar.Shortcut("p", "purgar"),
            new ShortcutBar.Shortcut("x", "eliminar listas vacías"),
            new ShortcutBar.Shortcut("q/Esc", "salir")));

    @Override
    public TerminalSize getPreferredSize(TaskViewComponent component) {
        return TerminalSize.ONE;
    }

    @Override
    public void drawComponent(TextGUIGraphics g, TaskViewComponent component) {
        TerminalSize size = g.getSize();
        int cols = size.getColumns();
        int rows = size.getRows();

        g.setBackgroundColor(VisualStyle.BACKGROUND);
        g.setForegroundColor(VisualStyle.FOREGROUND);
        g.fill(' ');

        if (rows <= STATUS_BAR_HEIGHT) {
            return;
        }
        int separatorRow = rows - STATUS_BAR_HEIGHT - 1;
        int statusBarTop = separatorRow + 1;

        List<String> logo = component.zoom() >= 0 ? AppLogo.fit(cols) : List.of();
        int logoHeight = logo.isEmpty() ? 0 : logo.size() + 1;

        if (logoHeight > 0) {
            drawLogo(g, logo, cols);
        }

        int listTop = logoHeight;
        int listHeight = separatorRow - listTop;
        if (listHeight >= 2) {
            drawList(g, component, cols, listTop, listHeight);
        }
        drawSeparator(g, cols, separatorRow);
        drawStatusBar(g, component, cols, statusBarTop);
    }

    private void drawLogo(TextGUIGraphics g, List<String> logo, int cols) {
        g.setForegroundColor(VisualStyle.ACCENT);
        for (int i = 0; i < logo.size(); i++) {
            String line = logo.get(i);
            int x = Math.max(0, (cols - line.length()) / 2);
            g.putString(x, i, line);
        }
    }

    private void drawList(TextGUIGraphics g, TaskViewComponent component, int cols, int top, int height) {
        if (height < 2 || cols < 2) {
            return;
        }
        drawBox(g, cols, top, height);

        String indicator = component.listIndicator();
        if (!indicator.isEmpty() && cols >= indicator.length() + 4) {
            int parenStart = indicator.indexOf('(');
            int parenEnd = indicator.indexOf(')');

            if (parenStart >= 0 && parenEnd > parenStart) {
                g.setForegroundColor(VisualStyle.ACCENT);
                g.enableModifiers(SGR.BOLD);
                g.putString(2, top, " " + indicator.substring(0, parenStart));
                
                g.setForegroundColor(VisualStyle.DIM);
                g.clearModifiers();
                g.putString(2 + parenStart, top, indicator.substring(parenStart, parenEnd + 1));
                
                g.setForegroundColor(VisualStyle.ACCENT);
                g.enableModifiers(SGR.BOLD);
                g.putString(2 + parenEnd + 1, top, indicator.substring(parenEnd + 1) + " ");
                g.clearModifiers();
            } else {
                g.setForegroundColor(VisualStyle.ACCENT);
                g.enableModifiers(SGR.BOLD);
                g.putString(2, top, " " + indicator + " ");
                g.clearModifiers();
            }
        }

        List<Task> tasks = component.tasks();
        if (tasks.isEmpty()) {
            g.setForegroundColor(VisualStyle.DIM);
            int msgX = Math.max(1, (cols - NO_TASKS.length()) / 2);
            g.putString(msgX, top + 1, NO_TASKS);
            return;
        }
        int contentRows = height - 2;
        int rowHeight = 1 + Math.max(0, component.zoom());
        int row = 1;
        for (int i = 0; i < tasks.size() && row < contentRows; i++) {
            drawTaskRow(g, tasks.get(i), i == component.selected(), cols, top, row);
            row += rowHeight;
        }
    }

    private void drawBox(TextGUIGraphics g, int cols, int top, int height) {
        g.setForegroundColor(VisualStyle.ACCENT);
        g.putString(0, top, String.valueOf(BOX_TL));
        g.putString(cols - 1, top, String.valueOf(BOX_TR));
        g.putString(0, top + height - 1, String.valueOf(BOX_BL));
        g.putString(cols - 1, top + height - 1, String.valueOf(BOX_BR));
        hline(g, 1, top, cols - 2, BOX_H);
        hline(g, 1, top + height - 1, cols - 2, BOX_H);
        for (int r = 1; r < height - 1; r++) {
            g.putString(0, top + r, String.valueOf(BOX_V));
            g.putString(cols - 1, top + r, String.valueOf(BOX_V));
        }
    }

    private void drawTaskRow(TextGUIGraphics g, Task task, boolean selected, int cols, int top, int row) {
        int y = top + row;
        if (selected) {
            g.setForegroundColor(VisualStyle.ACCENT);
            g.putString(1, y, String.valueOf(ACCENT_BAR));
        }
        if (task.isCompleted()) {
            g.setForegroundColor(VisualStyle.DONE);
            g.putString(3, y, ICON_DONE);
            g.setForegroundColor(VisualStyle.DIM);
            g.enableModifiers(SGR.CROSSED_OUT);
        } else {
            g.setForegroundColor(VisualStyle.FOREGROUND);
            g.putString(3, y, ICON_PENDING);
        }
        if (selected) {
            g.enableModifiers(SGR.BOLD);
        }
        int titleWidth = Math.max(0, cols - 5 - 2);
        String title = truncateEnd(task.getTitle(), titleWidth);
        g.putString(5, y, title);
        g.clearModifiers();
        String due = task.getDue() != null ? task.getDue().toString() : null;
        if (due != null) {
            int x = 5 + title.length();
            if (x + 1 < cols - 1) {
                g.setForegroundColor(VisualStyle.DIM);
                g.putString(x, y, truncateEnd("  " + due, Math.max(0, cols - x - 1)));
            }
        }
        g.clearModifiers();
    }

    private void drawSeparator(TextGUIGraphics g, int cols, int row) {
        g.setForegroundColor(VisualStyle.DIM);
        hline(g, 0, row, cols, SEPARATOR);
    }

    private void drawStatusBar(TextGUIGraphics g, TaskViewComponent component, int cols, int top) {
        g.setForegroundColor(VisualStyle.ACCENT);
        hline(g, 0, top, cols, STRIPE_TOP);

        drawTitleRow(g, component, cols, top + 1);
        drawBanner(g, component, cols, top + 2);
        drawShortcuts(g, cols, top + 3);

        g.setForegroundColor(VisualStyle.ACCENT);
        hline(g, 0, top + 4, cols, STRIPE_BOTTOM);
    }

    private void drawTitleRow(TextGUIGraphics g, TaskViewComponent component, int cols, int row) {
        String counter = counterText(component);
        g.setForegroundColor(VisualStyle.ACCENT);
        g.putString(0, row, String.valueOf(ACCENT_BAR));
        int titleBudget = cols - 2 - counter.length();
        if (titleBudget > 0) {
            g.setForegroundColor(VisualStyle.FOREGROUND);
            g.enableModifiers(SGR.BOLD);
            g.putString(1, row, CenterTruncator.truncate(APP_TITLE, titleBudget));
            g.clearModifiers();
        }
        g.setForegroundColor(VisualStyle.DIM);
        if (counter.length() <= cols) {
            g.putString(cols - counter.length(), row, counter);
        }
    }

    private void drawBanner(TextGUIGraphics g, TaskViewComponent component, int cols, int row) {
        String message = component.message();
        if (message.isEmpty()) {
            return;
        }
        if (component.kind() == MessageKind.WARN) {
            g.setForegroundColor(VisualStyle.WARN);
        } else {
            g.setForegroundColor(VisualStyle.DIM);
        }
        g.putString(0, row, truncateEnd(message, cols));
    }

    private void drawShortcuts(TextGUIGraphics g, int cols, int row) {
        List<ShortcutBar.Shortcut> visible = shortcutBar.visible(cols);
        int x = 0;
        for (ShortcutBar.Shortcut s : visible) {
            g.setForegroundColor(VisualStyle.FOREGROUND);
            g.enableModifiers(SGR.BOLD);
            g.putString(x, row, s.key());
            g.clearModifiers();
            x += s.key().length();
            if (!s.label().isEmpty()) {
                g.setForegroundColor(VisualStyle.DIM);
                g.putString(x, row, " " + s.label());
                x += 1 + s.label().length();
            }
            x += 2;
        }
    }

    private String counterText(TaskViewComponent component) {
        long pending = component.tasks().stream().filter(t -> !t.isCompleted()).count();
        long completed = component.tasks().stream().filter(Task::isCompleted).count();
        return pending + " pendientes · " + completed + " completadas";
    }

    private static void hline(TextGUIGraphics g, int x, int y, int length, char c) {
        if (length <= 0) {
            return;
        }
        g.putString(x, y, String.valueOf(c).repeat(length));
    }

    private static String truncateEnd(String text, int maxWidth) {
        if (text.length() <= maxWidth) {
            return text;
        }
        if (maxWidth <= 1) {
            return text.substring(0, Math.max(0, maxWidth));
        }
        return text.substring(0, maxWidth - 1) + "…";
    }
}
