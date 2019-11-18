package propra.imageconverter;

import propra.imageconverter.exceptions.InvalidEncodingException;
import propra.imageconverter.handler.ByteHandler;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;

public class BaseN {

    /**
     * ALphabet used for en-/decoding
     */
    private String alphabet;

    /**
     * Switch to identify whether we currently use base-32 hex encoding, or a custom base-n encoding.
     */
    private boolean base32Hex = false;

    /**
     * Constructs the default Base N Encoder, that encodes in base 32 hex
     * with alphabet <code>0123456789ABCDEFGHIJKLMNOPQRSTUV</code>.
     * Has a switch to identify if this should really be base-32 hex encoding, or not.
     * This constructor is mainly used for base-n decoding, because during creation of the object,
     * the alphabet is not already present, and will be set later.
     *
     * @throws InvalidEncodingException if the length of the alphabet is not a power of two.
     */
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
        this.setAlphabet(alphabet);
    }

    /**
     * Returns the alphabet for en-/decoding.
     *
     * @return alphabet
     */
    public String getAlphabet() {
        return alphabet;
    }

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

    /**
     * Sets the alphabet for en-/decoding.
     *
     * @param alphabet alphabet to be set.
     */
    public void setAlphabet(String alphabet) throws InvalidEncodingException {
        if (!checkLength(alphabet.length())) {
            throw new InvalidEncodingException("Length of encoding alphabet has to be a power of two and cannot be more than 64");
        }
        this.alphabet = alphabet;
    }

    /**
     * Returns if the encoder is a base-32 hex encoder.
     *
     * @return base32Hex.
     */
    public boolean isBase32Hex() {
        return base32Hex;
    }

    /**
     * Converts the alphabet into a hash map, which is used for decoding,
     * to identify the index in the alphabet quicker.
     *
     * @return hash map representing alphabet with character as key and index as value.
     */
    public HashMap<Character, Integer> getAlphabetMap() {
        HashMap<Character, Integer> alphabetMap = new HashMap<>();

        for (int i = 0; i < alphabet.length(); i++) {
            alphabetMap.put(alphabet.charAt(i), i);
        }

        return alphabetMap;
    }

    /**
     * Returns number of bits that can be represented by one character of the alphabet.
     *
     * @return bits.
     */
    public int getBits() {
        int alphabetLength = alphabet.length();
        return (int) (Math.log(alphabetLength) / Math.log(2));
    }

    /**
     * Checks length of input alphabet.
     *
     * @param length length of alphabet.
     * @return true, if if it's a power of two and not greater than 64.
     */
    private boolean checkLength(int length) {
        return (length & (length - 1)) == 0 && length <= 64;
    }

    /**
     * Encodes the given byte array.
     *
     * @param bytes bytes to be encoded.
     * @return encoded String.
     */
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

    /**
     * Decodes a char array.
     *
     * @param chars char array to be decoded.
     * @return array of decoded bytes.
     * @throws InvalidEncodingException if there happened an error during decoding.
     */
    public byte[] decode(char[] chars) throws InvalidEncodingException {
        return decode(new String(chars));
    }

    /**
     * Decodes a String.
     *
     * @param string string to be decoded.
     * @return array of decoded bytes.
     * @throws InvalidEncodingException if there happened an error during decoding.
     */
    public byte[] decode(String string) throws InvalidEncodingException {
        ByteBuffer buf = ByteBuffer.allocate(this.getNumOutputBytes(string));
        int bits = this.getBits();
        HashMap<Character, Integer> alphabetMap = this.getAlphabetMap();

        do {
            int processTo = Math.min(string.length(), this.maxInputCharacters());

            long decodedLong = 0;
            for (int i = 0; i < processTo; i++) {
                Integer index = alphabetMap.get(string.charAt(i));
                if (index != null) {
                    decodedLong = (decodedLong << bits) + index;
                } else {
                    throw new InvalidEncodingException("File contains a character, that is not part of the alphabet.");
                }
            }

            // Shift decoded bits to the left most position in long
            int shiftOperand = 64 - bits * processTo;
            decodedLong <<= shiftOperand;

            int bytesInLong = Math.min(maxInputBytes(), this.getNumOutputBytes(string));
            byte[] decodedBytes = Arrays.copyOf(ByteHandler.longToByteArray(decodedLong, ByteOrder.BIG_ENDIAN), bytesInLong);
            buf.put(decodedBytes);

            string = string.substring(processTo);
        } while (string.length() > 0);

        // convert long back to byte array and trim that array to the calculated value of output bytes.
        return buf.array();
    }

    /**
     * Maximum amount of input bytes that can be handled with a long for encoding.
     *
     * @return number of bytes.
     */
    public int maxInputBytes() {
        int[] numBytes = new int[]{8, 8, 6, 8, 5, 6};

        return numBytes[this.getBits() - 1];
    }

    /**
     * Maximum amount of input characters that can be handled with a long for decoding.
     *
     * @return number of characters.
     */
    public int maxInputCharacters() {
        return (int) Math.ceil(this.maxInputBytes() * 8F / this.getBits());
    }

    /**
     * Calculates, how many characters will be returned while encoding, depending on the number of input bytes.
     *
     * @param bytes input bytes.
     * @return number of output characters.
     */
    private int getNumOutputCharacter(byte[] bytes) {
        return (int) Math.ceil(bytes.length * 8F / this.getBits());
    }

    /**
     * Calculates, how many bytes will be returned while decoding, depending on the length of input string.
     *
     * @param string input string.
     * @return number or output bytes.
     */
    private int getNumOutputBytes(String string) {
        return (int) Math.floor(string.length() / 8F * this.getBits());
    }

    /**
     * Calculates the mask, that is used for encoding.
     * That mask represents the bits in an int, that will be encoded.
     *
     * @return mask used for encoding.
     */
    private int getMask() {
        int mask = 0;
        int bits = this.getBits();
        for (int i = 0; i < bits; i++) {
            mask = (mask << 1) + 1;
        }

        return mask;
    }
}
