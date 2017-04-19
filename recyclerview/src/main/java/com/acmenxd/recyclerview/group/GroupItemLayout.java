package com.acmenxd.recyclerview.group;

import android.content.Context;
import android.support.v7.widget.OrientationHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/4/14 17:16
 * @detail RecyclerView -> 分组Item视图
 */
public class GroupItemLayout extends LinearLayout implements ViewTreeObserver.OnGlobalLayoutListener {
    private View mGroupItemView;
    private int width;
    protected int height;
    private ViewTreeObserver.OnGlobalLayoutListener listener;

    public GroupItemLayout(Context context) {
        this(context, null);
    }

    public GroupItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    protected void addGroupItemView(View view, int orientation, int groupItemPosition) {
        removeGroupItemView();
        mGroupItemView = view;
        // 添加groupItem视图
        this.addView(mGroupItemView, 0);
        // 设置布局排列
        if (orientation == OrientationHelper.VERTICAL) {
            this.setOrientation(OrientationHelper.VERTICAL);
            if (groupItemPosition != GroupListener.ITEM_TOP && groupItemPosition != GroupListener.ITEM_OUT_TOP) {
                groupItemPosition = GroupListener.ITEM_OUT_TOP;
            }
        } else if (orientation == OrientationHelper.HORIZONTAL) {
            this.setOrientation(OrientationHelper.HORIZONTAL);
            if (groupItemPosition != GroupListener.ITEM_LEFT && groupItemPosition != GroupListener.ITEM_OUT_LEFT) {
                groupItemPosition = GroupListener.ITEM_OUT_LEFT;
            }
        }
        // 设置groupItem排列
        if (groupItemPosition == GroupListener.ITEM_OUT_TOP
                || groupItemPosition == GroupListener.ITEM_OUT_LEFT) {
            changeWH(orientation);
        }
    }

    protected void removeGroupItemView() {
        if (mGroupItemView != null) {
            if (listener != null) {
                mGroupItemView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            }
            this.removeView(mGroupItemView);
            mGroupItemView = null;
        }
    }

    protected View getGroupItemView() {
        return mGroupItemView;
    }

    protected void changeWH(final int orientation) {
        final ViewGroup.LayoutParams params = getLayoutParams();
        if (mGroupItemView == null) {
            params.width = width;
            params.height = height;
            GroupItemLayout.this.setLayoutParams(params);
        } else {
            if (listener != null) {
                mGroupItemView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
            }
            listener = new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (mGroupItemView != null) {
                        if (listener != null) {
                            mGroupItemView.getViewTreeObserver().removeOnGlobalLayoutListener(listener);
                        }
                        if (orientation == OrientationHelper.VERTICAL) {
                            params.height = height + mGroupItemView.getMeasuredHeight();
                        } else if (orientation == OrientationHelper.HORIZONTAL) {
                            params.width = width + mGroupItemView.getMeasuredWidth();
                        }
                        GroupItemLayout.this.setLayoutParams(params);
                    }
                }
            };
            mGroupItemView.getViewTreeObserver().addOnGlobalLayoutListener(listener);
        }
    }

    @Override
    public void onGlobalLayout() {
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
    }
}
