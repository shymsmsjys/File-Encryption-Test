package com.amte.shcha.fileencryptiontest;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by kschoi on 2017-05-29.
 */
public class AESEncryptDecrypt {
    //dpfxldldkfdpvmdk
    public static String encrypt(String key, String plainInput) {
        try {
            Key secureKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.ENCRYPT_MODE, secureKey);
            byte[] encryptedData = cipher.doFinal(plainInput.getBytes("UTF-8"));
            return toHex(encryptedData);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String encryptedInput) {
        try {
            Key secureKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES");
            Cipher cipher = Cipher.getInstance("AES");

            cipher.init(Cipher.DECRYPT_MODE, secureKey);
            byte[] plainData = cipher.doFinal(toByte(encryptedInput));
            String strignplainData = new String(plainData);
            return strignplainData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String toHex(byte[] buf) {
        if (buf == null) {
            return "";
        }

        StringBuffer result = new StringBuffer();
        for (byte b : buf) {
            result.append(Integer.toString((b & 0xF0) >> 4, 16));
            result.append(Integer.toString(b & 0x0F, 16));
        }
        return result.toString();
    }

    private static byte[] toByte(String hexString) {
        byte[] result = null;
        try {
            if (hexString == null) {
                return null;
            }
            int length = hexString.length();
            if (length % 2 == 1) {
                throw new IllegalArgumentException("For input string: \"" + hexString + "\"");
            }

            length = length / 2;
            result = new byte[length];
            for (int i = 0; i < length; i++) {
                int index = i * 2;
                result[i] = (byte) (Short.parseShort(hexString.substring(index, index + 2), 16));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
