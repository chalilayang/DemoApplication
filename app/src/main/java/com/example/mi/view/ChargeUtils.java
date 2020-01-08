package com.example.mi.view;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ChargeUtils {

    private static final String TAG = "ChargeUtils";

    public static final int NONE = -1;
    public static final int WIRELESS = 10;
    public static final int NORMAL = 11;
    @IntDef({NONE, NORMAL, WIRELESS})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CHARGE_TYPE {}
}
