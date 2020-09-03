package com.particles.android;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mi.demoapplication.R;
import com.example.mi.view.GlTextureView;
import com.render.demo.GLProgressBar;
import com.render.demo.ViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class GlRenderActivity extends AppCompatActivity {

    @BindView(R.id.button)
    Button button;
    @BindView(R.id.image)
    ImageView image;
    private FrameLayout root;
    private GlTextureView glTextureView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_render);
        ButterKnife.bind(this);
        root = findViewById(R.id.root);
        addView();
    }
    GLProgressBar glProgressBar;
    private void addView() {
        glProgressBar = new GLProgressBar(this);
        ViewRenderer renderer = new ViewRenderer(getApplicationContext());
        glTextureView = new GlTextureView(getApplicationContext());
        glTextureView.setEGLContextClientVersion(2);
        glTextureView.setRenderer(renderer);
        glTextureView.setLayoutParams(new FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT));
        root.addView(glTextureView);
        root.addView(glProgressBar);
        glProgressBar.setViewRenderer(renderer);
    }

    @OnClick(R.id.button)
    public void onViewClicked() {
        Bitmap bitmap = glTextureView.getBitmap(
                image.getWidth(),
                (int) (image.getWidth() * glTextureView.getHeight() * 1.0f / glTextureView.getWidth()));
        if (bitmap != null) {
            image.setImageBitmap(bitmap);
        }
    }
}