package propra.imageconverter.io;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;
import propra.imageconverter.image.*;

import java.io.IOException;
import java.io.InputStream;

/**
 * Class to read a ProPra image from specified input file.
 */
public class ProPraReader extends ImageReader {

    /**
     * Buffer in binary representation.
     */
    private StringBuilder buffer = new StringBuilder();

    /**
     * EOF read.
     */
    private boolean eof = false;

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
        } else if (compressionType == 2) {
            compression = Compression.Huffman;
        }

        return new ProPraImageHeader(magic, imgWidth, imgHeight, pixelDepth, compression, dataSegmentSize, checksum, null);
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

    /**
     * Returns header for specific image file.
     *
     * @return image file header.
     * @throws IOException           if an I/O error occurs.
     * @throws InvalidImageException if imageHeader is invalid, or file is smaller than the header.
     */
    @Override
    public ImageHeader readHeader() throws IOException, InvalidImageException {
        ProPraImageHeader header = (ProPraImageHeader) super.readHeader();

        return header;
    }

    /**
     * Reads a row of pixels from the input file
     * in huffman coding. If tree is not already read, this happens first
     *
     * @param header   image file header.
     * @param checksum checksum to get updated.
     * @return row of pixels.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    @Override
    public Pixel[] readRow(ImageHeader header, Checksum checksum) throws IOException, InvalidImageException {
        Node tree = ((ProPraImageHeader) header).getHuffmanTree();
        if (header.getCompression() == Compression.Huffman) {
            if (tree.isEmpty()) {
                readTree(tree, checksum);
            }

            return readHuffmanRow(header, tree, checksum);
        }

        return super.readRow(header, checksum);
    }

    /**
     * Reads huffman tree from the input file
     *
     * @param tree     huffman tree.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method,
     *                     or an I/O error occurs.
     */
    public void readTree(Node tree, Checksum checksum) throws IOException, InvalidImageException {
        Node currentNode = tree;

        // Dispose first bit, as this represents the root, we already have.
        getNBits(1, checksum);

        while (currentNode.isAppendable()) {
            Integer bits = getNBits(1, checksum);

            if (bits == 0) {
                currentNode = currentNode.appendNode(new Node());
            } else if (bits == 1) {
                Integer symbol = getNBits(8, checksum);
                currentNode = currentNode.appendNode(new Node((byte) symbol.intValue()));
            }

            if (bits == null) {
                throw new InvalidImageException("Image in huffman coding does neither contain a full tree, " +
                        "nor has any data left behind the definition of the huffman tree");
            }
        }
    }

    /**
     * Reads n bits from the buffer and fills the buffer, whenever the buffer length is below 16 characters.
     * Whenever bytes are read from the file, the checksum gets updated.
     *
     * @param n        number of bits to be read.
     * @param checksum checksum to be updated when bytes are read from file.
     * @return read bits as integer.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method,
     *                     or an I/O error occurs.
     */
    private Integer getNBits(int n, Checksum checksum) throws IOException {
        // Fill buffer, when buffer length is below 16 characters (and if there is something left to read)
        if (!this.eof && this.buffer.length() < 16) {
            int b = this.read();
            if (b != -1) {
                checksum.add((byte) b);
                this.incrementDataSegmentSize(1);
                String binaryString = String.format("%8s", Integer.toBinaryString(b)).replace(' ', '0');
                this.buffer.append(binaryString);
            } else {
                this.eof = true;
            }
        }

        if (this.buffer.length() > 0) {
            String bitStr = this.buffer.substring(0, n);
            this.buffer.delete(0, n);

            return Integer.parseInt(bitStr, 2);
        }

        return null;
    }


    private Pixel[] readHuffmanRow(ImageHeader header, Node tree, Checksum checksum) throws IOException, InvalidImageException {
        Pixel[] row = new Pixel[header.getImgWidth()];

        for (int i = 0; i < header.getImgWidth(); i++) {
            row[i] = this.getPixel(header, tree, checksum);
        }

        return row;
    }


    private Pixel getPixel(ImageHeader header, Node tree, Checksum checksum) throws IOException, InvalidImageException {
        byte[] pixelBytes = new byte[header.getPixelDepth() / 8];

        for (int i = 0; i < pixelBytes.length; i++) {
            pixelBytes[i] = this.getByte(tree, checksum);
        }

        return new Pixel(pixelBytes, header.getPixelOrder());
    }


    private byte getByte(Node tree, Checksum checksum) throws IOException, InvalidImageException {
        Node currentNode = tree;

        while (!currentNode.isLeave()) {
            Integer bit = this.getNBits(1, checksum);

            if (bit == 0) {
                currentNode = currentNode.getLeftChild();
            } else if (bit == 1) {
                currentNode = currentNode.getRightChild();
            } else {
                throw new InvalidImageException("Less image data to read, than expected.");
            }
        }

        return currentNode.getSymbol();
    }
}
