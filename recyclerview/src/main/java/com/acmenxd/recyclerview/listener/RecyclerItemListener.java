package com.acmenxd.recyclerview.listener;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.group.GroupHeadLayout;
import com.acmenxd.recyclerview.group.GroupItemLayout;
import com.acmenxd.recyclerview.swipemenu.SwipeMenuLayout;
import com.acmenxd.recyclerview.wrapper.WrapperUtils;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/20 11:54
 * @detail RecyclerView -> 各种事件回调
 */
public final class RecyclerItemListener {
    private RecyclerView mRecyclerView;
    private OnItemTouchListenerCallback mOnItemTouchListenerCallback;
    private ItemTouchHelperCallback mItemTouchHelperCallback;
    private ItemCallback mItemCallback;//单击 & 长按 事件
    private ItemSwipeCallback mSwipeCallback;//滑动删除
    private ItemDragCallback mDragCallback;//拖拽变换

    public RecyclerItemListener(@NonNull RecyclerView pRecyclerView) {
        mRecyclerView = pRecyclerView;
    }

    /**
     * 设置RecyclerView支持 单击 & 长按 事件
     *
     * @param pItemCallback 单击 & 长按回调函数
     */
    public void setItemCallback(@NonNull ItemCallback pItemCallback) {
        mItemCallback = pItemCallback;
        if (mOnItemTouchListenerCallback == null) {
            mOnItemTouchListenerCallback = new OnItemTouchListenerCallback();
            mRecyclerView.addOnItemTouchListener(mOnItemTouchListenerCallback);
        }
    }

