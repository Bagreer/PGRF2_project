#version 330
layout(location = 0) in vec3 inPosition;
uniform mat4 mat;
void main() {
    gl_Position = mat * vec4(inPosition, 1.0);
}