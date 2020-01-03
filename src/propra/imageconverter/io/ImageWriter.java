package propra.imageconverter.io;

import propra.imageconverter.image.*;
import propra.imageconverter.rlepacket.Packet;
import propra.imageconverter.rlepacket.RawPacket;
import propra.imageconverter.rlepacket.RunLengthPacket;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class to write an image to a specified output file.
 */
public class ImageWriter extends BufferedOutputStream {
    /**
     * Number of bytes in data segment.
     */
    private long dataSegmentSize = 0;

    public ImageWriter(OutputStream out) {
        super(out);
    }

    /**
     * Returns data segment size.
     *
     * @return dataSegementSize.
     */
    public long getDataSegmentSize() {
        return dataSegmentSize;
    }


    /**
     * Writes a row of pixels into the output file in uncompressed or rle compressed format.
     * Checksum gets only updated for ProPra images.
     *
     * @param pixels   pixels to be written.
     * @param header   header for output file.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    public void writeRow(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        if (header.getCompression() == Compression.Uncompressed) {
            writeRowUncompressed(pixels, header, checksum);
        } else {
            // Directly handle edge case, where the row consits of only one
            if (pixels.length == 1) {
                Packet rawPacket = new RawPacket(pixels[0]);
                byte[] bytes = rawPacket.toByteArray(header.getPixelOrder());
                this.write(bytes);

                if (header instanceof ProPraImageHeader) {
                    checksum.add(bytes);
                    this.dataSegmentSize += bytes.length;
                }
            } else {
                writeRowRLE(pixels, header, checksum);
            }
        }
    }

    /**
     * Writes a row of pixels into the output file in uncompressed format.
     * Checksum gets only updated for ProPra images.
     *
     * @param pixels   pixels to be written.
     * @param header   header for output file.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    private void writeRowUncompressed(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        for (Pixel pixel : pixels) {
            byte[] convertedPixel = pixel.getPixel(header.getPixelOrder());
            this.write(convertedPixel);

            if (header instanceof ProPraImageHeader) {
                checksum.add(convertedPixel);
                this.dataSegmentSize += convertedPixel.length;
            }
        }
    }

    /**
     * Writes a row of pixels into the output file in rle compressed format.
     * Checksum gets only updated for ProPra images.
     *
     * @param pixels   pixels to be written.
     * @param header   header for output file.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
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
                if (i < (pixels.length - 1) && currentPixel.equals(pixels[i + 1])) {
                    this.writePacket(currentPacket, header, checksum);
                    currentPacket = null;
                } else {
                    currentPacket.addPixel(currentPixel);
                }
            }

            if (currentPacket == null) {
                if (i < (pixels.length - 1) && pixels[i].equals(pixels[i + 1])) {
                    currentPacket = new RunLengthPacket(pixels[i]);
                } else {
                    currentPacket = new RawPacket(pixels[i]);
                }
            }
        }

        this.writePacket(currentPacket, header, checksum);
    }

    /**
     * Write a packet (either uncompressed or a run-length packet) to the output file
     * and update checksum (if writing a ProPra image.
     *
     * @param packet   packet to be written.
     * @param header   header of output file.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    private void writePacket(Packet packet, ImageHeader header, Checksum checksum) throws IOException {
        byte[] bytes = packet.toByteArray(header.getPixelOrder());
        this.write(bytes);

        if (header instanceof ProPraImageHeader) {
            checksum.add(bytes);
            this.dataSegmentSize += bytes.length;
        }
    }
}
