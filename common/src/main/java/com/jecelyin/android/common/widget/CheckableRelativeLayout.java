package com.jecelyin.android.common.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.RelativeLayout;

import com.jecelyin.android.common.R;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class CheckableRelativeLayout extends RelativeLayout implements Checkable {
    private boolean mChecked;
    private Checkable mCheckable;

    private static final int[] CHECKED_STATE_SET = { android.R.attr.state_checked };

    public CheckableRelativeLayout(Context context) {
        super(context);
    }

    public CheckableRelativeLayout(final Context context, final AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int[] onCreateDrawableState(final int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked())
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        return drawableState;
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void setChecked(final boolean checked) {
        if (mChecked == checked)
            return;
        mChecked = checked;

        if(mCheckable == null) {
            mCheckable = (Checkable) findViewById(R.id.checkable);
        }

        if(mCheckable != null) {
            mCheckable.setChecked(checked);
        }

        refreshDrawableState();
    }
}
