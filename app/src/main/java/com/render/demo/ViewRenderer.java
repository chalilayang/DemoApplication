package com.render.demo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewRenderer implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {
	private static final String TAG = "ViewRenderer";
	private static final float[] sSquareCoordinate = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f,
	};

	private static final float[] mTextureCoordinate = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,
	};

	private int glSurfaceTex;
	private final Context context;
	private SurfaceTexture surfaceTexture;
	private Surface surface;

	private ShaderProgram mProgram;

	private final VertexArray mPositionArray;
	private final VertexArray mTextureCoordinateArray;

	private volatile boolean needRedraw;

	public ViewRenderer(Context context) {
		this.context = context;
		mPositionArray = new VertexArray(sSquareCoordinate);
		mTextureCoordinateArray = new VertexArray(mTextureCoordinate);
		needRedraw = false;
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		if (needRedraw) {
			surfaceTexture.updateTexImage();
			needRedraw = false;
		}
		mProgram.useProgram();
		mProgram.setTextureId(glSurfaceTex);
		mPositionArray.setVertexAttribPointer(
				0, mProgram.getPositionLocation(), 2, 0);
		mTextureCoordinateArray.setVertexAttribPointer(
				0, mProgram.getTextureCoordinateLocation(), 2, 0);
		mProgram.draw();
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		surface = null;
		if (surfaceTexture != null) {
			surfaceTexture.release();
		}
		int[] texture = new int[1];
		TextureHelper.createExternalSurfaceTexture(texture, width, height);
		glSurfaceTex = texture[0];
		if (glSurfaceTex > 0) {
			surfaceTexture = new SurfaceTexture(glSurfaceTex);
			surfaceTexture.setOnFrameAvailableListener(this);
			surfaceTexture.setDefaultBufferSize(width, height);
			surface = new Surface(surfaceTexture);
			mProgram = new ShaderProgram(context);
		}
	}

	public boolean isAvailable() {
		return surface != null;
	}

	public Canvas lockCanvas(boolean hardware) {
		if (surface == null) {
			return null;
		}
		Canvas canvasDrawing
				= hardware ? surface.lockHardwareCanvas() : surface.lockCanvas(null);
		canvasDrawing.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		return canvasDrawing;
	}

	public void unlockCanvasAndPost(Canvas canvas) {
		surface.unlockCanvasAndPost(canvas);
	}

	@Override
	public void onFrameAvailable(SurfaceTexture surfaceTexture) {
		needRedraw = true;
	}
}