package com.acmenxd.recyclerview.wrapper;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.util.SparseArrayCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.delegate.ViewHolder;


/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView 添加 头尾视图
 */
public class HeaderAndFooterWrapper<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_TYPE_HEADER = WrapperUtils.ITEM_TYPE_HEADER;
    private static final int ITEM_TYPE_FOOTER = WrapperUtils.ITEM_TYPE_FOOTER;

    private SparseArrayCompat<View> mHeaderViews = new SparseArrayCompat<>();
    private SparseArrayCompat<View> mFootViews = new SparseArrayCompat<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mInnerAdapter;

    public HeaderAndFooterWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter) {
        mRecyclerView = recyclerView;
        mInnerAdapter = adapter;
    }


    public void addHeaderView(@NonNull View view) {
        mHeaderViews.put(mHeaderViews.size() + ITEM_TYPE_HEADER, view);
    }

    public void addFooterView(@NonNull View view) {
        mFootViews.put(mFootViews.size() + ITEM_TYPE_FOOTER, view);
    }

    public int getHeadersCount() {
        return mHeaderViews.size();
    }

    public int getFootersCount() {
        return mFootViews.size();
    }

    protected RecyclerView.Adapter getNextAdapter() {
        return mInnerAdapter;
    }

    private boolean isHeaderViewPos(@IntRange(from = 0) int viewPosition) {
        return viewPosition < getHeadersCount();
    }

    private boolean isFooterViewPos(@IntRange(from = 0) int viewPosition) {
        int footerUpItemCount = WrapperUtils.getFooterUpItemCount(mRecyclerView);
        return viewPosition >= footerUpItemCount && viewPosition < footerUpItemCount + getFootersCount();
    }

    /**
     * 获取当前装饰器上 -> Footer之上的item个数
     */
    protected int getFooterUpItemCount_Wrapper() {
        return getHeadersCount();
    }

    /**
     * 获取Empty之上的item个数
     */
    protected int getEmptyUpItemCount_Wrapper() {
        return getHeadersCount();
    }

    /**
     * 获取包装器的item个数
     */
    protected int getWarpperItemCount_Wrapper() {
        return getHeadersCount() + getFootersCount();
    }

    @Override
    public int getItemCount() {
        return getHeadersCount() + getFootersCount() + mInnerAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int viewPosition) {
        if (isHeaderViewPos(viewPosition)) {
            return mHeaderViews.keyAt(viewPosition);
        } else if (isFooterViewPos(viewPosition)) {
            return mFootViews.keyAt(viewPosition - WrapperUtils.getFooterUpItemCount(mRecyclerView));
        }
        return mInnerAdapter.getItemViewType(
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (mHeaderViews.get(viewType) != null) {
            return ViewHolder.createViewHolder(parent.getContext(), mHeaderViews.get(viewType));
        } else if (mFootViews.get(viewType) != null) {
            return ViewHolder.createViewHolder(parent.getContext(), mFootViews.get(viewType));
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int viewPosition) {
        if (isHeaderViewPos(viewPosition)) {
            return;
        }
        if (isFooterViewPos(viewPosition)) {
            return;
        }
        mInnerAdapter.onBindViewHolder(viewHolder,
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        AdapterUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, new AdapterUtils.SpanSizeCallback() {
            @Override
            public int getSpanSize(GridLayoutManager layoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int viewPosition) {
                int viewType = getItemViewType(viewPosition);
                if (mHeaderViews.get(viewType) != null) {
                    return layoutManager.getSpanCount();
                } else if (mFootViews.get(viewType) != null) {
                    return layoutManager.getSpanCount();
                }
                if (oldLookup != null)
                    return oldLookup.getSpanSize(viewPosition);
                return 1;
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        mInnerAdapter.onViewAttachedToWindow(viewHolder);
        int viewPosition = viewHolder.getLayoutPosition();
        if (isHeaderViewPos(viewPosition) || isFooterViewPos(viewPosition)) {
            AdapterUtils.setFullSpan(viewHolder);
        }
    }
}
