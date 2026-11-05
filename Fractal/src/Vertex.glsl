#version 150 core

in vec2 iPosition;

out vec2 vPosition;

uniform vec2 uMin;
uniform vec2 uMax;

void main() {
    gl_Position = vec4(vec2(-1.0, -1.0) + iPosition * 2.0, 0.0, 1.0);

    vPosition = uMin + iPosition * (uMax - uMin);
}