package com.acmenxd.recyclerview.listener;

import android.view.View;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/9 16:50
 * @detail LoadMoreWarpper 事件回调
 */
public abstract class OnLoadMoreListener {
    public abstract void onLoadMore(View itemView);

    public void onLoadMoreClick(View itemView) {

    }
}
