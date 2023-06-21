#!/usr/bin/env python3

import math

from amaranth import *
from amaranth.lib.data import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.sim import Simulator, Delay, Tick

from enum import Enum, auto

from libcheesevoyage.misc_util import psconcat, sig_keep, Blank
from libcheesevoyage.general.container_types import (
	cast_shape, SigInfo, FieldInfo, Splitrec,
	PortDir, Modport, IntfShape, Splitintf, IntfarrShape, Splitintfarr
)
#from libcheesevoyage.general.general_types import SigInfo
#from libcheesevoyage.general.pipeline_mods import (
#	PipeSkidBufBus, PipeSkidBuf
#)
import libcheesevoyage.general.pipeline_mods as pipeline_mods
#from libcheesevoyage.general.container_types import *
#from libcheesevoyage.general.pipeline_mods import \
#	PstageBusFwdLayt, PstageBusBakLayt, PipeSkidBuf, PipeSkidBufBus
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
	def __init__(
		self, MAIN_WIDTH, DENOM_WIDTH, CHUNK_WIDTH, *,
		TAG_WIDTH=1, PIPELINED=False, USE_PIPE_SKID_BUF=True,
		FORMAL=False
	):
		self.__MAIN_WIDTH = MAIN_WIDTH
		self.__DENOM_WIDTH = DENOM_WIDTH
		self.__CHUNK_WIDTH = CHUNK_WIDTH
		self.__TAG_WIDTH = TAG_WIDTH
		self.__PIPELINED = PIPELINED
		self.__USE_PIPE_SKID_BUF = USE_PIPE_SKID_BUF
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
	def USE_PIPE_SKID_BUF(self):
		return self.__USE_PIPE_SKID_BUF
	def FORMAL(self):
		return self.__FORMAL
	#--------
	def TEMP_T_WIDTH(self):
		return (
			self.CHUNK_WIDTH()
			* ((self.MAIN_WIDTH() // self.CHUNK_WIDTH()) + 1)
		)
		#add_amount = 1 if not self.PIPELINED() else 2
		#return (self.CHUNK_WIDTH() 
		#	* ((self.MAIN_WIDTH() // self.CHUNK_WIDTH()) + add_amount))
	def build_temp_shape(
		self,
		*,
		attrs=sig_keep(),
		name="",
		in_wrapper: bool=False,
	):
		#printout("build_temp_shape(): ", name, "\n")
		#return Signal(self.CHUNK_WIDTH() * self.NUM_CHUNKS(), attrs=attrs,
		#	name=name)
		#ret = Signal(self.CHUNK_WIDTH() * self.NUM_CHUNKS(), attrs=attrs,
		#	name=name)
		#ret = Packarr(
		#	Packarr.Shape(self.CHUNK_WIDTH(), self.NUM_CHUNKS()),
		#	attrs=attrs,
		#	name=name
		#)
		#ret = Splitrec.View(
		#	ArrayLayout(unsigned(self.CHUNK_WIDTH()), self.NUM_CHUNKS()),
		#	attrs=attrs,
		#	name=name,
		#)
		ret = FieldInfo(
			(
				ArrayLayout(
					unsigned(self.CHUNK_WIDTH()),
					self.NUM_CHUNKS()
				) if not in_wrapper
				else unsigned(self.CHUNK_WIDTH() * self.NUM_CHUNKS())
			),
			attrs=attrs,
			name=name,
			#use_parent_name=True,
			use_parent_name=False,
		)
		#printout("build_temp_shape(): ", ret.sig().name, "\n")
		return ret
	def build_chunk_start_shape(self, attrs=sig_keep(), name_suffix=""):
		return FieldInfo(
			signed(self.CHUNK_WIDTH() + 1),
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
#class LongUdivIterData(Splitrec):
class LongUdivIterDataLayt(dict):
	#--------
	def __init__(
		self,
		constants: LongDivConstants,
		io_str: str,
		in_wrapper: bool=False,
	):
		#--------
		#super().__init__()
		#printout("LongUdivIterDataLayt.__init__(): ", io_str, "\n")
		#dbg_printout("LongUdivIterDataLayt.__init__()")
		#--------
		#self.__constants = constants
		#self.__DML_ENTRY_WIDTH = constants.DML_ELEM_WIDTH()
		#self.__FORMAL = constants.FORMAL()
		#self.__PIPELINED = constants.PIPELINED()
		#--------
		build_temp_shape = constants.build_temp_shape
		shape = {}
		#--------
		shape["temp_numer"] = build_temp_shape(
			name=f"temp_numer_{io_str}",
			in_wrapper=in_wrapper,
		)
		#printout("self.temp_numer: ",
		#	self.temp_numer.extra_args_name(), " ",
		#	self.temp_numer.sig().name, "\n")
		shape["temp_quot"] = build_temp_shape(
			name=f"temp_quot_{io_str}",
			in_wrapper=in_wrapper,
		)
		shape["temp_rema"] = build_temp_shape(
			name=f"temp_rema_{io_str}",
			in_wrapper=in_wrapper,
		)
		#--------
		#self.denom_mult_lut = Packarr(
		#	Packarr.Shape(constants.DML_ELEM_WIDTH(),
		#		constants.DML_SIZE()),
		#	attrs=sig_keep(),
		#	name=f"denom_mult_lut_{io_str}"
		#)
		#self.denom_mult_lut = Splitrec.View(
		#	ArrayLayout(unsigned(constants.DML_ELEM_WIDTH()),
		#		constants.DML_SIZE()),
		#	attrs=sig_keep(),
		#	name=f"denom_mult_lut_{io_str}"
		#)
		shape["denom_mult_lut"] = FieldInfo(
			(
				ArrayLayout(
					unsigned(constants.DML_ELEM_WIDTH()),
					constants.DML_SIZE()
				) if not in_wrapper
				else unsigned(
					constants.DML_ELEM_WIDTH()
					* constants.DML_SIZE()
				)
			),
			attrs=sig_keep(),
			name=f"denom_mult_lut_{io_str}"
		)
		#self.denom_mult_lut = Signal(
		#	constants.DML_ELEM_WIDTH() * constants.DML_SIZE(),
		#	attrs=sig_keep(),
		#	name=f"denom_mult_lut_{io_str}"
		#)
		#--------
		if constants.PIPELINED():
			shape["tag"] = FieldInfo(
				constants.TAG_WIDTH(),
				attrs=sig_keep(),
				name=f"tag_{io_str}"
			)
		#--------
		if constants.FORMAL():
			#--------
			#self.formal = Splitrec()
			shape["formal"] = {}
			#self.
			#--------
			shape["formal"]["formal_numer"] = (
				build_temp_shape(
					name=f"formal_numer_{io_str}",
					in_wrapper=in_wrapper,
				)
			)
			shape["formal"]["formal_denom"] = (
				build_temp_shape(
					name=f"formal_denom_{io_str}",
					in_wrapper=in_wrapper,
				)
			)

			shape["formal"]["oracle_quot"] = (
				build_temp_shape(
					name=f"oracle_quot_{io_str}",
					in_wrapper=in_wrapper,
				)
			)
			shape["formal"]["oracle_rema"] = (
				build_temp_shape(
					name=f"oracle_rema_{io_str}",
					in_wrapper=in_wrapper,
				)
			)
			#--------
			#self.formal.formal_denom_mult_lut = Signal(
			#	(bus.DML_ELEM_WIDTH() * bus.DML_SIZE()),
			#	attrs=sig_keep(),
			#		name=f"formal_denom_mult_lut_{io_str}"
			#)
			#self.formal.formal_denom_mult_lut = Packarr(
			#	Packarr.Shape(constants.DML_ELEM_WIDTH(),
			#		constants.DML_SIZE()),
			#	attrs=sig_keep(),
			#	name=f"formal_denom_mult_lut_{io_str}"
			#)
			#self.formal.formal_denom_mult_lut = Signal(
			#	constants.DML_ELEM_WIDTH() * constants.DML_SIZE(),
			#	attrs=sig_keep(),
			#	name=f"formal_denom_mult_lut_{io_str}"
			#)
			#self.formal.formal_denom_mult_lut = Splitrec.View(
			#	ArrayLayout(unsigned(constants.DML_ELEM_WIDTH()),
			#		constants.DML_SIZE()),
			#	attrs=sig_keep(),
			#	name=f"formal_denom_mult_lut_{io_str}"
			#)
			shape["formal"]["formal_denom_mult_lut"] = FieldInfo(
				(
					ArrayLayout(
						unsigned(constants.DML_ELEM_WIDTH()),
						constants.DML_SIZE()
					) if not in_wrapper
					else unsigned(
						constants.DML_ELEM_WIDTH()
						* constants.DML_SIZE()
					)
				),
				attrs=sig_keep(),
				name=f"formal_denom_mult_lut_{io_str}"
			)
			#--------
		#--------
		super().__init__(shape)
	#--------
	#def constants(self):
	#	return self.__constants
	#def DML_ENTRY_WIDTH(self):
	#	return self.__DML_ENTRY_WIDTH
	#def FORMAL(self):
	#	return self.__FORMAL
	#def PIPELINED(self):
	#	return self.__PIPELINED
	#def dml_elem(self, index):
	#	#return self.denom_mult_lut.word_select(index,
	#	#	self.__DML_ENTRY_WIDTH)
	#	return self.denom_mult_lut[index]
	#def formal_dml_elem(self, index):
	@staticmethod
	def formal_dml_elem(splitrec, index, FORMAL: bool):
		##assert self.__FORMAL
		#assert splitrec.shape().FORMAL()
		assert FORMAL
		##return self.formal.formal_denom_mult_lut.word_select(index,
		##	self.__DML_ENTRY_WIDTH)
		##return Value.cast(self.formal.formal_denom_mult_lut[index])
		##return self.formal.formal_denom_mult_lut[index]
		return splitrec.formal.formal_denom_mult_lut[index]
		##return self.formal.formal_denom_mult_lut.as_value().word_select(
		##	index, self.__constants.CHUNK_WIDTH()
		##)
	#--------
#--------
class LongUdivIterIshape(IntfShape):
	#--------
	def __init__(self, constants: LongDivConstants):
		shape = {}
		mp_dct = {}
		#--------
		# Inputs

		#dbg_printout("LongUdivIterBus().__init__()")

		# The `io_str` argument is for the Verilog output's signals to have
		# a suffix in the names of signals that prevents conflicts with
		# `pst_out`'s signals' names.
		shape["itd_in"] = FieldInfo(
			LongUdivIterDataLayt(constants=constants, io_str="in"),
			use_parent_name=False
		)
		mp_dct["itd_in"] = PortDir.Inp

		#printout("testificate: ", dbg_sync_bus, "\n")
		#dbg_printout("LongUdivIterBus().__init__()")

		shape["chunk_start"] = constants.build_chunk_start_shape()
		mp_dct["chunk_start"] = PortDir.Inp
		#--------
		# Outputs

		# The `io_str` argument is for the Verilog output's signals to have
		# a suffix in the names of signals that prevents conflicts with
		# `pst_in`'s signals' names.
		shape["itd_out"] = FieldInfo(
			LongUdivIterDataLayt(constants=constants, io_str="out"),
			use_parent_name=False
		)
		mp_dct["itd_out"] = PortDir.Outp
		#--------
		# Current quotient digit
		shape["quot_digit"] = FieldInfo(
			constants.CHUNK_WIDTH(),
			attrs=sig_keep()
		)
		mp_dct["quot_digit"] = PortDir.Outp

		# Remainder with the current chunk of `self.ps_data_in.temp_numer`
		# shifted in
		shape["shift_in_rema"] = FieldInfo(
			constants.build_temp_shape(name="shift_in_rema"),
			use_parent_name=False
		)
		mp_dct["shift_in_rema"] = PortDir.Outp

		# The vector of greater than comparison values
		shape["gt_vec"] = FieldInfo(
			constants.RADIX(), attrs=sig_keep()
		)
		mp_dct["gt_vec"] = PortDir.Outp
		#--------
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
		)
	#--------
class LongUdivIterBus:
	#--------
	def __init__(self, constants: LongDivConstants):
		#self.__constants = constants
		ishape = LongUdivIterIshape(constants=constants)
		self.__constants = constants
		self.__bus = Splitintf(ishape, name="bus", use_parent_name=False)
		#super().__init__(ishape)
		#self.intf = Splitintf(ishape)
	#--------
	@property
	def bus(self):
		return self.__bus
	def constants(self):
		return self.__constants
	def CHUNK_WIDTH(self):
		return self.constants().CHUNK_WIDTH()
	##--------
	#def __getattribute__(self, key):
	#	if (
	#		(
	#			key[0] == "_"
	#			or key in LongUdivIterBus.__dict__
	#			or key == "intf"
	#		)
	#	):
	#		return super().__getattribute__(key)
	#	else:
	#		return getattr(self.intf, key)
	#--------
#--------
# The combinational logic for an iteration of `LongUdiv`
class LongUdivIter(Elaboratable):
	#--------
	def __init__(self, constants: LongDivConstants):
		#dbg_printout("LongUdivIter.__init__()")
		self.__constants = constants
		self.__bus = LongUdivIterBus(constants=constants)

		#dbg_printout("LongUdivIter.__init__()")
	#--------
	def constants(self):
		return self.__constants
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		constants = self.constants()
		FORMAL = constants.FORMAL()
		bus = self.bus().bus

		itd_in = bus.itd_in
		itd_out = bus.itd_out
		#--------
		# Shift in the current chunk of `itd_in.temp_numer`
		m.d.comb += bus.shift_in_rema.as_value().eq(Cat(
			itd_in.temp_numer[bus.chunk_start],
			#itd_in.temp_numer.as_value().word_select(
			#	bus.chunk_start, bus.CHUNK_WIDTH()
			#),
			itd_in.temp_rema.as_value()[:constants.TEMP_T_WIDTH() 
				- constants.CHUNK_WIDTH()]
			#itd_in.temp_rema[:constants.TEMP_T_WIDTH() 
			#	- constants.CHUNK_WIDTH()]
		))

		# Compare every element of the computed `denom * digit` array to
		# `shift_in_rema`, computing `gt_vec`.
		# This creates perhaps a single LUT delay for the greater-than
		# comparisons given the existence of hard carry chains in FPGAs.
		for i in range(constants.RADIX()):
			m.d.comb += bus.gt_vec[i].eq(
				itd_in.denom_mult_lut[i] > bus.shift_in_rema
			)

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
				#m.d.comb += itd_out.temp_quot.as_value().word_select(
				#	i, bus.CHUNK_WIDTH()
				#).eq(bus.quot_digit)
			with m.Else(): # If(bus.chunk_start != i):
				m.d.comb += itd_out.temp_quot[i].eq(
					itd_in.temp_quot[i]
				)
				#m.d.comb += itd_out.temp_quot.as_value().word_select(
				#	i, bus.CHUNK_WIDTH()
				#).eq(itd_in.temp_quot.as_value().word_select(
				#	i, bus.CHUNK_WIDTH()
				#))

		m.d.comb += [
			#--------
			itd_out.temp_numer.as_value().eq(itd_in.temp_numer.as_value()),
			itd_out.temp_rema.as_value().eq(bus.shift_in_rema.as_value()
				- itd_in.denom_mult_lut[bus.quot_digit]
				#- itd_in.denom_mult_lut.as_value().word_select
				#	(bus.quot_digit, bus.CHUNK_WIDTH())
			),
			#--------
			itd_out.denom_mult_lut.eq(itd_in.denom_mult_lut),
			#--------
		]
		#--------
		if FORMAL:
			#--------
			formal_numer_in = itd_in.formal.formal_numer
			formal_denom_in = itd_in.formal.formal_denom
			skip_cond = ((formal_denom_in.as_value() == 0)
				| (bus.chunk_start < 0))

			oracle_quot_in = itd_in.formal.oracle_quot
			oracle_rema_in = itd_in.formal.oracle_rema

			formal_denom_mult_lut_in = itd_in.formal.formal_denom_mult_lut

			formal_numer_out = itd_out.formal.formal_numer
			formal_denom_out = itd_out.formal.formal_denom

			oracle_quot_out = itd_out.formal.oracle_quot
			oracle_rema_out = itd_out.formal.oracle_rema

			formal_denom_mult_lut_out = (
				itd_out.formal.formal_denom_mult_lut
			)
			#--------
			m.d.comb += [
				#--------
				Assert(oracle_quot_in.as_value()
					== (formal_numer_in.as_value()
						// formal_denom_in.as_value())),
				Assert(oracle_rema_in.as_value()
					== (formal_numer_in.as_value()
						% formal_denom_in.as_value())),
				#--------
			]

			m.d.comb += [
				Assert(
					itd_in.shape().formal_dml_elem(itd_in, i, FORMAL)
					== (formal_denom_in.as_value() * i)
				)
				for i in range(constants.RADIX())
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
					itd_out.formal.formal_denom_mult_lut.eq(
						formal_denom_mult_lut_in
					),
					#--------
				]
				#--------
				m.d.sync += [
					#--------
					Assert(
						skip_cond
						| (
							bus.quot_digit
							== oracle_quot_in[bus.chunk_start]
							#== oracle_quot_in.as_value().word_select
							#	(bus.chunk_start, bus.CHUNK_WIDTH())
						)
					),
					#--------
				]

				# If we are the last pipeline stage (or if we are
				# multi-cycle and computing the last chunk of quotient and
				# final remainder), check to see if our answer is correct  
				with m.If(bus.chunk_start <= 0x0):
					m.d.sync += [
						#--------
						Assert(skip_cond
							| (itd_out.temp_quot
								== (
									formal_numer_in.as_value()
									// formal_denom_in.as_value()
								))),
						Assert(skip_cond
							| (itd_out.temp_rema
								== (
									formal_numer_in.as_value()
									% formal_denom_in.as_value()
								))),
						#--------
					]
				#--------
			#--------
		#--------
		return m
		#--------
	#--------
#--------
class LongUdivIterSyncIshape(IntfShape):
	#--------
	def __init__(
		self,
		constants: LongDivConstants,
		#data_info: SigInfo,
		#USE_PIPE_SKID_BUF=True,
		*,
		#next_intf_tag=None,
		#prev_intf_tag=None,
		intf_tag_dct={
			"next": None,
			"prev": None,
		},
		in_wrapper: bool=False,
	):
		#--------
		#temp_tag_dct = intf_tag_dct.copy()
		temp_tag_dct = intf_tag_dct
		temp_tag_dct["misc"] = None
		#--------
		#super().__init__()
		shape = {}
		#--------
		#self.__constants = constants
		#self.__data_info = SigInfo(
		#	basenm="itd",
		#	shape=LongUdivIterDataLayt(
		#		constants=constants,
		#		io_str="in_sync",
		#	),
		#	ObjKind=Splitrec,
		#	use_parent_name=False
		#)
		##self.itd_in = self.__itd_in_data_info.mk_sig()
		##self.itd_in = self.__data_info.mk_sig(suffix="_in")

		###if constants.USE_PIPE_SKID_BUF():
		###	self.nodata_out = Splitrec(
		###		PstageOutpNodataLayt(),
		###		use_parent_name=False
		###	)
		###self.__itd_out_data_info = SigInfo(
		###	basenm="itd_out",
		###	shape=LongUdivIterDataLayt(
		###		constants=constants,
		###		io_str="out_sync",
		###	),
		###	ObjKind=Splitrec,
		###	use_parent_name=False
		###)
		###self.itd_out = self.__itd_out_data_info.mk_sig()
		##self.itd_out = self.__data_info.mk_sig(suffix="_out")
		##if constants.USE_PIPE_SKID_BUF():
		shape["sb_bus"] = pipeline_mods.PipeSkidBufIshape(
			#data_info=LongUdivIterSync.mk_data_info(
			#	constants=constants,
			#	pdir=PortDir.Inp,
			#	in_wrapper=in_wrapper,
			#),
			inp_data_info=LongUdivIterSync.mk_data_info(
				constants=constants,
				pdir=PortDir.Inp,
				in_wrapper=in_wrapper,
			),
			outp_data_info=LongUdivIterSync.mk_data_info(
				constants=constants,
				pdir=PortDir.Outp,
				in_wrapper=in_wrapper,
			),
			OPT_INCLUDE_VALID_BUSY=False,
			OPT_INCLUDE_READY_BUSY=False,
			#OPT_INCLUDE_BUSY=False,
			#OPT_INCLUDE_BUSY=True,
			#next_tag=next_intf_tag,
			#prev_tag=prev_intf_tag,
			tag_dct=temp_tag_dct,
		)
			#self.inp.valid = Signal(
			#	1, name="valid_in"
			#)
		# TODO: the below two lines stuff need to be added to other code
		#self.itd_in = self.sb_bus.inp.fwd.data
		#self.itd_out = self.sb_bus.outp.fwd.data


		#self.inp = Blank()
		#self.outp = Blank()

		#self.chunk_start = self.__constants.build_chunk_start_t(
		#	name_suffix="_sync"
		#)
		#printout("LongUdivIterSyncBus.__init__(): ",
		#	[Value.cast(val).name for val in self.itd_in_sync.flattened()],
		#	"\n")
		#--------
		if constants.FORMAL():
			#--------
			#self.formal = Blank()
			#shape["formal"] = {}
			#--------
			#self.formal.past_valid = Signal(reset=0b0, attrs=sig_keep(),
			#	name="formal_past_valid")
			shape["formal"] = IntfShape(
				{
					"past_valid": FieldInfo(
						1,
						reset=0b0, attrs=sig_keep(),
						name="formal_past_valid",
						use_parent_name=False,
					)
				},
				modport=Modport({
					"past_valid": PortDir.Outp
				}),
			)
			#--------
		#--------
		super().__init__(shape=shape)
	#--------
	#def constants(self):
	#	return self.__constants
	##def itd_in_data_info(self):
	##	return self.__itd_in_data_info
	##def itd_out_data_info(self):
	##	return self.__itd_out_data_info
	#def data_info(self):
	#	return self.__data_info
	#--------
class LongUdivIterSyncBus:
	def __init__(
		self,
		constants: LongDivConstants,
		*,
		#next_intf_tag=None,
		#prev_intf_tag=None,
		intf_tag_dct={
			"next": None,
			"prev": None,
		}
	):
		ishape = LongUdivIterSyncIshape(
			constants=constants,
			#next_intf_tag=next_intf_tag,
			#prev_intf_tag=prev_intf_tag,
			intf_tag_dct=intf_tag_dct,
		)
		#super().__init__(ishape)
		self.__constants = constants
		self.__bus = Splitintf(ishape, name="bus", use_parent_name=False)
	@property
	def bus(self):
		return self.__bus
	def constants(self):
		return self.__constants
#--------
class LongUdivIterSync(Elaboratable):
	#--------
	def __init__(
		self,
		constants: LongDivConstants,
		chunk_start_val: int,
		*,
		#next_intf_tag=None,
		#prev_intf_tag=None,
		intf_tag_dct={
			"next": None,
			"prev": None,
		},
	):
		self.__constants = constants
		self.__intf_tag_dct = intf_tag_dct

		self.__chunk_start_val = chunk_start_val
		self.__bus = LongUdivIterSyncBus(
			constants=constants,
			#next_intf_tag=next_intf_tag,
			#prev_intf_tag=prev_intf_tag,
			intf_tag_dct=intf_tag_dct,
		)
	#--------
	def bus(self):
		return self.__bus
	def constants(self):
		return self.__constants
	def intf_tag_dct(self):
		return self.__intf_tag_dct
	def chunk_start_val(self):
		return self.__chunk_start_val
	@staticmethod
	def mk_data_info(
		constants: LongDivConstants,
		pdir: PortDir,
		*,
		in_wrapper: bool=False,
	):
		return SigInfo(
			basenm="itd",
			shape=LongUdivIterDataLayt(
				constants=constants,
				io_str=(
					"in_sync"
					if pdir == PortDir.Inp
					else "out_sync"
				),
				in_wrapper=in_wrapper,
			),
			ObjKind=Splitrec,
			use_parent_name=False,
			#use_parent_name=True,
		)
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus().bus
		constants = self.constants()
		chunk_start_val = self.chunk_start_val()
		FORMAL = constants.FORMAL()
		NUM_CHUNKS = constants.NUM_CHUNKS()
		USE_PIPE_SKID_BUF = constants.USE_PIPE_SKID_BUF()
		#--------
		it = LongUdivIter(constants=constants)
		m.submodules.udiv_it = it

		if constants.USE_PIPE_SKID_BUF():
			skid_buf = pipeline_mods.PipeSkidBuf(
				#data_info=LongUdivIterSync.mk_data_info(
				#	constants=constants,
				#	pdir=PortDir.Inp,
				#),
				inp_data_info=LongUdivIterSync.mk_data_info(
					constants=constants,
					pdir=PortDir.Inp,
				),
				outp_data_info=LongUdivIterSync.mk_data_info(
					constants=constants,
					pdir=PortDir.Outp,
				),
				#FORMAL=constants.FORMAL(),
				#FORMAL=False,
				FORMAL=FORMAL,
				OPT_INCLUDE_VALID_BUSY=False,
				OPT_INCLUDE_READY_BUSY=False,
				OPT_TIE_IFWD_VALID=(chunk_start_val == NUM_CHUNKS - 1),
				#OPT_TIE_IBAK_READY=(chunk_start_val == 0),
				#OPT_INCLUDE_BUSY=False,
				#OPT_PASSTHROUGH=FORMAL,
				#OPT_PASSTHROUGH=False,
				#OPT_PASSTHROUGH=True,
				#tag_dct=self.__intf_tag_dct,
			)
			m.submodules.skid_buf = skid_buf
			sb_bus = skid_buf.bus().bus
			#loc_itd_out = bus.data_info().mk_sig(
			#	basenm="loc_itd_out",
			#	prefix="",
			#	suffix="",
			#)
			#with open(
			#	"debug-long_div_iter_mods-psb.txt.ignore", "w"
			#) as f:
			#	f.writelines([
			#		psconcat(self.__intf_tag_dct)
			#	])
		#--------
		it_bus = it.bus().bus
		itd_in = it_bus.itd_in
		itd_out = it_bus.itd_out

		#itd_in_sync = bus.itd_in
		#itd_out_sync = bus.itd_out
		ifwd = bus.sb_bus.inp.fwd
		ibak = bus.sb_bus.inp.bak
		ofwd = bus.sb_bus.outp.fwd
		obak = bus.sb_bus.outp.bak
		ifwd_mvp = (
			(
				not USE_PIPE_SKID_BUF
			) or (
				Past(ifwd.valid) & Past(obak.ready)
				#ifwd.valid & obak.ready
			)
		)
		ofwd_mvp = (
			(
				not USE_PIPE_SKID_BUF
			) or (
				Past(ofwd.valid) & Past(ibak.ready)
				#ofwd.valid & ibak.ready
			)
		)

		itd_in_sync = ifwd.data
		itd_out_sync = ofwd.data

		#constants = bus.constants()
		#loc = Blank()
		#--------
		#if constants.FORMAL():
		#	#--------
		#	loc.formal = Blank()
		#	#--------
		#	loc.formal.rst_cnt = Signal(
		#		signed(constants.CHUNK_WIDTH() + 1),
		#		reset=self.__chunk_start_val + 1,
		#		attrs=sig_keep(),
		#		name=f"formal_rst_cnt_{self.__chunk_start_val}",
		#	)
		#	rst_cnt = loc.formal.rst_cnt

		#	loc.formal.rst_cnt_done = Signal(
		#		attrs=sig_keep(),
		#		name="formal_rst_cnt_done",
		#	)
		#	rst_cnt_done = loc.formal.rst_cnt_done
		#	#--------
		#--------
		#--------
		#m.d.comb += [
		#	itd_in.eq(itd_in_sync),
		#	#it_bus.chunk_start.eq(self.__chunk_start_val),
		#]
		#m.d.comb += [
		#	itd_in.eq(itd_in_sync),
		#	it_bus.chunk_start.eq(self.__chunk_start_val),
		#]
		if not USE_PIPE_SKID_BUF:
			m.d.comb += [
				itd_in.eq(itd_in_sync),
				it_bus.chunk_start.eq(self.__chunk_start_val),
			]
			m.d.sync += itd_out_sync.eq(itd_out)
		else: # if USE_PIPE_SKID_BUF:
			#m.d.comb += [
			#	sb_bus.misc.clear.eq(0b0),

			#	#sb_bus.inp.fwd.eq(bus.sb_bus.inp.fwd),
			#	#sb_bus.inp.bak.eq(bus.sb_bus.inp.bak),
			#	##bus.sb_bus.outp.fwd.eq(sb_bus)
			#	##bus.sb_bus.outp.fwd.eq(sb_bus.outp.fwd),
			#	##bus.sb_bus.outp.bak.eq(sb_bus.outp.bak),
			#	#bus.sb_bus.outp.fwd.valid.eq(sb_bus.outp.fwd.valid),
			#	#bus.sb_bus.outp.bak.eq(sb_bus.outp.bak),
			#	#itd_in.eq(sb_bus.outp.fwd.data),
			#	## `itd_out_sync` is set to `bus.sb_bus.outp.fwd.data`
			#	#bus.sb_bus.outp.fwd.data.eq(itd_out),
			#]
			#m.d.comb += [
			#	sb_bus.inp.fwd.flattened()[i].eq(
			#		bus.sb_bus.inp.fwd.flattened()[i]
			#	)
			#	for i in range(len(sb_bus.inp.fwd.flattened()))
			#]
			#m.d.comb += [
			#	sb_bus.inp.bak.flattened()[i].eq(
			#		bus.sb_bus.inp.bak.flattened()[i]
			#	)
			#	for i in range(len(sb_bus.inp.bak.flattened()))
			#]
			#m.d.comb += [
			#	bus.sb_bus.outp.fwd.flattened()[i].eq(
			#		sb_bus.outp.fwd.flattened()[i]
			#	)
			#	for i in range(len(sb_bus.outp.fwd.flattened()))
			#]
			#m.d.comb += [
			#	bus.sb_bus.outp.bak.flattened()[i].eq(
			#		sb_bus.outp.bak.flattened()[i]
			#	)
			#	for i in range(len(sb_bus.outp.bak.flattened()))
			#]

			##temp_lst_shrink = -2
			temp_lst_shrink = -2
			##other_temp_lst_shrink = -1
			##other_temp_lst_shrink = -2
			#temp_lst_shrink = -3
			#other_temp_lst_shrink = -1
			other_temp_lst_shrink = -1
			#other_temp_lst_shrink = None

			#temp_lst_shrink = -3
			#other_temp_lst_shrink = -2
			
			pipeline_mods.PipeSkidBuf.connect_child(
				parent=m,
				parent_sb_bus=bus.sb_bus,
				child_sb_bus=sb_bus,
				parent_data={
					"from_child": itd_in,
					"to_out": itd_out,
				},
				use_tag=True,
				reduce_tag=True,
				#use_tag=False,
				#reduce_tag=False,
				#reduce_tag=False,
				lst_shrink=temp_lst_shrink,
				other_lst_shrink=other_temp_lst_shrink,
			)
			#if FORMAL:
			#	#m.d.comb += [
			#	#	itd_in.eq(itd_in_sync),
			#	#	it_bus.chunk_start.eq(self.__chunk_start_val),
			#	#]
			#	m.d.sync += itd_out_sync.eq(itd_out)


			#f = open("debug-long_div_iter_mods-inp.txt.ignore", "w")
			#bus.sb_bus.inp.connect(
			#	other=sb_bus.inp,
			#	m=m,
			#	kind=Splitintf.ConnKind.Parent2Child,
			#	f=f,
			#	#lst_shrink=-2,
			#	#other_lst_shrink=-1,
			#	#lst_shrink=-1,
			#	#other_lst_shrink=0,
			#	lst_shrink=temp_lst_shrink,
			#	other_lst_shrink=other_temp_lst_shrink,
			#	use_tag=True,
			#	reduce_tag=True,
			#	#reduce_tag=False,
			#)
			#f.close()
			##for i in range(len(bus.sb_bus.inp.flattened()))
			##m.d.comb += [
			##	#Cat(sb_bus.inp.flattened()).eq(
			##	#	bus.sb_bus.inp.flattened()
			##	#)
			##	sb_bus.inp.flattened()[i].eq(
			##		bus.sb_bus.inp.flattened()[i]
			##	)
			##	for i in range(len(bus.sb_bus.inp.flattened()))
			##]
			#m.d.comb += [
			#	bus.sb_bus.outp.fwd.valid.eq(sb_bus.outp.fwd.valid),
			#	itd_in.eq(sb_bus.outp.fwd.data),
			#	bus.sb_bus.outp.fwd.data.eq(itd_out),
			#]
			#f = open("debug-long_div_iter_mods-outp-bak.txt.ignore", "w")
			#bus.sb_bus.outp.bak.connect(
			#	other=sb_bus.outp.bak,
			#	m=m,
			#	kind=Splitintf.ConnKind.Parent2Child,
			#	f=f,
			#	#lst_shrink=-2,
			#	#other_lst_shrink=-1,
			#	#lst_shrink=-1,
			#	#other_lst_shrink=0,
			#	lst_shrink=temp_lst_shrink,
			#	other_lst_shrink=other_temp_lst_shrink,
			#	use_tag=True,
			#	reduce_tag=True,
			#)
			#f.close()
			#f = open("debug-long_div_iter_mods-misc.txt.ignore", "w")
			#bus.sb_bus.misc.connect(
			#	other=sb_bus.misc,
			#	m=m,
			#	kind=Splitintf.ConnKind.Parent2Child,
			#	f=f,
			#	#lst_shrink=-2,
			#	#other_lst_shrink=-1,
			#	#lst_shrink=-1,
			#	#other_lst_shrink=0,
			#	lst_shrink=temp_lst_shrink,
			#	other_lst_shrink=other_temp_lst_shrink,
			#	use_tag=True,
			#	reduce_tag=True,
			#)
			#f.close()
		#--------
		if constants.FORMAL():
			#--------
			skip_cond = itd_in.formal.formal_denom.as_value() == 0
			past_valid = bus.formal.past_valid
			#--------
			#if not constants.USE_PIPE_SKID_BUF():
			m.d.sync += past_valid.eq(0b1),
			#else: # if constants.USE_PIPE_SKID_BUF():
			#	m.d.sync += 
			#--------
			#m.d.comb += [
			#	rst_cnt_done.eq(rst_cnt < 0)
			#]
			#with m.If((~ResetSignal() & (~rst_cnt_done))):
			#	m.d.sync += [
			#		#--------
			#		rst_cnt.eq(rst_cnt - 1)
			#		#-------
			#	]
			#def mk_mux_ifwd(
			#	USE_PIPE_SKID_BUF,
			#	bus,
			#	test_true,
			#	test_false,
			#):
			#	ifwd = bus.sb_bus.inp.fwd
			#	ibak = bus.sb_bus.inp.bak
			#	ofwd = bus.sb_bus.outp.fwd
			#	obak = bus.sb_bus.outp.bak
			#	#return Mux(
			#	#	#not USE_PIPE_SKID_BUF or (
			#	#	#	Past(ofwd.valid) & Past(ibak.ready)
			#	#	#) | (
			#	#	#	ofwd.valid & ibak.ready
			#	#	#),
			#	#	not USE_PIPE_SKID_BUF or (
			#	#		ifwd.valid & obak.ready
			#	#	),
			#	#	test,
			#	#	1,
			#	#)
			#	#return (
			#	#	test
			#	#	if not USE_PIPE_SKID_BUF
			#	#	else 1
			#	#)
			#	return Mux(
			#		not USE_PIPE_SKID_BUF or (
			#			Past(ifwd.valid) & Past(obak.ready)
			#		),
			#		test_true,
			#		test_false,
			#	)
			#def mk_mux_ofwd(
			#	USE_PIPE_SKID_BUF,
			#	bus,
			#	test,
			#):
			#	ifwd = bus.sb_bus.inp.fwd
			#	ibak = bus.sb_bus.inp.bak
			#	ofwd = bus.sb_bus.outp.fwd
			#	obak = bus.sb_bus.outp.bak
			#	return Mux(
			#		#not USE_PIPE_SKID_BUF or (
			#		#	Past(ofwd.valid) & Past(ibak.ready)
			#		#) | (
			#		#	ofwd.valid & ibak.ready
			#		#),
			#		#not USE_PIPE_SKID_BUF or (
			#		#	ofwd.valid & ibak.ready
			#		#),
			#		not USE_PIPE_SKID_BUF or (
			#			#ofwd.valid & ibak.ready
			#			#&
			#			Past(ofwd.valid) & Past(ibak.ready)
			#		),
			#		test,
			#		1,
			#	)
			with m.If(
				~ResetSignal() & past_valid
				#& (
				#	0b1
				#	if not constants.USE_PIPE_SKID_BUF()
				#	else sb_bus.outp.fwd.valid
				#)
			):
				#--------
				m.d.sync += [
					#--------
					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.temp_numer.as_value()
								== itd_in_sync.temp_numer.as_value()),
							#(itd_in.temp_numer.as_value()
							#	== itd_in_sync.temp_numer.as_value()),
							(itd_in.temp_numer.as_value()
								== Past(itd_in_sync.temp_numer.as_value())),
						),
						1,
					)),

					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.temp_quot.as_value()
								== itd_in_sync.temp_quot.as_value()),
							(itd_in.temp_quot.as_value()
								== Past(itd_in_sync.temp_quot.as_value())),
						),
						1,
					)),
					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.temp_rema.as_value()
								== itd_in_sync.temp_rema.as_value()),
							(itd_in.temp_rema.as_value()
								== Past(itd_in_sync.temp_rema.as_value())),
						),
						1,
					)),

					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.tag == itd_in_sync.tag),
							(itd_in.tag == Past(itd_in_sync.tag)),
						),
						1,
					)),

					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.formal.formal_numer.as_value()
								== itd_in_sync.formal.formal_numer
								.as_value()),
							(itd_in.formal.formal_numer.as_value()
								== Past(itd_in_sync.formal.formal_numer
									.as_value())),
						),
						1,
					)),
					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(itd_in.formal.formal_denom.as_value()
								== itd_in_sync.formal.formal_denom
								.as_value()),
							(itd_in.formal.formal_denom.as_value()
								== Past(itd_in_sync.formal.formal_denom
									.as_value())),
						),
						1,
					)),

					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(
								skip_cond
								| (
									itd_in.formal.oracle_quot.as_value()
									== itd_in_sync.formal.oracle_quot
										.as_value()
								)
							),
							(
								skip_cond
								| (
									itd_in.formal.oracle_quot.as_value()
									== Past(itd_in_sync.formal
										.oracle_quot.as_value())
										
								)
							),
						),
						1,
					)),
					Assert(Mux(
						ifwd_mvp,
						Mux(
							not USE_PIPE_SKID_BUF,
							(
								skip_cond
								| (itd_in.formal.oracle_rema.as_value()
									== itd_in_sync.formal.oracle_rema
										.as_value())
							),
							(
								skip_cond
								| (
									itd_in.formal.oracle_rema.as_value()
									== Past(itd_in_sync.formal.oracle_rema
										.as_value())
								)
							),
						),
						1
					)),
					#--------
					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.temp_numer
							== Past(itd_in.temp_numer)),
						1,
					)),
					#Assert(
					#	itd_out_sync.formal == Past(itd_in.formal)),
					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.formal.formal_numer
							== Past(itd_in.formal.formal_numer)),
						1,
					)),
					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.formal.formal_denom
							== Past(itd_in.formal.formal_denom)),
						1,
					)),

					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.formal.oracle_quot
							== Past(itd_in.formal.oracle_quot)),
						1,
					)),
					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.formal.oracle_rema
							== Past(itd_in.formal.oracle_rema)),
						1,
					)),

					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.formal.formal_denom_mult_lut
							== Past(itd_in.formal
								.formal_denom_mult_lut)),
						1,
					)),
					#--------
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.temp_numer
								== Past(itd_in_sync.temp_numer)),
						#	##(itd_out_sync.temp_numer
						#	##	== Past(itd_out.temp_numer)),
						#	#(itd_out_sync.temp_numer
						#	#	== Past(itd_in.temp_numer)),
						#	1,
						#),
						1,
					)),
					#Assert(Mux(
					#)),

					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.temp_quot
							== Past(itd_out.temp_quot)),
						1,
					)),
					Assert(Mux(
						ofwd_mvp,
						(itd_out_sync.temp_rema
							== Past(itd_out.temp_rema)),
						1,
					)),
					#--------
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.denom_mult_lut
								== Past(itd_in_sync.denom_mult_lut)),
						#	##(itd_out_sync.denom_mult_lut
						#	##	== Past(itd_out.denom_mult_lut)),
						#	#(itd_out_sync.denom_mult_lut
						#	#	== Past(itd_in.denom_mult_lut)),
						#	1,
						#),
						1,
					)),
					#--------
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.formal.formal_numer
								== Past(itd_in_sync.formal.formal_numer)),
						#	##(itd_out_sync.formal.formal_numer
						#	##	== Past(itd_out.formal.formal_numer)),
						#	#(itd_out_sync.formal.formal_numer
						#	#	== Past(itd_in.formal.formal_numer)),
						#	1,
						#),
						1,
					)),
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.formal.formal_denom
								== Past(itd_in_sync.formal.formal_denom)),
						#	##(itd_out_sync.formal.formal_denom
						#	##	== Past(itd_out.formal.formal_denom)),
						#	#(itd_out_sync.formal.formal_denom
						#	#	== Past(itd_in.formal.formal_denom)),
						#	1,
						#),
						1,
					)),

					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.formal.oracle_quot
								== Past(itd_in_sync.formal.oracle_quot)),
						#	##(itd_out_sync.formal.oracle_quot
						#	##	== Past(itd_out.formal.oracle_quot)),
						#	#(itd_out_sync.formal.oracle_quot
						#	#	== Past(itd_in.formal.oracle_quot)),
						#	1,
						#),
						1,
					)),
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.formal.oracle_rema
								== Past(itd_in_sync.formal.oracle_rema)),
							##(itd_out_sync.formal.oracle_rema
							##	== Past(itd_out.formal.oracle_rema)),
							#(itd_out_sync.formal.oracle_rema
						#	#	== Past(itd_in.formal.oracle_rema)),
						#	1,
						#),
						1,
					)),
					#--------
					Assert(Mux(
						ofwd_mvp,
						#Mux(
						#	not USE_PIPE_SKID_BUF,
							(itd_out_sync.formal.formal_denom_mult_lut
								== Past(itd_in_sync.formal
									.formal_denom_mult_lut)),
							#(itd_out_sync.formal.formal_denom_mult_lut
							#	== Past(itd_out.formal
							#		.formal_denom_mult_lut)),
						#	(itd_out_sync.formal.formal_denom_mult_lut
						#		== Past(itd_in.formal
						#			.formal_denom_mult_lut)),
						#),
						1,
					)),
					#--------
				]
			#--------
		#--------
		return m
		#--------
#--------
