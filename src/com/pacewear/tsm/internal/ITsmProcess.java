
package com.pacewear.tsm.internal;

public interface ITsmProcess {
    public boolean start();

    public boolean isFinish();

    public boolean isIdle();

    public boolean setNext(ITsmProcess nextHanlder);
}
