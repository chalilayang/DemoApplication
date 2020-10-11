package com.example.mi.renderscript;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public abstract class ShowResultTask extends AsyncTask<Object, Integer, String> {
    protected WeakReference<Context> mContext;

    public ShowResultTask(Context context) {
        mContext = new WeakReference<Context>(context.getApplicationContext());
    }

    @Override
    protected void onPostExecute(String string) {
        super.onPostExecute(string);
        if (mContext.get() != null) {
            Toast.makeText(mContext.get(), "结果是 " + string, Toast.LENGTH_SHORT).show();
        }
    }
}
