#ifndef __PLANETS_H__
#define __PLANETS_H__
#include "cgmath.h"
#include <iostream>

struct planet {
	float radius;
	float rot_speed;
	float rev_speed;
	float au;
	std::string texture_path;
	std::string normal_texture_path;
	mat4 model_matrix;
	float dwarf_au;
	GLuint texture;
	GLuint normal_texture;

	void setVal(float r, float rts, float rvs, float a, char* tp, char* ntp, float da);
	void update_model(float t, float t_gap, float t_passed, bool b_rotate);
};


planet sun, mercury, venus, earth, mars, jupiter, saturn, uranus, neptune;
planet moon, dwarf1, dwarf2, dwarf3, dwarf4, dwarf11, dwarf12, dwarf13, dwarf14, dwarf15
		, dwarf21, dwarf22;

inline void planet::setVal(float r, float rts, float rvs, float a, char* tp, char* ntp, float da)
{
	radius = r;
	rot_speed = rts;
	rev_speed = rvs;
	au = a;
	texture_path = tp;
	dwarf_au = da;
	normal_texture_path = ntp;
}

inline void planet::update_model(float tt, float t_g, float t_p, bool b_r)
{
	float move = au * 100.f;
	float dwarf = dwarf_au * 100.f;
	float rot_theta = (t_p - t_g) * rot_speed;
	float rev_theta = (t_p - t_g) * rev_speed / 30;
	if (b_r)
	{
		rot_theta = (tt - t_g) * rot_speed;
		rev_theta = (tt - t_g) * rev_speed / 30;
	}
	if (dwarf_au != 0) {
		float m_rev_theta = 0;
		if (dwarf_au == earth.au) {
			m_rev_theta = (t_p - t_g) * earth.rev_speed / 30;
			if (b_r)	m_rev_theta = (tt - t_g) * earth.rev_speed / 30;
		}
		else if (dwarf_au == jupiter.au) {
			m_rev_theta = (t_p - t_g) * jupiter.rev_speed / 30;
			if (b_r)	m_rev_theta = (tt - t_g) * jupiter.rev_speed / 30;
		}
		else if (dwarf_au == uranus.au) {
			m_rev_theta = (t_p - t_g) * uranus.rev_speed / 30;
			if(b_r)		m_rev_theta = (tt - t_g) * uranus.rev_speed / 30;
		}
		else if (dwarf_au == neptune.au) {
			m_rev_theta = (t_p - t_g) * neptune.rev_speed / 30;
			if (b_r)		m_rev_theta = (tt - t_g) * neptune.rev_speed / 30;
		}
		model_matrix =
			mat4::translate(move * cos(rev_theta) + dwarf * cos(m_rev_theta),
				move * sin(rev_theta) + dwarf * sin(m_rev_theta), 0.0f) *
			mat4::rotate(vec3(0, 0, 1), rot_theta) *
			mat4::scale(vec3(radius, radius, radius));

	}
	else {
		model_matrix =
			mat4::translate(move * cos(rev_theta), move * sin(rev_theta), 0.0f) *
			mat4::rotate(vec3(0, 0, 1), rot_theta) *
			mat4::scale(vec3(radius, radius, radius));
	}
}

