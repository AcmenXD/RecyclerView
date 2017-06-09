package com.acmenxd.recyclerview.wrapper;

import android.support.annotation.IntRange;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.acmenxd.recyclerview.LoadMoreView;
import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.delegate.ViewHolder;
import com.acmenxd.recyclerview.listener.OnLoadMoreListener;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView 添加 加载更多 视图
 */
public class LoadMoreWrapper<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int ITEM_TYPE_LOAD_MORE = WrapperUtils.ITEM_TYPE_LOAD_MORE;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mInnerAdapter;
    private View mLoadMoreView;
    private int mLoadMoreLayoutId;
    private int mInvertedOrderNumber = 1;
    private OnLoadMoreListener mOnLoadMoreListener;

    public LoadMoreWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter, @Nullable OnLoadMoreListener loadMoreListener) {
        this(recyclerView, adapter, null, loadMoreListener);
    }

    public LoadMoreWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter, @NonNull View loadMoreView, @Nullable OnLoadMoreListener loadMoreListener) {
        mRecyclerView = recyclerView;
        mInnerAdapter = adapter;
        if (loadMoreView == null) {
            LoadMoreView view = new LoadMoreView(recyclerView.getContext());
            int orientation = AdapterUtils.getOrientation(mRecyclerView);
            view.setOrientation(orientation == OrientationHelper.HORIZONTAL ? OrientationHelper.HORIZONTAL : OrientationHelper.VERTICAL);
            view.showLoading();
            loadMoreView = view;
        }
        mLoadMoreView = loadMoreView;
        if (loadMoreListener != null) {
            mOnLoadMoreListener = loadMoreListener;
        }
    }

    public LoadMoreWrapper(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.Adapter adapter, @LayoutRes int loadMoreLayoutId, @Nullable OnLoadMoreListener loadMoreListener) {
        mRecyclerView = recyclerView;
        mInnerAdapter = adapter;
        mLoadMoreLayoutId = loadMoreLayoutId;
        if (loadMoreListener != null) {
            mOnLoadMoreListener = loadMoreListener;
        }
    }

    protected RecyclerView.Adapter getNextAdapter() {
        return mInnerAdapter;
    }

    /**
     * 提前多少个加载下一次数据,默认剩余1个item项时加载
     * * 如需点击加载更多, 设置为0即可
     */
    public void setRefreshBefore(@IntRange(from = 0) int pInvertedOrderNumber) {
        mInvertedOrderNumber = pInvertedOrderNumber;
        if (mLoadMoreView instanceof LoadMoreView) {
            if (mInvertedOrderNumber <= 0) {
                ((LoadMoreView) mLoadMoreView).showClick();
            } else {
                ((LoadMoreView) mLoadMoreView).showLoading();
            }
        }
    }

    public View getLoadMoreView() {
        return mLoadMoreView;
    }

    private boolean hasLoadMore() {
        return mLoadMoreView != null || mLoadMoreLayoutId != 0;
    }

    private boolean isShowLoadMore(@IntRange(from = 0) int viewPosition) {
        return hasLoadMore() && (viewPosition == WrapperUtils.getWarpperItemCount(mRecyclerView)
                + WrapperUtils.getDataItemCount(mRecyclerView) - 1);
    }

    /**
     * 获取当前装饰器上 -> Footer之上的item个数
     */
    protected int getFooterUpItemCount_Wrapper() {
        return 0;
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
        return hasLoadMore() ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return (hasLoadMore() ? 1 : 0) + mInnerAdapter.getItemCount();
    }

    @Override
    public int getItemViewType(int viewPosition) {
        if (isShowLoadMore(viewPosition)) {
            return ITEM_TYPE_LOAD_MORE;
        }
        return mInnerAdapter.getItemViewType(
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_LOAD_MORE) {
            ViewHolder viewHolder = null;
            if (mLoadMoreView == null && mLoadMoreLayoutId != 0) {
                mLoadMoreView = LayoutInflater.from(parent.getContext()).inflate(mLoadMoreLayoutId, parent);
            }
            if (mLoadMoreView != null) {
                viewHolder = ViewHolder.createViewHolder(parent.getContext(), mLoadMoreView);
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View pView) {
                        if (mRecyclerView.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                            if (mOnLoadMoreListener != null) {
                                mOnLoadMoreListener.onLoadMoreClick(mLoadMoreView);
                            }
                        }
                    }
                });
            }
            return viewHolder;
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int viewPosition) {
        if (isShowLoadMore(viewPosition)) {
            if (mOnLoadMoreListener != null && mInvertedOrderNumber == 1) {
                mOnLoadMoreListener.onLoadMore(mLoadMoreView);
            }
            return;
        }
        mInnerAdapter.onBindViewHolder(viewHolder,
                WrapperUtils.getFirstDataItemViewPosition(mRecyclerView, mInnerAdapter, viewPosition));
        if (mInvertedOrderNumber > 0 && getItemCount() > mInvertedOrderNumber && viewPosition == getItemCount() - mInvertedOrderNumber) {
            if (mOnLoadMoreListener != null) {
                mOnLoadMoreListener.onLoadMore(mLoadMoreView);
            }
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        AdapterUtils.onAttachedToRecyclerView(mInnerAdapter, recyclerView, new AdapterUtils.SpanSizeCallback() {
            @Override
            public int getSpanSize(GridLayoutManager layoutManager, GridLayoutManager.SpanSizeLookup oldLookup, int viewPosition) {
                if (isShowLoadMore(viewPosition)) {
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
    public void onViewAttachedToWindow(RecyclerView.ViewHolder viewHolder) {
        mInnerAdapter.onViewAttachedToWindow(viewHolder);
        if (isShowLoadMore(viewHolder.getLayoutPosition())) {
            AdapterUtils.setFullSpan(viewHolder);
        }
    }

}
