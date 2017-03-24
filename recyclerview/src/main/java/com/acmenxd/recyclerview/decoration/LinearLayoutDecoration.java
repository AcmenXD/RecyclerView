package com.acmenxd.recyclerview.decoration;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.acmenxd.recyclerview.utils.RecyclerViewUtils;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/10 15:45
 * @detail RecyclerView -> LinearLayoutManager下的item分隔线
 */
public class LinearLayoutDecoration extends RecyclerView.ItemDecoration {

    private final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable mDivider;

    private int linePaddingDip = 0;

    public LinearLayoutDecoration(Context context) {
        final TypedArray a = context.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
    }

    /**
     * 设置line线的填充边距
     */
    public void setLinePaddingDip(int pLinePaddingDip) {
        if (pLinePaddingDip >= 0) {
            linePaddingDip = pLinePaddingDip;
        }
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent) {
        int orientation = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
        if (orientation == LinearLayoutManager.VERTICAL) {
            drawVertical(c, parent);
        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            drawHorizontal(c, parent);
        } else {
            throw new IllegalArgumentException("must be LinearLayoutManager");
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        int linePadding = getLinePadding(parent, linePaddingDip, parent.getWidth());
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int top = child.getBottom() + params.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left + linePadding, top, right - linePadding, bottom);
            mDivider.draw(c);
        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent) {
        int linePadding = getLinePadding(parent, linePaddingDip, parent.getHeight());
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int childCount = parent.getChildCount() - 1;
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
                    .getLayoutParams();
            final int left = child.getRight() + params.rightMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top + linePadding, right, bottom - linePadding);
            mDivider.draw(c);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent) {
        int orientation = ((LinearLayoutManager) parent.getLayoutManager()).getOrientation();
        if (orientation == LinearLayoutManager.VERTICAL) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else if (orientation == LinearLayoutManager.HORIZONTAL) {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
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
