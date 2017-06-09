package com.acmenxd.recyclerview.delegate;


import android.support.annotation.IntRange;
import android.support.annotation.NonNull;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 多类型item的代表类
 */
public interface ItemDelegate<T> {

    /**
     * 判断是否使用getItemViewLayoutId返回的布局文件
     */
    boolean isItemViewType(@NonNull T data, @IntRange(from = 0) int dataPosition);

    /**
     * 返回item的布局文件Id
     */
    int getItemViewLayoutId();

    /**
     * 刷新视图
     */
    void convert(@NonNull ViewHolder viewHolder, @NonNull T data, @IntRange(from = 0) int dataPosition);

}
