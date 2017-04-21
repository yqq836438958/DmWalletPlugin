
package com.pacewear.tws.phoneside.wallet.lnt;

public interface ILntSdk {
    public static interface ILntCardPage {
        public boolean jump(String className);
    }

    public boolean resume(String content);

    public boolean charge();

    public void complaint();

    public void complaintQuery();

    public void destroy();
}
