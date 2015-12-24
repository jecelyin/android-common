package com.jecelyin.android.common.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Adapter;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class StaticListView extends LinearLayout {
    private Drawable divider;
    private int dividerHeight;
    protected Adapter adapter;
    protected Observer observer = new Observer(this);

    public StaticListView(Context context) {
        super(context);
    }

    public StaticListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, new int[]{
                android.R.attr.divider,
                android.R.attr.dividerHeight
        }, 0, 0);

        divider = a.getDrawable(0);
        dividerHeight = a.getDimensionPixelOffset(1, 0);
        a.recycle();
        setOrientation(VERTICAL);
    }

    public void setAdapter(Adapter adapter) {
        if (this.adapter != null)
            this.adapter.unregisterDataSetObserver(observer);
        this.adapter = adapter;
        adapter.registerDataSetObserver(observer);
        observer.onChanged();
    }

    private static class Observer extends DataSetObserver {
        StaticListView context;

        public Observer(StaticListView context) {
            this.context = context;
        }

        @Override
        public void onChanged() {
            List<View> oldViews = new ArrayList<>(context.getChildCount());
            for (int i = 0; i < context.getChildCount(); i++)
                oldViews.add(context.getChildAt(i));

            Iterator<View> iter = oldViews.iterator();
            context.removeAllViews();
            int count = context.adapter.getCount();
            for (int i = 0; i < count; i++) {
                View convertView = iter.hasNext() ? iter.next() : null;
                View view = context.adapter.getView(i, convertView, context);
                if (context.dividerHeight > 0) {
                    MarginLayoutParams lp = (MarginLayoutParams) view.getLayoutParams();
                    if(lp != null) {
                        lp.bottomMargin = context.dividerHeight;
                    }
                    view.setLayoutParams(lp);
                }

                context.addView(view);
            }
            super.onChanged();
        }

        @Override
        public void onInvalidated() {
            context.removeAllViews();
            super.onInvalidated();
        }
    }
}
