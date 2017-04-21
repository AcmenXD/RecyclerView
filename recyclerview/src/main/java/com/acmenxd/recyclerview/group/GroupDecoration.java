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

import com.acmenxd.recyclerview.adapter.AdapterUtils;
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
    private RecyclerView mRecyclerView;
    private GroupHeadLayout mGroupHeadLayout; // Head根布局
    private GroupListener mListener; // 回调监听
    private Map<Integer, Boolean> checkGroups; // 存储groupItem是否带有Head
    private int direction = 1; // 1:向上  2:向下  3:向左   4:向右

    private int currGroupHeadPosition = -1; // 记录当前显示的Head位置
    private int groupItemTypeNum = 1; // groupItem的类型数量
    private int groupItemLevelNum = 1; // groupItem的层级数量
    private int groupItemPosition = GroupListener.ITEM_OUT_TOP; // groupItem的显示位置
    private boolean isAutoSetGroupHeadViewWidthHeightByGroupItemView = false; // 是否自动配置Head的宽高,根据当前GroupItem

    public GroupDecoration(@NonNull GroupHeadLayout pGroupHeadLayout, @NonNull GroupListener pListener) {
        mGroupHeadLayout = pGroupHeadLayout;
        mListener = pListener;
        checkGroups = new HashMap<>();
        groupItemTypeNum = pListener.getGroupItemTypeNum();
        groupItemLevelNum = pListener.getGroupItemLevelNum();
        isAutoSetGroupHeadViewWidthHeightByGroupItemView = pListener.isAutoSetGroupHeadViewWidthHeightByGroupItemView();
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
                boolean isCreateGroupItemView = mListener.isCreateGroupItemView(dataPosition);
                if (isCreateGroupItemView) {
                    View groupItemView = groupItemLayout.getGroupItemView();
                    if (groupItemView == null || groupItemTypeNum > 1) {
                        groupItemView = mListener.getGroupItemView(groupItemLayout, dataPosition);
                        if (groupItemView != null) {
                            groupItemLayout.addGroupItemView(groupItemView,AdapterUtils.getOrientation(parent),groupItemPosition);
                        }
                    }
                    if (groupItemView != null) {
                        mListener.changeGroupItemView(groupItemView, dataPosition);
                       // groupItemLayout.checkChangeWH(AdapterUtils.getOrientation(parent), groupItemPosition);
                        checkGroups.put(viewPosition, true);
                    }
                } else {
                    groupItemLayout.removeGroupItemView();
                }
            }
        }
        if (mRecyclerView == null) {
            mRecyclerView = parent;
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
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        super.onDrawOver(c, parent, state);
        int firstVisiblePosition = findFirstVisiblePosition(parent);
        int groupHeadPosition = findUpGroupHeadPosition(firstVisiblePosition);
        boolean invisible = false;
        if (groupHeadPosition >= 0 && firstVisiblePosition >= groupHeadPosition) {
            boolean isChange = false;
            int orientation = AdapterUtils.getOrientation(parent);
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
                if (isAutoSetGroupHeadViewWidthHeightByGroupItemView && isChange) {
                    RecyclerView.ViewHolder changeViewHolder = parent.findViewHolderForAdapterPosition(currGroupHeadPosition);
                    if (changeViewHolder != null && changeViewHolder.itemView != null) {
                        setGroupHeadLayoutWH(changeViewHolder.itemView, orientation);
                    }
                }
            }
            View currView = null;
            int offset = 0;
            if (orientation == OrientationHelper.VERTICAL) {
                int height = mGroupHeadLayout.getChildHeight();
                int top = mGroupHeadLayout.getChildTop();
                if (groupItemTypeNum <= 1) {
                    currView = parent.findChildViewUnder(0, height);
                } else {
                    if (direction == 1) {
                        currView = parent.findChildViewUnder(0, height + top);
                    } else if (direction == 2) {
                        int currViewY = height;
                        int nextGroupHeadPosition = findDownGroupHeadPosition(currGroupHeadPosition, parent.getAdapter().getItemCount());
                        if (nextGroupHeadPosition >= 0) {
                            RecyclerView.ViewHolder nextViewHolder = parent.findViewHolderForAdapterPosition(nextGroupHeadPosition);
                            if (nextViewHolder != null && nextViewHolder.itemView != null) {
                                int viewT = nextViewHolder.itemView.getTop();
                                if (viewT >= 0 && viewT <= height) {
                                    currViewY = nextViewHolder.itemView.getTop();
                                }
                            }
                        }
                        currView = parent.findChildViewUnder(0, currViewY);
                    }
                }
                if (currView != null) {
                    int index = parent.getChildAdapterPosition(currView);
                    int currTop = currView.getTop();
                    if (height == 0) {
                        invisible = true;
                    }
                    if ((currGroupHeadPosition == index && currTop == 0)) {
                        invisible = true;
                    }
                    if (currTop > 0 && isGroupHeadLayout(parent, currView)) {
                        offset = currTop - height;
                    }
                }
            } else if (orientation == OrientationHelper.HORIZONTAL) {
                int width = mGroupHeadLayout.getChildWidth();
                int left = mGroupHeadLayout.getChildLeft();
                if (groupItemTypeNum <= 1) {
                    currView = parent.findChildViewUnder(width, 0);
                } else {
                    if (direction == 1) {
                        currView = parent.findChildViewUnder(width + left, 0);
                    } else if (direction == 2) {
                        int currViewX = width;
                        int nextGroupHeadPosition = findDownGroupHeadPosition(currGroupHeadPosition, parent.getAdapter().getItemCount());
                        if (nextGroupHeadPosition >= 0) {
                            RecyclerView.ViewHolder nextViewHolder = parent.findViewHolderForAdapterPosition(nextGroupHeadPosition);
                            if (nextViewHolder != null && nextViewHolder.itemView != null) {
                                int viewLeft = nextViewHolder.itemView.getLeft();
                                if (viewLeft >= 0 && viewLeft <= width) {
                                    currViewX = nextViewHolder.itemView.getLeft();
                                }
                            }
                        }
                        currView = parent.findChildViewUnder(currViewX, 0);
                    }
                }
                if (currView != null) {
                    int index = parent.getChildAdapterPosition(currView);
                    int currLeft = currView.getLeft();
                    if ((currGroupHeadPosition == index && currLeft == 0) || width == 0) {
                        invisible = true;
                    }
                    if (currLeft > 0 && isGroupHeadLayout(parent, currView)) {
                        offset = currLeft - width;
                    }
                }
            }
            if (currView != null) {
                mGroupHeadLayout.scrollChild(offset, orientation);
                mGroupHeadLayout.setVisibility(View.VISIBLE);
            }
        } else {
            invisible = true;
        }
        if (invisible) {
            mGroupHeadLayout.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 设置Head与对应的groupitem等宽 或 等高
     */
    private void setGroupHeadLayoutWH(View view, int orientation) {
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
        for (int i = formPosition + 1; i < count; i++) {
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
