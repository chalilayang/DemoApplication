package com.example.mi.view;

import android.view.animation.BaseInterpolator;

public class WirelessRapidChargeCarAniInterpolator extends BaseInterpolator {

    private static float POINT_1 = 6f / 15;
    private static float POINT_2 = 13f / 15;
    @Override
    public float getInterpolation(float input) {
        if (input <= 0) {
            return 0;
        } else if (input <= POINT_1) {
            return (float)(Math.cos((input / POINT_1 + 1) * Math.PI) / 2.0f) + 0.5f;
        } else if (input <= POINT_2) {
            return 1f;
        } else if (input < 1) {
            return (float)(Math.cos(((input - POINT_2) / (1 - POINT_2)) * Math.PI) / 2.0f) + 0.5f;
        } else {
            return 0;
        }
    }
}
