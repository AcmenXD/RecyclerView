package com.acmenxd.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.acmenxd.recyclerview.utils.RecyclerViewUtils;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/3/17 14:02
 * @detail 默认的底部布局样式
 */
public class LoadMoreView extends LinearLayout {
    private LinearLayout loadLayout; //正在加载布局
    private ProgressBar progressBar;//正在加载进度
    private TextView loadTV; //正在加载文本

    private LinearLayout finishLayout;//加载完成布局
    private TextView finishTV; //加载完成文本

    public LoadMoreView(Context context) {
        this(context, null);
    }

    public LoadMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        int height = (int) RecyclerViewUtils.dp2px(context, 40);
        int padding = (int) RecyclerViewUtils.dp2px(context, 3);
        // 创建loadLayout
        loadLayout = new LinearLayout(context);
        loadLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        loadLayout.setOrientation(HORIZONTAL);
        loadLayout.setGravity(Gravity.CENTER);
        // 进度progressBar
        progressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleSmall);
        progressBar.setPadding(0, padding, padding, 0);
        loadLayout.addView(progressBar);
        // 文本
        loadTV = new TextView(context);
        loadTV.setTextSize(14);
        loadTV.setTextColor(Color.GRAY);
        loadTV.setText("正在加载...");
        loadTV.setPadding(padding, 0, 0, 0);
        loadLayout.addView(loadTV);
        this.addView(loadLayout);

        // 完成文本
        finishLayout = new LinearLayout(context);
        finishLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
        finishLayout.setOrientation(HORIZONTAL);
        finishLayout.setGravity(Gravity.CENTER);
        finishTV = new TextView(context);
        finishTV.setTextSize(14);
        finishTV.setTextColor(Color.GRAY);
        finishTV.setText("已加载全部");
        finishLayout.addView(finishTV);
        this.addView(finishLayout);

        showLoading();
    }

    /**
     * 显示正在加载布局
     */
    public void showLoading() {
        loadLayout.setVisibility(VISIBLE);
        finishLayout.setVisibility(GONE);
    }

    /**
     * 显示加载完成布局
     */
    public void showFinish() {
        loadLayout.setVisibility(GONE);
        finishLayout.setVisibility(VISIBLE);
    }

    /**
     * 隐藏LoadMoreView
     */
    public void hide() {
        loadLayout.setVisibility(GONE);
        finishLayout.setVisibility(GONE);
    }
}
