#!/usr/bin/env python3

import math

from amaranth import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.sim import Simulator, Delay, Tick

from enum import Enum, auto

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.math_lcv.long_div_iter_mods import *
from libcheesevoyage.math_lcv.math_types import *
#--------
class LongDivBus:
	#--------
	def __init__(self, constants, signed_reset=0b0):
		#--------
		self.__constants = constants
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		# Inputs

		if not constants.PIPELINED():
			self.inp.ready = Signal(name="inp_ready")

		self.inp.numer = Signal(constants.MAIN_WIDTH(), name="inp_numer")
		self.inp.denom = Signal(constants.DENOM_WIDTH(), name="inp_denom")
		self.inp.signed = Signal(reset=signed_reset, name="inp_signed")

		if constants.PIPELINED():
			self.inp.tag = Signal(constants.TAG_WIDTH(), name="inp_tag")
		#--------
		# Outputs

		if not constants.PIPELINED():
			self.outp.valid = Signal(name="outp_valid")

		self.outp.quot = Signal(constants.MAIN_WIDTH(), name="outp_quot")
		self.outp.rema = Signal(constants.MAIN_WIDTH(), name="outp_rema")

		if constants.PIPELINED():
			self.outp.tag = Signal(constants.TAG_WIDTH(), name="outp_tag")
		#--------
	#--------
	def constants(self):
		return self.__constants
	#--------
