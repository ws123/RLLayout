package com.carlos;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

import com.carlos.rllayout.mylibrary.R;

/**
 * Created by carlos on 16/4/30.
 * 一个可以下拉刷新上拉加载的布局,直接把ListView放在布局中就可以了
 */
public class RLLayout extends RelativeLayout {
    //布局中的AbsListView
    private AbsListView absListView;
    //是否滚动到了顶部
    private boolean isScrollToTop = false;
    //是否滚动到了底部
    private boolean isScrollToBottom = false;
    //上次手势的Y轴坐标
    private int lastTouchY;
    //ACTION_DOWN时候的Y坐标
    private int touchStart = 0;
    //listView的回弹速度
    private int speedBackToPlace = 10;
    //头部下拉刷新的view动画
    private View headerView = null;
    private IHeaderView iHeaderView;
    //上拉加载的View动画
    private View footerView = null;
    private IFooterView iFooterView;
    //headerView是否超过了临界值
    private boolean isPassThreshold = false;
    //footerView是否超过了临界值
    private boolean isFooterPassThreshold = false;
    //刷新的监听器
    private RefreshListener refreshListener;
    //是否正在刷新
    private boolean isRefreshOrLoadMore = false;
    //是否开启下拉刷新功能
    private boolean isPullToRefresh = false;
    //是否开启上拉加载功能
    private boolean isLoadMore = false;


    public RLLayout(Context context) {
        super(context);
    }

