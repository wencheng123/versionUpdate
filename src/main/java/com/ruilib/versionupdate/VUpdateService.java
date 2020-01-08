package com.ruilib.versionupdate;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * @author wencheng
 * @create 2019/12/6
 * @Describe
 */
public class VUpdateService extends Service {
    private final String TAG = "VUpdateService";
    private VersionBean bean;
    private File apkFile;

    //是否正在下载
    private boolean isDownloading;
    //下载文件名
    private String fileName;

    /**
     * 线程池
     */
    private ThreadPoolExecutor poolExecutor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        poolExecutor = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(1),
                new ThreadPoolExecutor.DiscardOldestPolicy());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            bean = (VersionBean) bundle.getSerializable("updateVData");
            if (isDownloading) {
                ToastUtils.toast(VContext.getInstance().getString(R.string.str_updating));
            } else {
                initFileDownloader();
                okDownLoad();
            }
        }
        return Service.START_NOT_STICKY;
    }

    private void okDownLoad() {
        //   MyApp.getInstance().setIsUpdate(true);
        poolExecutor.execute(runnable);
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
//            bean.setAppName("http://oss.pgyer.com/ec09430311d40fcd68631eca241f34d3.apk?auth_key=1573615438-c1847110c8a6ccc612546b2c579c5591-0-bbc54aad31903c60ac03926b2ac468ba&response-content-disposition=attachment%3B+filename%3Dchat-debug.apk");
            downloadApk(bean.getAppUrl(), apkFile.getPath(), new OnDownloadListener() {
                @Override
                public void onDownloadSuccess(File file) {
                    isDownloading = false;
                    Log.i(TAG, "下载完成： " + file.getPath());
                    UpdateNotionManager.getInstance().remove();
                    //安装应用
                    installAPK();
                }

                @Override
                public void onDownloading(int progress) {
                    isDownloading = true;
                    Log.i(TAG, "下载进度： " + progress);
                    UpdateNotionManager.getInstance().notifyUser(getApplicationContext(), progress, apkFile);
                }

                @Override
                public void onDownloadFailed(Exception e) {
                    isDownloading = false;
                    Log.i(TAG, "下载失败： " + e.getMessage());
                    VUpdateService.this.stopSelf();
                    deleteApk();
                    UpdateNotionManager.getInstance().remove();
                }
            });
        }
    };

    private void initFileDownloader() {
        // FileDownloader.setup(MyApp.getInstance());
        if (TextUtils.isEmpty(VContext.getInstance().getString(R.string.app_name))) {
            fileName = "androidApp.apk";
        }else {
            fileName = VContext.getInstance().getString(R.string.app_name)+ ".apk";
        }

        apkFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
        deleteApk();
    }

    private void deleteApk() {
        if (apkFile != null && apkFile.exists()) {
            boolean delete = apkFile.delete();
        }
    }

    /**
     * okHttp下载问题
     *
     * @param url
     * @param filePath
     * @param listener
     */
    private void downloadApk(String url, final String filePath, final OnDownloadListener listener) {
        Request request = null;
        try {
            request = new Request.Builder().url(url).build();
        } catch (Exception e) {
            e.printStackTrace();
            //java.lang.IllegalArgumentException: unexpected url: 192.168.20.20:8017/storage/file/180516/1526439043.apk
        }
        if (request == null) {
            return;
        }
        OkHttpClient client = new OkHttpClient();
        client.newCall(request).enqueue(new Callback() {


            @Override
            public void onFailure(Call call, IOException e) {
                listener.onDownloadFailed(e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                InputStream is = null;
                byte[] buf = new byte[2048 * 2];
                int len = 0;
                FileOutputStream fos = null;
                // 储存下载文件的目录
                File file = new File(filePath);
                try {
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    fos = new FileOutputStream(file);
                    long sum = 0;
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                        sum += len;
                        int progress = (int) (sum * 1.0f / total * 100);
                        // 下载中更新进度条
//                        LogUtils.i("downLoad进度： "+ progress);
//                        if(MyApp.getInstance().getIsUpdate()) {
//                            listener.onDownloading(progress);
//                        }
                        listener.onDownloading(progress);
                    }
                    fos.flush();
                    // 下载完成
//                    LogUtils.i("downLoad成功： " + file.getPath());
                    listener.onDownloadSuccess(file);
                } catch (Exception e) {
//                    LogUtils.i("downLoad失败： " + e.getMessage());
                    listener.onDownloadFailed(e);
                } finally {
                    try {
                        if (is != null) {
                            is.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null) {
                            fos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 7.0兼容
     */

    private void installAPK() {
        if (Build.VERSION.SDK_INT > 23) {
            //7.0以上加入了fileProvider
            startInstallN(getApplicationContext(), apkFile);
        } else {
            startInstall(getApplicationContext(), apkFile);
        }
    }

    /**
     * android7.x
     *
     * @param apkFile 文件路径
     */
    public void startInstallN(Context context, File apkFile) {
        //参数1 上下文, 参数2 在AndroidManifest中的android:authorities值, 参数3  共享的文件
        if (context == null) {
            context = VContext.getInstance().getApplicationContext();
        }

        Log.i(TAG,"---安装---"+ context.getPackageName());
        Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);


        Intent install = new Intent(Intent.ACTION_VIEW);
        //由于没有在Activity环境下启动Activity,设置下面的标签
        install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // 仅需改变这一行
        FileProviderUtils.setIntentDataAndType(context, install,
                "application/vnd.android.package-archive", apkUri, true);
        //添加这一句表示对目标应用临时授权该Uri所代表的文件
        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        context.startActivity(install);
    }

    /**
     * android1.x-6.x
     *
     * @param apkFile 文件的路径
     */
    public void startInstall(Context context, File apkFile) {
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(install);
    }

    public interface OnDownloadListener {
        /**
         * @param file 下载成功后的文件
         */
        void onDownloadSuccess(File file);

        /**
         * @param progress 下载进度
         */
        void onDownloading(int progress);

        /**
         * @param e 下载异常信息
         */
        void onDownloadFailed(Exception e);
    }

    @Override
    public void onDestroy() {
        stopSelf();
        super.onDestroy();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        poolExecutor.remove(runnable);
        UpdateNotionManager.getInstance().remove();
    }
}
