package com.particles.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mi.demoapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class GlRenderActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.image)
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_render);
        ButterKnife.bind(this);
    }
}