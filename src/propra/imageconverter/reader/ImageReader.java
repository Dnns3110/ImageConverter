package propra.imageconverter.reader;

import propra.imageconverter.Compression;
import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.imageheader.ImageHeader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class ImageReader extends BufferedInputStream {

    private long numBytesRead = 0;

    public ImageReader(InputStream in) {
        super(in);
    }

    /**
     * Returns header for specific image file.
     *
     * @return image file header.
     * @throws IOException           if an I/O error occurs.
     * @throws InvalidImageException if imageHeader is invalid, or file is smaller than the header.
     */
    public ImageHeader readHeader() throws IOException, InvalidImageException {
        byte[] fileHeader = new byte[this.getHeaderSize()];
        int amountRead = this.read(fileHeader);
        if (amountRead != this.getHeaderSize()) {
            String message = String.format("Amount of bytes read does not correspond to header size. " +
                    "Expected %d, read %d bytes.", 0, amountRead);
            throw new InvalidImageException(message);
        }

        return parseHeader(fileHeader);
    }

    public Pixel readPixel(byte pixelDepth) throws IOException {
        int bytesToRead = pixelDepth / 8;
        byte[] pixelData = new byte[bytesToRead];
        long numBytesRead = this.read(pixelData);
        this.numBytesRead += numBytesRead;

        if (numBytesRead == bytesToRead) {
            return new Pixel(pixelData, this.getPixelOrder());
        }

        return null;
    }

    public Pixel[] readRow(ImageHeader header) throws IOException {
        if (header.getCompression() == Compression.Uncompressed) {
            return readUncompressedRow(header);
        } else if (header.getCompression() == Compression.RLE) {
            return readRLERow(header);
        }

        return null;
    }

    private Pixel[] readUncompressedRow(ImageHeader header) throws IOException {
        int bytesPerPixel = header.getPixelDepth() / 8;
        int bytesToRead = header.getImgWidth() * bytesPerPixel;
        byte[] readBytes = new byte[bytesToRead];
        Pixel[] row = new Pixel[header.getImgWidth()];
        int numBytesRead = this.read(readBytes);


        if (numBytesRead == bytesToRead) {
            this.numBytesRead += numBytesRead;
            for (int i = 0; i < row.length; i++) {
                byte[] pixel = Arrays.copyOfRange(readBytes, i * bytesPerPixel, i * bytesPerPixel + bytesPerPixel);
                row[i] = new Pixel(pixel, this.getPixelOrder());
            }

            return row;
        }

        return null;
    }

    private Pixel[] readRLERow(ImageHeader header) throws IOException {
        Pixel[] row = new Pixel[header.getImgWidth()];

        return null;
    }

    protected abstract PixelOrder getPixelOrder();

    protected abstract ImageHeader parseHeader(byte[] header) throws InvalidImageException;

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    public abstract int getHeaderSize();

    /**
     * Return, if Optional Data behind image Data is allowed.
     *
     * @return optional data is allowed.
     */
    public boolean allowOptionalData() {
        return true;
    }
}
