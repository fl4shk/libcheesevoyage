#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *

from math import *
#--------
class XbarSwitchBus:
	#--------
	def __init__(self, WIDTH, SIZE):
		#--------
		self.__WIDTH = WIDTH
		self.__SIZE = SIZE
		#--------
		# Which inputs to forward to which outputs
		self.sel = Packarr.build(self.SEL_SIZE(), self.SIZE())
		self.inp_data = Packarr.build(self.WIDTH(), self.SIZE())
		self.outp_data = Packarr.build(self.WIDTH(), self.SIZE())
		#--------
	#--------
	def WIDTH(self):
		return self.__WIDTH
	def SIZE(self):
		return self.__SIZE
	def SEL_SIZE(self):
		return math.ceil(math.log2(self.SIZE()))
	#--------
#--------
# A crossbar switch
class XbarSwitchSync(Elaboratable):
	#--------
	def __init__(self, WIDTH, SIZE):
		self.__bus = XbarSwitchBus(WIDTH=WIDTH, SIZE=SIZE)
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
		#for i in range(bus.SEL_SIZE()):
		#	with m.Switch(bus.inp.sel[i]):
		#		for j in range(bus.SIZE()):
		#			with m.Case(i):
		#--------
		return m
		#--------
	#--------
#--------
