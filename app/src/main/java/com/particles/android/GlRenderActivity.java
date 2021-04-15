package com.particles.android;

import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mi.demoapplication.R;
import com.render.demo.RenderFrameLayout;

import butterknife.ButterKnife;

public class GlRenderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gl_render);
        ButterKnife.bind(this);
        ImageView imageView = findViewById(R.id.image);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Drawable drawable=imageView.getDrawable();
                if(drawable instanceof Animatable){
                    ((Animatable)drawable).start();
                }
            }
        });
    }
}