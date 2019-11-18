package propra.imageconverter.exceptions;

/**
 * Exception thrown, when there is an error with the Base-N encoding or decoding.
 */
public class InvalidEncodingException extends Exception {
    /**
     * Constructs an InvalidEncodingException with the specified detail message.
     *
     * @param message the detail message.
     */
    public InvalidEncodingException(String message) {
        super(message);
    }
}
