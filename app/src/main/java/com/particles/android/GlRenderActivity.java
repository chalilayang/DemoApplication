package com.particles.android;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mi.demoapplication.R;
import com.render.demo.RenderFrameLayout;

import butterknife.ButterKnife;

public class GlRenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        RenderFrameLayout.hook();
        setContentView(R.layout.activity_gl_render);
        ButterKnife.bind(this);
    }
}