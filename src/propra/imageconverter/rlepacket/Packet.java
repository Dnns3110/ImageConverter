package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

public interface Packet {

    void addPixel(Pixel pixel);

    int packetSize();

    byte[] toByteArray(PixelOrder po);

    Pixel lastPixel();
}
