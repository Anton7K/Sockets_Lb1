package ua.nure.kaplun.sockets;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Created by Anton on 20.03.2017.
 */
public class Test {
    public static void main(String[] args){
        BigInteger integer = BigInteger.valueOf(10000);
        byte[] bytes=integer.toByteArray();
        System.out.println(Arrays.toString(bytes));
        byte[] bytes1 = Message.intToByteArray(10000);
        System.out.println(Arrays.toString(bytes1));
        System.out.println(new BigInteger(bytes1).intValue());
    }
}
