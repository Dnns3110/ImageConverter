package propra.imageconverter;

import propra.imageconverter.handler.ByteHandler;

import java.io.*;

public class ProPraImage implements Image {

//    public static final String INPUT = "Lorem ipsum dolor sit amet, consectetur adipisici elit, sed eiusmod tempor incidunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquid ex ea commodi consequat. Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.";

    private short imgWidth;
    private short imgHeight;
    private byte pixelDepth;
    private byte compressionType;
    private long dataSegmentSize;
    private int checkSum;
    private byte[][][] imgData;

    public ProPraImage(short imgWidth, short imgHeight, byte pixelDepth, byte compressionType, byte[][][] imgData) {
        this.imgWidth = imgWidth;
        this.imgHeight = imgHeight;
        this.pixelDepth = pixelDepth;
        this.compressionType = compressionType;
        this.dataSegmentSize = imgHeight * imgWidth * (pixelDepth / 8);
        this.imgData = imgData;
        this.checkSum = calcChecksum();
    }

    public ProPraImage(String filePath) {
        loadImageFromFile(filePath);
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

    public byte getCompressionType() {
        return compressionType;
    }

    public void setCompressionType(byte compressionType) {
        this.compressionType = compressionType;
    }

    public long getDataSegmentSize() {
        return dataSegmentSize;
    }

    public void setDataSegmentSize(long dataSegmentSize) {
        this.dataSegmentSize = dataSegmentSize;
    }

    public int getCheckSum() {
        return checkSum;
    }

    public void setCheckSum(int checkSum) {
        this.checkSum = checkSum;
    }

    public byte[][][] getImgData() {
        return imgData;
    }

    public void setImgData(byte[][][] imgData) {
        this.imgData = imgData;
    }

    public int getImgData(long i) {
        int d = (int) (i) % 3;
        int w = (int) ((i / 3) % this.getImgWidth());
        int h = (int) (i / 3 / this.getImgWidth());

        return this.imgData[h][w][d] & 0xFF;
    }

    public void loadImageFromFile(String filePath) {
        File inFile = new File(filePath);
        FileInputStream fis;
        byte[] fileHeader = new byte[28];
        byte[] pixel = new byte[3];
        byte[][][] imgData;
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

            // TODO Adjust comment
            // Handle just read File Header. We don't care about the first eight Bytes. ImageID has always length 0,
            // we don't use a color map, Image Type is always 2 for uncompressed true-color images and Color Map
            // Specification is irrelevant, as we don't use a color map.
            // From the Image Descriptor we only need bits 5 and 6.
            if (!ByteHandler.byteArrayToString(fileHeader, 10).equals("ProPraWS19")) {
                //TODO Fehlermeldung, weil beginnt nich mit ProPraWS19.
                System.out.println("LOOOL");
            }

            this.setImgWidth(ByteHandler.byteArrayToShort(fileHeader, 0x0A));
            this.setImgHeight(ByteHandler.byteArrayToShort(fileHeader, 0x0C));
            this.setPixelDepth(fileHeader[0x0E]);
            this.setCompressionType(fileHeader[0x0F]);
            this.setDataSegmentSize(ByteHandler.byteArrayToLong(fileHeader, 0x10));
            this.setCheckSum(ByteHandler.byteArrayToInt(fileHeader, 0x18));

            imgData = new byte[this.getImgHeight()][this.getImgWidth()][3];
            int ctr = 0x1C;
            int ctr2 = 0;

            while ((readRet = fis.read(pixel)) == 3 && rowCtr < this.getImgHeight()) {
                imgData[rowCtr][colCtr] = pixel;
//                if (ctr < 0x20) System.out.println(String.format("0x%08X [%d][%d]: 0x%02X 0x%02X 0x%02X", ctr, rowCtr, colCtr, pixel[0], pixel[1], pixel[2]));
//                if (ctr > 0x3F8C0 && ctr < 0x3F8F0) System.out.println(String.format("0x%08X %d [%d][%d]: 0x%02X 0x%02X 0x%02X", ctr, ctr2, rowCtr, colCtr, pixel[0], pixel[1], pixel[2]));
                pixel = new byte[3];

                ctr += 3;
                ctr2 += 3;

                if (++colCtr == this.getImgWidth()) {
                    rowCtr++;
                    colCtr = 0;
                }
            }

            this.setImgData(imgData);
            // TODO check readRet, or if there is still anything to be read
            // TODO Calculate Checksum and verify against read one.
            System.out.println(String.format("Checksum: 0x%08X", this.checkSum));
            System.out.println(String.format("Calculated Checksum: 0x%08X", calcChecksum()));

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
        short xOrigin = 0;
        short yOrigin = this.getImgHeight();
        byte imgDescriptor = 0x20;

        for (byte[][] col : this.getImgData()) {
            for (byte[] pixel : col) {
                // Swap G and B, R stays at the same position.
                byte g = pixel[0];
                pixel[0] = pixel[1];
                pixel[1] = g;
            }
        }

        return new TGAImage(xOrigin, yOrigin, this.getImgWidth(), this.getImgHeight(), this.getPixelDepth(), imgDescriptor, this.getImgData());
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
            fos.write("ProPraWS19".getBytes());
            fos.write(ByteHandler.shortToByteArray(this.getImgWidth()));
            fos.write(ByteHandler.shortToByteArray(this.getImgHeight()));
            fos.write(this.getPixelDepth());
            fos.write(this.getCompressionType());
            fos.write(ByteHandler.longToByteArray(this.getDataSegmentSize()));
            fos.write(ByteHandler.intToByteArray(this.getCheckSum()));

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

    public int calcChecksum() {
        long n = this.getDataSegmentSize();
        int x = 65513;
        int y = 65536;
        long a = 0;
        long b = 1;

        for (int i = 1; i <= n; i++) {
            a += i + getImgData(i - 1);
            b = (b + a % x) % x;
        }

        return (int) ((a % x * y) + b);
    }

}

