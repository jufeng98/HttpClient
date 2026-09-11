package org.javamaster.httpclient.utils;

import java.security.*;

public class KeyUtils {

    public static String getMainAlgorithm(String algorithm) {
        final int slashIndex = algorithm.indexOf('/');
        if (slashIndex > 0) {
            return algorithm.substring(0, slashIndex);
        }
        return algorithm;
    }

    public static KeyPairGenerator getKeyPairGenerator(String algorithm) throws Exception {
        KeyPairGenerator keyPairGen;
        keyPairGen = KeyPairGenerator.getInstance(getMainAlgorithm(algorithm));
        return keyPairGen;
    }

    public static KeyPair generateKeyPair(String algorithm, int keySize, SecureRandom random) throws Exception {
        final KeyPairGenerator keyPairGen = getKeyPairGenerator(algorithm);

        if (keySize > 0) {
            if (null != random) {
                keyPairGen.initialize(keySize, random);
            } else {
                keyPairGen.initialize(keySize);
            }
        }

        return keyPairGen.generateKeyPair();
    }

}
