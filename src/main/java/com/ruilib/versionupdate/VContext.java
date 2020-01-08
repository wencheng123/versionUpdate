package com.ruilib.versionupdate;

import android.content.Context;

/**
 * @author wencheng
 * @create 2019/12/6
 * @Describe
 */
public class VContext {

    private static Context instance;
    public static Context getInstance() {
        return instance;
    }

    public static void init(Context context) {
        instance = context;
    }

//
//    public static void setAppTitle(String title){
//        titles = title;
//    }
//
//    public static String getAppTitle() {
//        return titles;
//    }

}
