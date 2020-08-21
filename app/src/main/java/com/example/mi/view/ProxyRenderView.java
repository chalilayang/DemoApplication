package com.example.mi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.TextureView;

/**
 * Created by chalilayang on 20-8-11 下午9:35.
 **/
public class ProxyRenderView extends TextureView {

    public ProxyRenderView(Context context) {
        this(context, null);
    }

    public ProxyRenderView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProxyRenderView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOpaque(false);
    }
}
