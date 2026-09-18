package tasktracker.cli;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.graphics.Theme;

public final class VisualStyle {

    // ── Base surfaces (Stitch dark palette) ──
    public static final TextColor BACKGROUND = TextColor.ANSI.BLACK;
    public static final TextColor SURFACE = TextColor.ANSI.BLACK;
    public static final TextColor SURFACE_ALT = TextColor.ANSI.BLACK;

    // ── Primary accent: emerald green #10b981 ──
    public static final TextColor PRIMARY = TextColor.ANSI.GREEN_BRIGHT;

    // ── Secondary accent: electric cyan #38bdf8 ──
    public static final TextColor SECONDARY = TextColor.ANSI.CYAN;

    // ── Tertiary accent: magenta #a855f7 ──
    public static final TextColor TERTIARY = TextColor.ANSI.MAGENTA;

    // ── Text tokens ──
    public static final TextColor FOREGROUND = TextColor.ANSI.WHITE_BRIGHT;
    public static final TextColor DIM = TextColor.ANSI.WHITE;
    public static final TextColor MUTED = TextColor.ANSI.BLACK;
    public static final TextColor DISABLED = TextColor.ANSI.BLACK;

    // ── Semantic colors ──
    public static final TextColor DONE = TextColor.ANSI.GREEN_BRIGHT;
    public static final TextColor WARN = TextColor.ANSI.YELLOW;
    public static final TextColor ERROR = TextColor.ANSI.RED;
    public static final TextColor ACCENT = TextColor.ANSI.GREEN_BRIGHT;

    // ── Powerline status bar segments ──
    public static final TextColor STATUS_BG = TextColor.ANSI.BLACK;
    public static final TextColor STATUS_FG = TextColor.ANSI.WHITE_BRIGHT;

    private static final Theme THEME = buildTheme();

    private VisualStyle() {
    }

    public static Theme theme() {
        return THEME;
    }

    private static Theme buildTheme() {
        SimpleTheme theme = new SimpleTheme(FOREGROUND, BACKGROUND);
        theme.getDefaultDefinition().setSelected(PRIMARY, BACKGROUND);
        theme.getDefaultDefinition().setActive(PRIMARY, BACKGROUND);
        return theme;
    }
}
