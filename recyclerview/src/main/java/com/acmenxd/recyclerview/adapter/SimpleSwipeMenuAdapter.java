package com.acmenxd.recyclerview.adapter;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;

import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.listener.OnSwipeMenuListener;

import java.util.List;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 公共滑动菜单Adapter,简化了RecyclerView.Adapter并实现了滑动菜单功能
 */
public abstract class SimpleSwipeMenuAdapter<T> extends MultiItemTypeSwipeMenuAdapter<T> {
    public SimpleSwipeMenuAdapter(@LayoutRes final int layoutId, @NonNull List<T> datas, @NonNull OnSwipeMenuListener pSwipeMenuListener) {
        super(datas, pSwipeMenuListener);
        addItemViewDelegate(new ItemDelegate<T>() {
            @Override
            public int getItemViewLayoutId() {
                return layoutId;
            }

            @Override
            public boolean isItemViewType(T item, int dataPosition) {
                return true;
            }

            @Override
            public void convert(ViewHolder viewHolder, T item, int dataPosition) {
                SimpleSwipeMenuAdapter.this.convert(viewHolder, item, dataPosition);
            }
        });
    }

    public abstract void convert(@NonNull ViewHolder viewHolder, @NonNull T data, @IntRange(from = 0) int dataPosition);
}
