package propra.imageconverter;

import propra.imageconverter.handler.ArgumentHandler;

import java.io.*;

public class ImageConverter {

    public static void main(String[] args) {

        int h = 13;
        int w = 14;
        int d = 3;

//        for (int i = 0; i < h; i++)
//            for (int j = 0; j < w; j++)
//                for (int k = 0; k < d; k++)
//                    System.out.println(String.format("%d;%d;%d", i, j, k));

        ArgumentHandler argHandler = new ArgumentHandler(args);

        if (argHandler.getInFileExtension().equals(argHandler.getOutFileExtension())) {
            copyFile(argHandler);
        } else {
            convertFile(argHandler);
        }
    }


    public static void copyFile(ArgumentHandler argHandler) {
        File inFile = new File(argHandler.getInFile());
        File outFile = new File(argHandler.getOutFile());
        FileInputStream fis = null;
        FileOutputStream fos = null;
        int buffer;

        // Verify Infile
        if (!inFile.isFile()) {
            System.out.println("Please check your input file. The file does either not exist, or is no file.");
            System.exit(1337);
        } else if (!inFile.canRead()) {
            System.out.println("Please check your input file. The file is not readable.");
            System.exit(1337);
        }

        // Create (if necessary) path to outfile
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        // Copy File
        try {
            fis = new FileInputStream(inFile);
            fos = new FileOutputStream(outFile);

            while ((buffer = fis.read()) != -1) {
                fos.write(buffer);
            }

        } catch (FileNotFoundException e) {
            // Should not happen due to previous checks
            e.printStackTrace();
        } catch (IOException e) {
            System.exit(1337);
            // TODO Joa da muss noch n bisschen, wa?
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }

                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                System.exit(1337);
                e.printStackTrace();
            }
        }
    }

    public static void convertFile(ArgumentHandler argHandler) {
        Image img = argHandler.getInFileExtension().equals("tga") ? new TGAImage(argHandler.getInFile())
                : new ProPraImage(argHandler.getInFile());

        Image converted = img.convert();
        converted.save(argHandler.getOutFile());
    }
}
