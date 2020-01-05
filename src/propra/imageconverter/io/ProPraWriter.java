package propra.imageconverter.io;

import propra.imageconverter.image.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Class to write a ProPra Images to  a specified output file.
 */
public class ProPraWriter extends ImageWriter {

    /**
     * Buffer in binary representation.
     */
    private StringBuilder buffer = new StringBuilder();

    /**
     * Creates a new ProPra writer to write data to the
     * specified underlying output stream.
     *
     * @param out the underlying output stream.
     */
    public ProPraWriter(OutputStream out) {
        super(out);
    }

    /**
     * Writes a row of pixels into the output file in uncompressed, rle or huffman compressed format.
     * Checksum gets only updated for ProPra images.
     *
     * @param pixels   pixels to be written.
     * @param header   header for output file.
     * @param checksum checksum to get updated.
     * @throws IOException if this input stream has been closed by invoking its {@link #close()} method, or an I/O error occurs.
     */
    @Override
    public void writeRow(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        if (header.getCompression() == Compression.Huffman) {
            ProPraImageHeader proPraHeader = (ProPraImageHeader) header;
            if (proPraHeader.getHuffmanTable() == null) {
                // In this case, the table has not been created. This is an indicator, that the tree has not
                // already been written to the datasegment of the outfile.
                HashMap<Byte, String> huffmanTable = new HashMap<>();
                proPraHeader.getHuffmanTree().buildHuffmanTable("", huffmanTable);
                proPraHeader.setHuffmanTable(huffmanTable);
                this.putBits(proPraHeader.getHuffmanTree().getTreeInPreOrder(), checksum);
            }

            HashMap<Byte, String> huffmanTable = ((ProPraImageHeader) header).getHuffmanTable();

            for (Pixel pixel : pixels) {
                for (byte b : pixel.getPixel(header.getPixelOrder())) {
                    String bitString = huffmanTable.get(b);
                    this.putBits(bitString, checksum);
                }
            }
        } else {
            super.writeRow(pixels, header, checksum);
        }
    }

    /**
     * Put bits from <code>bits</code> to the buffer. Whenever the buffer has a length of a multiple of 8,
     * the first 8 characters (<code>0</code> or <code>1</code>) get converted into a byte and written to the outfile.
     *
     * @param bits     bit string to get added to the buffer.
     * @param checksum checksum to get updated.
     * @throws IOException if an I/O error occurs.
     */
    private void putBits(String bits, Checksum checksum) throws IOException {
        this.buffer.append(bits);

        while (this.buffer.length() >= 8) {
            String bitString = this.buffer.substring(0, 8);
            byte b = (byte) Integer.parseInt(bitString, 2);
            this.buffer.delete(0, 8);
            this.write(b);
            this.incrementDataSegmentSize(1);
            checksum.add(b);
        }
    }

    /**
     * Flush the buffer. Append <code>0</code> to the end of the buffer,
     * until the length of the buffer is a multiple of 8, and then write those to the outfile to clear the buffer.
     *
     * @param checksum checksum to get updated.
     * @throws IOException if an I/O error occurs.
     */
    public void flush(Checksum checksum) throws IOException {
        while (this.buffer.length() % 8 > 0) {
            this.buffer.append("0");
        }

        this.putBits("", checksum);
    }
}
