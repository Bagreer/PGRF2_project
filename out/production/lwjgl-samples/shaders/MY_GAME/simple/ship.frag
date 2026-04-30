#version 330
in vec2 texCoords;
out vec4 outColor;
uniform sampler2D shipTexture;
void main() {
    outColor = texture(shipTexture, texCoords);
}