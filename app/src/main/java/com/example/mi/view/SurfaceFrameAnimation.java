package com.example.mi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by daxiong on 2018/4/20.
 * 使用SurfaceView来做帧动画，因每一帧图像比较小，并且
 * 帧率要求不高，所以只使用一个线程来同时解码和绘制
 */

public class SurfaceFrameAnimation {

    private static final String TAG = "SurfaceFrameAnimation";

    public static final int RES_DRAW_TRANSPARENT = 0;

    public static final int MODE_ONCE = 1;
    public static final int MODE_REPEAT = 2;

    private final SurfaceView mSurfaceView;
    private final SurfaceHolder mSurfaceHolder;
    private final SurfaceHolder.Callback mCallBack;
    private final Context mContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final Handler mDrawHandler;
    private final HandlerThread mDrawThread;

    private volatile int mFrameInterval = 32;
    private volatile boolean mSupportInBitmap = true;
    private volatile int mMode = MODE_ONCE;
    private boolean mLastDrawAnim = false;

    private DrawRunnable mDrawRunnable;

    //重复Bitmap内存，防止过度的GC操作
    private final Queue<Bitmap> mRecycleBitmapQueue = new ArrayBlockingQueue<Bitmap>(2, true);

    public SurfaceFrameAnimation(SurfaceView surfaceView, SurfaceHolder.Callback callback) {
        mSurfaceView = surfaceView;
        mSurfaceHolder = surfaceView.getHolder();
        mCallBack = callback;
        mSurfaceHolder.setFormat(PixelFormat.TRANSLUCENT);
        mSurfaceView.setZOrderOnTop(true);
        mSurfaceHolder.addCallback(mCallBack);
        mContext = surfaceView.getContext();
        mDrawThread = new HandlerThread("FrameAnimation Draw Thread");
        mDrawThread.start();
        mDrawHandler = new Handler(mDrawThread.getLooper());
    }

    public void setFrameInterval(int frameInterval) {
        if (frameInterval < 0) {
            throw new UnsupportedOperationException("frameInterval < 0");
        }
        mFrameInterval = frameInterval;
    }

    /**
     * //是否复用bitmap内存，要求所有帧的大小和像素位数一致
     * @param supportInBitmap
     */
    public void setSupportInBitmap(boolean supportInBitmap) {
        mSupportInBitmap = supportInBitmap;
    }

    public void setMode(int mode) {
        if (mode != MODE_ONCE && mode != MODE_REPEAT) {
            throw new UnsupportedOperationException("wrong mode: " + mode);
        }
        mMode = mode;
    }

    public void startAnimation(int[] res, FrameAnimationListener l) {
        startAnimation(res, 0, RES_DRAW_TRANSPARENT, 0, l, null);
    }

    public void startAnimation(int[] res, int startPosition, int backgroundRes, int backgroundFrame
            , FrameAnimationListener l, CustomerDrawBitmap customerDrawBitmap) {
        mLastDrawAnim = false;
        stopAnimation();
        mDrawRunnable = new DrawRunnable(res, startPosition, backgroundRes, backgroundFrame, l, customerDrawBitmap);
        mDrawHandler.post(mDrawRunnable);
    }

    public void stopAnimation() {
        if (mDrawRunnable != null) {
            mDrawHandler.removeCallbacks(mDrawRunnable);
            if (mDrawRunnable.getDrawing()) {
                mDrawRunnable.stopDraw();
            }
        }
        mDrawRunnable = null;
    }

    public boolean isAnimating() {
        if (mDrawRunnable != null && mDrawRunnable.getDrawing()) {
            return true;
        } else {
            return false;
        }
    }

    public int getCurrentPosition() {
        if (isAnimating()) {
            return mDrawRunnable.getCurrentPosition();
        }
        return 0;
    }

    public void clean() {
        stopAnimation();
        mDrawHandler.post(new Runnable() {
            @Override
            public void run() {
                clearSurface();
            }
        });
    }

    public interface FrameAnimationListener {
        //动画开始
        void onStart();
        //动画中断
        void onInterrupt();
        //动画结束，结束和中断只会收到这两个其中一个，并且循环动画只能收到中断
        void onFinish();
    }

    public interface CustomerDrawBitmap {
        void drawBitmap(Canvas canvas, Bitmap bitmap, Matrix matrix);
    }

    private Matrix configureDrawMatrix(Bitmap bitmap, float scale) {
        Matrix matrix = new Matrix();
        final int srcWidth = bitmap.getWidth();
        final int dstWidth = mSurfaceView.getWidth();
        final int srcHeight = bitmap.getHeight();
        final int dstHeight = mSurfaceView.getHeight();
        final float dx = Math.round((dstWidth - srcWidth * scale) * 0.5f);
        final float dy = Math.round((dstHeight - srcHeight * scale) * 0.5f);
        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        return matrix;
    }

