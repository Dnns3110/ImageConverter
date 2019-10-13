package propra.imageconverter.handler;

import java.nio.ByteBuffer;

/**
 * Byte Handler can be used to convert between byte arrays and other primitive data types or Strings.
 */
public class ByteHandler {

    /**
     * Converts the given byte array into a string.
     *
     * @param bytes bytes to be converted.
     * @return String represented by the given byte array.
     */
    public static String byteArrayToString(byte[] bytes) {
        return ByteHandler.byteArrayToString(bytes, bytes.length, 0);
    }

    /**
     * Converts len number of elements from the given byte array into a string.
     * If len &gt; length of bytes, spaces will get added at the end of the String.
     *
     * @param bytes bytes to be converted.
     * @param len   number of elements from byte array.
     * @return String of length len represented by the given byte array.
     */
    public static String byteArrayToString(byte[] bytes, int len) {
        return ByteHandler.byteArrayToString(bytes, len, 0);
    }

    /**
     * Converts len number of elements starting at offset from the given byte array into a string.
     * If offset + len &gt; length of bytes, spaces will get added at the end of the String.
     *
     * @param bytes  bytes to be converted.
     * @param len    number of elements from byte array.
     * @param offset start at offset.
     * @return String of length len represented by the given byte array, starting at offset.
     */
    public static String byteArrayToString(byte[] bytes, int len, int offset) {
        String s = "";

        for (int i = offset; i < offset + len; i++) {
            if (i < bytes.length) s += (char) bytes[i];
            else s += " ";
        }

        return s;
    }

    /**
     * Converts a byte array, starting at offset, into a short.
     * @param bytes byte array to be converted.
     * @param offset start at offset.
     * @return short represented by given byte array, starting at offset.
     */
    public static short byteArrayToShort(byte[] bytes, int offset) {
        int len = Short.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        int index = len - 1;

        for (int i = offset; i < offset + len; i++) {
            if (i < offset + buf.limit()) {
                buf.put(index, bytes[i]);
                index--;
            }
        }

        return buf.getShort();
    }

    /**
     * Converts a byte array, starting at offset, into an int.
     * @param bytes byte array to be converted.
     * @param offset start at offset.
     * @return int represented by given byte array, starting at offset.
     */
    public static int byteArrayToInt(byte[] bytes, int offset) {
        int len = Integer.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        int index = len - 1;

        for (int i = offset; i < offset + len; i++) {
            if (i < offset + buf.limit()) {
                buf.put(index, bytes[i]);
                index--;
            }
        }

        return buf.getInt();
    }

    /**
     * Converts a byte array, starting at offset, into a long.
     * @param bytes byte array to be converted.
     * @param offset start at offset.
     * @return long represented by given byte array, starting at offset.
     */
    public static long byteArrayToLong(byte[] bytes, int offset) {
        int len = Long.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        int index = len - 1;

        for (int i = offset; i < offset + len; i++) {
            if (i < offset + buf.limit()) {
                buf.put(index, bytes[i]);
                index--;
            }
        }

        return buf.getLong();
    }

    /**
     * Converts a short into a byte array.
     * @param s short to be converted.
     * @return byte array that represents the passed short.
     */
    public static byte[] shortToByteArray(short s) {
        int len = Short.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putShort(s);

        return reverseArray(buf.array());
    }

    /**
     * Converts an int into a byte array.
     * @param i short to be converted.
     * @return byte array that represents the passed int.
     */
    public static byte[] intToByteArray(int i) {
        int len = Integer.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putInt(i);

        return reverseArray(buf.array());
    }

    /**
     * Converts a long into a byte array.
     * @param l short to be converted.
     * @return byte array that represents the passed long.
     */
    public static byte[] longToByteArray(long l) {
        int len = Long.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putLong(l);

        return reverseArray(buf.array());
    }

    /**
     * Reverse the order of bytes in given byte array.
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