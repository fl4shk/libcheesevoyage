#!/usr/bin/env python3

import math

from amaranth import *
from amaranth.lib import *

from libcheesevoyage.misc_util import (
	Blank, sig_keep,
)
from libcheesevoyage.general import *

#class MemWrapperRdIshape(IntfShape):
#	def __init__(
#		self,
#		data_width: int,
#		addr_width: int,
#		*,
#		is_from: bool,
#		from_mwrap_rd_tag=None,
#		to_mwrap_rd_tag=None,
#	):
#		pass

# This class is so that Amaranth `Memory`s can be used with `Splitintf` and
# friends
class MemWrapperIshape(IntfShape):
	def __init__(
		self,
		#data_width: int,
		#addr_width: int,
		#width: int, # data width
		shape,
		depth: int,
		*,
		is_from: bool,
		from_mwrap_tag=None,
		to_mwrap_tag=None,
	):
		from_mwrap_name = "from_mwrap"
		to_mwrap_name = "to_mwrap"

		from_mwrap_shape = {}
		to_mwrap_shape = {}

		from_mwrap_shape["rd_data"] = FieldInfo(
			#width,
			shape,
			attrs=sig_keep(), name="mwrap_from_rd_data",
		)
		to_mwrap_shape["rd_addr"] = FieldInfo(
			MemWrapperIshape.calc_addr_width(depth),
			attrs=sig_keep(),
			name="mwrap_to_rd_data",
		)

		to_mwrap_shape["wr_addr"] = FieldInfo(
			MemWrapperIshape.calc_addr_width(depth),
			attrs=sig_keep(),
			name="mwrap_to_wr_addr",
		)
		to_mwrap_shape["wr_data"] = FieldInfo(
			#width,
			shape,
			attrs=sig_keep(), name="mwrap_to_rd_data",
		)
		to_mwrap_shape["wr_en"] = FieldInfo(
			1, attrs=sig_keep(), name="mwrap_to_wr_en",
		)

		shape = IntfShape.mk_fromto_shape(
			from_name=from_mwrap_name, to_name=to_mwrap_name,
			from_shape=from_mwrap_shape, to_shape=to_mwrap_shape,
			is_from=is_from,
			from_tag=from_mwrap_tag, to_tag=to_mwrap_tag,
		)
		super().__init__(shape)
	@staticmethod
	def calc_addr_width(depth: int):
		return math.ceil(math.log2(depth))

class MemWrapperBus:
	def __init__(
		self,
		#width: int,
		shape,
		depth: int,
		*,
		is_from: bool,
		from_mwrap_tag=None,
		to_mwrap_tag=None,
	):
		#self.__width = width
		self.__shape = shape
		self.__depth = depth
		self.__is_from = is_from
		self.__from_mwrap_tag = from_mwrap_tag
		self.__to_mwrap_tag = to_mwrap_tag

		ishape = MemWrapperIshape(
			#width=width,
			shape=shape,
			depth=depth,
			is_from=is_from,
			from_mwrap_tag=from_mwrap_tag,
			to_mwrap_tag=to_mwrap_tag,
		)
		self.__bus = Splitintf(ishape)
		self.__width = len(self.__bus.from_mwrap.rd_data)
	@property
	def bus(self):
		return self.__bus
	def width(self):
		return self.__width
	def shape(self):
		return self.__shape
	def depth(self):
		return self.__depth
	def addr_width(self):
		return MemWrapperIshape.calc_addr_width(self.depth())
	def is_from(self):
		return self.__is_from
	def from_mwrap_tag(self):
		return self.__from_mwrap_tag
	def to_mwrap_tag(self):
		return self.__to_mwrap_tag

class MemWrapper(Elaboratable):
	def __init__(
		self,
		#width: int,
		shape,
		depth: int,
		init: list,
		*,
		attrs=None, # examples for Xilinx:
					# {"ram_style": "block"}
					# {"ram_style": "distributed"}
					# {"ram_style": "registers"}
					# {"ram_style": "ultra"}
					# {"ram_style": "mixed"}
		from_mwrap_tag=None,
		to_mwrap_tag=None,
	):
		self.__bus = MemWrapperBus(
			#width=width,
			shape=shape,
			depth=depth,
			is_from=True,
			from_mwrap_tag=None,
			to_mwrap_tag=None,
		)
		self.__mem = Memory(
			#width=width,
			width=self.bus().width(),
			depth=depth,
			init=init,
			attrs=attrs,
		)
	def bus(self):
		return self.__bus
	#def mem(self):
	#	return self.__mem
	def elaborate(self, platform: str) -> Module:
		m = Module()

		bus = self.bus().bus
		#from_mwrap = bus.from_mwrap
		#to_mwrap = bus.to_mwrap

		loc = Blank()
		loc.rd_port = self.__mem.read_port()
		loc.wr_port = self.__mem.write_port()

		#m.submodules.mem = self.__mem
		#m.submodules += [loc.rd_port, loc.wr_port]
		m.submodules.rd_port = loc.rd_port
		m.submodules.wr_port = loc.wr_port

		m.d.comb += [
			loc.rd_port.addr.eq(bus.to_mwrap.rd_addr),
			bus.from_mwrap.rd_data.eq(loc.rd_port.data),

			loc.wr_port.addr.eq(bus.to_mwrap.wr_addr),
			loc.wr_port.data.eq(bus.to_mwrap.wr_data),
			loc.wr_port.en.eq(bus.to_mwrap.wr_en),
		]

		return m
