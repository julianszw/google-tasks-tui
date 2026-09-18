package tasktracker.cli;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.ComponentRenderer;
import com.googlecode.lanterna.gui2.TextGUIGraphics;
import java.util.List;
import tasktracker.model.Task;

final class TaskViewRenderer implements ComponentRenderer<TaskViewComponent> {

    private static final String APP_TITLE = "TASKMASTER";
    private static final String NO_TASKS = "No hay tareas cargadas";

    private static final int STATUS_BAR_HEIGHT = 4;
    private static final int MIN_WIDE_WIDTH = 80;

    private static final char BOX_H = '─';
    private static final char BOX_V = '│';
    private static final char BOX_TL = '┌';
    private static final char BOX_TR = '┐';
    private static final char BOX_BL = '└';
    private static final char BOX_BR = '┘';
    private static final char BOX_ML = '├';
    private static final char BOX_MR = '┤';
    private static final char ACCENT_BAR = '▌';
    private static final char SEPARATOR = '─';
    private static final String ICON_PENDING = "○";
    private static final String ICON_DONE = "✓";
    private static final String PROGRESS_FULL = "█";
    private static final String PROGRESS_EMPTY = "░";
    private static final char POWERLINE_SEP = ' '; // space separator for powerline

    private final ShortcutBar shortcutBar = new ShortcutBar(List.of(
            new ShortcutBar.Shortcut("↑/k", "navegar"),
            new ShortcutBar.Shortcut("Enter", "detalles"),
            new ShortcutBar.Shortcut("Tab", "panel"),
            new ShortcutBar.Shortcut("a", "nueva"),
            new ShortcutBar.Shortcut("e", "editar"),
            new ShortcutBar.Shortcut("s", "ordenar"),
            new ShortcutBar.Shortcut("d", "hecha"),
            new ShortcutBar.Shortcut("p", "prioridad"),
            new ShortcutBar.Shortcut("x", "eliminar"),
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

        int headerHeight = headerHeight(component, cols);
        if (rows <= headerHeight + STATUS_BAR_HEIGHT) {
            drawTabs(g, component, cols);
            return;
        }

        int contentTop = headerHeight;
        int contentHeight = rows - headerHeight - STATUS_BAR_HEIGHT;

        drawTabs(g, component, cols);
        drawContent(g, component, cols, contentTop, contentHeight);
        drawStatusBar(g, component, cols, rows - STATUS_BAR_HEIGHT);
    }

    private static int headerHeight(TaskViewComponent component, int cols) {
        List<String> logo = component.zoom() >= 0 ? AppLogo.fit(cols) : List.of();
        return logo.size() + 1; // logo rows + tabs row
    }

    // ── Tabs / List Switcher ──
    private void drawTabs(TextGUIGraphics g, TaskViewComponent component, int cols) {
        int y = 0;
        // First row: logo + tabs
        List<String> logo = component.zoom() >= 0 ? AppLogo.fit(cols) : List.of();
        int logoHeight = logo.isEmpty() ? 0 : logo.size();
        if (logoHeight > 0) {
            drawLogo(g, logo, cols, y);
            y += logoHeight;
        }

        // Second row: list tabs with progress
        drawListTabs(g, component, cols, y);
    }

    private void drawLogo(TextGUIGraphics g, List<String> logo, int cols, int startRow) {
        g.setForegroundColor(VisualStyle.PRIMARY);
        g.enableModifiers(SGR.BOLD);
        for (int i = 0; i < logo.size(); i++) {
            String line = logo.get(i);
            int x = Math.max(0, (cols - line.length()) / 2);
            g.putString(x, startRow + i, line);
        }
        g.clearModifiers();
    }

    private void drawListTabs(TextGUIGraphics g, TaskViewComponent component, int cols, int row) {
        String indicator = component.listIndicator();
        if (indicator.isEmpty()) {
            return;
        }

        // Parse indicator: "ListName (Provider) · 1/3"
        int parenStart = indicator.indexOf('(');
        int parenEnd = indicator.indexOf(')');
        int sepPos = indicator.indexOf('·');

        String listName = parenStart >= 0 ? indicator.substring(0, parenStart).trim() : indicator;
        String provider = (parenStart >= 0 && parenEnd > parenStart)
                ? indicator.substring(parenStart + 1, parenEnd).trim()
                : "";
        String position = sepPos >= 0 ? indicator.substring(sepPos).trim() : "";

        // Draw tab style: [1] ListName (Provider) · position
        StringBuilder tab = new StringBuilder();
        tab.append("[1] ");
        tab.append(listName);
        if (!provider.isEmpty()) {
            tab.append(" (").append(provider).append(")");
        }
        if (!position.isEmpty()) {
            tab.append(" ").append(position);
        }

        String tabStr = tab.toString();
        if (tabStr.length() <= cols) {
            int x = 2;
            g.setForegroundColor(VisualStyle.PRIMARY);
            g.enableModifiers(SGR.BOLD);
            g.putString(x, row, "[1]");
            x += 3;
            g.putString(x, row, " " + listName);
            x += 1 + listName.length();
            g.clearModifiers();
            if (!provider.isEmpty()) {
                g.setForegroundColor(VisualStyle.DIM);
                g.putString(x, row, " (" + provider + ")");
                x += 2 + provider.length() + 1;
            }
            if (!position.isEmpty()) {
                g.setForegroundColor(VisualStyle.DIM);
                g.putString(x, row, " " + position);
            }
        }
    }

    // ── Main Content Area ──
    private void drawContent(TextGUIGraphics g, TaskViewComponent component, int cols, int top, int height) {
        if (height < 2 || cols < 2) {
            return;
        }

        List<Task> tasks = component.tasks();
        long pending = tasks.stream().filter(t -> !t.isCompleted()).count();
        long completed = tasks.stream().filter(Task::isCompleted).count();
        long total = pending + completed;

        if (cols >= MIN_WIDE_WIDTH && height >= 10) {
            drawWideLayout(g, component, tasks, cols, top, height, pending, completed, total);
        } else {
            drawCompactLayout(g, component, tasks, cols, top, height, pending, completed, total);
        }
    }

    // ── Wide Layout (80+ cols): Two panels ──
    private void drawWideLayout(TextGUIGraphics g, TaskViewComponent component, List<Task> tasks,
            int cols, int top, int height, long pending, long completed, long total) {
        int leftCols = (int) (cols * 0.65);
        int rightCols = cols - leftCols - 1;
        int rightX = leftCols + 1;

        // Left panel: task list
        drawTaskListPanel(g, component, tasks, leftCols, top, height, pending, completed, total);

        // Vertical divider
        drawVerticalDivider(g, top, height, leftCols);

        // Right panel: detail inspector
        if (tasks.isEmpty()) {
            // Empty state in right panel
        } else {
            drawDetailPanel(g, tasks.get(component.selected()), rightCols, top + 1, height - 2, rightX);
        }
    }

    // ── Compact Layout: Single panel ──
    private void drawCompactLayout(TextGUIGraphics g, TaskViewComponent component, List<Task> tasks,
            int cols, int top, int height, long pending, long completed, long total) {
        drawTaskListPanel(g, component, tasks, cols, top, height, pending, completed, total);
    }

    private void drawTaskListPanel(TextGUIGraphics g, TaskViewComponent component, List<Task> tasks,
            int cols, int top, int height, long pending, long completed, long total) {
        // Panel header
        int y = top;
        drawPanelHeader(g, cols, y, "TAREAS", pending, completed, total);
        y += 1;

        // Progress bar
        if (total > 0) {
            drawProgressBar(g, cols, y, pending, completed, total);
            y += 1;
        }

        // Task list
        int listHeight = height - (y - top) - 1;
        if (listHeight < 1) {
            return;
        }

        if (tasks.isEmpty()) {
            g.setForegroundColor(VisualStyle.DIM);
            int msgX = Math.max(1, (cols - NO_TASKS.length()) / 2);
            g.putString(msgX, y, NO_TASKS);
            return;
        }

        int selected = component.selected();
        for (int i = 0; i < tasks.size() && i < listHeight; i++) {
            drawTaskRow(g, tasks.get(i), i == selected, cols, y + i);
        }
    }

    private void drawPanelHeader(TextGUIGraphics g, int cols, int row, String title,
            long pending, long completed, long total) {
        g.setForegroundColor(VisualStyle.PRIMARY);
        g.enableModifiers(SGR.BOLD);
        g.putString(1, row, "┌─");
        g.putString(3, row, title);
        g.clearModifiers();

        String count = pending + "/" + total + " completadas";
        int countX = Math.max(4 + title.length(), cols - count.length() - 2);
        g.setForegroundColor(VisualStyle.WARN);
        g.putString(countX, row, count);

        g.setForegroundColor(VisualStyle.PRIMARY);
        g.putString(cols - 1, row, "┐");
    }

    private void drawProgressBar(TextGUIGraphics g, int cols, int row, long pending, long completed, long total) {
        int barWidth = Math.min(cols - 12, 30);
        int filled = (int) (completed * barWidth / total);
        int empty = barWidth - filled;

        StringBuilder bar = new StringBuilder("  [");
        for (int i = 0; i < filled; i++) {
            bar.append(PROGRESS_FULL);
        }
        for (int i = 0; i < empty; i++) {
            bar.append(PROGRESS_EMPTY);
        }
        bar.append("] ");

        g.setForegroundColor(VisualStyle.PRIMARY);
        g.putString(1, row, "  [");
        g.setForegroundColor(VisualStyle.DONE);
        g.putString(4, row, PROGRESS_FULL.repeat(filled));
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(4 + filled, row, PROGRESS_EMPTY.repeat(empty));
        g.setForegroundColor(VisualStyle.PRIMARY);
        g.putString(4 + barWidth, row, "]");

        String pct = total > 0 ? (completed * 100 / total) + "%" : "0%";
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(6 + barWidth, row, pct);
    }

    private void drawTaskRow(TextGUIGraphics g, Task task, boolean selected, int cols, int row) {
        if (selected) {
            g.setForegroundColor(VisualStyle.PRIMARY);
            g.putString(1, row, String.valueOf(ACCENT_BAR));
        }

        // Checkbox
        int x = 3;
        if (task.isCompleted()) {
            g.setForegroundColor(VisualStyle.DONE);
            g.putString(x, row, ICON_DONE);
            g.enableModifiers(SGR.CROSSED_OUT);
            g.setForegroundColor(VisualStyle.DIM);
        } else {
            g.setForegroundColor(VisualStyle.DIM);
            g.putString(x, row, ICON_PENDING);
        }

        // Title
        if (selected) {
            g.enableModifiers(SGR.BOLD);
            g.setForegroundColor(VisualStyle.FOREGROUND);
        }
        int titleMax = cols - 14;
        String title = truncateEnd(task.getTitle(), Math.max(0, titleMax));
        g.putString(5, row, title);
        g.clearModifiers();

        // Due date (right-aligned)
        String due = task.getDue() != null ? task.getDue().toString() : null;
        if (due != null) {
            g.setForegroundColor(VisualStyle.WARN);
            int dueX = cols - due.length() - 2;
            if (dueX > 5 + title.length()) {
                g.putString(dueX, row, due);
            }
        }
    }

    // ── Vertical Divider for Wide Layout ──
    private void drawVerticalDivider(TextGUIGraphics g, int top, int height, int col) {
        g.setForegroundColor(VisualStyle.DIM);
        for (int i = 0; i < height; i++) {
            g.putString(col, top + i, "│");
        }
    }

    // ── Detail Panel (Right Side) ──
    private void drawDetailPanel(TextGUIGraphics g, Task task, int cols, int top, int height, int startX) {
        if (cols < 10 || height < 5) {
            return;
        }

        int y = top;
        int innerCols = cols - 2;

        // Panel header
        g.setForegroundColor(VisualStyle.SECONDARY);
        g.enableModifiers(SGR.BOLD);
        g.putString(startX, y, "┌─");
        g.putString(startX + 2, y, "INSPECCIÓN");
        g.clearModifiers();

        // Task ID badge
        String id = "#tsk";
        g.setForegroundColor(VisualStyle.SECONDARY);
        g.putString(startX + cols - id.length() - 3, y, id);
        g.putString(startX + cols - 1, y, "┐");
        y += 2;

        // Title
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(startX + 1, y, "TÍTULO");
        y += 1;
        g.setForegroundColor(VisualStyle.FOREGROUND);
        g.enableModifiers(SGR.BOLD);
        g.putString(startX + 1, y, truncateEnd(task.getTitle(), innerCols));
        g.clearModifiers();
        y += 2;

        // Meta info
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(startX + 1, y, "FECHA LÍMITE");
        y += 1;
        String due = task.getDue() != null ? task.getDue().toString() : "Sin fecha";
        g.setForegroundColor(task.getDue() != null ? VisualStyle.WARN : VisualStyle.DIM);
        g.putString(startX + 1, y, due);
        y += 2;

        // Status
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(startX + 1, y, "ESTADO");
        y += 1;
        if (task.isCompleted()) {
            g.setForegroundColor(VisualStyle.DONE);
            g.putString(startX + 1, y, "✓ COMPLETADA");
        } else {
            g.setForegroundColor(VisualStyle.PRIMARY);
            g.putString(startX + 1, y, "○ PENDIENTE");
        }
        y += 2;

        // Quick actions
        g.setForegroundColor(VisualStyle.DIM);
        g.putString(startX + 1, y, "ACCIONES");
        y += 1;
        g.putString(startX + 1, y, "[e] Editar  [d] Hecha");
    }

    // ── Powerline Status Bar ──
    private void drawStatusBar(TextGUIGraphics g, TaskViewComponent component, int cols, int top) {
        drawPowerlineBar(g, component, cols, top);
        drawShortcuts(g, cols, top + 1);
        drawMessage(g, component, cols, top + 2);
    }

    private void drawPowerlineBar(TextGUIGraphics g, TaskViewComponent component, int cols, int row) {
        // Segment 1: App name (inverted)
        String appName = APP_TITLE;
        g.setBackgroundColor(VisualStyle.PRIMARY);
        g.setForegroundColor(VisualStyle.BACKGROUND);
        g.enableModifiers(SGR.BOLD);
        g.putString(0, row, " " + appName + " ");
        g.clearModifiers();

        int x = appName.length() + 2;
        g.setBackgroundColor(VisualStyle.BACKGROUND);
        g.setForegroundColor(VisualStyle.FOREGROUND);

        // Segment 2: Status mode
        String mode = "NORMAL";
        g.setBackgroundColor(VisualStyle.DIM);
        g.setForegroundColor(VisualStyle.FOREGROUND);
        g.enableModifiers(SGR.BOLD);
        g.putString(x, row, " " + mode + " ");
        g.clearModifiers();
        g.setBackgroundColor(VisualStyle.BACKGROUND);
        x += mode.length() + 2;

        // Segment 3: Active list
        String indicator = component.listIndicator();
        int parenStart = indicator.indexOf('(');
        String listName = parenStart >= 0 ? indicator.substring(0, parenStart).trim() : indicator;
        g.setForegroundColor(VisualStyle.PRIMARY);
        g.putString(x, row, " ≡ " + listName);
        x += 3 + listName.length();

        // Segment 4: Counters
        long pending = component.tasks().stream().filter(t -> !t.isCompleted()).count();
        long completed = component.tasks().stream().filter(Task::isCompleted).count();
        g.setForegroundColor(VisualStyle.WARN);
        g.putString(x, row, " " + pending + " pendientes");
        x += 1 + String.valueOf(pending).length() + 10;

        g.setForegroundColor(VisualStyle.DIM);
        g.putString(x, row, " · ");
        x += 3;

        g.setForegroundColor(VisualStyle.DONE);
        g.putString(x, row, completed + " completadas");
        x += String.valueOf(completed).length() + 11;

        // Segment 5: Sync info (right-aligned)
        String sync = "↻ Sync";
        int syncX = cols - sync.length();
        if (syncX > x) {
            g.setForegroundColor(VisualStyle.DIM);
            g.putString(syncX, row, sync);
        }
    }

    private void drawShortcuts(TextGUIGraphics g, int cols, int row) {
        List<ShortcutBar.Shortcut> visible = shortcutBar.visible(cols);
        int x = 1;
        for (ShortcutBar.Shortcut s : visible) {
            // Key badge
            g.setForegroundColor(VisualStyle.PRIMARY);
            g.enableModifiers(SGR.BOLD);
            g.putString(x, row, "[" + s.key() + "]");
            g.clearModifiers();
            x += s.key().length() + 2;
            // Label
            if (!s.label().isEmpty()) {
                g.setForegroundColor(VisualStyle.DIM);
                g.putString(x, row, " " + s.label());
                x += 1 + s.label().length();
            }
            x += 1;
        }
    }

    private void drawMessage(TextGUIGraphics g, TaskViewComponent component, int cols, int row) {
        String message = component.message();
        if (message.isEmpty()) {
            return;
        }
        if (component.kind() == MessageKind.WARN) {
            g.setForegroundColor(VisualStyle.WARN);
        } else {
            g.setForegroundColor(VisualStyle.DIM);
        }
        g.putString(1, row, truncateEnd(message, cols - 2));
    }

    // ── Utilities ──
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
