#!/usr/bin/env python3

from enum import Enum, auto, unique

from nmigen import *
from nmigen.hdl.rec import *

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *

class VgaTiming:
	def __init__(self, visib, front, sync, back):
		self.__visib, self.__front, self.__sync, self.__back \
			= visib, front, sync, back
		#self.__state = Signal(unsigned(width_from_len(State)),
		#	reset=State.FRONT)
		#self.__state_counter = Signal(unsigned(self.COUNTER_WIDTH()),
		#	reset=0x0)
	#--------
	class State(Enum):
		FRONT = 0
		SYNC = auto()
		BACK = auto()
		VISIB = auto()
	#--------
	def visib(self):
		return self.__visib
	def front(self):
		return self.__front
	def sync(self):
		return self.__sync
	def back(self):
		return self.__back
	#--------
	# This is specifically the minimum width instead of like, 32-bit or
	# something
	def COUNTER_WIDTH(self):
		return max \
			([
				width_from_arg(arg)
				for arg in [self.visib(), self.front(), self.sync(),
					self.back()]
			])
	#--------
	def no_change_update_next_s(self, m, state_cnt):
		m.d.comb += state_cnt["next_s"].eq(state_cnt["s"])
	def update_state_cnt(self, m, state_cnt):
		def mk_case(m, state_cnt, state_size, next_state):
			#counter_p_1 = state_cnt["c"] + 0x1
			#with m.If(counter_p_1 >= state_size):
			#	m.d.sync += state_cnt["c"].eq(0x0),
			#	m.d.comb += state_cnt["next_s"].eq(next_state)
			#with m.Else():
			#	m.d.sync += state_cnt["c"].eq(counter_p_1)
			#	m.d.comb += state_cnt["next_s"].eq(state_cnt["s"])
			#m.d.sync += state_cnt["s"].eq(state_cnt["next_s"])
			counter_p_1 = state_cnt["c"] + 0x1
			with m.If(counter_p_1 >= state_size):
				m.d.sync += state_cnt["s"].eq(next_state)
				m.d.sync += state_cnt["c"].eq(0x0)
				#m.d.comb += state_cnt["next_s"].eq(next_state)
			with m.Else():
				m.d.sync += state_cnt["c"].eq(counter_p_1)
				#self.no_change_update_next_s(m, state_cnt)

			with m.If((state_cnt["c"] + 0x2) >= state_size):
				m.d.comb += state_cnt["next_s"].eq(next_state)
			with m.Else():
				m.d.comb += state_cnt["next_s"].eq(state_cnt["s"])

		State = VgaTiming.State
		with m.Switch(state_cnt["s"]):
			with m.Case(State.FRONT):
				mk_case(m, state_cnt, self.front(), State.SYNC)
			with m.Case(State.SYNC):
				mk_case(m, state_cnt, self.sync(), State.BACK)
			with m.Case(State.BACK):
				mk_case(m, state_cnt, self.back(), State.VISIB)
			with m.Case(State.VISIB):
				mk_case(m, state_cnt, self.visib(), State.FRONT)
	#--------

class VgaTimingInfo:
	def __init__(self, PIXEL_CLK, HTIMING, VTIMING):
		self.__PIXEL_CLK, self.__HTIMING, self.__VTIMING \
			= PIXEL_CLK, HTIMING, VTIMING
	def PIXEL_CLK(self):
		return self.__PIXEL_CLK
	def HTIMING(self):
		return self.__HTIMING
	def VTIMING(self):
		return self.__VTIMING


class RgbColorLayout(Packrec.Layout):
	def __init__(self, CHAN_WIDTH=None):
		self.__CHAN_WIDTH = CHAN_WIDTH if CHAN_WIDTH != None \
			else RgbColor.DEF_CHAN_WIDTH()
		super().__init__ \
		([
			("r", self.CHAN_WIDTH()),
			("g", self.CHAN_WIDTH()),
			("b", self.CHAN_WIDTH()),
		])

	def CHAN_WIDTH(self):
		return self.__CHAN_WIDTH
	def __unsgn_chan(self):
		return unsigned(self.CHAN_WIDTH())

	def drive(self, other):
		return Cat(self.r, self.g, self.b) \
			.eq(Cat(other.r, other.g, other.b))

class RgbColor(Packrec):
	def __init__(self, CHAN_WIDTH=None):
		REAL_CHAN_WIDTH = CHAN_WIDTH if CHAN_WIDTH != None \
			else RgbColor.DEF_CHAN_WIDTH()
		super().__init__(RgbColorLayout(CHAN_WIDTH=REAL_CHAN_WIDTH))

	@staticmethod
	def DEF_CHAN_WIDTH():
		return 4

	def CHAN_WIDTH(self):
		return self.layout.CHAN_WIDTH()
	def drive(self, other):
		self.layout.drive(other)

#class VgaDriverBufLayout(Packrec.Layout):
#	def __init__(self, CHAN_WIDTH=RgbColor.DEF_CHAN_WIDTH()):
#		super().__init__ \
#		([
#			("can_prep", 1),
#			("prep", 1),
#			("col", RgbColorLayout(CHAN_WIDTH=CHAN_WIDTH)),
#		])
class VgaDriverBuf(Splitrec):
	def __init__(self, CHAN_WIDTH=RgbColor.DEF_CHAN_WIDTH()):
		#super().__init__(VgaDriverBufLayout(CHAN_WIDTH=CHAN_WIDTH))
		self.can_prep = Splitrec.cast_elem(1)
		self.prep = Splitrec.cast_elem(1)
		self.col = Splitrec.cast_elem \
			(RgbColorLayout(CHAN_WIDTH=CHAN_WIDTH))

		self.__CHAN_WIDTH = CHAN_WIDTH

	def CHAN_WIDTH(self):
		return self.__CHAN_WIDTH
