#version 150 core
out vec4 fragPosition;
in vec2 t_uv;
in vec3 t_normal;
uniform vec3 color;
uniform sampler2D tex;
void main(){
    //fragPosition = vec3(texture(tex,t_uv))*color;
    fragPosition = vec4(vec3(texture(tex,t_uv))*color*max(0.4,dot(normalize(vec3(1,1,0)),t_normal)),1.0);
    //fragPosition = vec3(max(0,dot(vec3(1,0,0),t_normal)),max(0,dot(vec3(0,1,0),t_normal)),max(0,dot(vec3(0,0,1),t_normal)));
}