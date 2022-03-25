#version 130

in vec4 vert_color;
flat in float out_time;
flat in int vert_drawable_id;

out vec4 color;

void main()
{
    vec3 lum = vec3(0.299, 0.587, 0.114);
    color = vec4(vec3(dot(vert_color.rgb, lum)), vert_color.a);
}
