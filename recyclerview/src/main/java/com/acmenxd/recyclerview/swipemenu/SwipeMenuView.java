package com.acmenxd.recyclerview.swipemenu;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.OverScroller;

import com.acmenxd.recyclerview.listener.OnSwipeMenuListener;
import com.acmenxd.recyclerview.wrapper.WrapperUtils;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/20 9:27
 * @detail 滑动菜单->菜单项基类
 */
public abstract class SwipeMenuView extends LinearLayout {
    /**
     * 左侧菜单
     */
    public static final int LEFT_DIRECTION = 1;
    /**
     * 右侧菜单
     */
    public static final int RIGHT_DIRECTION = -1;

    protected Checker mChecker = new Checker();
    protected SwipeMenuLayout mSwipeMenuLayout;

    public SwipeMenuView(Context context) {
        this(context, null);
    }

    public SwipeMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SwipeMenuLayout getSwipeMenuLayout() {
        return mSwipeMenuLayout;
    }

    public void setSwipeMenuLayout(@NonNull SwipeMenuLayout pSwipeMenuLayout) {
        mSwipeMenuLayout = pSwipeMenuLayout;
    }

    /**
     * 重置
     */
    public void resetMenu() {
        // 移除所有子控件
        this.removeAllViews();
        // 重置宽高
        ViewGroup.LayoutParams params = this.getLayoutParams();
        params.width = 0;
        params.height = 0;
        this.setLayoutParams(params);
    }

    /**
     * 添加menuview
     */
    public void addMenuView(@NonNull View menuView, @IdRes int[] ids, int direction, @NonNull RecyclerView pRecyclerView, @NonNull final OnSwipeMenuListener pSwipeMenuListener) {
        if (menuView != null) {
            this.addView(menuView);
            // 设置布局
            ViewGroup.LayoutParams menuViewParams = menuView.getLayoutParams();
            ViewGroup.LayoutParams params = this.getLayoutParams();
            params.width = menuViewParams.width;
            params.height = menuViewParams.height;
            this.setLayoutParams(params);
            if (ids != null) {
                int len = ids.length;
                if (len > 1) {
                    for (int i = 1; i < len; i++) {
                        final int fdirrection = direction;
                        final RecyclerView recyclerView = pRecyclerView;
                        final int resId = ids[i];
                        View menuItem = menuView.findViewById(resId);
                        menuItem.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View pView) {
                                SwipeMenuLayout view = SwipeMenuView.this.getSwipeMenuLayout();
                                if (view != null && view.isEnabled()) {
                                    int viewPosition = recyclerView.getChildAdapterPosition(view);
                                    int dataPosition = viewPosition - WrapperUtils.getEmptyUpItemCount(recyclerView);
                                    boolean isClose = pSwipeMenuListener.onMenuItemClick(dataPosition, resId, fdirrection);
                                    if (isClose && view.isMenuOpen()) {
                                        view.smoothCloseMenu();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    public abstract boolean isMenuOpen(int scrollX);

    public abstract boolean isMenuOpenNotEqual(final int scrollX);

    public abstract boolean isClickOnMenuView(int pMenuLayoutWidth, float pEvX);

    public abstract void autoOpenMenu(OverScroller scroller, int scrollX, int duration);

    public abstract void autoCloseMenu(OverScroller scroller, int scrollX, int duration);

    public abstract Checker checkXY(int x, int y);

    public static final class Checker {
        public int x;
        public int y;
        public boolean shouldResetSwipe;
    }
}
