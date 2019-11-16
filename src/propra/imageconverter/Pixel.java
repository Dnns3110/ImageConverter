package propra.imageconverter;

public class Pixel {
    private byte r;
    private byte g;
    private byte b;

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

    @Override
    public String toString() {
        return "Pixel{" +
                "r=" + r +
                ", g=" + g +
                ", b=" + b +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Pixel) {
            Pixel other = (Pixel) obj;
            return this.r == other.r && this.g == other.g && this.b == other.b;
        }

        return false;
    }
}
