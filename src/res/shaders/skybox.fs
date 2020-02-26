#version 400
out vec4 fragColor;
in vec3 t_uv;

uniform samplerCube cubeMap;
void main(){
	fragColor = texture(cubeMap,t_uv);
}