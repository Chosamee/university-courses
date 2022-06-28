#ifdef GL_ES
	#ifndef GL_FRAGMENT_PRECISION_HIGH	// highp may not be defined
		#define highp mediump
	#endif
	precision highp float; // default precision needs to be defined
#endif

// input from vertex shader
in vec4 epos;
in vec3 norm;
in vec2 tc;

// the only output variable
out vec4 fragColor;

uniform mat4	view_matrix;
uniform float	shininess;
uniform vec4	light_position, Ia, Id, Is;	// light
uniform vec4	Ka, Kd, Ks;					// material properties

// texture sampler
uniform sampler2D TEX;
uniform sampler2D TEX_norm;
uniform sampler2D TEX_alpha;
uniform int mode;
uniform int planet_cnt;
uniform int check_norm = 0;
uniform int is_alpha = 0;

// bump mapping
vec3 c1 = cross(norm,vec3(0,0,1));
vec3 c2 = cross(norm,vec3(0,1,0));
vec3 tangent = normalize(length(c1)>length(c2)?c1:c2);
vec3 binormal = cross(norm,tangent);
mat3 TBN = mat3(tangent, binormal, norm);
vec3 normal = texture(TEX_norm, tc).xyz;
// value of texture(rgb) = vec/2 + 0.5
vec3 newn = normalize(TBN*(2.0 * normal - vec3(1.f,1.f,1.f)));

vec4 phong( vec3 l, vec3 n, vec3 h, vec4 Kd )
{
	vec4 Ira = Ka*Ia;									// ambient reflection
	vec4 Ird = max(Kd*dot(l,n)*Id,0.0);					// diffuse reflection
	vec4 Irs = max(Ks*pow(dot(h,n),shininess)*Is,0.0);	// specular reflection
	return Ira + Ird + Irs;
}

void main()
{
	// light position in the eye space
	vec4 lpos = view_matrix*light_position;
	vec3 n = normalize(norm);	// norm interpolated via rasterizer should be normalized again here
	vec3 p = epos.xyz;			// 3D position of this fragment
	vec3 l = normalize(lpos.xyz-(lpos.a==0.0?vec3(0):p));	// lpos.a==0 means directional light
	vec3 v = normalize(-p);		// eye-epos = vec3(0)-epos
	vec3 h = normalize(l+v);	// the halfway vector

	vec3 nn = n;

	if (check_norm == 1)
	{
		nn = normalize(newn);	// norm interpolated via rasterizer should be normalized again here
	}

	vec4 iKd = texture( TEX, tc );	// Kd from image
	vec4 iKd_alpha = texture(TEX_alpha, tc);

	if(mode==0)
	{
		fragColor = phong( l, n, h, iKd );
		if (planet_cnt == 0)		fragColor = iKd;
		if (check_norm == 1)	fragColor = phong(l, nn, h, iKd);
		if (is_alpha == 1)		fragColor.a = iKd_alpha.r;
	}
	else if(mode==1)
	{
		fragColor = phong( l, n, h, iKd );
		if (planet_cnt == 0)		fragColor = iKd;
		if (is_alpha == 1)		fragColor.a = iKd_alpha.r;
	}
	else if(mode==2)
	{
		fragColor = phong( l, n, h, Kd );
		if (planet_cnt == 0)		fragColor = Kd;
	}
	else if(mode==3)	fragColor = iKd;
	else				fragColor = vec4( tc, 0, 1 );
}
