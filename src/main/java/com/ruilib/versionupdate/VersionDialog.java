package com.ruilib.versionupdate;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * @author wencheng
 * @create 2019/11/12
 * @Describe 版本更新弹框
 */
public class VersionDialog implements View.OnClickListener {

    private Context context;
    private Dialog dialog;
    private LinearLayout lLayout_bg;
    private Display display;


    public VersionDialog(Context context) {
        this.context = context;
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        display = windowManager.getDefaultDisplay();
    }

    public VersionDialog builder() {
        View view = LayoutInflater.from(context).inflate(
                R.layout.version_update, null);

        lLayout_bg = (LinearLayout) view.findViewById(R.id.version_dialog_bg);

        dialog = new Dialog(context, R.style.AlertDialogStyle);
        dialog.setContentView(view);

        lLayout_bg.setLayoutParams(new FrameLayout.LayoutParams((int) (display
                .getWidth() * 0.75), ViewGroup.LayoutParams.WRAP_CONTENT));

        dialog.setCancelable(false); //点击外部不消失
        initViews(view);
        return this;
    }

    private TextView tvVersion;
    private TextView tvContent;
    private LinearLayout llOkCancel;
    private TextView btnCancel;
    private TextView btnOk;
    private ImageView ivLogo;


    private void initViews(View view) {

        tvVersion = (TextView) view.findViewById(R.id.version_version);
        tvContent = (TextView) view.findViewById(R.id.version_content);
        llOkCancel = (LinearLayout) view.findViewById(R.id.version_ok_cancel);
        btnCancel = (TextView) view.findViewById(R.id.version_cancel);
        btnOk = (TextView) view.findViewById(R.id.version_ok);

        ivLogo = view.findViewById(R.id.version_ivLogo);


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (myOnItemClick != null) {
                    myOnItemClick.onOkClick();
                }
            }
        });

    }

    public void show() {
        if (dialog != null) {
            dialog.show();
        }
    }

    public void setNewVersion(String content) {
        if (tvVersion != null) {
            tvVersion.setText(content);
        }
    }

    public void setVersionContent(String content) {
        if (tvContent != null) {
            if (!TextUtils.isEmpty(content)) {
                //解决服务端返回数据中，不能换行。
                content = content.replace("\\n", "\n");
                tvContent.setText(content);
            }
        }
    }

    public void setLogo(int resId) {
        if (ivLogo != null) {
            ivLogo.setImageResource(resId);
        }
    }

    /**
     * 是否需要强制更新
     *
     * @param isForce
     */
    public void setForceUpdate(boolean isForce) {
        if (btnCancel != null && btnOk != null) {
            if (isForce) {
                btnCancel.setVisibility(View.GONE);
            } else {
                btnCancel.setVisibility(View.VISIBLE);
            }
        }


    }

    public void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            default:
                break;
        }
    }


    public void setMyOnItemClick(MyOnItemClick myOnItemClick) {
        this.myOnItemClick = myOnItemClick;
    }

    MyOnItemClick myOnItemClick;

    public interface MyOnItemClick {
        void onOkClick();
    }
}

