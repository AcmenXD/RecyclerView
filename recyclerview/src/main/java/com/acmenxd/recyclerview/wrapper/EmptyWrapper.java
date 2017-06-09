package com.acmenxd.recyclerview.wrapper;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.listener.OnEmptyListener;


/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView 添加 数据空时显示的视图
 */
public final class EmptyWrapper<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_TYPE_EMPTY = WrapperUtils.ITEM_TYPE_EMPTY;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mInnerAdapter;
    private View mEmptyView;
    private int mEmptyLayoutId;
    private OnEmptyListener mOnEmptyListener;

    public EmptyWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter, @NonNull View emptyView, @Nullable OnEmptyListener onEmptyListener) {
        mRecyclerView = recyclerView;
        mInnerAdapter = adapter;
        mEmptyView = emptyView;
        mOnEmptyListener = onEmptyListener;
    }

    public EmptyWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter, @LayoutRes int emptyLayoutId, @Nullable OnEmptyListener onEmptyListener) {
        mRecyclerView = recyclerView;
        mInnerAdapter = adapter;
        mEmptyLayoutId = emptyLayoutId;
        mOnEmptyListener = onEmptyListener;
    }

    private boolean isEmpty() {
        return (mEmptyView != null || mEmptyLayoutId != 0) && WrapperUtils.getDataItemCount(mRecyclerView) == 0;
    }

    private boolean isEmptyView(@IntRange(from = 0) int viewPosition) {
        if (isEmpty() && WrapperUtils.getEmptyUpItemCount(mRecyclerView) == viewPosition) {
            return true;
        }
        return false;
    }

    protected RecyclerView.Adapter getNextAdapter() {
        return mInnerAdapter;
    }

    /**
     * 获取当前装饰器上 -> Footer之上的item个数
     */
    protected int getFooterUpItemCount_Wrapper() {
        return isEmpty() ? 1 : 0;
    }

    /**
     * 获取Empty之上的item个数
     */
    protected int getEmptyUpItemCount_Wrapper() {
        return 0;
    }

    /**
     * 获取包装器的item个数
     */
    protected int getWarpperItemCount_Wrapper() {
        return isEmpty() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return (isEmpty() ? 1 : 0) + mInnerAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int viewPosition) {
        if (isEmptyView(viewPosition)) {
            return ITEM_TYPE_EMPTY;
        }
        return mInnerAdapter.getItemViewType(
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_EMPTY) {
            ViewHolder viewHolder = null;
            if (mEmptyView == null && mEmptyLayoutId != 0) {
                mEmptyView = LayoutInflater.from(parent.getContext()).inflate(mEmptyLayoutId, parent);
            }
            if (mEmptyView != null) {
                viewHolder = ViewHolder.createViewHolder(parent.getContext(), mEmptyView);
            }
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View pView) {
                    if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                        if (mOnEmptyListener != null) {
                            mOnEmptyListener.onEmptyClick(mEmptyView);
                        }
                    }
                }
            });
            return viewHolder;
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int viewPosition) {
        if (isEmptyView(viewPosition)) {
            return;
        }
        mInnerAdapter.onBindViewHolder(viewHolder,
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        AdapterUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, new AdapterUtils.SpanSizeCallback() {
            @Override
            public int getSpanSize(GridLayoutManager gridLayoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int viewPosition) {
                if (isEmptyView(viewPosition)) {
                    return gridLayoutManager.getSpanCount();
                }
                if (oldLookup != null) {
                    return oldLookup.getSpanSize(viewPosition);
                }
                return 1;
            }
        });
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        mInnerAdapter.onViewAttachedToWindow(viewHolder);
        if (isEmptyView(viewHolder.getLayoutPosition())) {
            AdapterUtils.setFullSpan(viewHolder);
        }
    }

}
