package com.acmenxd.recyclerview.listener;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 11:54
 * @detail item 拖拽变换 事件回调
 */
public abstract class ItemDragCallback {
    /**
     * 支持向左拖拽
     */
    public static final int LEFT_Drag = ItemTouchHelper.LEFT;
    /**
     * 支持向右拖拽
     */
    public static final int RIGHT_Drag = ItemTouchHelper.RIGHT;
    /**
     * 支持向上拖拽
     */
    public static final int UP_Drag = ItemTouchHelper.UP;
    /**
     * 支持向下拖拽
     */
    public static final int DOWN_Drag = ItemTouchHelper.DOWN;

    private int mDragFlags; // 拖拽变换方向

    public ItemDragCallback() {
        this(0);
    }

    /**
     * @param pDragFlags 拖拽变换方向(可设置多个方向)
     */
    public ItemDragCallback(int pDragFlags) {
        this.mDragFlags = pDragFlags;
    }

    protected int getDragFlags() {
        return mDragFlags;
    }

    /**
     * 由于OnItemTransformListener未持有recyclerView的数据
     * 所以必须实现此方法,用来变换数据,之后会自动同步视图
     *
     * @param fromDataPosition 定位数据的position
     * @param toDataPosition   定位数据的position
     * @param fromViewPosition 定位adapterPosition
     * @param toViewPosition   定位adapterPosition
     * @return true:自动更新视图  false:不自动更新视图,需手动更新
     */
    public abstract boolean onTransformData(@NonNull RecyclerView.ViewHolder fromViewHolder,
                                            @NonNull RecyclerView.ViewHolder toViewHolder,
                                            @IntRange(from = 0) int fromDataPosition, @IntRange(from = 0) int toDataPosition,
                                            @IntRange(from = 0) int fromViewPosition, @IntRange(from = 0) int toViewPosition);

    /**
     * 如有不能拖动的item项,重写此方法.
     *
     * @param dataPosition 定位数据的position
     * @return true->可以拖动 false->不可拖动
     */
    public boolean onTransformCheck(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition) {
        return true;
    }

    /**
     * 如有不能拖动"到"的item项,重写此方法.
     *
     * @param dataPosition 定位数据的position
     * @return true->可以拖动"到" false->不可拖动"到"
     */
    public boolean onTransformToCheck(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition) {
        return true;
    }

    /**
     * 当item开始拖动的时候回调
     */
    public void onSelectedStart(@NonNull RecyclerView.ViewHolder viewHolder) {
    }

    /**
     * 当item停止拖动的时候回调
     */
    public void onSelectedEnd(@NonNull RecyclerView.ViewHolder viewHolder) {
    }

}


