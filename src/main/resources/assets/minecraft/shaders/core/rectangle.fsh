#version 120

uniform vec2 position;
uniform vec2 size;
uniform vec4 rounding;
uniform vec2 smoothness;
uniform vec4 color1;
uniform vec4 color2;
uniform vec4 color3;
uniform vec4 color4;

float distanceFunction(vec2 point, vec2 border, vec4 roundParams) {
    roundParams.xy = (point.x > 0.0) ? roundParams.xy : roundParams.zw;
    roundParams.x  = (point.y > 0.0) ? roundParams.x  : roundParams.y;

    vec2 adjustedPoint = abs(point) - border + roundParams.x;

    return min(max(adjustedPoint.x, adjustedPoint.y), 0.0) + length(max(adjustedPoint, 0.0)) - roundParams.x;
}

vec4 createGradient(vec2 coords, vec4 color1, vec4 color2, vec4 color3, vec4 color4) {
    // Используем smoothstep для более плавной интерполяции
    float xBlend = smoothstep(0.0, 1.0, coords.x);
    float yBlend = smoothstep(0.0, 1.0, coords.y);
    return mix(mix(color1, color2, yBlend), mix(color3, color4, yBlend), xBlend);
}

vec3 createGradientWithoutAlpha(vec2 coords, vec3 color1, vec3 color2, vec3 color3, vec3 color4) {
    float xBlend = smoothstep(0.0, 1.0, coords.x);
    float yBlend = smoothstep(0.0, 1.0, coords.y);
    return mix(mix(color1, color2, yBlend), mix(color3, color4, yBlend), xBlend);
}

void main() {
    vec2 adjustedCoords = gl_FragCoord.xy - position;
    vec2 normalizedCoords = adjustedCoords / size;

    vec2 localCoords = normalizedCoords * size;
    vec2 halfSize = 0.5 * size;

    float smoothAlpha = 1.0 - smoothstep(smoothness.x, smoothness.y, distanceFunction(halfSize - localCoords, halfSize - 1.0, rounding));

    vec4 gradientColor = createGradient(normalizedCoords, color1, color2, color3, color4);
    vec4 gradientColorWithoutAlpha = vec4(createGradientWithoutAlpha(normalizedCoords, color1.rgb, color2.rgb, color3.rgb, color4.rgb), 0.0);

    gl_FragColor = mix(gradientColorWithoutAlpha, gradientColor, smoothAlpha);
}
