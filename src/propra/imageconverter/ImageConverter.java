package propra.imageconverter;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ArgumentHandler;
import propra.imageconverter.image.*;
import propra.imageconverter.io.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * ImageConverter is a Program, that can be used to convert images between TGA and ProPra format.
 */
public class ImageConverter {

    public static void main(String[] args) {
        try {
            ArgumentHandler argHandler = new ArgumentHandler(args);

            if (argHandler.getWorkMode() == WorkMode.Encode) {
                encodeFile(argHandler);
            } else if (argHandler.getWorkMode() == WorkMode.Decode) {
                decodeFile(argHandler);
            } else {
                // Convert two (for tga) or three (for propra) times using all compression methods that exist,
                // identify the smallest one and remove the other one(s)
                if (argHandler.getWorkMode() == WorkMode.ConvertAuto) {
                    String outExtension = argHandler.getOutFileExtension();
                    File outFile = argHandler.getOutFile();
                    File uncompressedOutFile = new File(outFile.getAbsolutePath() + "uncompressed." + outExtension);
                    File rleOutFile = new File(outFile.getAbsolutePath() + "rle." + outExtension);
                    File smallestFile;

                    // Create uncompressed file.
                    argHandler.setOutFile(uncompressedOutFile);
                    argHandler.setWorkMode(WorkMode.ConvertUncompressed);
                    convertFile(argHandler);
                    System.out.println("");

                    // Create rle compressed file.
                    argHandler.setOutFile(rleOutFile);
                    argHandler.setWorkMode(WorkMode.ConvertRLE);
                    convertFile(argHandler);
                    System.out.println("");

                    if (uncompressedOutFile.length() < rleOutFile.length()) {
                        smallestFile = uncompressedOutFile;
                        rleOutFile.delete();
                    } else {
                        smallestFile = rleOutFile;
                        uncompressedOutFile.delete();
                    }

                    // Create huffman compressed file (only for propra files.
                    if (outFile.getAbsolutePath().endsWith("propra")) {
                        File huffmanOutFile = new File(outFile.getAbsolutePath() + "huffman." + outExtension);
                        argHandler.setOutFile(huffmanOutFile);
                        argHandler.setWorkMode(WorkMode.ConvertHuffman);
                        convertFile(argHandler);
                        System.out.println();

                        if (huffmanOutFile.length() < smallestFile.length()) {
                            smallestFile.delete();
                            smallestFile = huffmanOutFile;
                        } else {
                            huffmanOutFile.delete();
                        }
                    }

                    System.out.println("Identified " + smallestFile.getName() + " as smallest file.");
                    System.out.println("Rename " + smallestFile.getName() + " => " + outFile.getName());
                    smallestFile.renameTo(outFile);

                } else {
                    convertFile(argHandler);
                }
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(123);
        }
    }

    /**
     * Convert file from either TGA format to ProPra or vice versa. Can handle uncompressed of rle compressed files.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void convertFile(ArgumentHandler argHandler) {
        System.out.println(String.format("Convert File %s -> %s", argHandler.getInFile(), argHandler.getOutFile()));
        ImageHeader inputHeader = null;
        ImageHeader outputHeader = null;
        Checksum inputChecksum = new Checksum(ProPraImageHeader.PIXEL_ORDER);
        Checksum outputChecksum = new Checksum(ProPraImageHeader.PIXEL_ORDER);
        long outputDataSegmentSize = 0;
        Node tree = null;

        if (argHandler.getWorkMode() == WorkMode.ConvertHuffman) {
            tree = buildTree(argHandler);
        }

        try (ImageReader reader = getReader(argHandler);
             ImageWriter writer = getWriter(argHandler)) {

            System.out.println("Read/Write file header.");
            inputHeader = reader.readHeader();
            outputHeader = convertHeader(inputHeader, argHandler, tree);
            writer.write(outputHeader.toByteArray());

            System.out.println("Convert image.");
            for (long i = 0; i < inputHeader.getImgHeight(); i++) {
                Pixel[] pixels = reader.readRow(inputHeader, inputChecksum);

                if (pixels != null) {
                    writer.writeRow(pixels, outputHeader, outputChecksum);
                } else {
                    throw new InvalidImageException("Less image data to read, than expected.");
                }
            }

            // Write rest of String buffer for huffman compression.
            if (writer instanceof ProPraWriter) {
                if (outputHeader.getCompression() == Compression.Huffman) {
                    ((ProPraWriter) writer).flush(outputChecksum);
                }
            }

            // Check whether there is optional data, that should not be there. If optional data is allowed, just ignore it.
            if (!reader.allowOptionalData()) {
                if (reader.read() != -1) {
                    throw new InvalidImageException("Found optional data in a file format, where no optional data is allowed.");
                }
            }

            // Validate Checksum of ProPra image.
            if (inputHeader instanceof ProPraImageHeader) {
                ((ProPraImageHeader) inputHeader).reValidateHeader(inputChecksum, reader.getDataSegmentSize());
            }

            outputDataSegmentSize = writer.getDataSegmentSize();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error occurred during conversion process:\n" + e.toString());
            System.exit(123);
        }

        // If we have a ProPra Image as output, we need to update the calculated checksum.
        if (outputHeader instanceof ProPraImageHeader) {
            ((ProPraImageHeader) outputHeader).updateHeader(argHandler.getOutFile(), outputChecksum, outputDataSegmentSize);
        }

        System.out.println("Conversion finished successfully");
    }

    /**
     * Encodes the specified file. Encoding is based on the arguments.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void encodeFile(ArgumentHandler argHandler) {
        BaseN encoder = argHandler.getEncoder();
        int maxInputBytes = encoder.maxInputBytes();
        byte[] bytesRead = new byte[maxInputBytes];
        int numBytesRead = 0;

        System.out.println(String.format("Encode File %s -> %s", argHandler.getInFile(), argHandler.getOutFile()));

        try (BufferedInputStream reader = new BufferedInputStream(new FileInputStream(argHandler.getInFile()));
             BufferedWriter writer = new BufferedWriter(new FileWriter(argHandler.getOutFile()))) {

            if (!encoder.isBase32Hex()) {
                writer.write(encoder.getAlphabet());
                writer.write(0x0A); //Line Feed
            }

            while ((numBytesRead = reader.read(bytesRead)) > 0) {
                if (numBytesRead != maxInputBytes) {
                    bytesRead = Arrays.copyOf(bytesRead, numBytesRead);
                }

                writer.write(encoder.encode(bytesRead));
            }

            writer.flush();
        } catch (Exception e) {
            System.err.println("Unexpected error occurred during encoding process:\n" + e.toString());
            System.exit(123);
        }

        System.out.println("Encoding finished successfully");
    }

    /**
     * Decodes the specified file. Decoding is based on the arguments.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void decodeFile(ArgumentHandler argHandler) {
        BaseN encoder = argHandler.getEncoder();
        int maxInputCharacters = encoder.maxInputCharacters();
        char[] charsRead = new char[maxInputCharacters];
        int numChardRead = 0;

        System.out.println(String.format("Decode File %s -> %s", argHandler.getInFile(), argHandler.getOutFile()));

        try (BufferedReader reader = new BufferedReader(new FileReader(argHandler.getInFile()));
             BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(argHandler.getOutFile()))) {

            if (!encoder.isBase32Hex()) {
                encoder.setAlphabet(reader.readLine());
            }


            while ((numChardRead = reader.read(charsRead)) > 0) {
                if (numChardRead != maxInputCharacters) {
                    charsRead = Arrays.copyOf(charsRead, numChardRead);
                }

                writer.write(encoder.decode(charsRead));
            }

            writer.flush();
        } catch (Exception e) {
            System.err.println("Unexpected error occurred during decoding process:\n" + e.toString());
            System.exit(123);
        }

        System.out.println("Decoding finished successfully");
    }

    /**
     * Reads the input File once to build the huffman tree.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     * @return huffman tree.
     */
    public static Node buildTree(ArgumentHandler argHandler) {
        System.out.println("Build Huffman Tree");
        int[] byteCount = new int[256];

        try (ImageReader reader = getReader(argHandler)) {
            ImageHeader inputHeader = reader.readHeader();
            Checksum chk = new Checksum();

            for (long i = 0; i < inputHeader.getImgHeight(); i++) {
                Pixel[] pixels = reader.readRow(inputHeader, chk);
                for (Pixel pixel : pixels) {
                    for (byte b : pixel.getPixel(inputHeader.getPixelOrder())) {
                        byteCount[Byte.toUnsignedInt(b)]++;
                    }
                }
            }
        } catch (IOException | InvalidImageException e) {
            e.printStackTrace();
        }

        ArrayList<Node> nodeList = byteCountToNodeList(byteCount);

        while (nodeList.size() > 1) {
            Collections.sort(nodeList);

            Node newNode = new Node(nodeList.get(0), nodeList.get(1));
            nodeList.remove(1);
            nodeList.remove(0);
            nodeList.add(newNode);
        }

        Node tree = nodeList.get(0);

        // Handle the case, that the tree consists of only 1 node.
        if (tree.isLeave() && tree.isRoot()) {
            return new Node(tree, new Node((byte) (tree.getSymbol() + 1), 1));
        }

        return tree;
    }

    /**
     * Returns the suitable reader for the input file format.
     * This application can (at the moment) only handle tga or propra images. And as we verified in ArgumentHandler, that
     * the input is either one of those formats, we know, that if the input file is not in tga format, it has to be in propra format instead.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     * @return suitable reader.
     * @throws FileNotFoundException if the input file does not exist.
     */
    private static ImageReader getReader(ArgumentHandler argHandler) throws IOException {
        if (argHandler.getInFileExtension().equals("tga"))
            return new TGAReader(new FileInputStream(argHandler.getInFile()));

        return new ProPraReader(new FileInputStream(argHandler.getInFile()));
    }

    /**
     * Returns the suitable writer for the input file format.
     * This application can (at the moment) only handle tga or propra images. And as we verified in ArgumentHandler, that
     * the input is either one of those formats, we know, that if the input file is not in tga format, it has to be in propra format instead.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     * @return suitable writer.
     * @throws FileNotFoundException if the input file does not exist.
     */
    private static ImageWriter getWriter(ArgumentHandler argHandler) throws IOException {
        if (argHandler.getOutFileExtension().equals("propra"))
            return new ProPraWriter(new FileOutputStream(argHandler.getOutFile()));

        return new ImageWriter(new FileOutputStream(argHandler.getOutFile()));
    }

    /**
     * converts the read header into the suitable output header.
     *
     * @param inputHeader header from input file.
     * @param argHandler  ArgumentHandler, that contains both paths to input and output file.
     * @param tree        Huffman tree for output header (in case of conversion to huffman compressed propra file.
     *                    Otherwise this is <code>null</code>
     * @return output header.
     * @throws InvalidImageException if constructed output header is invalid.
     */
    private static ImageHeader convertHeader(ImageHeader inputHeader, ArgumentHandler argHandler, Node tree) throws InvalidImageException {
        short imgWidth = inputHeader.getImgWidth();
        short imgHeight = inputHeader.getImgHeight();
        byte pixelDepth = inputHeader.getPixelDepth();
        Compression compression;
        switch (argHandler.getWorkMode()) {
            case ConvertRLE:
                compression = Compression.RLE;
                break;
            case ConvertHuffman:
                compression = Compression.Huffman;
                break;
            default:
                compression = Compression.Uncompressed;
        }

        if (argHandler.getOutFileExtension().equals("tga")) {
            byte imageIDLength = 0;
            byte imageType = (byte) (argHandler.getWorkMode() == WorkMode.ConvertRLE ? 10 : 2);
            short xOrigin = 0;
            short yOrigin = inputHeader.getImgHeight();
            byte imgDescriptor = 0x20;

            return new TGAImageHeader(imageIDLength, imageType, xOrigin, yOrigin, imgWidth, imgHeight, pixelDepth, imgDescriptor, compression);
        } else {
            String magic = "ProPraWS19";
            long dataSegmentSize = imgWidth * imgHeight * (pixelDepth / 8);
            // Checksum will be 0 first, as we cannot set the real checksum, before all pixels are read
            int checksum = 0;

            return new ProPraImageHeader(magic, imgWidth, imgHeight, pixelDepth, compression, dataSegmentSize, checksum, tree);
        }
    }

    /**
     * Converts the byteCount array into an ArrayList of Nodes, that only contains nodes with a weight.
     *
     * @param byteCount array to be converted.
     * @return ArrayList of Nodes.
     */
    public static ArrayList<Node> byteCountToNodeList(int[] byteCount) {
        ArrayList<Node> list = new ArrayList<>();

        for (int i = 0; i < byteCount.length; i++) {
            if (byteCount[i] > 0) {
                list.add(new Node((byte) i, byteCount[i]));
            }
        }

        return list;
    }
}
