#include "cgmath.h"		// slee's simple math library
#define STB_IMAGE_IMPLEMENTATION
#include "cgut.h"		// slee's OpenGL utility
#include "trackball.h"	// virtual trackball
#include "planets.h"
#include <iostream>

//*************************************
// global constants
static const char*	window_name = "Full Solar System";
static const char*	vert_shader_path = "../bin/shaders/texphong.vert";
static const char*	frag_shader_path = "../bin/shaders/texphong.frag";



struct light_t
{
	vec4	position = vec4( 0.0f, 0.0f, 0.0f, 1.0f );   // directional light
    vec4	ambient  = vec4( 0.2f, 0.2f, 0.2f, 1.0f );
    vec4	diffuse  = vec4( 0.8f, 0.8f, 0.8f, 1.0f );
    vec4	specular = vec4( 1.0f, 1.0f, 1.0f, 1.0f );
};

struct material_t
{
	vec4	ambient  = vec4( 0.2f, 0.2f, 0.2f, 1.0f );
    vec4	diffuse  = vec4( 0.8f, 0.8f, 0.8f, 1.0f );
    vec4	specular = vec4( 1.0f, 1.0f, 1.0f, 1.0f );
	float	shininess = 1000.0f;
};

//*************************************
// window objects
GLFWwindow*	window = nullptr;
ivec2		window_size = cg_scale_by_dpi(1280, 720); // initial window size

//*************************************
// OpenGL objects
GLuint	program	= 0;	// ID holder for GPU program

//*************************************
// global variables
int		frame = 0;	// index of rendering frames
int		mode = 0;	// display mode

//*************************************
// scene objects
mesh*		p_mesh = new mesh;
mesh*		ring_mesh = new mesh;
camera		cam;
trackball	tb;
light_t		light;
material_t	material;

//*************************************
//constant, type
static const uint longitude = 72;
static const uint latitude = 36;
static const float unit_theta = PI / latitude;
static const float unit_phi = 2 * PI / longitude;
static const uint ring_tess = 100;
static const float r = 10.0f; //unit radius
static float t_passed = 0.0f;
static float t_gap = 0.0f;
bool b_rotate = true;
// build the model matrix for oscillating scale
mat4 model_matrix = mat4::scale(1, 1, 1);

std::vector<planet> solar = create_solar();
std::vector<ring> rings = create_ring();

//*************************************
// holder of vertices and indices of a unit circle
std::vector<vertex>	sphere_vertices;
std::vector<vertex> ring_vertices;
bool	b_wireframe = false;


//*************************************
std::vector<vertex> create_sphere_vertices() {
	// P(x, y, z) = (r sin(theta) cos(phi), r sin(theta)sin(phi), r cos(theta))
	std::vector<vertex> vertices;
	for (uint k = 0; k <= latitude; k++)
	{
		float theta = k * unit_theta;
		for (uint i = 0; i <= longitude; i++)
		{
			float phi = i * unit_phi;
			float x = r * sin(theta) * cos(phi);
			float y = r * sin(theta) * sin(phi);
			float z = r * cos(theta);

			vertices.push_back({ vec3(x, y, z), vec3(x / r, y / r, z / r), vec2(phi / (2 * PI), 1 - theta / PI) });
		}
	}
	return vertices;
}

std::vector<vertex> create_ring_vertices()
{
	std::vector<vertex> vertices;
	float per_theta = 2 * PI / ring_tess;
	float out_r = 25.0f;
	float in_r = 15.0f;
	for (uint k = 0; k <= ring_tess; k++)
	{
		float theta = k * per_theta;
		float x = out_r * sin(theta);
		float y = out_r * cos(theta);
		vertices.push_back({ vec3(x,y,0), vec3(x / out_r, y / out_r, 0), vec2(0, cos(theta / 4)) });
	}
	for (uint k = 0; k <= ring_tess; k++)
	{
		float theta = k * per_theta;
		float x = in_r * sin(theta);
		float y = in_r * cos(theta);
		vertices.push_back({ vec3(x,y,0), vec3(x / in_r, y / in_r, 0), vec2(1, cos(theta / 4)) });
	}
	return vertices;
}

