package com.wondersgroup.tzscws1.verification;


import com.wondersgroup.tzscws1.constant.Constant;
import com.wondersgroup.tzscws1.entity.HeaderDataEntty;

import com.sun.xml.internal.ws.util.StringUtils;
import org.apache.commons.codec.binary.Base64;


import java.security.MessageDigest;

/**
 * 数据验证
 */
public class Verification {


    /**
     * 验证headSing的值
     * @param headerData
     * @return
     */
    public boolean verHeadSign(HeaderDataEntty headerData){
        boolean flag=false;
        String ver=headerData.getEventId()+headerData.getRequestTime()+headerData.getHosId()+Constant.PASSORD;
        if(MD5(ver).equals(headerData.getHeadSign())){
            flag=true;
        }
        return flag;

    }




    private String MD5(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(s.getBytes("utf-8"));
            return toHex(bytes);
        } catch (Exception e) {
                throw new RuntimeException(e);
        }
    }


    private static String toHex(byte[] bytes) {
        final char[] HEX_DIGITS = "0123456789abcdef".toCharArray();
        StringBuilder ret = new StringBuilder(bytes.length * 2);
        for (int i=0; i<bytes.length; i++) {
            ret.append(HEX_DIGITS[(bytes[i] >> 4) & 0x0f]);
            ret.append(HEX_DIGITS[bytes[i] & 0x0f]);
        }
        return ret.toString();
    }


    /**
     * beas64转码
     * @param str
     * @return
     */
    public byte[] beas64Code(String str){
        return Base64.encodeBase64(str.getBytes());
    }


    /**
     * 二进制数据编码为BASE64字符串
     * @param
     * @return
     */
    public static String encodeBase64(byte[] bytes){
        return new String(Base64.encodeBase64(bytes));
    }

    /**
     * BASE64解码
     * @param bytes
     * @return
     */
    public static byte[] decodeBase64(byte[] bytes) {
        byte[] result = null;
        try {
            result = Base64.decodeBase64(bytes);
        } catch (Exception e) {
            return null;
        }
        return result;
    }

    public static void main(String[] args) {
        String str="123456";
        Verification vers=new Verification();
        System.out.println(vers.beas64Code(str));


        String stre="11111111111<body>\n" +
                "\t<!--个案卡数据-->\n" +
                "\t\t<reportCards>\n" +
                "\t\t\t<reportCard>\n" +
                "\t\t\t\t<arg1>地</arg1>\n" +
                "\t\t\t\t<arg2>一</arg2>\n" +
                "\t\t\t\t<arg3>的</arg3>\n" +
                "\t\t\t\t<arg4>了</arg4>\n" +
                "\t\t\t</reportCard>\n" +
                "\t\t\t<reportCard>\n" +
                "\t\t\t\t<arg1>上</arg1>\n" +
                "\t\t\t\t<arg2>人</arg2>\n" +
                "\t\t\t\t<arg3>同</arg3>\n" +
                "\t\t\t\t<arg4>和</arg4>\n" +
                "\t\t\t</reportCard>\n" +
                "\t\t</reportCards>\n" +
                "\t\t<!--新增或需修改企业基本信息-->\n" +
                "\t\t<employingUnits>\n" +
                "\t\t\t<employingUnit>\n" +
                "\t\t\t\t<arg1>主</arg1>\n" +
                "\t\t\t\t<arg2>地</arg2>\n" +
                "\t\t\t\t<arg3>了</arg3>\n" +
                "\t\t\t\t<arg4>以</arg4>\n" +
                "\t\t\t</employingUnit>\n" +
                "\t\t\t<employingUnit>\n" +
                "\t\t\t\t<arg1>上</arg1>\n" +
                "\t\t\t\t<arg2>人</arg2>\n" +
                "\t\t\t\t<arg3>有</arg3>\n" +
                "\t\t\t\t<arg4>地</arg4>\n" +
                "\t\t\t</employingUnit>\n" +
                "\t\t</employingUnits>\n" +
                "\t</body>2222222222222";

//        StringUtils.substringBeforeLast(stre, "<body>");
//        int index = stre.IndexOf("<body>");
        System.out.println(stre.indexOf("</body>"));
        System.out.println(stre.substring(stre.indexOf("<body>"),stre.indexOf("</body>")+7));

    }

}
