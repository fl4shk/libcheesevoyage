#!/usr/bin/env python3

import enum as pyenum

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum
#from libcheesevoyage.misc_util import psconcat, mk_keep_obj
from libcheesevoyage.misc_util import psconcat, sig_keep, Blank
from libcheesevoyage.general.container_types import (
	cast_shape, SigInfo, FieldInfo, Splitrec,
	PortDir, Modport, IntfShape, Splitintf,
)
#from libcheesevoyage.math_lcv.reduce_tree_mod import *


#class PstageInpNodata(Splitrec):
#class PstageInpNodataLayt(dict):
#	def __init__(
#		self,
#		#data_info: SigInfo,
#		*,
#		OPT_INCLUDE_BUSY: bool=False,
#	):
#		#super().__init__()
#		shape = {}
#		shape["valid"] = FieldInfo(
#			1, name="inp_valid", attrs=sig_keep()
#		)
#		shape["ready"] = FieldInfo(
#			1, name="inp_ready", attrs=sig_keep()
#		)
#		if OPT_INCLUDE_BUSY:
#			shape["busy"] = FieldInfo(
#				1, name="inp_busy", attrs=sig_keep()
#			)
#		#shape["data"] = self.ObjKind()(self.shape(),
#		#	name="inp_data", attrs=data_info.attrs())
#
#		#shape["data"] = SigInfo.like(
#		#	data_info, basenm="inp_data",
#		#).mk_sig()
#
#		# Use this if uncommenting
#		#shape["data"] = data_info.mk_sig(basenm="inp_data")
#
#
#		#shape["clear"] = self.ObjKind()(self.shape(),
#		#	name="inp_clear", attrs=sig_keep())
#		shape["clear"] = FieldInfo(
#			1, name="inp_clear", attrs=sig_keep()
#		)
#		super().__init__(shape)
##class PstageOutpNodata(Splitrec):
#class PstageOutpNodataLayt(dict):
#	def __init__(
#		self,
#		#data_info: SigInfo,
#	):
#		#super().__init__()
#		shape = {}
#		shape["valid"] = FieldInfo(
#			1, name="outp_valid", attrs=sig_keep()
#		)
#		shape["ready"] = FieldInfo(1,
#			name="outp_ready", attrs=sig_keep()
#		)
#		#shape["busy"] = FieldInfo(
#		#	1, name="outp_busy", attrs=sig_keep()
#		#)
#		#shape["data"] = self.ObjKind()(self.shape(),
#		#	name="outp_data", attrs=self.data_attrs())
#		#shape["data"] = data_info.mk_outp_sig_w_basenm(
#		#	basenm="outp_data"
#		#)["sig"]
#		#shape["data"] = SigInfo.like(
#		#	data_info, basenm="outp_data",
#		#).mk_sig()
#		#shape["data"] = data_info.mk_nosuf_sig_w_basenm(
#		#	basenm="outp_data"
#		#)["sig"]
#
#		# Use this if uncommenting
#		#shape["data"] = data_info.mk_sig(basenm="outp_data")
#		super().__init__(shape)

#class PstageInpNodataFwdLayt(dict):
#	def __init__(self):
#		shape = {}
#		shape["valid"] = FieldInfo(
#			1, name="inp_valid", attrs=sig_keep()
#		)
#		super().__init__(shape)
#class PstageInpNodataBakLayt(dict):
#	def __init__(self):
#		shape = {}
#		shape["ready"] = FieldInfo(
#			1, name="inp_ready", attrs=sig_keep()
#		)
#		super().__init__(shape)
#class PstageInpNodataNonpipeLayt(dict):
#	def __init__(self):
#		shape = {}
#class PstageOutpNodataFwdLayt(dict):
#	def __init__(self):
#		shape = {}
#		shape["valid"] = FieldInfo(
#			1, name="outp_fwd_valid", attrs=sig_keep()
#		)
#		super().__init__(shape)
#class PstageOutpNodataBakLayt(dict):
#	def __init__(self):
#		shape = {}
#		shape["ready"] = FieldInfo(1,
#			name="outp_bak_ready", attrs=sig_keep()
#		)
#		super().__init__(shape)

