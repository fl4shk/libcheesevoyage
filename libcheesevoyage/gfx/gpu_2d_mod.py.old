#!/usr/bin/env python3

from misc_util import *
from nmigen import *
from nmigen.hdl.rec import *

#from PIL import Image

class TilemapEntryLayout(Layout):
	def __init__(self, TILE_MEM_DEPTH, NUM_LAYERS):
		super().__init__ \
		([
			("tile", unsigned(width_from_arg(TILE_MEM_DEPTH))),
			("priority", unsigned(width_from_arg(NUM_LAYERS))),
			("hflip", unsigned(1)),
			("vflip", unsigned(1)),
		])

class TilemapEntry(Record):
	def __init__(self, TILE_MEM_DEPTH, NUM_LAYERS):
		super().__init__(TilemapEntryLayout(TILE_MEM_DEPTH=TILE_MEM_DEPTH,
			NUM_LAYERS=NUM_LAYERS))

class Gpu2dBus:
	def __init__(self, RGB_CHAN_WIDTH, BPP, TILE_SIZE_2D, TILE_MEM_DEPTH,
		FB_SIZE_2D, NUM_LAYERS):
		self.__RGB_CHAN_WIDTH = RGB_CHAN_WIDTH
		self.__BPP = BPP

		self.__TILE_SIZE_2D = TILE_SIZE_2D
		assert (self.TILE_SIZE_2D().x % 8) == 0
		assert (self.TILE_SIZE_2D().y % 8) == 0

		self.__TILE_MEM_DEPTH = TILE_MEM_DEPTH

		self.__SCREEN_SIZE_2D = Blank()
		self.SCREEN_SIZE_2D().x = FB_SIZE_2D.x / self.TILE_SIZE_2D().x
		self.SCREEN_SIZE_2D().y = FB_SIZE_2D.y / self.TILE_SIZE_2D().y

		# Need an integer number of tiles displayed on screen
		assert self.SCREEN_SIZE_2D().x == int(self.SCREEN_SIZE_2D().x)
		assert self.SCREEN_SIZE_2D().y == int(self.SCREEN_SIZE_2D().y)

		self.__SCREEN_SIZE_2D.x = int(self.SCREEN_SIZE_2D().x)
		self.__SCREEN_SIZE_2D.y = int(self.SCREEN_SIZE_2D().y)

		self.__NUM_LAYERS = NUM_LAYERS

		self.pal_mem = Memory(width=self.PAL_MEM_WIDTH(),
			depth=self.PAL_MEM_DEPTH())
		self.tile_mem = Memory(width=self.TILE_MEM_WIDTH(),
			depth=self.TILE_MEM_DEPTH())
		self.map_mem = [Memory(width=self.MAP_MEM_WIDTH(),
			depth=self.MAP_MEM_DEPTH()) for _ in range(self.NUM_LAYERS())]

	def RGB_CHAN_WIDTH(self):
		return self.__RGB_CHAN_WIDTH
	def BPP(self):
		return self.__BPP
	def TILE_SIZE_2D(self):
		return self.__TILE_SIZE_2D
	def SCREEN_SIZE_2D(self):
		return self.__SCREEN_SIZE_2D
	def NUM_LAYERS(self):
		return self.__NUM_LAYERS

	def PAL_MEM_WIDTH(self):
		return (1 << (self.RGB_CHAN_WIDTH() * 3))
	def PAL_MEM_DEPTH(self):
		return (1 << self.BPP())

	def TILE_MEM_WIDTH(self):
		return self.PAL_MEM_DEPTH() * self.TILE_SIZE_2D().x
			* self.TILE_SIZE_2D().y
	def TILE_MEM_DEPTH(self):
		return self.__TILE_MEM_DEPTH

	def MAP_MEM_WIDTH(self):
		return len(self.mk_map_entry())
	def MAP_MEM_DEPTH(self):
		return (self.SCREEN_SIZE_2D().x * self.SCREEN_SIZE_2D().y)

	def mk_map_entry(self):
		return TilemapEntry(TILE_MEM_DEPTH=self.TILE_MEM_DEPTH(),
			NUM_LAYERS=self.NUM_LAYERS())

# Configurable, Paletted, Tilemap-based GPU
class Gpu2d(Elaboratable):
	def __init__(self, RGB_CHAN_WIDTH, BPP, TILE_SIZE_2D, TILE_MEM_DEPTH,
		FB_SIZE_2D, NUM_LAYERS):
		self.__bus \
			= Gpu2dBus \
			(
				RGB_CHAN_WIDTH=RGB_CHAN_WIDTH,
				BPP=BPP,
				TILE_SIZE_2D=TILE_SIZE_2D,
				TILE_MEM_DEPTH=TILE_MEM_DEPTH,
				FB_SIZE_2D=FB_SIZE_2D,
				NUM_LAYERS=NUM_LAYERS,
			)

	def bus(self):
		return self.__bus
