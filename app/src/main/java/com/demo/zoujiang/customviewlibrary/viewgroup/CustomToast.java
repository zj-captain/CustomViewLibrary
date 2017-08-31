package com.demo.zoujiang.customviewlibrary.viewgroup;

import android.content.Context;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.widget.Toast;

import com.demo.zoujiang.customviewlibrary.R;

/**
 * Created by Administrator on 2017/8/12 0012.
 */

public class CustomToast {
    private static Toast mToast;
    private static Handler mHandler = new Handler();
    private static Runnable runnable = new Runnable() {
        @Override
        public void run() {
            mToast.cancel();
        }
    };

    public static void showToast(Context mContext, String text, int duration) {
        if (mToast != null) {
            ((TextView) mToast.getView().findViewById(R.id.tv_text)).setText(text);
        } else {
            mToast = new Toast(mContext);
            mToast.setDuration(duration);
            mToast.setView(LayoutInflater.from(mContext).inflate(R.layout.custom_toast_view, null));
            ((TextView) mToast.getView().findViewById(R.id.tv_text)).setText(text);
            mToast.setGravity(Gravity.BOTTOM, 0, 100);
            mHandler.postDelayed(runnable, duration);
        }
        mToast.show();
    }

    public static void showToast(Context mContext, int resId, int duration) {
        showToast(mContext, mContext.getResources().getString(resId), duration);
    }

}
