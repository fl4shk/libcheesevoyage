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
#include "verilated_vcd_c.h"

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
	PIXEL_CLK
		= 25.0;
		//= 12.5;
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
	std::unique_ptr<Uint32[]> pixels;
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
		//--------
		//if (pos.x >= HALF_SIZE_2D.x) {
		//	//cnt_x = 0;
		//	pos.x = HALF_SIZE_2D.x;
		//} else {
		//	++cnt_x;
		//	if (cnt_x >= CLKS_PER_PIXEL) {
		//		++pos.x;
		//		cnt_x = 0;
		//	}
		//}
		//--------
		++pos.x;
		if (pos.x >= HALF_SIZE_2D.x) {
			pos.x = HALF_SIZE_2D.x;
		}
		//--------
		//if (cnt_x >= (HALF_SIZE_2D.x * CLKS_PER_PIXEL)) {
		//	cnt_x = HALF_SIZE_2D.x * CLKS_PER_PIXEL;
		//	pos.x = HALF_SIZE_2D.x;
		//}
		//--------
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
	ExitSim = 12,
	Lim = 13,
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
	bool _do_exit = false;
protected:		// functions
	void _update_engine_key_status() {
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
				{SnesKeyKind::ExitSim, SDLK_ESCAPE},
			})
		);
		switch (_snes_key_state) {
			enum class Always: uint32_t {
				Disabled,
				KeyUp,
				KeyDown,
			};
			case SnesKeyState::DriveValid: {
				auto my_key_up_now
				= [&](
					const SnesKeyKind& key,
					Always always=Always::Disabled
				) -> uint32_t {
					uint32_t my_key_status = 0b0u;
					switch (always) {
						case Always::Disabled:
							my_key_status = uint32_t(
								_engine_key_status.key_up_now(key)
							);
							break;
						case Always::KeyUp:
							my_key_status = 0b1u;
							break;
						case Always::KeyDown:
							my_key_status = 0b0u;
							break;
					}
					return (
						my_key_status << uint32_t(key)
					);
				};
				_top->io_rawSnesButtons_valid = true;
				//_top->io_rawSnesButtons_payload(0) = 3;
				_top->io_rawSnesButtons_payload = (
					my_key_up_now(
						SnesKeyKind::B//,
						//Always::Disabled
					)
					| my_key_up_now(
						SnesKeyKind::Y//,
						//Always::Disabled
					)
					| my_key_up_now(SnesKeyKind::Select)
					| my_key_up_now(SnesKeyKind::Start)
					| my_key_up_now(SnesKeyKind::DpadUp)
					| my_key_up_now(SnesKeyKind::DpadDown)
					| my_key_up_now(SnesKeyKind::DpadLeft)
					| my_key_up_now(SnesKeyKind::DpadRight)
					| my_key_up_now(
						SnesKeyKind::A//,
						////Always::KeyDown
						//Always::Disabled
					)
					| my_key_up_now(SnesKeyKind::X)
					| my_key_up_now(SnesKeyKind::L)
					| my_key_up_now(
						SnesKeyKind::R//,
						//Always::KeyDown
					)
					| 0xf000
				);
				if (_engine_key_status.key_down_now(SnesKeyKind::ExitSim)) {
					_do_exit = true;
					printf("Exiting...\n");
				}
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
					//printf("testificate\n");
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
		//if (
		//	pos.x < HALF_SIZE_2D.x
		//	&& pos.y < HALF_SIZE_2D.y
		//	//true
		//) {
		//	this->set(
		//		(
		//			((_top->io_phys_col_r & 0xf) << 20)
		//			+ ((_top->io_phys_col_g & 0xf) << 12)
		//			+ ((_top->io_phys_col_b & 0xf) << 4)
		//		)
		//		//_top->io_phys_col_r & 0xf,
		//		//_top->io_phys_col_g & 0xf,
		//		//_top->io_phys_col_b & 0xf
		//	);
		//}
		//_last_vsync = _top->io_phys_vsync;
		//_last_hsync = _top->io_phys_hsync;
		//--------
		//_last_vsync = _top->io_phys_vsync;
		//_last_hsync = _top->io_phys_hsync;
		//_last_visib = _top->io_misc_visib;
		//--------
		//--------
		////_handle_visib_enable();
		//_handle_pos_update();
		_handle_draw();
	}
	virtual void pre_cycle() {
		//--------
		_handle_sdl_events();
		//_handle_visib_enable();
		_handle_pos_update();
		//_handle_draw();
		_last_vsync = _top->io_phys_vsync;
		_last_hsync = _top->io_phys_hsync;
		//_last_visib = _top->io_misc_visib;
		//--------
		//_handle_visib_enable();
		//--------
		//_last_vsync = _top->io_phys_vsync;
		//_last_hsync = _top->io_phys_hsync;
		//_last_visib = _top->io_misc_visib;
		//--------
		//pos.x = _top->io_misc_hpipeC;
		//pos.y = _top->io_misc_vpipeC;
		//--------
		//if (
		//	_top->io_phys_vsync
		//	&& !_last_vsync
		//	//!_top->io_phys_vsync
		//	//&& _last_vsync
		//) {
		//	pos.y = 0;
		//	refresh();
		//}
		//if (
		//	_top->io_phys_hsync
		//	&& !_last_hsync
		//	//!_top->io_phys_hsync
		//	//&& _last_hsync
		//	&& pos.x != 0
		//) {
		//	inc_y();
		//	cnt_x = 0;
		//	pos.x = 0;
		//}
		//--------
		//--------
		//pos.x = _top->io_misc_hpipeC;
		//pos.y = _top->io_misc_vpipeC;
		//--------
		//if (
		//	!_top->io_phys_vsync
		//	&& _last_vsync
		//) {
		//	//pos.y = 0;
		//	refresh();
		//}
		//if (
		//	//////_top->io_misc_pastVisib
		//	//////&& _top->io_misc_pixelEn
		//	//////_top->io_misc_pastVisib
		//	//_top->io_misc_visib
		//	//--------
		//	//&& _top->io_misc_pixelEn
		//	//&& _top->io_misc_pastPixelEn
		//	//--------
		//	//_top->io_misc_visibPipe1
		//	////&& _top->io_misc_pixelEn
		//	//!_top->io_phys_hsync
		//	//&& !_top->io_phys_vsync
		//	////_top->io_phys_hsync
		//	////&& _top->io_phys_vsync
		//	////true
		//	//_misc_visib_timer == CLKS_PER_PIXEL
		//	_visib_enable
		//) {
		//	//pos.x = _top->io_misc_hpipeC;
		//	//pos.y = _top->io_misc_vpipeC;
		//	//--------
		//	//if (
		//	//	(_top->io_phys_col_r & 0xf) == 0xf
		//	//	&& (_top->io_phys_col_g & 0xf) == 0x8
		//	//	&& (_top->io_phys_col_b & 0xf) == 0
		//	//) {
		//	//	printout(
		//	//		//uint32_t(_top->io_misc_hpipeC & 0xff), " ",
		//	//		//uint32_t(_top->io_misc_vpipeC & 0xff), "; ",
		//	//		_top->io_phys_col_r & 0xf, " ",
		//	//		_top->io_phys_col_g & 0xf, " ",
		//	//		_top->io_phys_col_b & 0xf, "; ",
		//	//		pos, "\n"
		//	//	);
		//	//}
		//	//--------
		//	//if (
		//	//	//_top->io_misc_visib
		//	//	//_top->io_misc_pastVisib
		//	//	true
		//	//) {
		//	//	inc_x();
		//	//}
		//	if (
		//		//_top->io_misc_pastPixelEn
		//		//&& 
		//		pos.x < HALF_SIZE_2D.x
		//		&& pos.y < HALF_SIZE_2D.y
		//		//true
		//	) {
		//		this->set(
		//			(
		//				((_top->io_phys_col_r & 0xf) << 20)
		//				+ ((_top->io_phys_col_g & 0xf) << 12)
		//				+ ((_top->io_phys_col_b & 0xf) << 4)
		//			)
		//			//_top->io_phys_col_r & 0xf,
		//			//_top->io_phys_col_g & 0xf,
		//			//_top->io_phys_col_b & 0xf
		//		);
		//	}
		//	//inc_x();
		//} 
		////else {
		////	refresh();
		////}
		//--------
		//if (
		//	_top->io_misc_visib
		//) {
		//	if (
		//		!_last_visib
		//	) {
		//		//_misc_visib_timer = 0;
		//		_visib_enable = false;
		//	} else if (
		//		//_top->io_misc_pastPixelEn
		//		_top->io_misc_pixelEn
		//	) {
		//		_visib_enable = true;
		//	}
		//} else {
		//	//if (
		//	//	_misc_visib_timer + 1 < CLKS_PER_PIXEL
		//	//) {
		//	//	++_misc_visib_timer;
		//	//}
		//	_visib_enable = false;
		//}
		//--------
		//--------
		//if (
		//	//_top->io_phys_vsync
		//	//&& !_last_vsync
		//	!_top->io_phys_vsync
		//	&& _last_vsync
		//) {
		//	pos.y = 0;
		//	refresh();
		//}
		//if (
		//	//_top->io_phys_hsync
		//	//&& !_last_hsync
		//	!_top->io_phys_hsync
		//	&& _last_hsync
		//	&& pos.x != 0
		//) {
		//	inc_y();
		//	cnt_x = 0;
		//	pos.x = 0;
		//}
	}
	GEN_GETTER_BY_CON_REF(do_exit);
protected:		// variables and helper functions
	//uint32_t _misc_visib_timer = 0;
	//bool _visib_enable = false;
	//bool _last_visib = false;
	//Vec2<Uint32> _temp_cnt{0, 0};
	bool _did_first_refresh = false;
	//void _handle_visib_enable() {
	//	if (
	//		_top->io_misc_visib
	//	) {
	//		if (
	//			!_last_visib
	//		) {
	//			//_misc_visib_timer = 0;
	//			_visib_enable = false;
	//		} else if (
	//			//_top->io_misc_pastPixelEn
	//			_top->io_misc_pixelEn
	//		) {
	//			_visib_enable = true;
	//		}
	//	} else {
	//		//if (
	//		//	_misc_visib_timer + 1 < CLKS_PER_PIXEL
	//		//) {
	//		//	++_misc_visib_timer;
	//		//}
	//		_visib_enable = false;
	//	}
	//}
	void _handle_pos_update() {
		const bool old_did_first_refresh = _did_first_refresh;
		//const Uint32 prev_pos_x = pos.x;
		const Vec2<Uint32> prev_pos = pos;
		if (
			_top->io_phys_vsync
			&& !_last_vsync
			//!_top->io_phys_vsync
			//&& _last_vsync
			&& (
				pos.y >= HALF_SIZE_2D.y
				|| !_did_first_refresh
			)
		) {
			if (!_did_first_refresh) {
				_did_first_refresh = true;
			}
			printf(
				"refreshing: x, y: %u, %u\n",
				pos.x, pos.y
			);
			pos.y = 0;
			refresh();
		} 
		//else
		if (
			old_did_first_refresh
		) {
			if (
				_top->io_phys_hsync
				&& !_last_hsync
				//!_top->io_phys_hsync
				//&& _last_hsync
				&& pos.x != 0
				//&& pos.x >= HALF_SIZE_2D.x
			) {
				inc_y();
				cnt_x = 0;
				pos.x = 0;
			}
			if (
				//_visib_enable
				//_top->io_misc_pastVisib
				//_top->io_misc_visib
				_top->io_misc_visibPrev1
				&& _top->io_misc_visib
				//--------
				&& _top->io_misc_pixelEn
				//&& _top->io_misc_pastPixelEn
				//--------
			) {
				//printf("testificate\n");
				inc_x();
			}
		}
		//if (
		//	pos.y != prev_pos.y
		//) {
		//	printf(
		//		"pos.y changed: x, y: %u, %u\n",
		//		pos.x, pos.y
		//	);
		//}
	}
	void _handle_draw() {
		if (
			//////_top->io_misc_pastVisib
			//////&& _top->io_misc_pixelEn
			//////_top->io_misc_pastVisib
			//_top->io_misc_visib
			_top->io_misc_visibPrev1
			&& _top->io_misc_visib
			//--------
			&& _top->io_misc_pixelEn
			//&& _top->io_misc_pastPixelEn
			//--------
			//_top->io_misc_visibPipe1
			////&& _top->io_misc_pixelEn
			//!_top->io_phys_hsync
			//&& !_top->io_phys_vsync
			////_top->io_phys_hsync
			////&& _top->io_phys_vsync
			////true
			//_misc_visib_timer == CLKS_PER_PIXEL
			//_visib_enable
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
			//if (
			//	//_top->io_misc_visib
			//	//_top->io_misc_pastVisib
			//	true
			//) {
			//	inc_x();
			//}
			if (
				//_top->io_misc_pastPixelEn
				//&& 
				pos.x < HALF_SIZE_2D.x
				&& pos.y < HALF_SIZE_2D.y
				//true
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
			//inc_x();
		} 
		//else {
		//	refresh();
		//}
	}
public:		// functions
};

int main(int argc, char** argv) {
	if (SDL_Init(SDL_INIT_VIDEO) < 0) {
		return 1;
	}
	//std::unique_ptr<SDL_Window> win(new SDL_Window);
	//SDL_Window* win = nullptr;
	std::unique_ptr<VerilatedVcdC> trace(nullptr);//(new VerilatedVcdC);
	if (trace) {
		Verilated::traceEverOn(true);
	}
	std::unique_ptr<VGpu2dSimDut> top(new VGpu2dSimDut());
	std::unique_ptr<VerilatedContext> contextp(new VerilatedContext());
	//if (trace) {
	//}
	Vga vga(top.get());
	contextp->randReset(2);
	if (trace) {
		contextp->traceEverOn(true);
	}
	contextp->commandArgs(argc, argv);
	if (trace) {
		top->trace(trace.get(), 20);
		trace->open("sdl_test.vcd");
	}

	top->clk = 0;
	top->reset = 1;
	size_t tick_cnt = 0;
	auto end_tick = [&]() -> void {
		++tick_cnt;
		top->eval();
		if (trace) {
			trace->dump(10 * tick_cnt - 1);
			if (!top->clk) {
				trace->flush();
			}
		}
	};

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
		//top->eval();
		//trace->dump(1);
		//if (!top->clk) {
		//	trace->flush();
		//}
		end_tick();
	}

	//for (;;) 
	for 
	//while
	(
		//!vga.do_exit()
		///*;*/size_t i=0;
		;
		//i<(HALF_SIZE_2D.x * HALF_SIZE_2D.y * 40 * 2) && !vga.do_exit();
		////i<(HALF_SIZE_2D.x * HALF_SIZE_2D.y * 2 * 2) && !vga.do_exit();
		//	i<(HALF_SIZE_2D.x * HALF_SIZE_2D.y * CLKS_PER_PIXEL * 3 * 2)
			//&& 
			!vga.do_exit()
		;
		////!vga.do_exit();
		//++i
	) {
		contextp->timeInc(1);
		top->clk = !top->clk;

		if (!top->clk) {
			vga.pre_cycle();
		} else {
			vga.post_cycle();
		}

		//top->eval();
		//trace->dump(1);
		//if (!top->clk) {
		//	trace->flush();
		//}
		end_tick();
	}

	if (trace) {
		trace->close();
	}
	top->final();

	return 0;
}
