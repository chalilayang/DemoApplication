package com.example.mi.demoapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BottomSheetActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view)
    GridView recyclerView;
    @BindView(R.id.bottom_sheet)
    RelativeLayout bottomSheet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_sheet);
        ButterKnife.bind(this);
        recyclerView.setNumColumns(3);
        recyclerView.setNestedScrollingEnabled(true);
        recyclerView.setAdapter(new ImageAdapter());
    }

    class ImageAdapter extends BaseAdapter {

        public int getCount() {
            return 30;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = new TextView(BottomSheetActivity.this);
                int size = 200;
                ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(size, size);
                convertView.setLayoutParams(lp);
                ((TextView) convertView).setGravity(Gravity.CENTER);
            }
            ((TextView) convertView).setText(String.valueOf(position));
            return convertView;
        }
    }
}
