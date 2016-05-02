package com.carlos;

import android.content.Context;
import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.RelativeLayout;

/**
 * Created by carlos on 16/4/30.
 * 一个可以下拉刷新上拉加载的布局,直接把ListView放在布局中就可以了
 */
public class RLLayout extends RelativeLayout {
    private AbsListView absListView;

    private boolean isScrollToTop;
    private boolean isScrollToBottom;
    private int lastTouchY;

    private int touchStart = 0;
    //listView的回弹速度
    private int speedBackToPlace = 15;
    //头部下拉刷新的view动画
    private View headerView = null;
    private IHeaderView iHeaderView;
    //上拉加载的View动画
    private View footerView = null;
    private IFooterView iFooterView;
    //是否超过了临界值
    private boolean isPassThreshold = false;
    //刷新的监听器
    private RefreshListener refreshListener;
    //是否正在刷新
    private boolean isRefreshOrLoadMore = false;


    public RLLayout(Context context) {
        super(context);
    }

    public RLLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RLLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isRefreshOrLoadMore) return super.dispatchTouchEvent(ev);
        int touchY = (int) ev.getY();
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStart = touchY;
                lastTouchY = touchY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (lastTouchY != 0) {
                    if (touchY > lastTouchY) {
                        //在往下滑
                        if (absListView == null) {
                            if (getChildCount() > 3 || !(getChildAt(0) instanceof AbsListView)) {
                                throw new RLException("布局里面只能有一个AbsListView");
                            }
                            absListView = (AbsListView) getChildAt(0);
                        }
                        if (!ViewCompat.canScrollVertically(absListView, -1)) {
                            isScrollToTop = true;
                        }
                    } else {
                        //在往上滑
                        if (absListView == null) {
                            if (getChildCount() > 3 || !(getChildAt(0) instanceof AbsListView)) {
                                throw new RLException("布局里面只能有一个AbsListView");
                            }
                            absListView = (AbsListView) getChildAt(0);
                        }
                        if (!ViewCompat.canScrollVertically(absListView, 1)) {
                            isScrollToBottom = true;
                        }
                    }
                }
                lastTouchY = touchY;
                break;
            case MotionEvent.ACTION_UP:
                lastTouchY = 0;
                isScrollToBottom = false;
                isScrollToTop = false;
                touchStart = 0;
                filterActionUp(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                lastTouchY = 0;
                isScrollToBottom = false;
                isScrollToTop = false;
                touchStart = 0;
                filterActionUp(true);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
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
     * 对手势过一下过滤处理,对于手势的判断逻辑在这里
     *
     * @param event 手势事件
     */
    private void filterTouchEvent(MotionEvent event) {
        int y = (int) event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                if (isScrollToTop) {
                    translateListView((y - touchStart) / 3);
                } else {
                    translateListView((y - touchStart) / 3);
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
        if (headerView == null) return;
        if (!isPassThreshold && absListView.getTranslationY() >= headerView.getHeight()) {
            iHeaderView.passThreshold(headerView, headerView.getHeight());
            isPassThreshold = true;
        } else if (isPassThreshold && absListView.getTranslationY() < headerView.getHeight()) {
            iHeaderView.backToThreshold(headerView, headerView.getHeight());
            isPassThreshold = false;
        }
    }

    /**
     * 判断并调用footerview的临界值方法
     */
    private void filterFooterViewThreshold() {
        if (footerView == null) return;
        if (!isPassThreshold && absListView.getTranslationY() <= -footerView.getHeight()) {
            iFooterView.passThreshold(footerView, footerView.getHeight());
            isPassThreshold = true;
        } else if (isPassThreshold && absListView.getTranslationY() > -footerView.getHeight()) {
            iFooterView.backToThreshold(footerView, footerView.getHeight());
            isPassThreshold = false;
        }
    }

    /**
     * 让listview回弹回原来的位置
     */
    private void backToPlace() {
        isRefreshOrLoadMore = false;
        if (absListView.getTranslationY() > 0) {
            if (absListView.getTranslationY() > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                if (headerView != null)
                    headerView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
            } else {
                absListView.setTranslationY(0);
                if (headerView != null)
                    headerView.setTranslationY(0);
            }
            filterHeaderViewThreshold();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backToPlace();
                }
            }, 10);
        } else if (absListView.getTranslationY() < 0) {
            if (absListView.getTranslationY() + speedBackToPlace < 0) {
                absListView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                if (footerView != null)
                    footerView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
            } else {
                absListView.setTranslationY(0);
                if (footerView != null)
                    footerView.setTranslationY(0);
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
    private void backToResreshOrLoad(boolean isRefresh) {
        if (isRefresh) {
            if (absListView.getTranslationY() - headerView.getHeight() > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                headerView.setTranslationY(absListView.getTranslationY() - speedBackToPlace);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backToResreshOrLoad(true);
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
            if (Math.abs(absListView.getTranslationY() + footerView.getHeight()) > speedBackToPlace) {
                absListView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                footerView.setTranslationY(absListView.getTranslationY() + speedBackToPlace);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        backToResreshOrLoad(false);
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
            if (headerView == null) {
                backToPlace();
                return;
            }
            //是下拉手势
            if (absListView.getTranslationY() >= headerView.getHeight()) {
                //达到了下拉刷新的条件
                backToResreshOrLoad(true);
            } else {
                backToPlace();
            }
        } else if (absListView.getTranslationY() < 0) {
            //是上拉手势
            if (footerView == null) {
                backToPlace();
                return;
            }
            if (absListView.getTranslationY() <= -footerView.getHeight()) {
                //达到了上拉加载的条件
                backToResreshOrLoad(false);
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
        this.iFooterView = iFooterView;
        footerView = iFooterView.initFooterView();
        this.addView(footerView);
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
