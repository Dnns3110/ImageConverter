package propra.imageconverter.rlepacket;

import propra.imageconverter.Pixel;
import propra.imageconverter.PixelOrder;

/**
 * A packet, used for RLE Compression.
 */
public interface Packet {

    /**
     * Adds a pixel to the packet.
     *
     * @param pixel pixel to be added.
     */
    void addPixel(Pixel pixel);

    /**
     * Returns size of the packet.
     * @return packetSize.
     */
    int packetSize();

    /**
     * Converts the packet into a byte array to be written to a file.
     * @param po Pixel Order used to convert Pixel to byte array.
     * @return byte array representing the packet.
     */
    byte[] toByteArray(PixelOrder po);

    /**
     * Returns the last pixel that got added to the packet.
     * @return last added pixel.
     */
    Pixel lastPixel();
}
