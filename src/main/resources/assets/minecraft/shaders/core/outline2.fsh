#version 120

uniform sampler2D sampler;
uniform vec4 color;

void main(void)
{
    float s = 0.001;
    vec4 t = texture2D(sampler, vec2(gl_TexCoord[0].x, 1.0 - gl_TexCoord[0].y));

    bool isOutline = texture2D(sampler, vec2(gl_TexCoord[0].x + s, 1.0 - gl_TexCoord[0].y)).a == 0.0 && t.a != 0.0 ||
                     texture2D(sampler, vec2(gl_TexCoord[0].x - s, 1.0 - gl_TexCoord[0].y)).a == 0.0 && t.a != 0.0 ||
                     texture2D(sampler, vec2(gl_TexCoord[0].x, 1.0 - gl_TexCoord[0].y - s)).a == 0.0 && t.a != 0.0 ||
                     texture2D(sampler, vec2(gl_TexCoord[0].x, 1.0 - gl_TexCoord[0].y + s)).a == 0.0 && t.a != 0.0;

    if (isOutline) {
        t.rgba = color;
    } else {
        t = vec4(0.0);
    }

    gl_FragColor = t;
}
