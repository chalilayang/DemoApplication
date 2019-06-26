package com.example.mi.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.TextureView;

import com.example.mi.demoapplication.R;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class WirelessChargeCircleDrawer implements AnimationView.AnimationDrawer {
    public static final int[] WIRELESS_CIRCLE_RES_ARRAY = {
            R.drawable.wireless_rapid_charge_0,
            R.drawable.wireless_rapid_charge_1,
            R.drawable.wireless_rapid_charge_2,
            R.drawable.wireless_rapid_charge_3,
            R.drawable.wireless_rapid_charge_4,
            R.drawable.wireless_rapid_charge_5,
            R.drawable.wireless_rapid_charge_6,
            R.drawable.wireless_rapid_charge_7,
            R.drawable.wireless_rapid_charge_8,
            R.drawable.wireless_rapid_charge_9,
            R.drawable.wireless_rapid_charge_10,
            R.drawable.wireless_rapid_charge_11,
            R.drawable.wireless_rapid_charge_12,
            R.drawable.wireless_rapid_charge_13,
            R.drawable.wireless_rapid_charge_14,
            R.drawable.wireless_rapid_charge_15,
            R.drawable.wireless_rapid_charge_16,
            R.drawable.wireless_rapid_charge_17,
            R.drawable.wireless_rapid_charge_18,
            R.drawable.wireless_rapid_charge_19,
            R.drawable.wireless_rapid_charge_20,
            R.drawable.wireless_rapid_charge_21,
            R.drawable.wireless_rapid_charge_22,
            R.drawable.wireless_rapid_charge_23
    };
    private static final int FPS = 36;
    private int mFrameInterval;
    private Context mContext;

    private DecodeTask mDecodeTask;
    private final Queue<BitmapInfo> mBitmapQueue = new ArrayBlockingQueue<>(2);
    //重复Bitmap内存，防止过度的GC操作
    private final Queue<Bitmap> mRecycleBitmapQueue = new ArrayBlockingQueue<Bitmap>(2, true);

    private HandlerThread mDecodeThread;
    private Handler mDecodeHandler;
    private final Object mHandlerLock = new Object();
    private long mLastFrameTime = -1;
    private Paint mPaint;
    private Matrix mMatrix;
    private BitmapInfo mDrawingBitmapInfo;

    WirelessChargeCircleDrawer(Context context) {
        mContext = context;
        mFrameInterval = 1000 / FPS;
        mPaint = new Paint();
        mMatrix = new Matrix();
    }

    @Override
    public void onAnimationDraw(TextureView textureView, long frameTime) {
        BitmapInfo bitmapInfo = mBitmapQueue.peek();
        if (bitmapInfo != null) {
            if (mLastFrameTime == -1) {
                if (mDrawingBitmapInfo != null) {
                    try {
                        mRecycleBitmapQueue.offer(mDrawingBitmapInfo.mBitmap);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                mDrawingBitmapInfo = bitmapInfo;
                mBitmapQueue.poll();
                mLastFrameTime = frameTime;
            } else {
                long elapse = frameTime - mLastFrameTime;
                if (elapse >= (mFrameInterval * 1000000)) {
                    if (mDrawingBitmapInfo != null) {
                        try {
                            mRecycleBitmapQueue.offer(mDrawingBitmapInfo.mBitmap);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    mDrawingBitmapInfo = bitmapInfo;
                    mBitmapQueue.poll();
                    mLastFrameTime = frameTime;
                    Log.i(TAG, "onAnimationDraw: " + mDrawingBitmapInfo.mPosition + " " + mRecycleBitmapQueue.size());
                    drawBitmap(textureView, mDrawingBitmapInfo.mBitmap, mPaint, mMatrix);
                }
            }
        }
    }

    private static final String TAG = "WirelessChargeCircleDra";

    private void drawBitmap(TextureView textureView, Bitmap bitmap, Paint paint, Matrix matrix) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        Canvas canvas = textureView.lockCanvas();
        if (canvas != null) {
            try {
                matrix.reset();
                int srcWidth = bitmap.getWidth();
                int dstWidth = canvas.getWidth();
                int srcHeight = bitmap.getHeight();
                int dstHeight = canvas.getHeight();
                float dx = Math.round((dstWidth - srcWidth) * 0.5f);
                float dy = Math.round((dstHeight - srcHeight) * 0.5f);
                float scale = Math.min(dstWidth * 1.0f / srcWidth, dstHeight * 1.0f / srcHeight);
                matrix.postTranslate(dx, dy);
                matrix.postScale(scale, scale, dstWidth / 2.0f, dstHeight / 2.0f);
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(bitmap, matrix, paint);
            } finally {
                textureView.unlockCanvasAndPost(canvas);
            }
        }

    }

    public void startAnimation() {
        mLastFrameTime = -1;
        mBitmapQueue.clear();
        prepareDecodeThread();
        mDecodeTask = new DecodeTask(WIRELESS_CIRCLE_RES_ARRAY, 0);
        mDecodeTask.setDecoding(true);
        mDecodeHandler.post(mDecodeTask);
    }

    private void prepareDecodeThread() {
        if (mDecodeThread == null) {
            mDecodeThread = new HandlerThread("charge_frame_animation");
            mDecodeThread.start();
        }
        synchronized (mHandlerLock) {
            if (mDecodeHandler == null) {
                mDecodeHandler = new Handler(mDecodeThread.getLooper());
            }
        }
    }

    @Override
    public void release() {
        if (mDecodeTask != null) {
            mDecodeTask.setDecoding(false);
            mDecodeTask = null;
        }
        synchronized (mHandlerLock) {
            if (mDecodeHandler != null) {
                mDecodeHandler.removeCallbacksAndMessages(null);
                mDecodeHandler = null;
            }
        }
        if (mDecodeThread != null) {
            mDecodeThread.quit();
            mDecodeThread = null;
        }
        mBitmapQueue.clear();
        setAnimationListener(null);
    }

    private class DecodeTask implements Runnable {
        private final int[] mAnimRes;
        private volatile int mCurrentPosition;
        private volatile boolean mDecoding = false;
        private DecodeTask(int[] animRes, int startPosition) {
            mAnimRes = animRes;
            mCurrentPosition = startPosition % animRes.length;
        }

        private void setDecoding(boolean d) {
            mDecoding = d;
        }

        private boolean shouldFinish() {
            return mAnimRes == null
                    || mAnimRes.length == 0
                    || !mDecoding;
        }

        @Override
        public void run() {
            if (shouldFinish()) {
                setDecoding(false);
                return;
            }
            int res = mAnimRes[mCurrentPosition];
            Bitmap bitmap = decodeBitmap(res);
            boolean success = false;
            try {
                success = mBitmapQueue.offer(new BitmapInfo(bitmap, mCurrentPosition));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (success) {
                mCurrentPosition++;
                if (mCurrentPosition >= mAnimRes.length) {
                    mCurrentPosition = 0;
                }
            }
            if (shouldFinish()) {
                setDecoding(false);
                return;
            }
            synchronized (mHandlerLock) {
                if (mDecodeHandler != null) {
                    mDecodeHandler.post(this);
                }
            }
        }
    }

    private Bitmap decodeBitmap(int res) {
        try {
            Bitmap inBitmap = null;
            //SurfaceView有双缓冲，所以要复用之前的第二个bitmap内存
            if (mRecycleBitmapQueue.size() >= 2) {
                inBitmap = mRecycleBitmapQueue.poll();
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inMutable = true;
            options.inSampleSize = 1;
            options.inBitmap = inBitmap;
            Log.i(TAG, "decodeBitmap: " + inBitmap + " " + mRecycleBitmapQueue.size());
            return BitmapFactory.decodeResource(mContext.getResources(), res, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private class BitmapInfo {
        private final Bitmap mBitmap;
        private final int mPosition;
        BitmapInfo(Bitmap b, int p) {
            mBitmap = b;
            mPosition = p;
        }
    }
}
