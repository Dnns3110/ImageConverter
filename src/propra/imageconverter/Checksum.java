package propra.imageconverter;

public class Checksum {

    private static int X = 65513;
    private static int TWO_POW_SIXTEEN = 65536;

    private long a = 0;
    private long b = 1;
    private PixelOrder pixelOrder;
    private long i = 1;

    public Checksum() {
        this(PixelOrder.GBR);
    }

    public Checksum(PixelOrder pixelOrder) {
        this.pixelOrder = pixelOrder;
    }

    public void add(Pixel pixel) {
        this.add(pixel.getPixel(this.pixelOrder));
    }

    public void add(byte b1) {
        a += this.i + Byte.toUnsignedInt(b1);
        b = (b + a % X) % X;
        this.i++;
    }

    public void add(byte[] bytes) {
        for (int j = 0; j < bytes.length; j++) {
            this.add(bytes[j]);
        }
    }

    public int getChecksum() {
        return (int) ((a % X * TWO_POW_SIXTEEN) + b);
    }

    @Override
    public String toString() {
        return String.format("0x%08X", this.getChecksum());
    }
}
