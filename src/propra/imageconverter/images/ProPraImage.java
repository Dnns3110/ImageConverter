package propra.imageconverter.images;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ByteHandler;

import java.nio.ByteBuffer;

/**
 * Image in ProPra Format.
 */
public class ProPraImage extends Image {

    /**
     * Compression type field from image file header.
     */
    private byte compressionType;

    /**
     * Data segment size field from image file header.
     */
    private long dataSegmentSize;

    /**
     * Checksum field from image file header.
     */
    private int checksum;

    /**
     * Constructs a ProPra image with xOrigin, yOrigin, width, height, pixelDepth, image descriptor and pixel data of
     * image.
     *
     * @param imgWidth        width of image.
     * @param imgHeight       height of image.
     * @param pixelDepth      pixel Depth of image.
     * @param compressionType compression type field from image file header.
     * @param imgData         pixel data of image.
     */
    public ProPraImage(short imgWidth, short imgHeight, byte pixelDepth, byte compressionType, byte[][][] imgData) {
        super(imgWidth, imgHeight, pixelDepth, imgData);

        this.compressionType = compressionType;
        this.dataSegmentSize = imgHeight * imgWidth * (pixelDepth / 8);
        this.checksum = calcChecksum();
    }

    /**
     * Constructs a  ProPra image, that is loaded from a file.
     *
     * @param filePath path of image file to be loaded.
     * @throws InvalidImageException if loaded image is invalid.
     */
    public ProPraImage(String filePath) throws InvalidImageException {
        super(filePath);

        int calculatedChecksum = calcChecksum();
        if (calculatedChecksum != this.getChecksum()) {
            throw new InvalidImageException(String.format("Mismatch between read checksum(0x%08X) and " +
                    "calculated checksum(0x%08X). Please verify.", this.getChecksum(), calculatedChecksum));
        }
    }

    /**
     * Returns compression type field from image file header.
     *
     * @return compressionType.
     */
    public byte getCompressionType() {
        return compressionType;
    }

    /**
     * Sets compression type field from image file header.
     *
     * @param compressionType compressionType to be set.
     */
    public void setCompressionType(byte compressionType) {
        this.compressionType = compressionType;
    }

    /**
     * Returns data segment size field from image file header.
     *
     * @return dataSegmentSize.
     */
    public long getDataSegmentSize() {
        return dataSegmentSize;
    }

    /**
     * Sets data segment size field from image file header.
     *
     * @param dataSegmentSize dataSegmentSize to be set.
     */
    public void setDataSegmentSize(long dataSegmentSize) {
        this.dataSegmentSize = dataSegmentSize;
    }

    /**
     * Returns checksum field from image file header.
     *
     * @return checksum.
     */
    public int getChecksum() {
        return checksum;
    }

    /**
     * Sets checksum field from image file header.
     *
     * @param checksum checksum to be set.
     */
    public void setCheckSum(int checksum) {
        this.checksum = checksum;
    }

    /**
     * Return byte of image data at index i (like it is stored in the file).
     *
     * @param i index of image data
     * @return byte at index
     */
    public int getImgData(long i) {
        int d = (int) (i) % 3;
        int w = (int) ((i / 3) % this.getImgWidth());
        int h = (int) (i / 3 / this.getImgWidth());

        return this.getImgData()[h][w][d] & 0xFF;
    }

    /**
     * Handle header of image.
     *
     * @param fileHeader file header from image file to be processed.
     */
    @Override
    protected void handleHeader(byte[] fileHeader) throws InvalidImageException {
        if (!ByteHandler.byteArrayToString(fileHeader, 10).equals("ProPraWS19")) {
            throw new InvalidImageException("Loaded ProPra Image does not start with String \"ProPraWS19\"");
        }

        this.setImgWidth(ByteHandler.byteArrayToShort(fileHeader, 0x0A));
        this.setImgHeight(ByteHandler.byteArrayToShort(fileHeader, 0x0C));
        this.setPixelDepth(fileHeader[0x0E]);
        this.setCompressionType(fileHeader[0x0F]);
        this.setDataSegmentSize(ByteHandler.byteArrayToLong(fileHeader, 0x10));
        this.setCheckSum(ByteHandler.byteArrayToInt(fileHeader, 0x18));

        long dataSegmentSize = this.getImgHeight() * this.getImgWidth() * (this.getPixelDepth() / 8);
        if (this.getDataSegmentSize() != dataSegmentSize) {
            throw new InvalidImageException("Read data segment size does not match height, width and pixel depth.");
        } else if (this.getCompressionType() != 0) {
            throw new InvalidImageException("Unsupported compression type used: Supported: 0, found: " + this.getCompressionType());
        }

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
     */
    @Override
    protected byte[] getHeader() {
        ByteBuffer buf = ByteBuffer.allocate(getHeaderSize());

        buf.put("ProPraWS19".getBytes());
        buf.put(ByteHandler.shortToByteArray(this.getImgWidth()));
        buf.put(ByteHandler.shortToByteArray(this.getImgHeight()));
        buf.put(this.getPixelDepth());
        buf.put(this.getCompressionType());
        buf.put(ByteHandler.longToByteArray(this.getDataSegmentSize()));
        buf.put(ByteHandler.intToByteArray(this.getChecksum()));

        return buf.array();
    }

    /**
     * Returns header size of image type.
     *
     * @return size of header in byte.
     */
    @Override
    public int getHeaderSize() {
        return 28;
    }

    /**
     * Returns a converted Image.
     *
     * @return converted image.
     */
    @Override
    public Image convert() {
        short xOrigin = 0;
        short yOrigin = this.getImgHeight();
        byte imgDescriptor = 0x20;

        swapPixelOrder();

        return new TGAImage(xOrigin, yOrigin, this.getImgWidth(), this.getImgHeight(), this.getPixelDepth(), imgDescriptor, this.getImgData());
    }

    /**
     * Calculates checksum from image data.
     *
     * @return calculated checksum.
     */
    public int calcChecksum() {
        long n = this.getDataSegmentSize();
        int x = 65513;
        long a = 0;
        long b = 1;

        for (int i = 1; i <= n; i++) {
            a += i + getImgData(i - 1);
            b = (b + a % x) % x;
        }

        return (int) ((a % x * (int) Math.pow(2, 16)) + b);
    }
}

