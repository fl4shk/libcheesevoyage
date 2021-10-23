#!/usr/bin/env python3

from nmigen import *
from nmigen.hdl.rec import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.gfx.vga_ext_types import *
from libcheesevoyage.general.fifo_mods import *
from libcheesevoyage.general.container_types import *
#from bram_mod import *

VGA_TIMING_INFO_DICT \
= {
	# 640 x 480 @ 60 Hz, taken from http://www.tinyvga.com
	"640x480@60":
		VgaTimingInfo
		(
			PIXEL_CLK=25,
			HTIMING
				= VgaTiming
				(
					visib=640,
					front=16,
					sync=96,
					back=48
				),
			VTIMING
				= VgaTiming
				(
					visib=480,
					front=10,
					sync=2,
					back=33
				),
		),
	"800x600@60":
		VgaTimingInfo
		(
			PIXEL_CLK=40,
			HTIMING
				= VgaTiming
				(
					visib=800,
					front=40,
					sync=128,
					back=88
				),
			VTIMING
				= VgaTiming
				(
					visib=600,
					front=1,
					sync=4,
					back=23
				),
		),
}

#class VgaDriverBusLayout(Layout):
#	def __init__(self):
#		super().__init__ \
#		([
#			# Global VGA driving enable (white screen when off)
#			("en", unsigned(1)),
#
#			# VGA physical pins
#			("col", RgbColorLayout()),
#			("hsync", unsigned(1)),
#			("vsync", unsigned(1)),
#
#			# Pixel buffer
#			("buf", VgaDriverBufLayout()),
#
#			# Debug
#			("dbg_fifo_empty", unsigned(1)),
#			("dbg_fifo_full", unsigned(1)),
#
#			# Misc.
#			("pixel_en", unsigned(1)),
#			("next_visib", unsigned(1)),
#			("visib", unsigned(1)),
#			("past_visib", unsigned(1)),
#			("draw_pos", Vec2Layout(self.CoordShapeT())),
#			("past_draw_pos", Vec2Layout(self.CoordShapeT())),
#			("size", Vec2Layout(self.CoordShapeT())),
#			#("start_draw", unsigned(1)),
#		])
#
#	def CoordShapeT(self):
#		return unsigned(16)
#
#class VgaDriverBus(Record):
#	def __init__(self):
#		super().__init__(VgaDriverBusLayout())

class VgaDriverBus:
	def __init__(self, ColorT=RgbColor):
		# Global VGA driving enable (white screen when off)
		self.en = Signal()

		# VGA physical pins
		self.col = ColorT()
		self.hsync = Signal()
		self.vsync = Signal()

		# Pixel buffer
		self.buf = VgaDriverBuf()

		# Debug
		self.dbg_fifo_empty = Signal()
		self.dbg_fifo_full = Signal()

		# Misc.
		self.pixel_en = Signal()
		self.next_visib = Signal()
		self.visib = Signal()
		self.past_visib = Signal()
		self.draw_pos = Vec2(self.CoordShapeT())
		self.past_draw_pos = Vec2(self.CoordShapeT())
		self.size = Vec2(self.CoordShapeT())
		#self.start_draw = Signal()

	def CoordShapeT(self):
		return 16

