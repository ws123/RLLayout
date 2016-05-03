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
 * Created by carlos on 16/5/1.
 * 自己实现的HeaderView
 */
public class RLHeaderView implements IHeaderView {
    private Context context;
    private View view;
    private Animation animation;

    /**
     * 构造函数
     *
     * @param context 上下文
     * @param parent  RLLayout的实例
     */
    public RLHeaderView(Context context, RLLayout parent) {
        this.context = context;
        view = LayoutInflater.from(context).inflate(R.layout.layout_rl_headview, parent, false);
    }

    @Override
    public View initHeaderView() {
        return view;
    }

    @Override
    public void pullingDown(View view, int translateY) {

    }

    @Override
    public void passThreshold(View view, int threshold) {
        view.findViewById(R.id.firstImage).setRotation(180);
        ((TextView) view.findViewById(R.id.textView)).setText("松开即可刷新");
    }

    @Override
    public void backToThreshold(View view, int threshold) {
        view.findViewById(R.id.firstImage).setRotation(0);
        ((TextView) view.findViewById(R.id.textView)).setText("下拉刷新");
    }

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

    @Override
    public void stopRefresh(View view) {
        //恢复headView
        view.findViewById(R.id.secondImage).clearAnimation();
        view.findViewById(R.id.firstImage).setVisibility(View.VISIBLE);
        view.findViewById(R.id.secondImage).setVisibility(View.GONE);
    }
}
