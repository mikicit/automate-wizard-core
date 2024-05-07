package dev.mikita.automatewizard.exception;

public class NotFoundException extends BaseException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public static NotFoundException create(String resourceName, Object identifier) {
        return new NotFoundException(resourceName + " identified by " + identifier + " not found.");
    }
}
