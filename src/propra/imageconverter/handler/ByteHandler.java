package propra.imageconverter.handler;

import java.nio.ByteBuffer;

public class ByteHandler {

    public static String byteArrayToString(byte[] bytes) {
        return ByteHandler.byteArrayToString(bytes, bytes.length, 0);
    }

    public static String byteArrayToString(byte[] bytes, int len) {
        return ByteHandler.byteArrayToString(bytes, len, 0);
    }

    public static String byteArrayToString(byte[] bytes, int len, int offset) {
        String s = "";

        for (int i = offset; i < offset + len; i++) {
            if (i < bytes.length) s += (char) bytes[i];
        }

        return s;
    }

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

    public static byte[] shortToByteArray(short s) {
        int len = Short.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putShort(s);

        return reverseArray(buf.array());
    }

    public static byte[] intToByteArray(int i) {
        int len = Integer.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putInt(i);

        return reverseArray(buf.array());
    }

    public static byte[] longToByteArray(long l) {
        int len = Long.BYTES;
        ByteBuffer buf = ByteBuffer.allocate(len);
        buf.putLong(l);

        return reverseArray(buf.array());
    }

    public static byte[] reverseArray(byte[] bytes) {
        for (int i = 0; i < bytes.length / 2; i++) {
            byte b = bytes[i];
            bytes[i] = bytes[bytes.length - i - 1];
            bytes[bytes.length - i - 1] = b;
        }

        return bytes;
    }
}