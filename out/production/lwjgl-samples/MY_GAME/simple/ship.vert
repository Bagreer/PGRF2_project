#version 330
layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;
uniform mat4 mat;
out vec2 texCoords;
void main() {
    gl_Position = mat * vec4(inPosition, 1.0);
    texCoords = inTexCoord;
}