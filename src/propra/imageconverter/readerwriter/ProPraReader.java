package propra.imageconverter.readerwriter;

import propra.imageconverter.Compression;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;
import propra.imageconverter.imageheader.ImageHeader;
import propra.imageconverter.imageheader.ProPraImageHeader;

import java.io.InputStream;

/**
 * Class to read a ProPra image from specified input file.
 */
public class ProPraReader extends ImageReader {

    /**
     * Creates a <code>ProPraReader</code>
     * and saves its  argument, the input stream
     * <code>in</code>, for later use. An internal
     * buffer array is created and  stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     */
    public ProPraReader(InputStream in) {
        super(in);
    }

    /**
     * Returns Pixel order of the image format.
     *
     * @return pixel order
     */
    @Override
    protected PixelOrder getPixelOrder() {
        return ProPraImageHeader.PIXEL_ORDER;
    }

    /**
     * Parses read bytes into the corresponding fields of the image header
     *
     * @param header read bytes from input file
     * @return created header from read bytes
     * @throws InvalidImageException if header is invalid
     */
    @Override
    protected ImageHeader parseHeader(byte[] header) throws InvalidImageException {
        String magic = new String(header, 0, 10);
        short imgWidth = ByteHandler.byteArrayToShort(header, 0x0A);
        short imgHeight = ByteHandler.byteArrayToShort(header, 0x0C);
        byte pixelDepth = header[0x0E];
        byte compressionType = header[0x0F];
        Compression compression = null;
        long dataSegmentSize = ByteHandler.byteArrayToLong(header, 0x10);
        int checksum = ByteHandler.byteArrayToInt(header, 0x18);

        if (compressionType == 0) {
            compression = Compression.Uncompressed;
        } else if (compressionType == 1) {
            compression = Compression.RLE;
        }

        return new ProPraImageHeader(magic, imgWidth, imgHeight, pixelDepth, compression, dataSegmentSize, checksum);
    }

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    @Override
    public int getHeaderSize() {
        return ProPraImageHeader.HEADER_SIZE;
    }

    /**
     * For ProPra format, after the image data there is no optional data allowed.
     *
     * @return false.
     */
    @Override
    public boolean allowOptionalData() {
        return false;
    }
}
