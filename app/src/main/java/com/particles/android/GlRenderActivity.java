package com.particles.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mi.demoapplication.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GlRenderActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.image)
    ImageView image;
    private FrameLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_render);
        ButterKnife.bind(this);
        root = findViewById(R.id.root);
    }

    private void addView2() {
        ProgressBar glProgressBar = new ProgressBar(this);
        root.addView(glProgressBar);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
//        if (glTextureView == null || glTextureView.getWidth() == 0) {
//            return;
//        }
//        Bitmap bitmap = glTextureView.getBitmap(
//                image.getWidth(),
//                (int) (image.getWidth() * glTextureView.getHeight() * 1.0f / glTextureView.getWidth()));
//        if (bitmap != null) {
//            image.setImageBitmap(bitmap);
//        }
    }
}