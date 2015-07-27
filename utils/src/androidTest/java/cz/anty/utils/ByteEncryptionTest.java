package cz.anty.utils;

import junit.framework.TestCase;

/**
 * Created by anty on 17.6.15.
 *
 * @author anty
 */
public class ByteEncryptionTest extends TestCase {

    public void testXor() throws Exception {
        String start = "test";
        System.out.println(start);
        start = ByteEncryption.xor(start);
        System.out.println(start);
        start = ByteEncryption.xor(start);
        System.out.println(start);
    }
}