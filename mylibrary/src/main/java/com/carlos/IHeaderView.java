package com.carlos;

import android.view.View;

/**
 * Created by carlos on 16/5/1.
 * 头部View的接口
 */
public interface IHeaderView {

    /**
     * 初始化一个headview,使用LayoutInflater来实例一个xml布局文件
     *
     * @return 返回一个实例化的父布局
     */
    View initHeaderView();

    /**
     * 下拉方法,表示整个View正在被下拉出现<p/>
     * 在下拉过程中,这个方法会被多次调用<p/>
     * 可以根据偏移量的大小,在这里做一些动画,比如偏移量越大,箭头越来越大,颜色越来越重等等<p/>
     * 如果下拉超过临界后,会调用passThreshold()方法<p/>
     *
     * @param translateY 下拉的偏移量<p/>
     *                   在下拉过程中,会有一个临界值,<p/>
     *                   如果小于临界值,translateY会越来越大<p/>
     *                   如果大于临界值,即使再继续下拉,translateY也不会再变大<p/>
     *                   如果一直小于临界值,用户没有松手,而是又往上滑,translateY会慢慢变小,最小为0<p/>
     *                   如果大于临界值,用户没有松手,又往上滑,直到小于临界值,translateY才会慢慢变小,最小为0<p/>
     * @param view       headView,就是View initHeaderView()这个方法返回的View实例<p/>
     */
    void pullingDown(View view, int translateY);

    /**
     * 用户下拉超过临界值后,松开手指会调用这个方法<p/>
     * 如果没有松手,即使超过临界值,也不会调用这个方法<p/>
     *
     * @param threshold 临界值
     * @param view      headView,就是View initHeaderView()这个方法返回的View实例
     */
    void passThreshold(View view, int threshold);

    /**
     * 用户的下拉距离又低于临界值了
     *
     * @param threshold 临界值
     * @param view      headView,就是View initHeaderView()这个方法返回的View实例
     */
    void backToThreshold(View view, int threshold);

    /**
     * 下拉超过临界值后,用户松开手机,会调用这个方法,也就是执行下拉刷新<p/>
     * 也就是说,这个方法一定会在passThreshold()之后调用<p/>
     * 但调用passThreshold()后,如果用户没有松开手指,这个方法不会调用<p/>
     *
     * @param view headView,就是View initHeaderView()这个方法返回的View实例
     */
    void fingerUp(View view);

    /**
     * 停止刷新,应该在这个方法里,还原下拉刷新的HeadView
     *
     * @param view headView,就是View initHeaderView()这个方法返回的View实例
     */
    void stopRefresh(View view);
}