    public RLLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initProperty(context, attrs);
    }

    public RLLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initProperty(context, attrs);
    }

    private void initProperty(Context context, AttributeSet attributeSet) {
        TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.rlLayout);
        isPullToRefresh = typedArray.getBoolean(R.styleable.rlLayout_enablePullToRefresh, false);
        isLoadMore = typedArray.getBoolean(R.styleable.rlLayout_enableLoadMore, false);
        typedArray.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (headerView != null) {
            headerView.layout(0, -headerView.getHeight(), getWidth(), 0);
        }
        if (footerView != null) {
            footerView.layout(0, getHeight(), getWidth(), getHeight() + footerView.getHeight());
        }
        initAbsListView();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isRefreshOrLoadMore || (!isPullToRefresh && !isLoadMore) || absListView == null) {
            return super.dispatchTouchEvent(ev);
        }
        int touchY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                zeroTemporaryData();
                touchStart = touchY;
                lastTouchY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastTouchY != 0) {
                    if (touchY > lastTouchY) {
                        if (!isPullToRefresh) return super.dispatchTouchEvent(ev);
                        //在往下滑
                        if (!ViewCompat.canScrollVertically(absListView, -1)) {
                            isScrollToTop = true;
                        }
                    } else {
                        if (!isLoadMore) return super.dispatchTouchEvent(ev);
                        //在往上滑
                        if (!ViewCompat.canScrollVertically(absListView, 1)) {
                            isScrollToBottom = true;
                        }
                    }
                }
                lastTouchY = touchY;
                break;
            case MotionEvent.ACTION_UP:
                zeroTemporaryData();
                filterActionUp(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                zeroTemporaryData();
                filterActionUp(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        System.out.println(ev.toString());
        return isScrollToTop || isScrollToBottom || super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isScrollToBottom || isScrollToTop) {
            filterTouchEvent(event);
            return true;
        } else {
            return super.onTouchEvent(event);
        }
    }

    /**
     * 清空触摸临时数据
     */
    private void zeroTemporaryData() {
        lastTouchY = 0;
        isScrollToBottom = false;
        isScrollToTop = false;
        touchStart = 0;
    }

    /**
     * 对手势过一下过滤处理,对于手势的判断逻辑在这里
     *
     * @param event 手势事件
     */
    private void filterTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isScrollToTop) {
                    if (y < touchStart) return;
                    if (headerView != null) {
                        if ((y - touchStart) / 2 < headerView.getHeight() * 3 / 2) {
                            translateListView((y - touchStart) / 2);
                        } else {
                            translateListView(headerView.getHeight() * 3 / 2);
                        }
                        return;
                    }
                    translateListView((y - touchStart) / 2);
                } else {
                    if (y > touchStart) return;
                    if (footerView != null) {
                        if ((y - touchStart) / 2 > -footerView.getHeight() * 3 / 2) {
                            translateListView((y - touchStart) / 2);
                        } else {
                            translateListView(-footerView.getHeight() * 3 / 2);
                        }
                        return;
                    }
                    translateListView((y - touchStart) / 2);
                }
                break;
        }
    }

    /**
     * 根据手势,让listview位移
     *
     * @param translateY 位移的距离
     */
    private void translateListView(int translateY) {
        System.out.println("位移的量" + translateY);
        absListView.setTranslationY(translateY);
        if (headerView != null)
            headerView.setTranslationY(translateY);
        if (footerView != null)
            footerView.setTranslationY(translateY);
        filterHeaderViewThreshold();
        filterFooterViewThreshold();
    }

    /**
     * 判断并调用headerview的临界值方法
     */
    private void filterHeaderViewThreshold() {
        if (headerView == null || !isPullToRefresh || !isScrollToTop) return;
        if (!isPassThreshold && absListView.getTranslationY() >= headerView.getHeight()) {
            iHeaderView.passThreshold(headerView, headerView.getHeight());
            isPassThreshold = true;
        } else if (isPassThreshold && absListView.getTranslationY() < headerView.getHeight()) {
            iHeaderView.backToThreshold(headerView, headerView.getHeight());
            isPassThreshold = false;
        }
        if (absListView.getTranslationY() <= headerView.getHeight()) {
            iHeaderView.pullingDown(headerView, (int) absListView.getTranslationY());
        }
    }

    /**
     * 判断并调用footerview的临界值方法
     */
    private void filterFooterViewThreshold() {
        if (footerView == null || !isLoadMore || !isScrollToBottom) return;
        if (!isFooterPassThreshold && absListView.getTranslationY() <= -footerView.getHeight()) {
            iFooterView.passThreshold(footerView, footerView.getHeight());
            isFooterPassThreshold = true;
        } else if (isFooterPassThreshold && absListView.getTranslationY() > -footerView.getHeight()) {
            iFooterView.backToThreshold(footerView, footerView.getHeight());
            isFooterPassThreshold = false;
        }
        if (absListView.getTranslationY() >= -footerView.getHeight()) {
            iFooterView.pullingDown(footerView, Math.abs((int) absListView.getTranslationY()));
        }
    }

    /**
     * 让listview回弹回原来的位置
     */
    private void backToPlace() {
        if (absListView.getTranslationY() > 0) {
            if (!isPullToRefresh) return;
            if (absListView.getTranslationY() > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                if (headerView != null)
                    headerView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
            } else {
                absListView.setTranslationY(0);
                isRefreshOrLoadMore = false;
                if (headerView != null)
                    headerView.setTranslationY(0);
            }
            filterHeaderViewThreshold();
            if (headerView != null && absListView.getTranslationY() <= headerView.getHeight()) {
                iHeaderView.pullingDown(headerView, (int) absListView.getTranslationY());
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backToPlace();
                }
            }, 10);
        } else if (absListView.getTranslationY() < 0) {
            if (!isLoadMore) return;
            if (absListView.getTranslationY() + speedBackToPlace < 0) {
                absListView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                if (footerView != null)
                    footerView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
            } else {
                isRefreshOrLoadMore = false;
                absListView.setTranslationY(0);
                if (footerView != null)
                    footerView.setTranslationY(0);
            }
            if (footerView != null && absListView.getTranslationY() >= -footerView.getHeight()) {
                iFooterView.pullingDown(footerView, Math.abs((int) absListView.getTranslationY()));
            }
            filterFooterViewThreshold();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backToPlace();
                }
            }, 10);
        }
    }


    /**
     * 手势已经达到了下拉刷新或者上拉加载的条件,手指抬起后,调用这个方法使用view回到原位
     *
     * @param isRefresh 是否是下拉刷新
     */
    private void backToRefreshOrLoad(boolean isRefresh) {
        if (isRefresh) {
            if (!isPullToRefresh) return;
            if (absListView.getTranslationY() - headerView.getHeight() > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                headerView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backToRefreshOrLoad(true);
                    }
                }, 10);
            } else {
                //正式开始下拉刷新
                absListView.setTranslationY(headerView.getHeight());
                headerView.setTranslationY(headerView.getHeight());
                iHeaderView.fingerUp(headerView);
                if (refreshListener != null) refreshListener.startRefresh();
                isRefreshOrLoadMore = true;
            }
        } else {
            if (!isLoadMore) return;
            if (Math.abs(absListView.getTranslationY() + footerView.getHeight()) > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                footerView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backToRefreshOrLoad(false);
                    }
                }, 10);
            } else {
                absListView.setTranslationY(-footerView.getHeight());
                footerView.setTranslationY(-footerView.getHeight());
                iFooterView.fingerUp(footerView);
                if (refreshListener != null) refreshListener.startLoadMore();
                isRefreshOrLoadMore = true;
            }
        }
    }

    /**
     * 初使化AbsListView
     */
    private void initAbsListView() {
        if (absListView == null) {
            if (getChildCount() > 3 || !(getChildAt(0) instanceof AbsListView)) {
                throw new RLException("布局里面只能有一个AbsListView");
            }
            absListView = (AbsListView) getChildAt(0);
        }
    }

    /**
     * 用户手指抬起或者取消后的处理方法
     *
     * @param isCancel 是否是取消手势
     */
    private void filterActionUp(boolean isCancel) {
        if (isCancel) {
            backToPlace();
            return;
        }
        if (absListView.getTranslationY() > 0) {
            if (!isPullToRefresh) return;
            if (headerView == null) {
                backToPlace();
                return;
            }
            //是下拉手势
            if (absListView.getTranslationY() >= headerView.getHeight()) {
                //达到了下拉刷新的条件
                backToRefreshOrLoad(true);
            } else {
                backToPlace();
            }
        } else if (absListView.getTranslationY() < 0) {
            if (!isLoadMore) return;
            //是上拉手势
            if (footerView == null) {
                backToPlace();
                return;
            }
            if (absListView.getTranslationY() <= -footerView.getHeight()) {
                //达到了上拉加载的条件
                backToRefreshOrLoad(false);
            } else {
                backToPlace();
            }
        }
    }

    /**
     * @param speedBackToPlace 设置回弹的速度
     */
    public void setSpeedBackToPlace(int speedBackToPlace) {
        this.speedBackToPlace = speedBackToPlace;
    }

    /**
     * 停止下拉刷新或者加载更多
     */
    public void stopRefreshOrLoadMore() {
        if (iHeaderView != null)
            iHeaderView.stopRefresh(headerView);
        if (iFooterView != null)
            iFooterView.stopLoadMore(footerView);
        backToPlace();
    }

    /**
     * @param iHeaderView 要设置下拉刷新的头部View
     */
    public void setiHeaderView(IHeaderView iHeaderView) {
        if (iHeaderView == null)
            throw new RLException("iHeaderView can not be null");
        if (!isPullToRefresh) return;
        this.iHeaderView = iHeaderView;
        headerView = iHeaderView.initHeaderView();
        this.addView(this.headerView);
    }

    /**
     * @param refreshListener 下拉刷新与上拉加载的监听器
     */
    public void setRefreshListener(RefreshListener refreshListener) {
        this.refreshListener = refreshListener;
    }

    /**
     * @param iFooterView 要设置的上拉加载的底部的View
     */
    public void setiFooterView(IFooterView iFooterView) {
        if (iFooterView == null) {
            throw new RLException("iFooterView can not be null");
        }
        if (!isLoadMore) return;
        this.iFooterView = iFooterView;
        footerView = iFooterView.initFooterView();
        this.addView(footerView);
    }

    /**
     * @param pullToRefresh 设置是否允许下拉刷新
     */
    public void setPullToRefresh(boolean pullToRefresh) {
        isPullToRefresh = pullToRefresh;
    }

    /**
     * @param loadMore 设置是否允许上拉加载更多
     */
    public void setLoadMore(boolean loadMore) {
        isLoadMore = loadMore;
    }

    public interface RefreshListener {
        /**
         * 下拉刷新触发成功
         */
        void startRefresh();

        /**
         * 上拉加载触发成功
         */
        void startLoadMore();
    }
}
