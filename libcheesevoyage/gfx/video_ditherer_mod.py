#!/usr/bin/env python3

from amaranth import *
from amaranth.hdl.rec import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.gfx.vga_ext_types import *

class VideoDithererIshape(IntfShape):
	def __init__(
		self,
		FB_SIZE_2D, CHAN_WIDTH,
		#*,
		#tag_dct={
		#	"inp": None,
		#	"outp": None,
		#},
	):
		inp_shape = {}
		outp_shape = {}

		inp_shape["en"] = 1

		outp_shape["col"] = RgbColorLayt(
			CHAN_WIDTH=VideoDithererBus.OUT_CHAN_WIDTH(CHAN_WIDTH)
		)
		inp_shape["col"] = RgbColorLayt(
			CHAN_WIDTH=CHAN_WIDTH
		)

		outp_shape["frame_cnt"] = VideoDithererBus.DITHER_DELTA_WIDTH()
		outp_shape["next_pos"] = VideoDithererBus.coord_shape()
		outp_shape["pos"] = VideoDithererBus.coord_shape()
		outp_shape["past_pos"] = VideoDithererBus.coord_shape()

		#self.inp = Splitrec(inp_shape, use_parent_name=False)
		#self.outp = Splitrec(outp_shape, use_parent_name=False)
		shape = IntfShape.mk_io_shape(
			shape_dct={
				"inp": inp_shape,
				"outp": outp_shape,
			},
			#tag_dct={
			#	"inp": inp_tag,
			#	"outp": outp_tag,
			#},
			#tag_dct=tag_dct,
			mk_modport_dct={
				"inp": True,
				"outp": True,
			}
		)
		super().__init__(shape)
class VideoDithererBus:
	def __init__(
		self,
		FB_SIZE_2D, CHAN_WIDTH,
		#*,
		#tag_dct={
		#	"inp": None,
		#	"outp": None,
		#},
	):
		self.__FB_SIZE_2D, self.__CHAN_WIDTH = FB_SIZE_2D, CHAN_WIDTH
		#self.__inp_tag = inp_tag
		#self.__outp_tag = outp_tag
		#self.__tag_dct = tag_dct

		#self.__bus = Splitintf(IntfShape(shape))
		ishape = VideoDithererIshape(
			FB_SIZE_2D=FB_SIZE_2D,
			CHAN_WIDTH=CHAN_WIDTH,
			#tag_dct=tag_dct,
		)
		self.__bus = Splitintf(ishape, name="bus")
		# Need a channel width of at least 3 for dithering to work (though
		# if it *were* 3, it probably wouldn't work very well!)
		assert CHAN_WIDTH > self.CHAN_WIDTH_DELTA()

	@property
	def bus(self):
		return self.__bus
	@staticmethod
	def DITHER_DELTA_WIDTH():
		return width_from_arg(4)
	def FB_SIZE_2D(self):
		return self.__FB_SIZE_2D
	def CHAN_WIDTH(self):
		return self.__CHAN_WIDTH
	@staticmethod
	def OUT_CHAN_WIDTH(CHAN_WIDTH):
		return (CHAN_WIDTH - VideoDithererBus.CHAN_WIDTH_DELTA())
	@staticmethod
	def CHAN_WIDTH_DELTA():
		return 2
	#def CoordT(self):
	#	return Vec2(16)
	@staticmethod
	def coord_shape():
		return Vec2Layt(16)
	#def tag_dct(self):
	#	return self.__tag_dct
	#def inp_tag(self):
	#	#return self.__inp_tag
	#	return self.tag_dct()["inp"]
	#def outp_tag(self):
	#	#return self.__outp_tag
	#	return self.tag_dct()["outp"]

