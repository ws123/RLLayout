package com.carlos.rllayout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.carlos.IFooterView;

/**
 * Created by carlos on 16/5/2.
 * 上拉加载的FooterView
 */
public class RLFooterView implements IFooterView {
    private Context context;
    private View view;
    private Animation animation;

    public RLFooterView(Context context, ViewGroup viewGroup) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.layout_rl_headview, viewGroup, false);
    }

    @Override
    public View initFooterView() {
        return view;
    }

    @Override
    public void pullingDown(View view, int translateY) {

    }

    @Override
    public void passThreshold(View view, int threshold) {
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.firstImage).setRotation(180);
        ((TextView) view.findViewById(com.carlos.rllayout.mylibrary.R.id.textView)).setText("松开即可刷新");
    }

    @Override
    public void backToThreshold(View view, int threshold) {
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.firstImage).setRotation(0);
        ((TextView) view.findViewById(com.carlos.rllayout.mylibrary.R.id.textView)).setText("下拉刷新");
    }

    @Override
    public void fingerUp(View view) {
        //开始刷新中的动画
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.firstImage).setVisibility(View.GONE);
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.secondImage).setVisibility(View.VISIBLE);
        if (animation == null) {
            animation = AnimationUtils.loadAnimation(context, com.carlos.rllayout.mylibrary.R.anim.rotate_anim);
        }
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.secondImage).startAnimation(animation);
        ((TextView) view.findViewById(com.carlos.rllayout.mylibrary.R.id.textView)).setText("正在刷新数据");
    }

    @Override
    public void stopLoadMore(View view) {
        //恢复headView
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.secondImage).clearAnimation();
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.firstImage).setVisibility(View.VISIBLE);
        view.findViewById(com.carlos.rllayout.mylibrary.R.id.secondImage).setVisibility(View.GONE);
    }
}
