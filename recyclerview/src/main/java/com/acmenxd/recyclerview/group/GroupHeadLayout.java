package com.acmenxd.recyclerview.group;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/4/14 11:49
 * @detail RecyclerView -> 分组功能head
 */
public final class GroupHeadLayout extends LinearLayout {
    protected int groupItemLevelNum = -1;
    private int mOrientation = OrientationHelper.VERTICAL;
    private boolean isHORIZONTAL = false;
    private Map<Integer, View> mViews;
    private List mResetPositions;

    public GroupHeadLayout(Context context) {
        this(context, null);
    }

    public GroupHeadLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupHeadLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mViews = new HashMap<>();
        mResetPositions = new ArrayList();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        mOrientation = getOrientation();
        isHORIZONTAL = mOrientation == OrientationHelper.HORIZONTAL;
        int width = 0;
        int height = 0;
        for (int i = getChildCount() - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (child != null) {
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final int paddingTop = getPaddingTop();
                final int paddingLeft = getPaddingLeft();
                int mTop = paddingTop + lp.topMargin;
                int mLeft = paddingLeft + lp.leftMargin;
                if (isHORIZONTAL) {
                    mLeft = mLeft + width;
                } else {
                    mTop = mTop + height;
                }
                int mRight = child.getMeasuredWidth() + mLeft;
                int mBottom = child.getMeasuredHeight() + mTop;
                width = mRight;
                height = mBottom;
                child.layout(mLeft, mTop, mRight, mBottom);

                for (Map.Entry<Integer, View> entry : mViews.entrySet()) {
                    if (entry.getValue() == child) {
                        for (int j = 0; j < mResetPositions.size(); j++) {
                            if (mResetPositions.get(j) == entry.getKey()) {
                                int wh = isHORIZONTAL ? -child.getMeasuredWidth() : -child.getMeasuredHeight();
                                setPositionNum(child, wh);
                            }
                        }
                    }
                }
            }
        }
        mResetPositions.clear();
    }

    private void setPositionNum(@NonNull View child, int num) {
        if (isHORIZONTAL) {
            child.setX(num);
        } else {
            child.setY(num);
        }
    }

    protected void addResetPosition(@IntRange(from = 0) int level) {
        mResetPositions.add(level);
    }

    protected void resetPosition(@IntRange(from = 0) int level) {
        if (mViews.containsKey(level)) {
            setPositionNum(mViews.get(level), getWHByLevelSmall(level));
        }
    }

    protected int getAllWH() {
        int wh = 0;
        for (int i = 0, len = getChildCount(); i < len; i++) {
            wh += (isHORIZONTAL ? getChildAt(i).getWidth() : getChildAt(i).getHeight());
        }
        return wh;
    }

    protected boolean[] changePosition(int currViewTL, @IntRange(from = 0) int maxLevel, @IntRange(from = 0) int minLevel) {
        int wh = 0;
        boolean isScroll = false;
        boolean startDelete = false;
        boolean[] deletes = new boolean[groupItemLevelNum];
        Arrays.fill(deletes, false);
        for (int level = 0; level < groupItemLevelNum; level++) {
            if (mViews.containsKey(level)) {
                View view = mViews.get(level);
                if (isScroll) {
                    setPositionNum(view, 0 - (isHORIZONTAL ? view.getWidth() : view.getHeight()));
                }
                if (startDelete) {
                    if (level > minLevel) {
                        deletes[level] = true;
                    }
                } else {
                    wh += (isHORIZONTAL ? view.getWidth() : view.getHeight());
                    if (currViewTL - wh <= 0) {
                        if (level >= maxLevel) {
                            int scroll = getWHByLevelSmall(level) - (wh - currViewTL);
                            setPositionNum(view, scroll);
                            isScroll = true;
                        }
                        startDelete = true;
                    }
                }
            }
        }
        return deletes;
    }

    protected boolean isCanChangePosition(int currViewTL, @IntRange(from = 0) int level) {
        int nowWH = getWHByLevelSmall(level);
        if (currViewTL < nowWH) {
            return true;
        }
        if (mViews.containsKey(level)) {
            int tl = isHORIZONTAL ? mViews.get(level).getLeft() : mViews.get(level).getTop();
            if (tl < nowWH) {
                return true;
            }
        }
        return false;
    }

    protected int getWHByLevel(@IntRange(from = 0) int level) {
        int wh = 0;
        for (Map.Entry<Integer, View> entry : mViews.entrySet()) {
            if (entry.getKey() <= level) {
                wh += (isHORIZONTAL ? entry.getValue().getWidth() : entry.getValue().getHeight());
            }
        }
        return wh;
    }

    protected int getWHByLevelSmall(@IntRange(from = 0) int level) {
        int wh = 0;
        for (Map.Entry<Integer, View> entry : mViews.entrySet()) {
            if (entry.getKey() < level) {
                wh += (isHORIZONTAL ? entry.getValue().getWidth() : entry.getValue().getHeight());
            }
        }
        return wh;
    }

    protected void removeGroupHeadViewByLevel(@IntRange(from = 0) int level) {
        for (int i = level; i < groupItemLevelNum; i++) {
            if (mViews.containsKey(i)) {
                View view = mViews.get(i);
                mViews.remove(i);
                this.removeView(view);
            }
        }
    }

    protected void removeGroupHeadViewByLevel2(@IntRange(from = 0) int level) {
        if (mViews.containsKey(level)) {
            View view = mViews.get(level);
            mViews.remove(level);
            this.removeView(view);
        }
    }

    protected void addGroupHeadView(@NonNull View view, @IntRange(from = 0) int level) {
        mViews.put(level, view);
        this.addView(view, 0);
    }

    protected void removeGroupHeadView() {
        mViews.clear();
        this.removeAllViews();
    }

    protected View getGroupHeadView(@IntRange(from = 0) int level) {
        if (mViews.containsKey(level)) {
            return mViews.get(level);
        }
        return null;
    }

    protected boolean isHave() {
        return mViews.size() > 0;
    }

    protected int[] getLevels() {
        int[] levels = new int[groupItemLevelNum];
        Arrays.fill(levels, -1);
        for (int i = 0; i < groupItemLevelNum; i++) {
            if (mViews.containsKey(i)) {
                levels[i] = i;
            }
        }
        return levels;
    }

    protected int getMaxLevel() {
        for (int i = 0; i < groupItemLevelNum; i++) {
            if (mViews.containsKey(i)) {
                return i;
            }
        }
        return -1;
    }

    protected void setGroupItemLevelNum(@IntRange(from = 0) int groupItemLevelNum) {
        this.groupItemLevelNum = groupItemLevelNum;
    }
}
