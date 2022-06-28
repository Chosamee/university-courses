#pragma once
#ifndef __CIRCLE_H__
#define __CIRCLE_H__
#include <random>

struct circle_t
{
	vec2	center=vec2(0);		// 2D position for translation
	float	radius;		// radius
	vec4	color;				// RGBA color in [0,1]
	vec2	velocity;
	mat4	model_matrix;	// modeling transformation
	std::vector<circle_t> cir_list;
	// public functions
	void	update (float t , uint cir_n, ivec2 window_size, std::vector<circle_t> circles, int i, float passed_t);
	void	check_collision(ivec2 window_size, float t, float passed_t);
	void	check_collision_circle(std::vector<circle_t> circles, float t, int i, uint cir_n, float passed_t);
};


inline std::vector<circle_t> create_circles(uint cir_n, float t, ivec2 window_size)
{
	std::vector<circle_t> circles;
	circle_t c;

	std::random_device rd;
	std::mt19937 gen(rd());
	std::uniform_int_distribution<int> dist(0, 999);
	std::uniform_int_distribution<int> distx(0, window_size.x);
	std::uniform_int_distribution<int> disty(0, window_size.y);
	std::uniform_int_distribution<int> expo(0, 1);

	float rmin = float(0.4 / sqrt(cir_n));
	float rmax = float(0.9 / sqrt(cir_n));
	int a = int(rmin * 100);
	int b = int(rmax * 100);
	std::uniform_int_distribution<int> disrad(a, b);
	srand(uint(time(NULL)));


	for (uint k = 0;k < cir_n;k++) {
		float color[4] = {};
		bool flags = false;
		for (int i = 0; i < 4;i++) {
			color[i] = float(dist(gen))/1000;
		}
		float width = float(dist(gen))/10000*30 - 1.5f;
		float height = float(dist(gen))/10000*16 - 0.8f;
		float rad = float(disrad(gen)) / 100;
		float vel_scale = 333;
		float dx = float(dist(gen)) / vel_scale;
		float dy = float(dist(gen)) / vel_scale;
		float balance = 999/(vel_scale*2);
		c = { vec2(width, height), rad, vec4(color[0], color[1], color[2], color[3]), vec2(dx-balance, dy-balance)};
		for (auto& c2 : circles) {
			if (sqrt(pow((c.center.x - c2.center.x), 2) + pow((c.center.y - c2.center.y), 2)) < c.radius + c2.radius) flags = true;
		}
		if (flags) {
			k--;
			continue;
		}
		circles.emplace_back(c);
	}
	return circles;
}


inline void circle_t::check_collision(ivec2 window_size, float t, float passed_t)
{
	float wraped_x = velocity.x * (t - passed_t);
	float wraped_y = velocity.y * (t - passed_t);
	if (center.x+radius+wraped_x >= 1.0f*16/9 || center.x-radius+wraped_x <= -1.0f * 16 / 9) {
		velocity.x = -velocity.x;
	}
	if (center.y+radius+wraped_y >= 1.0f || center.y-radius+wraped_y <= -1.0f) {
		velocity.y = -velocity.y;
	}
}


inline void circle_t::check_collision_circle(std::vector<circle_t> clist, float t, int i, uint cir_n, float passed_t) {
	for (auto &c2:clist) {
		//circle_t c2 = clist[j];
		float mass = float(pow(radius, 2) * PI);
		float cmass = float(pow(c2.radius, 2) * PI);

		float norm_center = float(pow((center.x - c2.center.x), 2) + pow((center.y - c2.center.y), 2));

		float proj_c = float((velocity.x * (center.x - c2.center.x) + velocity.y * (center.y - c2.center.y))
			/ sqrt(norm_center));
		float proj_c2 = float((c2.velocity.x * (center.x - c2.center.x) + c2.velocity.y * (center.y - c2.center.y))
			/ sqrt(norm_center));
		float inner_prod = float((velocity.x - c2.velocity.x) * (center.x - c2.center.x) + (velocity.y - c2.velocity.y) * (center.y - c2.center.y));
		if (center.x == c2.center.x && center.y == c2.center.y) continue;
		if (sqrt(norm_center) + (proj_c - proj_c2) * (t - passed_t)/2 <= radius + c2.radius )
		{
			//after update
			velocity.x = float(velocity.x - ((2.0f * cmass * inner_prod * (center.x - c2.center.x))
				/ ((mass + cmass) * norm_center)));
			velocity.y = float(velocity.y - ((2.0f * cmass * inner_prod * (center.y - c2.center.y))
				/ ((mass + cmass) * norm_center)));
		}
	}
}



inline void circle_t::update( float t , uint cir_n, ivec2 window_size, std::vector<circle_t> circles, int i, float passed_t)
{
	// these transformations will be explained in later transformation lecture
	
	if (i == 0) {
		cir_list = circles;
	}
	
	std::vector<circle_t> clist = circles[0].cir_list;

	check_collision(window_size, t, passed_t);
	check_collision_circle(clist, t, i, cir_n, passed_t);

	mat4 scale_matrix =
	{
		radius, 0, 0, 0,
		0, radius, 0, 0,
		0, 0, 1, 0,
		0, 0, 0, 1
	};

	center.x += velocity.x * (t-passed_t);
	center.y += velocity.y * (t-passed_t);

	mat4 translate_matrix =
	{
		1, 0, 0, center.x,
		0, 1, 0, center.y,
		0, 0, 1, 0,
		0, 0, 0, 1
	};
	model_matrix = translate_matrix*scale_matrix;
}

#endif
