package com.acmenxd.recyclerview.swipemenu;

import android.content.Context;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;
import android.widget.OverScroller;

import com.acmenxd.recyclerview.R;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/2/17 17:19
 * @detail 滑动菜单->单个item总项
 */
public final class SwipeMenuLayout extends FrameLayout {
    private SwipeMenuViewLeft leftMenuView; // 左侧菜单栏
    private SwipeMenuViewRight rightMenuView; // 右侧菜单栏
    private SwipeMenuView currMenuView; // 当前选中的菜单栏
    private FrameLayout contentView; // 内容栏

    private int mScaledTouchSlop; // 手指大于此距离才移动控件
    private int mScaledMinimumFlingVelocity; // 最小滑动速率
    private int mScaledMaximumFlingVelocity; // 最大滑动速率
    private VelocityTracker mVelocityTracker; // 滑动速率辅助类
    private OverScroller mScroller;

    private boolean mDragging; // 一次事件轮回的判断
    private boolean shouldResetSwipe; // 重置当前滑动菜单

    private int mDownX; // 首次触发的X坐标
    private int mDownY; // 首次触发的Y坐标
    private int mLastX; // 最后触发的X坐标
    private int mLastY; // 最后触发的Y坐标
    private int mScrollerDuration = 200; // 滑动是的时间(毫秒)
    private boolean loseOnceTouch = false; // 整个列表中,除自身外是否有打开的Menu

    private RecyclerView mRecyclerView;

    public void setRecyclerView(@NonNull RecyclerView pRecyclerView) {
        mRecyclerView = pRecyclerView;
    }

    public boolean isLoseOnceTouch() {
        return loseOnceTouch;
    }

    public SwipeMenuLayout(Context context) {
        this(context, null);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mScroller = new OverScroller(getContext());
        ViewConfiguration mViewConfig = ViewConfiguration.get(getContext());
        mScaledTouchSlop = mViewConfig.getScaledTouchSlop();
        mScaledMinimumFlingVelocity = mViewConfig.getScaledMinimumFlingVelocity();
        mScaledMaximumFlingVelocity = mViewConfig.getScaledMaximumFlingVelocity();
    }

    public FrameLayout getContentView() {
        return contentView;
    }

    /**
     * 重置左侧&右侧菜单项
     */
    public void resetMenu() {
        // 回复原位
        scrollTo(0, 0);
        // 重置菜单
        leftMenuView.resetMenu();
        rightMenuView.resetMenu();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (leftMenuView == null) {
            leftMenuView = (SwipeMenuViewLeft) findViewById(R.id.swipe_menu_left);
            leftMenuView.setSwipeMenuLayout(this);
        }
        if (rightMenuView == null) {
            rightMenuView = (SwipeMenuViewRight) findViewById(R.id.swipe_menu_right);
            rightMenuView.setSwipeMenuLayout(this);
        }
        if (contentView == null) {
            contentView = (FrameLayout) findViewById(R.id.swipe_menu_content);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (contentView != null) {
            int width = contentView.getMeasuredWidthAndState();
            int height = contentView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
            int l = getPaddingLeft() + lp.leftMargin;
            int t = getPaddingTop() + lp.topMargin;
            contentView.layout(l, t, l + width, t + height);
        }
        if (leftMenuView != null) {
            int width = leftMenuView.getMeasuredWidthAndState();
            int height = leftMenuView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) leftMenuView.getLayoutParams();
            int t = getPaddingTop() + lp.topMargin;
            leftMenuView.layout(-width, t, 0, t + height);
        }
        if (rightMenuView != null) {
            int width = rightMenuView.getMeasuredWidthAndState();
            int height = rightMenuView.getMeasuredHeightAndState();
            LayoutParams lp = (LayoutParams) rightMenuView.getLayoutParams();
            int t = getPaddingTop() + lp.topMargin;
            int parentViewWidth = getMeasuredWidthAndState();
            rightMenuView.layout(parentViewWidth, t, parentViewWidth + width, t + height);
        }
    }

