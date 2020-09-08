package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.Surface.OutOfResourcesException;
import android.widget.ProgressBar;

public class GLProgressBar extends ProgressBar {
	private static final String TAG = "GLProgressBar";
	ViewRenderer viewRenderer;

	public GLProgressBar(Context context) {
		super(context);
	}

	public void setViewRenderer(ViewRenderer renderer) {
		viewRenderer = renderer;
	}

//	@Override
//	protected void onDraw(Canvas canvas) {
//		Log.e(TAG, "onDraw: ", new Throwable());
//		if (viewRenderer != null && viewRenderer.isAvailable()) {
//			try {
//				final Canvas surfaceCanvas = viewRenderer.lockCanvas(false);
//				Log.i(TAG, "onDraw: isHardwareAccelerated " + surfaceCanvas.isHardwareAccelerated());
//				super.onDraw(surfaceCanvas);
//				viewRenderer.unlockCanvasAndPost(surfaceCanvas);
//			} catch (OutOfResourcesException e) {
//				e.printStackTrace();
//			}
//		}
//	}

	@Override
	public void draw(Canvas canvas) {
		if (viewRenderer != null && viewRenderer.isAvailable()) {
			try {
				invalidate();
				final Canvas surfaceCanvas = viewRenderer.lockCanvas(true);
				Log.i(TAG, "onDraw: isHardwareAccelerated " + surfaceCanvas.isHardwareAccelerated());
				surfaceCanvas.drawColor(Color.BLUE);
				super.draw(surfaceCanvas);
				viewRenderer.unlockCanvasAndPost(surfaceCanvas);
			} catch (OutOfResourcesException e) {
				e.printStackTrace();
			}
		} else {
			super.draw(canvas);
		}
	}
}
