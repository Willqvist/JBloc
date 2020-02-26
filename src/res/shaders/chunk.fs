#version 460 core
out vec4 fragPosition;
in vec2 t_uv;
in vec3 t_normal;
in float t_light;
vec3 col = vec3(0.941, 0.941, 1);
uniform vec3 color;
uniform sampler2D tex;
void main(){
    vec4 tex = texture(tex,t_uv);
    if (tex.a == 0.0){
        discard;
    }
    //fragPosition = vec3(texture(tex,t_uv))*color;
    float shade = 0.9*max(/*dot(aNormal,normalize(lightDir)*Olight)*/t_light,0.0)+0.1;
    fragPosition = vec4(vec3(tex)*shade*color*col/*max(0.4,dot(normalize(vec3(0,1,0)),t_normal))*/,tex.a);
    //fragPosition = vec3(max(0,dot(vec3(1,0,0),t_normal)),max(0,dot(vec3(0,1,0),t_normal)),max(0,dot(vec3(0,0,1),t_normal)));
}