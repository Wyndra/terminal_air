package top.srcandy.candyterminal.exception;

public class TooManyRequestsException extends RuntimeException {

    private int statusCode;
    private String message;


    public TooManyRequestsException() {
        super("Too many requests. Please try again later.");
        this.statusCode = 429;  // HTTP 429 Too Many Requests
    }

    // 带消息构造函数
    public TooManyRequestsException(String message) {
        super(message);
        this.statusCode = 429;
    }

    // 获取状态码
    public int getStatusCode() {
        return statusCode;
    }

    // 设置状态码
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    // 获取自定义消息
    @Override
    public String getMessage() {
        return message != null ? message : super.getMessage();
    }

    // 设置自定义消息
    public void setMessage(String message) {
        this.message = message;
    }

}
