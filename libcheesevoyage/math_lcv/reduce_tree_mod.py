#!/usr/bin/env python3

from enum import Enum, auto
import math

import functools 
import operator

from amaranth import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.misc_types import *
#--------
class ReduceTreeBinop(Enum):
	ADD = 0x0

	AND = auto()
	OR = auto()
	XOR = auto()
#--------
class ReduceTreeBus:
	#--------
	def __init__(
		self,
		INP_DATA_WIDTH,
		INP_SIZE,
		BINOP,
		*,
		FORMAL=False,
		tag_dct={
			"inp": None,
			"outp": None,
		},
	):
		#--------
		self.__INP_DATA_WIDTH = INP_DATA_WIDTH
		self.__INP_SIZE = INP_SIZE

		if not isinstance(BINOP, ReduceTreeBinop):
			raise TypeError("`BINOP` `{!r}` must be a `ReduceTreeBinop`"
				.format(BINOP))
		self.__BINOP = BINOP
		self.__FORMAL = FORMAL
		#--------
		self.__NUM_STAGES = math.ceil(math.log2(self.INP_SIZE()))
		self.__INP_SIZE_INT = 1 << self.NUM_STAGES()
		#self.__OUTP_DATA_WIDTH \
		#	= self.INP_DATA_WIDTH() + self.NUM_STAGES() \
		#		if self.BINOP() == ReduceTreeBinop.ADD \
		#		else self.INP_DATA_WIDTH()
		self.__OUTP_DATA_WIDTH = self.INP_DATA_WIDTH() + self.NUM_STAGES()
		#--------
		inp_shape = {}
		outp_shape = {}

		#inp_shape["arr"] = IntfarrShape(
		#	[
		#		IntfShape(
		#			#name=psconcat("inp_data_", i),
		#			#name="data",
		#			shape={
		#				"data": FieldInfo(
		#					self.INP_DATA_WIDTH(),
		#					name=psconcat("inp_arr_data_", i)
		#				)
		#			},
		#			modport=Modport({
		#				#psconcat("inp_data_", i):
		#				"data": PortDir.Inp,
		#			}),
		#			#pdir=PortDir.Inp,
		#			tag=inp_tag,
		#		)
		#		#IntfShape.mk_single_pdir_shape(
		#		#	#name=psconcat("inp_arr_", i),
		#		#	name="data",
		#		#	shape={
		#		#		"data": FieldInfo(
		#		#			self.INP_DATA_WIDTH(),
		#		#			name=psconcat("inp_data_", i),
		#		#		)
		#		#	},
		#		#	pdir=PortDir.Inp,
		#		#	tag=inp_tag,
		#		#)
		#		for i in range(self.INP_SIZE())
		#	],
		#)
		inp_shape["data"] = [
			FieldInfo(
				self.INP_DATA_WIDTH(),
				name=psconcat("inp_data_", i)
			)
			for i in range(self.INP_SIZE())
		]

		outp_shape["data"] = FieldInfo(
			self.OUTP_DATA_WIDTH(),
			name="outp_data"
		)
		#modport=Modport({
		#	"data": PortDir.Outp
		#})

		#self.inp = Splitrec(inp_shape, use_parent_name=False)
		#self.outp = Splitrec(outp_shape, use_parent_name=False)
		ishape = IntfShape.mk_io_shape(
			shape_dct={
				"inp": inp_shape,
				"outp": outp_shape,
			},
			#inp_tag=None,
			#tag_dct={
			#	"inp": inp_tag,
			#	"outp": outp_tag,
			#},
			tag_dct=tag_dct,
			mk_modport_dct={
				"inp": True,
				"outp": True,
			},
		)
		#ishape = {
		#	"inp": IntfShape(
		#		shape=inp_shape,
		#	)
		#	"outp": IntfShape(
		#		shape=outp_shape,
		#		modport=Modport({
		#			key: PortDir.Outp
		#			for key in outp_shape
		#		}),
		#		tag=outp_tag
		#	),
		#}
		#--------
		if self.FORMAL():
			#self.formal = 
			ishape.update(
				IntfShape.mk_single_pdir_shape(
					name="formal",
					shape={
						"oracle_outp_data": FieldInfo(
							self.OUTP_DATA_WIDTH(),
							name="oracle_outp_data"
						)
					},
					#pdir=PortDir.Outp,
					pdir=PortDir.Noconn,
					tag=None,
				)
			)
		#with open("reduce_tree-show_ishape.txt.ignore", "w") as f:
		#	f.writelines([
		#		do_psconcat_flattened(ishape)
		#	])
		#--------
		self.__bus = Splitintf(IntfShape(shape=ishape))
	#--------
	@property
	def bus(self):
		return self.__bus
	def INP_DATA_WIDTH(self):
		return self.__INP_DATA_WIDTH
	def INP_SIZE(self):
		return self.__INP_SIZE
	def BINOP(self):
		return self.__BINOP
	def FORMAL(self):
		return self.__FORMAL
	#--------
	def NUM_STAGES(self):
		return self.__NUM_STAGES
	def INP_SIZE_INT(self):
		return self.__INP_SIZE_INT
	def OUTP_DATA_WIDTH(self):
		return self.__OUTP_DATA_WIDTH
	def ST_NUM_OUT(self, stage):
		return self.INP_SIZE_INT() >> stage
	def ST_WIDTH(self, stage):
		#return self.INP_DATA_WIDTH() + stage \
		#	if self.BINOP() == ReduceTreeBinop.ADD \
		#	else self.INP_DATA_WIDTH()
		return self.INP_DATA_WIDTH() + stage
	#--------
