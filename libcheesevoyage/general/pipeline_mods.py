#!/usr/bin/env python3

import enum as pyenum
from collections import OrderedDict

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum
#from libcheesevoyage.misc_util import psconcat, mk_keep_obj
from libcheesevoyage.misc_util import psconcat, sig_keep, Blank
from libcheesevoyage.general.general_types import SigInfo

class PipeSigDir(pyenum.Enum):
	Fwd = 0
	Bak = pyenum.auto()
	InpOnly = pyenum.auto()
	OutpOnly = pyenum.auto()
	RegOnly = pyenum.auto()
	NosufOnly = pyenum.auto()

	#EdgeToNext = pyenum.auto()
	#EdgeToPrev = pyenum.auto()

	#Start2Next = pyenum.auto()
	#Middle2Next = pyenum.auto()
#class PipeSigKind(pyenum.Enum):
#	Begin = 0
#	Middle = pyenum.auto()
#	End = pyenum.auto()

#class PipeSigAttrs:
#	def __init__(
#		self,
#		*,
#		valid_attrs: str=sig_keep(),
#		ready_attrs: str=sig_keep(),
#		busy_attrs: str=sig_keep(),
#		data_attrs: str=sig_keep(),
#		clear_attrs: str=sig_keep(),
#	):
#		self.__valid_attrs = valid_attrs
#		self.__ready_attrs = ready_attrs
#		self.__busy_attrs = busy_attrs
#		self.__data_attrs = data_attrs
#		self.__clear_attrs = clear_attrs
#
#	def valid_attrs(self):
#		return self.__valid_attrs
#	def ready_attrs(self):
#		return self.__ready_attrs
#	def busy_attrs(self):
#		return self.__busy_attrs
#	def data_attrs(self):
#		return self.__data_attrs
#	def clear_attrs(self):
#		return self.__clear_attrs

class PipeSigInfo:
	def __init__(
		self,
		info: SigInfo,
		psig_dir: PipeSigDir,
		*,
		can_auto_copy: bool=True,
		prefix: str="",
		#enable_reg=True
	):
		self.__info = info
		#self.__inpnm: self.basenm() + (
		#	"_prev" if psig_dir == PipeSigDir.Fwd else "_next"
		#)
		#self.__outpnm: self.basenm() + (
		#	"_next" if psig_dir == PipeSigDir.Fwd else "_prev"
		#)
		self.__inpnm_suffix = (
			"_prev" if psig_dir == PipeSigDir.Fwd else "_next"
		)
		self.__ouptnm_suffix = (
			"_next" if psig_dir == PipeSigDir.Fwd else "_prev"
		)
		#if enable_reg:
			#self.__regnm: self.basenm() + "_reg"
		self.__regnm_suffix: "_reg"

		self.__psig_dir: psig_dir
		self.__can_auto_copy = can_auto_copy
		self.__prefix = prefix
		#self.__enable_reg = enable_reg
	@staticmethod
	def like(other, **kwargs):
		kw = {
			"info": other.info(),
			"psig_dir": other.psig_dir(),
			"can_auto_copy": other.can_auto_copy(),
			"prefix": other.prefix(),
		}
		kw.update(kwargs)

		return PipeSigInfo(**kw)
	@staticmethod
	def like_w_basenm(other, basenm: str, **kwargs):
		return PipeSigInfo.like(
			other,
			info=SigInfo.like(
				other.info(), basenm=basenm,
			),
			kwargs=kwargs,
		)

	def info(self):
		return self.__info
	def basenm(self):
		#return self.__basenm
		return self.info().basenm()
	def have_both_io(self):
		return (
			self.psig_dir() == PipeSigDir.Fwd
			or self.psig_dir() == PipeSigDir.Bak
		)
	def have_inp(self):
		return (
			self.have_both_io() or self.psig_dir() == PipeSigDir.InpOnly
		)
	def have_outp(self):
		return (
			self.have_both_io() or self.psig_dir() == PipeSigDir.OutpOnly
		)
	def have_reg(self):
		return (
			self.have_both_io() or self.psig_dir() == PipeSigDir.RegOnly
		)
	#def inpnm(self):
	#	assert self.have_inp()
	#	return self.__inpnm
	#def outpnm(self):
	#	assert self.have_outp()
	#	return self.__outpnm
	#def regnm(self):
	#	#assert self.enable_reg()
	#	assert self.have_reg()
	#	return self.__regnm
	def inpnm_suffix(self):
		assert self.have_inp()
		return self.__inpnm_suffix
	def outpnm_suffix(self):
		assert self.have_outp()
		return self.__outpnm_suffix
	def regnm_suffix(self):
		#assert self.enable_reg()
		assert self.have_reg()
		return self.__regnm_suffix
	def mk_inp_sig(
		self,
		#*,
		#prefix=""
	):
		return self.info().mk_sig(
			prefix=self.prefix,
			suffix=self.inpnm_suffix()
		)
	def mk_outp_sig(
		self,
		#*,
		#prefix=""
	):
		return self.info().mk_sig(
			prefix=self.prefix,
			suffix=self.outpnm_suffix()
		)
	def mk_reg_sig(
		self,
		#*,
		#prefix=""
	):
		return self.info().mk_sig(
			prefix=self.prefix,
			suffix=self.regnm_suffix()
		)
	def mk_nosuf_sig(
		self,
		#*,
		#prefix=""
	):
		return self.info().mk_sig(
			prefix=self.prefix,
			suffix=""
		)
	def mk_inp_sig_w_basenm(
		self,
		basenm: str,
		#prefix="",
		**kwargs
	):
		# Not sure this function is really needed, but I've included it for
		# consistency
		ret = {
			"info": PipeSigInfo.like_w_basenm(
				self,
				basenm=basenm,
				psig_dir=PipeSigDir.InpOnly,
				#prefix=prefix,
				kwargs=kwargs
			)
		}
		ret["sig"] = ret["info"].mk_inp_sig()
		return ret
	def mk_outp_sig_w_basenm(
		self,
		basenm: str,
		#prefix="",
		**kwargs
	):
		# Not sure this function is really needed, but I've included it for
		# consistency
		ret = {
			"info": PipeSigInfo.like_w_basenm(
				self,
				basenm=basenm,
				psig_dir=PipeSigDir.OutpOnly,
				#prefix=prefix,
				kwargs=kwargs,
			)
		}
		ret["sig"] = ret["info"].mk_outp_sig()
		return ret
	def mk_reg_sig_w_basenm(
		self,
		basenm: str,
		#prefix="",
		**kwargs,
	):
		ret = {
			"info": PipeSigInfo.like_w_basenm(
				self,
				basenm=basenm,
				psig_dir=PipeSigDir.RegOnly,
				#prefix=prefix,
				kwargs=kwargs,
			)
		}
		ret["sig"] = ret["info"].mk_reg_sig()
		return ret
	def mk_nosuf_sig_w_basenm(
		self,
		basenm: str,
		#prefix="",
		**kwargs,
	):
		ret = {
			"info": PipeSigInfo.like_w_basenm(
				self,
				basenm=basenm,
				psig_dir=PipeSigDir.NosufOnly,
				#prefix=prefix,
				kwargs=kwargs,
			)
		}
		ret["sig"] = ret["info"].mk_nosuf_sig()
		return ret

	#def signm_lst(self):
	#	if self.psig_dir() == PipeSigDir.InpOnly:
	#		return [self.inpnm()]
	#	elif self.psig_dir() == PipeSigDir.OutpOnly:
	#		return [self.outpnm()]
	#	elif self.psig_dir() == PipeSigDir.RegOnly:
	#		return [self.regnm()]
	#	else:
	#		ret = [self.outpnm(), self.inpnm()]
	#		#if self.enable_reg():
	#		if self.have_reg():
	#			ret += [self.regnm()]
	#		return ret
	def signm_lst(self):
		suffix_lst = self.signm_suffix_lst()
		return [psconcat(self.basenm(), suffix) for suffix in suffix_lst]
	def signm_suffix_lst(self):
		assert self.psig_dir() != PipeSigDir.NosufOnly

		if self.psig_dir() == PipeSigDir.InpOnly:
			return [self.inpnm_suffix()]
		elif self.psig_dir() == PipeSigDir.OutpOnly:
			return [self.outpnm_suffix()]
		elif self.psig_dir() == PipeSigDir.RegOnly:
			return [self.regnm_suffix()]
		else:
			ret = [self.outpnm_suffix(), self.inpnm_suffix()]
			#if self.enable_reg():
			if self.have_reg():
				ret += [self.regnm_suffix()]
			return ret

	def psig_dir(self):
		return self.__psig_dir
	def shapelayt(self):
		#return self.__shapelayt
		return self.info().shapelayt()
	def ObjKind(self):
		#return self.__ObjKind 
		return self.info().ObjKind ()
	def reset(self):
		#return self.__reset
		return self.info().reset()
	def attrs(self):
		return self.__attrs
		return self.info().attrs()

	def can_auto_copy(self):
		return self.__can_auto_copy
	@property
	def prefix(self):
		return self.__prefix
	@prefix.setter
	def prefix(self, n_prefix: str):
		self.__prefix = n_prefix
	#def enable_reg(self):
	#	return self.__enable_reg


