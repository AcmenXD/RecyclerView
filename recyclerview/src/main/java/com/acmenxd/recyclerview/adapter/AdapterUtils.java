package com.acmenxd.recyclerview.adapter;

import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.ViewGroup;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/16 16:00
 * @detail RecyclerView -> Adapter工具类
 */
public final class AdapterUtils {
    public interface SpanSizeCallback {
        int getSpanSize(@NonNull GridLayoutManager layoutManager, @NonNull GridLayoutManager.SpanSizeLookup oldLookup, @IntRange(from = 0) int viewPosition);
    }

    public static void onAttachedToRecyclerView(@NonNull RecyclerView.Adapter innerAdapter, @NonNull RecyclerView recyclerView, @NonNull final SpanSizeCallback callback) {
        if (innerAdapter != null) {
            innerAdapter.onAttachedToRecyclerView(recyclerView);
        }
        RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final GridLayoutManager.SpanSizeLookup spanSizeLookup = gridLayoutManager.getSpanSizeLookup();
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int viewPosition) {
                    return callback.getSpanSize(gridLayoutManager, spanSizeLookup, viewPosition);
                }
            });
            gridLayoutManager.setSpanCount(gridLayoutManager.getSpanCount());
        }
    }

    public static void setFullSpan(@NonNull RecyclerView.ViewHolder viewHolder) {
        ViewGroup.LayoutParams lp = viewHolder.itemView.getLayoutParams();
        if (lp != null && lp instanceof StaggeredGridLayoutManager.LayoutParams) {
            StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
            p.setFullSpan(true);
        }
    }

    /**
     * * 如RecyclerView请求网络或读取数据,可以直接调用adapter的notifyDataSetChanged
     * * 如RecyclerView直接刷新,必须挑用此函数刷新
     * 更新RecyclerView下的Adapters
     * 解决在Adapter.onBindViewHolder()中调用notifyDataSetChanged()崩溃的问题
     *
     * @param pAdapters : 传入真正设置到RecyclerView中的Adapter即可
     */
    public static void notifyDataSetChanged(@NonNull final RecyclerView pRecyclerView, @NonNull final RecyclerView.Adapter... pAdapters) {
        if (pAdapters == null || pAdapters.length <= 0) {
            return;
        }
        pRecyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {
                //是否正在滑动 && 是否正在计算layout
                // if (pRecyclerView.getScrollState() == pRecyclerView.SCROLL_STATE_IDLE
                //            && !pRecyclerView.isComputingLayout()) {
                for (int i = 0, len = pAdapters.length; i < len; i++) {
                    pAdapters[i].notifyDataSetChanged();
                }
                // }
            }
        }, 50);
    }

    /**
     * 获取RecyclerView是横|纵排列
     */
    public static int getOrientation(@NonNull RecyclerView parent) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof GridLayoutManager) {
            return ((GridLayoutManager) layoutManager).getOrientation();
        } else if (layoutManager instanceof LinearLayoutManager) {
            return ((LinearLayoutManager) layoutManager).getOrientation();
        }
        return 0;
    }

}
