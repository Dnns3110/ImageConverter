package propra.imageconverter.io;

import propra.imageconverter.image.*;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;

public class ProPraWriter extends ImageWriter {

    private StringBuilder buffer = new StringBuilder();

    public ProPraWriter(OutputStream out) {
        super(out);
    }

    @Override
    public void writeRow(Pixel[] pixels, ImageHeader header, Checksum checksum) throws IOException {
        if (header.getCompression() == Compression.Huffman) {
            ProPraImageHeader proPraHeader = (ProPraImageHeader) header;
            if (proPraHeader.getHuffmanTable() == null) {
                // In this case, the table has not been created. This is an indicator, that the tree has not
                // already been written to the datasegment of the outfile.
                HashMap<Byte, String> huffmanTable = new HashMap<>();
                proPraHeader.getHuffmanTree().getCode("", huffmanTable);
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

    public void flush(Checksum checksum) throws IOException {
        while (this.buffer.length() % 8 > 0) {
            this.buffer.append("0");
        }

        this.putBits("", checksum);
    }
}
