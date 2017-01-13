
package com.pacewear.tsm.internal.core;

import com.qq.taf.jce.JceStruct;

import java.util.List;

public interface IProcessEventConsum {

    public int onParserApdu(JceStruct rsp, List<String> apdus, boolean fromLocal);

    public boolean returnWithoutTransmit();
}
