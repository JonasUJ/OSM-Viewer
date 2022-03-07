#version 130

in vec3 inColor;
in vec3 inPosition;
uniform mat4 inTrans;
uniform mat4 inOrthographic;

out vec4 color;

void main()
{
    gl_Position = inOrthographic * inTrans * vec4(inPosition, 1.0);
    color = vec4(inColor, 1.0);
}
