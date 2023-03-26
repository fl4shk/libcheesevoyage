#!/usr/bin/env python3

import math

from amaranth import *
from amaranth.lib.data import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.sim import Simulator, Delay, Tick

from enum import Enum, auto

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
#--------
#dbg_sync_bus = None
#def dbg_printout(func_name):
#	if dbg_sync_bus is not None:
#		printout("dbg_printout(): ")
#		itd_out_sync = dbg_sync_bus.itd_out_sync
#
#		printout(func_name, ": ")
#		fields = itd_out_sync.fields()
#		for name in fields:
#			printout(Value.cast(fields[name]).name, " ",
#				fields[name], "; ",)
#		printout("\n\n")
#--------
class LongDivConstants:
	#--------
	def __init__(self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH, *,
		TAG_WIDTH=1, PIPELINED=False, FORMAL=False):
		self.__MAIN_WIDTH = MAIN_WIDTH
		self.__DENOM_WIDTH = DENOM_WIDTH
		self.__CHUNK_WIDTH = CHUNK_WIDTH
		self.__TAG_WIDTH = TAG_WIDTH
		self.__PIPELINED = PIPELINED
		self.__FORMAL = FORMAL
	#--------
	def MAIN_WIDTH(self):
		return self.__MAIN_WIDTH
	def DENOM_WIDTH(self):
		return self.__DENOM_WIDTH
	def CHUNK_WIDTH(self):
		return self.__CHUNK_WIDTH
	def TAG_WIDTH(self):
		return self.__TAG_WIDTH
	def PIPELINED(self):
		return self.__PIPELINED
	def FORMAL(self):
		return self.__FORMAL
	#--------
	def TEMP_T_WIDTH(self):
		return (self.CHUNK_WIDTH()
			* ((self.MAIN_WIDTH() // self.CHUNK_WIDTH()) + 1))
		#add_amount = 1 if not self.PIPELINED() else 2
		#return (self.CHUNK_WIDTH() 
		#	* ((self.MAIN_WIDTH() // self.CHUNK_WIDTH()) + add_amount))
	def build_temp_t(self, *, attrs=sig_keep(), name=""):
		#printout("build_temp_t(): ", name, "\n")
		#return Signal(self.CHUNK_WIDTH() * self.NUM_CHUNKS(), attrs=attrs,
		#	name=name)
		#ret = Packarr(
		#	Packarr.Shape(self.CHUNK_WIDTH(), self.NUM_CHUNKS()),
		#	attrs=attrs,
		#	name=name
		#)
		ret = View(
			ArrayLayout(unsigned(self.CHUNK_WIDTH()), self.NUM_CHUNKS()),
			attrs=attrs,
			name=name
		)
		#printout("build_temp_t(): ", ret.sig().name, "\n")
		return ret
	def build_chunk_start_t(self, attrs=sig_keep(), name_suffix=""):
		return Signal(
			shape=signed(self.CHUNK_WIDTH() + 1),
			attrs=attrs,
			name=psconcat("chunk_start", name_suffix)
		)

	def NUM_CHUNKS(self):
		return (self.TEMP_T_WIDTH() // self.CHUNK_WIDTH())

	def RADIX(self):
		return (1 << self.CHUNK_WIDTH())
	#--------
	def DML_ELEM_WIDTH(self):
		return (self.DENOM_WIDTH() + self.CHUNK_WIDTH())
	def DML_SIZE(self):
		return self.RADIX()
	#--------
	#def chunk_ws(self, temp_data, index):
	#	return temp_data.word_select(index, self.CHUNK_WIDTH())
	#--------
#--------
class LongUdivIterData(Splitrec):
	#--------
	def __init__(self, constants: LongDivConstants, io_str: str):
		#--------
		super().__init__()
		#printout("LongUdivIterData.__init__(): ", io_str, "\n")
		#dbg_printout("LongUdivIterData.__init__()")
		#--------
		self.__constants = constants
		self.__DML_ENTRY_WIDTH = constants.DML_ELEM_WIDTH()
		self.__FORMAL = constants.FORMAL()
		self.__PIPELINED = constants.PIPELINED()
		#--------
		build_temp_t = constants.build_temp_t
		#--------
		self.temp_numer = build_temp_t(name=f"temp_numer_{io_str}")
		#printout("self.temp_numer: ",
		#	self.temp_numer.extra_args_name(), " ",
		#	self.temp_numer.sig().name, "\n")
		self.temp_quot = build_temp_t(name=f"temp_quot_{io_str}")
		self.temp_rema = build_temp_t(name=f"temp_rema_{io_str}")
		#--------
		#self.denom_mult_lut = Packarr(
		#	Packarr.Shape(constants.DML_ELEM_WIDTH(),
		#		constants.DML_SIZE()),
		#	attrs=sig_keep(),
		#	name=f"denom_mult_lut_{io_str}"
		#)
		self.denom_mult_lut = View(
			ArrayLayout(
				unsigned(constants.DML_ELEM_WIDTH()),
				constants.DML_SIZE()
			),
			attrs=sig_keep(),
			name=f"denom_mult_lut_{io_str}"
		)
		#--------
		if self.__PIPELINED:
			self.tag = Signal(constants.TAG_WIDTH(), attrs=sig_keep(),
				name=f"tag_{io_str}")
		#--------
		if self.__FORMAL:
			#--------
			self.formal = Splitrec()
			#--------
			self.formal.formal_numer \
				= build_temp_t(name=f"formal_numer_{io_str}")
			self.formal.formal_denom \
				= build_temp_t(name=f"formal_denom_{io_str}")

			self.formal.oracle_quot \
				= build_temp_t(name=f"oracle_quot_{io_str}")
			self.formal.oracle_rema \
				= build_temp_t(name=f"oracle_rema_{io_str}")
			#--------
			#self.formal.formal_denom_mult_lut = Signal \
			#	((bus.DML_ELEM_WIDTH() * bus.DML_SIZE()), attrs=sig_keep(),
			#		name=f"formal_denom_mult_lut_{io_str}")
			#self.formal.formal_denom_mult_lut = Packarr(
			#	Packarr.Shape(constants.DML_ELEM_WIDTH(),
			#		constants.DML_SIZE()),
			#	attrs=sig_keep(),
			#	name=f"formal_denom_mult_lut_{io_str}"
			#)
			self.formal.formal_denom_mult_lut = View(
				ArrayLayout(
					unsigned(constants.DML_ELEM_WIDTH()),
					constants.DML_SIZE()
				),
				attrs=sig_keep(),
				name=f"formal_denom_mult_lut_{io_str}"
			)
			#--------
		#--------
	#--------
	#def dml_elem(self, index):
	#	#return self.denom_mult_lut.word_select(index,
	#	#	self.__DML_ENTRY_WIDTH)
	#	return self.denom_mult_lut[index]
	def formal_dml_elem(self, index):
		assert self.__FORMAL
		#return self.formal.formal_denom_mult_lut.word_select(index,
		#	self.__DML_ENTRY_WIDTH)
		return self.formal.formal_denom_mult_lut[index]
	#--------
#--------
class LongUdivIterBus:
	#--------
	def __init__(self, constants: LongDivConstants):
		#--------
		self.__constants = constants
		#--------
		# Inputs

		#dbg_printout("LongUdivIterBus().__init__()")

		# The `io_str` argument is for the Verilog output's signals to have
		# a suffix in the names of signals that prevents conflicts with
		# `pst_out`'s signals' names.
		self.itd_in = LongUdivIterData(constants=constants, io_str="in")

		#printout("testificate: ", dbg_sync_bus, "\n")
		#dbg_printout("LongUdivIterBus().__init__()")

		self.chunk_start = constants.build_chunk_start_t()
		#--------
		# Outputs

		# The `io_str` argument is for the Verilog output's signals to have
		# a suffix in the names of signals that prevents conflicts with
		# `pst_in`'s signals' names.
		self.itd_out = LongUdivIterData(constants=constants, io_str="out")
		#--------
		# Current quotient digit
		self.quot_digit = Signal(constants.CHUNK_WIDTH(), attrs=sig_keep())

		# Remainder with the current chunk of `self.ps_data_in.temp_numer`
		# shifted in
		self.shift_in_rema = constants.build_temp_t(name="shift_in_rema")

		# The vector of greater than comparison values
		self.gt_vec = Signal(constants.RADIX(), attrs=sig_keep())
		#--------
	#--------
	def constants(self):
		return self.__constants
	#--------
#--------
# The combinational logic for an iteration of `LongUdiv`
class LongUdivIter(Elaboratable):
	#--------
	def __init__(self, constants: LongDivConstants):
		#dbg_printout("LongUdivIter.__init__()")
		self.__bus = LongUdivIterBus(constants=constants)

		#dbg_printout("LongUdivIter.__init__()")
	#--------
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()

		itd_in = bus.itd_in
		itd_out = bus.itd_out

		constants = bus.constants()
		#--------
		# Shift in the current chunk of `itd_in.temp_numer`
		m.d.comb += bus.shift_in_rema.eq(Cat(
			itd_in.temp_numer[bus.chunk_start],
			itd_in.temp_rema[:constants.TEMP_T_WIDTH()
				- constants.CHUNK_WIDTH()]
		))

		# Compare every element of the computed `denom * digit` array to
		# `shift_in_rema`, computing `gt_vec`.
		# This creates perhaps a single LUT delay for the greater-than
		# comparisons given the existence of hard carry chains in FPGAs.
		for i in range(constants.RADIX()):
			m.d.comb += bus.gt_vec[i].eq(itd_in.denom_mult_lut[i]
				> bus.shift_in_rema)

		# Find the current quotient digit with something resembling a
		# priority encoder.
		# This implements binary search.
		with m.Switch(bus.gt_vec):
			for i in range(len(bus.gt_vec)):
				with m.Case(("1" * (len(bus.gt_vec) - (i + 1)))
					+ ("0" * (i + 1))):
					m.d.comb += bus.quot_digit.eq(i)
			with m.Default():
				m.d.comb += bus.quot_digit.eq(0)

			# Here is an example of the expanded form of this `m.Switch()`
			#with m.Case("1110"):
			#	m.d.comb += bus.quot_digit.eq(0)
			#with m.Case("1100"):
			#	m.d.comb += bus.quot_digit.eq(1)
			#with m.Case("1000"):
			#	m.d.comb += bus.quot_digit.eq(2)
			#with m.Case("0000"):
			#	m.d.comb += bus.quot_digit.eq(3)
			#with m.Default():
			#	m.d.comb += bus.quot_digit.eq(0)

		# Drive `itd_out.temp_quot` and `itd_out.temp_rema`
		for i in range(constants.NUM_CHUNKS()):
			with m.If(bus.chunk_start == i):
				m.d.comb += itd_out.temp_quot[i].eq(bus.quot_digit)
			with m.Else(): # If(bus.chunk_start != i):
				m.d.comb += itd_out.temp_quot[i].eq(itd_in.temp_quot[i])

		m.d.comb += [
			#--------
			itd_out.temp_numer.eq(itd_in.temp_numer),
			itd_out.temp_rema.eq(bus.shift_in_rema
				- itd_in.denom_mult_lut[bus.quot_digit]),
			#--------
			itd_out.denom_mult_lut.eq(itd_in.denom_mult_lut),
			#--------
		]
		#--------
		if constants.FORMAL():
			#--------
			formal_numer_in = itd_in.formal.formal_numer
			formal_denom_in = itd_in.formal.formal_denom
			skip_cond = ((formal_denom_in == 0) | (bus.chunk_start < 0))

			oracle_quot_in = itd_in.formal.oracle_quot
			oracle_rema_in = itd_in.formal.oracle_rema

			formal_denom_mult_lut_in = itd_in.formal.formal_denom_mult_lut


			formal_numer_out = itd_out.formal.formal_numer
			formal_denom_out = itd_out.formal.formal_denom

			oracle_quot_out = itd_out.formal.oracle_quot
			oracle_rema_out = itd_out.formal.oracle_rema

			formal_denom_mult_lut_out \
				= itd_out.formal.formal_denom_mult_lut
			#--------
			m.d.comb += [
				#--------
				Assert(oracle_quot_in
					== (formal_numer_in // formal_denom_in)),
				Assert(oracle_rema_in
					== (formal_numer_in % formal_denom_in)),
				#--------
			]

			for i in range(constants.RADIX()):
				m.d.comb += [
					#--------
					Assert(itd_in.formal_dml_elem(i)
						== (formal_denom_in * i)),
					#--------
				]
			with m.If(~ResetSignal()):
				#--------
				m.d.comb += [
					#--------
					formal_numer_out.eq(formal_numer_in),
					formal_denom_out.eq(formal_denom_in),

					oracle_quot_out.eq(oracle_quot_in),
					oracle_rema_out.eq(oracle_rema_in),
					#--------
					itd_out.formal.formal_denom_mult_lut
						.eq(formal_denom_mult_lut_in),
					#--------
				]
				#--------
				m.d.comb += [
					#--------
					Assert(skip_cond
						| (bus.quot_digit
							== oracle_quot_in[bus.chunk_start])),
					#--------
				]

				# If we are the last pipeline stage (or if we are
				# multi-cycle and computing the last chunk of quotient and
				# final remainder), check to see if our answer is correct  
				with m.If(bus.chunk_start == 0x0):
					m.d.comb += [
						#--------
						Assert(skip_cond
							| (itd_out.temp_quot
								== (formal_numer_in // formal_denom_in))),
						Assert(skip_cond
							| (itd_out.temp_rema
								== (formal_numer_in % formal_denom_in))),
						#--------
					]
				#--------
			#--------
		#--------
		return m
		#--------
	#--------
#--------
class LongUdivIterSyncBus:
	#--------
	def __init__(self, constants: LongDivConstants):
		#--------
		super().__init__()
		#--------
		self.__constants = constants
		self.itd_in = LongUdivIterData(
			constants=constants,
			io_str="in_sync",
		)
		self.itd_out = LongUdivIterData(
			constants=constants,
			io_str="out_sync",
		)
		#self.chunk_start \
		#	= self.__constants.build_chunk_start_t(name_suffix="_sync")
		#printout("LongUdivIterSyncBus.__init__(): ",
		#	[Value.cast(val).name for val in self.itd_in_sync.flattened()],
		#	"\n")
		#--------
		if constants.FORMAL():
			#--------
			self.formal = Blank()
			#--------
			self.formal.past_valid = Signal(reset=0b0, attrs=sig_keep(),
				name="formal_past_valid")
			#--------
		#--------
	#--------
	def constants(self):
		return self.__constants
	#--------
#--------
class LongUdivIterSync(Elaboratable):
	#--------
	def __init__(self, constants: LongDivConstants, chunk_start_val: int):
		self.__constants = constants

		self.__bus = LongUdivIterSyncBus(constants=constants)
		self.__chunk_start_val = chunk_start_val
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
		it = LongUdivIter(constants=bus.constants())
		m.submodules += it
		#--------
		it_bus = it.bus()
		itd_in = it_bus.itd_in
		itd_out = it_bus.itd_out

		itd_in_sync = bus.itd_in
		itd_out_sync = bus.itd_out

		constants = bus.constants()
		#--------
		m.d.comb += [
			itd_in.eq(itd_in_sync),
			it_bus.chunk_start.eq(self.__chunk_start_val),
		]
		m.d.sync += itd_out_sync.eq(itd_out)
		#--------
		if constants.FORMAL():
			#--------
			skip_cond = itd_in.formal.formal_denom == 0
			past_valid = bus.formal.past_valid
			#--------
			m.d.sync += past_valid.eq(0b1),
			#--------
			with m.If((~ResetSignal()) & past_valid):
				#--------
				m.d.comb += [
					#--------
					Assert(itd_in.temp_numer == itd_in_sync.temp_numer),

					Assert(itd_in.temp_quot == itd_in_sync.temp_quot),
					Assert(itd_in.temp_rema == itd_in_sync.temp_rema),

					Assert(itd_in.tag == itd_in_sync.tag),

					Assert(itd_in.formal.formal_numer
						== itd_in_sync.formal.formal_numer),
					Assert(itd_in.formal.formal_denom
						== itd_in_sync.formal.formal_denom),

					Assert(itd_in.formal.oracle_quot
						== itd_in_sync.formal.oracle_quot),
					Assert(itd_in.formal.oracle_rema
						== itd_in_sync.formal.oracle_rema),
					#--------
					Assert(itd_out_sync.temp_numer
						== Past(itd_in.temp_numer)),
					#Assert(itd_out_sync.formal == Past(itd_in.formal)),
					Assert(itd_out_sync.formal.formal_numer
						== Past(itd_in.formal.formal_numer)),
					Assert(itd_out_sync.formal.formal_denom
						== Past(itd_in.formal.formal_denom)),

					Assert(itd_out_sync.formal.oracle_quot
						== Past(itd_in.formal.oracle_quot)),
					Assert(itd_out_sync.formal.oracle_rema
						== Past(itd_in.formal.oracle_rema)),

					Assert(itd_out_sync.formal.formal_denom_mult_lut
						== Past(itd_in.formal.formal_denom_mult_lut)),
					#--------
					Assert(itd_out_sync.temp_numer
						== Past(itd_in_sync.temp_numer)),

					Assert(itd_out_sync.temp_quot
						== Past(itd_out.temp_quot)),
					Assert(itd_out_sync.temp_rema
						== Past(itd_out.temp_rema)),
					#--------
					Assert(itd_out_sync.denom_mult_lut
						== Past(itd_in_sync.denom_mult_lut)),
					#--------
					Assert(itd_out_sync.formal.formal_numer
						== Past(itd_in_sync.formal.formal_numer)),
					Assert(itd_out_sync.formal.formal_denom
						== Past(itd_in_sync.formal.formal_denom)),

					Assert(itd_out_sync.formal.oracle_quot
						== Past(itd_in_sync.formal.oracle_quot)),
					Assert(itd_out_sync.formal.oracle_rema
						== Past(itd_in_sync.formal.oracle_rema)),
					#--------
					Assert(itd_out_sync.formal.formal_denom_mult_lut
						== Past(itd_in_sync.formal.formal_denom_mult_lut)),
					#--------
				]
			#--------
		#--------
		return m
		#--------
#--------
