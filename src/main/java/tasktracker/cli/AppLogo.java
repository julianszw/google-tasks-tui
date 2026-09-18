package tasktracker.cli;

import java.util.List;

final class AppLogo {

    private static final int MIN_WIDTH = 50;

    private static final List<String> LINES = List.of(
            "████████╗ █████╗ ███████╗██╗  ██╗███╗   ███╗ █████╗ ███████╗████████╗███████╗██████╗ ",
            "╚══██╔══╝██╔══██╗██╔════╝██║ ██╔╝████╗ ████║██╔══██╗██╔════╝╚══██╔══╝██╔════╝██╔══██╗",
            "   ██║   ███████║███████╗█████═╝ ██╔████╔██║███████║███████╗   ██║   █████╗  ██████╔╝",
            "   ██║   ██╔══██║╚════██║██╔═██╗ ██║╚██╔╝██║██╔══██║╚════██║   ██║   ██╔══╝  ██╔══██╗",
            "   ██║   ██║  ██║███████║██║ ╚██╗██║ ╚═╝ ██║██║  ██║███████║   ██║   ███████╗██║  ██║",
            "   ╚═╝   ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝╚═╝     ╚═╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝╚═╝  ╚═╝"
    );

    private AppLogo() {
    }

    static List<String> lines() {
        return LINES;
    }

    static int minWidth() {
        return MIN_WIDTH;
    }

    static int height() {
        return LINES.size();
    }

    static List<String> fit(int width) {
        if (width < MIN_WIDTH) {
            return List.of();
        }
        return LINES.stream()
                .map(line -> truncate(line, width))
                .toList();
    }

    private static String truncate(String line, int width) {
        if (line.length() <= width) {
            return line;
        }
        return line.substring(0, width);
    }
}
