package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.mi.view.GlTextureView;

import java.util.Set;

import de.robv.android.xposed.DexposedBridge;
import de.robv.android.xposed.XC_MethodHook;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout {
    private static final String TAG = "RenderFrameLayout";
    private GlTextureView mRenderView;
    private ViewRenderer mViewRenderer;
    GLProgressBar glProgressBar;

    public RenderFrameLayout(Context context) {
        this(context, null);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        mViewRenderer = new ViewRenderer(context);
        mRenderView = new GlTextureView(context);
        mRenderView.setEGLContextClientVersion(2);
        mRenderView.setRenderer(mViewRenderer);
        addView(mRenderView, new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        glProgressBar = new GLProgressBar(context);
        glProgressBar.setViewRenderer(mViewRenderer);
        addView(glProgressBar);
    }

    public static void hook() {
        final Set<Class<?>> set = new ArraySet<>();
//        set.add(RenderFrameLayout.class);
        set.add(GLProgressBar.class);
        class ViewMethodHook extends XC_MethodHook{
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                if (set.contains(param.thisObject.getClass())) {
                    Object renderNode = DebugUtils.getRenderNode((View)param.thisObject);
                    boolean displayListValid = DebugUtils.displayListValid(renderNode);
                    boolean recreateDisplayList = DebugUtils.getRecreateDisplayList((View)param.thisObject);
                    int privateFlag = DebugUtils.getPrivateFlag((View)param.thisObject);
                    boolean flag = (privateFlag & 0x00008000) == 0;
                    Log.i(TAG, "beforeHookedMethod: displayListValid " + displayListValid
                            + " recreateDisplayList " + recreateDisplayList + " flag " + flag + " " + param.method.getName());
                }
            }
        }
        DexposedBridge.findAndHookMethod(View.class,
                "updateDisplayListIfDirty", new ViewMethodHook());
        DexposedBridge.findAndHookMethod(View.class,
                "draw", Canvas.class, new ViewMethodHook());
        class ConstructMethodHook extends XC_MethodHook{
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                if (DebugUtils.sRenderNodeClazz == null) {
                    Object renderNode = null;
                    try {
                        renderNode = DebugUtils.getRenderNode((View)param.thisObject);
                    } catch (Exception e) {
                        Log.e(TAG, "afterHookedMethod: ", e);
                        e.printStackTrace();
                    }
                    if (renderNode != null) {
                        DebugUtils.sRenderNodeClazz = renderNode.getClass();
                    }
                } else {
                    DexposedBridge.unhookMethod(param.method, this);
                }
            }
        }
        DexposedBridge.hookAllConstructors(View.class, new ConstructMethodHook());
    }
}
