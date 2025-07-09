package top.srcandy.terminal_air.exception;

public class ServiceException extends RuntimeException{
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;

    public ServiceException(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ServiceException(String message) {
        this.code = 500;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
