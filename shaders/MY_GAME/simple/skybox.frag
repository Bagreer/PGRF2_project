#version 330
in vec3 texCoords;
out vec4 outColor;
uniform samplerCube skyboxTexture;
void main() {
    outColor = texture(skyboxTexture, texCoords);
}