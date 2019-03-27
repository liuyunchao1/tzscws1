package com.wondersgroup.tzscws1.util;

import com.wondersgroup.tzscws1.verification.Verification;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Map;

/**
 * 签名以及验签
 */
public class Demo {

    public static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    public static final String ENCODE_ALGORITHM = "SHA-256";
    public static final String PLAIN_TEXT = "test string";

    public static void main(String[] args) {
        // 公私钥对
        Map<String, byte[]> keyMap = LukeRsa.generateKeyBytes();
        System.out.println("未还原的公钥----》》"+keyMap.get(LukeRsa.PUBLIC_KEY)+"---完成");
        System.out.println("未还原的私钥----》》"+keyMap.get(LukeRsa.PRIVATE_KEY));

        PublicKey publicKey = LukeRsa.restorePublicKey(keyMap.get(LukeRsa.PUBLIC_KEY));



        System.out.println("还原的公钥----》》"+publicKey+"---完成");
        PrivateKey privateKey = LukeRsa.restorePrivateKey(keyMap.get(LukeRsa.PRIVATE_KEY));
        System.out.println("还原后的私钥----》》"+privateKey);
        // 签名
        byte[] sing_byte = sign(privateKey, PLAIN_TEXT);
        // 验签
        verifySign(publicKey, PLAIN_TEXT, sing_byte);
    }

    /**
     * 签名
     *
     * @param privateKey
     *            私钥
     * @param plain_text
     *            明文
     * @return
     */
    public static byte[] sign(PrivateKey privateKey, String plain_text) {
        MessageDigest messageDigest;
        byte[] signed = null;
        try {
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            messageDigest.update(plain_text.getBytes());
            byte[] outputDigest_sign = messageDigest.digest();
            System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_sign));
            Signature Sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            Sign.initSign(privateKey);
            Sign.update(outputDigest_sign);
            signed = Sign.sign();
            System.out.println("SHA256withRSA签名后-----》" + bytesToHexString(signed));
            //besa64转码
            Verification ver= new Verification();
            System.out.println("besa64转码---》》》"+ver.beas64Code(bytesToHexString(signed)));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return signed;
    }

    /**
     * 验签
     *
     * @param publicKey
     *            公钥
     * @param plain_text
     *            明文
     * @param signed
     *            签名
     */
    public static boolean verifySign(PublicKey publicKey, String plain_text, byte[] signed) {

        MessageDigest messageDigest;
        boolean SignedSuccess=false;
        try {
            messageDigest = MessageDigest.getInstance(ENCODE_ALGORITHM);
            messageDigest.update(plain_text.getBytes());
            byte[] outputDigest_verify = messageDigest.digest();
            //System.out.println("SHA-256加密后-----》" +bytesToHexString(outputDigest_verify));
            Signature verifySign = Signature.getInstance(SIGNATURE_ALGORITHM);
            verifySign.initVerify(publicKey);
            verifySign.update(outputDigest_verify);
            SignedSuccess = verifySign.verify(signed);
            System.out.println("验证成功？---" + SignedSuccess);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }
        return SignedSuccess;
    }

    /**
     * bytes[]换成16进制字符串
     *
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}