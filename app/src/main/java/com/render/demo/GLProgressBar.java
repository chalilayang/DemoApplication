package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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

	@Override
	public void draw(Canvas canvas) {
		if (viewRenderer != null && viewRenderer.isAvailable()) {
			Canvas surfaceCanvas = null;
			try {
				invalidate();
				surfaceCanvas = viewRenderer.lockCanvas(true);
				surfaceCanvas.drawColor(Color.BLUE);
				super.draw(surfaceCanvas);
			} catch (OutOfResourcesException e) {
				e.printStackTrace();
			} finally {
				if (surfaceCanvas != null) {
					viewRenderer.unlockCanvasAndPost(surfaceCanvas);
				}
			}
		} else {
			super.draw(canvas);
		}
	}
}
