package com.render.demo;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.Display;
import android.view.Surface;

import com.particles.android.data.VertexArray;
import com.particles.android.programs.ShaderProgram;
import com.particles.android.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ViewRenderer implements GLSurfaceView.Renderer {
	private static float sSquareCoordinate[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f,
	};

	private float mTextureCoordinate[] = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f,
	};

	int glSurfaceTex;
	Context context;
	private IRendedView rendererView;
	private SurfaceTexture surfaceTexture = null;
	private Surface surface;
	private int mWidth;
	private int mHeight;

	private ShaderProgram mProgram;

	private VertexArray mPositionArray;
	private VertexArray mTextureCoordinateArray;

	public ViewRenderer(Context context, IRendedView rendererView, Display mDisplay) {
		this.context = context;
		this.rendererView = rendererView;
		mWidth = mDisplay.getWidth();
		mHeight = mDisplay.getHeight();
		mPositionArray = new VertexArray(sSquareCoordinate);
		mTextureCoordinateArray = new VertexArray(mTextureCoordinate);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
		surfaceTexture.updateTexImage();
		mProgram.useProgram();
		mProgram.setTextureId(glSurfaceTex);
		mPositionArray.setVertexAttribPointer(
				0, mProgram.getPositionLocation(), 2, 0);
		mTextureCoordinateArray.setVertexAttribPointer(
				0, mProgram.getTextureCoordinateLocation(), 2, 0);
		mProgram.draw();
	}
	
	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		surface = null;
		surfaceTexture = null;
		int[] texture = new int[1];
		TextureHelper.createExternalSurfaceTexture(texture, mWidth, mHeight);
		glSurfaceTex = texture[0];
		if (glSurfaceTex > 0) {
			surfaceTexture = new SurfaceTexture(glSurfaceTex);
			surfaceTexture.setDefaultBufferSize(mWidth, mHeight);
			surface = new Surface(surfaceTexture);
			rendererView.configSurface(surface);
			rendererView.configSurfaceTexture(surfaceTexture);
			mProgram = new ShaderProgram(context);
		}
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {}
}