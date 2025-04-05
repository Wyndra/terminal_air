package top.srcandy.candyterminal.exception;

public class ServiceException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private String code;
    private String message;

    public ServiceException(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message) {
        this.code = "500";
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
