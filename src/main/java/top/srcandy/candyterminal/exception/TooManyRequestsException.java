package top.srcandy.candyterminal.exception;

public class TooManyRequestsException extends RuntimeException {

    private int statusCode = 429; // HTTP 429 Too Many Requests
    private String user; // 操作用户

    public TooManyRequestsException() {
        super("Too many requests. Please try again later.");
    }

    // 带消息构造函数
    public TooManyRequestsException(String message) {
        super(message);
    }

    public TooManyRequestsException(String message, String user) {
        super(message);
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
