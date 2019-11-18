package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

import java.nio.ByteBuffer;

/**
 * Run lenght packet used in RLW compression.
 */
public class RunLengthPacket implements Packet {

    /**
     * Size of the packet.
     */
    private int packetSize;

    /**
     * Pixel, this packet contains.
     */
    private Pixel pixel;

    /**
     * Constructs a new run-length packet and adds the first pixel to it.
     *
     * @param pixel first pixel to be added.
     */
    public RunLengthPacket(Pixel pixel) {
        this.pixel = pixel;
        this.addPixel(pixel);
    }

    /**
     * Adds a pixel to the packet. (Only increases counter)
     * @param pixel pixel to be added.
     */
    @Override
    public void addPixel(Pixel pixel) {
        this.packetSize++;
    }

    /**
     * Returns size of the packet.
     * @return packetSize.
     */
    @Override
    public int packetSize() {
        return this.packetSize;
    }

    /**
     * Converts the packet into a byte array to be written to a file.
     * @param po Pixel Order used to convert Pixel to byte array.
     * @return byte array representing the packet.
     */
    @Override
    public byte[] toByteArray(PixelOrder po) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        byte controlByte = (byte) ((this.packetSize - 1) | 0x80);
        byte[] pixel = this.pixel.getPixel(po);

        buf.put(controlByte);
        buf.put(pixel);

        return buf.array();
    }

    /**
     * Returns the last pixel that got added to the packet. (This packet contains only one pixel)
     * @return last added pixel.
     */
    @Override
    public Pixel lastPixel() {
        return this.pixel;
    }
}
