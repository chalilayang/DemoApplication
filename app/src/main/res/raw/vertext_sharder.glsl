uniform mat4 u_TextureTransform;

attribute vec2 a_TextureCoordinates;
attribute vec4 a_Position;

varying vec2 v_TexCoordinate;

void main () {
    v_TexCoordinate = a_TextureCoordinates;
    gl_Position = a_Position;
}
