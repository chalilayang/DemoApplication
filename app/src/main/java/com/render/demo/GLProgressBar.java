package com.render.demo;


import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.Surface.OutOfResourcesException;
import android.view.ViewGroup.LayoutParams;
import android.widget.ProgressBar;

public class GLProgressBar extends ProgressBar {
	ValueAnimator animator;
	Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	ViewRenderer viewRenderer;

	public GLProgressBar(Context context) {
		super(context);
		setLayoutParams(new LayoutParams(800, 800));
	}

	public void setViewRenderer(ViewRenderer renderer) {
		viewRenderer = renderer;
	}

	private int value = 100;
	@Override
	protected void onDraw(Canvas canvas) {
		if (viewRenderer != null && viewRenderer.isAvailable()) {
			try {
				final Canvas surfaceCanvas = viewRenderer.lockCanvas(false);
				super.onDraw(surfaceCanvas);
				if (animator == null) {
					animator = ValueAnimator.ofInt(100, 500).setDuration(10000);
					animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
						@Override
						public void onAnimationUpdate(ValueAnimator animation) {
							value = (int) animation.getAnimatedValue();
							invalidate();
						}
					});
					animator.start();
				}
				mPaint.setColor(Color.RED);
				mPaint.setTextSize(100);
				surfaceCanvas.drawText(""+value, 100, 100, mPaint);
				viewRenderer.unlockCanvasAndPost(surfaceCanvas);
			} catch (OutOfResourcesException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (animator != null) {
			animator.cancel();
		}
	}
}
