
package com.pacewear.tsm.common;

import java.util.Random;

/**
 * SeqGenerator
 * 
 * @author baodingzhou
 */

public class SeqGenerator {

    private volatile static SeqGenerator mInstacne = null;

    private final static Object oLocked = new Object();

    private volatile long mSeq = 0;

    public SeqGenerator() {
        Random random = new Random(System.currentTimeMillis());
        mSeq = Math.abs(random.nextLong());
    }

    public static SeqGenerator getInstance() {

        if (mInstacne == null) {
            synchronized (oLocked) {
                if (mInstacne == null) {
                    mInstacne = new SeqGenerator();
                }
            }
        }

        return mInstacne;
    }

    public long uniqueSeq() {
        // 这里简单处理了
        // 当前业务不存在溢出的可能
        synchronized (oLocked) {
            return mSeq++;
        }
    }
}
