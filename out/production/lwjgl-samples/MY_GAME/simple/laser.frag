#version 330
out vec4 outColor;
uniform vec3 laserColor;
void main() {
    outColor = vec4(laserColor, 1.0);
}