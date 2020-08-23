#version 120
#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES u_ExternalTexture;
varying vec2 v_TexCoordinate;

void main () {
    vec4 color = texture2D(u_ExternalTexture, v_TexCoordinate);
    gl_FragColor = color;
}
