// vertex attributes
layout(location=0) in vec3 position;
layout(location=1) in vec3 normal;
layout(location=2) in vec2 texcoord;

// matrices
uniform mat4 rotate_matrix;
uniform mat4 view_projection_matrix;
uniform mat4 aspect_matrix;

out vec3 norm;


// input from vertex shader
out vec2 tc;

void main()
{
	tc = texcoord;
	gl_Position = aspect_matrix * view_projection_matrix * rotate_matrix * vec4(position,1);
}
