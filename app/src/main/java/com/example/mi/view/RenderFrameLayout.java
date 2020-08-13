package com.example.mi.view;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.lang.reflect.Field;

/**
 * Created by chalilayang on 20-8-11 下午9:41.
 **/
public class RenderFrameLayout extends FrameLayout implements TextureView.SurfaceTextureListener {
    private ProxyRenderView mRenderView;
    private View mTargetView;
    private Surface mSurface;
    public RenderFrameLayout(Context context) {
        this(context, null);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RenderFrameLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(true);
        mRenderView = new ProxyRenderView(context, attrs, defStyle);
        super.addView(mRenderView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mRenderView.setSurfaceTextureListener(this);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (mTargetView != null || child == null) {
            return;
        }
        mTargetView = child;
        mTargetView.setLayoutParams(params);
        super.addView(child, index, params);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mSurface = new Surface(surface);
        if (mTargetView != null && mTargetView.isAttachedToWindow()) {
            setThreadedRenderSurface(mTargetView, mSurface);
        }
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mSurface = null;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {}

    private static void setThreadedRenderSurface(View targetView, Surface surface) {
        try {
            Object attachInfo = getObjectField(targetView, "mAttachInfo");
            if (attachInfo != null) {
                Object threadedRender = getObjectField(
                        attachInfo,
                        Class.forName("android.view.View.AttachInfo"), "mThreadedRenderer");
                if (threadedRender != null) {

                }
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Object getObjectField(Object target, Class<?> clazz, String field) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Field declaredField = clazz.getDeclaredField(field);
        declaredField.setAccessible(true);
        return declaredField.get(target);
    }

    public static void setObjectField(
            Object target, String field, Object value) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Class<? extends Object> clazz = target.getClass();
        Field declaredField = clazz.getDeclaredField(field);
        declaredField.setAccessible(true);
        declaredField.set(target, value);
    }

    public static Object getObjectField(Object target, String field) throws NoSuchFieldException, SecurityException,
            IllegalArgumentException, IllegalAccessException {
        Class<? extends Object> clazz = target.getClass();
        Field declaredField = clazz.getDeclaredField(field);
        declaredField.setAccessible(true);
        return declaredField.get(target);
    }
}
