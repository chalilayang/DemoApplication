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
}
