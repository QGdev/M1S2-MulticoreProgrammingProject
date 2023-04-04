package fr.univnantes.pmc.project.api;

/**
 * Exception thrown when a transaction is aborted
 *
 * @author Quentin GOMES DOS REIS
 * @author Matthéo LÉCRIVAIN
 */
public class AbortException extends Exception {

    /**
     * Constructs an AbortException with no detail message.
     */
    public AbortException() {
    }

    /**
     * Constructs an AbortException with the specified detail message.
     *
     * @param message the detail message.
     */
    public AbortException(String message) {
        super(message);
    }

    /**
     * Constructs an AbortException with the specified detail message and cause.
     *
     * @param message the detail message.
     * @param cause   the exception cause.
     */
    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an AbortException with the specified cause.
     *
     * @param cause exception the cause.
     */
    public AbortException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an AbortException with the specified detail message, cause, suppression enabled or disabled, and
     * writable stack trace enabled or disabled.
     *
     * @param message            the detail message.
     * @param cause              the exception cause.
     * @param enableSuppression  whether or not suppression is enabled or disabled
     * @param writableStackTrace whether or not the stack trace should be writable
     */
    public AbortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
