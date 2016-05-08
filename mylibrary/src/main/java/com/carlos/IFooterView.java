package com.carlos;

import android.view.View;

/**
 * Created by carlos on 16/5/2.
 * 底部上拉加载的View的接口
 */
public interface IFooterView {
    /**
     * 初始化一个footerview,使用LayoutInflater来实例一个xml布局文件
     *
     * @return 返回一个实例化的父布局
     */
    View initFooterView();

    /**
     * 上拉方法,表示整个View正在被上拉出现
     * 在上拉过程中,这个方法会被多次调用
     * 可以根据偏移量的大小,在这里做一些动画,比如偏移量越大,箭头越来越大,颜色越来越重等等
     * 如果上拉超过临界后,会调用passThreshold()方法
     * 临界值为自己定义的footerView的高度
     *
     * @param translateY 上拉的偏移量
     *                   在上拉过程中,会有一个临界值,
     *                   如果小于临界值,translateY会越来越大
     *                   如果大于临界值,即使再继续下拉,translateY也不会再变大
     *                   如果一直小于临界值,用户没有松手,而是又往上滑,translateY会慢慢变小,最小为0
     *                   如果大于临界值,用户没有松手,又往下滑,直到小于临界值,translateY才会慢慢变小,最小为0
     * @param view       footerView,就是View initFooterView()这个方法返回的View实例
     */
    void pullingDown(View view, int translateY);

    /**
     * 用户上拉超过临界值后,松开手指会调用这个方法
     * 如果没有松手,即使超过临界值,也不会调用这个方法
     *
     * @param threshold 临界值
     * @param view      footerView,就是View initFooterView()这个方法返回的View实例
     */
    void passThreshold(View view, int threshold);

    /**
     * 用户的上拉距离又低于临界值了
     *
     * @param threshold 临界值
     * @param view      footerView,就是View initFooterView()这个方法返回的View实例
     */
    void backToThreshold(View view, int threshold);

    /**
     * 上拉超过临界值后,用户松开手机,会调用这个方法,也就是执行上拉加载
     * 也就是说,这个方法一定会在passThreshold()之后调用
     * 但调用passThreshold()后,如果用户没有松开手指,这个方法不会调用
     *
     * @param view footerView,就是View initFooterView()这个方法返回的View实例
     */
    void fingerUp(View view);

    /**
     * 停止加载,应该在这个方法里,还原上拉加载的FooterView
     *
     * @param view footerView,就是View initFooterView()这个方法返回的View实例
     */
    void stopLoadMore(View view);
}
