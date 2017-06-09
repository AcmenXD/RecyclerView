package com.acmenxd.recyclerview.group;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/4/14 17:16
 * @detail RecyclerView -> 分组Item视图
 */
public final class GroupItemLayout extends LinearLayout {
    private Context mContext;
    private GroupHeadLayout mGroupHeadLayout;
    private int mOrientation = -1;
    private int mGroupItemPosition = -1;

    public GroupItemLayout(Context context) {
        this(context, null);
    }

    public GroupItemLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupItemLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    protected void addGroupItemView(@NonNull View view, @IntRange(from = 0) int level, @IntRange(from = OrientationHelper.HORIZONTAL, to = OrientationHelper.VERTICAL) int orientation, @IntRange(from = GroupListener.ITEM_TOP, to = GroupListener.ITEM_OUT_LEFT) int groupItemPosition) {
        if (mGroupHeadLayout == null) {
            this.mOrientation = orientation;
            this.mGroupItemPosition = groupItemPosition;
            int width = LayoutParams.WRAP_CONTENT;
            int height = LayoutParams.WRAP_CONTENT;
            mGroupHeadLayout = new GroupHeadLayout(mContext);
            // 设置布局排列
            if (mOrientation == OrientationHelper.VERTICAL) {
                this.setOrientation(OrientationHelper.VERTICAL);
                if (mGroupItemPosition != GroupListener.ITEM_TOP && mGroupItemPosition != GroupListener.ITEM_OUT_TOP) {
                    mGroupItemPosition = GroupListener.ITEM_OUT_TOP;
                }
                mGroupHeadLayout.setOrientation(OrientationHelper.VERTICAL);
                width = LayoutParams.MATCH_PARENT;
            } else if (mOrientation == OrientationHelper.HORIZONTAL) {
                this.setOrientation(OrientationHelper.HORIZONTAL);
                if (mGroupItemPosition != GroupListener.ITEM_LEFT && mGroupItemPosition != GroupListener.ITEM_OUT_LEFT) {
                    mGroupItemPosition = GroupListener.ITEM_OUT_LEFT;
                }
                mGroupHeadLayout.setOrientation(OrientationHelper.HORIZONTAL);
                height = LayoutParams.MATCH_PARENT;
            }
            // 设置groupItem排列
            if (mGroupItemPosition == GroupListener.ITEM_OUT_TOP
                    || mGroupItemPosition == GroupListener.ITEM_OUT_LEFT) {
                ViewGroup.LayoutParams params = getLayoutParams();
                if (mOrientation == OrientationHelper.VERTICAL) {
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                } else if (mOrientation == OrientationHelper.HORIZONTAL) {
                    params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
                }
                setLayoutParams(params);
            }
            mGroupHeadLayout.setLayoutParams(new LayoutParams(width, height));
            addView(mGroupHeadLayout, 0);
        }
        if (mGroupHeadLayout != null) {
            mGroupHeadLayout.addGroupHeadView(view, level);
        }
    }

    protected void removeGroupItemView() {
        if (mGroupHeadLayout != null) {
            mGroupHeadLayout.removeGroupHeadView();
        }
    }

    protected View getGroupItemView(@IntRange(from = 0) int level) {
        if (mGroupHeadLayout != null) {
            return mGroupHeadLayout.getGroupHeadView(level);
        }
        return null;
    }

    protected boolean isHave() {
        if (mGroupHeadLayout != null) {
            return mGroupHeadLayout.isHave();
        }
        return false;
    }

    protected int[] getLevels() {
        if (mGroupHeadLayout != null) {
            return mGroupHeadLayout.getLevels();
        }
        return new int[]{};
    }

    protected int getMaxLevel() {
        if (mGroupHeadLayout != null) {
            return mGroupHeadLayout.getMaxLevel();
        }
        return -1;
    }

    protected void setGroupItemLevelNum(@IntRange(from = 0) int groupItemLevelNum) {
        if (mGroupHeadLayout != null) {
            mGroupHeadLayout.setGroupItemLevelNum(groupItemLevelNum);
        }
    }
}
