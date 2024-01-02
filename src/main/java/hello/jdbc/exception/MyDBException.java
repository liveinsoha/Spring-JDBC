package hello.jdbc.exception;

public class MyDBException extends RuntimeException {

    public MyDBException() {
    }

    public MyDBException(String message) {
        super(message);
    }

    public MyDBException(String message, Throwable cause) {
        super(message, cause);
    }

    public MyDBException(Throwable cause) {
        super(cause);
    }

    public MyDBException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
