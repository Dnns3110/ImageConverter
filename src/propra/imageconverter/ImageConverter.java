package propra.imageconverter;

import propra.imageconverter.exceptions.InvalidImageException;
import propra.imageconverter.handler.ArgumentHandler;
import propra.imageconverter.handler.ByteHandler;
import propra.imageconverter.imageheader.ImageHeader;
import propra.imageconverter.imageheader.ProPraImageHeader;
import propra.imageconverter.imageheader.TGAImageHeader;
import propra.imageconverter.reader.ImageReader;
import propra.imageconverter.reader.ProPraReader;
import propra.imageconverter.reader.TGAReader;

import java.io.*;
import java.util.Arrays;

/**
 * ImageConverter is a Program, that can be used to convert images between TGA and ProPra format.
 */
public class ImageConverter {

    public static void main(String[] args) {
        ArgumentHandler argHandler = null;
        try {
            argHandler = new ArgumentHandler(args);
            if (argHandler.getWorkMode() == WorkMode.Encode) {
                encodeFile(argHandler);
            } else if (argHandler.getWorkMode() == WorkMode.Decode) {
                decodeFile(argHandler);
            } else {
                convertFile(argHandler);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
            System.exit(123);
        }


    }

    /**
     * Convert file from either TGA format to ProPra or vice versa.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void convertFile(ArgumentHandler argHandler) {
        System.out.println(String.format("Convert File %s -> %s", argHandler.getInFile(), argHandler.getOutFile()));
        ImageHeader inputHeader = null;
        ImageHeader outputHeader = null;
        Checksum checksum = new Checksum(ProPraImageHeader.PIXEL_ORDER);

        try (ImageReader reader = getReader(argHandler);
             BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(argHandler.getOutFile()))) {

            System.out.println("Read/Write file header.");
            inputHeader = reader.readHeader();
            outputHeader = convertHeader(inputHeader, argHandler);
            writer.write(outputHeader.toByteArray());

            System.out.println("Convert image.");
            for (long i = 0; i < inputHeader.getImgHeight(); i++) {
                Pixel[] pixels = reader.readRow(inputHeader);

                if (pixels != null) {
                    for (Pixel pixel : pixels) {
                        byte[] convertedPixel = pixel.getPixel(outputHeader.getPixelOrder());
                        writer.write(convertedPixel);
                        checksum.add(pixel);
                    }
                } else {
                    throw new InvalidImageException("Less image data to read, than expected.");
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
                ((ProPraImageHeader) inputHeader).validateChecksum(checksum);
            }

            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Unexpected error occurred during conversion process:\n" + e.toString());
            System.exit(123);
        }

        // If we have a ProPra Image as output, we need to update the calculated checksum.
        if (outputHeader instanceof ProPraImageHeader) {
            try {
                updateChecksum(argHandler.getOutFile(), checksum);
            } catch (InvalidImageException e) {
                System.err.println("Unexpected error occurred during conversion process:\n" + e.toString());
                System.exit(123);
            }
        }

        System.out.println("Conversion finished successfully");
    }

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

    private static ImageReader getReader(ArgumentHandler argHandler) throws FileNotFoundException {
        // This application can (at the moment) only handle tga or propra images. And as we verified in ArgumentHandler, that
        // the input is either one of those formats, we know, that if the input file is not in tga format, it has to be in propra format instead.
        if (argHandler.getInFileExtension().equals("tga"))
            return new TGAReader(new FileInputStream(argHandler.getInFile()));

        return new ProPraReader(new FileInputStream(argHandler.getInFile()));
    }

    private static ImageHeader convertHeader(ImageHeader inputHeader, ArgumentHandler argHandler) throws InvalidImageException {
        short imgWidth = inputHeader.getImgWidth();
        short imgHeight = inputHeader.getImgHeight();
        byte pixelDepth = inputHeader.getPixelDepth();
        Compression compression = argHandler.getWorkMode() == WorkMode.ConvertRLE ? Compression.RLE : Compression.Uncompressed;

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

            return new ProPraImageHeader(magic, imgWidth, imgHeight, pixelDepth, compression, dataSegmentSize, checksum);
        }
    }

    private static void updateChecksum(File outFile, Checksum checksum) throws InvalidImageException {

        try (RandomAccessFile raf = new RandomAccessFile(outFile, "rw")) {
            long checksumPos = 24;
            raf.seek(checksumPos);
            raf.write(ByteHandler.intToByteArray(checksum.getChecksum()));
        } catch (IOException e) {
            throw new InvalidImageException(e.toString());
        }
    }
}
