package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * A Raw packet used in RLE Compression.
 */
public class RawPacket implements Packet {

    /**
     * Stores pixels in that packet.
     */
    private ArrayList<Pixel> pixels;

    /**
     * Constructs a new Raw packet and adds the first pixel to it.
     *
     * @param pixel first pixel to be added.
     */
    public RawPacket(Pixel pixel) {
        this.pixels = new ArrayList<>();
        this.addPixel(pixel);
    }

    /**
     * Adds a pixel to the packet.
     * @param pixel pixel to be added.
     */
    @Override
    public void addPixel(Pixel pixel) {
        pixels.add(pixel);
    }

    /**
     * Returns size of the packet.
     * @return packetSize.
     */
    @Override
    public int packetSize() {
        return this.pixels.size();
    }

    /**
     * Converts the packet into a byte array to be written to a file.
     * @param po Pixel Order used to convert Pixel to byte array.
     * @return byte array representing the packet.
     */
    @Override
    public byte[] toByteArray(PixelOrder po) {
        ByteBuffer buf = ByteBuffer.allocate(this.packetSize() * 3 + 1);
        byte controlByte = (byte) ((this.packetSize() - 1) & 0x7F);
        buf.put(controlByte);

        for (Pixel pixel : pixels) {
            buf.put(pixel.getPixel(po));
        }

        return buf.array();
    }

    /**
     * Returns the last pixel that got added to the packet.
     * @return last added pixel.
     */
    @Override
    public Pixel lastPixel() {
        return this.pixels.get(this.pixels.size() - 1);
    }
}
