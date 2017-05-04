
package com.pacewear.tws.phoneside.wallet.bean;

public class ModuleBean {
    private int icon;
    private int moduleName;
    private String tagetClass;

    public int getIcon() {
        return icon;
    }

    public int getName() {
        return moduleName;
    }

    public String getTargetClass() {
        return tagetClass;
    }

    public ModuleBean(int icon, int name, String target) {
        this.icon = icon;
        this.moduleName = name;
        this.tagetClass = target;
    }
}
