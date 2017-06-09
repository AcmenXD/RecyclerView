package com.acmenxd.recyclerview.delegate;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 多类型item管理类
 */
public final class ItemDelegateManager<T> {
    SparseArrayCompat<ItemDelegate<T>> delegates = new SparseArrayCompat();

    public ItemDelegateManager<T> addDelegate(@NonNull ItemDelegate<T> delegate) {
        int viewType = delegates.size();
        if (delegate != null) {
            delegates.put(viewType, delegate);
            viewType++;
        }
        return this;
    }

    public ItemDelegateManager<T> addDelegate(@IntRange(from = 0) int viewType, @NonNull ItemDelegate<T> delegate) {
        if (delegates.get(viewType) != null) {
            throw new IllegalArgumentException(
                    "viewType 已被占用 = " + viewType + ". 被占用的viewType对象是:" + delegates.get(viewType));
        }
        delegates.put(viewType, delegate);
        return this;
    }

    public ItemDelegateManager<T> removeDelegate(@NonNull ItemDelegate<T> delegate) {
        if (delegate == null) {
            throw new NullPointerException("ItemDelegate 不能为null");
        }
        int indexToRemove = delegates.indexOfValue(delegate);
        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }
        return this;
    }

    public ItemDelegateManager<T> removeDelegate(@IntRange(from = 0) int viewType) {
        int indexToRemove = delegates.indexOfKey(viewType);
        if (indexToRemove >= 0) {
            delegates.removeAt(indexToRemove);
        }
        return this;
    }

    public int getItemViewType(@NonNull T data, @IntRange(from = 0) int dataPosition) {
        int delegatesCount = delegates.size();
        for (int i = delegatesCount - 1; i >= 0; i--) {
            ItemDelegate<T> delegate = delegates.valueAt(i);
            if (delegate.isItemViewType(data, dataPosition)) {
                return delegates.keyAt(i);
            }
        }
        throw new IllegalArgumentException("ItemDelegate 无匹配, dataPosition=" + dataPosition);
    }

    public ItemDelegate getItemViewDelegate(@IntRange(from = 0) int viewType) {
        return delegates.get(viewType);
    }

    public int getItemViewDelegateCount() {
        return delegates.size();
    }

    public void convert(@NonNull ViewHolder viewHolder, @NonNull T data, @IntRange(from = 0) int dataPosition) {
        int delegatesCount = getItemViewDelegateCount();
        for (int i = 0; i < delegatesCount; i++) {
            ItemDelegate<T> delegate = delegates.valueAt(i);
            if (delegate.isItemViewType(data, dataPosition)) {
                delegate.convert(viewHolder, data, dataPosition);
                return;
            }
        }
        throw new IllegalArgumentException("ItemDelegate 无匹配, dataPosition=" + dataPosition);
    }

}
