package com.jecelyin.android.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import com.jecelyin.android.common.R;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class CenterTitleToolbar extends Toolbar {
    private int mTitleTextAppearance;
    private TextView mTitleTextView;
    private int mTitleTextColor;
    private CharSequence mTitleText;

    public CenterTitleToolbar(Context context) {
        this(context, null);
    }

    public CenterTitleToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.toolbarStyle);
    }

    public CenterTitleToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public CenterTitleToolbar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar,
                defStyleAttr, defStyleRes);

        mTitleTextAppearance = a.getResourceId(R.styleable.Toolbar_titleTextAppearance, 0);
        a.recycle();
    }

    public CharSequence getTitle() {
        return mTitleText;
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            ensureTitleView();
            if (mTitleTextView.getParent() == null) {
                LayoutParams lp = new LayoutParams(Gravity.CENTER);
                mTitleTextView.setLayoutParams(lp);
                mTitleTextView.setGravity(Gravity.CENTER);
                addView(mTitleTextView, lp);
            }
        } else if (mTitleTextView != null && mTitleTextView.getParent() != null) {
            removeView(mTitleTextView);
        }
        if (mTitleTextView != null) {
            mTitleTextView.setText(title);
        }
        mTitleText = title;
    }

    @Override
    public void setTitleTextColor(int color) {
        mTitleTextColor = color;
        if (mTitleTextView != null) {
            mTitleTextView.setTextColor(color);
        }
    }

    public void setTitleTextAppearance(Context context, int resId) {
        mTitleTextAppearance = resId;
        if (mTitleTextView != null) {
            mTitleTextView.setTextAppearance(context, resId);
        }
    }

    private void ensureTitleView() {
        if (mTitleTextView == null) {
            final Context context = getContext();
            mTitleTextView = new TextView(context);
            mTitleTextView.setSingleLine();
            mTitleTextView.setEllipsize(TextUtils.TruncateAt.END);
            if (mTitleTextAppearance != 0) {
                mTitleTextView.setTextAppearance(context, mTitleTextAppearance);
            }
            if (mTitleTextColor != 0) {
                mTitleTextView.setTextColor(mTitleTextColor);
            }
        }
    }

    public TextView getTitleTextView() {
        return mTitleTextView;
    }
}
