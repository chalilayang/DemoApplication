/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.render.demo;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glUseProgram;
import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import com.example.mi.demoapplication.R;

public class ShaderProgram {
    private static final String TAG = "ShaderProgram";
    protected static final String A_POSITION_LOCATION = "a_Position";
    protected static final String A_TEX_COORDINATES_LOCATION = "a_TextureCoordinates";

    protected static final String U_TEX_TRANSFORM_LOCATION = "u_TextureTransform";
    protected static final String U_EXTERNAL_TEX_LOCATION = "u_ExternalTexture";

    protected final int program;

    protected int aPositionLocation;
    protected int aTextureCoordinateLocation;

    protected int uExternalTextureLocation;

    protected int uTextureTransformLocation;

    public ShaderProgram(Context context) {
        this(context, R.raw.vertext_sharder, R.raw.fragment_shader);
        uTextureTransformLocation = glGetUniformLocation(program, U_TEX_TRANSFORM_LOCATION);
        uExternalTextureLocation = glGetUniformLocation(program, U_EXTERNAL_TEX_LOCATION);
        aPositionLocation = glGetAttribLocation(program, A_POSITION_LOCATION);
        aTextureCoordinateLocation = glGetAttribLocation(program, A_TEX_COORDINATES_LOCATION);
    }

    protected ShaderProgram(
            Context context, int vertexShaderResourceId, int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
            TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
            TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
    }

    public void setMatrix(float[] matrix) {
        if (uTextureTransformLocation > 0) {
            glUniformMatrix4fv(uTextureTransformLocation, 1, false, matrix, 0);
        }
    }

    public void setTextureId(int textureId) {
        if (uExternalTextureLocation > 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureId);
            glUniform1i(uExternalTextureLocation, 0);
        }
    }

    public void useProgram() {
        glUseProgram(program);
    }

    public void draw() {
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public int getPositionLocation() {
        return aPositionLocation;
    }

    public int getTextureCoordinateLocation() {
        return aTextureCoordinateLocation;
    }
}
