#ifdef GL_ES
	#ifndef GL_FRAGMENT_PRECISION_HIGH	// highp may not be defined
		#define highp mediump
	#endif
	precision highp float; // default precision needs to be defined
#endif


// fragment shader
in vec2 tc;
out vec4 fragColor;

// global values
uniform int b_color_type;

void main()
{
	if (b_color_type == 0) fragColor = vec4(tc.xy,0,1);
	else if (b_color_type == 1) fragColor = vec4(tc.xxx, 1);
	else if (b_color_type == 2) fragColor = vec4(tc.yyy, 1);
}