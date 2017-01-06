
package com.pacewear.tsm;

public class TsmConstants {
    public static final int TSM_TYPE_INSTALLAPPLET = 1001;
    public static final int TSM_TYPE_DELETEAPPLET = TSM_TYPE_INSTALLAPPLET + 1;
    public static final int TSM_TYPE_UPDATEKEY = TSM_TYPE_DELETEAPPLET + 1;
    public static final int TSM_TYPE_AUTH = TSM_TYPE_UPDATEKEY + 1;
    public static final int TSM_TYPE_GETCPLC = TSM_TYPE_AUTH + 1;
    public static final int TSM_TYPE_GETCARDINFO = TSM_TYPE_GETCPLC + 1;
    public static final int TSM_TYPE_SELECTAID = TSM_TYPE_GETCARDINFO + 1;
    public static final int TSM_TYPE_INSTALLSECDOM = TSM_TYPE_SELECTAID + 1;
    public static final int TSM_TYPE_APPINFO = TSM_TYPE_INSTALLSECDOM + 1;
    public static final String TSM_DEFAULT_MAIN_AID = "A000000151000000";
}