#def mk_ps_pair_constants(
#	base_name: str, shape, psig_dir: PipeSigDir
#) -> OrderedDict:
#	return {
#		"base": base,
#		"next": base + "_next",
#		"prev": base + "_prev",
#		"shape": shape,
#		"psig_dir": psig_dir,
#	}
class SkidBufRegBus:
	def __init__(
		self,
		#data_info: PipeSigInfo,
		data_info: SigInfo,
		#shapelayt, # data shape/layout
		#*,
		#ObjKind=Signal,
		#reset=0b0,
	):
		#--------
		#self.__shapelayt = shapelayt
		#self.__ObjKind = ObjKind
		#self.__reset = reset
		self.__data_info = data_info
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		self.inp.clock_enable = Signal(1,
			name="inp_clock_enable", attrs=sig_keep())
		self.inp.clear = Signal(1,
			name="inp_clear", attrs=sig_keep())
		#self.inp.data = self.ObjKind()(
		#	self.shapelayt(),
		#	name="inp_data", attrs=sig_keep()
		#)
		#self.inp.data = self.data_info().mk_nosuf_sig_w_basenm(
		#	basenm="inp_data"
		#)
		self.inp.data = self.data_info().mk_sig(basenm="inp_data")
		#--------
		#self.outp.data = self.ObjKind()(
		#	self.shapelayt(),
		#	name="outp_data", attrs=sig_keep(),
		#	reset=self.reset()
		#)
		#self.outp.data = self.data_info().mk_nosuf_sig_w_basenm(
		#	basenm="outp_data"
		#)
		self.outp.data = self.data_info().mk_sig(basenm="outp_data")
		#--------

	def data_info(self):
		return self.__data_info
	def data_shapelayt(self):
		#return self.__shapelayt
		return self.data_info().shapelayt()
	def DataObjKind(self):
		#return self.__ObjKind
		return self.data_info().ObjKind()
	def data_reset(self):
		#return self.__reset
		return self.data_info().reset()

# A basic register for use with `SkidBuf`, based on this:
# http://fpgacpu.ca/fpga/Register.html
# which is MIT licensed
class SkidBufReg(Elaboratable):
	def __init__(
		self,
		#data_info: PipeSigInfo,
		data_info: SigInfo,
	):
		self.__bus = SkidBufRegBus(data_info=data_info)

	def bus(self):
		return self.__bus
	def data_shapelayt(self):
		return self.bus().data_shapelayt()
	def DataObjKind(self):
		return self.bus().DataObjKind()
	def data_reset(self):
		return self.bus().data_reset()

	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		inp = bus.inp
		outp = bus.outp
		#--------
		# if (clock_enable == 1'b1)
		with m.If(inp.clock_enable):
			# data_out <= data_in;
			m.d.sync += outp.data.eq(inp.data)

		# if (clear == 1'b1)
		with m.If(inp.clear):
			# data_out <= RESET_VALUE
			m.d.sync += outp.data.eq(self.reset())
		#--------
		return m
		#--------

