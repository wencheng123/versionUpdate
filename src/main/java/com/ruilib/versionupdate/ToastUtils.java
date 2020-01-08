package com.ruilib.versionupdate;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * @author wencheng
 * @create 2019/12/6
 * @Describe
 */
public class ToastUtils {

    private static Toast toast;

    /**
     * 短时间Toast
     *
     * @param message 消息
     */
    @SuppressLint("ShowToast")
    public static void toast(String message) {
        try {
            if (VContext.getInstance() == null){
                return;
            }
            if (!TextUtils.isEmpty(message)) {
                if (Build.VERSION.SDK_INT >= 28) {
                    //9.0以上toast直接用原生的方法即可，并不用setText防止重复的显示的问题
                    //下列写法：是为了解决部分手机的提示语 前面 会出现  包名。  例如：  "app名：请输入密码"
                    Toast toast = Toast.makeText(VContext.getInstance(), "", Toast.LENGTH_SHORT);
                    toast.setText(message);
                    toast.show();
                } else {
                    if (toast == null) {
                        toast = Toast.makeText(VContext.getInstance(), message, Toast.LENGTH_SHORT);
                    } else {
                        toast.setDuration(Toast.LENGTH_SHORT);
                        toast.setText(message);
                    }
                    toast.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