class PstageBusFwdIshape(IntfShape):
	def __init__(
		self,
		data_info: SigInfo,
		pdir: PortDir,
		#io_str: str,
		*,
		intf_tag=None,
	):
		shape = {}
		shape["valid"] = FieldInfo(
			1,
			#name=f"fwd_valid_{io_str}",
			name=(
				"inp_"
				if pdir == PortDir.Inp
				else "outp_"
			) + "fwd_valid",
			use_parent_name=False,
			attrs=sig_keep(),
		)
		shape["data"] = FieldInfo(
			data_info,
			#name=f"fwd_data_{io_str}",
			name=(
				"inp_"
				if pdir == PortDir.Inp
				else "outp_"
			) + "fwd_data",
			use_parent_name=False,
			attrs=sig_keep(),
		)
		mp_dct = {
			key: pdir
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			tag=intf_tag,
		)
class PstageBusBakIshape(IntfShape):
	def __init__(
		self,
		pdir: PortDir,
		#io_str: str,
		*,
		intf_tag=None,
	):
		shape = {}
		shape["ready"] = FieldInfo(
			1,
			#name=f"bak_ready_{io_str}",
			name=(
				"inp_"
				if pdir == PortDir.Inp
				else "outp_"
			) + "bak_ready",
			use_parent_name=False,
			attrs=sig_keep(),
		)
		mp_dct = {
			key: pdir
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			tag=intf_tag,
		)

class PipeSkidBufInpSideIshape(IntfShape):
	def __init__(
		self, 
		data_info: SigInfo,
		*,
		#OPT_INCLUDE_VALID_BUSY: bool=False,
		#OPT_INCLUDE_READY_BUSY: bool=False,
		fwd_intf_tag=None,
		bak_intf_tag=None,
		#misc_intf_tag=None,
	):
		shape = {}
		#shape["fwd"] = FieldInfo(
		#	PstageBusFwdLayt(
		#		data_info=data_info,
		#		#io_str="in"
		#	),
		#	use_parent_name=False,
		#	#attrs=sig_keep(),
		#)
		#shape["bak"] = FieldInfo(
		#	PstageBusBakLayt(
		#		#io_str="in"
		#	),
		#	use_parent_name=False,
		#	#attrs=sig_keep(),
		#)
		shape["fwd"] = PstageBusFwdIshape(
			data_info=data_info,
			pdir=PortDir.Inp,
			intf_tag=fwd_intf_tag,
		)
		shape["bak"] = PstageBusBakIshape(
			pdir=PortDir.Inp,
			intf_tag=bak_intf_tag,
		)
		#if OPT_INCLUDE_VALID_BUSY:
		#	shape["valid_busy"] = FieldInfo(1, name="valid_busy")
		#if OPT_INCLUDE_READY_BUSY:
		#	shape["ready_busy"] = FieldInfo(1, name="inp_ready_busy")
		#shape["clear"] = FieldInfo(1, name="inp_clear")
		#super().__init__(shape)
		#mp_dct = {
		#	key: PortDir.Inp
		#	for key in shape
		#}
		super().__init__(shape=shape)
class PipeSkidBufOutpSideIshape(IntfShape):
	def __init__(
		self,
		data_info: SigInfo,
		*,
		fwd_intf_tag=None,
		bak_intf_tag=None,
	):
		shape = {}
		#shape["fwd"] = FieldInfo(
		#	PstageBusFwdLayt(
		#		data_info=data_info,
		#		#io_str="out"
		#	),
		#	use_parent_name=False,
		#	#attrs=sig_keep(),
		#)
		#shape["bak"] = FieldInfo(
		#	PstageBusBakLayt(
		#		#io_str="out"
		#	),
		#	use_parent_name=False,
		#	#attrs=sig_keep(),
		#)
		shape["fwd"] = PstageBusFwdIshape(
			data_info=data_info,
			pdir=PortDir.Outp,
			intf_tag=fwd_intf_tag,
		)
		shape["bak"] = PstageBusBakIshape(
			pdir=PortDir.Outp,
			intf_tag=bak_intf_tag,
		)
		#mp_dct = {
		#	key: PortDir.Outp
		#	for key in shape
		#}
		super().__init__(shape=shape)
class PipeSkidBufMiscIshape(IntfShape):
	def __init__(
		self,
		*,
		OPT_INCLUDE_VALID_BUSY: bool,
		OPT_INCLUDE_READY_BUSY: bool,
		intf_tag=None,
	):
		shape = {}
		if OPT_INCLUDE_VALID_BUSY:
			shape["valid_busy"] = FieldInfo(1, name="valid_busy")
		if OPT_INCLUDE_READY_BUSY:
			shape["ready_busy"] = FieldInfo(1, name="inp_ready_busy")
		shape["clear"] = FieldInfo(1, name="inp_clear")

		mp_dct = {
			key: PortDir.Inp
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			tag=intf_tag,
		)
