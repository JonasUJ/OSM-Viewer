#version 130

in vec4 vert_color;
flat in uint draw;
flat in float out_time;
flat in int vert_drawable_id;

out vec4 color;

//  Function from Iñigo Quiles
//  https://www.shadertoy.com/view/MsS3Wc
vec3 hsb2rgb(in vec3 c) {
    vec3 rgb = clamp(
        abs(mod(
            c.x * 6.0 + vec3(0.0, 4.0, 2.0),
            6.0
        ) - 3.0) - 1.0,
        0.0, 1.0
    );

    rgb = rgb * rgb * (3.0 - 2.0 * rgb);
    return c.z * mix(vec3(1.0), rgb, c.y);
}

void main()
{
    if (draw == 0u) {
        discard;
    }

    float shift = mod(2 * out_time + float(vert_drawable_id), 6.28) / 6.28;
    vec3 drawable_color = hsb2rgb(vec3(shift, 1.0, 1.0));
    color = vec4(drawable_color, vert_color.a);
}
