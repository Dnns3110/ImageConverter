package propra.imageconverter.imageheader;

import propra.imageconverter.Compression;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Abstract class for image headers.
 */
public abstract class ImageHeader {

    /**
     * Width of image.
     */
    private short imgWidth;

    /**
     * Height of image.
     */
    private short imgHeight;

    /**
     * Pixel Depth of image.
     */
    private byte pixelDepth;

    /**
     * Image Compression.
     */
    private Compression compression;

    /**
     * Returns width of image.
     *
     * @return imgWidth.
     */
    public short getImgWidth() {
        return imgWidth;
    }

    /**
     * Returns height of image.
     *
     * @return imgHeight.
     */
    public short getImgHeight() {
        return imgHeight;
    }

    /**
     * Returns pixel depth.
     *
     * @return pixelDepth.
     */
    public byte getPixelDepth() {
        return pixelDepth;
    }

    /**
     * Constructs an Image with width, height, pixelDepth and pixel data of that image.
     *
     * @param imgWidth   width of image.
     * @param imgHeight  height of image.
     * @param pixelDepth pixel depth.
     * @param compression compression.
     * @throws InvalidImageException if the image header is invalid
     */
    public ImageHeader(short imgWidth, short imgHeight, byte pixelDepth, Compression compression) throws InvalidImageException {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.pixelDepth = pixelDepth;
        this.compression = compression;

        if (imgWidth == 0) {
            throw new InvalidImageException("Invalid image dimensions. Width of 0 is not allowed.");
        } else if (imgHeight == 0) {
            throw new InvalidImageException("Invalid image dimensions. Height of 0 is not allowed.");
        } else if (pixelDepth != 24) {
            throw new InvalidImageException(String.format("Unsupported pixel depth. Supported: 3, found: %d.", pixelDepth));
        } else if (this.compression == null) {
            throw new InvalidImageException("Invalid compression. Only uncompressed or rle compression allowed.");
        }
    }

    /**
     * Returns compression type.
     *
     * @return compression.
     */
    public Compression getCompression() {
        return compression;
    }

    /**
     * Loads image from file.
     *
     * @param filePath path of image file to be loaded.
     * @throws InvalidImageException if loaded image is invalid.
     */
    protected void loadImageFromFile(String filePath) throws InvalidImageException {
        File inFile = new File(filePath);
        BufferedInputStream bis = null;
        byte[] pixel;
        byte[][][] imgData;
        int x = 0;
        int y = 0;
        int amountRead;

        try {
            bis = new BufferedInputStream(new FileInputStream(inFile));

            // Init imgData based on image size from file header.
            imgData = new byte[this.getImgHeight()][this.getImgWidth()][this.getPixelDepth() / 8];
            pixel = new byte[this.getPixelDepth() / 8];

            while ((amountRead = bis.read(pixel)) == (this.getPixelDepth() / 8) && y < this.getImgHeight()) {
                imgData[y][x] = pixel;
                pixel = new byte[this.getPixelDepth() / 8];

                if (++x == this.getImgWidth()) {
                    y++;
                    x = 0;
                }
            }


        } catch (IOException e) {
            throw new InvalidImageException(e.toString());
        }
    }

    /**
     * Returns header as byte array to be written into a file.
     *
     * @return this header as byte array.
     */
    public abstract byte[] toByteArray();

    /**
     * Return pixel order for the actual image format. Can currently be BGR or GBR.
     *
     * @return pixel order for image format.
     */
    public abstract PixelOrder getPixelOrder();

}
