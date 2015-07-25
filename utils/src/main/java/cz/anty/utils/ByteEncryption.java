package cz.anty.utils;

import java.nio.charset.Charset;

/**
 * Created by anty on 17.6.15.
 *
 * @author anty
 */
class ByteEncryption {

    public static final byte[] MY_KEY = "Copyright ANTY 2015".getBytes(Charset.defaultCharset());

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
