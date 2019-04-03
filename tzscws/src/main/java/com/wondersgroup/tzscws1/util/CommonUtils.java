package com.wondersgroup.tzscws1.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class CommonUtils {

    /**
     * 获取字符串，非null
     * @param obj
     * @return
     */
    public static String getString(Object obj) {
        return obj == null ? "" : obj.toString();
    }

    public static String formatDate(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

}