class SkidBufBus:
	def __init__(
		self,
		#shapelayt, # data shape/layout

		#data_info: PipeSigInfo,
		data_info: SigInfo,
		## should be an `OrderedDict` of `SigInfo`, not `PipeSigInfo`
		#info_dct: OrderedDict, 
		#*,
		#sig_attrs=PipeSigAttrs()
		#attrs: str=sig_keep()
		#ObjKind=Signal,
		#reset=0b0,
		#--------
	):
		#--------
		self.__data_info = data_info
		#self.__shapelayt = shapelayt
		#self.__sig_attrs = sig_attrs
		#self.__ObjKind = ObjKind
		#self.__reset = reset
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		self.inp.valid = Signal(1,
			name="inp_valid", attrs=sig_keep())
		self.inp.ready = Signal(1,
			name="inp_ready", attrs=sig_keep())
		#self.inp.busy = Signal(1,
		#	name="inp_busy", attrs=sig_keep())
		#self.inp.data = self.ObjKind()(self.shapelayt(),
		#	name="inp_data", attrs=data_info.attrs())

		#self.inp.data = self.data_info().mk_nosuf_sig_w_basenm(
		#	basenm="inp_data"
		#)["sig"]
		#self.inp.data = self.data_info().mk_sig(basenm="inp_data")

		#self.inp.data = SigInfo.like(
		#	self.data_info(), basenm="inp_data",
		#).mk_sig()
		self.inp.data = self.data_info().mk_sig(basenm="inp_data")

		#self.inp.clear = self.ObjKind()(self.shapelayt(),
		#	name="inp_clear", attrs=sig_keep())
		self.inp.clear = Signal(1,
			name="inp_clear", attrs=sig_keep())
		#--------
		self.outp.valid = Signal(1,
			name="outp_valid", attrs=sig_keep())
		self.outp.ready = Signal(1,
			name="outp_ready", attrs=sig_keep())
		#self.outp.busy = Signal(1,
		#	name="outp_busy", attrs=sig_keep())
		#self.outp.data = self.ObjKind()(self.shapelayt(),
		#	name="outp_data", attrs=self.data_attrs())
		#self.outp.data = self.data_info().mk_outp_sig_w_basenm(
		#	basenm="outp_data"
		#)["sig"]
		#self.outp.data = SigInfo.like(
		#	self.data_info(), basenm="outp_data",
		#).mk_sig()
		#self.outp.data = self.data_info().mk_nosuf_sig_w_basenm(
		#	basenm="outp_data"
		#)["sig"]
		self.outp.data = self.data_info().mk_sig(basenm="outp_data")
		#--------
	def data_info(self):
		return self.__data_info
	def data_shapelayt(self):
		#return self.__shapelayt
		return self.data_info().shapelayt()
	#def sig_attrs(self):
	#	return self.__sig_attrs
	#def valid_attrs(self):
	#	return self.sig_attrs().valid_attrs
	#def ready_attrs(self):
	#	return self.sig_attrs().ready_attrs
	#def busy_attrs(self):
	#	return self.sig_attrs().busy_attrs
	#def data_attrs(self):
	#	return self.sig_attrs().data_attrs
	#def clear_attrs(self):
	#	return self.sig_attrs().clear_attrs
	def DataObjKind(self):
		#return self.__ObjKind
		return self.data_info().ObjKind()
	def data_reset(self):
		#return self.__reset
		return self.data_info().reset()