#--------
class LongDivMultiCycle(Elaboratable):
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH,
		*, FORMAL=False, signed_reset=0b0):
		self.__constants \
			= LongDivConstants(
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
		inp = bus.inp
		outp = bus.outp

		zero_d = inp.denom == 0
		State = LongDivMultiCycle.State

		loc = Blank()
		# Submodules go here
		loc.m = [LongUdivIter(constants=constants)]
		m.submodules += loc.m
		loc.state = Signal(shape=Shape.cast(State), reset=State.IDLE,
			attrs=sig_keep())
		#loc.temp_numer = Signal(constants.MAIN_WIDTH(),
		#	attrs=sig_keep(), name="loc_temp_numer")
		loc.temp_numer = Signal(constants.TEMP_T_WIDTH(),
			attrs=sig_keep(), name="loc_temp_numer")
		#loc.temp_denom = Signal(constants.DENOM_WIDTH(),
		#	attrs=sig_keep(), name="loc_temp_denom")
		loc.temp_denom = Signal(constants.TEMP_T_WIDTH(),
			attrs=sig_keep(), name="loc_temp_denom")
		loc.numer_was_lez = Signal()
		loc.denom_was_lez = Signal()

		loc.quot_will_be_lez = Signal()
		loc.rema_will_be_lez = Signal()

		loc.bus_szd_temp_q = Signal(len(outp.quot))
		loc.bus_szd_temp_r = Signal(len(outp.rema))
		loc.lez_bus_szd_temp_q = Signal(len(outp.quot))
		loc.lez_bus_szd_temp_r = Signal(len(outp.rema))

		loc.temp_bus_quot = Signal(len(outp.quot),
			attrs=sig_keep(), name="loc_temp_bus_quot")
		loc.temp_bus_rema = Signal(len(outp.rema),
			attrs=sig_keep(), name="loc_temp_bus_rema")
		loc.chunk_start_begin = Signal(constants.TEMP_T_WIDTH(),
			attrs=sig_keep(), name="loc_chunk_start_begin")
		loc.temp_dbg = Signal(attrs=sig_keep(), name="loc_temp_dbg")

		if constants.FORMAL():
			loc.formal = Blank()
			loc.formal.past_valid \
				= Signal(
					attrs=sig_keep(),
					name="formal_past_valid"
				)
			past_valid = loc.formal.past_valid
		#--------
		it_bus = loc.m[0].bus()
		CHUNK_WIDTH = constants.CHUNK_WIDTH()
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
		m.d.comb += [
			#loc.temp_numer.eq(Mux(inp.signed & inp.numer[-1],
			#	(~inp.numer) + 1, inp.numer)),
			loc.temp_numer.eq(Mux(inp.signed & inp.numer[-1],
				(~inp.numer) + 1, inp.numer)),
			loc.temp_denom.eq(Mux(inp.signed & inp.denom[-1],
				(~inp.denom) + 1, inp.denom)),

			loc.quot_will_be_lez.eq(loc.numer_was_lez
				!= loc.denom_was_lez),
			# Implement C's rules for modulo's sign
			loc.rema_will_be_lez.eq(loc.numer_was_lez),

			loc.bus_szd_temp_q.eq(itd_out.temp_quot.as_value()
				[:len(outp.quot)]),
			loc.bus_szd_temp_r.eq(itd_out.temp_rema.as_value()
				[:len(outp.rema)]),

			loc.lez_bus_szd_temp_q.eq((~loc.bus_szd_temp_q) + 1),
			loc.lez_bus_szd_temp_r.eq((~loc.bus_szd_temp_r) + 1),

			loc.temp_bus_quot.eq(Mux(loc.quot_will_be_lez,
				loc.lez_bus_szd_temp_q, loc.bus_szd_temp_q)),
			loc.temp_bus_rema.eq(Mux(loc.rema_will_be_lez,
				loc.lez_bus_szd_temp_r, loc.bus_szd_temp_r)),
			#loc.temp_bus_quot.eq(loc.bus_szd_temp_q),
			#loc.temp_bus_rema.eq(loc.bus_szd_temp_r),

			#loc.chunk_start_begin.eq(chunk_start
			#	== (constants.NUM_CHUNKS() - 1)),
			loc.chunk_start_begin.eq(constants.NUM_CHUNKS() - 1),
		]
		#--------
		with m.If(~ResetSignal()):
			with m.Switch(loc.state):
				with m.Case(State.IDLE):
					#--------
					m.d.sync += [
						# Need to check for `inp.signed` so that unsigned
						# divides still work properly.
						loc.numer_was_lez.eq(inp.signed & inp.numer[-1]),
						loc.denom_was_lez.eq(inp.signed & inp.denom[-1]),

						#chunk_start.eq(constants.NUM_CHUNKS() - 1),
						chunk_start.eq(loc.chunk_start_begin),

						itd_in.temp_numer.eq(loc.temp_numer),
						itd_in.temp_quot.eq(0x0),
						itd_in.temp_rema.eq(0x0),
					]
					m.d.sync += [
						itd_in.denom_mult_lut[i].eq(loc.temp_denom * i)
							for i in range(constants.DML_SIZE())
					]
					#--------
					with m.If(inp.ready):
						m.d.sync += [
							outp.quot.eq(0x0),
							outp.rema.eq(0x0),
							outp.valid.eq(0b0),

							loc.state.eq(State.RUNNING),
						]
						#m.d.sync += [
						#	# Need to check for `inp.signed` so that
						#	# unsigned divides still work properly.
						#	loc.numer_was_lez
						#		.eq(inp.signed & inp.numer[-1]),
						#	loc.denom_was_lez
						#		.eq(inp.signed & inp.denom[-1]),

						#	chunk_start.eq(constants.NUM_CHUNKS() - 1),

						#	itd_in.temp_numer.eq(loc.temp_numer),
						#	itd_in.temp_quot.eq(0x0),
						#	itd_in.temp_rema.eq(0x0),
						#]
						#m.d.sync += [
						#	itd_in.denom_mult_lut[i].eq(loc.temp_denom * i)
						#		for i in range(constants.DML_SIZE())
						#]
					#--------
				with m.Case(State.RUNNING):
					#--------
					with m.If(chunk_start > 0):
						# Since `itd_in` and `itd_out` are `Splitrec`s, we
						# can do a simple `.eq()` regardless of whether or
						# not `constants.FORMAL()` is true.
						m.d.sync += itd_in.eq(itd_out)
					with m.Else(): # m.If(chunk_start <= 0):
						m.d.sync += [
							outp.quot.eq(loc.temp_bus_quot),
							outp.rema.eq(loc.temp_bus_rema),
							outp.valid.eq(0b1),

							loc.state.eq(State.IDLE),
						]
					m.d.sync += [
						chunk_start.eq(chunk_start - 1),
					]
					#--------
			#--------
			if constants.FORMAL():
				#--------
				m.d.comb += [
					#--------
					Assume(~(skip_cond)),
					#--------
					#Assume(Stable(inp.signed)),
					#--------
				]
				#--------
				#with m.If(~past_valid):
				#	m.d.comb += [
				#		Assume(~inp.ready)
				#	]
				#with m.Else(): # If(past_valid):
				#	#--------
				#	#m.d.comb += [
				#	#	#--------
				#	#	Assume(Stable(inp.signed)),
				#	#	#--------
				#	#	Assume(Stable(loc.temp_numer)),
				#	#	Assume(Stable(loc.temp_denom)),
				#	#	#--------
				#	#]
				#	#with m.If((~inp.ready) & Stable(inp.ready)):
				#	#	m.d.comb += [
				#	#		#--------
				#	#		Assume(Stable(loc.state))
				#	#		#--------
				#	#	]
				#	#--------
				#	with m.Switch(loc.state):
				#		#--------
				#		with m.Case(State.IDLE):
				#			#--------
				#			#with m.If(inp.ready & (~Stable(inp.ready))):
				#			with m.If(inp.ready):
				#				m.d.sync += [
				#					itd_in.formal.formal_numer
				#						.eq(loc.temp_numer),
				#					itd_in.formal.formal_denom
				#						.eq(loc.temp_denom),

				#					itd_in.formal.oracle_quot
				#						.eq(loc.temp_numer
				#							// loc.temp_denom),
				#					itd_in.formal.oracle_rema
				#						.eq(loc.temp_numer
				#							% loc.temp_denom),
				#				]
				#				m.d.sync += [
				#					itd_in.formal_dml_elem(i)
				#						.eq(loc.temp_denom * i)
				#					for i in range(constants.DML_SIZE())
				#				]
				#			#--------
				#		with m.Case(State.RUNNING):
				#			with m.If(~Stable(loc.state)):
				#				m.d.comb += [
				#					Assert(
				#						itd_in.denom_mult_lut[i]
				#						#itd_in.denom_mult_lut.word_select
				#						#	(i, CHUNK_WIDTH)
				#						== (
				#							itd_in.formal_dml_elem(i)
				#							#itd_in.formal.formal_denom
				#							#	.as_value()
				#							#* i
				#						)
				#					)
				#					for i in range
				#						(constants.DML_SIZE())
				#				]
				#				#m.d.comb += [
				#				#	Assert(
				#				#		itd_in.formal_dml_elem(i)
				#				#		== (
				#				#			itd_in.formal.formal_denom
				#				#				.as_value()
				#				#			* i
				#				#		)
				#				#	)
				#				#	for i in range
				#				#		(constants.DML_SIZE())
				#				#]
				#				#m.d.comb += [
				#				#	Assert(
				#				#		itd_in.denom_mult_lut[0]
				#				#		#itd_in.denom_mult_lut.as_value()
				#				#		#	.word_select(0, CHUNK_WIDTH)
				#				#		== (
				#				#			#itd_in.formal.formal_denom
				#				#			#	.as_value()
				#				#			#* 0
				#				#		)
				#				#	),
				#				#	Assert(
				#				#		itd_in.denom_mult_lut[1]
				#				#		#itd_in.denom_mult_lut.as_value()
				#				#		#	.word_select(1, CHUNK_WIDTH)
				#				#		== (
				#				#			itd_in.formal_dml_elem(1)
				#				#			#itd_in.formal.formal_denom
				#				#			#	.as_value()
				#				#			#* 1
				#				#		)
				#				#	)
				#				#]
				#			#with m.Else(): # If(Stable(loc.state)):
				#			#	#m.d.comb += [
				#			#	#	Assert(Stable())
				#			#	#]
				#			#	pass
				#	#with m.Switch(loc.state):
				#	#	with m.Case(State.IDLE):
				#	#		#--------
				#	#		#with m.If(Stable(loc.state)):
				#	#		#	m.d.comb += [
				#	#		#	]
				#	#		#with m.Else(): # If(~Stable(loc.state))
				#	#		#	m.d.comb
				#	#		#--------
				#	#	with m.Case(State.RUNNING):
				#	#		#--------
				#	#		#--------
				#	#--------
				with m.If(past_valid):
					m.d.comb += [
						#--------
						Assume(Stable(inp.signed)),
						#--------
						Assume(Stable(loc.temp_numer)),
						Assume(Stable(loc.temp_denom)),
						#--------
					]
				with m.Switch(loc.state):
					with m.Case(State.IDLE):
						m.d.sync += [
							itd_in.formal.formal_numer.eq(loc.temp_numer),
							itd_in.formal.formal_denom.eq(loc.temp_denom),

							itd_in.formal.oracle_quot
								.eq(loc.temp_numer // loc.temp_denom),
							itd_in.formal.oracle_rema
								.eq(loc.temp_numer % loc.temp_denom),
						]
						m.d.sync += [
							itd_in.formal_dml_elem(i).eq
								(loc.temp_denom * i)
								for i in range(constants.DML_SIZE())
						]
						with m.If(past_valid & (~Stable(loc.state))):
							m.d.comb += [
								#--------
								Assert(skip_cond
									| (outp.quot
										== Past(loc.temp_bus_quot))),
								Assert(skip_cond
									| (outp.rema
										== Past(loc.temp_bus_rema))),
								Assert(outp.valid),
								#--------
							]
						#with m.Elif(past_valid & Stable(loc.state)):
					with m.Case(State.RUNNING):
						#with m.If(~Stable(loc.state)):
						#	m.d.comb += [
						#		Assert(past_valid)
						#	]
						#with m.If(Past(loc.state) == State.IDLE):
						#	m.d.sync += [
						#		Assert(chunk_start
						#			== loc.chunk_start_begin),
						#	]
						with m.If(
							past_valid & (Past(loc.state) == State.IDLE)
						):
							m.d.sync += [
								#--------
								Assert(loc.numer_was_lez
									== (Past(inp.signed) 
										& Past(inp.numer)[-1])),
								Assert(loc.denom_was_lez
									== (Past(inp.signed) 
										& Past(inp.denom)[-1])),
								#--------
								#Assert(chunk_start
								#	== (constants.NUM_CHUNKS() - 1)),
								#Assert(chunk_start[:len(chunk_start) - 1]
								#	== (constants.NUM_CHUNKS() - 1)),
								Assert(chunk_start
									[:len(loc.chunk_start_begin)]
									== loc.chunk_start_begin),
								#Assert(loc.chunk_start_begin),
								#--------
								Assert(itd_in.temp_numer
									== Past(loc.temp_numer)),
								Assert(itd_in.temp_quot.as_value() == 0x0),
								Assert(itd_in.temp_rema.as_value() == 0x0),
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
							m.d.sync += [
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
							m.d.sync += [
								Assert(itd_in.denom_mult_lut[i]
									== (
										itd_in.formal.formal_denom
											.as_value()
										* i
									))
									for i in range(constants.DML_SIZE())
							]
							m.d.sync += [
								Assert(
									itd_in.formal_dml_elem(i)
									== (
										itd_in.formal.formal_denom
											.as_value()
										* i
									))
									for i in range(constants.DML_SIZE())
							]
		#--------
		return m
		#--------
	#--------
#--------
class LongDivPipelined(Elaboratable):
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH,
		*, FORMAL=False, signed_reset=0b0):
		self.__constants \
			= LongDivConstants(
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
		inp = bus.inp
		outp = bus.outp

		#NUM_PSTAGES = constants.NUM_CHUNKS() + 1
		NUM_PSTAGES = constants.NUM_CHUNKS()
		NUM_PS_ELEMS = NUM_PSTAGES + 1

		loc = Blank()
		# Submodules go here
		loc.m = [
			LongUdivIterSync(constants=constants,
				chunk_start_val=(NUM_PSTAGES - 1) - i)
			for i in range(NUM_PSTAGES)
		]
		m.submodules += loc.m
		loc.temp_numer = Signal(constants.MAIN_WIDTH())
		loc.temp_denom = Signal(constants.DENOM_WIDTH())

		loc.numer_was_lez = Signal(NUM_PS_ELEMS)
		loc.denom_was_lez = Signal(NUM_PS_ELEMS)

		loc.quot_will_be_lez = Signal(NUM_PS_ELEMS)
		loc.rema_will_be_lez = Signal(NUM_PS_ELEMS)

		loc.bus_szd_temp_q = Signal(len(outp.quot))
		loc.bus_szd_temp_r = Signal(len(outp.rema))
		loc.lez_bus_szd_temp_q = Signal(len(outp.quot))
		loc.lez_bus_szd_temp_r = Signal(len(outp.rema))

		loc.temp_bus_quot = Signal(len(outp.quot))
		loc.temp_bus_rema = Signal(len(outp.rema))
		#--------
		if constants.FORMAL():
			loc.formal = Blank()
			loc.formal.past_valid \
				= Signal(
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
		m.d.comb += [
			itd_in[i + 1].eq(itd_out[i])
				for i in range(len(loc.m) - 1)
		]
		#--------
		m.d.sync += [
			#--------
			loc.temp_numer.eq(Mux(inp.signed & inp.numer[-1],
				(~inp.numer) + 1, inp.numer)),
			loc.temp_denom.eq(Mux(inp.signed & inp.denom[-1],
				(~inp.denom) + 1, inp.denom)),
			#--------
			loc.numer_was_lez[0].eq(inp.signed & inp.numer[-1]),
			loc.denom_was_lez[0].eq(inp.signed & inp.denom[-1]),

			loc.quot_will_be_lez[0].eq(loc.numer_was_lez[0]
				!= loc.denom_was_lez[0]),
			# Implement C's rules for modulo's sign
			loc.rema_will_be_lez[0].eq(loc.numer_was_lez[0]),
			#--------
		]
		m.d.comb += [
			#--------
			loc.bus_szd_temp_q.eq(itd_out[-1].temp_quot.as_value()
				[:len(outp.quot)]),
			loc.bus_szd_temp_r.eq(itd_out[-1].temp_rema.as_value()
				[:len(outp.rema)]),

			loc.lez_bus_szd_temp_q.eq((~loc.bus_szd_temp_q) + 1),
			loc.lez_bus_szd_temp_r.eq((~loc.bus_szd_temp_r) + 1),

			loc.temp_bus_quot.eq(Mux(loc.quot_will_be_lez,
				loc.lez_bus_szd_temp_q, loc.bus_szd_temp_q)),
			loc.temp_bus_rema.eq(Mux(loc.rema_will_be_lez,
				loc.lez_bus_szd_temp_r, loc.bus_szd_temp_r)),
			#--------
		]

		m.d.sync += [
			loc.numer_was_lez[i + 1].eq(loc.numer_was_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync += [
			loc.denom_was_lez[i + 1].eq(loc.denom_was_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync += [
			loc.quot_will_be_lez[i + 1].eq(loc.quot_will_be_lez[i])
				for i in range(len(loc.m))
		]
		m.d.sync += [
			loc.rema_will_be_lez[i + 1].eq(loc.rema_will_be_lez[i])
				for i in range(len(loc.m))
		]
		#--------
		m.d.sync += [
			itd_in[0].temp_numer.eq(loc.temp_numer),

			itd_in[0].temp_quot.eq(0x0),
			itd_in[0].temp_rema.eq(0x0),

			itd_in[0].tag.eq(inp.tag),
		]
		m.d.sync += [
			itd_in[0].denom_mult_lut[i].eq(loc.temp_denom * i)
				for i in range(constants.DML_SIZE())
		]
		m.d.comb += [
			outp.quot.eq(loc.temp_bus_quot),
			outp.rema.eq(loc.temp_bus_rema),

			outp.tag.eq(itd_out[-1].tag)
		]
		#--------
		if constants.FORMAL():
			#--------
			skip_cond = itd_out[-1].formal.formal_denom == 0
			#--------
			m.d.sync += [
				past_valid.eq(0b1),
				itd_in[0].formal.formal_numer.eq(loc.temp_numer),
				itd_in[0].formal.formal_denom.eq(loc.temp_denom),

				itd_in[0].formal.oracle_quot.eq(loc.temp_numer
					// loc.temp_denom),
				itd_in[0].formal.oracle_rema.eq(loc.temp_numer
					% loc.temp_denom),
			]
			m.d.sync += [
				itd_in[0].formal_dml_elem(i).eq(loc.temp_denom * i)
					for i in range(constants.DML_SIZE())
			]
			with m.If((~ResetSignal()) & past_valid):
				#--------
				m.d.comb += [
					#--------
					Assert(loc.temp_numer
						== Mux(Past(inp.signed) & Past(inp.numer)[-1],
							(~Past(inp.numer)) + 1, Past(inp.numer))),
					Assert(loc.temp_denom
						== Mux(Past(inp.signed) & Past(inp.denom)[-1],
							(~Past(inp.denom)) + 1, Past(inp.denom))),
					#--------
					Assert(loc.numer_was_lez[0]
						== (Past(inp.signed) & Past(inp.numer)[-1])),
					Assert(loc.denom_was_lez[0]
						== (Past(inp.signed) & Past(inp.denom)[-1])),

					Assert(loc.quot_will_be_lez[0]
						== (Past(loc.numer_was_lez)[0]
							!= Past(loc.denom_was_lez)[0])),
					# Implement C's rules for modulo's sign
					Assert(loc.rema_will_be_lez[0]
						== Past(loc.numer_was_lez)[0]),
					#--------
				]
				#--------
				m.d.comb += [
					Assert(loc.numer_was_lez[i + 1]
						== Past(loc.numer_was_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb += [
					Assert(loc.denom_was_lez[i + 1]
						== Past(loc.denom_was_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb += [
					Assert(loc.quot_will_be_lez[i + 1]
						== Past(loc.quot_will_be_lez)[i])
						for i in range(len(loc.m))
				]
				m.d.comb += [
					Assert(loc.rema_will_be_lez[i + 1]
						== Past(loc.rema_will_be_lez)[i])
						for i in range(len(loc.m))
				]
				#--------
				m.d.comb += [
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
