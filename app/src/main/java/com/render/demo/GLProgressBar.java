package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.view.Surface.OutOfResourcesException;
import android.widget.ProgressBar;

public class GLProgressBar extends ProgressBar {
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
				surfaceCanvas = viewRenderer.lockCanvas(true);
				super.draw(surfaceCanvas);
			} catch (OutOfResourcesException e) {
				e.printStackTrace();
			} finally {
				if (surfaceCanvas != null) {
					viewRenderer.unlockCanvasAndPost(surfaceCanvas);
				}
			}
		} else {
			invalidate();
		}
	}
}
