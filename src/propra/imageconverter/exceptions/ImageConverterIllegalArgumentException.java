package propra.imageconverter.exceptions;

/**
 * Exception thrown, when arguments passed to the ImageConverter program are not valid.
 */
public class ImageConverterIllegalArgumentException extends Exception {
    /**
     * Constructs an ImageConverterIllegalArgumentException with the specified detail message.
     *
     * @param message the detail message.
     */
    public ImageConverterIllegalArgumentException(String message) {
        super(message);
    }
}
