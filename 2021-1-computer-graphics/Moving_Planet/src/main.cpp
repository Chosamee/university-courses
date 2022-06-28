#include "cgmath.h"		// slee's simple math library
#include "cgut.h"		// slee's OpenGL utility
#include "trackball.h"	// virtual trackball
#include <iostream>

//*************************************
// global constants
static const char*	window_name = "moving-planets";
static const char* vert_shader_path = "../bin/shaders/trackball.vert";
static const char* frag_shader_path = "../bin/shaders/trackball.frag";

//*************************************
// window objects
GLFWwindow*	window = nullptr;
ivec2		window_size = cg_scale_by_dpi(1280, 720); // initial window size

//*************************************
//constant, type
static const uint longitude = 72;
static const uint latitude = 36;
static const float unit_theta = PI / latitude;
static const float unit_phi = 2 * PI / longitude;
static const float radius[9] = { 2.25f, 0.383f, 0.950f, 1.f, 0.532f, 1.97f, 0.914f, 0.98f, 0.87f }; // radius
static const float rot_speed[9] = {1.9969f, 0.03f, 0.018f, 0.4651f, 0.2411f, 1.6f, 2.87f, 2.59f, 2.68f}; //rotation speed
static const float rev_speed[9] = {0, 47.87f, 35.02f, 29.79f, 24.13f, 13.07f, 9.67f, 6.84f, 5.48f}; //revolution speed
static const float au[9] = {0.f, 0.39f, 0.7f, 1.f, 1.52f, 2.f, 2.5f, 3.f, 3.5f};
static const float r = 10.0f; //unit radius
static float t_passed = 0.0f;
static float t_gap = 0.0f;
bool b_rotate = true;
int b_color_type = 0;
// build the model matrix for oscillating scale
mat4 model_matrix = mat4::scale(1, 1, 1);

//*************************************
// holder of vertices and indices of a unit circle
std::vector<vertex>	sphere_vertices;
bool	b_wireframe = false;


//*************************************
// OpenGL objects
GLuint	program	= 0;	// ID holder for GPU program

//*************************************
// global variables
int		frame = 0;				// index of rendering frames

//*************************************
// scene objects
mesh*		pMesh = new mesh();
camera		cam;
trackball	tb;

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
	pMesh->index_list = indices;
	glGenBuffers(1, &pMesh->index_buffer);
	glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, pMesh->index_buffer);
	glBufferData(GL_ELEMENT_ARRAY_BUFFER, sizeof(uint) * indices.size(), &indices[0], GL_STATIC_DRAW);
}

void update()
{
	// update projection matrix
	cam.aspect = window_size.x/float(window_size.y);
	cam.projection_matrix = mat4::perspective( cam.fovy, cam.aspect, cam.dnear, cam.dfar );



	// update uniform variables in vertex/fragment shaders
	GLint uloc;
	uloc = glGetUniformLocation(program, "b_color_type");			if(uloc>-1) glUniform1i(uloc, b_color_type);
	uloc = glGetUniformLocation( program, "view_matrix" );			if(uloc>-1) glUniformMatrix4fv( uloc, 1, GL_TRUE, cam.view_matrix );
	uloc = glGetUniformLocation( program, "projection_matrix" );	if(uloc>-1) glUniformMatrix4fv( uloc, 1, GL_TRUE, cam.projection_matrix );
	uloc = glGetUniformLocation( program, "model_matrix" );			if(uloc>-1) glUniformMatrix4fv( uloc, 1, GL_TRUE, model_matrix );
}

void render()
{
	// clear screen (with background color) and clear depth buffer
	glClear( GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT );

	// notify GL that we use our own program
	glUseProgram( program );

	// bind vertex array object
	if(pMesh&&pMesh->vertex_array) glBindVertexArray( pMesh->vertex_array );

	// render vertices: trigger shader programs to process vertex data
	float t = float(glfwGetTime());


	for (int k = 0; k < 9; k++) {
		float move = au[k]*100.f;
		float rot_theta = (t_passed - t_gap) * rot_speed[k];
		float rev_theta = (t_passed - t_gap) * rev_speed[k]/30;
		//array로 주면 어떨까?
		if (b_rotate)
		{
			rot_theta = (t - t_gap) * rot_speed[k];
			rev_theta = (t - t_gap) * rev_speed[k] / 30;
		}
		// build the model matrix
		mat4 model_matrix = 
			mat4::translate(move * cos(rev_theta), -abs(move) * sin(rev_theta), 0.0f) *
			mat4::translate(cam.at) *
			mat4::rotate(vec3(0, 0, 1), -rot_theta) *
			mat4::translate(-cam.at) *
			mat4::scale(vec3(radius[k], radius[k], radius[k]));

		// update the uniform model matrix and render
		glUniformMatrix4fv(glGetUniformLocation(program, "model_matrix"), 1, GL_TRUE, model_matrix);
		// render vertices: trigger shader programs to process vertex data
		glDrawElements(GL_TRIANGLES, pMesh->index_list.size(), GL_UNSIGNED_INT, nullptr);
	}
	// swap front and back buffers, and display to screen
	glfwSwapBuffers( window );
}

