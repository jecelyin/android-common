package com.jecelyin.android.common.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

/**
 * @author Jecelyin <jecelyin@gmail.com>
 */
public class DrawClickableEditText extends EditText implements TextWatcher {
    private Drawable drawableRight;
    private Drawable drawableLeft;
    private Drawable drawableTop;
    private Drawable drawableBottom;
    private Drawable clearDrawable;

    int actionX, actionY;

    private DrawableClickListener clickListener;
    private boolean clearVisible;

    public enum DrawablePosition { TOP, BOTTOM, LEFT, RIGHT }
    public interface DrawableClickListener {
        void onClick(DrawClickableEditText editText, DrawablePosition target);
    }

    public DrawClickableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DrawClickableEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        clearDrawable = getResources().getDrawable(android.R.drawable.presence_offline);
        clearDrawable.setBounds(0, 0, clearDrawable.getIntrinsicWidth(), clearDrawable.getIntrinsicHeight());
        setClearIconVisible(false);
        addTextChangedListener(this);
    }

    protected void setClearIconVisible(boolean visible) {
        clearVisible = visible;
        invalidate();
    }

    /**
     * tap outside edittext to lose focus
     * 对于activity
     * {@link android.app.Activity#dispatchTouchEvent(MotionEvent)}
     * 对于fragment
     * {@link com.fangdr.common.app.FangdrActivity#addDispatchTouchEventListener(FangdrActivity.DispatchTouchEventListener)}
     * 在dispatchTouchEvent访问调用本方法即可
     * @param event
     * @return
     */
    public boolean onDispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isFocused()) {
                Rect outRect = new Rect();
                getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(),(int)event.getRawY()))   {
                    clearFocus();
                    //
                    // Hide keyboard
                    //
                    InputMethodManager imm = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getWindowToken(), 0);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            setClearIconVisible(getText().length() > 0);
        } else {
            setClearIconVisible(false);
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (isFocused()) {
            setClearIconVisible(getText().length() > 0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(clearDrawable != null && clearVisible) {
            drawableClearable(canvas);
        }
    }

    private void drawableClearable(Canvas canvas) {
        int vspace = getBottom() - getTop() - getCompoundPaddingBottom() - getCompoundPaddingTop();
        int rightOffset = 0;
        Drawable[] compoundDrawables = getCompoundDrawables();
        if(compoundDrawables[2] != null) {
            rightOffset += compoundDrawables[2].getIntrinsicWidth() + getCompoundDrawablePadding();
        }

        canvas.save();
        canvas.translate(getScrollX() + getRight() - getLeft() - getPaddingRight()
                        - clearDrawable.getIntrinsicWidth() - rightOffset,
                getScrollY() + getCompoundPaddingTop() + (vspace - clearDrawable.getIntrinsicHeight()) / 2);
        clearDrawable.draw(canvas);
        canvas.restore();
    }

    @Override
    public void setCompoundDrawables(Drawable left, Drawable top,
                                     Drawable right, Drawable bottom) {
        if (left != null) {
            drawableLeft = left;
        }
        if (right != null) {
            drawableRight = right;
        }
        if (top != null) {
            drawableTop = top;
        }
        if (bottom != null) {
            drawableBottom = bottom;
        }
        super.setCompoundDrawables(left, top, right, bottom);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            actionX = (int) event.getX();
            actionY = (int) event.getY();

            if (drawableBottom != null
                    && drawableBottom.getBounds().contains(actionX, actionY)) {
                clickListener.onClick(this, DrawablePosition.BOTTOM);
                return super.onTouchEvent(event);
            }

            if (drawableTop != null
                    && drawableTop.getBounds().contains(actionX, actionY)) {
                clickListener.onClick(this, DrawablePosition.TOP);
                return super.onTouchEvent(event);
            }

            // this works for left since container shares 0,0 origin with bounds
            if (drawableLeft != null) {
                int dX = getPaddingLeft() + drawableLeft.getIntrinsicWidth() + getCompoundDrawablePadding();

                if (actionX < dX && clickListener != null) {
                    clickListener.onClick(this, DrawablePosition.LEFT);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;

                }
            }

            int width = getWidth();
            if (drawableRight != null) {
                int dX = width - getPaddingRight() - drawableRight.getIntrinsicWidth() - getCompoundDrawablePadding();
                if(actionX > dX && clickListener != null) {
                    clickListener.onClick(this, DrawablePosition.RIGHT);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }
            }
            if (clearDrawable != null) {
                int rightOffset = 0;
                if(drawableRight != null) {
                    rightOffset += drawableRight.getIntrinsicWidth() + getCompoundDrawablePadding();
                }

                int dX = width - getPaddingRight() - clearDrawable.getIntrinsicWidth() - rightOffset - getCompoundDrawablePadding();
                if(actionX > dX) {
                    setText("");
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    return false;
                }

            }
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void finalize() throws Throwable {
        drawableRight = null;
        drawableBottom = null;
        drawableLeft = null;
        drawableTop = null;
        super.finalize();
    }

    public void setDrawableClickListener(DrawableClickListener listener) {
        this.clickListener = listener;
    }
}
