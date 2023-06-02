#!/usr/bin/env python3
#--------
import math

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
#--------
class AdcBus:
	def __init__(self, ELEM_WIDTH):
		self.__ELEM_WIDTH = ELEM_WIDTH

		#self.inp \
		#	= Packrec \
		#	([
		#		("a", ELEM_WIDTH),
		#		("b", ELEM_WIDTH),
		#		("carry_in", 1)
		#	])
		#self.outp \
		#	= Packrec \
		#	([
		#		("carry", 1),
		#		("sum", ELEM_WIDTH),
		#	])
		self.inp = Struct({
			"a": ELEM_WIDTH,
			"b": ELEM_WIDTH,
			"carry_in": 1,
		})
		self.outp = Struct({
			"carry": 1,
			"sum": ELEM_WIDTH,
		})
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
class VectorAddBus:
	def __init__(self, shape, SIZE):
		self.shape = shape
		self.__SIZE = SIZE

		#self.a = Packarr(Packarr.Shape(shape, SIZE, name="a"))
		#self.b = Packarr.like(self.a, name="b")
		#self.inp \
		#	= Packrec \
		#	([
		#		("a", Packarr.Shape(shape, SIZE)),
		#		("b", Packarr.Shape(shape, SIZE)),
		#	])
		#self.inp \
		#	= Packarr \
		#	(
		#		Packarr.Shape
		#		(
		#			Packrec.Layout
		#			([
		#				("a", shape),
		#				("b", shape)
		#			]),
		#			SIZE
		#		),
		#		name="inp"
		#	)
		#self.sum = Packarr(Packarr.Shape(shape, SIZE), name="sum")
		##self.sum = Packarr.like(self.a, name="sum")
		#self.sum_next = Packarr.like(self.sum, name="sum_next")

		self.inp = View(
			ArrayLayout(
				StructLayout({
					"a": shape,
					"b", shape,
				}),
				SIZE
			),
			name="inp"
		)
		self.sum = View(ArrayLayout(shape, SIZE), name="sum")
		self.sum_next = View(ArrayLayout(shape, SIZE), name="sum_next")
		#self.sum_next = Packarr.like(self.sum, name="sum_next")
	def shape(self):
		return self.shape
	def SIZE(self):
		return self.__SIZE

class VectorAdd(Elaboratable):
	#--------
	def __init__(self, shape, SIZE):
		self.__bus = VectorAddBus(shape, SIZE)
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
	def __init__(self, shape, SIZE):
		self.shape = shape
		self.__SIZE = SIZE

		#self.inp \
		#	= Packrec([
		#		("a", Packarr.Shape(shape, SIZE)),
		#		("b", Packarr.Shape(shape, SIZE)),
		#		("sel", math.ceil(math.log2(shape))),
		#	])
		#self.outp \
		#	= Splitrec \
		#	([
		#		("sum_next", Signal(shape, name="outp_sum_next")),
		#		("sum", Signal(shape, name="outp_sum")),
		#	])
		self.inp = View(
			StructLayout({
				"a", ArrayLayout(shape, SIZE),
				"b", ArrayLayout(shape, SIZE),
				"sel", math.ceil(math.log2(shape)),
			})
		)
		self.outp = Splitrec({
			"sum_next": Signal(shape, name="outp_sum_next"),
			"sum": Signal(shape, name="outp_sum"),
		})
	def shape(self):
		return self.shape
	def SIZE(self):
		return self.__SIZE

class AddChosenScalars(Elaboratable):
	#--------
	def __init__(self, shape, SIZE):
		self.__bus = AddChosenScalarsBus(shape, SIZE)
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
