package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public class TsmPassThrough extends TsmBaseProcess {
    public static enum PT_TYPE{
        TOPUP,CARDQUERY
    }
    public TsmPassThrough(TsmContext context, PT_TYPE reportType) {
        super(context,0);
    }

    @Override
    protected int onCheck() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    protected boolean onStart() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected int getApduList(JceStruct rsp, List<String> apdus, boolean fromLoacal) {
        // TODO Auto-generated method stub
        return 0;
    }

}