    /**
     * 设置RecyclerView支持 滑动删除
     *
     * @param pSwipeCallback 滑动删除 回调函数
     */
    public void setItemSwipeCallback(@NonNull ItemSwipeCallback pSwipeCallback) {
        mSwipeCallback = pSwipeCallback;
        if (mItemTouchHelperCallback == null) {
            mItemTouchHelperCallback = new ItemTouchHelperCallback();
            new ItemTouchHelper(mItemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        }
        mItemTouchHelperCallback.setSwipeFlags(getSwipeFlags(pSwipeCallback.getSwipeFlags()));
    }

    /**
     * 设置RecyclerView支持 拖拽变换位置
     *
     * @param pDragCallback 拖拽变换 回调函数
     */
    public void setItemDragCallback(@NonNull ItemDragCallback pDragCallback) {
        mDragCallback = pDragCallback;
        if (mItemTouchHelperCallback == null) {
            mItemTouchHelperCallback = new ItemTouchHelperCallback();
            new ItemTouchHelper(mItemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        }
        mItemTouchHelperCallback.setDragFlags(getDragFlags(pDragCallback.getDragFlags()));
    }

    private int getSwipeFlags(int pSwipeFlags) {
        int orientation = AdapterUtils.getOrientation(mRecyclerView);
        if (pSwipeFlags == 0) {
            if (orientation == OrientationHelper.VERTICAL) {
                pSwipeFlags = ItemSwipeCallback.LEFT_Swipe | ItemSwipeCallback.RIGHT_Swipe;
            } else {
                pSwipeFlags = ItemSwipeCallback.UP_Swipe | ItemSwipeCallback.DOWN_Swipe;
            }
        }
        return pSwipeFlags;
    }

    private int getDragFlags(int pDragFlags) {
        int orientation = AdapterUtils.getOrientation(mRecyclerView);
        if (pDragFlags == 0) {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof StaggeredGridLayoutManager) {
                pDragFlags = ItemDragCallback.UP_Drag | ItemDragCallback.DOWN_Drag | ItemDragCallback.LEFT_Drag | ItemDragCallback.RIGHT_Drag;
            } else if (layoutManager instanceof GridLayoutManager) {
                pDragFlags = ItemDragCallback.UP_Drag | ItemDragCallback.DOWN_Drag | ItemDragCallback.LEFT_Drag | ItemDragCallback.RIGHT_Drag;
            } else if (layoutManager instanceof LinearLayoutManager) {
                if (orientation == OrientationHelper.VERTICAL) {
                    pDragFlags = ItemDragCallback.UP_Drag | ItemDragCallback.DOWN_Drag;
                } else {
                    pDragFlags = ItemDragCallback.LEFT_Drag | ItemDragCallback.RIGHT_Drag;
                }
            }
        }
        return pDragFlags;
    }

    /**
     * item 单击 & 长按 Touch类
     */
    private class OnItemTouchListenerCallback implements RecyclerView.OnItemTouchListener {
        private GestureDetectorCompat mGestureDetector;

        private boolean isDownInView(View view, int x, int y) {
            Rect rect = new Rect();
            view.getDrawingRect(rect);
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            rect.left = location[0];
            rect.top = location[1];
            rect.right = rect.right + location[0];
            rect.bottom = rect.bottom + location[1];
            return rect.contains(x, y);
        }

        public OnItemTouchListenerCallback() {
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                                if (mItemCallback != null) {
                                    View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                                    if (child != null) {
                                        boolean isMenuOpen = false;
                                        if (child instanceof SwipeMenuLayout && (((SwipeMenuLayout) child).isMenuOpen() || ((SwipeMenuLayout) child).isLoseOnceTouch())) {
                                            isMenuOpen = true;
                                        }
                                        boolean isGroup = false;
                                        if (child instanceof GroupItemLayout && ((GroupItemLayout) child).getChildAt(0) instanceof GroupHeadLayout) {
                                            isGroup = isDownInView(((GroupItemLayout) child).getChildAt(0), (int) e.getRawX(), (int) e.getRawY());
                                        }
                                        if (child.isEnabled() && !isMenuOpen && !isGroup) {
                                            int viewPosition = mRecyclerView.getChildAdapterPosition(child);
                                            int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                                            boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                                            if (!isWrapper) {
                                                mItemCallback.onClick(mRecyclerView.getChildViewHolder(child), dataPosition);
                                            }
                                        }
                                    }
                                }
                            }
                            return super.onSingleTapUp(e);
                        }

                        @Override
                        public void onLongPress(MotionEvent e) {
                            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                                if (mItemCallback != null) {
                                    if (mItemCallback.isLongEnabled()) {
                                        View child = mRecyclerView.findChildViewUnder(e.getX(), e.getY());
                                        if (child != null) {
                                            boolean isMenuOpen = false;
                                            if (child instanceof SwipeMenuLayout && (((SwipeMenuLayout) child).isMenuOpen() || ((SwipeMenuLayout) child).isLoseOnceTouch())) {
                                                isMenuOpen = true;
                                            }
                                            if (child.isEnabled() && !isMenuOpen) {
                                                int viewPosition = mRecyclerView.getChildAdapterPosition(child);
                                                int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                                                boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                                                if (!isWrapper) {
                                                    mItemCallback.onLongClick(mRecyclerView.getChildViewHolder(child), dataPosition);
                                                }
                                            }
                                        }
                                    } else {
                                        mItemCallback.setLongEnabled(true);
                                    }
                                }
                            }
                        }
                    });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            mGestureDetector.onTouchEvent(e);
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    /**
     * item 滑动删除 & 拖拽变换 Touch类
     */
    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private int mSwipeFlags;
        private int mDragFlags;
        private boolean isSelectedStart = false; // 是否进入拖拽选中状态

        public void setSwipeFlags(int pSwipeFlags) {
            mSwipeFlags = pSwipeFlags;
        }

        public void setDragFlags(int pDragFlags) {
            mDragFlags = pDragFlags;
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = 0;
            int swipeFlags = 0;
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                View child = viewHolder.itemView;
                if (child instanceof SwipeMenuLayout && ((SwipeMenuLayout) child).isLoseOnceTouch()) {

                } else {
                    int viewPosition = viewHolder.getAdapterPosition();
                    if (viewPosition >= 0) {
                        int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                        boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                        if (mDragCallback != null) {
                            if (mDragCallback.onTransformCheck(viewHolder, dataPosition)) {
                                if (!isWrapper) {
                                    dragFlags = mDragFlags;
                                }
                            }
                        }
                        if (mSwipeCallback != null) {
                            if (mSwipeCallback.onDeleteCheck(viewHolder, dataPosition)) {
                                if (!isWrapper) {
                                    swipeFlags = mSwipeFlags;
                                }
                            }
                        }
                    }
                }
            }
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (mDragCallback != null) {
                    int fromViewPosition = viewHolder.getAdapterPosition();
                    int toViewPosition = target.getAdapterPosition();
                    if (fromViewPosition >= 0 && toViewPosition >= 0) {
                        int dp = WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                        int fromDataPosition = fromViewPosition - dp;
                        int toDataPosition = toViewPosition - dp;
                        boolean isFromWrapper = WrapperUtils.isItemWrapper(mRecyclerView, fromViewPosition);
                        boolean isToWrapper = WrapperUtils.isItemWrapper(mRecyclerView, toViewPosition);
                        if (mDragCallback.onTransformCheck(viewHolder, fromDataPosition)
                                && mDragCallback.onTransformToCheck(viewHolder, toDataPosition)) {
                            if (!isFromWrapper && !isToWrapper) {
                                View child = viewHolder.itemView;
                                if (child instanceof SwipeMenuLayout && ((SwipeMenuLayout) child).isMenuOpen()) {
                                    ((SwipeMenuLayout) child).smoothCloseMenu();
                                }
                                if (mDragCallback.onTransformData(viewHolder, target, fromDataPosition, toDataPosition, fromViewPosition, toViewPosition)) {
                                    mRecyclerView.getAdapter().notifyItemMoved(fromViewPosition, toViewPosition);
                                }
                            }
                        }
                    }
                }
            }
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (mSwipeCallback != null) {
                    int viewPosition = viewHolder.getAdapterPosition();
                    if (viewPosition >= 0) {
                        int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                        boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                        if (!isWrapper) {
                            View child = viewHolder.itemView;
                            if (child instanceof SwipeMenuLayout && ((SwipeMenuLayout) child).isMenuOpen()) {
                                ((SwipeMenuLayout) child).smoothCloseMenu();
                            }
                            if (mSwipeCallback.onDeleteData(viewHolder, dataPosition, viewPosition)) {
                                mRecyclerView.getAdapter().notifyItemRemoved(viewPosition);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    float alpha = 1;
                    int orientation = AdapterUtils.getOrientation(recyclerView);
                    if (orientation == OrientationHelper.VERTICAL) {
                        alpha = 1 - Math.abs(dX) / viewHolder.itemView.getWidth();
                    } else {
                        alpha = 1 - Math.abs(dY) / viewHolder.itemView.getHeight();
                    }
                    viewHolder.itemView.setAlpha(alpha);
                }
            }
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    if (mItemCallback != null) {
                        mItemCallback.setLongEnabled(false);
                    }
                    if (mDragCallback != null) {
                        mDragCallback.onSelectedStart(viewHolder);
                        isSelectedStart = true;
                    }
                    Log.v("AcmenXD", "item进入拖拽状态");
                } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    if (mItemCallback != null) {
                        mItemCallback.setLongEnabled(false);
                    }
                    Log.v("AcmenXD", "item进入滑动状态");
                } else if (actionState == ItemTouchHelper.ACTION_STATE_IDLE) {
                    Log.v("AcmenXD", "item无状态");
                }
            }
        }

        @Override
        public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            super.clearView(recyclerView, viewHolder);
            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                if (mDragCallback != null && isSelectedStart) {
                    mDragCallback.onSelectedEnd(viewHolder);
                    isSelectedStart = false;
                }
            }
        }
    }
}
