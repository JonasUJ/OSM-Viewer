#version 130

in vec2 position;
in int drawable_id;
uniform mat4 projection;
uniform sampler1D color_map;
uniform usampler1D map;
uniform uint category_bitset;
uniform float time;

out vec4 vert_color;
flat out uint draw;
flat out float out_time;
flat out int vert_drawable_id;

void main()
{
    vert_drawable_id = drawable_id;
    out_time = time;

    float size = textureSize(map, 0) - 1.0;
    float idx = float(drawable_id) / size;

    vert_color = texture(color_map, idx);

    uvec2 drawable = texture(map, idx).rg;
    uint category = drawable.r;
    uint layer = drawable.g;

    draw = category_bitset & category;

    gl_Position = projection * vec4(position, float(layer) / size, 1.0);
}
