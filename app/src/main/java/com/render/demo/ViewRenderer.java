package com.render.demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Display;
import android.view.Surface;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES11Ext.GL_TEXTURE_EXTERNAL_OES;

public class ViewRenderer implements GLSurfaceView.Renderer {
	int glSurfaceTex;
	DirectDrawer mDirectDrawer;
	Context context;
	private IRendedView rendererView;
	private SurfaceTexture surfaceTexture = null;
	private Surface surface;
	private int mWidth;
	private int mHeight;
	
	public ViewRenderer(Context context, IRendedView rendererView, Display mDisplay){
		this.context = context;
		this.rendererView = rendererView;
		mWidth = mDisplay.getWidth();
		mHeight = mDisplay.getHeight();
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		synchronized (this) {
			surfaceTexture.updateTexImage();
		}
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);

		float[] mtx = new float[16];
		surfaceTexture.getTransformMatrix(mtx);
		mDirectDrawer.draw(mtx);
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		surface = null;
		surfaceTexture = null;
		glSurfaceTex = createSurfaceTexture();
		if (glSurfaceTex > 0) {
			surfaceTexture = new SurfaceTexture(glSurfaceTex);
			surfaceTexture.setDefaultBufferSize(mWidth, mHeight);
			surface = new Surface(surfaceTexture);
			rendererView.configSurface(surface);
			rendererView.configSurfaceTexture(surfaceTexture);
			mDirectDrawer = new DirectDrawer(glSurfaceTex);
		}
	}

	int createSurfaceTexture() {
		int[] textures = new int[1];
		GLES20.glGenTextures(1, textures, 0);
		glSurfaceTex = textures[0];
		if (glSurfaceTex > 0) {
			GLES20.glBindTexture(GL_TEXTURE_EXTERNAL_OES, glSurfaceTex);
			GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
			GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
			GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
			GLES20.glTexParameteri(GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
		}
		return glSurfaceTex;
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {}
}