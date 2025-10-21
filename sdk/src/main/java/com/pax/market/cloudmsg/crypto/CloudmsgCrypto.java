//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.pax.market.cloudmsg.crypto;

public class CloudmsgCrypto {
    public static native String decryptCloudmsg(String var0, String var1);

    static {
        System.loadLibrary("cloudmsg-crypto");
    }
}