    private void drawBitmap(Bitmap bitmap, Bitmap background, float scale, CustomerDrawBitmap customer) {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null || bitmap == null) {
            Log.i(TAG, "drawBitmap: bitmap or canvas is null");
            return;
        }
        try {
            Matrix matrix = configureDrawMatrix(bitmap, scale);
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            if (background != null) {
                Matrix backgroundMatrix = configureDrawMatrix(background, scale);
                canvas.drawBitmap(background, backgroundMatrix, null);
            }
            if (customer == null) {
                canvas.drawBitmap(bitmap, matrix, null);
            } else {
                customer.drawBitmap(canvas, bitmap, matrix);
            }
        } finally {
            unlockCanvasAndPostSafely(canvas);
        }
    }

    private void clearSurface() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }
        try {
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        } finally {
            unlockCanvasAndPostSafely(canvas);
        }
    }

    private void unlockCanvasAndPostSafely(Canvas canvas) {
        Surface surface = mSurfaceHolder.getSurface();
        if (surface != null && surface.isValid()) {
            mSurfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    private Bitmap decodeBitmap(int res) {
        Bitmap inBitmap = null;
        //SurfaceView有双缓冲，所以要复用之前的第二个bitmap内存
        if (mSupportInBitmap && mRecycleBitmapQueue.size() >= 2) {
            inBitmap = mRecycleBitmapQueue.poll();
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inSampleSize = 1;
        options.inBitmap = inBitmap;
        try {
            return BitmapFactory.decodeResource(mContext.getResources(), res, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private float paintALpha = 1.0f;
    public void setAlpha(float value) {
        paintALpha = value;
    }

    private class DrawRunnable implements Runnable {

        private final int[] mAnimRes;
        //背景资源
        private final int mBackgroundRes;
        //需要画背景资源的帧数
        private final int mBackgroundFrame;
        private final FrameAnimationListener mFrameAnimationListener;
        private final CustomerDrawBitmap mCustomerDrawBitmap;

        private volatile int mCurrentPosition;
        private boolean mDrawing = false;

        private DrawRunnable(int[] animRes, int startPosition, int backgroundRes, int backgroundFrame
                , FrameAnimationListener l, CustomerDrawBitmap customerDrawBitmap) {
            mAnimRes = animRes;
            mCurrentPosition = startPosition % animRes.length;
            mBackgroundRes = backgroundRes;
            mBackgroundFrame = backgroundFrame;
            mFrameAnimationListener = l;
            mCustomerDrawBitmap = customerDrawBitmap;
        }

        public synchronized void setDrawing(boolean d) {
            mDrawing = d;
        }

        public synchronized boolean getDrawing() {
            return mDrawing;
        }

        public int getCurrentPosition() {
            return mCurrentPosition;
        }

        public void stopDraw() {
            setDrawing(false);
            mDrawThread.interrupt();
            mRecycleBitmapQueue.clear();
        }

        @Override
        public void run() {
            setDrawing(true);
            notifyStart();
            if (mAnimRes == null || mAnimRes.length  == 0) {
                notifyFinish();
                setDrawing(false);
                return;
            }

            boolean interrupt = true;
            mRecycleBitmapQueue.clear();
            final Bitmap background = mBackgroundRes == RES_DRAW_TRANSPARENT ? null : decodeBitmap(mBackgroundRes);
            int count = 0;
            while (getDrawing()) {
                long now = System.currentTimeMillis();
                int res = mAnimRes[mCurrentPosition];
                if (res == RES_DRAW_TRANSPARENT || paintALpha < 0.01f) {
                    clearSurface();
                } else {
                    Bitmap bitmap = decodeBitmap(res);
                    if (bitmap == null) {
                        stopDraw();
                        break;
                    }
                    drawBitmap(bitmap, count < mBackgroundFrame || mBackgroundFrame <= 0 ? background : null
                            , 1, mCustomerDrawBitmap);
                    mRecycleBitmapQueue.offer(bitmap);
                }
                mCurrentPosition++;
                count++;
                if (mCurrentPosition == mAnimRes.length) {
                    if (mMode == MODE_ONCE) {
                        interrupt = false;
                        mRecycleBitmapQueue.clear();
                        break;
                    } else if (mMode == MODE_REPEAT) {
                        mCurrentPosition = 0;
                    }
                }
                try {
                    //控制两帧之间的间隔
                    long spend = System.currentTimeMillis() - now;
                    if (mFrameInterval - spend > 0) {
                        Thread.sleep(mFrameInterval - spend);
                    }
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }
            setDrawing(false);
            if (interrupt) {
                notifyInterrupt();
            } else {
                notifyFinish();
            }
        }

        private void notifyStart() {
            if (mFrameAnimationListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFrameAnimationListener.onStart();
                    }
                });
            }
        }

        private void notifyInterrupt() {
            if (mFrameAnimationListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFrameAnimationListener.onInterrupt();
                    }
                });
            }
        }

        private void notifyFinish() {
            if (mFrameAnimationListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mFrameAnimationListener.onFinish();
                    }
                });
            }
        }
    }
}