package propra.imageconverter.images;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;

import java.nio.ByteBuffer;

/**
 * Image in TGA Format
 */
public class TGAImage extends Image {

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
     * @param xOrigin       X Origin field from image file header.
     * @param yOrigin       Y Origin field from image file header.
     * @param imgWidth      width of image.
     * @param imgHeight     height of image.
     * @param pixelDepth    pixel Depth of image.
     * @param imgDescriptor image descriptor from image file header.
     * @param imgData       pixel data of image.
     */
    public TGAImage(short xOrigin, short yOrigin, short imgWidth, short imgHeight, byte pixelDepth, byte imgDescriptor, byte[][][] imgData) {
        super(imgWidth, imgHeight, pixelDepth, imgData);

        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.imgDescriptor = imgDescriptor;
    }

    /**
     * Constructs a TGA image, that is loaded from a file.
     *
     * @param filePath path of image file to be loaded.
     * @throws InvalidImageException if loaded image is invalid.
     */
    public TGAImage(String filePath) throws InvalidImageException {
        super(filePath);
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
     * Sets X Origin field from image file header.
     *
     * @param xOrigin yOrigin to be set.
     */
    public void setxOrigin(short xOrigin) {
        this.xOrigin = xOrigin;
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
     * Sets X Origin field from image file header.
     *
     * @param yOrigin yOrigin to be set.
     */
    public void setyOrigin(short yOrigin) {
        this.yOrigin = yOrigin;
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
     * Sets image descriptor field from image file header.
     *
     * @param imgDescriptor imgDescriptor to be set.
     */
    public void setImgDescriptor(byte imgDescriptor) {
        this.imgDescriptor = imgDescriptor;
    }

    /**
     * Handle header of image.
     *
     * @param fileHeader file header from image file to be processed.
     */
    @Override
    protected void handleHeader(byte[] fileHeader) throws InvalidImageException {
        byte imageIDLength = fileHeader[0x00];
        byte imageType = fileHeader[0x02];
        this.setxOrigin(ByteHandler.byteArrayToShort(fileHeader, 0x08));
        this.setyOrigin(ByteHandler.byteArrayToShort(fileHeader, 0x0A));
        this.setImgWidth(ByteHandler.byteArrayToShort(fileHeader, 0x0C));
        this.setImgHeight(ByteHandler.byteArrayToShort(fileHeader, 0x0E));
        this.setPixelDepth(fileHeader[0x10]);
        this.setImgDescriptor(fileHeader[0x11]);

        // Validate Header.
        if (imageIDLength != 0) {
            throw new InvalidImageException("Unsupported image ID length used: Supported: 0, found: " + imageType);
        } else if (imageType != 2) {
            throw new InvalidImageException("Unsupported image type used: Supported: 2, found: " + imageType);
        } else if (this.getImgDescriptor() != 0x20 || this.getxOrigin() != 0 || this.getyOrigin() != this.getImgHeight()) {
            throw new InvalidImageException("Origin of image has to be at the top left corner.");
        }
    }

    /**
     * Returns header for specific image file.
     *
     * @return image file header.
     */
    @Override
    protected byte[] getHeader() {
        ByteBuffer buf = ByteBuffer.allocate(getHeaderSize());

        // First 8 Bytes of TGA Header are fixed values in our case.
        buf.put(new byte[]{0x0, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0});
        buf.put(ByteHandler.shortToByteArray(this.getxOrigin()));
        buf.put(ByteHandler.shortToByteArray(this.getyOrigin()));
        buf.put(ByteHandler.shortToByteArray(this.getImgWidth()));
        buf.put(ByteHandler.shortToByteArray(this.getImgHeight()));
        buf.put(this.getPixelDepth());
        buf.put(this.getImgDescriptor());

        return buf.array();
    }

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    @Override
    public int getHeaderSize() {
        return 18;
    }

    /**
     * Returns a converted Image.
     *
     * @return converted image.
     */
    @Override
    public Image convert() {
        byte compressionType = 0x0;
        swapPixelOrder();

        return new ProPraImage(this.getImgWidth(), this.getImgHeight(), this.getPixelDepth(), compressionType, this.getImgData());
    }
}
