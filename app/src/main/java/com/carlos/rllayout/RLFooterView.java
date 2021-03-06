package com.carlos.rllayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.carlos.IFooterView;
import com.carlos.RLLayout;

/**
 * Created by carlos on 16/5/2.
 * 上拉加载的FooterView
 */
public class RLFooterView implements IFooterView {
    private Context context;
    private View view;
    private Animation animation;

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param parent  RLLayout的实例
     */
    public RLFooterView(Context context, RLLayout parent) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.layout_rl_footview, parent, false);
    }

    // 返回一个自定义的footview的实例View
    @Override
    public View initFooterView() {
        return view;
    }

    //用户在下拉过程中，这个方法会不断被回调
    //translateY为下拉的距离，会越来越大，最大值是view.getHeight()
    @Override
    public void pullingDown(View view, int translateY) {

    }

    //上拉的距离超过阀值的时候，会回调这个方法
    @Override
    public void passThreshold(View view, int threshold) {
        view.findViewById(R.id.firstImage).setRotation(180);
        ((TextView) view.findViewById(R.id.textView)).setText("松开即可刷新");
    }

    //上拉的距离又小于阀值的时候，会回调这个方法
    @Override
    public void backToThreshold(View view, int threshold) {
        view.findViewById(R.id.firstImage).setRotation(0);
        ((TextView) view.findViewById(R.id.textView)).setText("上拉刷新");
    }

    //达到下拉刷新的条件，并且松开了手指，会回调这个方法
    //也就是正在下拉刷新中
    @Override
    public void fingerUp(View view) {
        //开始刷新中的动画
        view.findViewById(R.id.firstImage).setVisibility(View.GONE);
        view.findViewById(R.id.secondImage).setVisibility(View.VISIBLE);
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(context, R.anim.rotate_anim);
        }
        view.findViewById(R.id.secondImage).startAnimation(animation);
        ((TextView) view.findViewById(R.id.textView)).setText("正在刷新数据");
    }

    //停止刷新的时候回调这个方法
    @Override
    public void stopLoadMore(View view) {
        //恢复headView
        view.findViewById(R.id.secondImage).clearAnimation();
        view.findViewById(R.id.firstImage).setVisibility(View.VISIBLE);
        view.findViewById(R.id.secondImage).setVisibility(View.GONE);
    }
}
