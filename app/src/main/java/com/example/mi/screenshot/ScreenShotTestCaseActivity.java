package com.example.mi.screenshot;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mi.demoapplication.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScreenShotTestCaseActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_shot_test_case);
        mRecyclerView = findViewById(R.id.list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext(), RecyclerView.VERTICAL, false));
        mRecyclerView.setAdapter(new Adapter());
    }

    private class Adapter extends RecyclerView.Adapter<VH> {

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            TextView tv = new TextView(parent.getContext());
            tv.setTextSize(60);
            tv.setTextColor(Color.RED);
            tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            return new VH(tv);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            holder.title.setText("" + position + "" + position + "" + position + "" + position);
            holder.title.setBackgroundDrawable(getDrawabledd(dd.get(position % dd.size())));
        }

        @Override
        public int getItemCount() {
            return 23;
        }
    }

    public static class VH extends RecyclerView.ViewHolder{
        public final TextView title;
        public VH(View v) {
            super(v);
            title = (TextView) v;
        }
    }

    private static List<Integer> dd = new ArrayList<>();
    {
        dd.add(R.drawable.aod_bg_tree);
        dd.add(R.drawable.aod_bg_cactus);
        dd.add(R.drawable.aod_bg_ghost);
        dd.add(R.drawable.aod_bg_moonlight);
        dd.add(R.drawable.aod_bg_paint);
        dd.add(R.drawable.aod_bg_shadow);
        dd.add(R.drawable.aod_bg_spaceman);
        dd.add(R.drawable.aod_bg_spirit);
        dd.add(R.drawable.aod_bg_succulent);
    }

    public static Map<Integer, Drawable> map = new HashMap<>();

    public Drawable getDrawabledd(int id) {
        if (map.containsKey(id)) {
            return map.get(id);
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 3;
//        options.inPreferredConfig = Bitmap.Config.HARDWARE;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id, options);
        Drawable drawable = new BitmapDrawable(bitmap);
        map.put(id, drawable);
        return drawable;
    }
}