void reshape( GLFWwindow* window, int width, int height )
{
	// set current viewport in pixels (win_x, win_y, win_width, win_height)
	// viewport: the window area that are affected by rendering 
	window_size = ivec2(width,height);
	glViewport( 0, 0, width, height );
}

void print_help()
{
	printf( "[help]\n" );
	printf( "- press ESC or 'q' to terminate the program\n" );
	printf( "- press F1 or 'h' to see help\n" );
	printf( "- press 'w' to toggle wireframe\n");
	printf( "- press Home to reset camera\n" );
	printf( "- press Pause to pause and start\n");
	printf("- press 'd' to toggle(tc.xy, 0) > (tc.xxx) > (tc.yyy)\n");
	printf("- mouse left button: rotate camera\n");
	printf("- mouse right button | left shift+left button: zoom camera\n");
	printf("- mouse middle button | left ctrl+left button: pan camera\n");
	printf( "\n" );
}

void keyboard( GLFWwindow* window, int key, int scancode, int action, int mods )
{
	if(action==GLFW_PRESS)
	{
		if(key==GLFW_KEY_ESCAPE||key==GLFW_KEY_Q)	glfwSetWindowShouldClose( window, GL_TRUE );
		else if(key==GLFW_KEY_H||key==GLFW_KEY_F1)	print_help();
		else if(key==GLFW_KEY_HOME)					cam=camera();
		else if (key == GLFW_KEY_W)
		{
			b_wireframe = !b_wireframe;
			glPolygonMode(GL_FRONT_AND_BACK, b_wireframe ? GL_LINE : GL_FILL);
			printf("> using %s mode\n", b_wireframe ? "wireframe" : "solid");
		}
		else if(key==GLFW_KEY_PAUSE)
		{
			if (b_rotate) t_passed = float(glfwGetTime());
			else t_gap += float(glfwGetTime()) - t_passed;
			b_rotate = !b_rotate;
		}
		else if (key == GLFW_KEY_D)
		{
			b_color_type = (b_color_type + 1) % 3;
			if (b_color_type == 0) printf("> using (texcoord.xy, 0) as color\n");
			else if (b_color_type == 1) printf("> using (texcoord.xxx, 0) as color\n");
			else if (b_color_type == 2) printf("> using (texcoord.yyy, 0) as color\n");
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
	glClearColor( 39/255.0f, 40/255.0f, 34/255.0f, 1.0f );	// set clear color
	glEnable( GL_CULL_FACE );								// turn on backface culling
	glEnable( GL_DEPTH_TEST );								// turn on depth tests

	// define position of vertices

	sphere_vertices = std::move(create_sphere_vertices());

	// create index buffer
	update_index_buffer(sphere_vertices);

	// load the mesh
	pMesh->vertex_list = sphere_vertices;
	glGenBuffers(1, &pMesh->vertex_buffer);
	glBindBuffer(GL_ARRAY_BUFFER, pMesh->vertex_buffer);
	glBufferData(GL_ARRAY_BUFFER, sizeof(vertex) * sphere_vertices.size(), &sphere_vertices[0], GL_STATIC_DRAW);
	pMesh->vertex_array = cg_create_vertex_array(pMesh->vertex_buffer, pMesh->index_buffer);

	return true;
}

void user_finalize()
{
}

int main( int argc, char* argv[] )
{
	// create window and initialize OpenGL extensions
	if(!(window = cg_create_window( window_name, window_size.x, window_size.y ))){ glfwTerminate(); return 1; }
	if(!cg_init_extensions( window )){ glfwTerminate(); return 1; }	// version and extensions

	// initializations and validations
	if(!(program=cg_create_program( vert_shader_path, frag_shader_path ))){ glfwTerminate(); return 1; }	// create and compile shaders/program
	if(!user_init()){ printf( "Failed to user_init()\n" ); glfwTerminate(); return 1; }					// user initialization

	// register event callbacks
	glfwSetWindowSizeCallback( window, reshape );	// callback for window resizing events
    glfwSetKeyCallback( window, keyboard );			// callback for keyboard events
	glfwSetMouseButtonCallback( window, mouse );	// callback for mouse click inputs
	glfwSetCursorPosCallback( window, motion );		// callback for mouse movement

	// enters rendering/event loop
	for( frame=0; !glfwWindowShouldClose(window); frame++ )
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
