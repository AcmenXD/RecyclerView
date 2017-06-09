package com.acmenxd.recyclerview.listener;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/20 12:00
 * @detail item 滑动 事件回调
 */
public abstract class ItemSwipeCallback {
    /**
     * 支持向左滑动
     */
    public static final int LEFT_Swipe = ItemTouchHelper.START;
    /**
     * 支持向右滑动
     */
    public static final int RIGHT_Swipe = ItemTouchHelper.END;
    /**
     * 支持向上滑动
     */
    public static final int UP_Swipe = ItemTouchHelper.UP;
    /**
     * 支持向下滑动
     */
    public static final int DOWN_Swipe = ItemTouchHelper.DOWN;

    private int mSwipeFlags;// 滑动方向

    public ItemSwipeCallback() {
        this(0);
    }

    /**
     * @param pSwipeFlags 滑动删除方向(可设置多个方向)
     */
    public ItemSwipeCallback(int pSwipeFlags) {
        this.mSwipeFlags = pSwipeFlags;
    }

    protected int getSwipeFlags() {
        return mSwipeFlags;
    }

    /**
     * 由于OnItemDeleteListener未持有recyclerView的数据
     * 所以必须实现此方法,用来删除数据
     *
     * @param dataPosition 定位数据的position
     * @param viewPosition 定位adapterPosition
     * @return true:自动更新视图  false:不自动更新视图,需手动更新
     */
    public abstract boolean onDeleteData(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition, @IntRange(from = 0) int viewPosition);

    /**
     * 如有不能删除的item项,重写此方法.
     *
     * @param dataPosition 定位数据的position
     * @return true->可以滑动删除 false->不可滑动删除
     */
    public boolean onDeleteCheck(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition) {
        return true;
    }
}
