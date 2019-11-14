package propra.imageconverter.handler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Byte Handler can be used to convert between byte arrays and other primitive data types.
 * All handled byte Arrays are in Little Endian format.
 */
public class ByteHandler {

    /**
     * Converts a byte array, starting at offset, into a short with byte order Little Endian.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @return short represented by given byte array, starting at offset.
     */
    public static short byteArrayToShort(byte[] bytes, int offset) {
        return byteArrayToShort(bytes, offset, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a byte array, starting at offset, into a short.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return short represented by given byte array, starting at offset.
     */
    public static short byteArrayToShort(byte[] bytes, int offset, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.allocate(Short.BYTES);
        int length = Math.min(bytes.length - offset, buf.capacity());
        buf.order(byteOrder);
        buf.put(bytes, offset, length);
        buf.rewind();

        return buf.getShort();
    }

    /**
     * Converts a byte array, starting at offset, into an int with byte order Little Endian.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @return int represented by given byte array, starting at offset.
     */
    public static int byteArrayToInt(byte[] bytes, int offset) {
        return byteArrayToInt(bytes, offset, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a byte array, starting at offset, into an int.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return int represented by given byte array, starting at offset.
     */
    public static int byteArrayToInt(byte[] bytes, int offset, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES);
        int length = Math.min(bytes.length - offset, buf.capacity());
        buf.order(byteOrder);
        buf.put(bytes, offset, length);
        buf.rewind();

        return buf.getInt();
    }


    /**
     * Converts a byte array, starting at offset, into a long with byte order Little Endian.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @return long represented by given byte array, starting at offset.
     */
    public static long byteArrayToLong(byte[] bytes, int offset) {
        return byteArrayToLong(bytes, offset, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a byte array, starting at offset, into a long.
     *
     * @param bytes     byte array to be converted.
     * @param offset    start at offset.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return long represented by given byte array, starting at offset.
     */
    public static long byteArrayToLong(byte[] bytes, int offset, ByteOrder byteOrder) {
        ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
        int length = Math.min(bytes.length - offset, buf.capacity());
        buf.order(byteOrder);
        buf.put(bytes, offset, length);
        buf.rewind();

        return buf.getLong();
    }

    /**
     * Converts a short into a byte array with byte order Little Endian.
     *
     * @param s         short to be converted.
     * @return byte array that represents the passed short.
     */
    public static byte[] shortToByteArray(short s) {
        return shortToByteArray(s, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a short into a byte array.
     *
     * @param s         short to be converted.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return byte array that represents the passed short.
     */
    public static byte[] shortToByteArray(short s, ByteOrder byteOrder) {
        int len = Short.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putShort(s);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return reverseArray(buf.array());
        }

        return buf.array();
    }

    /**
     * Converts an int into a byte array with byte order Little Endian.
     *
     * @param i         short to be converted.
     * @return byte array that represents the passed int.
     */
    public static byte[] intToByteArray(int i) {
        return intToByteArray(i, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts an int into a byte array.
     *
     * @param i         short to be converted.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return byte array that represents the passed int.
     */
    public static byte[] intToByteArray(int i, ByteOrder byteOrder) {
        int len = Integer.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putInt(i);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return reverseArray(buf.array());
        }

        return buf.array();
    }

    /**
     * Converts a long into a byte array with byte order Little Endian.
     *
     * @param l short to be converted.
     * @return byte array that represents the passed long.
     */
    public static byte[] longToByteArray(long l) {
        return longToByteArray(l, ByteOrder.LITTLE_ENDIAN);
    }

    /**
     * Converts a long into a byte array.
     *
     * @param l         short to be converted.
     * @param byteOrder byte order of <code>bytes</code> in short.
     * @return byte array that represents the passed long.
     */
    public static byte[] longToByteArray(long l, ByteOrder byteOrder) {
        int len = Long.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putLong(l);

        if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            return reverseArray(buf.array());
        }

        return buf.array();
    }

    /**
     * Reverse the order of bytes in given byte array.
     *
     * @param bytes bytes to be reversed.
     * @return byte array in reversed order.
     */
    public static byte[] reverseArray(byte[] bytes) {
        byte[] reversed = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            reversed[i] = bytes[bytes.length - i - 1];
        }

        return reversed;
    }
}