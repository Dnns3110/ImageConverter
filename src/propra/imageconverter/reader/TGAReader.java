package propra.imageconverter.reader;

import propra.imageconverter.Compression;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;
import propra.imageconverter.imageheader.ImageHeader;
import propra.imageconverter.imageheader.TGAImageHeader;

import java.io.InputStream;

public class TGAReader extends ImageReader {

    public TGAReader(InputStream in) {
        super(in);
    }

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

    @Override
    protected PixelOrder getPixelOrder() {
        return TGAImageHeader.PIXEL_ORDER;
    }
}
