#version 150 core
#define MAX_COLORS 16

in vec2 vPosition;

out vec4 oColor;

uniform vec4 uColors[MAX_COLORS];
uniform float uPositions[MAX_COLORS];
uniform int uCount;

void main() {
    vec4 color = uColors[0];
    float y = vPosition.y;

    for(int i = uCount - 1; i != 0; i--) {
        if(y <= uPositions[i] && y >= uPositions[i - 1]) {
            color = mix(uColors[i - 1], uColors[i], (y - uPositions[i - 1]) / (uPositions[i] - uPositions[i - 1]));
        }
    }
    oColor = color;
}