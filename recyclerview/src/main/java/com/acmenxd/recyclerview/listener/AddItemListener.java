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
public final class AddItemListener {
    /**
     * 设置RecyclerView支持 单击 & 长按 事件
     *
     * @param pRecyclerView recyclerView实例
     * @param pItemCallback 单击 & 长按回调函数
     */
    public AddItemListener(@NonNull RecyclerView pRecyclerView, @NonNull ItemCallback pItemCallback) {
        if (pItemCallback != null) {
            pRecyclerView.addOnItemTouchListener(new OnItemTouchListenerCallback(pRecyclerView, pItemCallback));
        }
    }

    /**
     * 设置RecyclerView支持 滑动删除
     *
     * @param pRecyclerView  recyclerView实例
     * @param pSwipeCallback 滑动删除 回调函数
     */
    public AddItemListener(@NonNull RecyclerView pRecyclerView, @NonNull ItemSwipeCallback pSwipeCallback) {
        if (pSwipeCallback != null) {
            createCallback(pRecyclerView, pSwipeCallback.getSwipeFlags(), 0, pSwipeCallback, null, null);
        }
    }

    /**
     * 设置RecyclerView支持 拖拽变换位置
     *
     * @param pRecyclerView recyclerView实例
     * @param pDragCallback 拖拽变换 回调函数
     */
    public AddItemListener(@NonNull RecyclerView pRecyclerView, @NonNull ItemDragCallback pDragCallback) {
        if (pDragCallback != null) {
            createCallback(pRecyclerView, 0, pDragCallback.getDragFlags(), null, pDragCallback, null);
        }
    }

    /**
     * 设置 单击&长按 & 滑动删除 & 拖拽变换
     * * 对应Callback设置为null,表示不支持此回调
     *
     * @param pRecyclerView  recyclerView实例
     * @param pItemCallback  单击 & 长按回调函数
     * @param pSwipeCallback 滑动删除 回调函数
     * @param pDragCallback  拖拽变换 回调函数
     */
    public AddItemListener(@NonNull RecyclerView pRecyclerView, ItemCallback pItemCallback, ItemSwipeCallback pSwipeCallback, ItemDragCallback pDragCallback) {
        int swipeFlags = 0;
        int dragFlags = 0;
        if (pSwipeCallback != null) {
            swipeFlags = pSwipeCallback.getSwipeFlags();
        }
        if (pDragCallback != null) {
            dragFlags = pDragCallback.getDragFlags();
        }
        createCallback(pRecyclerView, swipeFlags, dragFlags, pSwipeCallback, pDragCallback, pItemCallback);
        if (pItemCallback != null) {
            pRecyclerView.addOnItemTouchListener(new OnItemTouchListenerCallback(pRecyclerView, pItemCallback));
        }
    }

    /**
     * item 单击 & 长按 Touch类
     */
    private class OnItemTouchListenerCallback implements RecyclerView.OnItemTouchListener {
        private GestureDetectorCompat mGestureDetector;
        private RecyclerView mRecyclerView;
        private ItemCallback mItemCallBack;

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

