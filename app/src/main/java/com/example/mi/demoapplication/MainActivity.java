package com.example.mi.demoapplication;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.mi.view.GTChargeAniView;
import com.example.mi.view.RapidChargeView;
import com.example.mi.view.WirelessRapidChargeView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    RapidChargeView rapidChargeView;
    WirelessRapidChargeView countView;

    @BindView(R.id.edit_query)
    EditText editQuery;
    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;
    @BindView(R.id.gt_charge_view)
    GTChargeAniView gtChargeAniView;
    private int count;
    private Handler countHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            count++;
            if (count > 100) {
                count = 0;
            }
            countView.setProgress(count);
            rapidChargeView.setProgress(count);
            this.sendEmptyMessageDelayed(0, 100);
        }
    };

    private boolean rapid = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        countView = new WirelessRapidChargeView(this);
        countView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countView.setChargeState(!rapid, !rapid);
                rapid = !rapid;
            }
        });
        rapidChargeView = new RapidChargeView(this);
        rapidChargeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rapidChargeView.setChargeState(!rapid, !rapid);
                rapid = !rapid;
            }
        });
        countHandler.sendEmptyMessage(0);
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.button:
                if (!countView.isAttachedToWindow()) {
                    countView.addToWindow("ddd");
                } else {
                    countView.zoomLarge();
                }
                break;
            case R.id.button2:
//                startActivity(new Intent(this, TextDrawActivity.class));
                if (!rapidChargeView.isAttachedToWindow()) {
                    rapidChargeView.addToWindow("eee");
                }
                rapidChargeView.zoomLarge();
                break;
            case R.id.button3:
//                startActivity(new Intent(this, TextDrawActivity.class));
                gtChargeAniView.animationToShow();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        countHandler.removeCallbacksAndMessages(null);
    }
}
