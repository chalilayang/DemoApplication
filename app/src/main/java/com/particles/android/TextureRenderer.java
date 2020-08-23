package com.particles.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.Surface;

import com.particles.android.data.VertexArray;
import com.particles.android.programs.ShaderProgram;
import com.particles.android.util.TextureHelper;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

public class TextureRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "TextureRenderer";
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

    private SurfaceTexture mSurfaceTexture;
    private Surface mSurface;
    private int[] mTextureIds = new int[1];

    private float[] mTextureTransform = new float[16];
    private volatile boolean mFrameAvailable = false;
    private Context mContext;

    private VertexArray mPositionArray;
    private VertexArray mTextureCoordinateArray;

    private ShaderProgram mShaderProgram;
    public TextureRenderer(Context context) {
        mContext = context;
        mPositionArray = new VertexArray(sSquareCoordinate);
        mTextureCoordinateArray = new VertexArray(mTextureCoordinate);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.i(TAG, "onSurfaceCreated: ");
        glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (mSurfaceTexture == null) {
            if (TextureHelper.createExternalSurfaceTexture(mTextureIds)) {
                mSurfaceTexture = new SurfaceTexture(mTextureIds[0]);
                mSurfaceTexture.setDefaultBufferSize(1080, 1080);
                mSurfaceTexture.setOnFrameAvailableListener(this::onFrameAvailable);
                mSurface = new Surface(mSurfaceTexture);
            }
        }
        mShaderProgram = new ShaderProgram(mContext);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.i(TAG, "onSurfaceChanged: ");
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.i(TAG, "onDrawFrame: ");
        glClear(GL_COLOR_BUFFER_BIT);
        if (mFrameAvailable) {
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mTextureTransform);
            mFrameAvailable = false;
        }
        mShaderProgram.useProgram();
        bindBufferData(mShaderProgram);
        mShaderProgram.setMatrix(mTextureTransform);
        mShaderProgram.setTextureId(mTextureIds[0]);
        mShaderProgram.draw();
    }

    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        mFrameAvailable = true;
    }

    public void bindBufferData(ShaderProgram program) {
        mPositionArray.setVertexAttribPointer(
                0, program.getPositionLocation(), 2, 0);
        mTextureCoordinateArray.setVertexAttribPointer(
                0, program.getTextureCoordinateLocation(), 2, 0);
    }

    public Canvas lockCanvas(Rect dirty) {
        if (mSurface != null) {
            return mSurface.lockCanvas(dirty);
        } else {
            return null;
        }
    }

    public void unlockCanvasAndPost(Canvas canvas) {
        if (mSurface != null) {
            mSurface.unlockCanvasAndPost(canvas);
        }
    }
}
