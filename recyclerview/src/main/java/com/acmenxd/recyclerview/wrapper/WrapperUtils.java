package com.acmenxd.recyclerview.wrapper;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> 包装器工具类
 */
public final class WrapperUtils {
    public static final int ITEM_TYPE_EMPTY = Integer.MAX_VALUE - 1;
    public static final int ITEM_TYPE_LOAD_MORE = Integer.MAX_VALUE - 2;
    public static final int ITEM_TYPE_HEADER = Integer.MAX_VALUE - 10000;
    public static final int ITEM_TYPE_FOOTER = Integer.MAX_VALUE - 20000;

    /**
     * 获取第一个真实数据itemview的位置
     * * 除去各Wrapper中顶部item的位置
     */
    public static int getFirstDataItemViewPosition(@NonNull RecyclerView pRecyclerView, @NonNull RecyclerView.Adapter pAdapter, @IntRange(from = 0) int viewPosition) {
        if (pAdapter instanceof HeaderAndFooterWrapper
                || pAdapter instanceof EmptyWrapper
                || pAdapter instanceof LoadMoreWrapper) {
            return viewPosition;
        } else {
            return viewPosition - getEmptyUpItemCount(pRecyclerView);
        }
    }

    /**
     * 根据item position判断是否是Wrapper装饰器
     */
    public static boolean isItemWrapper(@NonNull RecyclerView pRecyclerView, @IntRange(from = 0) int viewPosition) {
        return isItemHeader(pRecyclerView, viewPosition)
                || isItemFooter(pRecyclerView, viewPosition)
                || isItemEmpty(pRecyclerView, viewPosition)
                || isItemLoadMore(pRecyclerView, viewPosition);
    }

    /**
     * 根据item position判断是否是Header
     */
    public static boolean isItemHeader(@NonNull RecyclerView pRecyclerView, @IntRange(from = 0) int viewPosition) {
        int itemViewType = pRecyclerView.getAdapter().getItemViewType(viewPosition);
        return ITEM_TYPE_HEADER <= itemViewType && itemViewType < ITEM_TYPE_LOAD_MORE;
    }

    /**
     * 根据item position判断是否是Footer
     */
    public static boolean isItemFooter(@NonNull RecyclerView pRecyclerView, @IntRange(from = 0) int viewPosition) {
        int itemViewType = pRecyclerView.getAdapter().getItemViewType(viewPosition);
        return ITEM_TYPE_FOOTER <= itemViewType && itemViewType < ITEM_TYPE_HEADER;
    }

    /**
     * 根据item position判断是否是Empty
     */
    public static boolean isItemEmpty(@NonNull RecyclerView pRecyclerView, @IntRange(from = 0) int viewPosition) {
        int itemViewType = pRecyclerView.getAdapter().getItemViewType(viewPosition);
        return itemViewType == ITEM_TYPE_EMPTY;
    }

    /**
     * 根据item position判断是否是LoadMore
     */
    public static boolean isItemLoadMore(@NonNull RecyclerView pRecyclerView, @IntRange(from = 0) int viewPosition) {
        int itemViewType = pRecyclerView.getAdapter().getItemViewType(viewPosition);
        return itemViewType == ITEM_TYPE_LOAD_MORE;
    }

    /**
     * 获取所有装饰器上 -> Footer之上的item个数
     */
    public static int getFooterUpItemCount(@NonNull RecyclerView pRecyclerView) {
        return getDataItemCount(pRecyclerView) + getFooterUpItemCount(pRecyclerView.getAdapter());
    }

    private static int getFooterUpItemCount(@NonNull RecyclerView.Adapter pAdapter) {
        int footerUpItemCount_Wrapper = 0;
        if (pAdapter instanceof HeaderAndFooterWrapper) {
            HeaderAndFooterWrapper adapter = (HeaderAndFooterWrapper) pAdapter;
            footerUpItemCount_Wrapper += adapter.getFooterUpItemCount_Wrapper();
            footerUpItemCount_Wrapper += getFooterUpItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof EmptyWrapper) {
            EmptyWrapper adapter = (EmptyWrapper) pAdapter;
            footerUpItemCount_Wrapper += adapter.getFooterUpItemCount_Wrapper();
            footerUpItemCount_Wrapper += getFooterUpItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof LoadMoreWrapper) {
            LoadMoreWrapper adapter = (LoadMoreWrapper) pAdapter;
            footerUpItemCount_Wrapper += adapter.getFooterUpItemCount_Wrapper();
            footerUpItemCount_Wrapper += getFooterUpItemCount(adapter.getNextAdapter());
        }
        return footerUpItemCount_Wrapper;
    }

