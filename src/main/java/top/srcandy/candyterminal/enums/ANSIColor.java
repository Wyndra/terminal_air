package top.srcandy.candyterminal.enums;

public enum ANSIColor {
    BLACK("\u001b[30m"),
    RED("\u001b[31m"),
    GREEN("\u001b[32m"),
    YELLOW("\u001b[33m"),
    BLUE("\u001b[34m"),
    MAGENTA("\u001b[35m"),
    CYAN("\u001b[36m"),
    WHITE("\u001b[37m"),
    RESET("\u001b[0m"),  // 用于重置颜色

    // 高亮颜色
    BRIGHT_BLACK("\u001b[90m"),
    BRIGHT_RED("\u001b[91m"),
    BRIGHT_GREEN("\u001b[92m"),
    BRIGHT_YELLOW("\u001b[93m"),
    BRIGHT_BLUE("\u001b[94m"),
    BRIGHT_MAGENTA("\u001b[95m"),
    BRIGHT_CYAN("\u001b[96m"),
    BRIGHT_WHITE("\u001b[97m"),

    // 其他扩展颜色
    LIGHT_GRAY("\u001b[37;1m"), // 浅灰色
    DARK_GRAY("\u001b[30;1m");  // 深灰色

    private final String code;

    ANSIColor(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
