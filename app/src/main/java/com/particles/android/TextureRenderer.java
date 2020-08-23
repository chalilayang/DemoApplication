package com.particles.android;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;

import com.particles.android.util.TextureHelper;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

class TextureRenderer implements GLSurfaceView.Renderer {

    private static float sSquareSize = 1.0f;
    private static float sSquareCoordinate[] = {
            -sSquareSize, sSquareSize, 0.0f,   // top left
            -sSquareSize, -sSquareSize, 0.0f,   // bottom left
            sSquareSize, -sSquareSize, 0.0f,    // bottom right
            sSquareSize, sSquareSize, 0.0f     // top right
    };

    private FloatBuffer mTextureBuffer;
    private float mTextureCoordinate[] = {
            0.0f, 1.0f, 0.0f, 1.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 0.0f, 0.0f, 1.0f,
            1.0f, 1.0f, 0.0f, 1.0f
    };
    private static short sDrawOrder[] = {
            0, 1, 2,
            0, 2, 3
    };
    private int mShaderProgram;
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawOrderBuffer;

    private SurfaceTexture mSurfaceTexture;
    private int[] mTextureIds = new int[1];

    private float[] mVideoTextureTransform;
    private boolean mFrameAvailable = false;
    private Context mContext;

    public TextureRenderer() {}

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        if (mSurfaceTexture == null) {
            if (TextureHelper.createExternalSurfaceTexture(mTextureIds)) {
                mSurfaceTexture = new SurfaceTexture(mTextureIds[0]);
                mSurfaceTexture.setOnFrameAvailableListener(this::onFrameAvailable);
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

    }

    @Override
    public void onDrawFrame(GL10 gl) {

    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {

    }

    private void setupVertexBuffer() {
        ByteBuffer orderByteBuffer = ByteBuffer.allocateDirect(sDrawOrder.length * 2);
        orderByteBuffer.order(ByteOrder.nativeOrder());
        mDrawOrderBuffer = orderByteBuffer.asShortBuffer();
        mDrawOrderBuffer.put(sDrawOrder);
        mDrawOrderBuffer.position(0);

        ByteBuffer bb = ByteBuffer.allocateDirect(sSquareCoordinate.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(sSquareCoordinate);
        mVertexBuffer.position(0);
    }

    private void setupTexture() {
        ByteBuffer buffer = ByteBuffer.allocateDirect(mTextureCoordinate.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        mTextureBuffer = buffer.asFloatBuffer();
        mTextureBuffer.put(mTextureCoordinate);
        mTextureBuffer.position(0);
    }
}
