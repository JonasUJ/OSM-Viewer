#version 130

in vec2 position;
in int drawable_id;
uniform mat4 transform;
uniform mat4 orthographic;
uniform sampler1D color_map;
uniform usampler1D map;
uniform uint category_bitset;
uniform float time;

out vec4 vert_color;
flat out float out_time;
flat out int vert_drawable_id;

void main()
{
    // This assumes color_map and category_map are the same size
    float size = textureSize(map, 0) - 1.0;
    float idx = float(drawable_id) / size;

    vec4 color = texture(color_map, idx);

    uvec2 drawable = texture(map, idx).rg;
    uint category = texture(map, idx).r;
    uint layer = texture(map, idx).g;

    gl_Position = orthographic * transform * vec4(position, float(layer) / size, 1.0);

    vert_drawable_id = drawable_id;
    out_time = time;
    vert_color = vec4(color.rgb, color.a * int((category_bitset & category) != 0u));
}
