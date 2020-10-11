package com.example.mi.renderscript;

import android.content.Context;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;

import com.example.mi.ScriptC_compute;

import java.util.Arrays;

public class ComputeTask extends ShowResultTask {
    private static final String TAG = "ComputeTask";
    public ComputeTask(Context context) {
        super(context);
    }

    @Override
    protected String doInBackground(Object... objects) {
        if (mContext.get() != null) {
            float[] data = {
                    1.1f, 2.2f, 3.3f, 4.4f,
                    5.5f, 6.6f, 7.7f, 8.8f,
                    9.9f, 10.1f, 11.11f, 12.12f,
                    13.13f, 14.14f, 15.15f, 16.16f,
                    17.17f, 18.18f, 19.19f, 20.2f,
                    21.21f, 22.22f, 23.23f, 24.24f};
            final int WIDTH = 6;
            RenderScript RS = RenderScript.create(mContext.get());
            ScriptC_compute script = new ScriptC_compute(RS);
            Allocation inputAllocation = Allocation.createTyped(RS, new Type.Builder(RS, Element.F32_4(RS)).setX(WIDTH).create());
            inputAllocation.copy1DRangeFrom(0, WIDTH, data); // 直接输入基础类型数组，框架进行封装成float4

            Allocation outputAllocation = Allocation.createTyped(RS, new Type.Builder(RS, Element.F32_2(RS)).setX(WIDTH).create());

            Log.d(TAG, "before: " + script.get_param());
            script.set_param(5);
            script.invoke_compute(inputAllocation, outputAllocation, 1, 3, 5, -11);
            Log.d(TAG, "after: " + script.get_param());

            float[] result = new float[WIDTH * 2];
            outputAllocation.copyTo(result);
            RS.destroy();

            String resultS = Arrays.toString(result);
            Log.i(TAG, "doInBackground: " + resultS);
            return "single source 结果 " + resultS;
        }
        return null;
    }
}

