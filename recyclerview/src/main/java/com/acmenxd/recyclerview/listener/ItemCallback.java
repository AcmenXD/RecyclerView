package com.acmenxd.recyclerview.listener;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/15 16:03
 * @detail item 单击 & 长按 事件回调
 */
public abstract class ItemCallback {
    // 当前开启长按事件, 解决事件冲突
    private boolean isLongEnabled = true;

    protected boolean isLongEnabled() {
        return isLongEnabled;
    }

    protected void setLongEnabled(boolean pLongEnabled) {
        isLongEnabled = pLongEnabled;
    }

    /**
     * item单击事件回调
     *
     * @param dataPosition 定位数据的position
     */
    public abstract void onClick(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition);

    /**
     * item长按事件回调
     *
     * @param dataPosition 定位数据的position
     */
    public void onLongClick(@NonNull RecyclerView.ViewHolder viewHolder, @IntRange(from = 0) int dataPosition) {
    }
}

