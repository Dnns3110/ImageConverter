package propra.imageconverter.readerwriter;

import propra.imageconverter.Checksum;
import propra.imageconverter.Compression;
import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;
import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.imageheader.ImageHeader;
import propra.imageconverter.imageheader.ProPraImageHeader;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Abstract class for Image readers.
 */
public abstract class ImageReader extends BufferedInputStream {

    /**
     * Number of bytes in data segment.
     */
    private long dataSegmentSize = 0;

    /**
     * Creates an <code>ImageReader</code>
     * and saves its  argument, the input stream
     * <code>in</code>, for later use. An internal
     * buffer array is created and  stored in <code>buf</code>.
     *
     * @param in the underlying input stream.
     */
    public ImageReader(InputStream in) {
        super(in);
    }

    /**
     * Returns data segment size.
     *
     * @return dataSegementSize.
     */
    public long getDataSegmentSize() {
        return dataSegmentSize;
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

    /**
     * Reads a row of pixels from the input file
     * in uncompressed or rle compressed format
     * and converts it into an array of Pixels.
     * Checksum gets only updated for ProPra images.
     *
     * @param header   image file header.
     * @param checksum checksum to get updated.
     * @return
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    public Pixel[] readRow(ImageHeader header, Checksum checksum) throws IOException {
        if (header.getCompression() == Compression.Uncompressed) {
            return readUncompressedRow(header, checksum);
        } else if (header.getCompression() == Compression.RLE) {
            return readRLERow(header, checksum);
        }

        return null;
    }

    /**
     * Reads an umcompressed Row from the input file and converts it into an array of Pixels.
     * Checksum gets only updated for ProPra images.
     *
     * @param header   image file header.
     * @param checksum checksum to get updated.
     * @return
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    private Pixel[] readUncompressedRow(ImageHeader header, Checksum checksum) throws IOException {
        int bytesPerPixel = header.getPixelDepth() / 8;
        int bytesToRead = header.getImgWidth() * bytesPerPixel;
        byte[] readBytes = new byte[bytesToRead];
        Pixel[] row = new Pixel[header.getImgWidth()];
        int numBytesRead = this.read(readBytes);

        if (header instanceof ProPraImageHeader) {
            checksum.add(readBytes);
            this.dataSegmentSize += numBytesRead;
        }

        if (numBytesRead == bytesToRead) {
            for (int i = 0; i < row.length; i++) {
                byte[] pixel = Arrays.copyOfRange(readBytes, i * bytesPerPixel, i * bytesPerPixel + bytesPerPixel);
                row[i] = new Pixel(pixel, this.getPixelOrder());
            }

            return row;
        }

        return null;
    }

    /**
     * Reads a run-length encoded Row from the input file and converts it into an array of Pixels.
     * Checksum gets only updated for ProPra images.
     *
     * @param header   header from input file.
     * @param checksum checksum to get updated.
     * @return array of pixel representing the currently read row
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    private Pixel[] readRLERow(ImageHeader header, Checksum checksum) throws IOException {
        int pixelsToRead = header.getImgWidth();
        Pixel[] row = new Pixel[pixelsToRead];
        int bytesPerPixel = header.getPixelDepth() / 8;
        int numPixelsRead = 0;

        while (numPixelsRead < pixelsToRead) {
            int controlByte = this.read();
            boolean isRaw = (controlByte & 0x80) == 0;
            int numPixels = (controlByte & 0x7F) + 1;
            int bytesToRead = isRaw ? numPixels * bytesPerPixel : bytesPerPixel;
            byte[] readBytes = new byte[bytesToRead];
            int numBytesRead = this.read(readBytes);

            if (header instanceof ProPraImageHeader) {
                this.dataSegmentSize += numBytesRead + 1;
                checksum.add((byte) controlByte);
                checksum.add(readBytes);
            }

            if (numBytesRead == readBytes.length) {
                if (isRaw) {
                    for (int i = 0; i < numPixels; i++) {
                        byte[] pixel = Arrays.copyOfRange(readBytes, i * bytesPerPixel, i * bytesPerPixel + bytesPerPixel);
                        row[numPixelsRead + i] = new Pixel(pixel, this.getPixelOrder());
                    }
                } else {
                    Pixel pixel = new Pixel(readBytes, this.getPixelOrder());
                    for (int i = 0; i < numPixels; i++) {
                        row[numPixelsRead + i] = pixel;
                    }
                }


                numPixelsRead += numPixels;
            } else {
                return null;
            }

        }

        return row;
    }

    /**
     * Returns Pixel order of the image format.
     *
     * @return pixel order
     */
    protected abstract PixelOrder getPixelOrder();

    /**
     * Parses read bytes into the corresponding fields of the image header
     *
     * @param header read bytes from input file
     * @return created header from read bytes
     * @throws InvalidImageException if header is invalid
     */
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
