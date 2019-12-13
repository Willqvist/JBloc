#version 330 core
in vec3 position;
in vec2 uv;
in vec3 normal;
in float light;
out vec2 t_uv;
out vec3 t_normal;
out float t_light;
uniform mat4 mvp;
void main(){
    gl_Position =mvp*vec4(position,1.0);
    t_uv = uv;
    t_normal = normal;
    t_light= light;
}