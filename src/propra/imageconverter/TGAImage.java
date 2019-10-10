package propra.imageconverter;

import propra.imageconverter.handler.ByteHandler;

import java.io.*;

public class TGAImage implements Image {

    private short xOrigin;
    private short yOrigin;
    private short imgWidth;
    private short imgHeight;
    private byte pixelDepth;
    private byte imgDescriptor;
    //private byte originDirectionH;
    //private byte originDirectionV;
    private byte[][][] imgData;

    public TGAImage(short xOrigin, short yOrigin, short imgWidth, short imgHeight, byte pixelDepth, byte imgDescriptor, byte[][][] imgData) {
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.pixelDepth = pixelDepth;
        this.imgDescriptor = imgDescriptor;
        this.imgData = imgData;
    }

    public TGAImage(String filePath) {
        loadImageFromFile(filePath);
        System.out.println(1);
    }

    public short getxOrigin() {
        return xOrigin;
    }

    public void setxOrigin(short xOrigin) {
        this.xOrigin = xOrigin;
    }

    public short getyOrigin() {
        return yOrigin;
    }

    public void setyOrigin(short yOrigin) {
        this.yOrigin = yOrigin;
    }

    public short getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(short imgWidth) {
        this.imgWidth = imgWidth;
    }

    public short getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(short imgHeight) {
        this.imgHeight = imgHeight;
    }

    public byte getPixelDepth() {
        return pixelDepth;
    }

    public void setPixelDepth(byte pixelDepth) {
        this.pixelDepth = pixelDepth;
    }

    public byte getImgDescriptor() {
        return imgDescriptor;
    }

    public void setImgDescriptor(byte imgDescriptor) {
        this.imgDescriptor = imgDescriptor;
    }

    public byte[][][] getImgData() {
        return imgData;
    }

    public void setImgData(byte[][][] imgData) {
        this.imgData = imgData;
    }

    public void loadImageFromFile(String filePath) {
        File inFile = new File(filePath);
        FileInputStream fis;
        byte[] fileHeader = new byte[18];
        byte pixel[] = new byte[3];
        byte imgData[][][];
        int colCtr = 0;
        int rowCtr = 0;
        int readRet;

        if (!inFile.isFile()) {
            System.out.println("Please check your input file. The file does either not exist, or is no file.");
            System.exit(1337);
        } else if (!inFile.canRead()) {
            System.out.println("Please check your input file. The file is not readable.");
            System.exit(1337);
        }

        try {
            fis = new FileInputStream(inFile);
            // TODO Check file long enough for at least this
            fis.read(fileHeader);

            // Handle just read File Header. We don't care about the first eight Bytes. ImageID has always length 0,
            // we don't use a color map, Image Type is always 2 for uncompressed true-color images and Color Map
            // Specification is irrelevant, as we don't use a color map.
            // From the Image Descriptor we only need bits 5 and 6.
            this.setxOrigin(ByteHandler.byteArrayToShort(fileHeader, 0x08));
            this.setyOrigin(ByteHandler.byteArrayToShort(fileHeader, 0x0A));
            this.setImgWidth(ByteHandler.byteArrayToShort(fileHeader, 0x0C));
            this.setImgHeight(ByteHandler.byteArrayToShort(fileHeader, 0x0E));
            this.setPixelDepth(fileHeader[0x10]);
            this.setImgDescriptor(fileHeader[0x11]);

            imgData = new byte[this.getImgHeight()][this.getImgWidth()][3];
            while ((readRet = fis.read(pixel)) == 3 && rowCtr < this.getImgHeight()) {
                imgData[rowCtr][colCtr] = pixel;
                pixel = new byte[3];

                if (++colCtr == this.getImgWidth()) {
                    rowCtr++;
                    colCtr = 0;
                }
            }

            this.setImgData(imgData);
            // TODO check readRet, or if there is still anything to be read

            System.out.println(1);
        } catch (FileNotFoundException e) {
            // Should usually not happen, as we checked before, that this file does exist.
            e.printStackTrace();
        } catch (IOException e) {
            //TODO Handle IOException
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            //TODO Handle Too large Files... show error
            e.printStackTrace();
            System.exit(1337);
        }
    }

    @Override
    public Image convert() {
        byte compressionType = 0x0;
        for (byte[][] col : this.getImgData()) {
            for (byte[] pixel : col) {
                // Swap G and B, R stays at the same position.
                byte g = pixel[0];
                pixel[0] = pixel[1];
                pixel[1] = g;
            }
        }

        return new ProPraImage(this.imgWidth, this.imgHeight, this.pixelDepth, compressionType, this.getImgData());
    }

    @Override
    public void save(String filePath) {
        File outFile = new File(filePath);
        FileOutputStream fos = null;

        // Create (if necessary) path to outfile
        if (!outFile.getParentFile().exists()) {
            outFile.getParentFile().mkdirs();
        }

        try {
            fos = new FileOutputStream(outFile);

            // Write first 8 Bytes, that are fixed values in our case
            fos.write(new byte[]{0x0, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0, 0x0});
            fos.write(ByteHandler.shortToByteArray(this.getxOrigin()));
            fos.write(ByteHandler.shortToByteArray(this.getyOrigin()));
            fos.write(ByteHandler.shortToByteArray(this.getImgWidth()));
            fos.write(ByteHandler.shortToByteArray(this.getImgHeight()));
            fos.write(this.getPixelDepth());
            fos.write(this.getImgDescriptor());

            for (byte[][] col : this.getImgData()) {
                for (byte[] pixel : col) {
                    fos.write(pixel);
                }
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
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                System.exit(1337);
                e.printStackTrace();
            }
        }
    }
}
