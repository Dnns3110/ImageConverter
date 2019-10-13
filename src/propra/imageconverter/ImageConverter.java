package propra.imageconverter;

import propra.imageconverter.exceptions.ImageConverterIllegalArgumentException;
import propra.imageconverter.handler.ArgumentHandler;
import propra.imageconverter.images.Image;
import propra.imageconverter.images.ProPraImage;
import propra.imageconverter.images.TGAImage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * ImageConverter is a Program, that can be used to convert images between TGA and ProPra format.
 */
public class ImageConverter {

    public static void main(String[] args) {
        try {
            ArgumentHandler argHandler = new ArgumentHandler(args);
            if (argHandler.getInFileExtension().equals(argHandler.getOutFileExtension())) {
                copyFile(argHandler);
            } else {
                convertFile(argHandler);
            }
        } catch (ImageConverterIllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(123);
        }
    }

    /**
     * Copies the passed input file into the specified output file.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void copyFile(ArgumentHandler argHandler) {
        File inFile = new File(argHandler.getInFile());
        File outFile = new File(argHandler.getOutFile());

        System.out.println(String.format("Copy File %s -> %s", inFile.getAbsolutePath(), outFile.getAbsolutePath()));

        try {
            Files.copy(inFile.toPath(), outFile.toPath());
        } catch (IOException e) {
            System.err.println("Unexpected error occurred during copy process:\n" + e.toString());
            System.exit(123);
        }

        System.out.println("Copied file successfully");
    }

    /**
     * Convert file from either TGA format to ProPra or vice versa.
     *
     * @param argHandler ArgumentHandler, that contains both paths to input and output file.
     */
    public static void convertFile(ArgumentHandler argHandler) {
        System.out.println(String.format("Convert File %s -> %s", argHandler.getInFile(), argHandler.getOutFile()));

        // In ArgumentHandler we verified the files, that they can only be in propra or tga format. Therefore we can
        // say for sure, that if the extension is not tga, it must be propra.
        try {
            Image img = argHandler.getInFileExtension().equals("tga") ? new TGAImage(argHandler.getInFile())
                    : new ProPraImage(argHandler.getInFile());

            System.out.println("Loaded image into memory. Start conversion now.");
            Image converted = img.convert();

            System.out.println("Save image to output file.");
            converted.save(argHandler.getOutFile());
        } catch (Exception e) {
            System.err.println("Unexpected error occurred during conversion process:\n" + e.toString());
            System.exit(123);
        }

        System.out.println("Conversion finished successfully");
    }
}
