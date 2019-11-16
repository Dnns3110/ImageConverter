package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

import java.nio.ByteBuffer;
import java.util.ArrayList;

public class RawPacket implements Packet {
    private ArrayList<Pixel> pixels;

    public RawPacket(Pixel pixel) {
        this.pixels = new ArrayList<>();
        this.addPixel(pixel);
    }

    @Override
    public void addPixel(Pixel pixel) {
        pixels.add(pixel);
    }

    @Override
    public int packetSize() {
        return this.pixels.size();
    }

    @Override
    public byte[] toByteArray(PixelOrder po) {
        ByteBuffer buf = ByteBuffer.allocate(this.packetSize() * 3 + 1);
        byte controlByte = (byte) ((this.packetSize() - 1) & 0x7F);
        buf.put(controlByte);

        for (Pixel pixel : pixels) {
            buf.put(pixel.getPixel(po));
        }

        System.out.printf("Raw: 0x%08X | 0x%08X\n", this.packetSize() - 1, this.packetSize() * 3);

        return buf.array();
    }

    @Override
    public Pixel lastPixel() {
        return this.pixels.get(this.pixels.size() - 1);
    }
}