void ring_index_buffer(const std::vector<vertex>& vertices)
{
	std::vector<uint> indices;

	int i = 0;
	int j = ring_tess;

	for (uint k = 0; k <= ring_tess; k++)
	{
		indices.push_back(i+1);
		indices.push_back(i);
		indices.push_back(j);

		indices.push_back(i + 1);
		indices.push_back(j);
		indices.push_back(j + 1);

		i++;
		j++;

	}

	ring_mesh->index_list = indices;
	glGenBuffers(1, &ring_mesh->index_buffer);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ring_mesh->index_buffer);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(uint) * indices.size(), &indices[0], GL_STATIC_DRAW);
}

void update_index_buffer(const std::vector<vertex>& vertices)
{
	// i -- i+1   z positive (1)
	// |  /  |
	// j -- j+1	  
	std::vector<uint> indices;

	// each latitude, has longitude + 1 vertices
	int i = 0;
	int j = longitude + 1;

	for (uint k = 0; k < latitude; k++)
	{
		for (uint _ = 0; _ <= longitude; _++)
		{
			// backface-culling Counter-clockwise
			// upper triangle
			indices.push_back(i + 1);
			indices.push_back(i);
			indices.push_back(j);
			// lower triangle
			indices.push_back(i + 1);
			indices.push_back(j);
			indices.push_back(j + 1);

			i++;
			j++;
		}
	}
	p_mesh->index_list = indices;
	glGenBuffers(1, &p_mesh->index_buffer);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, p_mesh->index_buffer);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(uint) * indices.size(), &indices[0], GL_STATIC_DRAW);
}

void update()
{
	// update projection matrix
	cam.aspect_ratio = window_size.x / float(window_size.y);
	cam.projection_matrix = mat4::perspective(cam.fovy, cam.aspect_ratio, cam.dNear, cam.dFar);

	// update uniform variables in vertex/fragment shaders
	GLint uloc;
	uloc = glGetUniformLocation(program, "view_matrix");		if (uloc > -1) glUniformMatrix4fv(uloc, 1, GL_TRUE, cam.view_matrix);
	uloc = glGetUniformLocation(program, "projection_matrix");	if (uloc > -1) glUniformMatrix4fv(uloc, 1, GL_TRUE, cam.projection_matrix);
	glUniform1i(glGetUniformLocation(program, "mode"), mode);

	// setup light properties
	glUniform4fv(glGetUniformLocation(program, "light_position"), 1, light.position);
	glUniform4fv(glGetUniformLocation(program, "Ia"), 1, light.ambient);
	glUniform4fv(glGetUniformLocation(program, "Id"), 1, light.diffuse);
	glUniform4fv(glGetUniformLocation(program, "Is"), 1, light.specular);

	// setup material properties
	glUniform4fv(glGetUniformLocation(program, "Ka"), 1, material.ambient);
	glUniform4fv(glGetUniformLocation(program, "Kd"), 1, material.diffuse);
	glUniform4fv(glGetUniformLocation(program, "Ks"), 1, material.specular);
	glUniform1f(glGetUniformLocation(program, "shininess"), material.shininess);
}

