
package com.pacewear.tsm.query;

public interface ITsmApplet {
    public static final String FOODCARD = "111111111";
    public static final String SHANGHAITONG = "2222222222";
    public static final String BEIJINGTONG = "9156000014010001";

    public String parse(String tag, String[] apduList);
}
