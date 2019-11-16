package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

import java.nio.ByteBuffer;

public class RunLengthPacket implements Packet {

    private int packetSize;
    private Pixel pixel;

    public RunLengthPacket(Pixel pixel) {
        this.pixel = pixel;
        this.addPixel(pixel);
    }

    @Override
    public void addPixel(Pixel pixel) {
        this.packetSize++;
    }

    @Override
    public int packetSize() {
        return this.packetSize;
    }

    @Override
    public byte[] toByteArray(PixelOrder po) {
        ByteBuffer buf = ByteBuffer.allocate(4);
        byte controlByte = (byte) ((this.packetSize - 1) | 0x80);
        byte[] pixel = this.pixel.getPixel(po);

        buf.put(controlByte);
        buf.put(pixel);

        System.out.printf("Run-Length: 0x%08X | 0x%08X\n", (this.packetSize - 1) | 0x80, this.packetSize() * 3);

        return buf.array();
    }

    @Override
    public Pixel lastPixel() {
        return this.pixel;
    }
}
