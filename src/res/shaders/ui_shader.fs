#version 150 core
out vec4 fragPosition;
in vec2 t_uv;
in float ta;
uniform vec4 color;
uniform sampler2D tex;
void main(){
    fragPosition = texture(tex,t_uv)*color*ta + (1-ta)*color;
}