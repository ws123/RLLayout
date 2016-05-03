package com.carlos.rllayout;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.carlos.RLLayout;

public class MainActivity extends AppCompatActivity implements RLLayout.RefreshListener {
    private ListView listView;
    private RLLayout rlLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView = (ListView) findViewById(R.id.listView);
        rlLayout = (RLLayout) findViewById(R.id.rlLayout);
        listView.setAdapter(new MyAdapter());
//        rlLayout.setiHeaderView(new RLHeaderView(this, rlLayout));
//        rlLayout.setiFooterView(new RLFooterView(this, rlLayout));
        rlLayout.setRefreshListener(this);
    }

    @Override
    public void startRefresh() {
        //模拟加载数据,延迟5秒,然后就停止加载
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rlLayout.stopRefreshOrLoadMore();
            }
        }, 5000);
    }

    @Override
    public void startLoadMore() {
        //模拟加载数据,延迟5秒,然后就停止加载
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                rlLayout.stopRefreshOrLoadMore();
            }
        }, 5000);
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 400;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView textView = new TextView(MainActivity.this);
            textView.setText("这是第" + position + "行");
            return textView;
        }
    }
}