#--------
class ReduceTree(Elaboratable):
	#--------
	def __init__(self, INP_DATA_WIDTH, INP_SIZE, BINOP=ReduceTreeBinop.ADD,
		*, FORMAL=False, DOMAIN=BasicDomain.COMB):
		#--------
		if not isinstance(DOMAIN, BasicDomain):
			raise TypeError("`DOMAIN` `{!r}` must be a `BasicDomain`"
				.format(DOMAIN))
		self.__DOMAIN = DOMAIN
		#--------
		self.__bus \
			= ReduceTreeBus(
				INP_DATA_WIDTH=INP_DATA_WIDTH,
				INP_SIZE=INP_SIZE,
				BINOP=BINOP,
				FORMAL=FORMAL,
			)
		#--------
	#--------
	def DOMAIN(self):
		return self.__DOMAIN
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus().bus

		loc = Blank()
		loc.data = [
			[Signal(self.bus().OUTP_DATA_WIDTH(),
				name=psconcat("data_", j, "_", i))
				for i in range(self.bus().INP_SIZE_INT())]
			for j in range(self.bus().NUM_STAGES() + 1)
		]
		#--------
		for st in range(self.bus().NUM_STAGES() + 1):
			ST_NUM_OUT = self.bus().ST_NUM_OUT(st)
			sw = self.bus().ST_WIDTH(st)

			#md = m.d.comb \
			#	if self.DOMAIN() == BasicDomain.COMB \
			#	else m.d.sync
			md = basic_domain_to_actual_domain(m, self.DOMAIN())

			if st == 0:
				# st 0 is actually the module inputs
				for op in range(ST_NUM_OUT):
					if op < self.bus().INP_SIZE():
						m.d.comb += loc.data[st][op][:sw] \
							.eq(bus.inp.data[op][:sw])
					else: # if op >= self.bus().INP_SIZE():
						m.d.comb += loc.data[st][op].eq(0x0)
			else: # if st > 0:
				# All other stages hold operator instance outputs
				for op in range(ST_NUM_OUT):
					out = loc.data[st][op][:sw]
					left = loc.data[st - 1][op * 2][:sw - 1]
					right = loc.data[st - 1][(op * 2) + 1][:sw - 1]

					if self.bus().BINOP() == ReduceTreeBinop.ADD:
						md += out.eq(left + right)
					elif self.bus().BINOP() == ReduceTreeBinop.AND:
						md += out.eq(left & right)
					elif self.bus().BINOP() == ReduceTreeBinop.OR:
						md += out.eq(left | right)
					else: # if self.bus().BINOP() == ReduceTreeBinop.XOR:
						md += out.eq(left ^ right)

		m.d.comb += bus.outp.data.eq(loc.data[self.bus().NUM_STAGES()][0])

		if self.bus().FORMAL():
			if self.DOMAIN() != BasicDomain.COMB:
				raise ValueError(("`self.DOMAIN()` `{!r}` must be "
					+ "`BasicDomain.COMB` when doing formal verification")
					.format(self.DOMAIN()))

			m.d.comb += Assert(
				bus.outp.data == bus.formal.oracle_outp_data
			)

			#temp_inp_data = [
			#	elem.data
			#	for elem in bus.inp.arr
			#]
			temp_inp_data = bus.inp.data
			if self.bus().BINOP() == ReduceTreeBinop.ADD:
				m.d.comb += bus.formal.oracle_outp_data \
					.eq(functools.reduce(operator.add, temp_inp_data))
			elif self.bus().BINOP() == ReduceTreeBinop.AND:
				m.d.comb += bus.formal.oracle_outp_data \
					.eq(functools.reduce(operator.and_, temp_inp_datadata))
			elif self.bus().BINOP() == ReduceTreeBinop.OR:
				m.d.comb += bus.formal.oracle_outp_data \
					.eq(functools.reduce(operator.or_, temp_inp_data))
			else: # if self.bus().BINOP() == ReduceTreeBinop.XOR:
				m.d.comb += bus.formal.oracle_outp_data \
					.eq(functools.reduce(operator.xor, temp_inp_data))
		#--------
		return m
		#--------
	#--------
#--------
