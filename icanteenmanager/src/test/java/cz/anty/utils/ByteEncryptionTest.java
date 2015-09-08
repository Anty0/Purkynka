package cz.anty.utils;

import junit.framework.TestCase;

import java.util.Scanner;

/**
 * Created by anty on 7.9.15.
 *
 * @author anty
 */
public class ByteEncryptionTest extends TestCase {

    public void testXorToByte() throws Exception {
        Scanner scanner = new Scanner(System.in);
        String start = scanner.nextLine();
        System.out.println(start);
        start = ByteEncryption.xorToByte(start);
        System.out.println(start);
        start = ByteEncryption.xorFromByte(start);
        System.out.println(start);
    }
}