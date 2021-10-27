#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *

from math import *
import functools
import operator
#--------
class XbarSwitchBus:
	#--------
	def __init__(self, ElemKindT, INP_SIZE, OUTP_SIZE, SIGNED=False):
		#--------
		self.__ElemKindT = ElemKindT
		self.__INP_SIZE = INP_SIZE
		self.__OUTP_SIZE = OUTP_SIZE
		self.__SIGNED = SIGNED
		#--------
		# Which inputs to forward to which outputs
		self.sel = [Signal(self.SEL_SIZE()) for i in range(self.SIZE())]

		#self.inp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.inp_data \
			= [
				Splitrec.cast_elem(self.ElemKindT(), self.SIGNED(),
					name=psconcat("inp_data_", i))
					for i in range(self.INP_SIZE())
			]

		#self.outp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.outp_data \
			= [
				Splitrec.cast_elem(self.ElemKindT(), self.SIGNED(),
					name=psconcat("outp_data_", i))
					for i in range(self.OUTP_SIZE())
			]
		#--------
	#--------
	def ElemKindT(self):
		return self.__ElemKindT
	def INP_SIZE(self):
		return self.__INP_SIZE
	def OUTP_SIZE(self):
		return self.__OUTP_SIZE
	def SIGNED(self):
		return self.__SIGNED
	def SEL_SIZE(self):
		return math.ceil(math.log2(self.SIZE()))
	#--------
#--------
# A crossbar switch (combinational logic)
class XbarSwitchComb(Elaboratable):
	#--------
	def __init__(self, ElemKindT, INP_SIZE, OUTP_SIZE, SIGNED=False,
		PRIO_LST_2D=None):
		if (not isinstance(PRIO_LST_2D, list)) \
			and (not isinstance(PRIO_LST_2D, type(None))):
			raise TypeError(psconcat
				("`PRIO_LST` `{!r}` must be a `list` or `None`"
				.format(PRIO_LST_2D)))

		if isinstance(PRIO_LST_2D, list):
			if len(PRIO_LST_2D) != OUTP_SIZE:
				raise ValueError(psconcat
					("`PRIO_LST_2D` `{!r}` must be of length `OUTP_SIZE` ",
					"`{!r}`".format(PRIO_LST_2D)))

			for PRIO_LST in PRIO_LST_2D:
				temp = set(PRIO_LST)

				if (len(PRIO_LST) != len(temp)) \
					or (temp != set(list(range(INP_SIZE)))):
					raise ValueError(psconcat
						("`PRIO_LST` `{!r}` must ".format(PRIO_LST),
						"consist of all unique `int`s that are between 0 ",
						"and `INP_SIZE` `{!r}`".format(INP_SIZE)))

			self.__PRIO_LST_2D = PRIO_LST_2D
		else: # if isinstance(PRIO_LST, None):
			self.__PRIO_LST_2D \
				= [
					[i for i in range(INP_SIZE)]
						for j in range(OUTP_SIZE)
				]
		self.__bus = XbarSwitchBus(ElemKindT=ElemKindT, INP_SIZE=INP_SIZE,
			OUTP_SIZE=OUTP_SIZE, SIGNED=SIGNED)
	#--------
	def PRIO_LST_2D(self):
		return self.__PRIO_LST_2D
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		PRIO_LST = self.PRIO_LST()

		loc = Blank()
		loc.found_arr_2d \
			= [
				Signal(bus.INP_SIZE(), name="found_arr_2d_{j}")
					for j in range(bus.OUTP_SIZE())
			]
		#loc.found_arr_2d = Packarr.build(bus.SIZE(), bus.SIZE())
		#loc.temp_data_arr_2d \
		#	= Packarr.build \
		#	(
		#		Packarr.Shape(bus.ElemKindT(), bus.SIZE(), bus.signeD()),
		#		bus.SIZE(), bus.SIGNED()
		#	)
		#loc.temp_or_arr_2d \
		#	= [
		#		[
		#			Splitrec.cast_elem(bus.ElemKindT(), bus.SIGNED())
		#				for i in range(bus.SIZE() - 1)
		#		]
		#		for j in range(bus.size())
		#	]
		#--------
		for j in range(len(bus.outp_data)):
			for i in range(len(bus.inp_data)):
				m.d.comb \
				+= [
					loc.found_arr_2d[j][i].eq(bus.sel[j] == PRIO_LST[i]),
					loc.temp_data_arr_2d[j][i]
						.eq(Mux(loc.found_arr_2d[j][i],
							bus.inp_data[PRIO_LST[i]], 0x0))
				]
				#with m.If(bus.sel[j] == PRIO_LST[i]):
				#	m.d.comb \
				#	+= [
				#		bus.outp_data[j].eq(bus.inp_data[PRIO_LST[i]])
				#	]
		#--------
		return m
		#--------
	#--------
#--------
