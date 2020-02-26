#version 460 core
in vec3 position;
out vec3 t_uv;
uniform mat4 transform;
uniform mat4 viewProj;
void main(){
    gl_Position = viewProj*transform*vec4(position,1.0);
    t_uv = position;
}