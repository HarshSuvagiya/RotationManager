package com.scorpion.rotationmanager;

public class RotationModel {

    private String appLabel;
    private String icon;
    private String pkgName;
    private boolean autoRotation;
    private int orientationMode;

    public RotationModel() {
    }

    public RotationModel(String appLabel, String pkgName, String icon, boolean autoRotation, int orientationMode) {
        this.appLabel = appLabel;
        this.icon = icon;
        this.pkgName = pkgName;
        this.autoRotation = autoRotation;
        this.orientationMode = orientationMode;
    }

    public String getAppLabel() {
        return appLabel;
    }

    public void setAppLabel(String appLabel) {
        this.appLabel = appLabel;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getPkgName() {
        return pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public boolean isAutoRotation() {
        return autoRotation;
    }

    public void setAutoRotation(boolean autoRotation) {
        this.autoRotation = autoRotation;
    }

    public int getOrientationMode() {
        return orientationMode;
    }

    public void setOrientationMode(int orientationMode) {
        this.orientationMode = orientationMode;
    }
}
