package com.acmenxd.recyclerview.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.delegate.ItemDelegate;
import com.acmenxd.recyclerview.delegate.ItemDelegateManager;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.group.GroupListener;
import com.acmenxd.recyclerview.wrapper.WrapperUtils;

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
    private GroupListener mGroupListener; // 兼容Group分组功能,网格或瀑布流,必须设置,否则无法支持Group功能

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

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        AdapterUtils.onAttachedToRecyclerView(null, recyclerView, new AdapterUtils.SpanSizeCallback() {
            @Override
            public int getSpanSize(GridLayoutManager layoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int viewPosition) {
                if (isGroupItemLayout(viewPosition)) {
                    return layoutManager.getSpanCount();
                }
                if (oldLookup != null) {
                    return oldLookup.getSpanSize(viewPosition);
                }
                return 1;
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if (isGroupItemLayout(holder.getLayoutPosition())) {
            AdapterUtils.setFullSpan(holder);
        }
    }

    /**
     * 兼容Group分组功能,网格或瀑布流,必须设置,否则无法支持Group功能
     */
    public void setGroupListener(GroupListener pGroupListener) {
        mGroupListener = pGroupListener;
    }

    /**
     * 兼容Group分组功能,网格或瀑布流,必须设置,否则无法支持Group功能
     */
    private boolean isGroupItemLayout(int viewPosition) {
        if (mGroupListener != null) {
            int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(mRecyclerView);
            if (dataPosition >= 0 && dataPosition < mDatas.size()) {
                boolean result = mGroupListener.isCreateGroupItemView(dataPosition);
                return result;
            }
        }
        return false;
    }

}
