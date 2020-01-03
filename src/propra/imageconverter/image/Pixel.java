package propra.imageconverter.image;

/**
 * Represents a pixel.
 */
public class Pixel {

    private byte r;
    private byte g;
    private byte b;

    /**
     * Constructs a pixel from a given byte array, based on the given pixel order.
     *
     * @param pixel      bytes that represent the pixel.
     * @param pixelOrder order of the colors.
     */
    public Pixel(byte[] pixel, PixelOrder pixelOrder) {
        switch (pixelOrder) {
            case BGR:
                this.b = pixel[0];
                this.g = pixel[1];
                this.r = pixel[2];
                break;
            case GBR:
                this.g = pixel[0];
                this.b = pixel[1];
                this.r = pixel[2];
                break;
            default:
                this.r = pixel[0];
                this.g = pixel[1];
                this.b = pixel[2];
                break;
        }
    }

    /**
     * Returns the pixel as byte array in a specified pixel order.
     * @param pixelOrder order of the colors in the byte array.
     * @return bytes representing pixel.
     */
    public byte[] getPixel(PixelOrder pixelOrder) {
        switch (pixelOrder) {
            case BGR:
                return new byte[]{this.b, this.g, this.r};
            case GBR:
                return new byte[]{this.g, this.b, this.r};
            default:
                return new byte[]{this.r, this.g, this.b};
        }
    }

    /**
     * Compares this pixel to an object.
     * @param obj object to be compared to.
     * @return true, if r, g and b value of the given object is equal to the values of this pixel.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pixel) {
            Pixel other = (Pixel) obj;
            return this.r == other.r && this.g == other.g && this.b == other.b;
        }

        return false;
    }
}