void setPlanets()
{
	sun.setVal(3.00f, 1.9969f, 0.f, 0.f, "../bin/textures/sun.jpg", "", 0);
	mercury.setVal(0.383f, 0.03f, 47.87f, 0.39f, "../bin/textures/mercury.jpg", "../bin/textures/mercury-normal.jpg", 0);
	venus.setVal(0.950f, 0.018f, 35.02f, 0.7f, "../bin/textures/venus.jpg", "../bin/textures/venus-normal.jpg", 0);
	earth.setVal(1.f, 0.4651f, 29.79f, 1.f, "../bin/textures/earth.jpg", "../bin/textures/earth-normal.jpg", 0);
	mars.setVal(0.532f, 0.2411f, 24.13f, 1.52f, "../bin/textures/mars.jpg", "../bin/textures/mars-normal.jpg", 0);
	jupiter.setVal(1.97f, 1.6f, 13.07f, 2.f, "../bin/textures/jupiter.jpg", "", 0);
	saturn.setVal(0.914f, 2.87f, 9.67f, 2.5f, "../bin/textures/saturn.jpg", "", 0);
	uranus.setVal(0.98f, 2.59f, 6.84f, 3.f, "../bin/textures/uranus.jpg", "", 0);
	neptune.setVal(0.87f, 2.68f, 5.48f, 3.5f, "../bin/textures/neptune.jpg", "", 0);

	moon.setVal(0.2f, 1.022f, 50.022f, 0.2f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", earth.au);
	dwarf1.setVal(0.2f, 1.022f, 30.655f, 0.25f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", jupiter.au);
	dwarf2.setVal(0.2f, 2.022f, 15.655f, 0.3f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", jupiter.au);
	dwarf3.setVal(0.2f, 1.22f, 40.655f, 0.27f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", jupiter.au);
	dwarf4.setVal(0.2f, 1.72f, 20.655f, 0.35f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", jupiter.au);

	dwarf11.setVal(0.2f, 1.72f, 30.655f, 0.17f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", uranus.au);
	dwarf12.setVal(0.2f, 1.12f, 20.655f, 0.25f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", uranus.au);
	dwarf13.setVal(0.2f, 2.42f, 15.655f, 0.15f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", uranus.au);
	dwarf14.setVal(0.2f, 2.72f, 40.655f, 0.20f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", uranus.au);
	dwarf15.setVal(0.2f, 2.72f, 50.655f, 0.23f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", uranus.au);

	dwarf21.setVal(0.2f, 2.72f, 70.655f, 0.20f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", neptune.au);
	dwarf22.setVal(0.2f, 1.72f, 40.655f, 0.40f, "../bin/textures/moon.jpg", "../bin/textures/moon-normal.jpg", neptune.au);
}

inline std::vector<planet> create_solar() {
	setPlanets();
	std::vector<planet> solar;
	solar.emplace_back(sun);
	solar.emplace_back(mercury);
	solar.emplace_back(venus);
	solar.emplace_back(earth);
	solar.emplace_back(mars);
	solar.emplace_back(jupiter);
	solar.emplace_back(saturn);
	solar.emplace_back(uranus);
	solar.emplace_back(neptune);
	solar.emplace_back(moon);
	solar.emplace_back(dwarf1);
	solar.emplace_back(dwarf2);
	solar.emplace_back(dwarf3);
	solar.emplace_back(dwarf4);
	solar.emplace_back(dwarf11);
	solar.emplace_back(dwarf12);
	solar.emplace_back(dwarf13);
	solar.emplace_back(dwarf14);
	solar.emplace_back(dwarf15);
	solar.emplace_back(dwarf21);
	solar.emplace_back(dwarf22);
	return solar;
}

struct ring {
	float au;
	float scale;
	float rev_speed;
	mat4 model_matrix;
	std::string texture_path;
	GLuint texture;
	std::string alpha_texture_path;
	GLuint alpha_texture;

	void setVal(float au, float scale);
	void update_model(float t, float t_gap, float t_passed, bool b_rotate);
};

inline void ring::setVal(float a, float sc)
{
	au = a;
	scale = sc;
	if (au == saturn.au)
	{
		rev_speed = saturn.rev_speed;
		texture_path = "../bin/textures/saturn-ring.jpg";
		alpha_texture_path = "../bin/textures/saturn-ring-alpha.jpg";
	}
	else if (au == uranus.au)
	{
		rev_speed = uranus.rev_speed;
		texture_path = "../bin/textures/uranus-ring.jpg";
		alpha_texture_path = "../bin/textures/uranus-ring-alpha.jpg";
	}
}

inline void ring::update_model(float tt, float t_g, float t_p, bool b_r)
{
	float move = au * 100.f;
	float rev_theta = (t_p - t_g) * rev_speed / 30;
	if (b_r)
	{
		rev_theta = (tt - t_g) * rev_speed / 30;
	}
	model_matrix =
		mat4::translate(move * cos(rev_theta), move * sin(rev_theta), 0.0f) *
		mat4::scale(vec3(scale, scale, scale));
}

inline std::vector<ring> create_ring()
{
	ring saturn_ring, uranus_ring;
	saturn_ring.setVal(saturn.au, 1.f);
	uranus_ring.setVal(uranus.au, 1.f);
	std::vector<ring> rings;
	rings.emplace_back(saturn_ring);
	rings.emplace_back(uranus_ring);
	
	return rings;
}

#endif