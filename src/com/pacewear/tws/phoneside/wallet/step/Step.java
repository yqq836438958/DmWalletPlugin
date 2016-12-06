
package com.pacewear.tws.phoneside.wallet.step;

/**
 * @author baodingzhou
 */

public abstract class Step<STEP> implements IStep<STEP> {

    protected STEP mStep = null;

    protected STATUS mStatus = STATUS.QUIT;

    public Step(STEP step) {
        mStep = step;
    }

    @Override
    public final STEP getStep() {
        return mStep;
    }

    @Override
    public final void onEnterStep() {
        mStatus = STATUS.ENTER;
        notifyStepStatus(mStep, mStatus);
        onStep();
    }

    @Override
    public final void onStep() {
        if (isCurrentStep() && mStatus != STATUS.HANDLE) {
            mStatus = STATUS.HANDLE;
            notifyStepStatus(mStep, mStatus);
            onStepHandle();
        }
    }

    @Override
    public final void keepStep() {
        mStatus = STATUS.KEEP;
        notifyStepStatus(mStep, mStatus);
    }

    @Override
    public final void onQuitStep() {
        mStatus = STATUS.QUIT;
        notifyStepStatus(mStep, mStatus);
    }

    @Override
    public final boolean isCurrentStep() {
        return mStatus != STATUS.QUIT;
    }

    @Override
    public final STATUS getStatus() {
        return mStatus;
    }

    @Override
    public final boolean switchStep(IStep<STEP> step) {
        if (isCurrentStep()) {
            return setStep(step);
        }

        return false;
    }

    protected abstract boolean setStep(IStep<STEP> step);

    protected abstract void notifyStepStatus(STEP step, STATUS status);
}
