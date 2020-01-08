package com.ruilib.versionupdate;

import java.io.Serializable;


/**
 * @author by benny
 * @date on 2018/6/25.
 * @function 版本信息
 */

public class VersionBean implements Serializable {

    private int versionCode; // 10
    private String versionName; //V1.0.2
    private String appUrl; //下载地址
    private String content;  //内容
    private int isMustUpdate; // 是否强制更新 1：强制

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIsMustUpdate() {
        return isMustUpdate;
    }

    public void setIsMustUpdate(int isMustUpdate) {
        this.isMustUpdate = isMustUpdate;
    }

    @Override
    public String toString() {
        return "VersionBean{" +
                "versionCode='" + versionCode + '\'' +
                ", versionName='" + versionName + '\'' +
                ", appUrl='" + appUrl + '\'' +
                ", content='" + content + '\'' +
                ", isMustUpdate=" + isMustUpdate +
                '}';
    }
}