void render()
{
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glUseProgram(program);
	float t = float(glfwGetTime());
	glActiveTexture(GL_TEXTURE0);

	int i = 0;
	// planets
	if (p_mesh && p_mesh->vertex_array) glBindVertexArray(p_mesh->vertex_array);
	for (auto& p : solar)
	{
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, p.texture);
		glUniform1i(glGetUniformLocation(program, "TEX"), 0);	 // GL_TEXTURE0

		p.update_model(t, t_gap, t_passed, b_rotate);
		glUniformMatrix4fv(glGetUniformLocation(program, "model_matrix"), 1, GL_TRUE, p.model_matrix);
		glUniform1i(glGetUniformLocation(program, "planet_cnt"), i);


		if (p.normal_texture_path != "")
		{
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, p.normal_texture);
			glUniform1i(glGetUniformLocation(program, "TEX_norm"), 1);
			glUniform1i(glGetUniformLocation(program, "check_norm"), 1);
		}
		glDrawElements(GL_TRIANGLES, p_mesh->index_list.size(), GL_UNSIGNED_INT, nullptr);
		glUniform1i(glGetUniformLocation(program, "check_norm"), 0);
		i++;
	}

	// ring texture
	if (ring_mesh && ring_mesh->vertex_array) glBindVertexArray(ring_mesh->vertex_array);
	for (auto& ri : rings)
	{
		glUniform1i(glGetUniformLocation(program, "is_alpha"), 1);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, ri.texture);
		glUniform1i(glGetUniformLocation(program, "TEX"), 0);	 // GL_TEXTURE0

		glActiveTexture(GL_TEXTURE2);
		glBindTexture(GL_TEXTURE_2D, ri.alpha_texture);
		glUniform1i(glGetUniformLocation(program, "TEX_alpha"), 2);

		ri.update_model(t, t_gap, t_passed, b_rotate);
		glUniformMatrix4fv(glGetUniformLocation(program, "model_matrix"), 1, GL_TRUE, ri.model_matrix);
		glDrawElements(GL_TRIANGLES, ring_mesh->index_list.size(), GL_UNSIGNED_INT, nullptr);

		glUniform1i(glGetUniformLocation(program, "is_alpha"), 0);
	}
	glfwSwapBuffers(window);
}

void reshape(GLFWwindow* window, int width, int height)
{
	// set current viewport in pixels (win_x, win_y, win_width, win_height)
	// viewport: the window area that are affected by rendering 
	window_size = ivec2(width, height);
	glViewport(0, 0, width, height);
}

void print_help()
{
	printf("[help]\n");
	printf("- press ESC or 'q' to terminate the program\n");
	printf("- press F1 or 'h' to see help\n");
	printf("- press 'w' to toggle wireframe\n");
	printf("- press Home to reset camera\n");
	printf("- press Pause to pause and start\n");
	printf("- mouse left button: rotate camera\n");
	printf("- mouse right button | left shift+left button: zoom camera\n");
	printf("- mouse middle button | left ctrl+left button: pan camera\n");
	printf("- press 'd' to toggle display mode\n");
	printf("  - 0: textured shading\n");
	printf("  - 1: textured shading (except bump mapping)\n");
	printf("  - 2: shading only\n");
	printf("  - 3: texture only\n");
	printf("  - 4: texture coordinates\n");
	printf("\n");
}

void keyboard(GLFWwindow* window, int key, int scancode, int action, int mods)
{
	if (action == GLFW_PRESS)
	{
		if (key == GLFW_KEY_ESCAPE || key == GLFW_KEY_Q)	glfwSetWindowShouldClose(window, GL_TRUE);
		else if (key == GLFW_KEY_H || key == GLFW_KEY_F1)	print_help();
		else if (key == GLFW_KEY_HOME)						cam = camera();
		else if (key == GLFW_KEY_D)		{ mode = (mode + 1) % 5; printf(">> display mode %d\n", mode);}
		else if (key == GLFW_KEY_W)
		{
			b_wireframe = !b_wireframe;
			glPolygonMode(GL_FRONT_AND_BACK, b_wireframe ? GL_LINE : GL_FILL);
			printf("> using %s mode\n", b_wireframe ? "wireframe" : "solid");
		}
		else if (key == GLFW_KEY_PAUSE)
		{
			if (b_rotate) t_passed = float(glfwGetTime());
			else t_gap += float(glfwGetTime()) - t_passed;
			b_rotate = !b_rotate;
		}
	}
}

void mouse(GLFWwindow* window, int button, int action, int mods)
{
	tb.button = button;
	tb.mods = mods;
	dvec2 pos; glfwGetCursorPos(window, &pos.x, &pos.y);
	vec2 npos = cursor_to_ndc(pos, window_size);
	if (action == GLFW_PRESS)			tb.begin(npos, &cam);
	else if (action == GLFW_RELEASE)	tb.end();
}

