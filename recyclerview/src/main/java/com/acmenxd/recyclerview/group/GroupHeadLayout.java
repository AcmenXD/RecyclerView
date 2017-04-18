package com.acmenxd.recyclerview.group;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.OrientationHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/4/14 11:49
 * @detail RecyclerView -> 分组功能head
 */
public class GroupHeadLayout extends LinearLayout {
    private int mOffset;
    private int mLastOffset;
    private int mOrientation;
    private View mGroupHeadView;

    public GroupHeadLayout(Context context) {
        this(context, null);
    }

    public GroupHeadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupHeadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final View child = getChildAt(0);
        if (child != null) {
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            final int paddingLeft = getPaddingLeft();
            final int paddingTop = getPaddingTop();

            int mLeft = paddingLeft + lp.leftMargin;
            int mTop = paddingTop + lp.topMargin;
            if (mOrientation == OrientationHelper.VERTICAL) {
                mTop = mTop + mOffset;
            } else if (mOrientation == OrientationHelper.HORIZONTAL) {
                mLeft = mLeft + mOffset;
            }
            final int mRight = child.getMeasuredWidth() + mLeft;
            final int mBottom = child.getMeasuredHeight() + mTop;

            child.layout(mLeft, mTop, mRight, mBottom);
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof MarginLayoutParams;
    }

    protected void scrollChild(int offset, int orientation) {
        if (mLastOffset != offset) {
            mOffset = offset;
            mOrientation = orientation;
            if (mOrientation == OrientationHelper.VERTICAL) {
                ViewCompat.offsetTopAndBottom(getChildAt(0), mOffset - mLastOffset);
            } else if (mOrientation == OrientationHelper.HORIZONTAL) {
                ViewCompat.offsetLeftAndRight(getChildAt(0), mOffset - mLastOffset);
            }
        }
        mLastOffset = mOffset;
    }

    protected int getChildHeight() {
        final View child = getChildAt(0);
        if (child != null) {
            return getChildAt(0).getHeight();
        }
        return 0;
    }

    protected int getChildWidth() {
        final View child = getChildAt(0);
        if (child != null) {
            return getChildAt(0).getWidth();
        }
        return 0;
    }

    protected void addGroupHeadView(View view) {
        removeGroupHeadView();
        mGroupHeadView = view;
        this.addView(mGroupHeadView);
    }

    protected void removeGroupHeadView() {
        if (mGroupHeadView != null) {
            this.removeView(mGroupHeadView);
            mGroupHeadView = null;
        }
    }

    protected View getGroupHeadView() {
        return mGroupHeadView;
    }
}