        public OnItemTouchListenerCallback(@NonNull RecyclerView pRecyclerView, @NonNull ItemCallback pItemCallBack) {
            this.mRecyclerView = pRecyclerView;
            this.mItemCallBack = pItemCallBack;
            mGestureDetector = new GestureDetectorCompat(mRecyclerView.getContext(),
                    new GestureDetector.SimpleOnGestureListener() {
                        @Override
                        public boolean onSingleTapUp(MotionEvent e) {
                            if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                                if (mItemCallBack != null) {
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
                                                mItemCallBack.onClick(mRecyclerView.getChildViewHolder(child), dataPosition);
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
                                if (mItemCallBack != null) {
                                    if (mItemCallBack.isLongEnabled()) {
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
                                                    mItemCallBack.onLongClick(mRecyclerView.getChildViewHolder(child), dataPosition);
                                                }
                                            }
                                        }
                                    } else {
                                        mItemCallBack.setLongEnabled(true);
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
     * 设置RecyclerView支持 滑动删除 & 拖拽变换位置
     *
     * @param pRecyclerView  recyclerView实例
     * @param pSwipeFlags    滑动删除方向(可设置多个方向)
     * @param pDragFlags     拖拽变换方向(可设置多个方向)
     * @param pSwipeCallBack 滑动删除的回调函数,null表示屏蔽此功能
     * @param pDragCallBack  拖拽变换的回调函数,null表示屏蔽此功能
     */
    private void createCallback(@NonNull RecyclerView pRecyclerView, int pSwipeFlags, int pDragFlags, ItemSwipeCallback pSwipeCallBack, ItemDragCallback pDragCallBack, ItemCallback pItemCallback) {
        int[] result = getSwipeAndDrag(pRecyclerView, pSwipeFlags, pDragFlags);
        new ItemTouchHelper(new ItemTouchHelperCallback(pRecyclerView, result[0], result[1], pSwipeCallBack, pDragCallBack, pItemCallback)).attachToRecyclerView(pRecyclerView);
    }

    private int[] getSwipeAndDrag(@NonNull RecyclerView pRecyclerView, int pSwipeFlags, int pDragFlags) {
        int orientation = AdapterUtils.getOrientation(pRecyclerView);
        if (pSwipeFlags == 0) {
            if (orientation == OrientationHelper.VERTICAL) {
                pSwipeFlags = ItemSwipeCallback.LEFT_Swipe | ItemSwipeCallback.RIGHT_Swipe;
            } else {
                pSwipeFlags = ItemSwipeCallback.UP_Swipe | ItemSwipeCallback.DOWN_Swipe;
            }
        }
        if (pDragFlags == 0) {
            RecyclerView.LayoutManager layoutManager = pRecyclerView.getLayoutManager();
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
        return new int[]{pSwipeFlags, pDragFlags};
    }

    private class ItemTouchHelperCallback extends ItemTouchHelper.Callback {
        private RecyclerView mRecyclerView;
        private ItemSwipeCallback mSwipeCallBack;
        private ItemDragCallback mDragCallBack;
        private ItemCallback mItemCallback;
        private int mSwipeFlags;
        private int mDragFlags;
        private boolean isSelectedStart = false; // 是否进入拖拽选中状态

        public ItemTouchHelperCallback(@NonNull RecyclerView pRecyclerView, int pSwipeFlags, int pDragFlags, ItemSwipeCallback pSwipeCallBack, ItemDragCallback pDragCallBack, ItemCallback pItemCallback) {
            this.mRecyclerView = pRecyclerView;
            this.mSwipeCallBack = pSwipeCallBack;
            this.mDragCallBack = pDragCallBack;
            this.mItemCallback = pItemCallback;
            this.mSwipeFlags = pSwipeFlags;
            this.mDragFlags = pDragFlags;
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
                    int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                    boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                    if (mDragCallBack != null) {
                        if (mDragCallBack.onTransformCheck(viewHolder, dataPosition)) {
                            if (!isWrapper) {
                                dragFlags = mDragFlags;
                            }
                        }
                    }
                    if (mSwipeCallBack != null) {
                        if (mSwipeCallBack.onDeleteCheck(viewHolder, dataPosition)) {
                            if (!isWrapper) {
                                swipeFlags = mSwipeFlags;
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
                if (mDragCallBack != null) {
                    int dp = WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                    int fromViewPosition = viewHolder.getAdapterPosition();
                    int toViewPosition = target.getAdapterPosition();
                    int fromDataPosition = fromViewPosition - dp;
                    int toDataPosition = toViewPosition - dp;
                    boolean isFromWrapper = WrapperUtils.isItemWrapper(mRecyclerView, fromViewPosition);
                    boolean isToWrapper = WrapperUtils.isItemWrapper(mRecyclerView, toViewPosition);
                    if (mDragCallBack.onTransformCheck(viewHolder, fromDataPosition)
                            && mDragCallBack.onTransformToCheck(viewHolder, toDataPosition)) {
                        if (!isFromWrapper && !isToWrapper) {
                            View child = viewHolder.itemView;
                            if (child instanceof SwipeMenuLayout && ((SwipeMenuLayout) child).isMenuOpen()) {
                                ((SwipeMenuLayout) child).smoothCloseMenu();
                            }
                            if (mDragCallBack.onTransformData(viewHolder, target, fromDataPosition, toDataPosition, fromViewPosition, toViewPosition)) {
                                mRecyclerView.getAdapter().notifyItemMoved(fromViewPosition, toViewPosition);
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
                if (mSwipeCallBack != null) {
                    int viewPosition = viewHolder.getAdapterPosition();
                    int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
                    boolean isWrapper = WrapperUtils.isItemWrapper(mRecyclerView, viewPosition);
                    if (!isWrapper) {
                        View child = viewHolder.itemView;
                        if (child instanceof SwipeMenuLayout && ((SwipeMenuLayout) child).isMenuOpen()) {
                            ((SwipeMenuLayout) child).smoothCloseMenu();
                        }
                        if (mSwipeCallBack.onDeleteData(viewHolder, dataPosition, viewPosition)) {
                            mRecyclerView.getAdapter().notifyItemRemoved(viewPosition);
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
                    if (mDragCallBack != null) {
                        mDragCallBack.onSelectedStart(viewHolder);
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
                if (mDragCallBack != null && isSelectedStart) {
                    mDragCallBack.onSelectedEnd(viewHolder);
                }
            }
        }
    }
}