class PipeSkidBufIshape(IntfShape):
	def __init__(
		self,
		data_info: SigInfo,
		*,
		OPT_INCLUDE_VALID_BUSY: bool,
		OPT_INCLUDE_READY_BUSY: bool,
		next_intf_tag=None,
		prev_intf_tag=None,
		misc_intf_tag=None,
	):
		shape = {
			"inp": PipeSkidBufInpSideIshape(
				data_info=data_info,
				fwd_intf_tag=prev_intf_tag,
				bak_intf_tag=next_intf_tag,
				#misc_intf_tag=
			),
			"outp": PipeSkidBufOutpSideIshape(
				data_info=data_info,
				fwd_intf_tag=next_intf_tag,
				bak_intf_tag=prev_intf_tag,
			),
			"misc": PipeSkidBufMiscIshape(
				OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
				OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
				intf_tag=misc_intf_tag,
			)
		}
		#shape = {
		#	"fwd": PipeSkidBufSideIshape(
		#		data_info=data_info, tag=fwd_tag,
		#	),
		#	"bak": PipeSkidBufSideIshape(
		#		data_info=data_info, tag=bak_tag,
		#	),
		#	"misc": PipeSkidBufMiscIshape(
		#		data_info=data_info, tag=misc_tag,
		#		OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
		#		OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
		#	)
		#}
		#mp_dct = {
		#	#"inp": PortDir.Inp,
		#	#"outp": PortDir.Outp,
		#	"misc": PortDir.Inp,
		#}
		super().__init__(
			shape=shape,
			#modport=Modport(mp_dct),
			#tag=tag,
		)
class PipeSkidBufBus:
	def __init__(
		self,
		data_info: SigInfo,
		*,
		OPT_INCLUDE_VALID_BUSY: bool=False,
		OPT_INCLUDE_READY_BUSY: bool=False,
		#tag=None,
		next_intf_tag=None,
		prev_intf_tag=None,
		misc_intf_tag=None,
	):
		#self.__data_info = data_info
		#self.__OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY
		#self.__OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY
		ishape = PipeSkidBufIshape(
			data_info=data_info,
			OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
			OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
			#tag=tag,
			next_intf_tag=next_intf_tag,
			prev_intf_tag=prev_intf_tag,
			misc_intf_tag=misc_intf_tag,
		)
		#super().__init__(ishape)
		self.__bus = Splitintf(ishape)
		self.__data_info = data_info
		self.__OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY
		self.__OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY
		#self.inp = Splitrec(
		#	PipeSkidBufInpLayt(
		#		data_info=data_info,
		#		OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
		#		OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
		#	),
		#	#use_parent_name=False,
		#)
		#self.outp = Splitrec(
		#	PipeSkidBufOutpLayt(data_info=data_info),
		#	#use_parent_name=False,
		#)

		#self.inp = Blank()
		#self.outp = Blank()


	@property
	def bus(self):
		return self.__bus
	def data_info(self):
		return self.__data_info
	#def data_shape(self):
	#	#return self.__shape
	#	return self.data_info().shape()
	#def DataObjKind(self):
	#	return self.data_info().ObjKind()
	#def data_reset(self):
	#	return self.data_info().reset()
	def OPT_INCLUDE_VALID_BUSY(self):
		return self.__OPT_INCLUDE_VALID_BUSY
	def OPT_INCLUDE_READY_BUSY(self):
		return self.__OPT_INCLUDE_READY_BUSY