    /**
     * RecyclerView 中是否有menu是打开的
     *
     * @param pCompareView 排除当前对比view, 无需对比传null即可
     * @return 非当前对比view, 并且menu为打开的view
     */
    public SwipeMenuLayout isMenuOpen_checkAll(@NonNull RecyclerView pRecyclerView, View pCompareView) {
        int count = pRecyclerView.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = pRecyclerView.getChildAt(i);
            if (view instanceof SwipeMenuLayout && ((SwipeMenuLayout) view).isMenuOpen() && view != pCompareView) {
                return (SwipeMenuLayout) view;
            }
        }
        return null;
    }

    /**
     * 检查菜单是否打开
     */
    public boolean isMenuOpen() {
        return isLeftMenuOpen() || isRightMenuOpen();
    }

    private boolean isLeftMenuOpen() {
        return leftMenuView.isMenuOpen(getScrollX());
    }

    private boolean isRightMenuOpen() {
        return rightMenuView.isMenuOpen(getScrollX());
    }

    private boolean isMenuOpenNotEqual() {
        return isLeftMenuOpenNotEqual() || isRightMenuOpenNotEqual();
    }

    private boolean isLeftMenuOpenNotEqual() {
        return leftMenuView != null && leftMenuView.isMenuOpenNotEqual(getScrollX());
    }

    private boolean isRightMenuOpenNotEqual() {
        return rightMenuView != null && rightMenuView.isMenuOpenNotEqual(getScrollX());
    }

    /**
     * 平滑打开菜单
     */
    protected void smoothOpenMenu() {
        smoothOpenMenu(mScrollerDuration);
    }

    private void smoothOpenMenu(@IntRange(from = 0) int duration) {
        if (currMenuView != null) {
            currMenuView.autoOpenMenu(mScroller, getScrollX(), duration);
            invalidate();
        }
    }

    /**
     * 平滑关闭菜单
     */
    public void smoothCloseMenu() {
        smoothCloseMenu(mScrollerDuration);
    }

    protected void smoothCloseMenu(@IntRange(from = 0) int duration) {
        if (currMenuView != null) {
            currMenuView.autoCloseMenu(mScroller, getScrollX(), duration);
            invalidate();
        }
    }

    /**
     * 根据偏移量判断是打开还是关闭菜单
     */
    private void judgeOpenClose(int dx, int dy) {
        if (currMenuView != null) {
            if (Math.abs(getScrollX()) >= (currMenuView.getWidth() * 0.5f)) {
                if (Math.abs(dx) > mScaledTouchSlop || Math.abs(dy) > mScaledTouchSlop) {
                    if (isMenuOpenNotEqual()) {
                        smoothCloseMenu();
                    } else {
                        smoothOpenMenu();
                    }
                } else {
                    if (isMenuOpen()) {
                        smoothCloseMenu();
                    } else {
                        smoothOpenMenu();
                    }
                }
            } else {
                smoothCloseMenu();
            }
        }
    }

    private int getSwipeDuration(MotionEvent ev, int velocity) {
        int sx = getScrollX();
        int dx = (int) (ev.getX() - sx);
        final int width = currMenuView.getWidth();
        final int halfWidth = width / 2;
        final float distanceRatio = Math.min(1f, 1.0f * Math.abs(dx) / width);
        final float distance = halfWidth + halfWidth * distanceInfluenceForSnapDuration(distanceRatio);
        int duration;
        if (velocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / velocity));
        } else {
            final float pageDelta = (float) Math.abs(dx) / width;
            duration = (int) ((pageDelta + 1) * 100);
        }
        duration = Math.min(duration, mScrollerDuration);
        return duration;
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    public boolean onTouchEventCustom(MotionEvent ev) {
        if (!loseOnceTouch) {
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            }
            mVelocityTracker.addMovement(ev);
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mLastX = (int) ev.getX();
                    mLastY = (int) ev.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    mDragging = false;
                    int diX = (int) (mDownX - ev.getX());
                    int diY = (int) (mDownY - ev.getY());
                    mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
                    int velocityX = (int) mVelocityTracker.getXVelocity();
                    int velocity = Math.abs(velocityX);
                    if (velocity > mScaledMinimumFlingVelocity) {
                        if (currMenuView != null) {
                            int duration = getSwipeDuration(ev, velocity);
                            if (currMenuView instanceof SwipeMenuViewRight) {
                                if (velocityX < 0) {
                                    smoothOpenMenu(duration);
                                } else {
                                    smoothCloseMenu(duration);
                                }
                            } else {
                                if (velocityX > 0) {
                                    smoothOpenMenu(duration);
                                } else {
                                    smoothCloseMenu(duration);
                                }
                            }
                            ViewCompat.postInvalidateOnAnimation(this);
                        }
                    } else {
                        judgeOpenClose(diX, diY);
                    }
                    mVelocityTracker.clear();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                    if (Math.abs(diX) > mScaledTouchSlop || Math.abs(diY) > mScaledTouchSlop || isMenuOpen()) {
                        ev.setAction(MotionEvent.ACTION_CANCEL);
                        super.onTouchEvent(ev);
                        return true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    int disX = (int) (mLastX - ev.getX());
                    int disY = (int) (mLastY - ev.getY());
                    if (!mDragging && Math.abs(disX) > mScaledTouchSlop && Math.abs(disX) > Math.abs(disY)) {
                        mDragging = true;
                    }
                    if (mDragging) {
                        if (currMenuView == null || shouldResetSwipe) {
                            if (disX < 0) {
                                if (leftMenuView != null)
                                    currMenuView = leftMenuView;
                                else
                                    currMenuView = rightMenuView;
                            } else {
                                if (rightMenuView != null)
                                    currMenuView = rightMenuView;
                                else
                                    currMenuView = leftMenuView;
                            }
                        }
                        scrollBy(disX, 0);
                        mLastX = (int) ev.getX();
                        mLastY = (int) ev.getY();
                        shouldResetSwipe = false;
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    mDragging = false;
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    } else {
                        int dX = (int) (mDownX - ev.getX());
                        int dY = (int) (mDownY - ev.getY());
                        judgeOpenClose(dX, dY);
                    }
                    break;
            }
        }
        return super.onTouchEvent(ev);
    }

    public boolean onInterceptTouchEventCustom(MotionEvent ev) {
        boolean isIntercepted = super.onInterceptTouchEvent(ev);
        int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                SwipeMenuLayout view = isMenuOpen_checkAll(mRecyclerView, this);
                loseOnceTouch = view != null ? true : false;
                if (!loseOnceTouch) {
                    mDownX = mLastX = (int) ev.getX();
                    mDownY = mLastY = (int) ev.getY();
                    isIntercepted = false;
                } else {
                    view.smoothCloseMenu();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!loseOnceTouch) {
                    isIntercepted = false;
                    if (isMenuOpen() && currMenuView.isClickOnMenuView(this.getWidth(), ev.getX())) {
                        smoothCloseMenu();
                        isIntercepted = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!loseOnceTouch) {
                    int disX = (int) (ev.getX() - mDownX);
                    int disY = (int) (ev.getY() - mDownY);
                    isIntercepted = Math.abs(disX) > mScaledTouchSlop && Math.abs(disX) > Math.abs(disY);
                    if (isIntercepted && getParent() != null) {
                        getParent().requestDisallowInterceptTouchEvent(isIntercepted);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (!loseOnceTouch) {
                    isIntercepted = false;
                    if (!mScroller.isFinished()) {
                        mScroller.abortAnimation();
                    }
                }
                break;
        }
        return isIntercepted;
    }

    @Override
    public void scrollTo(int x, int y) {
        if (currMenuView == null) {
            super.scrollTo(x, y);
        } else {
            SwipeMenuView.Checker checker = currMenuView.checkXY(x, y);
            shouldResetSwipe = checker.shouldResetSwipe;
            if (checker.x != getScrollX()) {
                super.scrollTo(checker.x, checker.y);
            }
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset() && currMenuView != null) {
            if (currMenuView instanceof SwipeMenuViewRight) {
                scrollTo(Math.abs(mScroller.getCurrX()), 0);
                invalidate();
            } else {
                scrollTo(-Math.abs(mScroller.getCurrX()), 0);
                invalidate();
            }
        }
    }

}
