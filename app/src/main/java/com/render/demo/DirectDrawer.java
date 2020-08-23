package com.render.demo;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.example.mi.demoapplication.R;
import com.particles.android.util.ShaderHelper;
import com.particles.android.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class DirectDrawer {

	private FloatBuffer vertexBuffer, textureVerticesBuffer;
	private final int mProgram;
	private int mPositionHandle;
	private int mTextureCoordHandle;
	private static final int COORDS_PER_VERTEX = 2;
	private final int vertexStride = COORDS_PER_VERTEX * 4;

	static float squareCoords[] = {
			-1.0f, -1.0f,
			1.0f, -1.0f,
			-1.0f, 1.0f,
			1.0f, 1.0f, };

	static float textureVertices[] = {
			0.0f, 1.0f,
			1.0f, 1.0f,
			0.0f, 0.0f,
			1.0f, 0.0f, };

	private int texture;

	public DirectDrawer(int texture, Context context) {
		this.texture = texture;
		// initialize vertex byte buffer for shape coordinates
		ByteBuffer bb = ByteBuffer.allocateDirect(squareCoords.length * 4);
		bb.order(ByteOrder.nativeOrder());
		vertexBuffer = bb.asFloatBuffer();
		vertexBuffer.put(squareCoords);
		vertexBuffer.position(0);

		ByteBuffer bb2 = ByteBuffer.allocateDirect(textureVertices.length * 4);
		bb2.order(ByteOrder.nativeOrder());
		textureVerticesBuffer = bb2.asFloatBuffer();
		textureVerticesBuffer.put(textureVertices);
		textureVerticesBuffer.position(0);

		mProgram = ShaderHelper.buildProgram(
				TextResourceReader.readTextFileFromResource(context, R.raw.vs),
				TextResourceReader.readTextFileFromResource(context, R.raw.fs));
	}

	public void draw(float[] mtx) {
		GLES20.glUseProgram(mProgram);

		GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
		GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, texture);

		// get handle to vertex shader's vPosition member
		mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

		// Enable a handle to the triangle vertices
		GLES20.glEnableVertexAttribArray(mPositionHandle);

		// Prepare the <insert shape here> coordinate data
		GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, vertexBuffer);

		mTextureCoordHandle = GLES20.glGetAttribLocation(mProgram,
				"inputTextureCoordinate");
		GLES20.glEnableVertexAttribArray(mTextureCoordHandle);

		GLES20.glVertexAttribPointer(mTextureCoordHandle, COORDS_PER_VERTEX,
				GLES20.GL_FLOAT, false, vertexStride, textureVerticesBuffer);

		GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

		// Disable vertex array
		GLES20.glDisableVertexAttribArray(mPositionHandle);
		GLES20.glDisableVertexAttribArray(mTextureCoordHandle);
	}

	private int loadShader(int type, String shaderCode) {

		int shader = GLES20.glCreateShader(type);

		GLES20.glShaderSource(shader, shaderCode);
		GLES20.glCompileShader(shader);

		return shader;
	}
}