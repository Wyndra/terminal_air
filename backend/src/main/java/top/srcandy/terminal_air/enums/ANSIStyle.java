package top.srcandy.terminal_air.enums;

public enum ANSIStyle {

    BOLD("\u001b[1m"),
    UNDERLINE("\u001b[4m"),
    BLINK("\u001b[5m"),
    REVERSE("\u001b[7m"),
    RESET("\u001b[0m");  // 用于重置样式

    private final String code;

    ANSIStyle(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
