
package com.pacewear.tsm.internal.core;

public interface IProcessChecker {
    public boolean isReady();

    public boolean invoke(OnTsmProcessCallback callback);
}
