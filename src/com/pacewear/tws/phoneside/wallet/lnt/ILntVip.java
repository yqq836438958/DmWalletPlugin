
package com.pacewear.tws.phoneside.wallet.lnt;

public interface ILntVip {
    public static interface ILntVipCallback {
        public void onSuc(String usrId);

        public void onTransfer(String className);
    }

    public String getId();

    public void saveId(String usrId);

    public void destroy();
}
