package tech.nmhillusion.neon_di.exception;

/**
 * date: 2023-02-25
 * <p>
 * created-by: nmhillusion
 */

public class NeonException extends Exception {
    public NeonException(String message) {
        super(message);
    }

    public NeonException(Throwable cause) {
        super(cause);
    }
}
