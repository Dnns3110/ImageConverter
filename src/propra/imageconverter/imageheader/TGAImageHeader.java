package propra.imageconverter.imageheader;

import propra.imageconverter.Compression;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;

import java.nio.ByteBuffer;

/**
 * Image header in TGA Format
 */
public class TGAImageHeader extends ImageHeader {

    public static final int HEADER_SIZE = 18;
    public static PixelOrder PIXEL_ORDER = PixelOrder.BGR;

    /**
     * Image type field from image file header.
     */
    private byte imageType;


    /**
     * X Origin field from image file header.
     */
    private short xOrigin;

    /**
     * Y Origin field from image file header.
     */
    private short yOrigin;

    /**
     * Image descriptor field from image file header.
     */
    private byte imgDescriptor;

    /**
     * Constructs a TGA image with xOrigin, yOrigin, width, height, pixelDepth, image descriptor and pixel data of image.
     *
     * @param imageIDLength image ID length field from image file header.
     * @param imageType     image type field from image file header
     * @param xOrigin       X Origin field from image file header.
     * @param yOrigin       Y Origin field from image file header.
     * @param imgWidth      width of image.
     * @param imgHeight     height of image.
     * @param pixelDepth    pixel Depth of image.
     * @param imgDescriptor image descriptor from image file header.
     * @param compression   compression.
     * @throws InvalidImageException if image header is invalid.
     */
    public TGAImageHeader(byte imageIDLength, byte imageType, short xOrigin, short yOrigin, short imgWidth, short imgHeight,
                          byte pixelDepth, byte imgDescriptor, Compression compression) throws InvalidImageException {
        super(imgWidth, imgHeight, pixelDepth, compression);

        this.imageType = imageType;
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.imgDescriptor = imgDescriptor;

        if (imageIDLength != 0) {
            throw new InvalidImageException(String.format("Unsupported image ID length used: Supported: 0, found: %d.", imageType));
        } else if (imageType != 2 && imageType != 10) {
            throw new InvalidImageException(String.format("Unsupported image type used: Supported: 2 or 10, found: %d.", imageType));
        } else if (imgDescriptor != 0x20 || xOrigin != 0 || yOrigin != imgHeight) {
            throw new InvalidImageException("Origin of image has to be at the top left corner.");
        }
    }

    /**
     * Returns X Origin field from image file header.
     *
     * @return xOrigin.
     */
    public short getxOrigin() {
        return xOrigin;
    }

    /**
     * Returns Y Origin field from image file header.
     *
     * @return yOrigin.
     */
    public short getyOrigin() {
        return yOrigin;
    }

    /**
     * Returns image descriptor field from image file header.
     *
     * @return imgDescriptor.
     */
    public byte getImgDescriptor() {
        return imgDescriptor;
    }

    /**
     * Returns header as byte array to be written into a file.
     *
     * @return this header as byte array.
     */
    @Override
    public byte[] toByteArray() {
        ByteBuffer buf = ByteBuffer.allocate(HEADER_SIZE);

        // First 8 Bytes of TGA Header are fixed values in our case.
        buf.put(new byte[]{0x0, 0x0, this.imageType, 0x0, 0x0, 0x0, 0x0, 0x0});
        buf.put(ByteHandler.shortToByteArray(this.getxOrigin()));
        buf.put(ByteHandler.shortToByteArray(this.getyOrigin()));
        buf.put(ByteHandler.shortToByteArray(this.getImgWidth()));
        buf.put(ByteHandler.shortToByteArray(this.getImgHeight()));
        buf.put(this.getPixelDepth());
        buf.put(this.getImgDescriptor());

        return buf.array();
    }

    /**
     * Returns pixel order for ProPra image file.
     *
     * @return PixelOrder.BGR.
     */
    @Override
    public PixelOrder getPixelOrder() {
        return PIXEL_ORDER;
    }
}
