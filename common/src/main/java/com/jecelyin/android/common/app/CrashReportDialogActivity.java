package com.jecelyin.android.common.app;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.jecelyin.android.common.R;
import com.jecelyin.android.common.api.CrashReportApi;
import com.jecelyin.android.common.utils.SysUtils;

import java.io.File;
import java.io.FileWriter;
import java.net.URLEncoder;

public class CrashReportDialogActivity extends Activity implements DialogInterface.OnClickListener
{
    private EditText mComment;
    private EditText mailEditText;
    private String mTrace;
    private String mMsg;
    private Handler mSuccessHandler;
    public final static String KEY_MSG = "msg";
    public final static String KEY_TRACE = "trace";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Intent it = getIntent();
        mMsg = it.getStringExtra(KEY_MSG);
        mTrace = it.getStringExtra(KEY_TRACE);

        mSuccessHandler = new Handler(){
            @Override
            public void handleMessage(Message msg)
            {
                close();
            }
        };

        show();
        saveLog();
    }

    private void saveLog() {
        File saveDir = SysUtils.getSDCardDir();
        if(saveDir == null) {
            saveDir = getCacheDir();
        }
        if(saveDir == null)
            return;

        try {
            File logFile = new File(saveDir, "tk.log");
            FileWriter writer = new FileWriter(logFile, true);
            writer.write(DateFormat.format("yyyy-MM-dd HH:mm:ss", System.currentTimeMillis()) + "  --> " + mMsg + "\n" + mTrace);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    

    public void show()
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.crash_report);
        dialogBuilder.setView(buildCustomView());
        dialogBuilder.setPositiveButton(android.R.string.ok, CrashReportDialogActivity.this);
        dialogBuilder.setNegativeButton(android.R.string.cancel, CrashReportDialogActivity.this);
        
        AlertDialog dialog = dialogBuilder.create();
        
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }
    
    private View buildCustomView()
    {
        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(10, 10, 10, 10);
        root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        root.setFocusable(true);
        root.setFocusableInTouchMode(true);

        final ScrollView scroll = new ScrollView(this);
        root.addView(scroll, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f));
        final LinearLayout scrollable = new LinearLayout(this);
        scrollable.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(scrollable);

        // Add an optional prompt for user comments
        final TextView label = new TextView(this);
        label.setText(R.string.crash_comment);

        label.setPadding(label.getPaddingLeft(), 10, label.getPaddingRight(), label.getPaddingBottom());
        scrollable.addView(label, new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mComment = new EditText(this);
        mComment.setLines(2);
        scrollable.addView(mComment);

        // Add an optional user email field
        final TextView label1 = new TextView(this);
        label1.setText("Email");
        label1.setPadding(label1.getPaddingLeft(), 10, label1.getPaddingRight(), label1.getPaddingBottom());
        scrollable.addView(label1);

        mailEditText = new EditText(this);
        mailEditText.setSingleLine();
        mailEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        scrollable.addView(mailEditText);

        final TextView text = new TextView(this);
        text.setText(mTrace);

        if (Build.VERSION.SDK_INT >= 11) {
            text.setTextIsSelectable (true);
        }

        scrollable.addView(text);

        return root;
    }

    
    @Override
    public void onClick(DialogInterface dialog, final int which)
    {
        if (which != DialogInterface.BUTTON_POSITIVE) // no ok
        {
            close();
            return;
        }

        String mail = URLEncoder.encode(mailEditText.getText().toString().trim());
        String comment = URLEncoder.encode(mComment.getText().toString().trim());
        //TODO: send to server
        Toast.makeText(CrashReportDialogActivity.this, R.string.crash_report_success, Toast.LENGTH_SHORT).show();
        mSuccessHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                mSuccessHandler.obtainMessage().sendToTarget();
            }
        }, 2000);
    }

    private void close()
    {
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }

    private CrashReportApi getApi()
    {
        String version = "Unknown";
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            version = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        CrashReportApi api = new CrashReportApi();
        api.setAppVersion(version);
        api.setPhoneModel(Build.BRAND + " " + Build.MODEL);
        api.setAndroidVersion(Build.VERSION.RELEASE);
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        api.setScreen(display.getWidth() + "x" + display.getHeight());

        // Device Memory
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        api.setFreeMem(Long.toString(memoryInfo.availMem/1024/1024)+"M");

        return api;
    }
}
