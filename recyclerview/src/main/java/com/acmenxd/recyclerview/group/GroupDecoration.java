package com.acmenxd.recyclerview.group;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.swipemenu.SwipeMenuLayout;
import com.acmenxd.recyclerview.wrapper.WrapperUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/10 15:45
 * @detail RecyclerView -> 支持分组功能主类
 */
public class GroupDecoration extends RecyclerView.ItemDecoration {
    private GroupHeadLayout mGroupHeadLayout; // Head根布局
    private GroupListener mListener; // 回调监听
    private Map<Integer, Boolean> checkGroups; // 存储groupItem是否带有Head

    private int currGroupHeadPosition = -1; // 记录当前显示的Head位置
    private int groupItemTypeNum = 1; // groupItem的类型数量
    private int groupItemLevelNum = 1; // groupItem的层级数量
    private int groupItemPosition = GroupListener.ITEM_OUT_TOP; // groupItem的显示位置
    private boolean isAutoSetHeadWidthHeightByGroupItemView = false; // 是否自动配置Head的宽高,根据当前GroupItem

    public GroupDecoration(@NonNull GroupHeadLayout pGroupHeadLayout, @NonNull GroupListener pListener) {
        mGroupHeadLayout = pGroupHeadLayout;
        mListener = pListener;
        checkGroups = new HashMap<>();
        groupItemTypeNum = pListener.getGroupItemTypeNum();
        groupItemLevelNum = pListener.getGroupItemLevelNum();
        groupItemPosition = pListener.getGroupItemPosition();
        isAutoSetHeadWidthHeightByGroupItemView = pListener.isAutoSetHeadWidthHeightByGroupItemView();
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        int viewPosition = parent.getChildAdapterPosition(view);
        checkGroups.put(viewPosition, false);

        GroupItemLayout groupItemLayout = getGroupItemLayoutByView(view);
        if (groupItemLayout != null) {
            int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(parent);
            if (dataPosition >= 0) {
                View groupItemView = groupItemLayout.getGroupItemView();
                if (groupItemView == null || groupItemTypeNum > 1) {
                    groupItemView = mListener.getGroupItemView(groupItemLayout, dataPosition);
                    if (groupItemView != null) {
                        groupItemLayout.addGroupItemView(groupItemView, getOrientation(parent), groupItemPosition);
                    }
                }
                if (groupItemView != null) {
                    mListener.changeGroupItemView(groupItemView, dataPosition);
                    checkGroups.put(viewPosition, true);
                }
            }
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int firstVisiblePosition = findFirstVisiblePosition(parent);
        int groupHeadPosition = findUpGroupHeadPosition(firstVisiblePosition);
        if (groupHeadPosition >= 0 && firstVisiblePosition >= groupHeadPosition) {
            boolean isChange = false;
            int orientation = getOrientation(parent);
            if (currGroupHeadPosition != groupHeadPosition) {
                if (currGroupHeadPosition == -1) {
                    isChange = true;
                }
                currGroupHeadPosition = groupHeadPosition;
                int dataPosition = currGroupHeadPosition - WrapperUtils.getEmptyUpItemCount(parent);
                View groupHeadView = mGroupHeadLayout.getGroupHeadView();
                if (groupHeadView == null || groupItemTypeNum > 1) {
                    groupHeadView = mListener.getGroupHeadView(mGroupHeadLayout, dataPosition);
                    if (groupHeadView != null) {
                        mGroupHeadLayout.addGroupHeadView(groupHeadView);
                        if (orientation == OrientationHelper.VERTICAL) {
                            mGroupHeadLayout.setOrientation(OrientationHelper.VERTICAL);
                        } else if (orientation == OrientationHelper.HORIZONTAL) {
                            mGroupHeadLayout.setOrientation(OrientationHelper.HORIZONTAL);
                        }
                    }
                }
                if (groupHeadView != null) {
                    mListener.changeGroupHeadView(mGroupHeadLayout, dataPosition);
                    isChange = true;
                }
                if (isAutoSetHeadWidthHeightByGroupItemView && isChange) {
                    RecyclerView.ViewHolder changeViewHolder = parent.findViewHolderForAdapterPosition(currGroupHeadPosition);
                    if (changeViewHolder != null) {
                        setGroupHeadLayoutWH(changeViewHolder.itemView, orientation);
                    }
                }
            }
            View currView = null;
            int offset = 0;
            if (orientation == OrientationHelper.VERTICAL) {
                currView = parent.findChildViewUnder(0, mGroupHeadLayout.getChildHeight());
                if (currView != null) {
                    if (isGroupHeadLayout(parent, currView) && currView.getTop() > 0) {
                        offset = currView.getTop() - mGroupHeadLayout.getChildHeight();
                    }
                }
            } else if (orientation == OrientationHelper.HORIZONTAL) {
                currView = parent.findChildViewUnder(mGroupHeadLayout.getChildWidth(), 0);
                if (currView != null) {
                    if (isGroupHeadLayout(parent, currView) && currView.getLeft() > 0) {
                        offset = currView.getLeft() - mGroupHeadLayout.getChildWidth();
                    }
                }
            }
            if (currView != null) {
                mGroupHeadLayout.scrollChild(offset, orientation);
                mGroupHeadLayout.setVisibility(View.VISIBLE);
            }
        } else {
            mGroupHeadLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置Head与对应的groupitem等宽 或 等高
     */
    private void setGroupHeadLayoutWH(View view, int orientation) {
        if (isAutoSetHeadWidthHeightByGroupItemView && view != null) {
            GroupItemLayout groupItemLayout = getGroupItemLayoutByView(view);
            if (groupItemLayout != null) {
                View groupItemView = groupItemLayout.getGroupItemView();
                if (groupItemView != null) {
                    if (orientation == OrientationHelper.VERTICAL) {
                        ViewGroup.LayoutParams params = mGroupHeadLayout.getLayoutParams();
                        params.width = groupItemView.getMeasuredWidth();
                        mGroupHeadLayout.setLayoutParams(params);
                    } else if (orientation == OrientationHelper.HORIZONTAL) {
                        ViewGroup.LayoutParams params = mGroupHeadLayout.getLayoutParams();
                        params.height = groupItemView.getMeasuredHeight();
                        mGroupHeadLayout.setLayoutParams(params);
                    }
                }
            }
        }
    }

    /**
     * 获取GroupItemLayout
     */
    private GroupItemLayout getGroupItemLayoutByView(View view) {
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
     * 查找当前第一个显示的视图位置
     */
    private int findFirstVisiblePosition(RecyclerView parent) {
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
     * 获取RecyclerView是横|纵排列
     */
    private int getOrientation(RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        }
        return 0;
    }

    /**
     * 向上寻找最近的一个Head的位置(包括参数位置)
     */
    private int findUpGroupHeadPosition(int formPosition) {
        for (int i = formPosition; i >= 0; i--) {
            if (checkGroups.containsKey(i) && checkGroups.get(i)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 向下寻找最近的一个Head的位置(不包括参数位置)
     */
    private int findDownGroupHeadPosition(int formPosition, int count) {
        for (int i = formPosition; i < count; i++) {
            if (checkGroups.containsKey(i) && checkGroups.get(i)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * 检查pLayout是否带有Head
     */
    private boolean isGroupHeadLayout(RecyclerView parent, View pLayout) {
        final int viewPosition = parent.getChildAdapterPosition(pLayout);
        if (viewPosition == RecyclerView.NO_POSITION) {
            return false;
        }
        if (checkGroups.containsKey(viewPosition) && checkGroups.get(viewPosition)) {
            return true;
        }
        return false;
    }

}
