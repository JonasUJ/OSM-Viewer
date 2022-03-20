#version 130

in int inColor;
in vec3 inPosition;
uniform mat4 inTrans;
uniform mat4 inOrthographic;
uniform sampler1D colorMap;

out vec4 color;

void main()
{
    gl_Position = inOrthographic * inTrans * vec4(inPosition, 1.0);
    color = texture(colorMap, float(inColor) / (textureSize(colorMap, 0) - 1.0));
}
