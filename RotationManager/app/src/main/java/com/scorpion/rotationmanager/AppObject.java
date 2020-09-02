package com.scorpion.rotationmanager;

public class AppObject {

    private String appLabel;
    private String icon;
    private String pkgName;

    public AppObject(String str, String str2, String str3) {
        this.appLabel = str;
        this.pkgName = str2;
        this.icon = str3;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String str) {
        this.pkgName = str;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon(String str) {
        this.icon = str;
    }

    public String getAppLabel() {
        return this.appLabel;
    }

    public void setAppLabel(String str) {
        this.appLabel = str;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (!(obj instanceof AppObject)) {
            return false;
        }

        AppObject person = (AppObject) obj;

        return person.pkgName.equals(pkgName);
    }
}
