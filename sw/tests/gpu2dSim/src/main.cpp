#include <iostream>
#include <memory>
#include <string>
#include <vector>
#include <cstring>
//#include <SDL.h>
//#include <SDL_video.h>
//#include <SDL_render.h>
//#include <SDL_syswm.h>
#include "liborangepower_src/math/vec2_classes.hpp"
#include "liborangepower_src/misc/misc_includes.hpp"
#include "liborangepower_src/sdl2/sdl_rect.hpp"
#include "liborangepower_src/sdl2/sdl_video.hpp"
#include "liborangepower_src/sdl2/sdl_render.hpp"
#include "VGpu2dSimDut.h"

//using std::cout;
//using std::cin;
//using std::cerr;
namespace sdl = liborangepower::sdl;
using liborangepower::math::Vec2;


static constexpr Vec2<size_t>
	HALF_SIZE_2D{
		.x=1 << 7,
		.y=1 << 7
	},
	SIZE_2D{.x=HALF_SIZE_2D.x << 1, .y=HALF_SIZE_2D.y << 1};
class Display {
public:		// variables
	sdl::Window window;
	sdl::Renderer renderer;
	sdl::Texture texture;
	std::unique_ptr<Uint32> pixels;
	Vec2<Uint32> pos{.x=0, .y=0};
public:		// functions
	inline Display()
		: window(
			SDL_CreateWindow(
				"VGA",					// title
				SDL_WINDOWPOS_CENTERED, // x
				SDL_WINDOWPOS_CENTERED, // y
				SIZE_2D.x,				// WIDTH
				SIZE_2D.y,				// HEIGHT
										// flags
				(
					SDL_WINDOW_SHOWN
					//| SDL_WINDOW_RESIZABLE
				)
			)
		),
		renderer(
			SDL_CreateRenderer(
				window,	// window
				-1,		// index
				0		// flags
			)
		),
		texture(
			SDL_CreateTexture(
				renderer,
				SDL_PIXELFORMAT_ARGB8888,
				SDL_TEXTUREACCESS_STATIC,
				SIZE_2D.x,
				SIZE_2D.y
			)
		),
		pixels(new Uint32[SIZE_2D.x * SIZE_2D.y]),
		pos{.x=0, .y=0}
	{
		//SDL_SetWindowResizable(window, SDL_TRUE);
		//SDL_RenderSetScale(renderer, 2, 2);
		//SDL_RenderSetIntegerScale(renderer, SDL_TRUE);
		//SDL_RenderSetViewport(
		//	renderer,
		//	sdl::Rect(
		//		0, 0, // x, y
		//		WIDTH * 2, HEIGHT * 2
		//	)
		//);
		//SDL_RenderSetLogicalSize(renderer, SIZE_2D.x * 2, SIZE_2D.y * 2);
		std::memset(
			pixels.get(), 0, SIZE_2D.x * SIZE_2D.y * sizeof(Uint32)
		);
	}
	inline void set(Uint32 col) {
		//pixels.get()[pos.y * SIZE_2D.x + pos.x] = col;
		for (size_t j=0; j<2; ++j) {
			for (size_t i=0; i<2; ++i) {
				Vec2<Uint32> temp_pos;
				temp_pos.x = pos.x * 2 + i;
				temp_pos.y = pos.y * 2 + j;
				pixels.get()[temp_pos.y * SIZE_2D.x + temp_pos.x] = col;
			}
		}
	};
	inline void inc_x() {
		++pos.x;
		if (pos.x >= SIZE_2D.x) {
			pos.x = SIZE_2D.x;
		}
	};
	inline void inc_y() {
		++pos.y;
		if (pos.y >= SIZE_2D.y) {
			pos.y = SIZE_2D.y;
		}
	};

	inline void refresh() {
		SDL_UpdateTexture(
			texture, NULL, pixels.get(),
			sizeof(Uint32) * SIZE_2D.x // pitch
			//sizeof(Uint32) * SIZE_2D.x * SIZE_2D.y
		);
		SDL_RenderClear(renderer);
		SDL_RenderCopy(renderer, texture, NULL, NULL);
		SDL_RenderPresent(renderer);
		std::memset(pixels.get(), 0, SIZE_2D.x * sizeof(Uint32));
	};
	virtual void post_cycle() {
	}
	virtual void pre_cycle() {
	}
};

class Vga: public Display{
protected:	// variables
	//Vga(VBriey* top,int SIZE_2D.x, int SIZE_2D.y) : Display() {
	//	this->top = top;
	//}
	//std::unique_ptr<VGpu2dSimDut> _top;
	//std::unique_ptr<VerilatedContext> _contextp;
	VGpu2dSimDut* _top = nullptr;
	uint32_t
		_last_vsync = 0,
		_last_hsync = 0;
public:		// functions
	inline Vga(VGpu2dSimDut* s_top)
		: Display(),
		_top(s_top)
		//_top(new VGpu2dSimDut())
		//_contextp(new VerilatedContext()) 
		{
		//--------
		//_contextp->commandArgs(argc, argv);
		//--------
	}

	virtual ~Vga() {
	}

	virtual void post_cycle() {
	}

	virtual void pre_cycle(){
		//if (
		//	!_top->io_phys_vsync
		//	&& _last_vsync
		//) {
		//	//pos.y = 0;
		//	refresh();
		//}
		//if (
		//	!_top->io_phys_hsync
		//	&& _last_hsync
		//	&& pos.x != 0
		//) {
		//	inc_y();
		//	pos.x = 0;
		//}
		pos.x = _top->io_misc_hpipeC;
		pos.y = _top->io_misc_vpipeC;
		if (_top->io_misc_visib) {
			this->set(
				(_top->io_phys_col_r << 20)
				+ (_top->io_phys_col_g << 12)
				+ (_top->io_phys_col_b << 4)
			);
			//inc_x();
		}

		//_last_vsync = _top->io_phys_vsync;
		//_last_hsync = _top->io_phys_hsync;
	}
};

int main(int argc, char** argv) {
	//std::unique_ptr<SDL_Window> win(new SDL_Window);
	//SDL_Window* win = nullptr;
	std::unique_ptr<VGpu2dSimDut> top(new VGpu2dSimDut());
	std::unique_ptr<VerilatedContext> contextp(new VerilatedContext());
	Vga vga(top.get());
	contextp->randReset(2);
	contextp->traceEverOn(false);
	contextp->commandArgs(argc, argv);

	top->clk = 0;
	top->reset = 1;

	while (top->reset == !0) {
		contextp->timeInc(1);
		top->clk = !top->clk;
		if (!top->clk) {
			if (contextp->time() > 1 && contextp->time() < 10) {
				top->reset = 1;
			} else {
				top->reset = 0;
			}
		}
		top->eval();
	}

	for (;;) {
		contextp->timeInc(1);
		top->clk = !top->clk;

		if (!top->clk) {
			vga.post_cycle();
		} else {
			vga.pre_cycle();
		}

		top->eval();
	}

	top->final();

	return 0;
}
