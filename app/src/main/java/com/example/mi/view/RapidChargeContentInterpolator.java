package com.example.mi.view;

import android.view.animation.BaseInterpolator;

public class RapidChargeContentInterpolator extends BaseInterpolator {
    private final float mTension = 2.0f;
    private static float POINT_1 = 1f / 8; // 最大点
    private static float POINT_2 = 0.2f; // 恢复常态的点
    private static float POINT_3 = 65f / 80; // 开始缩小
    @Override
    public float getInterpolation(float input) {
        if (input <= 0) {
            return 0;
        } else if (input <= POINT_2) {
            float t = input / POINT_2 - 1.0f;
            return t * t * ((mTension + 1) * t + mTension) + 1.0f;
        } else if (input <= POINT_3) {
            return 1f;
        } else if (input < 1) {
            return (float)(Math.cos(((input - POINT_2) / (1 - POINT_2)) * Math.PI) / 2.0f) + 0.5f;
        } else {
            return 0;
        }
    }
}
