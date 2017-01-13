
package com.pacewear.tsm.internal;

import com.pacewear.tsm.card.TsmContext;
import com.pacewear.tsm.internal.core.TsmBaseProcess;
import com.qq.taf.jce.JceStruct;

import java.util.List;

public class TsmPassThrough extends TsmBaseProcess {

    public TsmPassThrough(TsmContext context, String aid) {
        super(context, aid, false);
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
    protected int onParse(JceStruct rsp, List<String> apdus, boolean fromLoacal) {
        // TODO Auto-generated method stub
        return 0;
    }

}
