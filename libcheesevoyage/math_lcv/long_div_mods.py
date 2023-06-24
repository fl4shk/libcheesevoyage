#!/usr/bin/env python3

import math

from amaranth import *
from amaranth.lib import enum
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.sim import Simulator, Delay, Tick

#from enum import Enum, auto
import enum as pyenum

from libcheesevoyage.misc_util import *
#from libcheesevoyage.general.container_types import *
#from libcheesevoyage.general.general_types import SigInfo
from libcheesevoyage.general.pipeline_mods import PipeSkidBuf
from libcheesevoyage.math_lcv.long_div_iter_mods import (
	LongDivConstants, LongUdivIterDataLayt,
	LongUdivIter, LongUdivIterBus, LongUdivIterSync, LongUdivIterSyncBus,
	FieldInfo, Splitrec,
	PortDir, Modport, IntfShape, Splitintf, IntfarrShape, Splitintfarr
)
from libcheesevoyage.math_lcv.math_types import *
#--------
#class LongDivBusIshape(IntfShape):
#class LongDivBus:
class LongDivBusIshape(IntfShape):
	#--------
	def __init__(
		self,
		constants,
		signed_reset=0b0,
		*,
		is_child: bool=True,
	):
		#--------
		#self.__constants = constants
		#--------
		shape = {
			"inp": {},
			"outp": {},
		}
		inp_shape = shape["inp"]
		outp_shape = shape["outp"]
		#inp_shape = {}
		#outp_shape = {}
		#--------
		# Inputs

		if not constants.PIPELINED():
			inp_shape["ready"] = FieldInfo(1, name="inp_ready")

		inp_shape["numer"] = FieldInfo(
			constants.MAIN_WIDTH(), name="inp_numer"
		)
		inp_shape["denom"] = FieldInfo(
			constants.DENOM_WIDTH(), name="inp_denom"
		)
		inp_shape["signed"] = FieldInfo(
			1, reset=signed_reset, name="inp_signed"
		)

		if constants.PIPELINED():
			inp_shape["tag"] = FieldInfo(
				constants.TAG_WIDTH(), name="inp_tag"
			)
		#--------
		# Outputs

		if not constants.PIPELINED():
			outp_shape["valid"] = FieldInfo(1, name="outp_valid")

		outp_shape["quot"] = FieldInfo(
			constants.MAIN_WIDTH(), name="outp_quot"
		)
		outp_shape["rema"] = FieldInfo(
			constants.MAIN_WIDTH(), name="outp_rema"
		)

		if constants.PIPELINED():
			outp_shape["tag"] = FieldInfo(
				constants.TAG_WIDTH(), name="outp_tag"
			)
		#--------
		#self.inp = Splitrec(inp_shape, use_parent_name=False)
		#self.outp = Splitrec(outp_shape, use_parent_name=False)
		#temp_mp_dct = {}
		#for inp_key in inp_shape.keys():
		#	temp_mp_dct["_".join(["inp", inp_key]
		temp_modport = Modport({
			"inp": PortDir.Inp,
			"outp": PortDir.Outp,
		})
		temp_modport = (
			temp_modport
			if is_child
			else reversed(temp_modport) 
		)
		#with open("testificate.txt.ignore", "w") as f:
		#	f.writelines([
		#		psconcat("temp_modport.dct(): ", temp_modport.dct(), "\n"),
		#		psconcat("shape: ", shape, "\n"),
		#	])
		super().__init__(
			shape=shape,
			modport=temp_modport,
		)
	#--------
class LongDivBus:
	def __init__(
		self,
		constants,
		signed_reset=0b0,
		*,
		is_child: bool=True
	):
		self.__constants = constants
		ishape = LongDivBusIshape(
			constants=constants,
			signed_reset=signed_reset,
			is_child=is_child,
		)
		self.__bus = Splitintf(
			ishape,
			name="bus", use_parent_name=False,
		)
		#super().__init__(ishape)
	#--------
	@property
	def bus(self):
		return self.__bus
	def constants(self):
		return self.__constants
	#--------