# Based on
# https://github.com/iammituraj/skid_buffer/blob/main/pipe_skid_buffer.sv
class PipeSkidBuf(Elaboratable):
	def __init__(
		self,
		data_info: SigInfo,
		*,
		OPT_INCLUDE_VALID_BUSY: bool=False,
		OPT_INCLUDE_READY_BUSY: bool=False,
	):
		self.__bus = PipeSkidBufBus(
			data_info=data_info,
			OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
			OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
		)
		self.__data_info = data_info
		self.__OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY
		self.__OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY

	def bus(self):
		return self.__bus
	def OPT_INCLUDE_VALID_BUSY(self):
		return self.__OPT_INCLUDE_VALID_BUSY
	def OPT_INCLUDE_READY_BUSY(self):
		return self.__OPT_INCLUDE_READY_BUSY
	def data_info(self):
		return self.__data_info
	#def data_shape(self):
	#	return self.data_info().shape()
	#def sig_attrs(self):
	#	return self.bus().sig_attrs()

	@staticmethod
	def STATE_WIDTH():
		return 1
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus().bus
		data_info = self.data_info()
		OPT_INCLUDE_VALID_BUSY = self.OPT_INCLUDE_VALID_BUSY()
		OPT_INCLUDE_READY_BUSY = self.OPT_INCLUDE_READY_BUSY()

		loc = Blank()
		#--------
		# // State encoding
		# localparam PIPE  = 1'b0 ;
		# localparam SKID = 1'b1 ;
		class State(enum.Enum, shape=PipeSkidBuf.STATE_WIDTH()):
			PIPE = 0b0
			SKID = 0b1

		# // State register
		# logic state_rg;

		# // Data buffer, Spare buffer
		# logic [DWIDTH-1 : 0] data_rg, sparebuff_rg; 

		# // Valid and Ready signals 
		# logic valid_rg, sparebuff_valid_rg, ready_rg;

		# // Pipeline ready signal
		# logic ready;
		sync_shape = {}
		comb_shape = {}

		sync_shape["state_rg"] = FieldInfo(
			State.as_shape(),
			#name="loc_state_rg", use_parent_name=False,
			attrs=sig_keep(),
			reset=State.PIPE
		)
		sync_shape["data_rg"] = FieldInfo(
			data_info,
			#name="loc_data_rg", use_parent_name=False,
			attrs=sig_keep(),
		)
		sync_shape["sparebuff_rg"] = FieldInfo(
			data_info,
			#name="loc_sparebuff_rg", use_parent_name=False,
			attrs=sig_keep(),
			use_parent_name=False,
		)

		sync_shape["valid_rg"] = FieldInfo(
			1,
			#name="loc_valid_rg", use_parent_name=False,
			attrs=sig_keep(),
		)
		sync_shape["sparebuff_valid_rg"] = FieldInfo(
			1,
			#name="loc_sparebuff_valid_rg", use_parent_name=False,
			attrs=sig_keep(),
		)
		sync_shape["ready_rg"] = FieldInfo(
			1,
			#name="loc_ready_rg", use_parent_name=False,
			attrs=sig_keep(),
		)
		loc.s = Splitrec(sync_shape)

		comb_shape["ready"] = FieldInfo(
			1,
			#name="loc_ready", use_parent_name=False,
			attrs=sig_keep(),
		)
		loc.c = Splitrec(comb_shape)

		# Synchronous logic
		with m.If(bus.misc.clear):
			m.d.sync += loc.s.eq(0x0)
		with m.Else(): # If(~bus.inp.clear):
			with m.Switch(loc.s.state_rg):
				# Stage where data is piped out or stored to spare buffer
				with m.Case(State.PIPE):
					# Pipe data out             
					# if (ready) begin
					# 	data_rg				<= i_data  ;
					# 	valid_rg			<= i_valid ;
					# 	ready_rg			<= 1'b1	 ;
					# end
					with m.If(loc.c.ready):
						m.d.sync += [
							loc.s.data_rg.eq(bus.inp.fwd.data),
							loc.s.valid_rg.eq(bus.inp.fwd.valid),
							loc.s.ready_rg.eq(0b1),
						]

					# Pipeline stall, store input data to spare buffer
					# (skid happened)
					# else begin
					# 	sparebuff_rg		  <= i_data  ;
					# 	sparebuff_valid_rg <= i_valid ;
					# 	ready_rg			  <= 1'b0	 ;
					# 	state_rg			  <= SKID	 ;
					# end
					with m.Else():
						m.d.sync += [
							loc.s.sparebuff_rg.eq(bus.inp.fwd.data),
							loc.s.sparebuff_valid_rg.eq(bus.inp.fwd.valid),
							loc.s.ready_rg.eq(0b0),
							loc.s.state_rg.eq(State.SKID),
						]
				# Stage to wait after data skid happened
				with m.Case(State.SKID):
					# Copy data from spare buffer to data buffer, resume
					# pipeline           
					# if (ready) begin
					# 	data_rg	<= sparebuff_rg		  ;
					# 	valid_rg <= sparebuff_valid_rg ;
					# 	ready_rg <= 1'b1				  ;
					# 	state_rg <= PIPE				  ;
					# end
					with m.If(loc.c.ready):
						m.d.sync += [
							loc.s.data_rg.eq(loc.s.sparebuff_rg),
							loc.s.valid_rg.eq(loc.s.sparebuff_valid_rg),
							loc.s.ready_rg.eq(0b1),
							loc.s.state_rg.eq(State.PIPE),
						]

		# Continuous assignments
		# assign ready   = i_ready || ~valid_rg ;
		# assign o_ready = ready_rg             ;
		# assign o_data  = data_rg              ;
		# assign o_valid = valid_rg ;
		m.d.comb += [
			loc.c.ready.eq(Mux(
				~bus.misc.clear
				& (
					# Use a Python "mux" instead of an Amaranth `Mux`
					# AND the upstream `ready` with ~`ready_busy`
					0b1
					if not OPT_INCLUDE_READY_BUSY
					else ~bus.inp.ready_busy
				),
				bus.inp.bak.ready | ~loc.s.valid_rg,
				0b0,
			)),
			bus.outp.bak.ready.eq(
				loc.s.ready_rg
				& (
					# Use a Python "mux" instead of an Amaranth `Mux`
					# AND the upstream `ready` with ~`ready_busy`
					0b1
					if not OPT_INCLUDE_READY_BUSY
					else ~bus.misc.ready_busy
				)
			),
			bus.outp.fwd.data.eq(loc.s.data_rg),
			bus.outp.fwd.valid.eq(
				loc.s.valid_rg
				& (
					# Use a Python "mux" instead of an Amaranth `Mux`
					# AND the downstream `valid` with ~`valid_busy`
					0b1
					if not OPT_INCLUDE_VALID_BUSY
					else ~bus.misc.valid_busy
				)
			),
		]

		#--------
		#--------
		return m
		#--------
	@staticmethod
	def connect(
		parent: Module,
		sb_bus_lst: list,
		tie_first_inp_fwd_valid: bool=True,
		tie_last_inp_bak_ready: bool=True,
	):
		assert len(sb_bus_lst) >= 2
		#print("testificate")

		for i in range(len(sb_bus_lst) - 1):
			sb_bus = sb_bus_lst[i]
			sb_bus_next = sb_bus_lst[i + 1]
			#parent.d.comb += [
			#	# Forwards connections
			#	sb_bus_next.inp.fwd.eq(sb_bus.outp.fwd),
			#	# Backwards connections
			#	sb_bus.inp.bak.eq(sb_bus_next.outp.bak),
			#]

			#sb_bus_next.inp.fwd.connect(
			#	other=sb_bus.outp.fwd,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel
			#)
			sb_bus.connect(
				other=sb_bus_next,
				m=parent,
				kind=Splitintf.ConnKind.Parallel,
				use_tag=True,
			)

			#sb_bus_next.inp.fwd.connect(
			#	other=sb_bus.outp.fwd,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel
			#)
			#sb_bus.inp.bak.connect(
			#	other=sb_bus_next.outp.bak,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel
			#)

			#fwd_flat = sb_bus.outp.fwd.flattened()
			#next_fwd_flat = sb_bus_next.inp.fwd.flattened()
			#bak_flat = sb_bus.inp.bak.flattened()
			#next_bak_flat = sb_bus_next.outp.bak.flattened()
			#for j in range(len(fwd_flat)):
			#	parent.d.comb += next_fwd_flat[j].eq(fwd_flat[j])
			#for j in range(len(bak_flat)):
			#	parent.d.comb += bak_flat[j].eq(next_bak_flat[j])
			#parent.d.comb += [
			#	sb_bus_next.inp.fwd.valid.eq(sb_bus.outp.fwd.valid),
			#	sb_bus_next.inp.fwd.data.eq(sb_bus.outp.fwd.data),
			#	sb_bus.inp.bak.ready.eq(sb_bus_next.outp.bak.ready),
			#]

		if tie_first_inp_fwd_valid:
			parent.d.comb += [
				sb_bus_lst[0].inp.fwd.valid.eq(0b1),
			]
		if tie_last_inp_bak_ready:
			parent.d.comb += [
				sb_bus_lst[-1].inp.bak.ready.eq(0b1),
			]
	@staticmethod
	def connect_child(
		parent: Module,
		parent_sb_bus,
		child_sb_bus,
		parent_data=None,
		use_tag: bool=True,
		reduce_tag: bool=True,
	):
		# This function is especially intended for when you have a
		# `<YourModule>Bus` that has its own `PipeSkidBufBus` instance that
		# you want to connect to the `PipeSkidBufBus` of an internal
		# `PipeSkidBuf`.

		# `parent_data` not being `None` provides the ability to do
		# internal processing on the `PipeSkidBuf`'s fwd output.
		# If you can't do all the needed processing on the `PipeSkidBuf`'s
		# fwd output, then you'll need to assert
		# `child_sb_bus.inp.valid_busy` until you're done with your
		# processing.

		#parent.d.comb += [
		#	child_sb_bus.inp.fwd.eq(parent_sb_bus.inp.fwd),
		#	##parent_sb_bus.outp.fwd.eq(child_sb_bus.outp.fwd),
		#	#child_sb_bus.inp.eq(parent_sb_bus.inp),
		#	parent_sb_bus.outp.fwd.valid.eq(child_sb_bus.outp.fwd.valid),
		#	child_sb_bus.inp.bak.eq(parent_sb_bus.inp.bak),
		#	parent_sb_bus.outp.bak.eq(child_sb_bus.outp.bak),
		#]

		parent_sb_bus.inp.fwd.connect(
			other=child_sb_bus.inp.fwd,
			m=parent,
			kind=Splitintf.ConnKind.Parent2Child,
			use_tag=use_tag,
			reduce_tag=reduce_tag,
		)
		parent.d.comb += [
			#child_sb_bus.inp.fwd.valid.eq(parent_sb_bus.inp.fwd.valid),
			parent_sb_bus.outp.fwd.valid.eq(child_sb_bus.outp.fwd.valid),
		]
		parent_sb_bus.inp.bak.connect(
			other=child_sb_bus.inp.bak,
			m=parent,
			kind=Splitintf.ConnKind.Parent2Child,
			use_tag=use_tag,
			reduce_tag=reduce_tag,
		)
		parent_sb_bus.outp.bak.connect(
			other=child_sb_bus.outp.bak,
			m=parent,
			kind=Splitintf.ConnKind.Parent2Child,
			use_tag=use_tag,
			reduce_tag=reduce_tag,
		)

		#if (
		#	parent_sb_bus.OPT_INCLUDE_VALID_BUSY()
		#	and child_sb_bus.OPT_INCLUDE_VALID_BUSY()
		#):
		#	m.d.comb += [
		#		child_sb_bus.misc.valid_busy.eq(
		#			parent_sb_bus.misc.valid_busy
		#		)
		#	]
		#if (
		#	parent_sb_bus.OPT_INCLUDE_READY_BUSY()
		#	and child_sb_bus.OPT_INCLUDE_READY_BUSY()
		#):
		#	m.d.comb += [
		#		child_sb_bus.misc.ready_busy.eq(
		#			parent_sb_bus.misc.ready_busy
		#		)
		#	]
		#if (
		#	"misc" in parent_sb_bus.dct()
		#	and "misc" in child_sb_bus.dct()
		#):
		# TODO: continue here
		parent_sb_bus.misc.connect(
			other=child_sb_bus.misc,
			m=parent,
			#kind=Splitintf.ConnKind.Parallel,
			kind=Splitintf.ConnKind.Parent2Child,
			#use_tag=True,
			use_tag=use_tag,
			reduce_tag=reduce_tag,
		)

		if parent_data is None:
			parent.d.comb += [
				parent_sb_bus.outp.fwd.data.eq(child_sb_bus.outp.fwd.data),
			]
		else: # if parent_data is not None:
			parent.d.comb += [
				parent_data["from_child"].eq(child_sb_bus.outp.fwd.data),
				parent_sb_bus.outp.fwd.data.eq(parent_data["to_out"]),
			]

