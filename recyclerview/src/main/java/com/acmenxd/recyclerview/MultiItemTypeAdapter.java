package com.acmenxd.recyclerview;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ItemDelegateManager;
import com.acmenxd.recyclerview.delegate.ViewHolder;

import java.util.List;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 多类型item Adapter,简化了RecyclerView.Adapter的实现
 */
public class MultiItemTypeAdapter<T> extends RecyclerView.Adapter<ViewHolder> {
    protected Context mContext;
    protected RecyclerView mRecyclerView;
    protected List<T> mDatas;
    protected ItemDelegateManager mItemDelegateManager;

    public MultiItemTypeAdapter(Context context, RecyclerView recyclerView, List<T> datas) {
        mContext = context;
        mRecyclerView = recyclerView;
        mDatas = datas;
        mItemDelegateManager = new ItemDelegateManager();
    }

    public List<T> getDatas() {
        return mDatas;
    }

    public MultiItemTypeAdapter addItemViewDelegate(ItemDelegate<T> pItemDelegate) {
        mItemDelegateManager.addDelegate(pItemDelegate);
        return this;
    }

    public MultiItemTypeAdapter addItemViewDelegate(int viewType, ItemDelegate<T> pItemDelegate) {
        mItemDelegateManager.addDelegate(viewType, pItemDelegate);
        return this;
    }

    @Override
    public int getItemCount() {
        int itemCount = mDatas.size();
        return itemCount;
    }

    @Override
    public int getItemViewType(int dataPosition) {
        if (mItemDelegateManager.getItemViewDelegateCount() <= 0) {
            return super.getItemViewType(dataPosition);
        }
        return mItemDelegateManager.getItemViewType(mDatas.get(dataPosition), dataPosition);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDelegate itemDelegate = mItemDelegateManager.getItemViewDelegate(viewType);
        int layoutId = itemDelegate.getItemViewLayoutId();
        ViewHolder viewHolder = ViewHolder.createViewHolder(mContext, parent, layoutId);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int dataPosition) {
        mItemDelegateManager.convert(viewHolder, mDatas.get(dataPosition), dataPosition);
    }

}
