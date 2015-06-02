package com.zwb.args.dbpratice.util;

/**
 * Created by pc on 2015/6/1.
 */
public class StringUtil {
    private StringUtil() {
    }

    /**
     * 将首字母变成大写字母
     *
     * @param str
     * @return
     */
    public static String getFirstUpperCaseStr(String str) {
        String firstStr = str.substring(0, 1);
        String result = firstStr.toUpperCase() + str.substring(1);
        return result;
    }
}
