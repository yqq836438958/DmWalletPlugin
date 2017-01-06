
package com.pacewear.tsm.channel;


import java.util.List;

public interface ITsmCardChannel {

    public boolean getCPLC(ITsmAPDUCallback callback);

    public boolean selectAID(String aid, ITsmAPDUCallback callback);

    public boolean transmit(List<String> apduList, ITsmAPDUCallback callback);

    public boolean close();

}