class VgaDriver(Elaboratable):
	def __init__(self, CLK_RATE, TIMING_INFO, FIFO_SIZE, ColorT=RgbColor):
		self.__bus = VgaDriverBus(ColorT=ColorT)

		self.__CLK_RATE = CLK_RATE
		self.__TIMING_INFO = TIMING_INFO
		#self.__NUM_BUF_SCANLINES = NUM_BUF_SCANLINES
		self.__FIFO_SIZE = FIFO_SIZE
		self.__ColorT = ColorT

	def bus(self):
		return self.__bus
	def CLK_RATE(self):
		return self.__CLK_RATE
	def TIMING_INFO(self):
		return self.__TIMING_INFO
	def CPP(self):
		return self.CLK_RATE() // self.TIMING_INFO().PIXEL_CLK()
	def HTIMING(self):
		return self.TIMING_INFO().HTIMING()
	def VTIMING(self):
		return self.TIMING_INFO().VTIMING()
	#def NUM_BUF_SCANLINES(self):
	#	return self.__NUM_BUF_SCANLINES
	#def FIFO_SIZE(self):
	#	return (self.FB_SIZE().x * self.NUM_BUF_SCANLINES())
	def FIFO_SIZE(self):
		return self.__FIFO_SIZE
	def FB_SIZE(self):
		ret = Blank()
		ret.x, ret.y = self.HTIMING().visib(), self.VTIMING().visib()
		return ret
	def CLK_CNT_WIDTH(self):
		return width_from_arg(self.CPP())
	def ColorT(self):
		return self.__ColorT

	def elaborate(self, platform: str):
		#--------
		m = Module()
		#--------
		# Local variables
		loc = Blank()
		bus = self.bus()
		#--------
		fifo = m.submodules.fifo \
			= AsyncReadFifo \
			(
				ShapeT=to_shape(self.ColorT()()),
				SIZE=self.FIFO_SIZE(),
			)

		##loc.fifo_rst = Signal(reset=0b1)

		##with m.If(loc.fifo_rst):
		##	m.d.sync += loc.fifo_rst.eq(~loc.fifo_rst)
		##m.d.comb += loc.fifo.bus().rst.eq(loc.fifo_rst)
		#m.d.comb += loc.fifo.bus().rst.eq(ResetSignal())
		#--------
		loc.col = self.ColorT()()
		#--------
		# Implement the clock enable
		loc.CLK_CNT_WIDTH = self.CLK_CNT_WIDTH()
		loc.clk_cnt = Signal(loc.CLK_CNT_WIDTH)

		# Force this addition to be of width `CLK_CNT_WIDTH + 1` to
		# prevent wrap-around
		loc.clk_cnt_p_1 = Signal(loc.CLK_CNT_WIDTH + 1)
		m.d.comb += loc.clk_cnt_p_1.eq(loc.clk_cnt + 0b1)

		# Implement wrap-around for the clock counter
		with m.If(loc.clk_cnt_p_1 < self.CPP()):
			m.d.sync += loc.clk_cnt.eq(loc.clk_cnt_p_1)
		with m.Else():
			m.d.sync += loc.clk_cnt.eq(0x0)

		# Since this is an alias, use ALL_CAPS for its name.
		#bus.pixel_en = (loc.clk_cnt == 0x0)
		m.d.comb += bus.pixel_en.eq(loc.clk_cnt == 0x0)
		loc.PIXEL_EN_NEXT_CYCLE = (loc.clk_cnt_p_1 == self.CPP())
		#--------
		# Implement the State/Counter stuff
		loc.Tstate = VgaTiming.State
		loc.hsc \
		= {
			"s": Signal(width_from_len(loc.Tstate)),
			"c": Signal(self.HTIMING().COUNTER_WIDTH()),
			"next_s": Signal(width_from_len(loc.Tstate)),
		}
		loc.vsc \
		= {
			"s": Signal(width_from_len(loc.Tstate)),
			"c": Signal(self.VTIMING().COUNTER_WIDTH()),
			"next_s": Signal(width_from_len(loc.Tstate)),
		}
		#--------
		## Implement HSYNC and VSYNC logic
		with m.If(bus.pixel_en): 
			self.HTIMING().update_state_cnt(m, loc.hsc)

			with m.Switch(loc.hsc["s"]):
				with m.Case(loc.Tstate.FRONT):
					m.d.sync += bus.hsync.eq(0b1)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.SYNC):
					m.d.sync += bus.hsync.eq(0b0)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.BACK):
					m.d.sync += bus.hsync.eq(0b1)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.VISIB):
					m.d.sync += bus.hsync.eq(0b1),
					with m.If((loc.hsc["c"] + 0x1) >= self.FB_SIZE().x):
						self.VTIMING().update_state_cnt(m, loc.vsc)
					with m.Else():
						self.VTIMING().no_change_update_next_s(m, loc.vsc)

			with m.Switch(loc.vsc["s"]):
				with m.Case(loc.Tstate.FRONT):
					m.d.sync += bus.vsync.eq(0b1)
				with m.Case(loc.Tstate.SYNC):
					m.d.sync += bus.vsync.eq(0b0)
				with m.Case(loc.Tstate.BACK):
					m.d.sync += bus.vsync.eq(0b1)
				with m.Case(loc.Tstate.VISIB):
					m.d.sync += bus.vsync.eq(0b1)
		with m.Else(): # If(~bus.pixel_en):
			self.HTIMING().no_change_update_next_s(m, loc.hsc)
			self.VTIMING().no_change_update_next_s(m, loc.vsc)
		#--------
		# Implement drawing the picture

		with m.If(bus.pixel_en):
			# Visible area
			with m.If(bus.visib):
				with m.If(~bus.en):
					m.d.sync \
					+= [
						bus.col.r.eq(0xf),
						bus.col.g.eq(0xf),
						bus.col.b.eq(0xf),
					]
				with m.Else(): # If(bus.en):
					m.d.sync \
					+= [
						bus.col.eq(loc.col)
					]
			# Black border
			with m.Else(): # If (~bus.visib)
				m.d.sync \
				+= [
					bus.col.r.eq(0x0),
					bus.col.g.eq(0x0),
					bus.col.b.eq(0x0),
				]
		#--------
		# Implement VgaDriver bus to Fifo bus transaction
		m.d.comb \
		+= [
			bus.buf.can_prep.eq(~fifo.bus().full),
			fifo.bus().wr_en.eq(bus.buf.prep),
			fifo.bus().wr_data.eq(bus.buf.col),
		]
		#--------
		# Implement grabbing pixels from the FIFO.

		with m.If(bus.pixel_en & bus.visib & (~fifo.bus().empty)):
			m.d.comb += fifo.bus().rd_en.eq(0b1)
		with m.Else():
			m.d.comb += fifo.bus().rd_en.eq(0b0)
		#with m.If(loc.PIXEL_EN_NEXT_CYCLE & bus.next_visib
		#	& (~fifo.bus().empty)):
		#	m.d.sync += fifo.bus().rd_en.eq(0b1)
		#with m.Else():
		#	m.d.sync += fifo.bus().rd_en.eq(0b0)

		m.d.comb \
		+= [
			loc.col.eq(fifo.bus().rd_data),
			bus.dbg_fifo_empty.eq(fifo.bus().empty),
			bus.dbg_fifo_full.eq(fifo.bus().full),
		]
		#m.d.comb \
		#+= [
		#	loc.col.eq(bus.buf.col)
		#]
		#--------
		m.d.comb \
			+= [
				#bus.visib.eq((loc.hsc["s"] == loc.Tstate.VISIB)
				#	& (loc.vsc["s"] == loc.Tstate.VISIB)),
				bus.draw_pos.x.eq(loc.hsc["c"]),
				bus.draw_pos.y.eq(loc.vsc["c"]),
				bus.size.x.eq(self.FB_SIZE().x),
				bus.size.y.eq(self.FB_SIZE().y),
			]
		m.d.sync \
			+= [
				bus.next_visib.eq((loc.hsc["next_s"] == loc.Tstate.VISIB)
					& (loc.vsc["next_s"] == loc.Tstate.VISIB)),
				bus.visib.eq(bus.next_visib),
				bus.past_visib.eq(bus.visib),
				bus.past_draw_pos.eq(bus.draw_pos)
			]
		#--------
		return m
		#--------
