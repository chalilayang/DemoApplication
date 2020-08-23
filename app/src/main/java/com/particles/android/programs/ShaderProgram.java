/***
 * Excerpted from "OpenGL ES for Android",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/kbogla for more book information.
***/
package com.particles.android.programs;

import static android.opengl.GLES20.glUseProgram;
import android.content.Context;

import com.particles.android.util.ShaderHelper;
import com.particles.android.util.TextResourceReader;

abstract class ShaderProgram {
    protected static final String A_POSITION = "a_Position";
    protected final int program;

    protected ShaderProgram(Context context, int vertexShaderResourceId,
        int fragmentShaderResourceId) {
        program = ShaderHelper.buildProgram(
            TextResourceReader.readTextFileFromResource(context, vertexShaderResourceId),
            TextResourceReader.readTextFileFromResource(context, fragmentShaderResourceId));
    }        

    public void useProgram() {
        glUseProgram(program);
    }
}
