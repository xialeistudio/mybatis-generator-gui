package com.ddhigh.mybatis.util;

public class StringUtil {
    public static String ucwords(String name) {
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        return name;
    }

    public static String humpString(String name) {
        String[] temp = name.split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : temp) {
            sb.append(ucwords(s));
        }
        return sb.toString();
    }
}
