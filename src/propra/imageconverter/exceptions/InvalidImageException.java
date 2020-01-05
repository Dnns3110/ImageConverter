package propra.imageconverter.exceptions;

/**
 * Exception thrown, when image, loaded from file is invalid.
 */
public class InvalidImageException extends Exception {

    /**
     * Constructs an InvalidImageException with the specified detail message.
     *
     * @param message the detail message.
     */
    public InvalidImageException(String message) {
        super(message);
    }
}
