package com.acmenxd.recyclerview;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutCompat;
import android.support.v7.widget.OrientationHelper;
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
public final class LoadMoreView extends LinearLayout {
    private Context mContext;
    private LinearLayout loadLayout; //正在加载布局
    private ProgressBar progressBar;//正在加载进度
    private TextView loadTV; //正在加载文本

    private LinearLayout clickLayout;//点击加载布局
    private TextView clickTV; //点击加载文本

    private LinearLayout finishLayout;//加载完成布局
    private TextView finishTV; //加载完成文本

    public LoadMoreView(Context context) {
        this(context, null);
        initView();
    }

    public LoadMoreView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadMoreView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
    }

    @Override
    public void setOrientation(int orientation) {
        super.setOrientation(orientation);
        initView();
    }

    private void initView(){
        this.removeAllViews();
        int orientation = getOrientation();
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        if(orientation == OrientationHelper.HORIZONTAL){
            width = ViewGroup.LayoutParams.WRAP_CONTENT;
        }
        this.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT));
        int height = (int) RecyclerViewUtils.dp2px(mContext, 40);
        int padding = (int) RecyclerViewUtils.dp2px(mContext, 3);
        // 创建loadLayout
        loadLayout = new LinearLayout(mContext);
        loadLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, height));
        loadLayout.setOrientation(HORIZONTAL);
        loadLayout.setGravity(Gravity.CENTER);
        // 进度progressBar
        progressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
        progressBar.setPadding(0, padding, padding, 0);
        loadLayout.addView(progressBar);
        // 文本
        loadTV = new TextView(mContext);
        loadTV.setTextSize(14);
        loadTV.setTextColor(Color.GRAY);
        loadTV.setText("正在加载...");
        loadTV.setPadding(padding, 0, 0, 0);
        loadLayout.addView(loadTV);
        this.addView(loadLayout);

        // 点击加载文本
        clickLayout = new LinearLayout(mContext);
        clickLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, height));
        clickLayout.setOrientation(HORIZONTAL);
        clickLayout.setGravity(Gravity.CENTER);
        clickTV = new TextView(mContext);
        clickTV.setTextSize(14);
        clickTV.setTextColor(Color.GRAY);
        clickTV.setText("点击加载更多");
        clickLayout.addView(clickTV);
        this.addView(clickLayout);

        // 完成文本
        finishLayout = new LinearLayout(mContext);
        finishLayout.setLayoutParams(new LinearLayoutCompat.LayoutParams(width, height));
        finishLayout.setOrientation(HORIZONTAL);
        finishLayout.setGravity(Gravity.CENTER);
        finishTV = new TextView(mContext);
        finishTV.setTextSize(14);
        finishTV.setTextColor(Color.GRAY);
        finishTV.setText("已加载全部");
        finishLayout.addView(finishTV);
        this.addView(finishLayout);

        showClick();
    }

    /**
     * 显示正在加载布局
     */
    public void showLoading() {
        loadLayout.setVisibility(VISIBLE);
        clickLayout.setVisibility(GONE);
        finishLayout.setVisibility(GONE);
    }

    /**
     * 显示点击加载布局
     */
    public void showClick() {
        loadLayout.setVisibility(GONE);
        clickLayout.setVisibility(VISIBLE);
        finishLayout.setVisibility(GONE);
    }

    /**
     * 显示加载完成布局
     */
    public void showFinish() {
        loadLayout.setVisibility(GONE);
        clickLayout.setVisibility(GONE);
        finishLayout.setVisibility(VISIBLE);
    }
}
