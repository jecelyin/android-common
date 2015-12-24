package com.jecelyin.android.common.app;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.jecelyin.android.common.utils.SysUtils;

import java.util.ArrayList;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class JecActivity extends AppCompatActivity {
    private boolean isAttached;
    private ArrayList<DispatchTouchEventListener> dispatchTouchEventListeners;

    public interface DispatchTouchEventListener {
        void onDispatchTouchEvent(MotionEvent event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(SysUtils.isAutoTester(this)) {
            hideStatusBar();
        }
    }

    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }

    public boolean isAttached() {
        return isAttached;
    }

    public boolean isDetached() {
        return !isAttached;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(dispatchTouchEventListeners != null) {
            for(DispatchTouchEventListener l : dispatchTouchEventListeners) {
                l.onDispatchTouchEvent(ev);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    public void addDispatchTouchEventListener(DispatchTouchEventListener l) {
        if(dispatchTouchEventListeners == null)
            dispatchTouchEventListeners = new ArrayList<>();
        dispatchTouchEventListeners.add(l);
    }

    public void removeDispatchTouchEventListener(DispatchTouchEventListener l) {
        if(dispatchTouchEventListeners == null)
            return;
        dispatchTouchEventListeners.remove(l);
    }
}
