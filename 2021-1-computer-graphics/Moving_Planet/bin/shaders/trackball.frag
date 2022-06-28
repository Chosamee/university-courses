#ifdef GL_ES
	#ifndef GL_FRAGMENT_PRECISION_HIGH	// highp may not be defined
		#define highp mediump
	#endif
	precision highp float; // default precision needs to be defined
#endif

// input from vertex shader
in vec3 norm;
in vec2 tc;

// the only output variable
out vec4 fragColor;

uniform int b_color_type;

void main()
{
	fragColor = vec4(normalize(norm), 1.0);
	if (b_color_type == 0) fragColor = vec4(tc.xy,0,1);
	else if (b_color_type == 1) fragColor = vec4(tc.xxx, 1);
	else if (b_color_type == 2) fragColor = vec4(tc.yyy, 1);
}
