
package com.pacewear.tws.phoneside.wallet.step;

/**
 * @author baodingzhou
 */

public interface IStep<STEP> {

    /**
     * 状态
     */
    public enum STATUS {
        /**
         * 进入状态
         */
        ENTER,

        /**
         * 处理状态
         */
        HANDLE,

        /**
         * 保持状态
         */
        KEEP,

        /**
         * 离开状态
         */
        QUIT
    }

    /**
     * 
     */
    public enum COMMON_STEP {
        /**
         * 不可用
         */
        UNAVAILABLE,

        /**
         * 变更
         */
        UPDATED,

        /**
         * 就绪(没有变化)
         */
        READY,

        /**
         * 不确定的
         */
        DUBIOUS,
    }

    public void onEnterStep();

    public void onStep();

    public void onStepHandle();

    public void keepStep();

    public void onQuitStep();

    public STEP getStep();

    /**
     * isCurrentStatus
     * 
     * @return
     */
    public boolean isCurrentStep();

    /**
     * getStatus
     * 
     * @return
     */
    public STATUS getStatus();

    /**
     * switchStep
     * 
     * @param step
     * @return
     */
    public boolean switchStep(IStep<STEP> step);
}
