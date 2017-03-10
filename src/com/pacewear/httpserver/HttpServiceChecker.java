
package com.pacewear.httpserver;

public class HttpServiceChecker {
    private static IServerErrChecker sChecker = null;

    public static interface IServerErrChecker {
        public void onTokenInvalid();
    }

    public static IServerErrChecker get() {
        return sChecker;
    }

    public static void regist(IServerErrChecker checker) {
        sChecker = checker;
    }
}
