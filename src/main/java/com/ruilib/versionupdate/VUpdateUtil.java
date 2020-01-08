package com.ruilib.versionupdate;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.List;

/**
 * @author wencheng
 * @create 2019/12/6
 * @Describe
 */
public class VUpdateUtil {


    private Context context;
    private VersionBean bean;
    VersionDialog versionDialog;  //更新对话框
    //是否强制更新
    private boolean isForcedUpdate = false;

    public VUpdateUtil(final Context context, VersionBean bean, boolean isToast) {
        this.context = context;
        this.bean = bean;
        showUpdatesDialog();
    }


    /**
     * 弹出版本更新提示框
     */
    private void showUpdatesDialog() {
        if (!isServiceRunning("com.ruilib.versionupdate.VUpdateService", context)) {

            String content = bean.getContent();
            String versionName = bean.getVersionName();
            //是否强制
            isForcedUpdate = bean.getIsMustUpdate() == 1 ? true : false;

            if (versionDialog == null) {
                versionDialog = new VersionDialog(context).builder();

            }
            //强制更新
            versionDialog.setForceUpdate(isForcedUpdate);

            if (!TextUtils.isEmpty(versionName)) {
                String title = context.getString(R.string.str_zxbb) + "" + versionName;
//                tvVersion.setText(title);
                versionDialog.setNewVersion(title);
            }

            versionDialog.setVersionContent(content);

            versionDialog.setMyOnItemClick(new VersionDialog.MyOnItemClick() {
                @Override
                public void onOkClick() {
                    if (!isForcedUpdate) {
                        versionDialog.dismiss();
                    }
                    //开启下载
                    Intent intent = new Intent(context, VUpdateService.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("updateVData", bean);
                    intent.putExtras(bundle);
                    context.startService(intent);

                    ToastUtils.toast(VContext.getInstance().getString(R.string.str_zzhtxz));
                }
            });

            if (context instanceof Activity) {
                if (((Activity) context).isFinishing()) {
                    return;
                }
            }
            versionDialog.show();

        } else {
//            ToastUtils.toast(context.getString(R.string.str_zzxz));
        }
    }


    /**
     * 判断服务是否开启
     *
     * @return
     */
    private boolean isServiceRunning(final String className, Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> info = activityManager.getRunningServices(Integer.MAX_VALUE);
        if (info == null || info.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningServiceInfo aInfo : info) {
            if (className.equals(aInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public void dismiss() {
        if (versionDialog != null) {
            versionDialog.dismiss();
        }
    }
}