#--------
class LongDivMultiCycle(Elaboratable):
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH,
		*, FORMAL=False, signed_reset=0b0):
		self.__constants = LongDivConstants(
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
	class State(enum.Enum, shape=1):
		IDLE = 0b0
		RUNNING = 0b1
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		constants = self.__constants
		FORMAL = constants.FORMAL()
		bus = self.bus().bus
		inp = bus.inp
		outp = bus.outp

		zero_d = inp.denom == 0
		State = LongDivMultiCycle.State

		loc = Blank()
		# Submodules go here
		loc.m = [LongUdivIter(constants=constants)]
		m.submodules += loc.m
		loc.state = Signal(
			#shape=Shape.cast(State),
			State.as_shape(),
			reset=State.IDLE,
			attrs=sig_keep()
		)
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

		if FORMAL:
			loc.formal = Blank()
			loc.formal.past_valid = Signal(
				attrs=sig_keep(),
				name="formal_past_valid"
			)
			past_valid = loc.formal.past_valid
		#--------
		it_bus = loc.m[0].bus().bus
		CHUNK_WIDTH = constants.CHUNK_WIDTH()
		chunk_start = it_bus.chunk_start
		itd_in = it_bus.itd_in
		itd_out = it_bus.itd_out
		#--------
		if FORMAL:
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
						# not `FORMAL` is true.
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
			if FORMAL:
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
				#					itd_in.shape().formal_dml_elem(
				#						itd_in, i, FORMAL,
				#					)
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
				#							itd_in.shape().formal_dml_elem(
				#								itd_in, i, FORMAL
				#							)
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
				#				#		itd_in.shape().formal_dml_elem(itd_in, i)
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
				#				#			itd_in.shape().formal_dml_elem(
				#				#				itd_in, 1, FORMAL,
				#				#			)
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
							itd_in.shape().formal_dml_elem(
								itd_in, i, FORMAL
							).eq(loc.temp_denom * i)
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
									itd_in.shape().formal_dml_elem(
										itd_in, i, FORMAL
									) == (
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
	def __init__(
		self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH,
		*, FORMAL=False, signed_reset=0b0,
		USE_PIPE_SKID_BUF=False,
	):
		self.__constants = LongDivConstants(
			MAIN_WIDTH=MAIN_WIDTH,
			DENOM_WIDTH=DENOM_WIDTH,
			CHUNK_WIDTH=CHUNK_WIDTH,
			# This `TAG_WIDTH` is just a heuristic
			#TAG_WIDTH=math.ceil(
			#	math.log2(MAIN_WIDTH // CHUNK_WIDTH) + 2
			#	#math.log2(MAIN_WIDTH // CHUNK_WIDTH)
			#),
			TAG_WIDTH=(
				CHUNK_WIDTH * ((MAIN_WIDTH // CHUNK_WIDTH) + 1)
			),
			PIPELINED=True,
			USE_PIPE_SKID_BUF=USE_PIPE_SKID_BUF,
			FORMAL=FORMAL
		)
		self.__bus = LongDivBus(self.__constants, signed_reset)
		#self.__USE_PIPE_SKID_BUF = USE_PIPE_SKID_BUF
	#--------
	def bus(self):
		return self.__bus
	#def USE_PIPE_SKID_BUF(self):
	#	return self.__USE_PIPE_SKID_BUF
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		constants = self.__constants
		#USE_PIPE_SKID_BUF = self.__USE_PIPE_SKID_BUF
		USE_PIPE_SKID_BUF = constants.USE_PIPE_SKID_BUF()
		bus = self.bus().bus
		inp = bus.inp
		outp = bus.outp

		#NUM_PSTAGES = constants.NUM_CHUNKS() + 1
		NUM_PSTAGES = constants.NUM_CHUNKS()
		NUM_PS_ELEMS = NUM_PSTAGES + 1

		loc = Blank()
		# Submodules go here
		loc.m = [
			LongUdivIterSync(
				constants=constants,
				chunk_start_val=(NUM_PSTAGES - 1) - i,
				#USE_PIPE_SKID_BUF=USE_PIPE_SKID_BUF,
				#next_intf_tag=psconcat(i),
				#prev_intf_tag=psconcat(i + 1),
				intf_tag_dct={
					"next": psconcat(i + 1),
					"prev": psconcat(i),
					#"prev": psconcat(i - 1),
				},
			)
			for i in range(NUM_PSTAGES)
		]
		#with open("tag_dct-LongDivPipelined.txt.ignore", "w") as f:
		#	for i in range(len(loc.m)):
		#		intf_tag_dct = loc.m[i].intf_tag_dct()
		#		f.writelines([
		#			psconcat(i, " ", intf_tag_dct, "\n"),
		#		])
		#m.submodules += loc.m
		for i in range(len(loc.m)):
			setattr(m.submodules, psconcat("udiv_its_", i), loc.m[i])
		temp_shape = {}
		temp_shape["temp_numer"] = constants.MAIN_WIDTH()
		temp_shape["temp_denom"] = constants.DENOM_WIDTH()

		temp_shape["numer_was_lez"] = NUM_PS_ELEMS
		temp_shape["denom_was_lez"] = NUM_PS_ELEMS

		temp_shape["quot_will_be_lez"] = NUM_PS_ELEMS
		temp_shape["rema_will_be_lez"] = NUM_PS_ELEMS

		temp_shape["bus_szd_temp_q"] = len(outp.quot)
		temp_shape["bus_szd_temp_r"] = len(outp.rema)
		temp_shape["lez_bus_szd_temp_q"] = len(outp.quot)
		temp_shape["lez_bus_szd_temp_r"] = len(outp.rema)

		temp_shape["temp_bus_quot"] = len(outp.quot)
		temp_shape["temp_bus_rema"] = len(outp.rema)
		loc.t = Splitrec(temp_shape)
		loc.t_prev = Splitrec(temp_shape)
		#--------
		if constants.FORMAL():
			#--------
			loc.formal = Blank()
			#--------
			loc.formal.past_valid = Signal(
				attrs=sig_keep(),
				name="formal_past_valid"
			)
			past_valid = loc.formal.past_valid
			loc.formal.past_valid_2 = Signal(
				attrs=sig_keep(),
				name="formal_past_valid_2"
			)
			past_valid_2 = loc.formal.past_valid_2
			#--------
			#loc.formal.rst_cnt_lst = [
			#	Signal(
			#		#signed(math.ceil(math.log2(len(loc.m))) + 1),
			#		signed(constants.CHUNK_WIDTH() + 1),
			#		reset=i + 1,
			#		attrs=sig_keep(),
			#		name=f"formal_rst_cnt_lst_{i}",
			#	)
			#	for i in range(len(loc.m))
			#]
			#	#= View(
			#	#	ArrayLayout(
			#	#		unsigned(math.ceil(math.log2(len(loc.m)))),
			#	#		#math.ceil(math.log2(len(loc.m))),
			#	#		len(loc.m),
			#	#	),
			#	#	attrs=sig_keep(),
			#	#	name="formal_rst_cnt_arr"
			#	#)
			#rst_cnt_lst = loc.formal.rst_cnt_lst

			#loc.formal.rst_cnt_done = Signal(
			#	len(loc.m),
			#	attrs=sig_keep(),
			#	name="formal_rst_cnt_done",
			#)
			#rst_cnt_done = loc.formal.rst_cnt_done
			#--------
		#--------
		its_bus = []

		ifwd = []
		ibak = []
		ofwd = []
		obak = []
		ifwd_move = []
		ofwd_move = []
		ifwd_mvp = []
		ofwd_mvp = []
		itd_in = []
		itd_out = []
		#its_formal = []
		for i in range(len(loc.m)):
			its_bus.append(loc.m[i].bus().bus)
			#its_formal.append(its_bus[i].formal)
			ifwd.append(its_bus[i].sb_bus.inp.fwd)
			ibak.append(its_bus[i].sb_bus.inp.bak)
			ofwd.append(its_bus[i].sb_bus.outp.fwd)
			obak.append(its_bus[i].sb_bus.outp.bak)
			ifwd_move.append(
				#(1 if not USE_PIPE_SKID_BUF else 0)
				not USE_PIPE_SKID_BUF
				or (ifwd[i].valid & obak[i].ready)
			)
			ofwd_move.append(
				not USE_PIPE_SKID_BUF
				or (ofwd[i].valid & ibak[i].ready)
			)
			ifwd_mvp.append(
				#(1 if not USE_PIPE_SKID_BUF else 0)
				not USE_PIPE_SKID_BUF
				or (Past(ifwd[i].valid) & Past(obak[i].ready))
			)
			ofwd_mvp.append(
				not USE_PIPE_SKID_BUF
				or (Past(ofwd[i].valid) & Past(ibak[i].ready))
			)
			itd_in.append(ifwd[i].data)
			itd_out.append(ofwd[i].data)
		#--------
		# Connect the pipeline stages together
		if not USE_PIPE_SKID_BUF:
			m.d.comb += [
				itd_in[i + 1].eq(itd_out[i])
					for i in range(len(loc.m) - 1)
			]
		else: # if USE_PIPE_SKID_BUF:
			for i in range(len(loc.m) - 1):
				#m.d.comb += [
				#	# Forwards connections
				#	its_bus[i + 1].sb_bus.inp.fwd.eq(
				#		its_bus[i].sb_bus.outp.fwd
				#	),
				#	# Backwards connections
				#	its_bus[i].sb_bus.inp.bak.eq(
				#		its_bus[i + 1].sb_bus.outp.bak
				#	),
				#]
				sb_bus = its_bus[i].sb_bus
				sb_bus_next = its_bus[i + 1].sb_bus
				for j in range(len(
					sb_bus.outp.fwd.flattened()
				)):
					# Forwards connections
					m.d.comb += [
						sb_bus_next.inp.fwd.flattened()[j].eq(
							sb_bus.outp.fwd.flattened()[j]
						)
					]
				for j in range(len(
					sb_bus.inp.bak.flattened()
				)):
					# Backwards connections
					m.d.comb += [
						sb_bus.inp.bak.flattened()[j].eq(
							sb_bus_next.outp.bak.flattened()[j]
						)
					]
			m.d.comb += [
				its_bus[0].sb_bus.inp.fwd.valid.eq(0b1),
				its_bus[-1].sb_bus.inp.bak.ready.eq(0b1),
			]
			#--------
			#PipeSkidBuf.connect_parallel(
			#	parent=m,
			#	sb_bus_lst=[
			#		its_bus[i].sb_bus
			#		for i in range(len(loc.m))
			#	],
			#	tie_first_inp_fwd_valid=True,
			#	tie_last_inp_bak_ready=True,
			#	##lst_shrink=-2,
			#	#lst_shrink=-1,
			#	#lst_shrink=-2,
			#	#other_lst_shrink=-2,
			#	#lst_shrink=-3,
			#	lst_shrink=-3,
			#	#other_lst_shrink=-1,
			#	##other_lst_shrink=0,
			#	#other_lst_shrink=-1,
			#	#lst_shrink=-3,
			#	#other_lst_shrink=-2,
			#	#lst_shrink=0,
			#)
			#--------
			#for i in range(len(loc.m) - 1):
			#	sb_bus = its_bus[i].sb_bus
			#	sb_bus_next = its_bus[i + 1].sb_bus
			#	f = open(
			#		psconcat("debug-LongDivPipelined-", i, ".txt.ignore"),
			#		"w"
			#	)
			#	#its_bus[i].connect(
			#	#	other=its_bus[i + 1],
			#	#	m=m,
			#	#	kind=Splitintf.ConnKind.Parallel,
			#	#	f=f,
			#	#	use_tag=True,
			#	#	reduce_tag=False,
			#	#	lst_shrink=-1,
			#	#)
			#	sb_bus_next.inp.fwd.connect(
			#		other=sb_bus.outp.fwd,
			#		m=m,
			#		kind=Splitintf.ConnKind.Parallel,
			#		f=f,
			#		use_tag=True,
			#		reduce_tag=False,
			#		lst_shrink=-3,
			#	)
			#	sb_bus.inp.bak.connect(
			#		other=sb_bus_next.outp.bak,
			#		m=m,
			#		kind=Splitintf.ConnKind.Parallel,
			#		f=f,
			#		use_tag=True,
			#		reduce_tag=False,
			#		lst_shrink=-3,
			#	)
			#	f.close()

			#	###lst_shrink = -3
			#	##lst_shrink = -1
			#	##other_lst_shrink = None
			#	#lst_shrink = -3
			#	#other_lst_shrink = -3
			#	if i == 0:
			#		#lst_shrink = -1
			#		#other_lst_shrink = -1
			#		m.d.comb += sb_bus.inp.fwd.valid.eq(0b1)
			#	elif i + 1 == len(loc.m) - 1:
			#		#lst_shrink = -1
			#		#other_lst_shrink = None
			#		m.d.comb += sb_bus_next.inp.bak.ready.eq(0b1)
			#	#else:
			#	#	lst_shrink = -1
			#	#	other_lst_shrink = None

			#	#f = open(
			#	#	psconcat("debug-LongDivPipelined-", i, ".txt.ignore"),
			#	#	"w"
			#	#)
			#	#sb_bus.connect(
			#	#	other=sb_bus_next,
			#	#	m=m,
			#	#	kind=Splitintf.ConnKind.Parallel,
			#	#	f=f,
			#	#	use_tag=False,
			#	#	lst_shrink=lst_shrink,
			#	#	other_lst_shrink=other_lst_shrink,
			#	#)
			#	#f.close()
		#--------
		with m.If(
			#(not USE_PIPE_SKID_BUF)
			#|
			ifwd_move[0]
		):
			m.d.sync += [
				#--------
				loc.t.temp_numer.eq(Mux(inp.signed & inp.numer[-1],
					(~inp.numer) + 1, inp.numer)),
				loc.t.temp_denom.eq(Mux(inp.signed & inp.denom[-1],
					(~inp.denom) + 1, inp.denom)),
				#--------
				loc.t.numer_was_lez[0].eq(inp.signed & inp.numer[-1]),
				loc.t.denom_was_lez[0].eq(inp.signed & inp.denom[-1]),

				loc.t.quot_will_be_lez[0].eq(loc.t.numer_was_lez[0]
					!= loc.t.denom_was_lez[0]),
				# Implement C's rules for modulo's sign
				loc.t.rema_will_be_lez[0].eq(loc.t.numer_was_lez[0]),
				#--------
			]
		m.d.sync += loc.t_prev.eq(loc.t)
		m.d.comb += [
			#--------
			loc.t.bus_szd_temp_q.eq(Mux(
				ofwd_move[-1],
				itd_out[-1].temp_quot.as_value()[:len(outp.quot)],
				loc.t_prev.bus_szd_temp_q,
			)),
			loc.t.bus_szd_temp_r.eq(Mux(
				ofwd_move[-1],
				itd_out[-1].temp_rema.as_value()[:len(outp.rema)],
				loc.t_prev.bus_szd_temp_r,
			)),

			loc.t.lez_bus_szd_temp_q.eq(Mux(
				ofwd_move[-1],
				(~loc.t.bus_szd_temp_q) + 1,
				loc.t_prev.lez_bus_szd_temp_q,
			)),
			loc.t.lez_bus_szd_temp_r.eq(Mux(
				ofwd_move[-1],
				(~loc.t.bus_szd_temp_r) + 1,
				loc.t_prev.lez_bus_szd_temp_r,
			)),

			loc.t.temp_bus_quot.eq(Mux(
				ofwd_move[-1],
				Mux(
					loc.t.quot_will_be_lez,
					loc.t.lez_bus_szd_temp_q, loc.t.bus_szd_temp_q
				),
				loc.t_prev.temp_bus_quot,
			)),
			loc.t.temp_bus_rema.eq(Mux(
				ofwd_move[-1],
				Mux(
					loc.t.rema_will_be_lez,
					loc.t.lez_bus_szd_temp_r, loc.t.bus_szd_temp_r
				),
				loc.t_prev.temp_bus_rema,
			)),
			#--------
		]
		#--------
		for i in range(len(loc.m)):
			#with m.If(
			#	not USE_PIPE_SKID_BUF
			#	| i == 0
			#):
			#with m.If(
			#	not USE_PIPE_SKID_BUF
			#)
			#with m.If(ofwd_move[i + 1]):
			#with m.If(ofwd_move[i] & ifwd_move[i + 1]):
			#with m.If(ifwd_move[i + 1]):
			with m.If(ofwd_move[i]):
				m.d.sync += [
					loc.t.numer_was_lez[i + 1].eq(loc.t.numer_was_lez[i]),
					loc.t.denom_was_lez[i + 1].eq(loc.t.denom_was_lez[i]),
					loc.t.quot_will_be_lez[i + 1].eq(
						loc.t.quot_will_be_lez[i]
					),
					loc.t.rema_will_be_lez[i + 1].eq(
						loc.t.rema_will_be_lez[i]
					),
				]
		#--------
		with m.If(ifwd_move[0]):
			m.d.sync += [
				itd_in[0].temp_numer.eq(loc.t.temp_numer),

				itd_in[0].temp_quot.eq(0x0),
				itd_in[0].temp_rema.eq(0x0),

				itd_in[0].tag.eq(inp.tag),
			]
			m.d.sync += [
				itd_in[0].denom_mult_lut[i].eq(loc.t.temp_denom * i)
					for i in range(constants.DML_SIZE())
			]
		m.d.comb += [
			outp.quot.eq(loc.t.temp_bus_quot),
			outp.rema.eq(loc.t.temp_bus_rema),

			outp.tag.eq(itd_out[-1].tag)
		]
		#--------
		if constants.FORMAL():
			#--------
			skip_cond = itd_out[-1].formal.formal_denom.as_value() == 0
			#--------
			m.d.sync += [
				#--------
				past_valid.eq(0b1),
				past_valid_2.eq(past_valid),
				#--------
			]
			with m.If(ifwd_move[0]):
				m.d.sync += [
					#--------
					itd_in[0].formal.formal_numer.eq(loc.t.temp_numer),
					itd_in[0].formal.formal_denom.eq(loc.t.temp_denom),

					itd_in[0].formal.oracle_quot.eq(loc.t.temp_numer
						// loc.t.temp_denom),
					itd_in[0].formal.oracle_rema.eq(loc.t.temp_numer
						% loc.t.temp_denom),
					#--------
				]
				m.d.sync += [
					itd_in[0].shape().formal_dml_elem(
						itd_in[0], i, constants.FORMAL(),
					).eq(
						loc.t.temp_denom * i)
						for i in range(constants.DML_SIZE()
					)
				]
			#m.d.comb += [
			#	rst_cnt_done[i].eq(rst_cnt_lst[i] < 0)
			#	for i in range(len(loc.m))
			#]
			#with m.If(~ResetSignal()):
			#	for i in range(len(loc.m)):
			#		with m.If(~rst_cnt_done[i]):
			#			m.d.sync += [
			#				#--------
			#				rst_cnt_lst[i].eq(rst_cnt_lst[i] - 1),
			#				#--------
			#			]

			with m.If(
				~ResetSignal() & past_valid
				#& (
				#	0b1
				#	if not constants.USE_PIPE_SKID_BUF()
				#	else its_bus[-1].sb_bus.outp.fwd.valid
				#)
			):
				#--------
				#with m.If(ifwd_move[0]):
				with m.If(ifwd_mvp[0]):
					m.d.comb += [
						#--------
						Assert(loc.t.temp_numer
							== Mux(Past(inp.signed) & Past(inp.numer)[-1],
								(~Past(inp.numer)) + 1, Past(inp.numer))),
						Assert(loc.t.temp_denom
							== Mux(Past(inp.signed) & Past(inp.denom)[-1],
								(~Past(inp.denom)) + 1, Past(inp.denom))),
						#--------
						Assert(loc.t.numer_was_lez[0]
							== (Past(inp.signed) & Past(inp.numer)[-1])),
						Assert(loc.t.denom_was_lez[0]
							== (Past(inp.signed) & Past(inp.denom)[-1])),

						Assert(loc.t.quot_will_be_lez[0]
							== (Past(loc.t.numer_was_lez)[0]
								!= Past(loc.t.denom_was_lez)[0])),
						# Implement C's rules for modulo's sign
						Assert(loc.t.rema_will_be_lez[0]
							== Past(loc.t.numer_was_lez)[0]),
						#--------
					]
				#--------
				for i in range(len(loc.m)):
					#with m.If(ofwd_move[i + 1]):
					#with m.If(ifwd_move[i + 1]):
					with m.If(ofwd_move[i]):
					#with m.If(ifwd_move[i]):
						m.d.sync += [
							#--------
							Assert(
								loc.t.numer_was_lez[i + 1]
								== Past(loc.t.numer_was_lez)[i]
							),
							Assert(
								loc.t.denom_was_lez[i + 1]
								== Past(loc.t.denom_was_lez)[i]
							),
							Assert(
								loc.t.quot_will_be_lez[i + 1]
								== Past(loc.t.quot_will_be_lez)[i]
							),
							Assert(
								loc.t.rema_will_be_lez[i + 1]
								== Past(loc.t.rema_will_be_lez)[i]
							),
							#--------
						]
				#with m.If(past_valid_2):
				#	for i in range(len(loc.m) - 1):
				#		itd_in_formal_flat = (
				#			itd_in[i].formal.flattened()
				#		)
				#		itd_in_next_formal_flat = (
				#			itd_in[i + 1].formal.flattened()
				#		)
				#		m.d.sync += [
				#			Assert(
				#				itd_in_next_formal_flat[i]
				#				== Past(itd_in_formal_flat[i])
				#			)
				#			for i in range(len(itd_in_formal_flat))
				#		]
				#		m.d.sync += [
				#			#Assert(
				#			#	itd_in[i + 1].formal.as_value()
				#			#		== Past(itd_in[i].formal.as_value())
				#			#),
				#			Assert(
				#				itd_in[i + 1].tag
				#					== Past(itd_in[i].tag)
				#			),
				#		]
				#--------
				#with m.If(ifwd_move[0]):
				with m.If(ifwd_mvp[0]):
					m.d.sync += [
						#--------
						Assert(
							itd_in[0].temp_numer == Past(loc.t.temp_numer)
						),
						Assert(
							itd_in[0].temp_quot.as_value() == 0x0
						),
						Assert(
							itd_in[0].temp_rema.as_value() == 0x0
						),
						#--------
						Assert(
							itd_in[0].formal.formal_numer
								== Past(loc.t.temp_numer)
						),
						Assert(
							itd_in[0].formal.formal_denom
								== Past(loc.t.temp_denom)
						),

						Assert(
							itd_in[0].formal.oracle_quot.as_value()
							== (
								Past(loc.t.temp_numer)
								// Past(loc.t.temp_denom)
							)
						),
						Assert(
							itd_in[0].formal.oracle_rema.as_value()
							== (
								Past(loc.t.temp_numer)
								% Past(loc.t.temp_denom)
							)
						),
					]
				#with m.If(ofwd_move[-1]):
				with m.If(ofwd_mvp[-1]):
					m.d.comb += [
						#--------
						Assert(
							skip_cond
							| (
								itd_out[-1].temp_quot
									.as_value()[:len(outp.quot)]
								== itd_out[-1].formal.oracle_quot
									.as_value()[:len(outp.quot)]
							)
						),
						#Assert((skip_cond)
						#	| (itd_out[-1].temp_rema.as_value()
						#		== itd_out[-1].formal.oracle_rema.as_value())),
						Assert(
							skip_cond
							| (
								itd_out[-1].temp_rema
									.as_value()[:len(outp.rema)]
								== itd_out[-1].formal.oracle_rema
									.as_value()[:len(outp.rema)]
							)
						),
						#--------
					]
				#--------
		#--------
		return m
		#--------
	#--------
#--------
