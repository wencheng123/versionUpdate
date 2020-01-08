package com.ruilib.versionupdate;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;

import java.io.File;

import static android.content.Context.NOTIFICATION_SERVICE;
/**
 * @author wencheng
 * @create 2019/12/6
 * @Describe
 */

public class UpdateNotionManager {
    private NotificationChannel mNotificationChannel;
    private NotificationManager mNotificationManager;
    private static UpdateNotionManager instance;
    private Context context;
    private Notification mNotification;
    private File apkFile;
    private String notifitionTag = "notifition_tag";
    private UpdateNotionManager() {
    }

    public static UpdateNotionManager getInstance() {
        if (instance == null) {
            synchronized (UpdateNotionManager.class) {
                if (instance == null) {
                    instance = new UpdateNotionManager();
                }
            }
        }
        return instance;
    }


    public void notifyUser(Context context, int progress, File apkFile) {
        this.context = context;
        this.apkFile = apkFile;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "1";
            String channelName = "版本更新";
            if (mNotificationChannel == null) {
                //创建 通知通道  channelId和channelName是必须的（自己命名就好）
                mNotificationChannel = new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_HIGH);
                //是否在桌面icon右上角展示小红点
                mNotificationChannel.enableLights(true);
                //小红点颜色
                mNotificationChannel.setLightColor(Color.GREEN);
                //是否在久按桌面图标时显示此渠道的通知
                mNotificationChannel.setShowBadge(true);
                mNotificationManager.createNotificationChannel(mNotificationChannel);
            }
            Notification.Builder builder = new Notification.Builder(context.getApplicationContext(), channelId);
            builder.setOnlyAlertOnce(true);
            builder.setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.str_zzxz))
                    .setAutoCancel(true);

            //设置进度
            if (progress > 0 && progress <= 100) {
                builder.setContentText(context.getString(R.string.str_download_pro) + progress + "%");
                builder.setProgress(100, progress, false);
                if (progress == 100) {
                    builder.setContentText(context.getString(R.string.str_download_finish));
                }
            } else {
                builder.setProgress(0, 0, false);
            }
            int notificationId = 0x1234;
            mNotificationManager.notify(notifitionTag,notificationId, builder.build());

        } else {
            //7.0,7.1以下版本
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

            /****
             * 7.0以上的手机通知栏会出现小白框图标。为了处理该问题，图标设置为没有背景色的图标即可解决。
             * builder.setSmallIcon(xxx);  xxx: 要设置为透明背景色的图标
             * *****/

            builder.setSmallIcon(R.drawable.app_logo)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.app_logo))
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.str_zzxz));

            if (progress > 0 && progress <= 100) {
                builder.setContentText(context.getString(R.string.str_download_pro) + progress + "%");
                builder.setProgress(100, progress, false);
                if (progress == 100) {
                    builder.setContentText(context.getString(R.string.str_download_finish));
                }
            } else {
                builder.setProgress(0, 0, false);
            }

            builder.setAutoCancel(true);
            builder.setWhen(System.currentTimeMillis());
            mNotification = builder.build();
            mNotificationManager.notify(notifitionTag,2, mNotification);
        }
    }

    /**
     * 进入安装
     *
     * @return
     */
    private PendingIntent getContentIntent() {
        mNotificationManager.cancelAll();
        //移除通知栏
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager.deleteNotificationChannel("1");
        }

        /*** 7.0,8.0兼容 * targetSdkVersion 26*/

        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 26) {
            //8.0(26)，8.1(27)
            Uri apkUri = FileProvider.getUriForFile(context.getApplicationContext(),
                    context.getPackageName() + ".fileprovider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");

        } else if (Build.VERSION.SDK_INT >= 23) {
            //6.0(23)；7.0(24)；7.1(25)；
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        context.startActivity(intent);

        android.os.Process.killProcess(android.os.Process.myPid());

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        context.startActivity(intent);
        return pendingIntent;
    }

    public void remove() {
        if (mNotificationManager == null) {
            return;
        }
        mNotificationManager.cancelAll();
        //移除通知栏
        if (Build.VERSION.SDK_INT >= 26) {
            mNotificationManager.cancel(notifitionTag,0x1234);
            mNotificationManager.deleteNotificationChannel("2018");
        }else {
            mNotificationManager.cancel(notifitionTag,2);
        }
    }
}
