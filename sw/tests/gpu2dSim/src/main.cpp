#include <iostream>
#include <memory>
#include <string>
#include <vector>
#include <cstring>
//#include <SDL.h>
//#include <SDL_video.h>
//#include <SDL_render.h>
//#include <SDL_syswm.h>
#include "liborangepower_src/game_stuff/engine_key_status_class.hpp"
#include "liborangepower_src/math/vec2_classes.hpp"
#include "liborangepower_src/misc/misc_includes.hpp"
#include "liborangepower_src/sdl2/sdl.hpp"
#include "liborangepower_src/sdl2/dpi_stuff.hpp"
#include "liborangepower_src/sdl2/keyboard_stuff.hpp"
#include "liborangepower_src/sdl2/sdl_video.hpp"
#include "liborangepower_src/sdl2/sdl_render.hpp"
#include "liborangepower_src/sdl2/sdl_surface.hpp"
#include "liborangepower_src/sdl2/sdl_rect.hpp"
#include <SDL_events.h>
#include "liborangepower_src/misc/misc_output_funcs.hpp"
#include "VGpu2dSimDut.h"

//using std::cout;
//using std::cin;
//using std::cerr;
namespace sdl = liborangepower::sdl;
using liborangepower::math::Vec2;
using namespace liborangepower::misc_output;

static constexpr double
	CLK_RATE
		//= 25.0,
		//= 50.0,
		//= 75.0,
		//= 100.0,
		= 125.0,
		//= 150.0,
		//= 200.0,
	PIXEL_CLK = 25.0;
static constexpr size_t
	CLKS_PER_PIXEL = size_t(CLK_RATE / PIXEL_CLK);
	//PIXELS_PER_CLK = PIXEL_CLK / CLK_RATE;
static constexpr Vec2<size_t>
	HALF_SIZE_2D{
		//.x=1 << 7,
		//.y=1 << 7,
		//.x=1 << 6,
		//.y=1 << 5,
		.x=320,
		.y=240,
		//.x=640,
		//.y=480,
	},
	SIZE_2D{.x=HALF_SIZE_2D.x << 1, .y=HALF_SIZE_2D.y << 1};
	//SIZE_2D=HALF_SIZE_2D;

class Display {
public:		// variables
	sdl::Window window;
	sdl::Renderer renderer;
	sdl::Texture texture;
	std::unique_ptr<Uint32> pixels;
	Vec2<Uint32> pos{.x=0, .y=0};
	//Vec2<double> pos{.x=0.0, .y=0.0};
	size_t cnt_x = 0;
public:		// functions
	inline Display()
		: window(
			SDL_CreateWindow(
				"VGA",					// title
				SDL_WINDOWPOS_CENTERED, // x
				SDL_WINDOWPOS_CENTERED, // y
				SIZE_2D.x,				// WIDTH
				SIZE_2D.y,				// HEIGHT
				//HALF_SIZE_2D.x,				// WIDTH
				//HALF_SIZE_2D.y,				// HEIGHT
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
				//HALF_SIZE_2D.x,
				//HALF_SIZE_2D.y
			)
		),
		pixels(new Uint32[SIZE_2D.x * SIZE_2D.y]),
		//pixels(new Uint32[HALF_SIZE_2D.x * HALF_SIZE_2D.y]),
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
			pixels.get(),
			0,
			SIZE_2D.x * SIZE_2D.y * sizeof(Uint32)
			//HALF_SIZE_2D.x * HALF_SIZE_2D.y * sizeof(Uint32)
		);
	}
	inline void set(
		Uint32 col
		//Uint32 col_r,
		//Uint32 col_g,
		//Uint32 col_b
	) {
		//pixels.get()[pos.y * SIZE_2D.x + pos.x] = col;
		//if (
		//	//((col >> 20) & 0xf) == 0xf
		//	//&& ((col >> 12) & 0xf) == 0x8
		//	//&& ((col >> 4) & 0xf) == 0x0
		//	col_r == 0xf
		//	&& col_g == 0x8
		//	&& col_b == 0x0
		//) {
		//	printout(
		//		"Found orange: ",
		//		pos, "; ",
		//		std::hex,
		//			col, "; ",
		//			"{", col_r, " ", col_g, " ", col_b, "}",
		//		std::dec,
		//		"\n"
		//	);
		//}
		//pixels.get()[
		//	uint32_t(pos.y) * HALF_SIZE_2D.x
		//	+ uint32_t(pos.x)
		//] = col;
		//pixels.get()[
		//	uint32_t(pos.y) * HALF_SIZE_2D.x + uint32_t(pos.x)
		//] = col;
		//pixels.get()[
		//	uint32_t(pos.y) * SIZE_2D.x + uint32_t(pos.x)
		//] = col;
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
		//++pos.x;
		//pos.x += PIXELS_PER_CLK;
		//if (pos.x >= HALF_SIZE_2D.x) {
		//	pos.x = HALF_SIZE_2D.x;
		//}

		//++cnt_x;
		//if ((cnt_x % CLKS_PER_PIXEL) == 0) {
		//	++pos.x;
		//}
		//if (pos.x >= HALF_SIZE_2D.x) {
		//	cnt_x = HALF_SIZE_2D.x * CLKS_PER_PIXEL;
		//	pos.x = HALF_SIZE_2D.x;
		//}
		if (pos.x >= HALF_SIZE_2D.x) {
			//cnt_x = 0;
			pos.x = HALF_SIZE_2D.x;
		} else {
			++cnt_x;
			if (cnt_x >= CLKS_PER_PIXEL) {
				++pos.x;
				cnt_x = 0;
			}
		}

		//if (cnt_x >= (HALF_SIZE_2D.x * CLKS_PER_PIXEL)) {
		//	cnt_x = HALF_SIZE_2D.x * CLKS_PER_PIXEL;
		//	pos.x = HALF_SIZE_2D.x;
		//}
	};
	inline void inc_y() {
		++pos.y;
		if (pos.y >= HALF_SIZE_2D.y) {
			pos.y = HALF_SIZE_2D.y;
		}
	};

	inline void refresh() {
		//--------
		SDL_UpdateTexture(
			texture,
			NULL,
			pixels.get(),
			sizeof(Uint32) * SIZE_2D.x // pitch
			//sizeof(Uint32) * HALF_SIZE_2D.x // pitch
			//sizeof(Uint32) * SIZE_2D.x * SIZE_2D.y
		);
		SDL_RenderClear(renderer);
		SDL_RenderCopy(renderer, texture, NULL, NULL);
		SDL_RenderPresent(renderer);
		//std::memset(pixels.get(), 0, HALF_SIZE_2D.x * sizeof(Uint32));
		std::memset(
			pixels.get(), 0,
			SIZE_2D.x * SIZE_2D.y * sizeof(Uint32)
			//HALF_SIZE_2D.x * HALF_SIZE_2D.y * sizeof(Uint32)
		);
		//--------
		//--------
	};
	virtual void post_cycle() {
	}
	virtual void pre_cycle() {
	}
};

