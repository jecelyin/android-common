package com.jecelyin.android.common.app;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.util.Log;

import com.bumptech.glide.request.target.ViewTarget;
import com.jecelyin.android.common.R;
import com.jecelyin.android.common.api.AbstractApi;
import com.jecelyin.android.common.utils.L;
import com.jecelyin.android.common.utils.SysUtils;

public class JecApp extends Application implements Thread.UncaughtExceptionHandler
{
    @Override
    public void onCreate()
    {
        super.onCreate();

        init();
    }

    @SuppressLint("NewApi")
    public void init()
    {
        if (SysUtils.isDebug(this))
        {
            L.debug = true;
            //内存泄漏监控
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            builder.detectAll();
            builder.penaltyLog();
            StrictMode.setVmPolicy(builder.build());
        }
        // 捕捉未知异常
        Thread.setDefaultUncaughtExceptionHandler(this);
        // fix: java.lang.IllegalArgumentException: You must not call setTag() on a view Glide is targeting
        ViewTarget.setTagId(R.id.tag_first);

        AbstractApi.API_URL = (String)SysUtils.getBuildConfigValue(this, "API_URL");

        ApplicationInfo appInfo = null;
        try {
            appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if(appInfo != null && appInfo.metaData != null) {
            int isTester = appInfo.metaData.getInt("is_tester", 0);
            if(isTester != 0) { // 测试版本
//                HttpConstant.API_URL = HttpConstant.TEST_API_URL;
//                File sdcard = SysUtils.getSDCardDir();
//                // SDCARD/tk.server不存在则为连接测试环境，否则连接正式环境
//                if(sdcard != null && new File(sdcard, "tk.server").exists()) {
//                    HttpConstant.API_URL = HttpConstant.SERVER_API_URL;
//                }
                L.debug = true;
            }
        }
    }

    @Override
    public void uncaughtException(Thread thread, final Throwable ex)
    {
        Log.e("fangApp", "错误："+ex.getMessage(), ex);

        Intent dialogIntent = new Intent(this, CrashReportDialogActivity.class);
        dialogIntent.putExtra(CrashReportDialogActivity.KEY_MSG, ex.getMessage());
        dialogIntent.putExtra(CrashReportDialogActivity.KEY_TRACE, Log.getStackTraceString(ex));
        dialogIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(dialogIntent);

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

}
