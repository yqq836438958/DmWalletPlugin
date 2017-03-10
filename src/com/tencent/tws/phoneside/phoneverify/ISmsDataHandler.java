
package com.tencent.tws.phoneside.phoneverify;

import com.qq.taf.jce.JceStruct;

public interface ISmsDataHandler {
    public static class ParseResult {
        public int iRet;
        public String msg;

        public static final ParseResult newInstance(int ret, String _msg) {
            ParseResult result = new ParseResult();
            result.iRet = ret;
            result.msg = (_msg == null) ? "" : _msg;
            return result;
        }
    }

    public ParseResult onParse(JceStruct response);

    public int onPostHandle(String msg);
}
