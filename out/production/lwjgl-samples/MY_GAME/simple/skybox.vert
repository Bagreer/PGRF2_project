#version 330
in vec3 inPosition;
out vec3 texCoords;
uniform mat4 mat;
void main() {
    texCoords = inPosition; // Pozice vertexu kostky slouží přímo jako směr do Cubemapy
    gl_Position = mat * vec4(inPosition, 1.0);
}