    /**
     * 获取所有装饰器上的Empty之上的item个数
     */
    public static int getEmptyUpItemCount(@NonNull RecyclerView pRecyclerView) {
        return getEmptyUpItemCount(pRecyclerView.getAdapter());
    }

    private static int getEmptyUpItemCount(@NonNull RecyclerView.Adapter pAdapter) {
        int emptyUpItemCount_Wrapper = 0;
        if (pAdapter instanceof HeaderAndFooterWrapper) {
            HeaderAndFooterWrapper adapter = (HeaderAndFooterWrapper) pAdapter;
            emptyUpItemCount_Wrapper += adapter.getEmptyUpItemCount_Wrapper();
            emptyUpItemCount_Wrapper += getEmptyUpItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof EmptyWrapper) {
            EmptyWrapper adapter = (EmptyWrapper) pAdapter;
            emptyUpItemCount_Wrapper += adapter.getEmptyUpItemCount_Wrapper();
            emptyUpItemCount_Wrapper += getEmptyUpItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof LoadMoreWrapper) {
            LoadMoreWrapper adapter = (LoadMoreWrapper) pAdapter;
            emptyUpItemCount_Wrapper += adapter.getEmptyUpItemCount_Wrapper();
            emptyUpItemCount_Wrapper += getEmptyUpItemCount(adapter.getNextAdapter());
        }
        return emptyUpItemCount_Wrapper;
    }

    /**
     * 获取所有装饰器上的包装器的item个数
     */
    public static int getWarpperItemCount(@NonNull RecyclerView pRecyclerView) {
        return getWarpperItemCount(pRecyclerView.getAdapter());
    }

    private static int getWarpperItemCount(@NonNull RecyclerView.Adapter pAdapter) {
        int warpperItemCount_Wrapper = 0;
        if (pAdapter instanceof HeaderAndFooterWrapper) {
            HeaderAndFooterWrapper adapter = (HeaderAndFooterWrapper) pAdapter;
            warpperItemCount_Wrapper += adapter.getWarpperItemCount_Wrapper();
            warpperItemCount_Wrapper += getWarpperItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof EmptyWrapper) {
            EmptyWrapper adapter = (EmptyWrapper) pAdapter;
            warpperItemCount_Wrapper += adapter.getWarpperItemCount_Wrapper();
            warpperItemCount_Wrapper += getWarpperItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof LoadMoreWrapper) {
            LoadMoreWrapper adapter = (LoadMoreWrapper) pAdapter;
            warpperItemCount_Wrapper += adapter.getWarpperItemCount_Wrapper();
            warpperItemCount_Wrapper += getWarpperItemCount(adapter.getNextAdapter());
        }
        return warpperItemCount_Wrapper;
    }

    /**
     * 获取真实数据的item个数
     * 除Header/Footer/LoadMore/Empty以外的item个数
     */
    public static int getDataItemCount(@NonNull RecyclerView pRecyclerView) {
        return getDataItemCount(pRecyclerView.getAdapter());
    }

    private static int getDataItemCount(@NonNull RecyclerView.Adapter pAdapter) {
        int dataItemCount = 0;
        if (pAdapter instanceof HeaderAndFooterWrapper) {
            HeaderAndFooterWrapper adapter = (HeaderAndFooterWrapper) pAdapter;
            dataItemCount = getDataItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof EmptyWrapper) {
            EmptyWrapper adapter = (EmptyWrapper) pAdapter;
            dataItemCount = getDataItemCount(adapter.getNextAdapter());
        } else if (pAdapter instanceof LoadMoreWrapper) {
            LoadMoreWrapper adapter = (LoadMoreWrapper) pAdapter;
            dataItemCount = getDataItemCount(adapter.getNextAdapter());
        } else {
            dataItemCount = pAdapter.getItemCount();
        }
        return dataItemCount;
    }
}
