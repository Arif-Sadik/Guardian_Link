package util;

/**
 * Manages application theme (Dark/Light mode).
 * Provides consistent color tokens based on current theme.
 */
public class ThemeManager {

    private static boolean darkMode = false;

    // Light mode colors
    private static final String LIGHT_BG = "#f8f9fa";
    private static final String LIGHT_CARD = "#ffffff";
    private static final String LIGHT_BORDER = "#e2e8f0";
    private static final String LIGHT_MUTED = "#f1f5f9";
    private static final String LIGHT_MUTED_FG = "#64748b";
    private static final String LIGHT_TEXT = "#1a1a1a";

    // Dark mode colors
    private static final String DARK_BG = "#1a1a2e";
    private static final String DARK_CARD = "#16213e";
    private static final String DARK_BORDER = "#0f3460";
    private static final String DARK_MUTED = "#0f3460";
    private static final String DARK_MUTED_FG = "#a0aec0";
    private static final String DARK_TEXT = "#e2e8f0";

    // Constant colors (same in both themes)
    public static final String PRIMARY = "#2563eb";
    public static final String PRIMARY_FG = "#ffffff";
    public static final String SECONDARY = "#16a34a";
    public static final String WARNING = "#f59e0b";
    public static final String DESTRUCTIVE = "#dc2626";
    public static final String INFO = "#0ea5e9";

    public static boolean isDarkMode() {
        return darkMode;
    }

    public static void setDarkMode(boolean dark) {
        darkMode = dark;
    }

    public static void toggleTheme() {
        darkMode = !darkMode;
    }

    public static String getBg() {
        return darkMode ? DARK_BG : LIGHT_BG;
    }

    public static String getCard() {
        return darkMode ? DARK_CARD : LIGHT_CARD;
    }

    public static String getBorder() {
        return darkMode ? DARK_BORDER : LIGHT_BORDER;
    }

    public static String getMuted() {
        return darkMode ? DARK_MUTED : LIGHT_MUTED;
    }

    public static String getMutedFg() {
        return darkMode ? DARK_MUTED_FG : LIGHT_MUTED_FG;
    }

    public static String getText() {
        return darkMode ? DARK_TEXT : LIGHT_TEXT;
    }

    public static String getThemeName() {
        return darkMode ? "Dark" : "Light";
    }
}
