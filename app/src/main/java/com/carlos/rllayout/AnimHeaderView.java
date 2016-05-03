package com.carlos.rllayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.carlos.IHeaderView;
import com.carlos.RLLayout;

/**
 * Created by carlos on 2016/5/3.
 * 一个带动画的HeaderView
 */
public class AnimHeaderView implements IHeaderView {
    private View view;
    private Animation animation;
    private Context context;

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param parent  RLLayout的实例
     */
    public AnimHeaderView(Context context, RLLayout parent) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.layout_anim_headerview, parent, false);
    }

    //用户必须重写这个方法，返回一个View实例来作为HeaderView
    @Override
    public View initHeaderView() {
        return view;
    }

    //用户在下拉过程中，这个方法会不断被回调
    //translateY为下拉的距离，会越来越大，最大值是view.getHeight()
    @Override
    public void pullingDown(View view, int translateY) {
        //这个translateY是一个随着下拉，慢慢变大的值
        //这里，我想让一个图片随着下拉，慢慢变大，但最大是原来的两倍
        //并且随着下拉,图片透明度越来越大,文字也越来越黑
        float scale = 1 + ((float) translateY / (float) view.getHeight());
        float alpha = ((float) translateY / (float) view.getHeight());
        view.findViewById(R.id.firstImage).setScaleX(scale);
        view.findViewById(R.id.firstImage).setAlpha(alpha);
        view.findViewById(R.id.textView).setAlpha(alpha);
        view.findViewById(R.id.firstImage).setScaleY(scale);
    }

    //下拉的距离超过了阀值
    @Override
    public void passThreshold(View view, int threshold) {
        ((TextView) view.findViewById(R.id.textView)).setText("松开即可刷新");
    }

    //下拉的距离又低于阀值
    @Override
    public void backToThreshold(View view, int threshold) {
        ((TextView) view.findViewById(R.id.textView)).setText("下拉刷新");
    }

    //达到下拉刷新的条件，并且松开了手指，会回调这个方法
    //也就是正在下拉刷新中
    @Override
    public void fingerUp(View view) {
        //开始刷新中的动画
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(context, R.anim.rotate_anim);
        }
        view.findViewById(R.id.firstImage).startAnimation(animation);
        ((TextView) view.findViewById(R.id.textView)).setText("正在刷新数据");
    }

    //停止刷新的时候回调这个方法
    @Override
    public void stopRefresh(View view) {
        view.findViewById(R.id.firstImage).clearAnimation();
        ((TextView) view.findViewById(R.id.textView)).setText("下拉刷新");
    }
}
