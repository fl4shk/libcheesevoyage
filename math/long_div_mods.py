#!/usr/bin/env python3

import math

from nmigen import *
from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from nmigen.sim import Simulator, Delay, Tick

from enum import Enum, auto

from misc_util import *
from general.container_types import *
from math.long_div_iter_mods import *
from math.math_types import *
#--------
class LongDivBus:
	#--------
	def __init__(self, constants, signed_reset=0b0):
		#--------
		self.__constants = constants
		#--------
		# Inputs

		if not constants.PIPELINED():
			self.ready = Signal()

		self.numer = Signal(constants.MAIN_WIDTH())
		self.denom = Signal(constants.DENOM_WIDTH())
		self.signed = Signal(reset=signed_reset)

		if constants.PIPELINED():
			self.tag_in = Signal(constants.TAG_WIDTH())
		#--------
		# Outputs

		if not constants.PIPELINED():
			self.valid = Signal()

		self.quot = Signal(constants.MAIN_WIDTH())
		self.rema = Signal(constants.MAIN_WIDTH())

		if constants.PIPELINED():
			self.tag_out = Signal(constants.TAG_WIDTH())
		#--------
	#--------
	def constants(self):
		return self.__constants
	#--------
#--------
class LongDivMultiCycle(Elaboratable):
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH, FORMAL=False,
		*, signed_reset=0b0):
		self.__constants \
			= LongDivConstants \
			(
				MAIN_WIDTH=MAIN_WIDTH,
				DENOM_WIDTH=DENOM_WIDTH,
				CHUNK_WIDTH=CHUNK_WIDTH,
				PIPELINED=False,
				FORMAL=FORMAL,
			)
		self.__bus = LongDivBus(self.__constants, signed_reset)
	#--------
	def bus(self):
		return self.__bus
	#--------
	class State(Enum):
		IDLE = 0
		RUNNING = auto()
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		constants = self.__constants
		bus = self.bus()

		zero_d = bus.denom == 0
		State = LongDivMultiCycle.State

		loc = Blank()
		# Submodules go here
		loc.m = [LongUdivIter(constants=constants)]
		m.submodules += loc.m
		loc.state = Signal(shape=Shape.cast(State), reset=State.IDLE,
			attrs=sig_keep())
		loc.temp_numer = Signal(constants.MAIN_WIDTH())
		loc.temp_denom = Signal(constants.DENOM_WIDTH())
		loc.numer_was_lez = Signal()
		loc.denom_was_lez = Signal()

		loc.quot_will_be_lez = Signal()
		loc.rema_will_be_lez = Signal()

		loc.bus_szd_temp_q = Signal(len(bus.quot))
		loc.bus_szd_temp_r = Signal(len(bus.rema))
		loc.lez_bus_szd_temp_q = Signal(len(bus.quot))
		loc.lez_bus_szd_temp_r = Signal(len(bus.rema))

		loc.temp_bus_quot = Signal(len(bus.quot))
		loc.temp_bus_rema = Signal(len(bus.rema))

		if constants.FORMAL():
			loc.formal = Blank()
			loc.formal.past_valid \
				= Signal \
				(
					attrs=sig_keep(),
					name="formal_past_valid"
				)
			past_valid = loc.formal.past_valid
		#--------
		it_bus = loc.m[0].bus()
		chunk_start = it_bus.chunk_start
		itd_in = it_bus.itd_in
		itd_out = it_bus.itd_out
		#--------
		if constants.FORMAL():
			#--------
			m.d.sync += past_valid.eq(0b1)
			#--------
			skip_cond = itd_in.formal.formal_denom == 0
			#--------
		#--------
		m.d.comb \
		+= [
			#loc.temp_numer.eq(Mux(bus.signed & bus.numer[-1],
			#	(~bus.numer) + 1, bus.numer)),
			loc.temp_numer.eq(Mux(bus.signed & bus.numer[-1],
				(~bus.numer) + 1, bus.numer)),
			loc.temp_denom.eq(Mux(bus.signed & bus.denom[-1],
				(~bus.denom) + 1, bus.denom)),

			loc.quot_will_be_lez.eq(loc.numer_was_lez
				!= loc.denom_was_lez),
			# Implement C's rules for modulo's sign
			loc.rema_will_be_lez.eq(loc.numer_was_lez),

			loc.bus_szd_temp_q.eq(itd_out.temp_quot[:len(bus.quot)]),
			loc.bus_szd_temp_r.eq(itd_out.temp_rema[:len(bus.rema)]),

			loc.lez_bus_szd_temp_q.eq((~loc.bus_szd_temp_q) + 1),
			loc.lez_bus_szd_temp_r.eq((~loc.bus_szd_temp_r) + 1),

			loc.temp_bus_quot.eq(Mux(loc.quot_will_be_lez,
				loc.lez_bus_szd_temp_q, loc.bus_szd_temp_q)),
			loc.temp_bus_rema.eq(Mux(loc.rema_will_be_lez,
				loc.lez_bus_szd_temp_r, loc.bus_szd_temp_r)),
		]
		#--------
		with m.If(~ResetSignal()):
			with m.Switch(loc.state):
				with m.Case(State.IDLE):
					#--------
					m.d.sync \
					+= [
						# Need to check for `bus.signed` so that unsigned
						# divides still work properly.
						loc.numer_was_lez.eq(bus.signed & bus.numer[-1]),
						loc.denom_was_lez.eq(bus.signed & bus.denom[-1]),

						chunk_start.eq(constants.NUM_CHUNKS() - 1),

						itd_in.temp_numer.eq(loc.temp_numer),
						itd_in.temp_quot.eq(0x0),
						itd_in.temp_rema.eq(0x0),
					]
					m.d.sync \
					+= [
						itd_in.denom_mult_lut[i].eq(loc.temp_denom * i)
							for i in range(constants.DML_SIZE())
					]
					#--------
					with m.If(bus.ready):
						m.d.sync \
						+= [
							bus.quot.eq(0x0),
							bus.rema.eq(0x0),
							bus.valid.eq(0b0),

							loc.state.eq(State.RUNNING),
						]
					#--------
				with m.Case(State.RUNNING):
					#--------
					with m.If(chunk_start > 0):
						# Since `itd_in` and `itd_out` are `Splitrec`s, we
						# can do a simple `.eq()` regardless of whether or
						# not `constants.FORMAL()` is true.
						m.d.sync += itd_in.eq(itd_out)
					with m.Else(): # m.If(chunk_start <= 0):
						m.d.sync \
						+= [
							bus.quot.eq(loc.temp_bus_quot),
							bus.rema.eq(loc.temp_bus_rema),
							bus.valid.eq(0b1),

							loc.state.eq(State.IDLE),
						]
					m.d.sync \
					+= [
						chunk_start.eq(chunk_start - 1),
					]
					#--------
			if constants.FORMAL():
				with m.If(past_valid):
					m.d.comb \
					+= [
						#--------
						Assume(Stable(bus.signed)),
						#--------
						Assume(Stable(loc.temp_numer)),
						Assume(Stable(loc.temp_denom)),
						#--------
					]
				with m.Switch(loc.state):
					with m.Case(State.IDLE):
						m.d.sync \
						+= [
							itd_in.formal.formal_numer.eq(loc.temp_numer),
							itd_in.formal.formal_denom.eq(loc.temp_denom),

							itd_in.formal.oracle_quot
								.eq(loc.temp_numer // loc.temp_denom),
							itd_in.formal.oracle_rema
								.eq(loc.temp_numer % loc.temp_denom),
						]
						m.d.sync \
						+= [
							itd_in.formal_dml_elem(i).eq(loc.temp_denom * i)
								for i in range(constants.DML_SIZE())
						]
						with m.If(past_valid & (~Stable(loc.state))):
							m.d.comb \
							+= [
								#--------
								Assert(skip_cond
									| (bus.quot
										== Past(loc.temp_bus_quot))),
								Assert(skip_cond
									| (bus.rema
										== Past(loc.temp_bus_rema))),
								Assert(bus.valid),
								#--------
							]
						#with m.Elif(past_valid & Stable(loc.state)):
					with m.Case(State.RUNNING):
						with m.If(past_valid & (~Stable(loc.state))):
							m.d.comb \
							+= [
								#--------
								Assert(loc.numer_was_lez
									== (Past(bus.signed) 
										& Past(bus.numer)[-1])),
								Assert(loc.denom_was_lez
									== (Past(bus.signed) 
										& Past(bus.denom)[-1])),
								#--------
								Assert(chunk_start
									== (constants.NUM_CHUNKS() - 1)),
								#--------
								Assert(itd_in.temp_numer
									== Past(loc.temp_numer)),
								Assert(itd_in.temp_quot == 0x0),
								Assert(itd_in.temp_rema == 0x0),
								#--------
								Assert(itd_in.formal.formal_numer
									== Past(loc.temp_numer)),
								Assert(itd_in.formal.formal_denom
									== Past(loc.temp_denom)),

								Assert(itd_in.formal.oracle_quot
									== (Past(loc.temp_numer)
										// Past(loc.temp_denom))),
								Assert(itd_in.formal.oracle_rema
									== (Past(loc.temp_numer)
										% Past(loc.temp_denom))),
								#--------
							]
						with m.Elif(past_valid & Stable(loc.state)):
							m.d.comb \
							+= [
								#--------
								Assert(Stable(loc.numer_was_lez)),
								Assert(Stable(loc.denom_was_lez)),
								#--------
								Assert(chunk_start
									== (Past(chunk_start) - 1)),
								#--------
								Assert(itd_out.temp_numer
									== Past(itd_in.temp_numer)),
								#--------
								Assert(itd_out.denom_mult_lut
									== Past(itd_in.denom_mult_lut)),
								#--------
								Assert(itd_out.formal.formal_numer
									== Past(itd_in.formal.formal_numer)),
								Assert(itd_out.formal.formal_denom
									== Past(itd_in.formal.formal_denom)),

								Assert(itd_out.formal.oracle_quot
									== Past(itd_in.formal.oracle_quot)),
								Assert(itd_out.formal.oracle_rema
									== Past(itd_in.formal.oracle_rema)),
								#--------
								Assert(itd_out.formal
									.formal_denom_mult_lut
									== Past(itd_in.formal
										.formal_denom_mult_lut)),
								#--------
							]
						with m.If(past_valid):
							m.d.comb \
							+= [
								Assert(itd_in.denom_mult_lut[i]
									== (itd_in.formal.formal_denom * i))
									for i in range(constants.DML_SIZE())
							]
							m.d.comb \
							+= [
								Assert(itd_in.formal_dml_elem(i)
									== (itd_in.formal.formal_denom * i))
									for i in range(constants.DML_SIZE())
							]
		#--------
		return m
		#--------
	#--------
#--------
class LongDivPipelined(Elaboratable):
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH, FORMAL=False,
		*, signed_reset=0b0):
		self.__constants \
			= LongDivConstants \
			(
				MAIN_WIDTH=MAIN_WIDTH,
				DENOM_WIDTH=DENOM_WIDTH,
				CHUNK_WIDTH=CHUNK_WIDTH,
				# This `TAG_WIDTH` is just a heuristic
				TAG_WIDTH=math.ceil(math.log2(MAIN_WIDTH // CHUNK_WIDTH)
					+ 2),
				PIPELINED=True,
				FORMAL=FORMAL
			)
		self.__bus = LongDivBus(self.__constants, signed_reset)
	#--------
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		constants = self.__constants
		bus = self.bus()

		#NUM_PSTAGES = constants.NUM_CHUNKS() + 1
		NUM_PSTAGES = constants.NUM_CHUNKS()
		NUM_PS_ELEMS = NUM_PSTAGES + 1

		loc = Blank()
		# Submodules go here
		loc.m \
			= [
				LongUdivIterSync
				(
					constants=constants,
					chunk_start_val=(NUM_PSTAGES - 1) - i
				)
					for i in range(NUM_PSTAGES)
			]
		m.submodules += loc.m
		loc.temp_numer = Signal(constants.MAIN_WIDTH())
		loc.temp_denom = Signal(constants.DENOM_WIDTH())

		loc.numer_was_lez = Signal(NUM_PS_ELEMS)
		loc.denom_was_lez = Signal(NUM_PS_ELEMS)

		loc.quot_will_be_lez = Signal(NUM_PS_ELEMS)
		loc.rema_will_be_lez = Signal(NUM_PS_ELEMS)

		loc.bus_szd_temp_q = Signal(len(bus.quot))
		loc.bus_szd_temp_r = Signal(len(bus.rema))
		loc.lez_bus_szd_temp_q = Signal(len(bus.quot))
		loc.lez_bus_szd_temp_r = Signal(len(bus.rema))

		loc.temp_bus_quot = Signal(len(bus.quot))
		loc.temp_bus_rema = Signal(len(bus.rema))
		#--------
		if constants.FORMAL():
			loc.formal = Blank()
			loc.formal.past_valid \
				= Signal \
				(
					attrs=sig_keep(),
					name="formal_past_valid"
				)
			past_valid = loc.formal.past_valid
		#--------
		its_bus = [loc.m[i].bus() for i in range(len(loc.m))]

		itd_in = [its_bus[i].itd_in for i in range(len(loc.m))]
		itd_out = [its_bus[i].itd_out for i in range(len(loc.m))]
		#--------
		# Connect the pipeline stages together
		m.d.comb \
		+= [
			itd_in[i + 1].eq(itd_out[i])
				for i in range(len(loc.m) - 1)
		]
		#--------
		m.d.sync \
		+= [
			#--------
			loc.temp_numer.eq(Mux(bus.signed & bus.numer[-1],
				(~bus.numer) + 1, bus.numer)),
			loc.temp_denom.eq(Mux(bus.signed & bus.denom[-1],
				(~bus.denom) + 1, bus.denom)),
			#--------
			loc.numer_was_lez[0].eq(bus.signed & bus.numer[-1]),
			loc.denom_was_lez[0].eq(bus.signed & bus.denom[-1]),

			loc.quot_will_be_lez[0].eq(loc.numer_was_lez[0]
				!= loc.denom_was_lez[0]),
			# Implement C's rules for modulo's sign
			loc.rema_will_be_lez[0].eq(loc.numer_was_lez[0]),
			#--------
		]
		m.d.comb \
		+= [
			#--------
			loc.bus_szd_temp_q.eq(itd_out[-1].temp_quot[:len(bus.quot)]),
			loc.bus_szd_temp_r.eq(itd_out[-1].temp_rema[:len(bus.rema)]),

			loc.lez_bus_szd_temp_q.eq((~loc.bus_szd_temp_q) + 1),
			loc.lez_bus_szd_temp_r.eq((~loc.bus_szd_temp_r) + 1),

			loc.temp_bus_quot.eq(Mux(loc.quot_will_be_lez,
				loc.lez_bus_szd_temp_q, loc.bus_szd_temp_q)),
			loc.temp_bus_rema.eq(Mux(loc.rema_will_be_lez,
				loc.lez_bus_szd_temp_r, loc.bus_szd_temp_r)),
			#--------
		]

		m.d.sync \
		+= [
			loc.numer_was_lez[i + 1].eq(loc.numer_was_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync \
		+= [
			loc.denom_was_lez[i + 1].eq(loc.denom_was_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync \
		+= [
			loc.quot_will_be_lez[i + 1].eq(loc.quot_will_be_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync \
		+= [
			loc.rema_will_be_lez[i + 1].eq(loc.rema_will_be_lez[i])
				for i in range(len(loc.m))
		]
		#--------
		m.d.sync \
		+= [
			itd_in[0].temp_numer.eq(loc.temp_numer),

			itd_in[0].temp_quot.eq(0x0),
			itd_in[0].temp_rema.eq(0x0),

			itd_in[0].tag.eq(bus.tag_in),
		]
		m.d.sync \
		+= [
			itd_in[0].denom_mult_lut[i].eq(loc.temp_denom * i)
				for i in range(constants.DML_SIZE())
		]
		m.d.comb \
		+= [
			bus.quot.eq(loc.temp_bus_quot),
			bus.rema.eq(loc.temp_bus_rema),

			bus.tag_out.eq(itd_out[-1].tag)
		]
		#--------
		if constants.FORMAL():
			#--------
			skip_cond = itd_out[-1].formal.formal_denom == 0
			#--------
			m.d.sync \
			+= [
				past_valid.eq(0b1),
				itd_in[0].formal.formal_numer.eq(loc.temp_numer),
				itd_in[0].formal.formal_denom.eq(loc.temp_denom),

				itd_in[0].formal.oracle_quot.eq(loc.temp_numer
					// loc.temp_denom),
				itd_in[0].formal.oracle_rema.eq(loc.temp_numer
					% loc.temp_denom),
			]
			m.d.sync \
			+= [
				itd_in[0].formal_dml_elem(i).eq(loc.temp_denom * i)
					for i in range(constants.DML_SIZE())
			]
			with m.If((~ResetSignal()) & past_valid):
				#--------
				m.d.comb \
				+= [
					#--------
					Assert(loc.temp_numer
						== Mux(Past(bus.signed) & Past(bus.numer)[-1],
							(~Past(bus.numer)) + 1, Past(bus.numer))),
					Assert(loc.temp_denom
						== Mux(Past(bus.signed) & Past(bus.denom)[-1],
							(~Past(bus.denom)) + 1, Past(bus.denom))),
					#--------
					Assert(loc.numer_was_lez[0]
						== (Past(bus.signed) & Past(bus.numer)[-1])),
					Assert(loc.denom_was_lez[0]
						== (Past(bus.signed) & Past(bus.denom)[-1])),

					Assert(loc.quot_will_be_lez[0]
						== (Past(loc.numer_was_lez)[0]
							!= Past(loc.denom_was_lez)[0])),
					# Implement C's rules for modulo's sign
					Assert(loc.rema_will_be_lez[0]
						== Past(loc.numer_was_lez)[0]),
					#--------
				]
				#--------
				m.d.comb \
				+= [
					Assert(loc.numer_was_lez[i + 1]
						== Past(loc.numer_was_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb \
				+= [
					Assert(loc.denom_was_lez[i + 1]
						== Past(loc.denom_was_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb \
				+= [
					Assert(loc.quot_will_be_lez[i + 1]
						== Past(loc.quot_will_be_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb \
				+= [
					Assert(loc.rema_will_be_lez[i + 1]
						== Past(loc.rema_will_be_lez)[i])
						for i in range(len(loc.m))
				]
				#--------
				m.d.comb \
				+= [
					#--------
					Assert(itd_in[0].temp_numer == Past(loc.temp_numer)),
					Assert(itd_in[0].temp_quot == 0x0),
					Assert(itd_in[0].temp_rema == 0x0),
					#--------
					Assert(itd_in[0].formal.formal_numer
						== Past(loc.temp_numer)),
					Assert(itd_in[0].formal.formal_denom
						== Past(loc.temp_denom)),

					Assert(itd_in[0].formal.oracle_quot
						== (Past(loc.temp_numer) // Past(loc.temp_denom))),
					Assert(itd_in[0].formal.oracle_rema
						== (Past(loc.temp_numer) % Past(loc.temp_denom))),
					#--------
					Assert(skip_cond
						| (itd_out[-1].temp_quot
							== itd_out[-1].formal.oracle_quot)),
					Assert(skip_cond
						| (itd_out[-1].temp_rema
							== itd_out[-1].formal.oracle_rema)),
					#--------
				]
				#--------
		#--------
		return m
		#--------
	#--------
#--------
