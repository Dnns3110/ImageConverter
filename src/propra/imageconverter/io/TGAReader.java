package propra.imageconverter.io;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;
import propra.imageconverter.image.Compression;
import propra.imageconverter.image.ImageHeader;
import propra.imageconverter.image.PixelOrder;
import propra.imageconverter.image.TGAImageHeader;

import java.io.InputStream;

/**
 * Class to read a TGA image from specified image file.
 */
public class TGAReader extends ImageReader {

    /**
     * Creates a <code>TGAReader</code>
     * and saves its  argument, the input stream
     * <code>in</code>, for later use. An internal
     * buffer array is created and  stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     */
    public TGAReader(InputStream in) {
        super(in);
    }

    /**
     * Returns Pixel order of the image format.
     *
     * @return pixel order
     */
    @Override
    protected PixelOrder getPixelOrder() {
        return TGAImageHeader.PIXEL_ORDER;
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
        byte imageIDLength = header[0x00];
        byte imageType = header[0x02];
        short xOrigin = ByteHandler.byteArrayToShort(header, 0x08);
        short yOrigin = ByteHandler.byteArrayToShort(header, 0x0A);
        short imgWidth = ByteHandler.byteArrayToShort(header, 0x0C);
        short imgHeight = ByteHandler.byteArrayToShort(header, 0x0E);
        byte pixelDepth = header[0x10];
        byte imgDescriptor = header[0x11];
        Compression compression = ((imageType & 0x8) == 0) ? Compression.Uncompressed : Compression.RLE;

        return new TGAImageHeader(imageIDLength, imageType, xOrigin, yOrigin, imgWidth, imgHeight, pixelDepth, imgDescriptor, compression);
    }

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    @Override
    public int getHeaderSize() {
        return TGAImageHeader.HEADER_SIZE;
    }
}
