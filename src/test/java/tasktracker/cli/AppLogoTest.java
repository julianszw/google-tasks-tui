package tasktracker.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AppLogoTest {

    @Test
    void linesAreNonEmptyAndUniformWidth() {
        List<String> lines = AppLogo.lines();

        assertEquals(6, lines.size());
        int width = lines.get(0).length();
        assertTrue(width > 0);
        for (String line : lines) {
            assertEquals(width, line.length());
            assertTrue(!line.isBlank());
        }
    }

    @Test
    void fitReturnsAllLinesWhenWideEnough() {
        int fullWidth = AppLogo.lines().get(0).length();

        List<String> fit = AppLogo.fit(fullWidth);

        assertEquals(AppLogo.lines(), fit);
    }

    @Test
    void fitTruncatesLinesToWidth() {
        int width = AppLogo.minWidth();

        List<String> fit = AppLogo.fit(width);

        assertEquals(6, fit.size());
        for (String line : fit) {
            assertEquals(width, line.length());
        }
    }

    @Test
    void fitOmitsLogoWhenTooNarrow() {
        assertTrue(AppLogo.fit(AppLogo.minWidth() - 1).isEmpty());
    }

    @Test
    void minWidthIsPositive() {
        assertTrue(AppLogo.minWidth() > 0);
    }
}
