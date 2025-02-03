package top.srcandy.candyterminal.utils;

import top.srcandy.candyterminal.enums.ANSIColor;
import top.srcandy.candyterminal.enums.ANSIStyle;

public class ANSIFormatterUtil {

    private ANSIFormatterUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 格式化消息，支持颜色和样式
     * @param message 要格式化的消息
     * @param color 消息的颜色
     * @param style 消息的样式
     * @return 格式化后的消息
     */
    public static String formatMessage(String message, ANSIColor color, ANSIStyle style) {
        // ANSI 转义序列的基础部分
        StringBuilder formattedMessage = new StringBuilder();

        // 添加颜色
        if (color != null) {
            formattedMessage.append(color.getCode());
        }

        // 添加样式
        if (style != null) {
            formattedMessage.append(style.getCode());
        }

        // 添加消息内容
        formattedMessage.append(" ").append(message);

        // 结束格式化
        formattedMessage.append(ANSIStyle.RESET.getCode());  // 重置样式

        return formattedMessage.toString();
    }

}
