package com.render.demo;


import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class GLProgressBar extends ProgressBar implements IRendedView {
	private Surface mSurface;
	private SurfaceTexture mSurfaceTexture;
	@Override
	public void configSurface(Surface surface) {
		this.mSurface = surface;
	}

	@Override
	public void configSurfaceTexture(SurfaceTexture surfaceTexture) {
		this.mSurfaceTexture = surfaceTexture;
	}

	public GLProgressBar(Context context) {
		super(context);
		setLayoutParams(new LayoutParams(800, 800));
	}

	Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	@Override
	protected void onDraw(Canvas canvas) {
		if (mSurface != null) {
			try {
				final Canvas surfaceCanvas = mSurface.lockCanvas(null);
				super.onDraw(surfaceCanvas);
				mPaint.setColor(Color.RED);
				canvas.drawRect(0, 0, 50, 50, mPaint);
				mSurface.unlockCanvasAndPost(surfaceCanvas);
			} catch (OutOfResourcesException e) {
				e.printStackTrace();
			}
		}

		if (mSurface != null) {
			mSurface.release();
			mSurface = null;
			mSurface = new Surface(mSurfaceTexture);
		}
	}
}
