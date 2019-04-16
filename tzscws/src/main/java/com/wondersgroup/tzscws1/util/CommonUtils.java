package com.wondersgroup.tzscws1.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class CommonUtils {

    /**
     * 获取字符串，非null
     * @param
     * @return
     */
    public static String getString(Object obj) {
        return obj == null ? "" : obj.toString();
    }
    //日期格式转换
    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }
    //数字字母字符正则判断
    public static boolean isLetterDigit(String str) {
        String regex = "^[a-z0-9A-Z]+$";
        return str.matches(regex);
    }
    //判断字符串最大长度
    public static boolean isMaxLength(String str,int len){
        return str.length()<=len;
    }
    //判断字符固定长度
    public static boolean isLength(String str,int len){
        return str.length()==len;
    }
    //判断数字字符
    public static boolean isDigist(String str){
        String regex = "^[0-9]*$";
        return str.matches(regex);
    }
    //数值型, 小数点后保留2位数字
    public static boolean isDouNot(String str){
        String regex ="^([1-9][0-9]{0,6})+(.[0-9]{2})?$";
        return str.matches(regex);
    }
    //数值型保留一位整数一位小数
    public static boolean idOneNot(String str){
        String regex="^(?=0\\.[1-9]|[1-9]\\.\\d).3}$";
        return str.matches(regex);
    }
    //匹配中文字符
    public static boolean isCNChar(String str){
        String pattern = "[\\u4E00-\\u9FA5]+";
        return Pattern.matches(pattern, str);
    }
    //固定日期型字符判断
    public static boolean isDateStr(String date,String format){
       SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        try {
            Date formatDate = simpleDateFormat.parse(date);
        }catch (ParseException e){
            return false;
        }
      return true;
    }
    //orgCode格式判断长度为10字母数字,第9位为-，eg: XXXXXXXX-X
    public static boolean isOrgCode(String str) {
        String regex = "^[a-z0-9A-Z]{0,8}-[a-z0-9A-Z]+$";
        return str.matches(regex);
    }

}