#class SkidBufPstageGen:
#	def __init__(
#		self,
#		parent: Module,
#		data_info: SigInfo,
#		*,
#		#FORMAL: bool=False,
#		#OPT_INCLUDE_CLEAR: bool=False,
#		submod_prefix=None, # submodule prefix (can be a string)
#	):
#		self.__parent = parent
#		self.__data_info = data_info
#		self.__OPT_INCLUDE_BUSY = OPT_INCLUDE_BUSY
#		#self.__OPT_INCLUDE_CLEAR = OPT_INCLUDE_CLEAR
#		#self.__box = box
#		#self.__clear_pipe_sig = clear_pipe_sig
#
#		#self.__logic_func = logic_func
#
#		self.__submod_prefix = submod_prefix
#
#		#self.__FORMAL = FORMAL
#		#self.__OPT_LOWPOWER = OPT_LOWPOWER
#		#self.__OPT_OUTREG = OPT_OUTREG
#		#self.__loc = loc
#
#	def parent(self):
#		return self.__parent
#	def data_info(self):
#		return self.__data_info
#	#def bus(self):
#	#	return self.__bus
#	def OPT_INCLUDE_BUSY(self):
#		return self.__OPT_INCLUDE_BUSY
#	#def OPT_INCLUDE_CLEAR(self):
#	#	return self.__OPT_INCLUDE_CLEAR
#	def submod_prefix(self):
#		return self.__submod_prefix
#	def gen(self):
#		m = self.parent()
#
#		OPT_INCLUDE_BUSY = self.OPT_INCLUDE_BUSY()
#		submod_prefix = self.submod_prefix()
#		#OPT_INCLUDE_CLEAR = self.OPT_INCLUDE_CLEAR()
#
#		#info_dct = self.info_dct()
#		#bundle = self.bundle()
#		#logic_func = self.logic_func()
#
#		#for info in info_dct:
#		#	if info.is_data():
#		#		skid_buf = SkidBuf(
#		#			data_info=info.info()
#		#		)
#		#		sb_bus = skid_buf.bus()
#
#		#		m.d.comb += [
#		#			sb_bus.inp.valid.eq(inp("valid")),
#		#			sb_bus.inp.ready.eq(inp("ready")),
#		#			sb_bus.inp.data.eq(inp(info.inpnm())),
#		#			sb_bus.inp.clear.eq(inp("clear_pipe")),
#
#		#			#outp("ready").eq(sb_bus.outp.ready),
#		#			#outp("valid").eq(sb_bus.outp.valid),
#		#			#outp(info.outpnm()).eq(sb_bus.outp.data),
#		#		]
#		#		if OPT_INCLUDE_BUSY:
#		#			m.d.comb += sb_bus.inp.busy.eq(reg("busy")),
#
#		#		if submod_prefix is None:
#		#			m.submodules += skid_buf
#		#		else:
#		#			submod_name = psconcat(submod_prefix, info.basenm())
#		#			assert submod_name not in m.submodules
#		#			setattr(m.submodules, submod_name, skid_buf)
#
#		#skid_buf = PipeSkidBuf(
#		#	data_info=SigInfo.like(self.inp.data_info, basenm="data"),
#		#	OPT_INCLUDE_BUSY=OPT_INCLUDE_BUSY
#		#)
#
#		#sb_bus = skid_buf.bus()
#		##m.d.comb += [
#		##	sb_bus.inp.nodata.valid.eq(inp.nodata.valid),
#		##	sb_bus.inp.nodata.ready.eq(inp.nodata.ready),
#
#		##	#sb_bus.inp.data.eq(inp.data),
#		##	sb_bus.inp.data.eq(reg.data),
#
#		##	sb_bus.inp.nodata.clear.eq(inp.nodata.clear_pipe),
#
#		##	outp.nodata.ready.eq(sb_bus.outp.nodata.ready),
#		##	outp.nodata.valid.eq(sb_bus.outp.nodata.valid),
#		##	outp.data.eq(sb_bus.outp.data),
#		##	#reg.data.eq(sb_bus.outp.data),
#		##]
#		##if OPT_INCLUDE_BUSY:
#		##	m.d.comb += sb_bus.inp.bus.eq(reg.busy)
#		#m.d.comb += [
#		#	sb_bus.inp.nodata.eq(inp.nodata),
#		#	#sb_bus.inp.data.eq(reg.data),
#		#	sb_bus.inp.data.eq(inp.data),
#		#	outp.nodata.eq(sb_bus.outp.nodata),
#		#	#outp.data.eq(sb_bus.outp.data),
#		#	reg.data.eq(sb_bus.outp.data),
#		#]
#		skid_buf = PipeSkidBuf(
#			data_info=self.data_info(), OPT_INCLUDE_BUSY=OPT_INCLUDE_BUSY,
#		)
#		sb_bus = skid_buf.bus()
#		if OPT_INCLUDE_BUSY:
#
#		if submod_prefix is None:
#			m.submodules += skid_buf
#		else:
#			submod_name = psconcat(submod_prefix, info.basenm())
#			assert submod_name not in m.submodules
#			setattr(m.submodules, submod_name, skid_buf)
#
#		#setattr(loc, submod_prefix, Blank())
#
#		#FORMAL = self.FORMAL()
#		#OPT_LOWPOWER = self.OPT_LOWPOWER()
#		#OPT_OUTREG = self.OPT_OUTREG()
#
#	# Old code below
#	#def gen(self):
#	#	# Translated logic from here:
#	#	# https://zipcpu.com/blog/2017/08/14/strategies-for-pipelining.html
#	#	m = self.parent()
#	#	box = self.box()
#	#	inp = box.inp
#	#	outp = box.outp
#	#	reg = box.reg
#
#	#	info_dct = self.info_dct()
#	#	logic_func = self.logic_func()
#
#	#	with m.If(ResetSignal()):
#	#		pass
#	#	# If the next stage is not busy
#	#	# if (!i_busy)
#	#	with m.Elif(~inp("busy")):
#	#		# if (!r_stb)
#	#		with m.If(~reg("stb")):
#	#			# Nothing is in the buffer, so send the input directly
#	#			# to the output.
#	#			# o_stb <= i_stb;
#	#			m.d.sync += outp("stb").eq(inp("stb"))
#
#	#			# This `logic_func()` function is arbitrary, and
#	#			# specific to what this stage is supposed to do.
#	#			# o_data <= logic(i_data);
#	#			logic_func(self, outp)
#	#		# else
#	#		with m.Else(): # If(reg("stb")):
#	#			# `outp("busy")` is true and something is in our
#	#			# buffer.
#	#			# Flush the buffer to the output port.
#
#	#			# o_stb <= 1'b1;
#	#			m.d.sync += outp("stb").eq(0b1)
#
#	#			# o_data <= r_data;
#	#			for item in info_dct.items():
#	#				if item[1].is_data():
#	#					m.d.sync += [
#	#						outp(basenm).eq(reg(basenm))
#	#					]
#
#	#			# We can ignore the input in this case, since we'll
#	#			# only be here if `outp("busy")` is also true
#
#	#		m.d.sync += [
#	#			# We can also clear any stall condition.
#	#			# o_busy <= 1'b0;
#	#			outp("busy").eq(0b0),
#
#	#			# And declare the register to be empty.
#	#			# r_stb <= 1'b0;
#	#			reg("stb").eq(0b0),
#	#		]
#	#	# Else, the next stage is busy
#	#	# else if (!o_stb)
#	#	with m.Elif(~outp("stb")):
#	#		m.d.sync += [
#	#			# o_stb <= i_stb;
#	#			outp("stb").eq(inp("stb")),
#
#	#			# o_busy <= 1'b0;
#	#			outp("busy").eq(0b0),
#
#	#			# Keep the buffer empty
#	#			# r_stb <= 1'b0;
#	#			reg("stb").eq(0b0),
#	#		]
#	#		# Apply the logic to the input data, and set the output
#	#		# data
#	#		# o_data <= logic(i_data);
#	#		logic_func(self, outp)
#	#	# i_busy and o_stb are both true
#	#	# else if ((i_stb) && (!o_busy))
#	#	with m.Elif(inp("stb") & ~outp("busy")):
#	#		# If the next stage *is* busy, though, and we haven't stalled
#	#		# yet, then we need to accept the requested value from the
#	#		# input. We'll place it into a temporary location.
#	#		m.d.sync += [
#	#			# r_stb <= (i_stb) && (o_stb);
#	#			reg("stb").eq(
#	#				inp("stb") & outp("stb")
#	#			),
#
#	#			# o_busy <= (i_stb) && (o_stb);
#	#			outp("busy").eq(
#	#				inp("stb") & outp("stb")
#	#			),
#	#		]
#	#	# if (!o_busy)
#	#	with m.If(~ResetSignal() & ~outp("busy")):
#	#		# r_data <= logic(i_data);
#	#		logic_func(self, bus.reg)


#def skid_buf_pstage(
#	loc,		# where to put the generated signals
#	m: Module,
#	logic_func, # should take `m` as its first argument
#	info_dct: dict,
#):


#def add_ps_pair_mbr(ObjKind, shape, )
#class SkidBuffer:
#	def __init__(
#		self, 
#	):
#		self.
