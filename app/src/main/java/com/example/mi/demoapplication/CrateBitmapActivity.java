package com.example.mi.demoapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.DRAWING_CACHE_QUALITY_HIGH;

public class CrateBitmapActivity extends AppCompatActivity {

    @BindView(R.id.image)
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crate_bitmap);
        ButterKnife.bind(this);
        createBitmap(this);
        image.setImageBitmap(createBitmap(this));
    }

    private Bitmap createBitmap(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.content_bottom_sheet, null);
        view.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.AT_MOST),
                View.MeasureSpec.makeMeasureSpec(1920, View.MeasureSpec.AT_MOST));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap result = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
        result.setDensity(getResources().getDisplayMetrics().densityDpi);
        Canvas canvas = new Canvas(result);
        view.draw(canvas);
        return result;
    }
}
