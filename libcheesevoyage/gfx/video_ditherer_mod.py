#!/usr/bin/env python3

from nmigen import *
from nmigen.hdl.rec import *

from ..misc_util import *
from ..general.container_types import *
from .vga_ext_types import *

class VideoDithererBus:
	def __init__(self, FB_SIZE, CHAN_WIDTH):
		self.__FB_SIZE, self.__CHAN_WIDTH = FB_SIZE, CHAN_WIDTH

		self.en = Signal()

		self.col_out = RgbColor(CHAN_WIDTH=self.OUT_CHAN_WIDTH())
		self.col_in = RgbColor(CHAN_WIDTH=self.CHAN_WIDTH())

		self.frame_cnt = Signal(self.DITHER_DELTA_WIDTH())
		self.next_pos = self.CoordT()
		self.pos = self.CoordT()
		self.past_pos = self.CoordT()

		# Need a channel width of at least 3 for dithering to work (though
		# if it *were* 3, it probably wouldn't work very well!)
		assert CHAN_WIDTH > self.CHAN_WIDTH_DELTA()

	def DITHER_DELTA_WIDTH(self):
		return width_from_arg(4)
	def FB_SIZE(self):
		return self.__FB_SIZE
	def CHAN_WIDTH(self):
		return self.__CHAN_WIDTH
	def OUT_CHAN_WIDTH(self):
		return (self.CHAN_WIDTH() - self.CHAN_WIDTH_DELTA())
	def CHAN_WIDTH_DELTA(self):
		return 2
	def CoordT(self):
		return Vec2(16)

# Temporally and spatially dither a CHAN_WIDTH color down to CHAN_WIDTH - 2
class VideoDitherer(Elaboratable):
	def __init__(self, FB_SIZE,
		CHAN_WIDTH=RgbColor.DEF_CHAN_WIDTH() + 2):

		self.__bus = VideoDithererBus(FB_SIZE=FB_SIZE,
			CHAN_WIDTH=CHAN_WIDTH)

		#self.__frame = Signal(width_from_arg(4))

	def bus(self):
		return self.__bus
	def FB_SIZE(self):
		return self.bus().FB_SIZE()
	def CHAN_WIDTH(self):
		return self.bus().CHAN_WIDTH()
	def OUT_CHAN_WIDTH(self):
		return self.bus().OUT_CHAN_WIDTH()

	# Dithering pattern
	def __PAT_VAL(self, val):
		return Const(val, self.bus().DITHER_DELTA_WIDTH())
	def PATTERN(self):
		return \
		Array \
		([
			Array
			([
				Array([self.__PAT_VAL(0), self.__PAT_VAL(1)]),
				Array([self.__PAT_VAL(3), self.__PAT_VAL(2)]),
			]),

			Array
			([
				Array([self.__PAT_VAL(1), self.__PAT_VAL(0)]),
				Array([self.__PAT_VAL(2), self.__PAT_VAL(3)]),
			]),

			Array
			([
				Array([self.__PAT_VAL(3), self.__PAT_VAL(2)]),
				Array([self.__PAT_VAL(0), self.__PAT_VAL(1)]),
			]),

			Array
			([
				Array([self.__PAT_VAL(2), self.__PAT_VAL(3)]),
				Array([self.__PAT_VAL(1), self.__PAT_VAL(0)]),
			]),
		])

	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		loc = Blank()
		#--------
		loc.past_col_out = RgbColor(CHAN_WIDTH=bus.OUT_CHAN_WIDTH())
		with m.If(bus.en):
			m.d.sync \
			+= [
				loc.past_col_out.eq(bus.col_out),
				bus.past_pos.eq(bus.pos),
			]

			# Update `bus.pos` and `bus.frame_cnt`
			loc.POS_PLUS_1 = {"x": bus.pos.x + 0x1, "y": bus.pos.y + 0x1}
			with m.If(loc.POS_PLUS_1["x"] < bus.FB_SIZE().x):
				#m.d.sync += bus.pos.x.eq(loc.POS_PLUS_1["x"])
				m.d.comb \
				+= [
					bus.next_pos.x.eq(loc.POS_PLUS_1["x"]),
					bus.next_pos.y.eq(bus.pos.y),
				]
			with m.Else(): # If(loc.POS_PLUS_1["x"] >= bus.FB_SIZE().x):
				#m.d.sync += bus.pos.x.eq(0x0)
				m.d.comb += bus.next_pos.x.eq(0x0),
				with m.If(loc.POS_PLUS_1["y"] < bus.FB_SIZE().y):
					m.d.comb += bus.next_pos.y.eq(loc.POS_PLUS_1["y"])
				with m.Else():
					# If(loc.POS_PLUS_1["y"] >= bus.FB_SIZE().y):
					m.d.comb += bus.next_pos.y.eq(0x0)

					# This wraps around to zero automatically due to
					# modular arithmetic, so we don't need another mux just
					# for this.
					m.d.sync += bus.frame_cnt.eq(bus.frame_cnt + 0x1)
			m.d.sync \
			+= [
				bus.pos.eq(bus.next_pos)
			]

			# Perform dithering
			loc.dicol = RgbColor(CHAN_WIDTH=bus.CHAN_WIDTH())
			loc.CHAN_DELTA \
				= self.PATTERN()[bus.frame_cnt][Value.cast(bus.pos.y[0])] \
					[Value.cast(bus.pos.x[0])]
			loc.col_in_plus_delta \
				= RgbColor(CHAN_WIDTH=bus.CHAN_WIDTH() + 1)

			m.d.comb \
			+= [
				loc.col_in_plus_delta.r.eq(bus.col_in.r + loc.CHAN_DELTA),
				loc.col_in_plus_delta.g.eq(bus.col_in.g + loc.CHAN_DELTA),
				loc.col_in_plus_delta.b.eq(bus.col_in.b + loc.CHAN_DELTA),
			]

			# Saturating arithmetic to prevent an artifact
			with m.If(loc.col_in_plus_delta.r
				[len(loc.col_in_plus_delta.r) - 1]):
				m.d.comb += loc.dicol.r.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.r.eq(loc.col_in_plus_delta.r
					[:len(loc.dicol.r)])
			with m.If(loc.col_in_plus_delta.g
				[len(loc.col_in_plus_delta.g) - 1]):
				m.d.comb += loc.dicol.g.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.g.eq(loc.col_in_plus_delta.g
					[:len(loc.dicol.g)])
			with m.If(loc.col_in_plus_delta.b
				[len(loc.col_in_plus_delta.b) - 1]):
				m.d.comb += loc.dicol.b.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.b.eq(loc.col_in_plus_delta.b
					[:len(loc.dicol.b)])


			m.d.comb \
			+= [
				#loc.dicol.r.eq(loc.COL_IN_PLUS_DELTA["r"]),
				#loc.dicol.g.eq(loc.COL_IN_PLUS_DELTA["g"]),
				#loc.dicol.b.eq(loc.COL_IN_PLUS_DELTA["b"]),

				bus.col_out.r.eq(loc.dicol.r[bus.CHAN_WIDTH_DELTA():]),
				bus.col_out.g.eq(loc.dicol.g[bus.CHAN_WIDTH_DELTA():]),
				bus.col_out.b.eq(loc.dicol.b[bus.CHAN_WIDTH_DELTA():]),
			]

		with m.Else(): # If(~bus.en):
			m.d.comb \
			+= [
				bus.col_out.eq(loc.past_col_out),
				bus.next_pos.eq(bus.pos),
			]
		#--------
		return m
		#--------
