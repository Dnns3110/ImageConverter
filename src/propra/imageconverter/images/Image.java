package propra.imageconverter.images;

import propra.imageconverter.exceptions.InvalidImageException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Abstract class for Images.
 */
public abstract class Image {

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
     * Pixel data of image.
     */
    private byte[][][] imgData;

    /**
     * Constructs an Image with width, height, pixelDepth and pixel data of that image.
     *
     * @param imgWidth   width of image.
     * @param imgHeight  height of image.
     * @param pixelDepth pixel depth.
     * @param imgData    pixel data of image.
     */
    public Image(short imgWidth, short imgHeight, byte pixelDepth, byte[][][] imgData) {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.pixelDepth = pixelDepth;
        this.imgData = imgData;
    }

    /**
     * Constructs an image, that is loaded from a file.
     *
     * @param filePath path of image file to be loaded.
     * @throws InvalidImageException if loaded image is invalid.
     */
    public Image(String filePath) throws InvalidImageException {
        loadImageFromFile(filePath);
    }

    /**
     * Returns width of image.
     *
     * @return imgWidth.
     */
    public short getImgWidth() {
        return imgWidth;
    }

    /**
     * Sets width of image.
     *
     * @param imgWidth imgWidth to be set.
     */
    public void setImgWidth(short imgWidth) {
        this.imgWidth = imgWidth;
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
     * Sets height of image.
     *
     * @param imgHeight imgHeight to be set.
     */
    public void setImgHeight(short imgHeight) {
        this.imgHeight = imgHeight;
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
     * Sets pixel depth.
     *
     * @param pixelDepth pixelDepth to be set.
     */
    public void setPixelDepth(byte pixelDepth) {
        this.pixelDepth = pixelDepth;
    }

    /**
     * Returns image Data.
     *
     * @return imgData.
     */
    public byte[][][] getImgData() {
        return imgData;
    }

    /**
     * Sets image data.
     *
     * @param imgData imgData to be set.
     */
    public void setImgData(byte[][][] imgData) {
        this.imgData = imgData;
    }

    /**
     * Loads image from file.
     *
     * @param filePath path of image file to be loaded.
     * @throws InvalidImageException if loaded image is invalid.
     */
    protected void loadImageFromFile(String filePath) throws InvalidImageException {
        File inFile = new File(filePath);
        FileInputStream fis = null;
        byte[] fileHeader = new byte[this.getHeaderSize()];
        byte[] pixel;
        byte[][][] imgData;
        int x = 0;
        int y = 0;
        int amountRead;

        try {
            fis = new FileInputStream(inFile);

            amountRead = fis.read(fileHeader);
            if (amountRead == this.getHeaderSize()) {
                handleHeader(fileHeader);
            } else {
                String message = String.format("Amount of bytes read does not correspond to header size. " +
                        "Expected %d, read %d bytes.", this.getHeaderSize(), amountRead);
                throw new InvalidImageException(message);
            }

            // Init imgData based on image size from file header.
            imgData = new byte[this.getImgHeight()][this.getImgWidth()][this.getPixelDepth() / 8];
            pixel = new byte[this.getPixelDepth() / 8];

            while ((amountRead = fis.read(pixel)) == (this.getPixelDepth() / 8) && y < this.getImgHeight()) {
                imgData[y][x] = pixel;
                pixel = new byte[this.getPixelDepth() / 8];

                if (++x == this.getImgWidth()) {
                    y++;
                    x = 0;
                }
            }
            this.setImgData(imgData);

            // Check, if there is still something to read.
            if (fis.read(pixel) != -1 && !this.allowOptionalData()) {
                throw new InvalidImageException("Found optional data in a file format, where no optional data is allowed.");
            }

            // If we are finished with the loop and have read all image data, we want, y should be imgHeight and x should be 0.
            if (y != this.getImgHeight() || x != 0) {
                throw new InvalidImageException("Less image data to read, than expected.");
            }
        } catch (InvalidImageException e) {
            throw e;
        } catch (IOException e) {
            throw new InvalidImageException(e.toString());
        } catch (OutOfMemoryError e) {
            throw new InvalidImageException("Image too large to get converted.");
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (IOException e) {
                throw new InvalidImageException(e.toString());
            }
        }
    }

    /**
     * Return, if Optional Data behind image Data is allowed.
     *
     * @return optional data is allowed.
     */
    public boolean allowOptionalData() {
        return true;
    }

    /**
     * Handle header of image.
     *
     * @param fileHeader file header from image file to be processed.
     * @throws InvalidImageException if header is invalid for this image type.
     */
    protected abstract void handleHeader(byte[] fileHeader) throws InvalidImageException;

    /**
     * Returns header for specific image file.
     *
     * @return image file header.
     */
    protected abstract byte[] getHeader();

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    public abstract int getHeaderSize();

    /**
     * Returns a converted Image.
     *
     * @return converted image.
     */
    public abstract Image convert();

    /**
     * Saves image to filePath.
     *
     * @param filePath output filepath for image.
     * @throws IOException if there is an error during save.
     */
    public void save(String filePath) throws IOException {
        File outFile = new File(filePath);
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(outFile);

            // Write file header
            fos.write(getHeader());

            // Write image data
            for (byte[][] col : this.getImgData()) {
                for (byte[] pixel : col) {
                    fos.write(pixel);
                }
            }

        } catch (IOException e) {
            // Need to catch Exception here, to have the finally block, that closes the file in case of an exception.
            throw e;
        } finally {
            if (fos != null) {
                fos.close();
            }
        }

    }

    /**
     * In TGA and ProPra format, the color order for pixels are different. TGA stores pixels as BGR, ProPra as GBR.
     */
    protected void swapPixelOrder() {
        for (byte[][] col : this.getImgData()) {
            for (byte[] pixel : col) {
                byte g = pixel[0];
                pixel[0] = pixel[1];
                pixel[1] = g;
            }
        }
    }
}