void motion(GLFWwindow* window, double x, double y)
{
	int button = tb.button;
	int mods = tb.mods;
	if (button == GLFW_MOUSE_BUTTON_LEFT && mods == 0)
	{
		if (!tb.is_tracking()) return;
		vec2 npos = cursor_to_ndc(dvec2(x, y), window_size);
		tb.update(npos);
	}
	else if (button == GLFW_MOUSE_BUTTON_MIDDLE ||
		(button == GLFW_MOUSE_BUTTON_LEFT && (mods & GLFW_MOD_CONTROL)))
	{
		if (!tb.is_tracking()) return;
		vec2 npos = cursor_to_ndc(dvec2(x, y), window_size);
		tb.update_pan(npos);
	}
	else if (button == GLFW_MOUSE_BUTTON_RIGHT ||
		(button == GLFW_MOUSE_BUTTON_LEFT && (mods & GLFW_MOD_SHIFT)))
	{
		if (!tb.is_tracking()) return;
		vec2 npos = cursor_to_ndc(dvec2(x, y), window_size);
		tb.update_zoom(npos);
	}
}

bool user_init()
{
	// log hotkeys
	print_help();

	// init GL states
	glLineWidth(1.0f);
	glClearColor(39 / 255.0f, 40 / 255.0f, 34 / 255.0f, 1.0f);	// set clear color
	glEnable(GL_CULL_FACE);								// turn on backface culling
	glEnable(GL_DEPTH_TEST);								// turn on depth tests
	glEnable(GL_TEXTURE_2D);			// enable texturing
	glActiveTexture(GL_TEXTURE0);		// notify GL the current texture slot is 0
	glEnable(GL_BLEND);
	glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	sphere_vertices = std::move(create_sphere_vertices());
	update_index_buffer(sphere_vertices);
	p_mesh->vertex_list = sphere_vertices;
	glGenBuffers(1, &p_mesh->vertex_buffer);
	glBindBuffer(GL_ARRAY_BUFFER, p_mesh->vertex_buffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vertex) * sphere_vertices.size(), &sphere_vertices[0], GL_STATIC_DRAW);
	p_mesh->vertex_array = cg_create_vertex_array(p_mesh->vertex_buffer, p_mesh->index_buffer);

	for (auto& p : solar)
	{
		p.texture = cg_create_texture(p.texture_path.c_str(), true);
		if(p.normal_texture_path != "") p.normal_texture = cg_create_texture(p.normal_texture_path.c_str(), true);
	}

	ring_vertices = std::move(create_ring_vertices());
	ring_index_buffer(ring_vertices);
	ring_mesh->vertex_list = ring_vertices;
	glGenBuffers(1, &ring_mesh->vertex_buffer);
	glBindBuffer(GL_ARRAY_BUFFER, ring_mesh->vertex_buffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vertex) * ring_vertices.size(), &ring_vertices[0], GL_STATIC_DRAW);
	ring_mesh->vertex_array = cg_create_vertex_array(ring_mesh->vertex_buffer, ring_mesh->index_buffer);

	for (auto& ri : rings)
	{
		ri.texture = cg_create_texture(ri.texture_path.c_str(), true);
		ri.alpha_texture = cg_create_texture(ri.alpha_texture_path.c_str(), true);
	}	
	return true;
}

void user_finalize()
{
}

int main(int argc, char* argv[])
{
	// create window and initialize OpenGL extensions
	if (!(window = cg_create_window(window_name, window_size.x, window_size.y))) { glfwTerminate(); return 1; }
	if (!cg_init_extensions(window)) { glfwTerminate(); return 1; }	// version and extensions

	// initializations and validations
	if (!(program = cg_create_program(vert_shader_path, frag_shader_path))) { glfwTerminate(); return 1; }	// create and compile shaders/program
	if (!user_init()) { printf("Failed to user_init()\n"); glfwTerminate(); return 1; }					// user initialization

	// register event callbacks
	glfwSetWindowSizeCallback(window, reshape);	// callback for window resizing events
	glfwSetKeyCallback(window, keyboard);			// callback for keyboard events
	glfwSetMouseButtonCallback(window, mouse);	// callback for mouse click inputs
	glfwSetCursorPosCallback(window, motion);		// callback for mouse movement

	// enters rendering/event loop
	for (frame = 0; !glfwWindowShouldClose(window); frame++)
	{
		glfwPollEvents();	// polling and processing of events
		update();			// per-frame update
		render();			// per-frame render
	}

	// normal termination
	user_finalize();
	cg_destroy_window(window);

	return 0;
}
