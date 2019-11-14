package propra.imageconverter;

import propra.imageconverter.exceptions.InvalidEncodingException;
import propra.imageconverter.handler.ByteHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

public class BaseN {

    private String alphabet;
    private boolean base32Hex = false;

    /**
     * Constructs the default Base N Encoder, that encodes in base 32 hex
     * with alphabet <code>0123456789ABCDEFGHIJKLMNOPQRSTUV</code>.
     *
     * @throws InvalidEncodingException if the length of the alphabet is not a power of two.
     */
    public BaseN() throws InvalidEncodingException {
        this("0123456789ABCDEFGHIJKLMNOPQRSTUV");
        this.base32Hex = true;
    }

    public BaseN(boolean base32Hex) throws InvalidEncodingException {
        this("0123456789ABCDEFGHIJKLMNOPQRSTUV");
        this.base32Hex = base32Hex;
    }

    /**
     * Constructs a Base N Encoder with custom alphabet.
     *
     * @param alphabet alphabet that is used for this encoder.
     * @throws InvalidEncodingException if the length of the alphabet is not a power of two.
     */
    public BaseN(String alphabet) throws InvalidEncodingException {
        if (!checkLength(alphabet.length())) {
            throw new InvalidEncodingException("Length of encoding alphabet has to be a power of two and cannot be more than 64");
        }

        this.alphabet = alphabet;
    }

    public String getAlphabet() {
        return alphabet;
    }

    public void setAlphabet(String alphabet) {
        this.alphabet = alphabet;
    }

    public HashMap<Character, Integer> getAlphabetMap() {
        HashMap<Character, Integer> alphabetMap = new HashMap<>();

        for (int i = 0; i < alphabet.length(); i++) {
            alphabetMap.put(alphabet.charAt(i), i);
        }

        return alphabetMap;
    }

    public int getBits() {
        int alphabetLength = alphabet.length();
        return (int) (Math.log(alphabetLength) / Math.log(2));
    }

    public boolean isBase32Hex() {
        return base32Hex;
    }

    private boolean checkLength(int length) {
        // Check, if the length of alphabet is a power of two.
        return (length & (length - 1)) == 0 && length <= 64;
    }

    public String encode(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        long toEncode = ByteHandler.byteArrayToLong(bytes, 0, ByteOrder.BIG_ENDIAN);
        int numOutputCharacter = this.getNumOutputCharacter(bytes);
        int mask = getMask();
        int bits = this.getBits();
        int offset = Long.BYTES * 8 - bits;

        do {
            int processTo = Math.min(numOutputCharacter, this.maxInputBytes());
            for (int i = 0; i < processTo; i++) {
                int index = (int) (toEncode >> offset) & mask;
                toEncode <<= bits;

                builder.append(this.alphabet.charAt(index));
            }

            numOutputCharacter -= processTo;
        } while (numOutputCharacter > 0);

        return builder.toString();
    }

    public byte[] decode(char[] s) throws InvalidEncodingException {
        return decode(new String(s));
    }

    public byte[] decode(String s) throws InvalidEncodingException {
        ByteBuffer buf = ByteBuffer.allocate(this.getNumOutputBytes(s));
        int bits = this.getBits();
        HashMap<Character, Integer> alphabetMap = this.getAlphabetMap();

        do {
            int processTo = Math.min(s.length(), this.maxInputCharacters());

            long decodedLong = 0;
            for (int i = 0; i < processTo; i++) {
                Integer index = alphabetMap.get(s.charAt(i));
                if (index != null) {
                    decodedLong = (decodedLong << bits) + index;
                } else {
                    throw new InvalidEncodingException("File contains a character, that is not part of the alphabet.");
                }
            }

            // Shift decoded bits to the left most position in long
            int shiftOperand = 64 - bits * processTo;
            decodedLong <<= shiftOperand;

            int bytesInLong = Math.min(maxInputBytes(), this.getNumOutputBytes(s));
            byte[] decodedBytes = Arrays.copyOf(ByteHandler.longToByteArray(decodedLong, ByteOrder.BIG_ENDIAN), bytesInLong);
            buf.put(decodedBytes);

            s = s.substring(processTo);
        } while (s.length() > 0);

        // convert long back to byte array and trim that array to the calculated value of output bytes.
        return buf.array();
    }

    public int maxInputBytes() {
        int[] numBytes = new int[]{8, 8, 6, 8, 5, 6};

        return numBytes[this.getBits() - 1];
    }

    public int maxInputCharacters() {
        return (int) Math.ceil(this.maxInputBytes() * 8F / this.getBits());
    }

    private int getNumOutputCharacter(byte[] bytes) {
        return (int) Math.ceil(bytes.length * 8F / this.getBits());
    }

    private int getNumOutputBytes(String s) {
        return (int) Math.floor(s.length() / 8F * this.getBits());
    }

    private int getMask() {
        int mask = 0;
        int bits = this.getBits();
        for (int i = 0; i < bits; i++) {
            mask = (mask << 1) + 1;
        }

        return mask;
    }
}