enum class SnesKeyKind: uint32_t {
	B = 0,
	Y = 1,
	Select = 2,
	Start = 3,
	DpadUp = 4,
	DpadDown = 5,
	DpadLeft = 6,
	DpadRight = 7,
	A = 8,
	X = 9,
	L = 10,
	R = 11,
	Lim = 12,
};

class Vga: public Display{
protected:	// variables
	//Vga(VBriey* top,int SIZE_2D.x, int SIZE_2D.y) : Display() {
	//	this->top = top;
	//}
	//std::unique_ptr<VGpu2dSimDut> _top;
	//std::unique_ptr<VerilatedContext> _contextp;
	VGpu2dSimDut* _top = nullptr;
	sdl::KeyStatusUmap _key_status_umap;
	liborangepower::game::EngineKeyStatus _engine_key_status;
	uint32_t
		_last_vsync = 0,
		_last_hsync = 0;
	enum class SnesKeyState: uint32_t {
		DriveValid,
		WaitFire,
	};
	SnesKeyState _snes_key_state = SnesKeyState::DriveValid;
protected:		// functions
	void _update_engine_key_status() {
		switch (_snes_key_state) {
			case SnesKeyState::DriveValid: {
				_engine_key_status.update(
					_key_status_umap,
					sdl::EngineKeycUmap<SnesKeyKind>({
						{SnesKeyKind::B, SDLK_k},
						{SnesKeyKind::Y, SDLK_j},
						{SnesKeyKind::Select, SDLK_a},
						{SnesKeyKind::Start, SDLK_RETURN},
						{SnesKeyKind::DpadUp, SDLK_e},
						{SnesKeyKind::DpadDown, SDLK_d},
						{SnesKeyKind::DpadLeft, SDLK_s},
						{SnesKeyKind::DpadRight, SDLK_f},
						{SnesKeyKind::A, SDLK_l},
						{SnesKeyKind::X, SDLK_i},
						{SnesKeyKind::L, SDLK_o},
						{SnesKeyKind::R, SDLK_p},
					})
				);
				_top->io_rawSnesButtons_valid = true;
				//_top->io_rawSnesButtons_payload(0) = 3;
				_top->io_rawSnesButtons_payload = (
					(
						_engine_key_status.key_up_now(SnesKeyKind::B)
						<< uint32_t(SnesKeyKind::B)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::Y)
						<< uint32_t(SnesKeyKind::Y)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::Select)
						<< uint32_t(SnesKeyKind::Select)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::Start)
						<< uint32_t(SnesKeyKind::Start)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::DpadUp)
						<< uint32_t(SnesKeyKind::DpadUp)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::DpadDown)
						<< uint32_t(SnesKeyKind::DpadDown)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::DpadLeft)
						<< uint32_t(SnesKeyKind::DpadLeft)
					) | (
						_engine_key_status.key_up_now(
							SnesKeyKind::DpadRight
						)
						<< uint32_t(SnesKeyKind::DpadRight)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::A)
						<< uint32_t(SnesKeyKind::A)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::X)
						<< uint32_t(SnesKeyKind::X)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::L)
						<< uint32_t(SnesKeyKind::L)
					) | (
						_engine_key_status.key_up_now(SnesKeyKind::R)
						<< uint32_t(SnesKeyKind::R)
					) | (
						0xf000
					)
				);
				//printf("0x%x\n", uint32_t(_top->io_rawSnesButtons_payload));
				_snes_key_state = SnesKeyState::WaitFire;
			}
				break;
			case SnesKeyState::WaitFire: {
				if (
					//_top->io_rawSnesButtons_valid
					//&& 
					_top->io_rawSnesButtons_ready
				) {
					//printf("_top->io_rawSnesButtons_ready == true\n");
					_top->io_rawSnesButtons_valid = false;
					_snes_key_state = SnesKeyState::DriveValid;
				}
			}
				break;
			default:
				break;
		}
	}
	void _handle_sdl_events() {
		bool ksm_perf_total_backup = true;
		SDL_Event e;

		while (SDL_PollEvent(&e) != 0) {
			if (e.type == SDL_QUIT) {
			} else if (
				liborangepower::sdl::handle_key_events(
					e,
					_key_status_umap, 
					ksm_perf_total_backup
				)
			) {
			}
		}
		_update_engine_key_status();
	}
