#!/usr/bin/env python3
#--------
import math

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
#--------
class AdcBus:
	def __init__(self, ELEM_WIDTH):
		self.__ELEM_WIDTH = ELEM_WIDTH

		self.inp \
			= Packrec \
			([
				("a", ELEM_WIDTH),
				("b", ELEM_WIDTH),
				("carry_in", 1)
			])
		self.outp \
			= Packrec \
			([
				("carry", 1),
				("sum", ELEM_WIDTH),
			])
		self.clocked_sum = Signal(ELEM_WIDTH)
		self.clocked_carry_out = Signal(1)
	def ELEM_WIDTH(self):
		return self.__ELEM_WIDTH

def Adc(Elaboratable):
	#--------
	def __init__(self, ELEM_WIDTH):
		self.__bus = AdcBus(ELEM_WIDTH=ELEM_WIDTH)
	#--------
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		#--------
		m.d.comb \
		+= [
			bus.outp.eq(bus.inp.a + bus.inp.b + bus.inp.carry)
		]
		m.d.sync \
		+= [
			Cat(bus.clocked_carry_out, bus.clocked_sum).eq(bus.outp)
		]
		#--------
		return m
		#--------
	#--------
#--------
# Test `Packarr`
class VectorAddBus:
	def __init__(self, ElemKindT, SIZE):
		self.__ElemKindT = ElemKindT
		self.__SIZE = SIZE

		#self.a = Packarr(Packarr.Shape(ElemKindT, SIZE, name="a"))
		#self.b = Packarr.like(self.a, name="b")
		#self.inp \
		#	= Packrec \
		#	([
		#		("a", Packarr.Shape(ElemKindT, SIZE)),
		#		("b", Packarr.Shape(ElemKindT, SIZE)),
		#	])
		self.inp \
			= Packarr \
			(
				Packarr.Shape
				(
					Packrec.Layout
					([
						("a", ElemKindT),
						("b", ElemKindT)
					]),
					SIZE
				),
				name="inp"
			)
		self.sum = Packarr(Packarr.Shape(ElemKindT, SIZE), name="sum")
		#self.sum = Packarr.like(self.a, name="sum")
		self.sum_next = Packarr.like(self.sum, name="sum_next")
	def ElemKindT(self):
		return self.__ElemKindT
	def SIZE(self):
		return self.__SIZE

class VectorAdd(Elaboratable):
	#--------
	def __init__(self, ElemKindT, SIZE):
		self.__bus = VectorAddBus(ElemKindT, SIZE)
	#--------
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		#--------
		m.d.comb \
		+= [
			#bus.sum_next[i].eq(bus.inp.a[i] + bus.inp.b[i])
			#	for i in range(bus.SIZE())
			bus.sum_next[i].eq(bus.inp[i].a + bus.inp[i].b)
				for i in range(bus.SIZE())
			#bus.sum_next[i].eq(bus.a[i] + bus.b[i])
			#	for i in range(bus.SIZE())
		]
		m.d.sync \
		+= [
			bus.sum.eq(bus.sum_next)
		]
		#--------
		return m
		#--------
	#--------
#--------
class AddChosenScalarsBus:
	def __init__(self, ElemKindT, SIZE):
		self.__ElemKindT = ElemKindT
		self.__SIZE = SIZE

		self.inp \
			= Packrec \
			([
				("a", Packarr.Shape(ElemKindT, SIZE)),
				("b", Packarr.Shape(ElemKindT, SIZE)),
				("sel", math.ceil(math.log2(ElemKindT))),
			])
		self.outp \
			= Splitrec \
			([
				("sum_next", Signal(ElemKindT, name="outp_sum_next")),
				("sum", Signal(ElemKindT, name="outp_sum")),
			])
	def ElemKindT(self):
		return self.__ElemKindT
	def SIZE(self):
		return self.__SIZE

class AddChosenScalars(Elaboratable):
	#--------
	def __init__(self, ElemKindT, SIZE):
		self.__bus = AddChosenScalarsBus(ElemKindT, SIZE)
	#--------
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		inp = bus.inp
		#--------
		m.d.comb \
		+= [
			bus.outp.sum_next.eq(inp.a[inp.sel] + inp.b[inp.sel]),
		]
		m.d.sync \
		+= [
			bus.outp.sum.eq(bus.outp.sum_next),
		]
		#--------
		return m
		#--------
	#--------
#--------
