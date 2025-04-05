package top.srcandy.terminal_air.enums;

public enum ExpireTime {
    ONE_HOUR(3600), ONE_DAY(86400), ONE_WEEK(604800), ONE_MONTH(2592000), ONE_YEAR(31536000);

    private final int time;

    ExpireTime(int time) {
        this.time = time;
    }

    public int getTime() {
        return time;
    }
}
