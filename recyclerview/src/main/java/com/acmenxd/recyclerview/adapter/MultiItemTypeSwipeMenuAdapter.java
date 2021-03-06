package com.acmenxd.recyclerview.adapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.R;
import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.listener.OnSwipeMenuListener;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuLayout;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuView;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuViewLeft;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuViewRight;

import java.util.List;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 多类型item的侧滑菜单 Adapter,简化了RecyclerView.Adapter并实现了滑动菜单功能
 */
public class MultiItemTypeSwipeMenuAdapter<T> extends MultiItemTypeAdapter<T> {
    private OnSwipeMenuListener mSwipeMenuListener;

    public MultiItemTypeSwipeMenuAdapter(@NonNull List<T> datas, @NonNull OnSwipeMenuListener pSwipeMenuListener) {
        super(datas);
        this.mSwipeMenuListener = pSwipeMenuListener;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView.addOnItemTouchListener(new OnItemSwipeListener());
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, @IntRange(from = 0) int viewType) {
        ItemDelegate itemDelegate = mItemDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemDelegate.getItemViewLayoutId();
        SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_swipe_menu, parent, false);
        swipeMenuLayout.setRecyclerView(mRecyclerView);
        // 内容
        ViewGroup viewGroup = (ViewGroup) swipeMenuLayout.findViewById(R.id.swipe_menu_content);
        View contentView = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
        viewGroup.addView(contentView);
        // 创建viewHolder
        ViewHolder viewHolder = ViewHolder.createViewHolder(mContext, swipeMenuLayout);
        // 设置布局
        ViewGroup.LayoutParams contentViewParams = contentView.getLayoutParams();
        ViewGroup.LayoutParams swipeMenuLayoutParams = swipeMenuLayout.getLayoutParams();
        ViewGroup.LayoutParams viewGroupParams = viewGroup.getLayoutParams();

        int orientation = AdapterUtils.getOrientation(mRecyclerView);
        if (contentViewParams.width == ViewGroup.LayoutParams.MATCH_PARENT || contentViewParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (orientation == OrientationHelper.VERTICAL) {
                swipeMenuLayoutParams.width = contentViewParams.width;
            }
            viewGroupParams.width = contentViewParams.width;
        }
        if (contentViewParams.height == ViewGroup.LayoutParams.MATCH_PARENT || contentViewParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
            if (orientation == OrientationHelper.HORIZONTAL) {
                swipeMenuLayoutParams.height = contentViewParams.height;
            }
            viewGroupParams.height = contentViewParams.height;
        }
        swipeMenuLayout.setLayoutParams(swipeMenuLayoutParams);
        viewGroup.setLayoutParams(viewGroupParams);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, @IntRange(from = 0) int dataPosition) {
        SwipeMenuLayout swipeMenuLayout = (SwipeMenuLayout) viewHolder.itemView;
        swipeMenuLayout.resetMenu();
        // 左侧按钮
        SwipeMenuViewLeft swipeMenuView_left = (SwipeMenuViewLeft) swipeMenuLayout.findViewById(R.id.swipe_menu_left);
        int[] leftIds = null;
        if (mSwipeMenuListener != null) {
            leftIds = mSwipeMenuListener.getLeftMenuLayoutIds(dataPosition);
        }
        if (leftIds != null && leftIds.length > 0) {
            swipeMenuView_left.addMenuView(LayoutInflater.from(swipeMenuView_left.getContext()).inflate(leftIds[0], swipeMenuView_left, false), leftIds, SwipeMenuView.LEFT_DIRECTION, mRecyclerView, mSwipeMenuListener);
        }
        // 右侧按钮
        SwipeMenuViewRight swipeMenuView_right = (SwipeMenuViewRight) swipeMenuLayout.findViewById(R.id.swipe_menu_right);
        int[] rightIds = null;
        if (mSwipeMenuListener != null) {
            rightIds = mSwipeMenuListener.getRightMenuLayoutIds(dataPosition);
        }
        if (rightIds != null && rightIds.length > 0) {
            swipeMenuView_right.addMenuView(LayoutInflater.from(swipeMenuView_right.getContext()).inflate(rightIds[0], swipeMenuView_right, false), rightIds, SwipeMenuView.RIGHT_DIRECTION, mRecyclerView, mSwipeMenuListener);
        }
        if (mDatas.size() > dataPosition) {
            mItemDelegateManager.convert(viewHolder, mDatas.get(dataPosition), dataPosition);
        }
    }

    /**
     * 滑动菜单
     */
    private class OnItemSwipeListener implements RecyclerView.OnItemTouchListener {
        SwipeMenuLayout mTargetSwipeMenuLayout;

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            if (e.getAction() == MotionEvent.ACTION_DOWN) {
                mTargetSwipeMenuLayout = null;
                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child instanceof SwipeMenuLayout) {
                    mTargetSwipeMenuLayout = (SwipeMenuLayout) child;
                }
            }
            if (mTargetSwipeMenuLayout != null && rv.getScrollState() == mRecyclerView.SCROLL_STATE_IDLE) {
                return mTargetSwipeMenuLayout.onInterceptTouchEventCustom(e);
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            if (mTargetSwipeMenuLayout != null && rv.getScrollState() == mRecyclerView.SCROLL_STATE_IDLE) {
                mTargetSwipeMenuLayout.onTouchEventCustom(e);
            }
            if (e.getAction() == MotionEvent.ACTION_UP || e.getAction() == MotionEvent.ACTION_CANCEL) {
                mTargetSwipeMenuLayout = null;
            }
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
}
