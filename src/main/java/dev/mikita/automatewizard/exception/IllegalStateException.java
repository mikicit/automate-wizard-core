package dev.mikita.automatewizard.exception;

public class IllegalStateException extends BaseException {
    public IllegalStateException(String message) {
        super(message);
    }

    public IllegalStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
