#!/usr/bin/env python3

from amaranth import *
from amaranth.hdl.rec import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.gfx.vga_ext_types import *
from libcheesevoyage.general.fifo_mods import *
from libcheesevoyage.general.container_types import *
#from bram_mod import *

VGA_TIMING_INFO_DICT = {
	# 640 x 480 @ 60 Hz, taken from http://www.tinyvga.com
	"640x480@60": VgaTimingInfo(
		PIXEL_CLK=25,
		HTIMING=VgaTiming(
			visib=640,
			front=16,
			sync=96,
			back=48
		),
		VTIMING=VgaTiming(
			visib=480,
			front=10,
			sync=2,
			back=33
		),
	),
	"800x600@60": VgaTimingInfo(
		PIXEL_CLK=40,
		HTIMING=VgaTiming(
			visib=800,
			front=40,
			sync=128,
			back=88
		),
		VTIMING=VgaTiming(
			visib=600,
			front=1,
			sync=4,
			back=23
		),
	),
	# This is an XGA VGA signal. It didn't work with my monitor.
	#"1024x768@60":
	#	VgaTimingInfo
	#	(
	#		PIXEL_CLK=65,
	#		HTIMING
	#			=VgaTiming
	#			(
	#				visib=1024,
	#				front=24,
	#				sync=136,
	#				back=160
	#			),
	#		VTIMING
	#			=VgaTiming
	#			(
	#				visib=768,
	#				front=3,
	#				sync=6,
	#				back=29
	#			),
	#	),
	"1280x800@60": VgaTimingInfo(
		PIXEL_CLK=83.46,
		HTIMING=VgaTiming(
			visib=1280,
			front=64,
			sync=136,
			back=200
		),
		VTIMING=VgaTiming(
			visib=800,
			front=1,
			sync=3,
			back=24
		),
	),
}

class VgaDriverBus:
	def __init__(
		self,
		#ColorT=RgbColor
		col_shape=RgbColorLayt,
		COL_CHAN_WIDTH=RgbColorLayt.DEF_CHAN_WIDTH()
	):
		#--------
		inp_shape = {}
		outp_shape = {}
		#--------
		# Global VGA driving enable (white screen when off)
		inp_shape["en"] = 1

		# VGA physical pins
		#self.outp_col = ColorT()
		outp_shape["col"] = col_shape(COL_CHAN_WIDTH)
		outp_shape["hsync"] = 1
		outp_shape["vsync"] = 1

		# Pixel buffer
		inp_shape["buf"] = VgaDriverBufInpInfo(
			#ColorT().CHAN_WIDTH()
			CHAN_WIDTH=COL_CHAN_WIDTH
		).shape
		outp_shape["buf"] = VgaDriverBufOutpInfo().shape

		# Debug
		outp_shape["dbg_fifo_empty"] = 1
		outp_shape["dbg_fifo_full"] = 1

		# Misc.
		outp_shape["pixel_en"] = 1
		outp_shape["next_visib"] = 1
		outp_shape["visib"] = 1
		outp_shape["past_visib"] = 1
		outp_shape["draw_pos"] = Vec2Layt(self.CoordElemKindT())
		outp_shape["past_draw_pos"] = Vec2Layt(self.CoordElemKindT())
		outp_shape["size"] = Vec2Layt(self.CoordElemKindT())
		#self.start_draw = Signal()
		#--------
		self.inp = Splitrec(inp_shape)
		self.outp = Splitrec(outp_shape)
		#--------

	def CoordElemKindT(self):
		return 16

