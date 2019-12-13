#version 330 core
in vec3 position;
in vec2 uv;
out vec2 t_uv;
out float ta;
uniform float textureActive;
uniform mat4 mvp;
void main(){
    gl_Position =mvp*vec4(position.xy,0.0,1.0);
    t_uv = uv;
    ta = textureActive;
}