# Temporally and spatially dither a CHAN_WIDTH color down to CHAN_WIDTH - 2
class VideoDitherer(Elaboratable):
	def __init__(
		self,
		FB_SIZE_2D,
		CHAN_WIDTH=RgbColorLayt.DEF_CHAN_WIDTH() + 2,
		#*,
		#tag_dct={
		#	"inp": None,
		#	"outp": None,
		#},
	):
		self.__bus = VideoDithererBus(
			FB_SIZE_2D=FB_SIZE_2D,
			CHAN_WIDTH=CHAN_WIDTH,
			#inp_tag=inp_tag,
			#outp_tag=outp_tag,
			#tag_dct=tag_dct,
		)

		#self.__frame = Signal(width_from_arg(4))

	def bus(self):
		return self.__bus
	def FB_SIZE_2D(self):
		return self.bus().FB_SIZE_2D()
	def CHAN_WIDTH(self):
		return self.bus().CHAN_WIDTH()
	def OUT_CHAN_WIDTH(self):
		#return self.bus().OUT_CHAN_WIDTH()
		return VideoDithererBus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())

	# Dithering pattern
	def __PAT_VAL(self, val):
		return Const(val, self.bus().DITHER_DELTA_WIDTH())
	def PATTERN(self):
		return Array([
			Array([
				Array([self.__PAT_VAL(0), self.__PAT_VAL(1)]),
				Array([self.__PAT_VAL(3), self.__PAT_VAL(2)]),
			]),
			Array([
				Array([self.__PAT_VAL(1), self.__PAT_VAL(0)]),
				Array([self.__PAT_VAL(2), self.__PAT_VAL(3)]),
			]),
			Array([
				Array([self.__PAT_VAL(3), self.__PAT_VAL(2)]),
				Array([self.__PAT_VAL(0), self.__PAT_VAL(1)]),
			]),
			Array([
				Array([self.__PAT_VAL(2), self.__PAT_VAL(3)]),
				Array([self.__PAT_VAL(1), self.__PAT_VAL(0)]),
			]),
		])

	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		inp = bus.bus.inp
		outp = bus.bus.outp

		loc = Blank()
		#--------
		loc.past_col_out = Splitrec(RgbColorLayt(
			CHAN_WIDTH=bus.OUT_CHAN_WIDTH(self.CHAN_WIDTH())
		))
		# Update `outp.pos` and `outp.frame_cnt`
		loc.POS_PLUS_1 = {"x": outp.pos.x + 0x1, "y": outp.pos.y + 0x1}

		# Perform dithering
		loc.dicol = Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH()))
		loc.CHAN_DELTA \
			= self.PATTERN()[outp.frame_cnt] \
				[Value.cast(outp.pos.y[0])][Value.cast(outp.pos.x[0])]
		loc.col_in_plus_delta \
			= Splitrec(RgbColorLayt(CHAN_WIDTH=bus.CHAN_WIDTH() + 1))

		with m.If(loc.POS_PLUS_1["x"] < bus.FB_SIZE_2D().x):
			#m.d.sync += outp.pos.x.eq(loc.POS_PLUS_1["x"])
			m.d.comb += [
				outp.next_pos.x.eq(loc.POS_PLUS_1["x"]),
				outp.next_pos.y.eq(outp.pos.y),
			]
		with m.Else(): # If(loc.POS_PLUS_1["x"] >= bus.FB_SIZE_2D().x):
			#m.d.sync += outp.pos.x.eq(0x0)
			m.d.comb += outp.next_pos.x.eq(0x0),
			with m.If(loc.POS_PLUS_1["y"] < bus.FB_SIZE_2D().y):
				m.d.comb += outp.next_pos.y.eq(loc.POS_PLUS_1["y"])
			with m.Else():
				# If(loc.POS_PLUS_1["y"] >= bus.FB_SIZE_2D().y):
				m.d.comb += outp.next_pos.y.eq(0x0)

				# This wraps around to zero automatically due to
				# modular arithmetic, so we don't need another mux just
				# for this.
				with m.If(inp.en):
					m.d.sync += outp.frame_cnt.eq(outp.frame_cnt + 0x1)

		with m.If(inp.en):
			m.d.sync += [
				loc.past_col_out.eq(outp.col),
				outp.past_pos.eq(outp.pos),
			]

			m.d.sync += [
				outp.pos.eq(outp.next_pos)
			]

			m.d.comb += [
				loc.col_in_plus_delta.r.eq(inp.col.r + loc.CHAN_DELTA),
				loc.col_in_plus_delta.g.eq(inp.col.g + loc.CHAN_DELTA),
				loc.col_in_plus_delta.b.eq(inp.col.b + loc.CHAN_DELTA),
			]

			# Saturating arithmetic to prevent an artifact
			with m.If(loc.col_in_plus_delta.r
				[len(loc.col_in_plus_delta.r) - 1]):
				m.d.comb += loc.dicol.r.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.r.eq(loc.col_in_plus_delta.r
					[:len(loc.dicol.r)])
			#m.d.comb += loc.dicol.r.eq(loc.col_in_plus_delta.r
			#	[:len(loc.dicol.r)])

			with m.If(loc.col_in_plus_delta.g
				[len(loc.col_in_plus_delta.g) - 1]):
				m.d.comb += loc.dicol.g.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.g.eq(loc.col_in_plus_delta.g
					[:len(loc.dicol.g)])
			#m.d.comb += loc.dicol.g.eq(loc.col_in_plus_delta.g
			#	[:len(loc.dicol.g)])

			with m.If(loc.col_in_plus_delta.b
				[len(loc.col_in_plus_delta.b) - 1]):
				m.d.comb += loc.dicol.b.eq(-1)
			with m.Else():
				m.d.comb += loc.dicol.b.eq(loc.col_in_plus_delta.b
					[:len(loc.dicol.b)])
			#m.d.comb += loc.dicol.b.eq(loc.col_in_plus_delta.b
			#	[:len(loc.dicol.b)])

			m.d.comb += [
				#loc.dicol.r.eq(loc.COL_IN_PLUS_DELTA["r"]),
				#loc.dicol.g.eq(loc.COL_IN_PLUS_DELTA["g"]),
				#loc.dicol.b.eq(loc.COL_IN_PLUS_DELTA["b"]),

				outp.col.r.eq(loc.dicol.r[bus.CHAN_WIDTH_DELTA():]),
				outp.col.g.eq(loc.dicol.g[bus.CHAN_WIDTH_DELTA():]),
				outp.col.b.eq(loc.dicol.b[bus.CHAN_WIDTH_DELTA():]),
				#outp.col.r.eq(loc.dicol.r[bus.CHAN_WIDTH_DELTA():]),
				#outp.col.g.eq(-1),
				#outp.col.b.eq(loc.dicol.b[bus.CHAN_WIDTH_DELTA():]),
			]

		with m.Else(): # If(~bus.en):
			m.d.comb += [
				loc.col_in_plus_delta.eq(0x0),
				loc.dicol.eq(0x0),
				outp.col.eq(loc.past_col_out),
				outp.next_pos.eq(outp.pos),
			]
		#--------
		return m
		#--------