class VgaDriver(Elaboratable):
	def __init__(
		self,
		CLK_RATE,
		TIMING_INFO,
		FIFO_SIZE,
		#ColorT=RgbColor,
		col_shape=RgbColorLayt,
		COL_CHAN_WIDTH=RgbColorLayt.DEF_CHAN_WIDTH(),
	):
		self.__bus = VgaDriverBus(ColorT=ColorT)

		self.__CLK_RATE = CLK_RATE
		self.__TIMING_INFO = TIMING_INFO
		#self.__NUM_BUF_SCANLINES = NUM_BUF_SCANLINES
		self.__FIFO_SIZE = FIFO_SIZE
		#self.__ColorT = ColorT
		self.__col_shape = col_shape
		self.__COL_CHAN_WIDTH = COL_CHAN_WIDTH

	def bus(self):
		return self.__bus
	def CLK_RATE(self):
		return self.__CLK_RATE
	def TIMING_INFO(self):
		return self.__TIMING_INFO
	def CPP(self):
		return math.trunc(self.CLK_RATE()
			// self.TIMING_INFO().PIXEL_CLK())
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
	#def ColorT(self):
	#	return self.__ColorT
	def col_shape(self):
		return self.__col_shape
	def COL_CHAN_WIDTH(self):
		return self.__COL_CHAN_WIDTH

	def elaborate(self, platform: str):
		#--------
		m = Module()
		#--------
		# Local variables
		loc = Blank()
		bus = self.bus()
		inp = bus.inp
		outp = bus.outp
		#--------
		fifo = m.submodules.fifo = AsyncReadFifo(
			ShapeT=to_shape(
				#self.ColorT()()
				View(self.col_shape(self.COL_CHAN_WIDTH()))
			),
			SIZE=self.FIFO_SIZE(),
		)
		fifo_inp = fifo.bus().inp
		fifo_outp = fifo.bus().outp

		##loc.fifo_rst = Signal(reset=0b1)

		##with m.If(loc.fifo_rst):
		##	m.d.sync += loc.fifo_rst.eq(~loc.fifo_rst)
		##m.d.comb += loc.fifo_inp.rst.eq(loc.fifo_rst)
		#m.d.comb += loc.fifo_inp.rst.eq(ResetSignal())
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
		#outp.pixel_en = (loc.clk_cnt == 0x0)
		m.d.comb += outp.pixel_en.eq(loc.clk_cnt == 0x0)
		loc.PIXEL_EN_NEXT_CYCLE = (loc.clk_cnt_p_1 == self.CPP())
		#--------
		# Implement the State/Counter stuff
		loc.Tstate = VgaTiming.State
		loc.hsc = {
			"s": Signal(width_from_len(loc.Tstate)),
			"c": Signal(self.HTIMING().COUNTER_WIDTH()),
			"next_s": Signal(width_from_len(loc.Tstate)),
		}
		loc.vsc = {
			"s": Signal(width_from_len(loc.Tstate)),
			"c": Signal(self.VTIMING().COUNTER_WIDTH()),
			"next_s": Signal(width_from_len(loc.Tstate)),
		}
		#--------
		## Implement HSYNC and VSYNC logic
		with m.If(outp.pixel_en): 
			self.HTIMING().update_state_cnt(m, loc.hsc)

			with m.Switch(loc.hsc["s"]):
				with m.Case(loc.Tstate.FRONT):
					m.d.sync += outp.hsync.eq(0b1)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.SYNC):
					m.d.sync += outp.hsync.eq(0b0)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.BACK):
					m.d.sync += outp.hsync.eq(0b1)
					self.VTIMING().no_change_update_next_s(m, loc.vsc)
				with m.Case(loc.Tstate.VISIB):
					m.d.sync += outp.hsync.eq(0b1),
					with m.If((loc.hsc["c"] + 0x1) >= self.FB_SIZE().x):
						self.VTIMING().update_state_cnt(m, loc.vsc)
					with m.Else():
						self.VTIMING().no_change_update_next_s(m, loc.vsc)

			with m.Switch(loc.vsc["s"]):
				with m.Case(loc.Tstate.FRONT):
					m.d.sync += outp.vsync.eq(0b1)
				with m.Case(loc.Tstate.SYNC):
					m.d.sync += outp.vsync.eq(0b0)
				with m.Case(loc.Tstate.BACK):
					m.d.sync += outp.vsync.eq(0b1)
				with m.Case(loc.Tstate.VISIB):
					m.d.sync += outp.vsync.eq(0b1)
		with m.Else(): # If(~outp.pixel_en):
			self.HTIMING().no_change_update_next_s(m, loc.hsc)
			self.VTIMING().no_change_update_next_s(m, loc.vsc)
		#--------
		# Implement drawing the picture

		with m.If(outp.pixel_en):
			# Visible area
			with m.If(outp.visib):
				with m.If(~inp.en):
					m.d.sync += [
						outp.col.r.eq(0xf),
						outp.col.g.eq(0xf),
						outp.col.b.eq(0xf),
					]
				with m.Else(): # If(inp.en):
					m.d.sync += [
						outp.col.eq(loc.col)
					]
			# Black border
			with m.Else(): # If (~outp.visib)
				m.d.sync += [
					outp.col.r.eq(0x0),
					outp.col.g.eq(0x0),
					outp.col.b.eq(0x0),
				]
		#--------
		# Implement VgaDriver bus to Fifo bus transaction
		m.d.comb += [
			outp.buf.can_prep.eq(~fifo_outp.full),
			fifo_inp.wr_en.eq(inp.buf.prep),
			fifo_inp.wr_data.eq(inp.buf.col),
		]
		#--------
		# Implement grabbing pixels from the FIFO.

		with m.If(outp.pixel_en & outp.visib & (~fifo_outp.empty)):
			m.d.comb += fifo_inp.rd_en.eq(0b1)
		with m.Else():
			m.d.comb += fifo_inp.rd_en.eq(0b0)
		#with m.If(loc.PIXEL_EN_NEXT_CYCLE & outp.next_visib
		#	& (~fifo_outp.empty)):
		#	m.d.sync += fifo_inp.rd_en.eq(0b1)
		#with m.Else():
		#	m.d.sync += fifo_inp.rd_en.eq(0b0)

		m.d.comb += [
			loc.col.eq(fifo_outp.rd_data),
			outp.dbg_fifo_empty.eq(fifo_outp.empty),
			outp.dbg_fifo_full.eq(fifo_outp.full),
		]
		#m.d.comb \
		#+= [
		#	loc.col.eq(bus.buf.col)
		#]
		#--------
		m.d.comb += [
			#outp.visib.eq((loc.hsc["s"] == loc.Tstate.VISIB)
			#	& (loc.vsc["s"] == loc.Tstate.VISIB)),
			outp.draw_pos.x.eq(loc.hsc["c"]),
			outp.draw_pos.y.eq(loc.vsc["c"]),
			outp.size.x.eq(self.FB_SIZE().x),
			outp.size.y.eq(self.FB_SIZE().y),
		]
		m.d.sync += [
			outp.next_visib.eq((loc.hsc["next_s"] == loc.Tstate.VISIB)
				& (loc.vsc["next_s"] == loc.Tstate.VISIB)),
			outp.visib.eq(outp.next_visib),
			outp.past_visib.eq(outp.visib),
			outp.past_draw_pos.eq(outp.draw_pos)
		]
		#--------
		return m
		#--------
