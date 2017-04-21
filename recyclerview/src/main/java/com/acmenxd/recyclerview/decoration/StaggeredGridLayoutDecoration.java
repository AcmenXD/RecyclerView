package com.acmenxd.recyclerview.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.acmenxd.recyclerview.adapter.AdapterUtils;
import com.acmenxd.recyclerview.utils.RecyclerViewUtils;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/10 15:45
 * @detail RecyclerView -> StaggeredGridLayoutManager下的item分隔线
 */
public class StaggeredGridLayoutDecoration extends RecyclerView.ItemDecoration {
    private final int[] ATTRS = new int[]{android.R.attr.listDivider};
    private Drawable mDivider;

    private int linePaddingDip = 0;

    /**
     * 设置line线的填充边距
     */
    public void setLinePaddingDip(int pLinePaddingDip) {
        if (pLinePaddingDip >= 0) {
            linePaddingDip = pLinePaddingDip;
        }
    }

    public StaggeredGridLayoutDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int orientation = AdapterUtils.getOrientation(parent);
        drawHorizontal(c, parent, orientation);
        drawVertical(c, parent, orientation);
    }

    private int getSpanCount(RecyclerView parent) {
        // 列数
        int spanCount = -1;
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof StaggeredGridLayoutManager) {
            spanCount = ((StaggeredGridLayoutManager) layoutManager).getSpanCount();
        }
        return spanCount;
    }

    private void drawHorizontal(Canvas c, RecyclerView parent, int orientation) {
        int childCount = parent.getChildCount() - 1;
        int linePadding = getLinePadding(parent, linePaddingDip, parent.getWidth());
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getLeft() - params.leftMargin;
            final int right = child.getRight() + params.rightMargin + mDivider.getIntrinsicWidth();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            if (bottom < parent.getHeight() || orientation == StaggeredGridLayoutManager.VERTICAL) {
                mDivider.setBounds(left + linePadding, top, right - linePadding, bottom);
            } else {
                mDivider.setBounds(0, 0, 0, 0);
            }
            mDivider.draw(c);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent, int orientation) {
        final int childCount = parent.getChildCount() - 1;
        int linePadding = getLinePadding(parent, linePaddingDip, parent.getHeight());
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getTop() - params.topMargin;
            final int bottom = child.getBottom() + params.bottomMargin;
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicWidth();
            if (right < parent.getWidth() || orientation == StaggeredGridLayoutManager.HORIZONTAL) {
                mDivider.setBounds(left, top + linePadding, right, bottom - linePadding);
            } else {
                mDivider.setBounds(0, 0, 0, 0);
            }
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        outRect.set(0, 0, mDivider.getIntrinsicWidth(), mDivider.getIntrinsicHeight());
    }

    private int getLinePadding(RecyclerView parent, int padding, int all) {
        if (padding <= 0) {
            return 0;
        }
        float linePadding = RecyclerViewUtils.dp2px(parent.getContext(), padding);
        if (linePadding * 3 < all) {
            return (int) linePadding;
        } else {
            return getLinePadding(parent, padding - 5, all);
        }
    }
}
