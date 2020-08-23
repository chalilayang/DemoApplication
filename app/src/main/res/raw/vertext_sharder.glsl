uniform mat4 u_TextureTransform;

attribute vec4 a_TextureCoordinates;
attribute vec4 a_Position;

varying vec2 v_TexCoordinate;

void main () {
    v_TexCoordinate = (u_TextureTransform * a_TextureCoordinates).xy;
    gl_Position = a_Position;
}
