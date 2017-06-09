package com.acmenxd.recyclerview.group;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuLayout;
import com.acmenxd.recyclerview.wrapper.WrapperUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/10 15:45
 * @detail RecyclerView -> 支持分组功能主类
 */
public final class GroupDecoration extends RecyclerView.ItemDecoration {
    private RecyclerView mRecyclerView;
    private GroupHeadLayout mGroupHeadLayout; // Head根布局
    private GroupListener mListener; // 回调监听
    private Map<Integer, CheckGroupItem> checkGroups; // 存储groupItem是否带有Head
    private int direction = 1; // 1:向上  2:向下  3:向左   4:向右
    private int orientation = OrientationHelper.VERTICAL; // 默认为垂直布局
    private boolean isHORIZONTAL = false; // 是否是水平布局

    private int[] currPositions = null; // 记录当前各层级显示Head位置
    private int groupItemLevelNum = 1; // groupItem的层级数量
    private boolean isGroupItemTypeMoreOne = false; // groupItem的类型是否大于一种
    private int groupItemPosition = GroupListener.ITEM_OUT_TOP; // groupItem的显示位置
    private boolean isAutoSetGroupHeadViewWidthHeightByGroupItemView = false; // 是否自动配置Head的宽高,根据当前GroupItem

    public GroupDecoration(@NonNull GroupHeadLayout pGroupHeadLayout, @NonNull GroupListener pListener) {
        mGroupHeadLayout = pGroupHeadLayout;
        mListener = pListener;
        checkGroups = new HashMap<>();
        groupItemLevelNum = pListener.getGroupItemLevelNum();
        isGroupItemTypeMoreOne = pListener.isGroupItemTypeMoreOne();
        isAutoSetGroupHeadViewWidthHeightByGroupItemView = pListener.isAutoSetGroupHeadViewWidthHeightByGroupItemView();
        // 记录当前各层级显示Head位置数据
        currPositions = new int[groupItemLevelNum];
        Arrays.fill(currPositions, -1);
        // 处理GroupHeadLayout数据
        mGroupHeadLayout.groupItemLevelNum = groupItemLevelNum;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        orientation = AdapterUtils.getOrientation(parent);
        isHORIZONTAL = orientation == OrientationHelper.HORIZONTAL;
        if (mRecyclerView == null) {
            mRecyclerView = parent;
            if (isHORIZONTAL) {
                mGroupHeadLayout.setOrientation(OrientationHelper.HORIZONTAL);
            } else {
                mGroupHeadLayout.setOrientation(OrientationHelper.VERTICAL);
            }
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dx > 0) {
                        direction = 3;
                    } else {
                        direction = 4;
                    }
                    if (dy > 0) {
                        direction = 1;
                    } else {
                        direction = 2;
                    }
                }
            });
        }
        // 创建groupItem
        int viewPosition = parent.getChildAdapterPosition(view);
        if (checkGroups.containsKey(viewPosition)) {
            checkGroups.get(viewPosition).isHave = false;
        } else {
            checkGroups.put(viewPosition, new CheckGroupItem());
        }
        GroupItemLayout groupItemLayout = getGroupItemLayoutByView(view);
        if (groupItemLayout != null) {
            int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(parent);
            if (dataPosition >= 0) {
                if (mListener.isCreateGroupItemView(dataPosition)) {
                    if (!groupItemLayout.isHave() || isGroupItemTypeMoreOne || groupItemLevelNum > 1) {
                        groupItemLayout.removeGroupItemView();
                        for (int level = 0; level < groupItemLevelNum; level++) {
                            View groupItemView = mListener.getGroupItemView(groupItemLayout, level, dataPosition);
                            if (groupItemView != null) {
                                groupItemLayout.addGroupItemView(groupItemView, level, orientation, groupItemPosition);
                                groupItemLayout.setGroupItemLevelNum(groupItemLevelNum);
                            }
                        }
                    }
                    if (groupItemLayout.isHave()) {
                        int[] levels = groupItemLayout.getLevels();
                        for (int i = 0, len = levels.length; i < len; i++) {
                            int level = levels[i];
                            if (level >= 0) {
                                mListener.changeGroupItemView(groupItemLayout.getGroupItemView(level), level, dataPosition);
                            }
                        }
                        checkGroups.get(viewPosition).isHave = true;
                        checkGroups.get(viewPosition).levels = levels;
                    }
                } else {
                    groupItemLayout.removeGroupItemView();
                }
            }
        }
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int lastPosition = -1;
        int firstPosition = findFirstVisiblePosition(parent);
        View currView = getCurrView(parent, mGroupHeadLayout.getAllWH(), 0);
        int nowPosition = parent.getChildAdapterPosition(currView);
        if (isGroupItemTypeMoreOne || groupItemLevelNum > 1) {
            View view = findVisiblePositionAsc(firstPosition, nowPosition);
            if (view != null) {
                currView = view;
                nowPosition = parent.getChildAdapterPosition(currView);
            }
        }
        if (currView == null) {
            return;
        }
        for (int level = 0; level < groupItemLevelNum; level++) {
            int haveFirst = findUpGroupHeadPosition(firstPosition, lastPosition, level);
            if (haveFirst < 0) {
                currPositions[level] = -1;
                mGroupHeadLayout.removeGroupHeadViewByLevel2(level);
            } else {
                int firstVisiblePosition = firstPosition;
                int firstDownPosition = findVisiblePositionDesc(nowPosition, firstVisiblePosition);
                if (firstDownPosition >= 0) {
                    firstVisiblePosition = firstDownPosition;
                }
                boolean changePosition = false;
                int currViewTL = currView != null ? isHORIZONTAL ? currView.getLeft() : currView.getTop() : 0;
                int currViewTL2 = getCurrViewTop(currView, level);
                if (nowPosition > firstVisiblePosition && isGroupHeadLayout(nowPosition)
                        && currViewTL < mGroupHeadLayout.getWHByLevel(level)) {
                    changePosition = true;
                    if (mGroupHeadLayout.isCanChangePosition(currViewTL2, level)) {
                        firstVisiblePosition = nowPosition;
                    }
                }
                int groupHeadPosition = findUpGroupHeadPosition(firstVisiblePosition, lastPosition, level);
                if (groupHeadPosition >= 0 && firstVisiblePosition >= groupHeadPosition) {
                    lastPosition = groupHeadPosition;
                    boolean changeWH = false;
                    if (currPositions[level] != groupHeadPosition) {
                        currPositions[level] = groupHeadPosition;
                        if (currPositions[level] == -1) {
                            changeWH = true;
                        }
                        int dataPosition = groupHeadPosition - WrapperUtils.getEmptyUpItemCount(parent);
                        if (!mGroupHeadLayout.isHave() || isGroupItemTypeMoreOne || groupItemLevelNum > 1) {
                            mGroupHeadLayout.removeGroupHeadViewByLevel(level);
                            View groupHeadView = mListener.getGroupHeadView(mGroupHeadLayout, level, dataPosition);
                            if (groupHeadView != null) {
                                mGroupHeadLayout.addGroupHeadView(groupHeadView, level);
                                if (direction == 2 || direction == 4) {
                                    mGroupHeadLayout.addResetPosition(level);
                                }
                            }
                        }
                        if (mGroupHeadLayout.isHave()) {
                            mListener.changeGroupHeadView(mGroupHeadLayout.getGroupHeadView(level), level, dataPosition);
                            changeWH = true;
                        }
                        if (isAutoSetGroupHeadViewWidthHeightByGroupItemView && changeWH) {
                            RecyclerView.ViewHolder changeViewHolder = parent.findViewHolderForAdapterPosition(groupHeadPosition);
                            if (changeViewHolder != null && changeViewHolder.itemView != null) {
                                setGroupHeadLayoutWH(changeViewHolder.itemView, level);
                            }
                        }
                    }
                    if (changePosition) {
                        int maxLevel = getMaxLevelByViewPosition(nowPosition);
                        int minLevel = getMinLevelByViewPosition(nowPosition);
                        if (maxLevel >= 0 && maxLevel <= level) {
                            boolean[] deletes = mGroupHeadLayout.changePosition(currViewTL, maxLevel, minLevel);
                            for (int i = 0, len = deletes.length; i < len; i++) {
                                if (deletes[i]) {
                                    currPositions[i] = -1;
                                    mGroupHeadLayout.removeGroupHeadViewByLevel(i);
                                }
                            }
                        }
                    } else {
                        mGroupHeadLayout.resetPosition(level);
                    }
                }
            }
        }
    }

    private int getCurrViewTop(@NonNull View currView, @IntRange(from = 0) int level) {
        if (currView != null) {
            GroupItemLayout groupItemLayout = getGroupItemLayoutByView(currView);
            if (groupItemLayout != null) {
                View view = groupItemLayout.getGroupItemView(level);
                if (view != null) {
                    return isHORIZONTAL ? view.getLeft() + currView.getLeft() : view.getTop() + currView.getTop();
                }
            }
            return isHORIZONTAL ? currView.getLeft() : currView.getTop();
        }
        return 0;
    }

    /**
     * 获取当前Head底部的View
     */
    private View getCurrView(@NonNull RecyclerView parent, @IntRange(from = 0) int wh, @IntRange(from = 0) int index) {
        View view = isHORIZONTAL ? parent.findChildViewUnder(wh, 0) : parent.findChildViewUnder(0, wh);
        if (view == null && index <= 40 && wh > 0) {
            view = getCurrView(parent, wh - 5, ++index);
        }
        return view;
    }

    /**
     * 获取GroupItemLayout
     */
    private GroupItemLayout getGroupItemLayoutByView(@NonNull View view) {
        GroupItemLayout groupItemLayout = null;
        if (view instanceof GroupItemLayout) {
            groupItemLayout = (GroupItemLayout) view;
        } else if (view instanceof SwipeMenuLayout) {
            View tempView = ((SwipeMenuLayout) view).getContentView().getChildAt(0);
            if (tempView != null && tempView instanceof GroupItemLayout) {
                groupItemLayout = (GroupItemLayout) tempView;
            }
        }
        return groupItemLayout;
    }

    /**
     * 设置Head与对应的groupitem等宽 或 等高
     */
    private void setGroupHeadLayoutWH(@NonNull View view, @IntRange(from = 0) int level) {
        GroupItemLayout groupItemLayout = getGroupItemLayoutByView(view);
        if (groupItemLayout != null) {
            View groupItemView = groupItemLayout.getGroupItemView(level);
            View groupHeadView = mGroupHeadLayout.getGroupHeadView(level);
            if (groupItemView != null && groupHeadView != null) {
                if (isHORIZONTAL) {
                    ViewGroup.LayoutParams params = groupHeadView.getLayoutParams();
                    params.height = groupItemView.getMeasuredHeight();
                    groupHeadView.setLayoutParams(params);
                } else {
                    ViewGroup.LayoutParams params = groupHeadView.getLayoutParams();
                    params.width = groupItemView.getMeasuredWidth();
                    groupHeadView.setLayoutParams(params);
                }
            }
        }
    }

    /**
     * 查找当前第一个显示的视图位置
     */
    private int findFirstVisiblePosition(@NonNull RecyclerView parent) {
        int firstVisiblePosition = 0;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            firstVisiblePosition = ((GridLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof LinearLayoutManager) {
            firstVisiblePosition = ((LinearLayoutManager) layoutManager).findFirstVisibleItemPosition();
        } else if (layoutManager instanceof StaggeredGridLayoutManager) {
            int[] mInto = new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()];
            ((StaggeredGridLayoutManager) layoutManager).findFirstVisibleItemPositions(mInto);
            firstVisiblePosition = Integer.MAX_VALUE;
            for (int pos : mInto) {
                firstVisiblePosition = Math.min(pos, firstVisiblePosition);
            }
        }
        return firstVisiblePosition;
    }

    /**
     * 查找nowPosition位置到当前第一个视图,有没有带GroupItemLayout的位置
     */
    public int findVisiblePositionDesc(@IntRange(from = 0) int nowPosition, @IntRange(from = 0) int firstVisiblePosition) {
        for (int i = nowPosition - 1; i > firstVisiblePosition; i--) {
            if (isGroupHeadLayout(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 查找nowPosition位置到当前第一个视图,有没有带GroupItemLayout的位置
     */
    public View findVisiblePositionAsc(@IntRange(from = 0) int firstVisiblePosition, @IntRange(from = 0) int nowPosition) {
        for (int i = firstVisiblePosition + 1; i < nowPosition; i++) {
            if (isGroupHeadLayout(i)) {
                RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(i);
                if (viewHolder != null) {
                    View view = viewHolder.itemView;
                    GroupItemLayout groupItemLayout = getGroupItemLayoutByView(view);
                    if (groupItemLayout != null) {
                        boolean isTL = isHORIZONTAL ? view.getLeft() > 0 : view.getTop() > 0;
                        if (isTL && groupItemLayout.getMaxLevel() <= mGroupHeadLayout.getMaxLevel()) {
                            return viewHolder.itemView;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 向上寻找最近的一个Head的位置(包括参数位置)
     */
    private int findUpGroupHeadPosition(@IntRange(from = 0) int formPosition, @IntRange(from = 0) int lastPosition, @IntRange(from = 0) int level) {
        if (formPosition < lastPosition) {
            formPosition = lastPosition;
        }
        for (int i = formPosition; i >= 0 && i >= lastPosition; i--) {
            if (checkGroups.containsKey(i) && checkGroups.get(i).isHave) {
                int[] levels = checkGroups.get(i).levels;
                for (int j = 0, len = levels.length; j < len; j++) {
                    if (levels[j] == level) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    /**
     * 检查pLayout是否带有Head
     */
    private boolean isGroupHeadLayout(int viewPosition) {
        if (viewPosition != RecyclerView.NO_POSITION && checkGroups.containsKey(viewPosition) && checkGroups.get(viewPosition).isHave) {
            return true;
        }
        return false;
    }

    /**
     * 根据ViewPosition找到最大Level层级
     */
    private int getMaxLevelByViewPosition(@IntRange(from = 0) int viewPosition) {
        if (checkGroups.containsKey(viewPosition) && checkGroups.get(viewPosition).isHave) {
            int[] levels = checkGroups.get(viewPosition).levels;
            for (int i = 0, len = levels.length; i < len; i++) {
                if (levels[i] >= 0) {
                    return levels[i];
                }
            }
        }
        return -1;
    }

    /**
     * 根据ViewPosition找到最小Level层级
     */
    private int getMinLevelByViewPosition(@IntRange(from = 0) int viewPosition) {
        if (checkGroups.containsKey(viewPosition) && checkGroups.get(viewPosition).isHave) {
            int[] levels = checkGroups.get(viewPosition).levels;
            for (int i = levels.length - 1; i >= 0; i--) {
                if (levels[i] >= 0) {
                    return levels[i];
                }
            }
        }
        return -1;
    }

    class CheckGroupItem {
        boolean isHave = false;
        int[] levels;
    }
}
