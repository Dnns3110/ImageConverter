package propra.imageconverter;

public class Checksum {

    private static int X = 65513;
    private static int TWO_POW_SIXTEEN = 65536;

    private long a = 0;
    private long b = 1;
    private PixelOrder pixelOrder;
    private long i = 1;

    /**
     * Constructs a default checksum for ProPra images pixel order.
     */
    public Checksum() {
        this(PixelOrder.GBR);
    }

    /**
     * Constructs a checksum using the given pixel order.
     *
     * @param pixelOrder pixel order to be used when adding a pixel to checksum.
     */
    public Checksum(PixelOrder pixelOrder) {
        this.pixelOrder = pixelOrder;
    }

    /**
     * Adds a pixel to the checksum.
     * @param pixel pixel to be added.
     */
    public void add(Pixel pixel) {
        this.add(pixel.getPixel(this.pixelOrder));
    }

    /**
     * Adds a byte to the checksum.
     * @param b1 bytes to be added.
     */
    public void add(byte b1) {
        a += this.i + Byte.toUnsignedInt(b1);
        b = (b + a % X) % X;
        this.i++;
    }

    /**
     * Adds a byte array to the checksum.
     * @param bytes bytes to be added.
     */
    public void add(byte[] bytes) {
        for (int j = 0; j < bytes.length; j++) {
            this.add(bytes[j]);
        }
    }

    /**
     * Does the final calculation of a checksum and returns it as int.
     * @return finally calculated checksum.
     */
    public int getChecksum() {
        return (int) ((a % X * TWO_POW_SIXTEEN) + b);
    }

    /**
     * Returns the checksum as 8 digit hex String.
     * @return checksum as String.
     */
    @Override
    public String toString() {
        return String.format("0x%08X", this.getChecksum());
    }
}
