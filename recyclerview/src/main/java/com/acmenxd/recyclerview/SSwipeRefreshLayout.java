package com.acmenxd.recyclerview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.acmenxd.recyclerview.group.GroupHeadLayout;

/**
 * @author AcmenXD
 * @version v1.0
 * @github https://github.com/AcmenXD
 * @date 2017/3/17 14:02
 * @detail 支持下拉刷新和上拉加载更多
 * 非侵入式，对原来的RecyclerView，ListView，ScrollView，GridView没有任何影响,用法和SwipeRefreshLayout类似。
 * 可自定义头部View的样式，调用setHeaderView方法即可
 * 可自定义页尾View的样式，调用setFooterView方法即可
 * 默认是跟随手指的滑动而滑动，也可以设置为不跟随：setTargetScrollWithLayout(false)
 * 可以根据下拉过程中distance的值做一系列动画
 */
@SuppressLint("ClickableViewAccessibility")
public class SSwipeRefreshLayout extends ViewGroup {
    private static final String TAG = SSwipeRefreshLayout.class.getSimpleName();
    private static final int HEADER_FOOTER_VIEW_HEIGHT = 55; // 头尾默认高度
    private static final int DEFAULT_CIRCLE_TARGET = HEADER_FOOTER_VIEW_HEIGHT; // 下拉上拉的检测高度
    private static final int DEFAULT_VIEW_HEIGHT = 45; // 默认视图的高度
    private static final float DECELERATE_INTERPOLATION_FACTOR = 2f;
    private static final int INVALID_POINTER = -1;
    private static final float DRAG_RATE = .5f;
    private static final int SCALE_DOWN_DURATION = 150;
    private static final int ANIMATE_TO_TRIGGER_DURATION = 250;// 下拉超出部分的回滚动画时间
    private static final int ANIMATE_TO_START_DURATION = 250;// 下拉完成回滚的动画时间
    private static final int[] LAYOUT_ATTRS = new int[]{android.R.attr.enabled};

    // SSwipeRefreshLayout内的目标View，比如RecyclerView,ListView,ScrollView,GridView
    private View mTarget;
    // 下拉刷新listener
    private OnRefreshListener mListener;
    // 上拉加载更多listener
    private OnLoadMoreListener mOnLoadMoreListener;
    private CircleProgressView defaultProgressView = null;
    private boolean targetScrollWithLayout = false; // 默认为非侵入式
    private float mTotalDragDistance = -1; // 滑动到多少dp触发

    private HeadViewContainer mHeadViewContainer;
    private RelativeLayout mFooterViewContainer;
    private Animation mScaleAnimation;
    private Animation mScaleDownAnimation;
    private Animation mScaleDownToStartAnimation;
    private final DecelerateInterpolator mDecelerateInterpolator;
    private int mScrollTriggerType = 1; //触发条件(上拉下拉)  1.只要滑动到顶部或底部就触发   2.初始位置在顶部或底部时,才触发
    private int mTouchSlop; //是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
    private int mActivePointerId = INVALID_POINTER;
    private boolean isToTop = false;
    private boolean isToBottom = false;
    private float mActionY;
    private boolean usingDefaultHeader = true;
    private float density = 1.0f;
    private int mHeaderViewWidth;
    private int mFooterViewWidth;
    private int mHeaderViewHeight;
    private int mFooterViewHeight;
    private int mDefaultViewHeight;
    private boolean mRefreshing = false;
    private boolean mLoadMore = false;
    private int mHeaderViewIndex = -1;
    private int mFooterViewIndex = -1;
    private int mMediumAnimationDuration;
    private int mCurrentTargetOffsetTop;
    private boolean mOriginalOffsetCalculated = false;
    private float mInitialMotionY;
    private boolean mIsBeingDragged;
    private boolean mReturningToStart;
    protected int mOriginalOffsetTop;
    private float mStartingScale;
    private boolean mNotify;
    private int pushDistance = 0;
    private boolean isProgressEnable = true;
    private float mSpinnerFinalOffset; // 最后停顿时的偏移量px
    protected int mFrom;
    private boolean mUsingCustomStart;
    private boolean mScale;

