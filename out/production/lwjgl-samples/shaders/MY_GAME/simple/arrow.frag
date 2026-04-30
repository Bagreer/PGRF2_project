#version 330
out vec4 outColor;
uniform vec3 arrowColor;
void main() {
    outColor = vec4(arrowColor, 1.0);
}