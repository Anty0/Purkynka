package cz.anty.purkynkamanager.utils;

import java.nio.charset.Charset;

/**
 * Created by anty on 17.6.15.
 *
 * @author anty
 */
class ByteEncryption {

    private static final String LOG_TAG = "ByteEncryption";
    private static final byte[] MY_KEY = "Copyright ANTY 2015".getBytes(Charset.defaultCharset());

    public static String xorToByte(final String input) {
        byte[] bytes = xor(input.getBytes(Charset.defaultCharset()), MY_KEY);
        return java.util.Arrays.toString(bytes).replace("[", "")
                .replace("]", "").replace(",", "");
    }

    public static String xorFromByte(final String input) {
        try {
            if (input.equals("")) return "";
            String[] strings = input.split(" ");
            byte[] bytes = new byte[strings.length];
            for (int i = 0; i < strings.length; i++) {
                bytes[i] = Byte.parseByte(strings[i]);
            }
            return new String(xor(bytes, MY_KEY), Charset.defaultCharset());
        } catch (Exception e) {
            Log.d(LOG_TAG, "xorFromByte", e);
            return xor(input);
        }
    }

    public static String xor(final String input) {
        return new String(xor(input.getBytes(Charset.defaultCharset()), MY_KEY), Charset.defaultCharset());
    }

    public static byte[] xor(final byte[] input, final byte[] secret) {
        final byte[] output = new byte[input.length];
        if (secret.length == 0) {
            throw new IllegalArgumentException("empty security key");
        }
        int sPos = 0;
        for (int pos = 0; pos < input.length; ++pos) {
            output[pos] = (byte) (input[pos] ^ secret[sPos]);
            ++sPos;
            if (sPos >= secret.length) {
                sPos = 0;
            }
        }
        return output;
    }
}
