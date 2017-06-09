package com.acmenxd.recyclerview.adapter;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ViewHolder;

import java.util.List;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 公共Adapter,简化了RecyclerView.Adapter的实现
 */
public abstract class SimpleAdapter<T> extends MultiItemTypeAdapter<T> {
    protected Context mContext;
    protected RecyclerView mRecyclerView;
    protected int mLayoutId;
    protected List<T> mDatas;
    protected LayoutInflater mInflater;

    public SimpleAdapter(@NonNull final Context context, @NonNull final RecyclerView recyclerView, @LayoutRes final int layoutId, @NonNull List<T> datas) {
        super(context, recyclerView, datas);
        mContext = context;
        mRecyclerView = recyclerView;
        mLayoutId = layoutId;
        mDatas = datas;
        mInflater = LayoutInflater.from(context);

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
                SimpleAdapter.this.convert(viewHolder, item, dataPosition);
            }
        });
    }

    public abstract void convert(@NonNull ViewHolder viewHolder, @NonNull T item, @IntRange(from = 0) int dataPosition);

}
