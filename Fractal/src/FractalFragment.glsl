#version 150 core
#define MAX_LINES 64 

in vec2 vPosition;

out vec4 oColor;

uniform int uJulia;
uniform vec2 uJuliaXY;
uniform int uMaxCount;
uniform float uBailout;
uniform float uFrequency;
uniform vec2 uCenter;
uniform vec2 uScale;
uniform vec2 uLines[MAX_LINES];
uniform int uLineCount;

uniform sampler2D uGradient;

float distance(vec2 a, vec2 b, vec2 p) {
    vec2 ab = b - a;
    vec2 ap = p - a;
    vec2 c = a;
    float s = dot(ab, ap);

    if(s > 0.0) {
        s /= dot(ab, ab);
        if(s < 1.0) {
            c = a + ab * s;
        } else { 
            c = b;
        }
    }
    return length(p - c);
}

void main() {
    vec4 color = vec4(0.0, 0.0, 0.0, 1.0);
    vec2 z = vec2(0.0, 0.0);
    float min = 999999.0;
    int i;

    if(uJulia != 0) {
        z = vPosition;
    }

    for(i = 0; i < uMaxCount && length(z) < uBailout; i++) {
        if(uJulia != 0) {
            z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + uJuliaXY;
        } else {
            z = vec2(z.x * z.x - z.y * z.y, 2.0 * z.x * z.y) + vPosition;
        }

        for(int j = 0; j != uLineCount; j += 2) {
            float d = distance(uLines[j] * uScale + uCenter, uLines[j + 1] * uScale + uCenter, z);

            if(d < min) {
                min = d;
            }
        }
    }

    if(i == uMaxCount) {
        color = vec4(0.0, 0.0, 0.0, 1.0);
    } else {
        color = texture(uGradient, vec2(sin(float(min) * uFrequency) * 0.5 + 0.5, 0.0));
    }
    oColor = color;
}