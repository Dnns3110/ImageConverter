package propra.imageconverter.exceptions;

/**
 * Exception thrown, when arguments passed to the ImageConverter program are not valid.
 */
public class IllegalArgumentException extends Exception {
    /**
     * Constructs an IllegalArgumentException with the specified detail message.
     *
     * @param message the detail message.
     */
    public IllegalArgumentException(String message) {
        super(message);
    }
}
