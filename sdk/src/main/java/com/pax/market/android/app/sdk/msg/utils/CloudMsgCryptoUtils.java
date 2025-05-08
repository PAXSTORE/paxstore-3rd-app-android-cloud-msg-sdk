/*
 * *******************************************************************************
 * COPYRIGHT
 *               PAX TECHNOLOGY, Inc. PROPRIETARY INFORMATION
 *   This software is supplied under the terms of a license agreement or
 *   nondisclosure agreement with PAX  Technology, Inc. and may not be copied
 *   or disclosed except in accordance with the terms in that agreement.
 *
 *      Copyright (C) 2017 PAX Technology, Inc. All rights reserved.
 * *******************************************************************************
 */
package com.pax.market.android.app.sdk.msg.utils;


import com.pax.market.api.sdk.java.base.constant.Constants;
import com.pax.market.api.sdk.java.base.util.AlgHelper;
import com.pax.market.api.sdk.java.base.util.KeyUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by fanjun on 2016/11/10.
 */
public class CloudMsgCryptoUtils {
    private static final Logger logger = LoggerFactory.getLogger(CloudMsgCryptoUtils.class);
    private static final String AES = "AES";
    private static final String AES_CBC = "AES/CBC/PKCS5Padding";

    /**
     * Use AES to decrypt the string and return the original string.
     *
     * @param input Decrypt content
     * @param secret Decryption key
     * @return base64 encoded string
     */
    public static String aesDecrypt(String input, String secret) {
        byte[] secretBytes = AlgHelper.hexStringToBytes(secret);
        byte[] secretKey = KeyUtils.genSecretKey(secretBytes);

        String result = null;
        try {
            result = new String(aesDecrypt(hexStringToBytes(input), secretKey), Constants.CHARSET_UTF8);
        } catch (Exception e) {
            logger.error("AES decrypt ex", e);
        }
        return result;
    }

    /**
     * Use AES to decrypt the string and return the original string.
     *
     * @param input base64 encoded encrypted string
     * @param key   A key that meets AES requirements
     * @return the byte [ ]
     */
    public static byte[] aesDecrypt(byte[] input, byte[] key) {
        return aes(input, key, Cipher.DECRYPT_MODE);
    }

    /**
     * Use AES to encrypt or decrypt the original byte array without encoding, and return the result of the byte array without encoding.
     *
     * @param input Raw byte array
     * @param key   A key that meets AES requirements
     * @param mode  Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
     */
    private static byte[] aes(byte[] input, byte[] key, int mode) {
        try {
            SecretKey secretKey = new SecretKeySpec(key, AES);
            Cipher cipher = Cipher.getInstance(AES_CBC);
            cipher.init(mode, secretKey);
            return cipher.doFinal(input);
        } catch (GeneralSecurityException e) {
            logger.error("AES ex, mode:{}", mode, e);
            return null;
        }
    }


    public static byte[] hexStringToBytes(String paramString) {
        paramString = paramString.toUpperCase();
        if ((paramString == null) || (paramString.equals(""))) {
            return null;
        }
        paramString = paramString.toUpperCase();
        int i = paramString.length() / 2;
        char[] arrayOfChar = paramString.toCharArray();
        byte[] arrayOfByte = new byte[i];
        for (int j = 0; j < i; j++) {
            int k = j * 2;
            arrayOfByte[j] = ((byte) (charToByte(arrayOfChar[k]) << 4 | (charToByte(arrayOfChar[(k + 1)]) & 0xff)));
        }
        return arrayOfByte;
    }

    public static byte charToByte(char paramChar) {
        return (byte) "0123456789ABCDEF".indexOf(paramChar);
    }
}