    /**
     * 下拉时，超过距离之后，弹回来的动画监听器
     */
    private final AnimationListener mRefreshListener = new AnimationListener() {
        @Override
        public void onAnimationStart(Animation animation) {
            isProgressEnable = false;
        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            isProgressEnable = true;
            if (mRefreshing) {
                if (mNotify) {
                    if (usingDefaultHeader) {
                        ViewCompat.setAlpha(defaultProgressView, 1.0f);
                        defaultProgressView.setOnDraw(true);
                        new Thread(defaultProgressView).start();
                    }
                    if (mListener != null) {
                        mListener.onRefresh();
                    }
                }
            } else {
                mHeadViewContainer.setVisibility(View.GONE);
                if (mScale) {
                    setAnimationProgress(0);
                } else {
                    setTargetOffsetTopAndBottom(mOriginalOffsetTop - mCurrentTargetOffsetTop, true);
                }
            }
            mCurrentTargetOffsetTop = mHeadViewContainer.getTop();
            updateListenerCallBack();
        }
    };
    private final Animation mAnimateToCorrectPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            int targetTop = 0;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                endTarget = (int) (mSpinnerFinalOffset - Math.abs(mOriginalOffsetTop));
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            targetTop = (mFrom + (int) ((endTarget - mFrom) * interpolatedTime));
            int offset = targetTop - mHeadViewContainer.getTop();
            setTargetOffsetTopAndBottom(offset, false /* requires update */);
        }

        @Override
        public void setAnimationListener(AnimationListener listener) {
            super.setAnimationListener(listener);
        }
    };
    private final Animation mAnimateToStartPosition = new Animation() {
        @Override
        public void applyTransformation(float interpolatedTime, Transformation t) {
            moveToStart(interpolatedTime);
        }
    };

    /**
     * 下拉刷新回调
     */
    public interface OnRefreshListener {
        void onRefresh();

        void onReach(boolean reach);

        void onPullDistance(int distance);
    }

    /**
     * 上拉加载更多
     */
    public interface OnLoadMoreListener {
        void onLoadMore();

        void onReach(boolean reach);

        void onPushDistance(int distance);
    }

    /**
     * 设置下拉监听
     */
    public void setOnRefreshListener(@NonNull OnRefreshListener listener) {
        mListener = listener;
    }

    /**
     * 设置上拉监听
     */
    public void setOnLoadMoreListener(@NonNull OnLoadMoreListener onLoadMoreListener) {
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    /**
     * 设置刷新状态
     */
    public void setRefreshing(boolean refreshing) {
        if (refreshing && mRefreshing != refreshing) {
            mRefreshing = refreshing;
            int endTarget = 0;
            if (!mUsingCustomStart) {
                endTarget = (int) (mSpinnerFinalOffset + mOriginalOffsetTop);
            } else {
                endTarget = (int) mSpinnerFinalOffset;
            }
            setTargetOffsetTopAndBottom(endTarget - mCurrentTargetOffsetTop, true);
            mNotify = false;
            startScaleUpAnimation(mRefreshListener);
        } else {
            setRefreshing(refreshing, false);
            if (usingDefaultHeader) {
                defaultProgressView.setOnDraw(false);
            }
        }
    }

    /**
     * 设置停止加载
     */
    public void setLoadMore(boolean loadMore) {
        if (!loadMore && mLoadMore) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                mLoadMore = false;
                pushDistance = 0;
                updateFooterViewPosition();
            } else {
                animatorFooterToBottom(mFooterViewHeight, 0);
            }
        }
    }

    /**
     * 添加顶部布局
     */
    public void setHeaderView(@NonNull View child) {
        if (child == null) {
            return;
        }
        if (mHeadViewContainer == null) {
            return;
        }
        usingDefaultHeader = false;
        mHeadViewContainer.removeAllViews();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mHeaderViewWidth, mHeaderViewHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mHeadViewContainer.addView(child, layoutParams);
    }

    /**
     * 添加底部布局
     */
    public void setFooterView(@NonNull View child) {
        if (child == null) {
            return;
        }
        if (mFooterViewContainer == null) {
            return;
        }
        mFooterViewContainer.removeAllViews();
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mFooterViewWidth, mFooterViewHeight);
        mFooterViewContainer.addView(child, layoutParams);
    }

    /**
     * 滚动到顶部
     */
    public void scrollToTop() {
        if (mTarget != null) {
            mTarget.setScrollY(0);
        }
    }

    /**
     * 滚动到底部
     */
    public void scrollToBottom() {
        if (mTarget != null) {
            mTarget.setScrollY(mTarget.getMeasuredHeight());
        }
    }

    /**
     * 滚动到左边
     */
    public void scrollToStart() {
        if (mTarget != null) {
            scrollToTop();
            mTarget.setScrollX(0);
        }
    }

    /**
     * 滚动到右边
     */
    public void scrollToEnd() {
        if (mTarget != null) {
            scrollToBottom();
            mTarget.setScrollX(mTarget.getMeasuredWidth());
        }
    }

    /**
     * 设置头部背景
     */
    public void setHeaderBackgroundResource(@DrawableRes int resId) {
        mHeadViewContainer.setBackgroundResource(resId);
    }

    /**
     * 设置头部背景色
     */
    public void setHeaderBackgroundColor(@ColorInt int color) {
        mHeadViewContainer.setBackgroundColor(color);
    }

    /**
     * 设置子View是否跟谁手指的滑动而滑动
     */
    public void setTargetScrollWithLayout(boolean targetScrollWithLayout) {
        this.targetScrollWithLayout = targetScrollWithLayout;
    }

    /**
     * 设置触发条件(上拉下拉)  1.只要滑动到顶部或底部就触发   2.初始位置在顶部或底部时,才触发
     */
    public void setScrollTriggerType(@IntRange(from = 1, to = 2) int scrollTriggerType) {
        mScrollTriggerType = scrollTriggerType;
    }

    /**
     * 设置滑动到多少dp触发
     */
    public void setDistanceToTriggerSync(int distance) {
        mTotalDragDistance = distance * density;
    }

    /**
     * 设置默认圆形背景色
     */
    public void setCircleBackgroundColor(@ColorInt int color) {
        if (usingDefaultHeader) {
            defaultProgressView.setCircleBackgroundColor(color);
        }
    }

    /**
     * 设置默认圆形进度条颜色
     */
    public void setCircleProgressColor(@ColorInt int color) {
        if (usingDefaultHeader) {
            defaultProgressView.setProgressColor(color);
        }
    }

    /**
     * 设置默认圆形的阴影颜色
     */
    public void setCircleShadowColor(@ColorInt int color) {
        if (usingDefaultHeader) {
            defaultProgressView.setShadowColor(color);
        }
    }

    public SSwipeRefreshLayout(Context context) {
        this(context, null);
    }

    @SuppressWarnings("deprecation")
    public SSwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 是一个距离，表示滑动的时候，手的移动要大于这个距离才开始移动控件。如果小于这个距离就不触发移动控件
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mMediumAnimationDuration = getResources().getInteger(android.R.integer.config_mediumAnimTime);

        setWillNotDraw(false);
        mDecelerateInterpolator = new DecelerateInterpolator(DECELERATE_INTERPOLATION_FACTOR);

        final TypedArray a = context.obtainStyledAttributes(attrs, LAYOUT_ATTRS);
        setEnabled(a.getBoolean(0, true));
        a.recycle();

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        final DisplayMetrics metrics = getResources().getDisplayMetrics();
        density = metrics.density;
        mTotalDragDistance = mSpinnerFinalOffset = DEFAULT_CIRCLE_TARGET * density;
        mHeaderViewWidth = mFooterViewWidth = display.getWidth();
        mHeaderViewHeight = mFooterViewHeight = (int) (HEADER_FOOTER_VIEW_HEIGHT * density);
        mDefaultViewHeight = (int) (DEFAULT_VIEW_HEIGHT * density);
        createHeaderViewContainer();
        createFooterViewContainer();
        ViewCompat.setChildrenDrawingOrderEnabled(this, true);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        if (getChildCount() == 0) {
            return;
        }
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        int distance = mCurrentTargetOffsetTop + mHeadViewContainer.getMeasuredHeight();
        if (!targetScrollWithLayout) {
            // 判断标志位，如果目标View不跟随手指的滑动而滑动，将下拉偏移量设置为0
            distance = 0;
        }
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop() + distance - pushDistance;// 根据偏移量distance更新
        final int childWidth = width - getPaddingLeft() - getPaddingRight();
        final int childHeight = height - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);// 更新目标View的位置
        int headViewWidth = mHeadViewContainer.getMeasuredWidth();
        int headViewHeight = mHeadViewContainer.getMeasuredHeight();
        mHeadViewContainer.layout((width / 2 - headViewWidth / 2),
                mCurrentTargetOffsetTop, (width / 2 + headViewWidth / 2),
                mCurrentTargetOffsetTop + headViewHeight);// 更新头布局的位置
        int footViewWidth = mFooterViewContainer.getMeasuredWidth();
        int footViewHeight = mFooterViewContainer.getMeasuredHeight();
        mFooterViewContainer.layout((width / 2 - footViewWidth / 2), height
                - pushDistance, (width / 2 + footViewWidth / 2), height
                + footViewHeight - pushDistance);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mTarget == null) {
            ensureTarget();
        }
        if (mTarget == null) {
            return;
        }
        mTarget.measure(MeasureSpec.makeMeasureSpec(getMeasuredWidth() - getPaddingLeft() - getPaddingRight(), MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(getMeasuredHeight() - getPaddingTop() - getPaddingBottom(), MeasureSpec.EXACTLY));
        mHeadViewContainer.measure(MeasureSpec.makeMeasureSpec(mHeaderViewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(3 * mHeaderViewHeight, MeasureSpec.EXACTLY));
        mFooterViewContainer.measure(MeasureSpec.makeMeasureSpec(mFooterViewWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(3 * mFooterViewHeight, MeasureSpec.EXACTLY));
        if (!mUsingCustomStart && !mOriginalOffsetCalculated) {
            mOriginalOffsetCalculated = true;
            mCurrentTargetOffsetTop = mOriginalOffsetTop = -mHeadViewContainer.getMeasuredHeight();
            updateListenerCallBack();
        }
        mHeaderViewIndex = -1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mHeadViewContainer) {
                mHeaderViewIndex = index;
                break;
            }
        }
        mFooterViewIndex = -1;
        for (int index = 0; index < getChildCount(); index++) {
            if (getChildAt(index) == mFooterViewContainer) {
                mFooterViewIndex = index;
                break;
            }
        }
    }

    /**
     * 子节点绘制的顺序
     */
    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        // 将新添加的View,放到最后绘制
        if (mHeaderViewIndex < 0 && mFooterViewIndex < 0) {
            return i;
        }
        if (i == childCount - 2) {
            return mHeaderViewIndex;
        }
        if (i == childCount - 1) {
            return mFooterViewIndex;
        }
        int bigIndex = mFooterViewIndex > mHeaderViewIndex ? mFooterViewIndex : mHeaderViewIndex;
        int smallIndex = mFooterViewIndex < mHeaderViewIndex ? mFooterViewIndex : mHeaderViewIndex;
        if (i >= smallIndex && i < bigIndex - 1) {
            return i + 1;
        }
        if (i >= bigIndex || (i == bigIndex - 1)) {
            return i + 2;
        }
        return i;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
    }

    /**
     * 主要判断是否应该拦截子View的事件<br>
     * 如果拦截，则交给自己的OnTouchEvent处理<br>
     * 否则，交给子View处理<br>
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        final int action = MotionEventCompat.getActionMasked(ev);
        if (!isEnabled() || mReturningToStart || mRefreshing || mLoadMore || (mListener == null && mOnLoadMoreListener == null) || (!isChildScrollToTop() && !isChildScrollToBottom())) {
            // 如果子View可以滑动，不拦截事件，交给子View处理-下拉刷新
            // 或者子View没有滑动到底部不拦截事件-上拉加载更多
            isToTop = false;
            isToBottom = false;
            return false;
        }
        if (action == MotionEvent.ACTION_DOWN) {
            setTargetOffsetTopAndBottom(mOriginalOffsetTop - mHeadViewContainer.getTop(), true);// 恢复HeaderView的初始位置
            mIsBeingDragged = false;
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            final float initialMotionY = getMotionEventY(ev, mActivePointerId);
            if (initialMotionY == -1) {
                return false;
            }
            mInitialMotionY = initialMotionY;// 记录按下的位置
            if (mScrollTriggerType == 2) {
                isToTop = isChildScrollToTop();
                isToBottom = isChildScrollToBottom();
            }
            if (mReturningToStart) {
                mReturningToStart = false;
            }
        }
        // 下拉刷新判断
        switch (action) {
            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == INVALID_POINTER) {
                    return false;
                }
                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == -1) {
                    return false;
                }
                if (mScrollTriggerType == 1) {
                    float scroll = y - mInitialMotionY;
                    if (mListener != null && isChildScrollToTop() && scroll > 0 && scroll > mTouchSlop && !mIsBeingDragged) {
                        mActionY = getMotionEventY(ev, MotionEventCompat.getPointerId(ev, 0));
                        isToTop = true;
                        isToBottom = false;
                        mIsBeingDragged = true;// 正在下拉
                    } else if (mOnLoadMoreListener != null && isChildScrollToBottom() && scroll < 0 && Math.abs(scroll) > mTouchSlop && !mIsBeingDragged) {
                        mActionY = getMotionEventY(ev, MotionEventCompat.getPointerId(ev, 0));
                        isToTop = false;
                        isToBottom = true;
                        mIsBeingDragged = true;// 正在上拉
                    }
                } else if (mScrollTriggerType == 2) {
                    if (isToTop && isToBottom) {
                        float scroll = y - mInitialMotionY;
                        if (mListener != null && scroll > 0 && scroll > mTouchSlop && !mIsBeingDragged) {
                            isToTop = true;
                            isToBottom = false;
                            mIsBeingDragged = true;// 正在下拉
                        } else if (mOnLoadMoreListener != null && scroll < 0 && Math.abs(scroll) > mTouchSlop && !mIsBeingDragged) {
                            isToTop = false;
                            isToBottom = true;
                            mIsBeingDragged = true;// 正在上拉
                        }
                    } else if (isToTop) {
                        if (mListener != null && isChildScrollToTop()) {
                            if ((y - mInitialMotionY) > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
                                isToTop = true;
                                isToBottom = false;
                                mIsBeingDragged = true;// 正在下拉
                            }
                        }
                    } else if (isToBottom) {
                        if (mOnLoadMoreListener != null && isChildScrollToBottom()) {
                            if ((mInitialMotionY - y) > mTouchSlop && !mIsBeingDragged) {// 判断是否下拉的距离足够
                                isToTop = false;
                                isToBottom = true;
                                mIsBeingDragged = true;// 正在上拉
                            }
                        }
                    }
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
        }
        return mIsBeingDragged;// 如果正在拖动，则拦截子View的事件
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        final int action = MotionEventCompat.getActionMasked(ev);
        if (mReturningToStart && action == MotionEvent.ACTION_DOWN) {
            mReturningToStart = false;
        }
        if (!isEnabled() || mReturningToStart || (mListener == null && mOnLoadMoreListener == null) || (!isChildScrollToTop() && !isChildScrollToBottom())) {
            // 如果子View可以滑动，不拦截事件，交给子View处理
            return false;
        }
        if (isToTop) {// 下拉刷新
            return handlerPullTouchEvent(ev, action);
        } else if (isToBottom) {// 上拉加载更多
            return handlerPushTouchEvent(ev, action);
        }
        return super.onTouchEvent(ev);
    }

    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = MotionEventCompat.findPointerIndex(ev, activePointerId);
        if (index < 0) {
            return -1;
        }
        return MotionEventCompat.getY(ev, index);
    }

    private boolean handlerPullTouchEvent(MotionEvent ev, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    return false;
                }
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollTop = (y - mActionY) * DRAG_RATE;
                if (mIsBeingDragged) {
                    float originalDragPercent = overscrollTop / mTotalDragDistance;
                    if (originalDragPercent < 0) {
                        return false;
                    }
                    float dragPercent = Math.min(1f, Math.abs(originalDragPercent));
                    float extraOS = Math.abs(overscrollTop) - mTotalDragDistance;
                    float slingshotDist = mUsingCustomStart ? mSpinnerFinalOffset - mOriginalOffsetTop : mSpinnerFinalOffset;
                    float tensionSlingshotPercent = Math.max(0, Math.min(extraOS, slingshotDist * 2) / slingshotDist);
                    float tensionPercent = (float) ((tensionSlingshotPercent / 4) - Math.pow((tensionSlingshotPercent / 4), 2)) * 2f;
                    float extraMove = (slingshotDist) * tensionPercent * 2;
                    int targetY = mOriginalOffsetTop + (int) ((slingshotDist * dragPercent) + extraMove);
                    if (mHeadViewContainer.getVisibility() != View.VISIBLE) {
                        mHeadViewContainer.setVisibility(View.VISIBLE);
                    }
                    if (!mScale) {
                        ViewCompat.setScaleX(mHeadViewContainer, 1f);
                        ViewCompat.setScaleY(mHeadViewContainer, 1f);
                    }
                    if (usingDefaultHeader) {
                        float alpha = overscrollTop / mTotalDragDistance;
                        if (alpha >= 1.0f) {
                            alpha = 1.0f;
                        }
                        ViewCompat.setScaleX(defaultProgressView, alpha);
                        ViewCompat.setScaleY(defaultProgressView, alpha);
                        ViewCompat.setAlpha(defaultProgressView, alpha);
                    }
                    if (overscrollTop < mTotalDragDistance) {
                        if (mScale) {
                            setAnimationProgress(overscrollTop / mTotalDragDistance);
                        }
                        if (mListener != null) {
                            mListener.onReach(false);
                        }
                    } else {
                        if (mListener != null) {
                            mListener.onReach(true);
                        }
                    }
                    setTargetOffsetTopAndBottom(targetY - mCurrentTargetOffsetTop, true);
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollTop = (y - mActionY) * DRAG_RATE;
                mIsBeingDragged = false;
                if (overscrollTop > mTotalDragDistance) {
                    setRefreshing(true, true /* notify */);
                } else {
                    mRefreshing = false;
                    AnimationListener listener = null;
                    if (!mScale) {
                        listener = new AnimationListener() {

                            @Override
                            public void onAnimationStart(Animation animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animation animation) {
                                if (!mScale) {
                                    startScaleDownAnimation(null);
                                }
                            }

                            @Override
                            public void onAnimationRepeat(Animation animation) {
                            }

                        };
                    }
                    animateOffsetToStartPosition(mCurrentTargetOffsetTop, listener);
                }
                mActivePointerId = INVALID_POINTER;
                return false;
            }
        }
        return true;
    }

    /**
     * 处理上拉加载更多的Touch事件
     */
    private boolean handlerPushTouchEvent(MotionEvent ev, int action) {
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                mIsBeingDragged = false;
                Log.d(TAG, "debug:onTouchEvent ACTION_DOWN");
                break;
            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0) {
                    Log.e(TAG, "Got ACTION_MOVE event but have an invalid active pointer id.");
                    return false;
                }
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollBottom = (mActionY - y) * DRAG_RATE;
                if (mIsBeingDragged) {
                    pushDistance = (int) overscrollBottom;
                    updateFooterViewPosition();
                    if (mOnLoadMoreListener != null) {
                        mOnLoadMoreListener.onReach(pushDistance >= mFooterViewHeight);
                    }
                }
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL: {
                if (mActivePointerId == INVALID_POINTER) {
                    if (action == MotionEvent.ACTION_UP) {
                        Log.e(TAG, "Got ACTION_UP event but don't have an active pointer id.");
                    }
                    return false;
                }
                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                final float y = MotionEventCompat.getY(ev, pointerIndex);
                final float overscrollBottom = (mActionY - y) * DRAG_RATE;// 松手是下拉的距离
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                if (overscrollBottom < mFooterViewHeight || mOnLoadMoreListener == null) {// 直接取消
                    pushDistance = 0;
                } else {// 下拉到mFooterViewHeight
                    pushDistance = mFooterViewHeight;
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                    updateFooterViewPosition();
                    if (pushDistance == mFooterViewHeight
                            && mOnLoadMoreListener != null) {
                        mLoadMore = true;
                        mOnLoadMoreListener.onLoadMore();
                    }
                } else {
                    animatorFooterToBottom((int) overscrollBottom, pushDistance);
                }
                return false;
            }
        }
        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    /**
     * 创建头布局的容器
     */
    private void createHeaderViewContainer() {
        defaultProgressView = new CircleProgressView(getContext());
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(mDefaultViewHeight, mDefaultViewHeight);
        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mHeadViewContainer = new HeadViewContainer(getContext());
        mHeadViewContainer.setVisibility(View.GONE);
        defaultProgressView.setVisibility(View.VISIBLE);
        defaultProgressView.setOnDraw(false);
        mHeadViewContainer.addView(defaultProgressView, layoutParams);
        addView(mHeadViewContainer);
    }

    /**
     * 添加底部布局
     */
    private void createFooterViewContainer() {
        mFooterViewContainer = new RelativeLayout(getContext());
        mFooterViewContainer.setVisibility(View.GONE);
        addView(mFooterViewContainer);
    }

    /**
     * 更新回调
     */
    private void updateListenerCallBack() {
        int distance = mCurrentTargetOffsetTop + mHeadViewContainer.getHeight();
        if (mListener != null) {
            mListener.onPullDistance(distance);
        }
        if (usingDefaultHeader && isProgressEnable) {
            defaultProgressView.setPullDistance(distance);
        }
    }

    private void startScaleUpAnimation(AnimationListener listener) {
        mHeadViewContainer.setVisibility(View.VISIBLE);
        mScaleAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(interpolatedTime);
            }
        };
        mScaleAnimation.setDuration(mMediumAnimationDuration);
        if (listener != null) {
            mHeadViewContainer.setAnimationListener(listener);
        }
        mHeadViewContainer.clearAnimation();
        mHeadViewContainer.startAnimation(mScaleAnimation);
    }

    private void setAnimationProgress(float progress) {
        if (!usingDefaultHeader) {
            progress = 1;
        }
        ViewCompat.setScaleX(mHeadViewContainer, progress);
        ViewCompat.setScaleY(mHeadViewContainer, progress);
    }

    private void setRefreshing(boolean refreshing, final boolean notify) {
        if (mRefreshing != refreshing) {
            mNotify = notify;
            ensureTarget();
            mRefreshing = refreshing;
            if (mRefreshing) {
                animateOffsetToCorrectPosition(mCurrentTargetOffsetTop, mRefreshListener);
            } else {
                //startScaleDownAnimation(mRefreshListener);
                animateOffsetToStartPosition(mCurrentTargetOffsetTop, mRefreshListener);
            }
        }
    }

    private void startScaleDownAnimation(AnimationListener listener) {
        mScaleDownAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                setAnimationProgress(1 - interpolatedTime);
            }
        };
        mScaleDownAnimation.setDuration(SCALE_DOWN_DURATION);
        mHeadViewContainer.setAnimationListener(listener);
        mHeadViewContainer.clearAnimation();
        mHeadViewContainer.startAnimation(mScaleDownAnimation);
    }

    /**
     * 确保mTarget不为空, mTarget一般是可滑动的ScrollView,ListView,RecyclerView等
     */
    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (!child.equals(mHeadViewContainer) && !child.equals(mFooterViewContainer) && !(child instanceof GroupHeadLayout)) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    /**
     * 判断是否滑动到顶部-还能否继续滑动
     */
    private boolean isChildScrollToTop() {
        if (Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return !(absListView.getChildCount() > 0 && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0).getTop() < absListView.getPaddingTop()));
            } else {
                return !(mTarget.getScrollY() > 0);
            }
        } else {
            return !ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    /**
     * 是否滑动到底部
     */
    private boolean isChildScrollToBottom() {
        if (mTarget instanceof RecyclerView) {
            RecyclerView recyclerView = (RecyclerView) mTarget;
            LayoutManager layoutManager = recyclerView.getLayoutManager();
            int count = recyclerView.getAdapter().getItemCount();
            if (layoutManager instanceof LinearLayoutManager && count > 0) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                if (linearLayoutManager.findLastCompletelyVisibleItemPosition() == count - 1) {
                    return true;
                }
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
                int[] lastItems = new int[2];
                staggeredGridLayoutManager.findLastCompletelyVisibleItemPositions(lastItems);
                int lastItem = Math.max(lastItems[0], lastItems[1]);
                if (lastItem == count - 1) {
                    return true;
                }
            }
            return false;
        } else if (mTarget instanceof AbsListView) {
            final AbsListView absListView = (AbsListView) mTarget;
            int count = absListView.getAdapter().getCount();
            int fristPos = absListView.getFirstVisiblePosition();
            if (fristPos == 0 && absListView.getChildAt(0).getTop() >= absListView.getPaddingTop()) {
                return false;
            }
            int lastPos = absListView.getLastVisiblePosition();
            if (lastPos > 0 && count > 0 && lastPos == count - 1) {
                return true;
            }
            return false;
        } else if (mTarget instanceof ScrollView) {
            ScrollView scrollView = (ScrollView) mTarget;
            View view = (View) scrollView.getChildAt(scrollView.getChildCount() - 1);
            if (view != null) {
                int diff = (view.getBottom() - (scrollView.getHeight() + scrollView.getScrollY()));
                if (diff == 0) {
                    return true;
                }
            }
        } else if (mTarget instanceof NestedScrollView) {
            NestedScrollView nestedScrollView = (NestedScrollView) mTarget;
            View view = (View) nestedScrollView.getChildAt(nestedScrollView.getChildCount() - 1);
            if (view != null) {
                int diff = (view.getBottom() - (nestedScrollView.getHeight() + nestedScrollView.getScrollY()));
                if (diff == 0) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 松手之后，使用动画将Footer从距离start变化到end
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void animatorFooterToBottom(int start, final int end) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(150);
        valueAnimator.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // update
                pushDistance = (Integer) valueAnimator.getAnimatedValue();
                updateFooterViewPosition();
            }
        });
        valueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (end > 0 && mOnLoadMoreListener != null) {
                    // start loading more
                    mLoadMore = true;
                    mOnLoadMoreListener.onLoadMore();
                } else {
                    resetTargetLayout();
                    mLoadMore = false;
                }
            }
        });
        valueAnimator.setInterpolator(mDecelerateInterpolator);
        valueAnimator.start();
    }

    private void animateOffsetToCorrectPosition(int from, AnimationListener listener) {
        mFrom = from;
        mAnimateToCorrectPosition.reset();
        mAnimateToCorrectPosition.setDuration(ANIMATE_TO_TRIGGER_DURATION);
        mAnimateToCorrectPosition.setInterpolator(mDecelerateInterpolator);
        if (listener != null) {
            mHeadViewContainer.setAnimationListener(listener);
        }
        mHeadViewContainer.clearAnimation();
        mHeadViewContainer.startAnimation(mAnimateToCorrectPosition);
    }

    private void animateOffsetToStartPosition(int from, AnimationListener listener) {
        if (mScale) {
            startScaleDownReturnToStartAnimation(from, listener);
        } else {
            mFrom = from;
            mAnimateToStartPosition.reset();
            mAnimateToStartPosition.setDuration(ANIMATE_TO_START_DURATION);
            mAnimateToStartPosition.setInterpolator(mDecelerateInterpolator);
            if (listener != null) {
                mHeadViewContainer.setAnimationListener(listener);
            }
            mHeadViewContainer.clearAnimation();
            mHeadViewContainer.startAnimation(mAnimateToStartPosition);
        }
        resetTargetLayoutDelay(ANIMATE_TO_START_DURATION);
    }

    /**
     * 重置Target位置
     */
    private void resetTargetLayoutDelay(int delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                resetTargetLayout();
            }
        }, delay);
    }

    /**
     * 重置Target的位置
     */
    private void resetTargetLayout() {
        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();
        final View child = mTarget;
        final int childLeft = getPaddingLeft();
        final int childTop = getPaddingTop();
        final int childWidth = child.getWidth() - getPaddingLeft() - getPaddingRight();
        final int childHeight = child.getHeight() - getPaddingTop() - getPaddingBottom();
        child.layout(childLeft, childTop, childLeft + childWidth, childTop + childHeight);
        int headViewWidth = mHeadViewContainer.getMeasuredWidth();
        int headViewHeight = mHeadViewContainer.getMeasuredHeight();
        mHeadViewContainer.layout((width / 2 - headViewWidth / 2), -headViewHeight, (width / 2 + headViewWidth / 2), 0);// 更新头布局的位置
        int footViewWidth = mFooterViewContainer.getMeasuredWidth();
        int footViewHeight = mFooterViewContainer.getMeasuredHeight();
        mFooterViewContainer.layout((width / 2 - footViewWidth / 2), height, (width / 2 + footViewWidth / 2), height + footViewHeight);
    }

    private void moveToStart(float interpolatedTime) {
        int targetTop = 0;
        targetTop = (mFrom + (int) ((mOriginalOffsetTop - mFrom) * interpolatedTime));
        int offset = targetTop - mHeadViewContainer.getTop();
        setTargetOffsetTopAndBottom(offset, false /* requires update */);
    }

    private void startScaleDownReturnToStartAnimation(int from, AnimationListener listener) {
        mFrom = from;
        mStartingScale = ViewCompat.getScaleX(mHeadViewContainer);
        mScaleDownToStartAnimation = new Animation() {
            @Override
            public void applyTransformation(float interpolatedTime, Transformation t) {
                float targetScale = (mStartingScale + (-mStartingScale * interpolatedTime));
                setAnimationProgress(targetScale);
                moveToStart(interpolatedTime);
            }
        };
        mScaleDownToStartAnimation.setDuration(SCALE_DOWN_DURATION);
        if (listener != null) {
            mHeadViewContainer.setAnimationListener(listener);
        }
        mHeadViewContainer.clearAnimation();
        mHeadViewContainer.startAnimation(mScaleDownToStartAnimation);
    }

    private void setTargetOffsetTopAndBottom(int offset, boolean requiresUpdate) {
        mHeadViewContainer.bringToFront();
        mHeadViewContainer.offsetTopAndBottom(offset);
        mCurrentTargetOffsetTop = mHeadViewContainer.getTop();
        if (requiresUpdate && Build.VERSION.SDK_INT < 11) {
            invalidate();
        }
        updateListenerCallBack();
    }

    /**
     * 修改底部布局的位置-敏感pushDistance
     */
    private void updateFooterViewPosition() {
        mFooterViewContainer.setVisibility(View.VISIBLE);
        mFooterViewContainer.bringToFront();
        //针对4.4及之前版本的兼容
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            mFooterViewContainer.getParent().requestLayout();
        }
        mFooterViewContainer.offsetTopAndBottom(-pushDistance);
        updatePushDistanceListener();
    }

    private void updatePushDistanceListener() {
        if (mOnLoadMoreListener != null) {
            mOnLoadMoreListener.onPushDistance(pushDistance);
        }
    }

    /**
     * 下拉刷新布局头部的容器
     */
    private class HeadViewContainer extends RelativeLayout {
        private AnimationListener mListener;

        public HeadViewContainer(Context context) {
            super(context);
        }

        public void setAnimationListener(AnimationListener listener) {
            mListener = listener;
        }

        @Override
        public void onAnimationStart() {
            super.onAnimationStart();
            if (mListener != null) {
                mListener.onAnimationStart(getAnimation());
            }
        }

        @Override
        public void onAnimationEnd() {
            super.onAnimationEnd();
            if (mListener != null) {
                mListener.onAnimationEnd(getAnimation());
            }
        }
    }

    /**
     * 默认的下拉刷新样式
     */
    private class CircleProgressView extends View implements Runnable {
        private static final int PEROID = 16;// 绘制周期
        private Paint progressPaint;
        private Paint bgPaint;
        private int width;// view的高度
        private int height;// view的宽度
        private boolean isOnDraw = false;
        private boolean isRunning = false;
        private int startAngle = 0;
        private int speed = 8;
        private RectF ovalRect = null;
        private RectF bgRect = null;
        private int swipeAngle;
        private int progressColor = 0xffff0000;
        private int circleBackgroundColor = 0xffffffff;
        private int shadowColor = 0xff0000ff;

        public CircleProgressView(Context context) {
            super(context);
        }

        public CircleProgressView(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        public CircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
            super(context, attrs, defStyleAttr);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawArc(getBgRect(), 0, 360, false, createBgPaint());
            int index = startAngle / 360;
            if (index % 2 == 0) {
                swipeAngle = (startAngle % 720) / 2;
            } else {
                swipeAngle = 360 - (startAngle % 720) / 2;
            }
            canvas.drawArc(getOvalRect(), startAngle, swipeAngle, false, createPaint());
        }

        @Override
        public void onWindowFocusChanged(boolean hasWindowFocus) {
            super.onWindowFocusChanged(hasWindowFocus);
        }

        @Override
        protected void onDetachedFromWindow() {
            isOnDraw = false;
            super.onDetachedFromWindow();
        }

        @Override
        public void run() {
            while (isOnDraw) {
                isRunning = true;
                long startTime = System.currentTimeMillis();
                startAngle += speed;
                postInvalidate();
                long time = System.currentTimeMillis() - startTime;
                if (time < PEROID) {
                    try {
                        Thread.sleep(PEROID - time);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        private RectF getBgRect() {
            width = getWidth();
            height = getHeight();
            if (bgRect == null) {
                int offset = (int) (density * 2);
                bgRect = new RectF(offset, offset, width - offset, height - offset);
            }
            return bgRect;
        }

        private RectF getOvalRect() {
            width = getWidth();
            height = getHeight();
            if (ovalRect == null) {
                int offset = (int) (density * 13);
                ovalRect = new RectF(offset, offset, width - offset, height - offset);
            }
            return ovalRect;
        }

        /**
         * 根据画笔的颜色，创建画笔
         */
        private Paint createPaint() {
            if (this.progressPaint == null) {
                progressPaint = new Paint();
                progressPaint.setStrokeWidth((int) (density * 3));
                progressPaint.setStyle(Paint.Style.STROKE);
                progressPaint.setAntiAlias(true);
            }
            progressPaint.setColor(progressColor);
            return progressPaint;
        }

        private Paint createBgPaint() {
            if (this.bgPaint == null) {
                bgPaint = new Paint();
                bgPaint.setColor(circleBackgroundColor);
                bgPaint.setStyle(Paint.Style.FILL);
                bgPaint.setAntiAlias(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    this.setLayerType(LAYER_TYPE_SOFTWARE, bgPaint);
                }
                bgPaint.setShadowLayer(4.0f, 0.0f, 2.0f, shadowColor);
            }
            return bgPaint;
        }

        protected void setOnDraw(boolean isOnDraw) {
            this.isOnDraw = isOnDraw;
        }

        protected void setPullDistance(int distance) {
            this.startAngle = distance * 2;
            postInvalidate();
        }

        public void setProgressColor(@ColorInt int progressColor) {
            this.progressColor = progressColor;
        }

        public void setCircleBackgroundColor(@ColorInt int circleBackgroundColor) {
            this.circleBackgroundColor = circleBackgroundColor;
        }

        public void setShadowColor(@ColorInt int shadowColor) {
            this.shadowColor = shadowColor;
        }
    }
}
