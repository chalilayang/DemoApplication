uniform mat4 u_TextureTransform;

attribute vec2 a_TextureCoordinates;
attribute vec4 a_Position;

varying vec2 v_TexCoordinate;

const int GAUSSIAN_SAMPLES = 9;

uniform float texelWidthOffset;
uniform float texelHeightOffset;

varying vec2 textureCoordinate;
varying vec2 blurCoordinates[GAUSSIAN_SAMPLES];

void main () {
    v_TexCoordinate = a_TextureCoordinates;
    gl_Position = a_Position;
}