public:		// functions
	inline Vga(VGpu2dSimDut* s_top)
		: Display(),
		_top(s_top),
		//_top(new VGpu2dSimDut())
		//_contextp(new VerilatedContext()) 
		_engine_key_status(int32_t(SnesKeyKind::Lim))
		{
		//--------
		//_contextp->commandArgs(argc, argv);
		//--------
	}

	virtual ~Vga() {
	}

	virtual void post_cycle() {
	}

	virtual void pre_cycle() {
		_handle_sdl_events();
		if (
			!_top->io_phys_vsync
			&& _last_vsync
		) {
			pos.y = 0;
			refresh();
		}
		if (
			!_top->io_phys_hsync
			&& _last_hsync
			&& pos.x != 0
		) {
			inc_y();
			cnt_x = 0;
			pos.x = 0;
		}
		//pos.x = _top->io_misc_hpipeC;
		//pos.y = _top->io_misc_vpipeC;
		//if (
		//	!_top->io_phys_vsync
		//	&& _last_vsync
		//) {
		//	//pos.y = 0;
		//	refresh();
		//}
		if (
			////_top->io_misc_pastVisib
			////&& _top->io_misc_pixelEn
			////_top->io_misc_pastVisib
			_top->io_misc_visib
			//&& _top->io_misc_pixelEn
			//!_top->io_phys_hsync
			//&& !_top->io_phys_vsync
			//_top->io_phys_hsync
			//&& _top->io_phys_vsync
		) {
			//pos.x = _top->io_misc_hpipeC;
			//pos.y = _top->io_misc_vpipeC;
			//--------
			//if (
			//	(_top->io_phys_col_r & 0xf) == 0xf
			//	&& (_top->io_phys_col_g & 0xf) == 0x8
			//	&& (_top->io_phys_col_b & 0xf) == 0
			//) {
			//	printout(
			//		//uint32_t(_top->io_misc_hpipeC & 0xff), " ",
			//		//uint32_t(_top->io_misc_vpipeC & 0xff), "; ",
			//		_top->io_phys_col_r & 0xf, " ",
			//		_top->io_phys_col_g & 0xf, " ",
			//		_top->io_phys_col_b & 0xf, "; ",
			//		pos, "\n"
			//	);
			//}
			//--------
			if (
				pos.x < HALF_SIZE_2D.x
				&& pos.y < HALF_SIZE_2D.y
			) {
				this->set(
					(
						((_top->io_phys_col_r & 0xf) << 20)
						+ ((_top->io_phys_col_g & 0xf) << 12)
						+ ((_top->io_phys_col_b & 0xf) << 4)
					)
					//_top->io_phys_col_r & 0xf,
					//_top->io_phys_col_g & 0xf,
					//_top->io_phys_col_b & 0xf
				);
			}
			inc_x();
		} 
		//else {
		//	refresh();
		//}

		_last_vsync = _top->io_phys_vsync;
		_last_hsync = _top->io_phys_hsync;
	}
};

int main(int argc, char** argv) {
	if (SDL_Init(SDL_INIT_VIDEO) < 0) {
		return 1;
	}
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
			vga.pre_cycle();
		} else {
			vga.post_cycle();
		}

		top->eval();
	}

	top->final();

	return 0;
}