# A basic skid buffer, based on this:
# http://fpgacpu.ca/fpga/Pipeline_Skid_Buffer.html
# which is MIT licensed
class SkidBuf(Elaboratable):
	def __init__(
		self,
		#data_info: PipeSigInfo,
		data_info: SigInfo,
		*,
		OPT_CIRC_BUF: bool=False,
		#--------
	):
		self.__bus = SkidBufBus(
			data_info=data_info
		)
		self.__OPT_CIRC_BUF = OPT_CIRC_BUF

	def bus(self):
		return self.__bus
	def OPT_CIRC_BUF(self):
		return self.__OPT_CIRC_BUF
	def data_info(self):
		return self.bus().data_info()
	def data_shapelayt(self):
		return self.bus().data_shapelayt()
	#def sig_attrs(self):
	#	return self.bus().sig_attrs()

	@staticmethod
	def STATE_WIDTH():
		return 2
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		data_info = bus.data_info()
		loc = Blank()

		# Data path
		loc.d = Blank()
		# data path submodules
		loc.d.sm = Blank()

		# Ctrl path
		loc.c = Blank()
		# Ctrl path submodules
		loc.c.sm = Blank()
		#--------
		# Data path code

		# EMPTY at start, so don't load
		# reg data_buffer_wren = 1'b0;
		loc.d.data_buffer_wren = Signal(1,
			name="loc_d_data_buffer_wren", attrs=sig_keep())

		# wire [WORD_WIDTH-1:0]   data_buffer_out;
		#loc.d.data_buffer_out = bus.DataObjKind()(
		#	bus.data_shapelayt(),
		#	name="loc_d_data_buffer_out", attrs=sig_keep()
		#)

		#loc.d.data_buffer_out_dct = data_info.mk_nosuf_sig_w_basenm(
		#	basenm="loc_d_data_buffer_out"
		#)
		loc.d.data_buffer_out = data_info.mk_sig(
			basenm="loc_d_data_buffer_out"
		)
		#loc.d.data_buffer_out_dct = data_info.mk_sig(
		#	basenm="loc_d_data_buffer_out"
		#)
		#loc.d.data_buffer_out = data_info.mk_sig(
		#	basenm="loc_d_data_buffer_out"
		#)

		# EMPTY at start, so accept data
		# reg data_out_wren = 1'b1;
		loc.d.data_out_wren = Signal(1,
			name="loc_d_data_out_wren", attrs=sig_keep(),
			reset=0b1)

		# reg use_buffered_data = 1'b0;
		loc.d.use_buffered_data = Signal(1,
			name="loc_d_use_buffered_data", attrs=sig_keep())

		# reg [WORD_WIDTH-1:0] selected_data = WORD_ZERO;
		#loc.d.selected_data = bus.DataObjKind()(
		#	bus.data_shapelayt(),
		#	name="loc_d_selected_data", attrs=sig_keep()
		#)
		#loc.d.selected_data_dct = data_info.mk_reg_sig_w_basenm(
		#	basenm="loc_d_selected_data"
		#)
		#loc.d.selected_data_dct = data_info.mk_nosuf_sig_w_basenm(
		#	basenm="loc_d_selected_data"
		#)
		loc.d.selected_data = data_info.mk_sig(
			basenm="loc_d_selected_data"
		)

		# always @(*) begin
		#	selected_data
		#		= (use_buffered_data == 1'b1)
		#		? data_buffer_out
		#		: input_data;

		m.d.comb += loc.d.selected_data.eq(
			Mux(
				loc.d.use_buffered_data, # (use_buffered_data == 1'b1)
				loc.d.data_buffer_out, # ? data_buffer_out
				bus.inp.data, # : inp_data
			)
		)
		# end

		# Connect locals to `data_buffer_reg` ports
		# Register
		# #(
		#	.WORD_WIDTH(WORD_WIDTH),
		#	.RESET_VALUE(WORD_ZERO)
		# )
		# data_buffer_reg
		# (
		data_buffer_reg = loc.d.sm.data_buffer_reg = SkidBufReg(
			#data_info=loc.d.selected_data_dct["info"]
			#data_info=loc.d.data_buffer_out_dct["info"]
			data_info=data_info
		)
		with data_buffer_reg.bus() as reg_bus:
			m.d.comb += [
				# 	.clock          (clock),
				# 	.clock_enable   (data_buffer_wren),
				# 	.clear          (clear),
				# 	.data_in        (input_data),
				# 	.data_out       (data_buffer_out)
				reg_bus.inp.clock_enable.eq(loc.d.data_buffer_wren),
				reg_bus.inp.clear.eq(bus.inp.clear),
				reg_bus.inp.data.eq(bus.inp.data),
				loc.d.data_buffer_out.eq(reg_bus.outp.data),
			]
		# );

		# Register
		# #(
		# 	.WORD_WIDTH     (WORD_WIDTH),
		# 	.RESET_VALUE    (WORD_ZERO)
		# )
		# data_out_reg
		# (
		data_out_reg = loc.d.sm.data_out_reg = SkidBufReg(
			#data_info=loc.d.selected_data_dct["info"]
			#data_info=loc.d.selected_data_dct["info"]
			data_info=data_info
		)
		with data_out_reg.bus() as reg_bus:
			m.d.comb += [
				# 	.clock          (clock),
				# 	.clock_enable   (data_out_wren),
				# 	.clear          (clear),
				# 	.data_in        (selected_data),
				# 	.data_out       (output_data)
				reg_bus.inp.clock_enable.eq(loc.d.data_out_wren),
				reg_bus.inp.clear.eq(bus.inp.clear),
				reg_bus.inp.data.eq(loc.d.selected_data),
				bus.outp.data.eq(reg_bus.outp.data),
			]
		# );

		# Install the data path submodules
		# (order of installation doesn't matter, I don't think?)
		m.submodules += [
			dpath_sm for dpath_sm in loc.d.sm.__dict__.values()
		]
		#--------
		# Ctrl path
		class State(Enum, shape=SkidBuf.STATE_WIDTH()):
			# Output and buffer registers empty
			EMPTY = 0b00
			# Output register holds data
			BUSY = 0b01
			# Both output and buffer registers hold data
			FULL = 0b10

		# There is no case where only the buffer register would hold data
		# No handling of erroneous and unreachable state 0b11.
		# We could check and raise an error flag.

		loc.c.state = Signal(State.as_shape(),
			name="loc_c_state", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next = Signal(State.as_shape(),
			name="loc_c_state_next", attrs=sig_keep(),
			reset=State.EMPTY)

		#--------
		# Extra signals to simulate Verilog's blocking assignments, while
		# also providing better debugging support.
		loc.c.state_next_load = Signal(State.as_shape(),
			name="loc_c_state_next_load", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_flow = Signal(State.as_shape(),
			name="loc_c_state_next_flow", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_fill = Signal(State.as_shape(),
			name="loc_c_state_next_fill", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_flush = Signal(State.as_shape(),
			name="loc_c_state_next_flush", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_unload = Signal(State.as_shape(),
			name="loc_c_state_next_unload", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_dump = Signal(State.as_shape(),
			name="loc_c_state_next_dump", attrs=sig_keep(),
			reset=State.EMPTY)
		loc.c.state_next_do_pass = Signal(State.as_shape(),
			name="loc_c_state_next_do_pass", attrs=sig_keep(),
			reset=State.EMPTY)
		#--------
		#Register
		##(
		#	.WORD_WIDTH     (1),
		#	.RESET_VALUE    (1'b1) // EMPTY at start, so accept data
		#)
		#output_ready_reg
		#(
		# named `input_ready` in the source material
		#loc.c.outp_ready_data_info = PipeSigInfo(
		#	info=SigInfo.like_sig(bus.outp.ready, reset=0b1),
		#	psig_dir=PipeSigDir.NosufOnly,
		#	can_auto_copy=False
		#)
		# named `input_ready_reg` in the source material
		output_ready_reg = loc.c.sm.output_ready_reg = SkidBufReg(
			#data_info=loc.c.outp_ready_data_info
			data_info=SigInfo.like_sig(bus.outp.ready, reset=0b1)
		)
		with output_ready_reg.bus() as reg_bus:
			m.d.comb += [
				# .clock          (clock),
				# .clock_enable   (1'b1),
				# .clear          (clear),
				# .data_in        (
				#		(state_next != FULL) || (CIRCULAR_BUFFER != 0)
				#	),
				# .data_out       (input_ready)
				reg_bus.inp.clock_enable.eq(0b1),
				reg_bus.inp.clear.eq(bus.inp.clear),
				reg_bus.inp.data.eq(
					(loc.c.state_next != State.FULL)
					| (OPT_CIRC_BUF != 0)
				),
				bus.outp.ready.eq(reg_bus.outp.data),
			]
		#);

		#Register
		##(
		#	.WORD_WIDTH     (1),
		#	.RESET_VALUE    (1'b0)
		#)
		#output_valid_reg
		#(
		#loc.c.outp_valid_data_info = PipeSigInfo(
		#	info=SigInfo.like_sig(bus.outp.valid),
		#	psig_dir=PipeSigDir.NosufOnly,
		#	can_auto_copy=False
		#)
		output_valid_reg = loc.c.sm.output_valid_reg = SkidBufReg(
			#data_info=loc.c.outp_valid_data_info
			data_info=SigInfo.like_sig(bus.outp.valid),
		)
		with output_valid_reg.bus() as reg_bus:
			m.d.comb += [
				# .clock          (clock),
				# .clock_enable   (1'b1),
				# .clear          (clear),
				# .data_in        (state_next != EMPTY),
				# .data_out       (output_valid)
				reg_bus.inp.clock_enable.eq(0b1),
				reg_bus.inp.clear.eq(bus.inp.clear),
				reg_bus.inp.data.eq(loc.c.state_next != State.EMPTY),
				bus.outp.valid.eq(reg_bus.outp.data),
			]
		#);

		loc.c.insert = Signal(1,
			name="loc_c_insert", attrs=sig_keep())
		loc.c.remove = Signal(1,
			name="loc_c_remove", attrs=sig_keep())

		#always @(*) begin
		#	insert = (input_valid  == 1'b1) && (input_ready  == 1'b1);
		#	remove = (output_valid == 1'b1) && (output_ready == 1'b1);
		#end
		# The source material has `input_ready` and `output_ready` swapped
		# when compared to this translation to Amaranth 
		m.d.comb += [
			loc.c.insert.eq(bus.inp.valid & bus.outp.ready),
			loc.c.remove.eq(bus.outp.valid & bus.inp.ready),
		]
		# Empty datapath inserts data into output register.
		# reg load	  = 1'b0;
		loc.c.load = Signal(1,
			name="loc_c_load", attrs=sig_keep(),
			reset=0b0)

		# New inserted data into output register as the old data is
		# removed.
		# reg flow	  = 1'b0;
		loc.c.flow = Signal(1,
			name="loc_c_flow", attrs=sig_keep(),
			reset=0b0)

		# New inserted data into buffer register. Data not removed from
		# output register.
		# reg fill	  = 1'b0;
		loc.c.fill = Signal(1,
			name="loc_c_fill", attrs=sig_keep(),
			reset=0b0)

		# Move data from buffer register into output register. Remove old
		# data. No new data inserted.
		# reg flush   = 1'b0;
		loc.c.flush= Signal(1,
			name="loc_c_flush", attrs=sig_keep(),
			reset=0b0)

		# Remove data from output register, leaving the datapath empty.
		# reg unload  = 1'b0;
		loc.c.unload = Signal(1,
			name="loc_c_unload", attrs=sig_keep(),
			reset=0b0)

		# New inserted data into buffer register. Move data from buffer
		# register into output register. Discard old output data. (CBM)
		# reg dump	  = 1'b0;
		loc.c.dump = Signal(1,
			name="loc_c_dump", attrs=sig_keep(),
			reset=0b0)
		# New inserted data into buffer register. Move data from buffer
		# register into output register. Remove old output data.  (CBM)
		# reg pass	  = 1'b0;
		loc.c.do_pass = Signal(1,
			name="loc_c_do_pass", attrs=sig_keep(),
			reset=0b0)

		# always @(*) begin
		m.d.comb += [
		#	  load	  = (state == EMPTY) && (insert == 1'b1) && (remove == 1'b0);
			loc.c.load.eq(
				(loc.c.state == State.EMPTY)
				& (loc.c.insert == 0b1)
				& (loc.c.remove == 0b0)
			),
		#	  flow	  = (state == BUSY)  && (insert == 1'b1) && (remove == 1'b1);
			loc.c.flow.eq(
				(loc.c.state == State.BUSY)
				& (loc.c.insert == 0b1)
				& (loc.c.remove == 0b1)
			),
		#	  fill	  = (state == BUSY)  && (insert == 1'b1) && (remove == 1'b0);
			loc.c.fill.eq(
				(loc.c.state == State.BUSY)
				& (loc.c.insert == 0b1)
				& (loc.c.remove == 0b0)
			),
		#	  unload  = (state == BUSY)  && (insert == 1'b0) && (remove == 1'b1);
			loc.c.unload.eq(
				(loc.c.state == State.BUSY)
				& (loc.c.insert == 0b0)
				& (loc.c.remove == 0b1)
			),
		#	  flush   = (state == FULL)  && (insert == 1'b0) && (remove == 1'b1);
			loc.c.flush.eq(
				(loc.c.state == State.FULL)
				& (loc.c.insert == 0b0)
				& (loc.c.remove == 0b1)
			),
		#	  dump	  = (state == FULL)  && (insert == 1'b1) && (remove == 1'b0) && (CIRCULAR_BUFFER != 0);
			loc.c.dump.eq(
				(loc.c.state == State.FULL)
				& (loc.c.insert == 0b1)
				& (loc.c.remove == 0b0)
				& (OPT_CIRC_BUF != 0)
			),
		#	  pass	  = (state == FULL)  && (insert == 1'b1) && (remove == 1'b1) && (CIRCULAR_BUFFER != 0);
			loc.c.do_pass.eq(
				(loc.c.state == State.FULL)
				& (loc.c.insert == 0b1)
				& (loc.c.remove == 0b1)
				& (OPT_CIRC_BUF != 0)
			),
		]
		# end

		# always @(*) begin
		m.d.comb += [
		#	state_next = (load	 == 1'b1) ? BUSY  : state;
			loc.c.state_next_load.eq(
				Mux(loc.c.load, State.BUSY, loc.c.state)
			),
		#	state_next = (flow	 == 1'b1) ? BUSY  : state_next;
			loc.c.state_next_flow.eq(
				Mux(loc.c.flow, State.BUSY, loc.c.state_next_load)
			),
		#	state_next = (fill	 == 1'b1) ? FULL  : state_next;
			loc.c.state_next_fill.eq(
				Mux(loc.c.fill, State.FULL, loc.c.state_next_flow)
			),
		#	state_next = (flush  == 1'b1) ? BUSY  : state_next;
			loc.c.state_next_flush.eq(
				Mux(loc.c.flush, State.BUSY, loc.c.state_next_fill)
			),
		#	state_next = (unload == 1'b1) ? EMPTY : state_next;
			loc.c.state_next_unload.eq(
				Mux(loc.c.unload, State.EMPTY, loc.c.state_next_flush)
			),
		#	state_next = (dump	 == 1'b1) ? FULL  : state_next;
			loc.c.state_next_dump.eq(
				Mux(loc.c.dump, State.FULL, loc.c.state_next_unload)
			),
		#	state_next = (pass	 == 1'b1) ? FULL  : state_next;
			loc.c.state_next_do_pass.eq(
				Mux(loc.c.do_pass, State.FULL, loc.c.state_next_dump)
			),
		#--------
			# finally past the pain of a lack of blocking assignments
			loc.c.state_next.eq(loc.c.state_next_do_pass),
		# end
		]

		#Register
		##(
		#	.WORD_WIDTH     (STATE_BITS),
		#	.RESET_VALUE    (EMPTY)         // Initial state
		#)
		#state_reg
		#(
		#loc.c.state_data_info = PipeSigInfo(
		#	info=SigInfo.like_sig(loc.c.state, reset=State.EMPTY),
		#	psig_dir=PipeSigDir.NosufOnly,
		#	can_auto_copy=False,
		#)
		state_reg = loc.c.sm.state_reg = SkidBufReg(
			#data_info=loc.c.state_data_info
			data_info=SigInfo.like_sig(loc.c.state, reset=State.EMPTY),
		)
		with state_reg.bus() as reg_bus:
			m.d.comb += [
				#	.clock          (clock),
				#	.clock_enable   (1'b1),
				#	.clear          (clear),
				#	.data_in        (state_next),
				#	.data_out       (state)
				reg_bus.inp.clock_enable.eq(0b1),
				reg_bus.inp.clear.eq(bus.inp.clear),
				reg_bus.inp.data.eq(loc.c.state_next),
				loc.c.state.eq(reg_bus.outp.data),
			]
		#);

		# always @(*) begin
		m.d.comb += [
		#	data_out_wren	  = (load  == 1'b1) || (flow == 1'b1) || (flush == 1'b1) || (dump == 1'b1) || (pass == 1'b1);
			loc.d.data_out_wren.eq(
				(loc.c.load == 0b1)
				| (loc.c.flow == 0b1)
				| (loc.c.flush == 0b1)
				| (loc.c.dump == 0b1)
				| (loc.c.do_pass == 0b1)
			),
		# 	data_buffer_wren  = (fill  == 1'b1)                                      || (dump == 1'b1) || (pass == 1'b1);
			loc.d.data_buffer_wren.eq(
				(loc.c.fill  == 0b1)
				| (loc.c.dump == 0b1)
				| (loc.c.do_pass == 0b1)
			),
		# 	use_buffered_data = (flush == 1'b1)                                      || (dump == 1'b1) || (pass == 1'b1);
			loc.d.use_buffered_data.eq(
				(loc.c.flush == 0b1)
				| (loc.c.dump == 0b1)
				| (loc.c.do_pass == 0b1)
			),
		]
		# end

		# Install the ctrl path submodules
		# (order of installation doesn't matter, I don't think?)
		m.submodules += [
			cpath_sm for cpath_sm in loc.c.sm.__dict__.values()
		]
		#--------
		#--------
		return m
		#--------

class PipeBundle:
	def __init__(
		self,
		box,
	):
		info_dct = box.info_dct()
		valid_attrs = box.valid_attrs()
		ready_attrs = box.ready_attrs()
		busy_attrs = box.busy_attrs()
		#ce_attrs = box.ce_attrs()
		clear_pipe_attrs = box.clear_pipe_attrs()
		prefix = box.prefix()
		##enable_reg = box.enable_reg()
		#enable_clear_pipe = box.enable_clear_pipe()
		##enable_extra_stalled = box.enable_extra_stalled
		##enable_clear_pipe = box.enable_clear_pipe()

		#self.__prefix = prefix
		assert "valid" not in info_dct, psconcat(
			"\"valid\" must not be in `info_dct`, ",
			"as it will be added later in this function"
		)
		assert "ready" not in info_dct, psconcat(
			"\"ready\" must not be in `info_dct`, ",
			"as it will be added later in this function"
		)
		assert "busy" not in info_dct, psconcat(
			"\"busy\" must not be in `info_dct`, ",
			"as it will be added later in this function"
		)
		#if enable_ce:
		#	assert "ce" not in info_dct, psconcat(
		#		"\"ce\" must not be in `info_dct`, ",
		#		"as it will be added later"
		#	)
		#if enable_clear_pipe:
		#	assert "clear_pipe" not in info_dct, psconcat(
		#		"\"clear_pipe\" must not be in `info_dct`, ",
		#		"as it will be added later in this function"
		#	)
		assert "clear_pipe" not in info_dct, psconcat(
			"\"clear_pipe\" must not be in `info_dct`, ",
			"as it will be added later in this function"
		)

		info_dct["valid"] = PipeSigInfo(
			info=SigInfo(
				basenm="valid",
				shapelayt=1,
				attrs=valid_attrs,
			),
			psig_dir=PipeSigDir.Fwd,
			can_auto_copy=False,
			prefix=prefix,
			#enable_reg=enable_reg
		)
		info_dct["ready"] = PipeSigInfo(
			info=SigInfo(
				basenm="ready",
				shapelayt=1,
				attrs=ready_attrs,
			),
			psig_dir=PipeSigDir.Bak,
			can_auto_copy=False,
			prefix=prefix,
			#enable_reg=enable_reg
		)
		info_dct["busy"] = PipeSigInfo(
			info=SigInfo(
				basenm="busy",
				shapelayt=1,
				attrs=busy_attrs,
			),
			psig_dir=PipeSigDir.RegOnly,
			can_auto_copy=False,
			prefix=prefix,
			#enable_reg=enable_reg
		)
		#if enable_clear_pipe:
			#info_dct["ce"] = PipeSigInfo(
			#	basenm="ce",
			#	psig_dir=PipeSigDir.Fwd,
			#	shapelayt=1,
			#	attrs=ce_attrs,
			#	can_auto_copy=False,
			#	prefix=prefix,
			#	#enable_reg=enable_reg
			#)
			#info_dct["clear_pipe"] = PipeSigInfo(
			#	info=SigInfo(
			#		basenm="clear_pipe",
			#		shapelayt=1,
			#		attrs=clear_pipe_attrs,
			#	),
			#	psig_dir=PipeSigDir.InpOnly,
			#	can_auto_copy=False,
			#	prefix=prefix,
			#	#enable_reg=enable_reg
			#)
		info_dct["clear_pipe"] = PipeSigInfo(
			info=SigInfo(
				basenm="clear_pipe",
				shapelayt=1,
				attrs=clear_pipe_attrs,
			),
			psig_dir=PipeSigDir.InpOnly,
			can_auto_copy=False,
			prefix=prefix,
			#enable_reg=enable_reg
		)
		#for sig_info in info_dct:
		#	for name in sig_info.signm_lst():
		#		self.__dict__[name] = sig_info.ObjKind()(
		#			sig_info.shapelayt(),
		#			name=prefix + name,
		#			reset=sig_info.reset(),
		#			attrs=sig_info.attrs(),
		#		)
		for sig_info in info_dct:
			for signm_suffix in sig_info.signm_suffix_lst():
				#name = psconcat(sig_info.basenm(), signm_suffix)
				#self.__dict__[name] = sig_info.ObjKind()(
				#	sig_info.shapelayt(),
				#	name=psconcat(prefix, name),
				#	reset=sig_info.reset(),
				#	attrs=sig_info.attrs(),
				#)
				sig_info.info().mk_sig(
					prefix=prefix,
					suffix=signm_suffix,
				)

class PipeBox:
	def __init__(
		self,
		info_dct: OrderedDict,
		#enable_clear_pipe: bool=False,
		*,
		prefix: str="",
		#enable_reg: bool=True,
		valid_attrs: str=sig_keep(),
		ready_attrs: str=sig_keep(),
		busy_attrs: str=sig_keep(),
		#ce_attrs: str=sig_keep(),
		clear_pipe_attrs: str=sig_keep()
	):
		self.__info_dct = info_dct,
		#self.__enable_clear_pipe = enable_clear_pipe

		self.__prefix = prefix
		#self.__enable_reg = enable_reg
		self.__valid_attrs = valid_attrs
		self.__ready_attrs = ready_attrs
		self.__busy_attrs = busy_attrs
		self.__clear_pipe_attrs = clear_pipe_attrs
		#self.__ce_attrs = ce_attrs

		self.__bundle = PipeBundle(box=self)

	def info_dct(self):
		return self.__info_dct
	def valid_attrs(self):
		return self.__valid_attrs
	def ready_attrs(self):
		return self.__ready_attrs
	def busy_attrs(self):
		return self.__busy_attrs
	#def ce_attrs(self):
	#	# This `assert` might not be necessary, but I'll include it anyway
	#	# for safety
	#	assert self.enable_ce()
	#	return self.__ce_attrs
	def clear_pipe_attrs(self):
		# This `assert` might not be necessary, but I'll include it anyway
		# for safety
		#assert self.enable_clear_pipe()
		return self.__clear_pipe_attrs
	def prefix(self):
		return self.__prefix
	def bundle(self):
		return self.__bundle

	##def enable_reg(self):
	##	return self.__enable_reg
	##	#return !self.enable_clear_pipe()
	##def enable_ce(self):
	##	return self.__enable_ce
	#def enable_clear_pipe(self):
	#	return self.__enable_clear_pipe

	def inp(self, basenm):
		info = self.info_dct()[basenm]
		assert info.have_inp()
		return self.bundle().__dict__[info.inpnm()]
	def outp(self, basenm):
		info = self.info_dct()[basenm]
		assert info.have_outp()
		return self.bundle().__dict__[info.outpnm()]
	def reg(self, basenm):
		#assert self.enable_reg()
		assert info.have_reg()
		return self.bundle().__dict__[self.info_dct()[basenm].regnm()]
	@staticmethod
	def connect(
		m: Module,
		box_lst: list
		#box: PipeBox,
		#box_next: PipeBox,
	) -> OrderedDict:
		def do_connect(
			m: Module,
			box_lst: list,
			i: int,
			ret: OrderedDict,
		):
			box = box_lst[i]
			box_next = box_lst[i + 1]

			for item in box.info_dct().items():
				if (
					item[0] not in box.info_dct()
					or item[0] not in box_next.info_dct()
				):
					continue

				info = box.info_dct()[item[0]]
				info_next = box_next.info_dct()[item[0]]

				if (
					# This is a heuristic
					not (
						info.psig_dir() == info_next.psig_dir()
						and info.ObjKind() == info_next.ObjKind()
						and info.shapelayt() == info_next.shapelayt()
					)
				):
					if (i not in ret):
						ret[i] = OrderedDict()
					ret[i][item[0]] = OrderedDict([
						("i", i),
						("basenm", item[0]),
						("box", box),
						("box_next", box_next),
						("info", info),
						("info_next", info_next),
					])
					continue

				# actually do the connection
				if info.psig_dir() == PipeSigDir.Fwd:
					m.d.sync += (
						box_next.inp(item[0]).eq(box.outp(item[0]))
					)
				elif info.psig_dir() == PipeSigDir.Bak:
					m.d.sync += (
						box.inp(item[0]).eq(box_next.outp(item[0]))
					)
		#if len(box_lst) < 2:
		#	return
		assert len(box_lst) >= 2
		ret = OrderedDict()

		for i in range(len(box_lst) - 1):
			do_connect(
				m=m,
				box_lst=box_lst,
				i=i,
				ret=ret,
			)
		return ret

## This probably could have been an `Elaboratable`, but doing things this
## way allows for not having another nesting level of `Module`s.
#class CpuInOrderPstageGen:
#	def __init__(
#		self,
#
#		parent: Module,
#
#		# This needs to have `(enable_reg == False) && (enable_ce == True)`
#		box: PipeBox,
#
#		# This is similar to the `logic_func()` of `SkidBufPstageGen`, but
#		# here it should only take one argument, that being
#		# this `CpuInOrderPstageGen` (`self`).
#		logic_func,
#
#		*,
#		# locals (from some `elaborate()` function) for use in
#		# `logic_func()`
#		loc=None,
#	):
#		self.__parent = parent
#		self.__box = box
#		self.__logic_func = logic_func
#		self.__loc = loc
#	def parent(self):
#		return self.__parent
#	#def bus(self):
#	#	return self.__bus
#	def box(self):
#		return self.__box
#	def info_dct(self):
#		return self.box().info_dct()
#		#return self.bundle().info_dct()
#	def bundle(self):
#		return self.box().bundle()
#	def logic_func(self):
#		return self.__logic_func
#	def loc(self):
#		return self.__loc
#	def gen(self):
#		m = self.parent()
#		box = self.box

# This probably could have been an `Elaboratable`, but doing things this
# way allows for not having another nesting level of `Module`s.
class SkidBufPstageGen:
	def __init__(
		self,

		parent: Module,
		#info_dct: OrderedDict,

		box: PipeBox,
		#clear_pipe_sig: Signal,

		# `logic_func` takes this `SkidBufPstageGen` (`self`) as its
		# first argument.
		# The second argument is either
		# `self.box().outp` or `self.box().regp`,
		# so you can call that to select which signals to drive in this
		# pipeline stage.
		# "valid" and "ready" should definitely not be driven by
		# `logic_func()`, and there is probably no reason to read from
		# `valid` or `ready`, either, as that logic is taken care of in
		# `SkidBufPstageGen.gen()`
		logic_func,

		*,
		#FORMAL: bool=False,
		#OPT_LOWPOWER: bool=0b0,
		#OPT_OUTREG: bool=0b1,

		# locals (from some `elaborate()` function) for use in
		# `logic_func()`
		loc=None, 
	):
		self.__parent = parent
		self.__box = box
		#self.__clear_pipe_sig = clear_pipe_sig

		self.__logic_func = logic_func

		#self.__FORMAL = FORMAL
		#self.__OPT_LOWPOWER = OPT_LOWPOWER
		#self.__OPT_OUTREG = OPT_OUTREG
		self.__loc = loc

	def parent(self):
		return self.__parent
	#def bus(self):
	#	return self.__bus
	def box(self):
		return self.__box
	def info_dct(self):
		return self.box().info_dct()
		#return self.bundle().info_dct()
	def bundle(self):
		return self.box().bundle()
	def logic_func(self):
		return self.__logic_func
	#def FORMAL(self):
	#	return self.__FORMAL
	#def OPT_LOWPOWER(self):
	#	return self.__OPT_LOWPOWER
	#def OPT_OUTREG(self):
	#	return self.__OPT_OUTREG
	def loc(self):
		return self.__loc
	def gen(self):
		m = self.parent()

		box = self.box()
		inp = box.inp
		outp = box.outp
		reg = box.reg

		info_dct = self.info_dct()
		logic_func = self.logic_func()

		#FORMAL = self.FORMAL()
		#OPT_LOWPOWER = self.OPT_LOWPOWER()
		#OPT_OUTREG = self.OPT_OUTREG()

		#m.d.comb += [
		#	# upstream ready
		#	# OR the upstream READY with the BUSY signal
		#	outp("ready").eq(inp("ready") | reg("busy")),

		#	# downstream valid
		#	# AND the downstream valid !BUSY
		#	outp("valid").eq(inp("valid") & ~reg("busy")),
		#]

		#with m.If(ResetSignal()):
		#	pass
		##with m.Elif(OPT_LOWPOWER & ResetSignal()):
		##	pass
		### else if (i_valid && o_ready)
		## else if (!o_valid || i_ready)
		#with m.Elif(outp("valid") | inp("ready")):
		#	logic_func(self, reg)

	# Old code below
	#def gen(self):
	#	# Translated logic from here:
	#	# https://zipcpu.com/blog/2017/08/14/strategies-for-pipelining.html
	#	m = self.parent()
	#	box = self.box()
	#	inp = box.inp
	#	outp = box.outp
	#	reg = box.reg

	#	info_dct = self.info_dct()
	#	logic_func = self.logic_func()

	#	with m.If(ResetSignal()):
	#		pass
	#	# If the next stage is not busy
	#	# if (!i_busy)
	#	with m.Elif(~inp("busy")):
	#		# if (!r_stb)
	#		with m.If(~reg("stb")):
	#			# Nothing is in the buffer, so send the input directly
	#			# to the output.
	#			# o_stb <= i_stb;
	#			m.d.sync += outp("stb").eq(inp("stb"))

	#			# This `logic_func()` function is arbitrary, and
	#			# specific to what this stage is supposed to do.
	#			# o_data <= logic(i_data);
	#			logic_func(self, outp)
	#		# else
	#		with m.Else(): # If(reg("stb")):
	#			# `outp("busy")` is true and something is in our
	#			# buffer.
	#			# Flush the buffer to the output port.

	#			# o_stb <= 1'b1;
	#			m.d.sync += outp("stb").eq(0b1)

	#			# o_data <= r_data;
	#			for item in info_dct.items():
	#				if item[1].can_auto_copy():
	#					m.d.sync += [
	#						outp(basenm).eq(reg(basenm))
	#					]

	#			# We can ignore the input in this case, since we'll
	#			# only be here if `outp("busy")` is also true

	#		m.d.sync += [
	#			# We can also clear any stall condition.
	#			# o_busy <= 1'b0;
	#			outp("busy").eq(0b0),

	#			# And declare the register to be empty.
	#			# r_stb <= 1'b0;
	#			reg("stb").eq(0b0),
	#		]
	#	# Else, the next stage is busy
	#	# else if (!o_stb)
	#	with m.Elif(~outp("stb")):
	#		m.d.sync += [
	#			# o_stb <= i_stb;
	#			outp("stb").eq(inp("stb")),

	#			# o_busy <= 1'b0;
	#			outp("busy").eq(0b0),

	#			# Keep the buffer empty
	#			# r_stb <= 1'b0;
	#			reg("stb").eq(0b0),
	#		]
	#		# Apply the logic to the input data, and set the output
	#		# data
	#		# o_data <= logic(i_data);
	#		logic_func(self, outp)
	#	# i_busy and o_stb are both true
	#	# else if ((i_stb) && (!o_busy))
	#	with m.Elif(inp("stb") & ~outp("busy")):
	#		# If the next stage *is* busy, though, and we haven't stalled
	#		# yet, then we need to accept the requested value from the
	#		# input. We'll place it into a temporary location.
	#		m.d.sync += [
	#			# r_stb <= (i_stb) && (o_stb);
	#			reg("stb").eq(
	#				inp("stb") & outp("stb")
	#			),

	#			# o_busy <= (i_stb) && (o_stb);
	#			outp("busy").eq(
	#				inp("stb") & outp("stb")
	#			),
	#		]
	#	# if (!o_busy)
	#	with m.If(~ResetSignal() & ~outp("busy")):
	#		# r_data <= logic(i_data);
	#		logic_func(self, bus.reg)


#def skid_buf_pstage(
#	loc,		# where to put the generated signals
#	m: Module,
#	logic_func, # should take `m` as its first argument
#	info_dct: OrderedDict,
#):


#def add_ps_pair_mbr(ObjKind, shape, )
#class SkidBuffer:
#	def __init__(
#		self, 
#	):
#		self.
