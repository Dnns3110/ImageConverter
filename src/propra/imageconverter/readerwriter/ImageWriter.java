package propra.imageconverter.readerwriter;

import propra.imageconverter.Checksum;
import propra.imageconverter.Compression;
import propra.imageconverter.Pixel;
import propra.imageconverter.imageheader.ImageHeader;
import propra.imageconverter.rlepacket.Packet;
import propra.imageconverter.rlepacket.RawPacket;
import propra.imageconverter.rlepacket.RunLengthPacket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageWriter extends BufferedOutputStream {

    public ImageWriter(OutputStream out) {
        super(out);
    }

    public void writeRow(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        if (header.getCompression() == Compression.Uncompressed) {
            writeRowUncompressed(pixels, header, checksum);
        } else {
            // Directly handle edge case, where the row consits of only one
            if (pixels.length == 1) {
                Packet rawPacket = new RawPacket(pixels[0]);
                byte[] bytes = rawPacket.toByteArray(header.getPixelOrder());
                this.write(bytes);
                checksum.add(bytes);
            } else {
                writeRowRLE(pixels, header, checksum);
            }
        }
    }

    private void writeRowUncompressed(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        for (Pixel pixel : pixels) {
            byte[] convertedPixel = pixel.getPixel(header.getPixelOrder());
            this.write(convertedPixel);
            checksum.add(convertedPixel);
        }
    }

    private void writeRowRLE(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        Packet currentPacket = null;

        for (int i = 0; i < pixels.length; i++) {
            if (currentPacket != null && currentPacket.packetSize() == 0x80) {
                this.writePacket(currentPacket, header, checksum);
                currentPacket = null;
            }

            if (currentPacket instanceof RunLengthPacket) {
                Pixel currentPixel = pixels[i];
                if (currentPacket.lastPixel().equals(currentPixel)) {
                    currentPacket.addPixel(currentPixel);
                } else {
                    this.writePacket(currentPacket, header, checksum);
                    currentPacket = null;
                }
            } else if (currentPacket instanceof RawPacket) {
                Pixel currentPixel = pixels[i];
                if (i < (pixels.length - i) && currentPixel.equals(pixels[i + 1])) {
                    this.writePacket(currentPacket, header, checksum);
                    currentPacket = null;
                } else {
                    currentPacket.addPixel(currentPixel);
                }
            }

            if (currentPacket == null) {
                if (i < (pixels.length - i) && pixels[i].equals(pixels[i + 1])) {
                    currentPacket = new RunLengthPacket(pixels[i]);
                } else {
                    currentPacket = new RawPacket(pixels[i]);
                }
            }
        }

        this.writePacket(currentPacket, header, checksum);
    }

    private void writePacket(Packet packet, ImageHeader header, Checksum checksum) throws IOException {
        byte[] bytes = packet.toByteArray(header.getPixelOrder());
        this.write(bytes);
        checksum.add(bytes);
    }
}
