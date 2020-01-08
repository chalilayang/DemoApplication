package com.example.mi.demoapplication;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AodTestActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.button2)
    Button button2;
    @BindView(R.id.button3)
    Button button3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aod_test);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.button, R.id.button2, R.id.button3})
    public void onViewClicked(View view) {
        int type = 1;
        switch (view.getId()) {
            case R.id.button:
                break;
            case R.id.button2:
                type = 2;
                break;
            case R.id.button3:
                type = 3;
                break;
        }
        Intent intent = new Intent(this, TextDrawActivity.class);
        intent.putExtra("type", type);
        startActivity(intent);
    }
}
