package com.acmenxd.recyclerview.swipemenu;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.OverScroller;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/20 9:27
 * @detail 滑动菜单->左侧菜单栏
 */
public final class SwipeMenuViewLeft extends SwipeMenuView {

    public SwipeMenuViewLeft(Context context) {
        this(context, null);
    }

    public SwipeMenuViewLeft(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuViewLeft(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public boolean isMenuOpen(int scrollX) {
        int i = -this.getWidth() * LEFT_DIRECTION;
        return scrollX <= i && i != 0;
    }

    @Override
    public boolean isMenuOpenNotEqual(int scrollX) {
        return scrollX < -this.getWidth() * LEFT_DIRECTION;
    }

    @Override
    public boolean isClickOnMenuView(int pMenuLayoutWidth, float pEvX) {
        return pEvX > this.getWidth();
    }

    @Override
    public void autoOpenMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(Math.abs(scrollX), 0, this.getWidth() - Math.abs(scrollX), 0, duration);
    }

    @Override
    public void autoCloseMenu(OverScroller scroller, int scrollX, int duration) {
        scroller.startScroll(-Math.abs(scrollX), 0, Math.abs(scrollX), 0, duration);
    }

    @Override
    public Checker checkXY(int x, int y) {
        mChecker.x = x;
        mChecker.y = y;
        mChecker.shouldResetSwipe = false;
        if (mChecker.x == 0) {
            mChecker.shouldResetSwipe = true;
        }
        if (mChecker.x >= 0) {
            mChecker.x = 0;
        }
        if (mChecker.x <= -this.getWidth()) {
            mChecker.x = -this.getWidth();
        }
        return mChecker;
    }


}
