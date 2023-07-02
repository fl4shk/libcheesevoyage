#!/usr/bin/env python3

import enum as pyenum

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

#from libcheesevoyage.misc_util import psconcat, mk_keep_obj
from libcheesevoyage.misc_util import psconcat, sig_keep, Blank
from libcheesevoyage.general.container_types import (
	cast_shape, SigInfo, FieldInfo, Splitrec, Splitarr,
	PortDir, Modport, IntfShape, Splitintf,
)
#from libcheesevoyage.math_lcv.reduce_tree_mod import *

# The direction of data movement is called "fwd", short for "forward"
class PstageBusFwdIshape(IntfShape):
	def __init__(
		self,
		data_info,
		pdir: PortDir,
		#io_str: str,
		#*,
		#tag=None,
	):
		shape = {}
		shape["valid"] = FieldInfo(
			1,
			#name=f"fwd_valid_{io_str}",
			name=(
				"inp"
				if pdir == PortDir.Inp
				else "outp"
			) + "_fwd_valid",
			use_parent_name=True,
			attrs=sig_keep(),
		)
		if data_info is not None:
			shape["data"] = FieldInfo(
				data_info,
				#name=f"fwd_data_{io_str}",
				name=(
					"inp"
					if pdir == PortDir.Inp
					else "outp"
				) + "_fwd_data",
				use_parent_name=True,
				attrs=sig_keep(),
			)
		mp_dct = {
			key: pdir
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			#tag=tag,
		)
class PstageBusBakIshape(IntfShape):
	def __init__(
		self,
		pdir: PortDir,
		#io_str: str,
		#*,
		#tag=None,
	):
		shape = {}
		shape["ready"] = FieldInfo(
			1,
			#name=f"bak_ready_{io_str}",
			name=(
				"inp"
				if pdir == PortDir.Inp
				else "outp"
			) + "_bak_ready",
			use_parent_name=True,
			attrs=sig_keep(),
		)
		mp_dct = {
			key: pdir
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			#tag=tag,
		)

#class PstageHandshakeIshape(IntfShape):
#	def __init__(
#		self,
#	):
# may not need the below since a `PipeSkidBuf` can already handle this!
#class ValidReadyRegdHandshakeIshape(IntfShape):
#	def __init__(
#		self,
#		data_info,
#		*,
#		is_from: bool,
#		#tag_dct={
#		#	"host": None,
#		#	"dev": None,
#		#},
#		#tag=None,
#	):
#		shape = {}
#		super().__init__
#class ValidReadyRegdHandshakeBus:
#	def __init__(
#		self,
#		data_info
#	):

class PipeSkidBufSideIshape(IntfShape):
	def __init__(
		self,
		data_info,
		pdir: PortDir,
		#*,
		#OPT_INCLUDE_NEXT: bool=True,
		#OPT_INCLUDE_PREV: bool=True,
		#fwd_tag=None,
		#bak_tag=None,
		#tag_dct={
		#	"fwd": None,
		#	"bak": None,
		#},
	):
		shape = {}
		#if OPT_INCLUDE_NEXT:
		shape["fwd"] = PstageBusFwdIshape(
			data_info=data_info,
			pdir=pdir,
			#tag=fwd_tag,
			#tag=tag_dct["fwd"],
		)
		#if OPT_INCLUDE_PREV:
		shape["bak"] = PstageBusBakIshape(
			pdir=pdir,
			#tag=bak_tag,
			#tag=tag_dct["bak"],
		)
		super().__init__(shape=shape)
class PipeSkidBufMiscIshape(IntfShape):
	def __init__(
		self,
		*,
		OPT_INCLUDE_VALID_BUSY: bool,
		OPT_INCLUDE_READY_BUSY: bool,
		#OPT_INCLUDE_BUSY: bool,
		#tag=None,
	):
		shape = {}
		if OPT_INCLUDE_VALID_BUSY:
			shape["valid_busy"] = FieldInfo(1, name="valid_busy")
		if OPT_INCLUDE_READY_BUSY:
			shape["ready_busy"] = FieldInfo(1, name="inp_ready_busy")
		#if OPT_INCLUDE_BUSY:
		#	shape["busy"] = FieldInfo(1, name="busy")
		shape["clear"] = FieldInfo(1, name="inp_clear")

		mp_dct = {
			key: PortDir.Inp
			for key in shape
		}
		super().__init__(
			shape=shape,
			modport=Modport(mp_dct),
			#tag=tag,
		)
class PipeSkidBufIshape(IntfShape):
	def __init__(
		self,
		#data_info,
		inp_data_info,
		outp_data_info,
		*,
		OPT_INCLUDE_VALID_BUSY: bool,
		OPT_INCLUDE_READY_BUSY: bool,
		#OPT_INCLUDE_BUSY: bool=False,
		#OPT_INCLUDE_NEXT: bool=True,
		#OPT_INCLUDE_PREV: bool=True,
		#tag_dct={
		#	"next": None,
		#	"prev": None,
		#	"misc": None,
		#},
	):
		shape = {
			"inp": PipeSkidBufSideIshape(
				#data_info=data_info,
				data_info=inp_data_info,
				pdir=PortDir.Inp,
				#OPT_INCLUDE_NEXT=OPT_INCLUDE_NEXT,
				#OPT_INCLUDE_PREV=OPT_INCLUDE_PREV,
				#fwd_tag=tag_dct["prev"],
				#bak_tag=tag_dct["next"],
				#tag_dct={
				#	"fwd": tag_dct["prev"],
				#	"bak": tag_dct["next"],
				#},
			),
			"outp": PipeSkidBufSideIshape(
				#data_info=data_info,
				data_info=outp_data_info,
				pdir=PortDir.Outp,
				#OPT_INCLUDE_NEXT=OPT_INCLUDE_NEXT,
				#OPT_INCLUDE_PREV=OPT_INCLUDE_PREV,
				#fwd_tag=tag_dct["next"],
				#bak_tag=tag_dct["prev"],
				#tag_dct={
				#	"fwd": tag_dct["next"],
				#	"bak": tag_dct["prev"],
				#},
			),
			"misc": PipeSkidBufMiscIshape(
				OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
				OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
				#OPT_INCLUDE_BUSY=OPT_INCLUDE_BUSY,
				#tag=tag_dct["misc"],
			)
		}
		super().__init__(shape=shape)
	@staticmethod
	def mk_fromto_shape_info(
		#FromDataLayt,
		#ToDataLayt,
		data_layt_dct: dict,
		*,
		in_from: bool,
		name_dct: dict,
		#tag_dct: dict,
		#inner_tag_dct: dict={
		#	"next": "sb_next_tag",
		#	"prev": "sb_prev_tag",
		#	"misc": "sb_misc_tag",
		#},

		OPT_IN_FROM_INCLUDE_VALID_BUSY: bool=True,
		OPT_IN_FROM_INCLUDE_READY_BUSY: bool=True,
		OPT_NOT_IN_FROM_INCLUDE_VALID_BUSY: bool=True,
		OPT_NOT_IN_FROM_INCLUDE_READY_BUSY: bool=True,
		#OPT_INCLUDE_BUSY_DCT={
		#	"in_from": {
		#		"valid": True,
		#		"ready": True,
		#	},
		#	"not_in_from": {
		#		"valid": True,
		#		"ready": True,
		#	},
		#},

		OPT_IN_FROM_TIE_IFWD_VALID: bool=False,
		OPT_NOT_IN_FROM_TIE_IFWD_VALID: bool=False,

		## Regarding the below four options, typically there would not be
		## other connections when using
		## `PipeSkidBufIshape.mk_fromto_shape()`, as this `staticmethod` is
		## generally intended to be used for heterogeneous pipelines (like
		## with strict in order CPUs) rather than homogeneous pipelines
		## (like with libcheesevoyage's own `LongDivPipelined`)
		#OPT_IN_FROM_INCLUDE_NEXT: bool=False,
		#OPT_IN_FROM_INCLUDE_PREV: bool=False,
		#OPT_NOT_IN_FROM_INCLUDE_NEXT: bool=False,
		#OPT_NOT_IN_FROM_INCLUDE_PREV: bool=False,
		##OPT_INCLUDE_SIDE_DCT={
		##	"in_from": {
		##		"fwd": False,
		##		"bak": False,
		##	},
		##	"not_in_from": {
		##		"fwd": False,
		##		"bak": False,
		##	},
		##},
	):
		ret = {
			"shape_dct": {
				"from": None,
				"to": None,
			},
		}
		shape_dct = ret["shape_dct"]
		if (
			data_layt_dct["from"] is not None
			and name_dct["from"] is not None
		):
			ret["from"] = {
				"inp_data_info": SigInfo(
					#basenm="from_mctrl_data",
					basenm="data",
					shape=data_layt_dct["from"],
				),
				"outp_data_info": SigInfo(
					#basenm="from_mctrl_data",
					basenm="data",
					shape=data_layt_dct["from"],
				),
				"OPT_INCLUDE_VALID_BUSY": (
					OPT_NOT_IN_FROM_INCLUDE_VALID_BUSY and not in_from
				),
				"OPT_INCLUDE_READY_BUSY": (
					OPT_IN_FROM_INCLUDE_READY_BUSY and in_from
				),
				#"tag_dct": inner_tag_dct,
			}
			shape_dct["from"] = PipeSkidBufIshape(**ret["from"])
		if (
			data_layt_dct["to"] is not None
			and name_dct["to"] is not None
		):
			ret["to"] = {
				"inp_data_info": SigInfo(
					#basenm="from_mctrl_data",
					basenm="inp_data",
					shape=data_layt_dct["to"],
				),
				"outp_data_info": SigInfo(
					#basenm="from_mctrl_data",
					basenm="outp_data",
					shape=data_layt_dct["to"],
				),
				"OPT_INCLUDE_VALID_BUSY": (
					OPT_IN_FROM_INCLUDE_VALID_BUSY and in_from
				),
				"OPT_INCLUDE_READY_BUSY": (
					OPT_NOT_IN_FROM_INCLUDE_READY_BUSY and not in_from
				),
				#"tag_dct": inner_tag_dct,
			}
			shape_dct["to"] = PipeSkidBufIshape(**ret["to"])
		#ret["inner_tag_dct"] = inner_tag_dct,
		ret["shape"] = IntfShape.mk_fromto_shape(
			name_dct=name_dct,
			#shape_dct={
			#	"from": PipeSkidBufIshape(**ret["from"]),
			#	"to": PipeSkidBufIshape(**ret["to"]),
			#},
			shape_dct=shape_dct,
			in_from=in_from,
			#tag_dct=tag_dct,
			mk_modport_dct={key: False for key in ["from", "to"]},
		)
		#return IntfShape(shape)
		#return shape
		return ret
class PipeSkidBufBus:
	def __init__(
		self,
		#data_info,
		inp_data_info,
		outp_data_info,
		*,
		OPT_INCLUDE_VALID_BUSY: bool=False,
		OPT_INCLUDE_READY_BUSY: bool=False,
		#OPT_INCLUDE_BUSY: bool=False,
		#OPT_INCLUDE_NEXT: bool=True,
		#OPT_INCLUDE_PREV: bool=True,
		#tag_dct={
		#	"next": None,
		#	"prev": None,
		#	"misc": None,
		#}
	):
		#self.__data_info = data_info
		#self.__OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY
		#self.__OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY
		ishape = PipeSkidBufIshape(
			#data_info=data_info,
			inp_data_info=inp_data_info,
			outp_data_info=outp_data_info,
			OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
			OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
			#OPT_INCLUDE_BUSY=OPT_INCLUDE_BUSY,
			#OPT_INCLUDE_NEXT=OPT_INCLUDE_NEXT,
			#OPT_INCLUDE_PREV=OPT_INCLUDE_PREV,
			#tag_dct=tag_dct,
		)
		#super().__init__(ishape)
		self.__bus = Splitintf(ishape)
		self.__data_info = inp_data_info
		self.__OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY
		self.__OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY
		#self.__OPT_INCLUDE_BUSY = OPT_INCLUDE_BUSY
		#self.__OPT_INCLUDE_NEXT = OPT_INCLUDE_NEXT,
		#self.__OPT_INCLUDE_PREV = OPT_INCLUDE_PREV,
		#self.__next_tag = next_tag
		#self.__prev_tag = prev_tag
		#self.__misc_tag = misc_tag
		#self.__next_tag = tag_dct["next"]
		#self.__prev_tag = tag_dct["prev"]
		#self.__misc_tag = tag_dct["misc"]
		#self.__tag_dct = tag_dct
		#with open("debug-pipeline_mods-tag_dct.txt.ignore", "w") as f:
		#	f.writelines([
		#		psconcat(tag_dct)
		#	])
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
	#def OPT_INCLUDE_BUSY(self):
	#	return self.__OPT_INCLUDE_BUSY
	#def OPT_INCLUDE_NEXT(self):
	#	return self.__OPT_INCLUDE_NEXT
	#def OPT_INCLUDE_PREV(self):
	#	return self.__OPT_INCLUDE_PREV
	#def tag_dct(self):
	#	return self.__tag_dct
	#def next_tag(self):
	#	#return self.__next_tag
	#	return self.__tag_dct["next"]
	#def prev_tag(self):
	#	#return self.__prev_tag
	#	self.__tag_dct["prev"]
	#def misc_tag(self):
	#	#return self.__misc_tag
	#	self.__tag_dct["misc"]

class PipeSkidBuf(Elaboratable):
	def __init__(
		self,
		# `...data_info` can be `None` if for some reason all you want from
		# `PipeSkidBuf` is the valid-ready handshaking it does.
		# Otherwise, it is recommended that `...data_info` be of the
		# `SigInfo` type.
		inp_data_info, 
		outp_data_info, 
		*,
		FORMAL: bool=False,
		OPT_INCLUDE_VALID_BUSY: bool=False,
		OPT_INCLUDE_READY_BUSY: bool=False,
		#OPT_INCLUDE_NEXT: bool=True,
		#OPT_INCLUDE_PREV: bool=True,
		OPT_PASSTHROUGH: bool=False,
		OPT_TIE_IFWD_VALID: bool=False,
		#OPT_TIE_IBAK_READY: bool=False,
		#OPT_INCLUDE_BUSY: bool=False,
		#tag_dct={
		#	"next": None,
		#	"prev": None,
		#	"misc": None,
		#},
		#next_tag=None,
		#prev_tag=None,
		#misc_tag=None,
	):
		self.__bus = PipeSkidBufBus(
			#data_info=data_info,
			inp_data_info=inp_data_info,
			outp_data_info=outp_data_info,
			OPT_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
			OPT_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
			#OPT_INCLUDE_BUSY=OPT_INCLUDE_BUSY,
			#OPT_INCLUDE_NEXT=OPT_INCLUDE_NEXT,
			#OPT_INCLUDE_PREV=OPT_INCLUDE_PREV,
			#next_tag=tag_dct["next"],
			#prev_tag=tag_dct["prev"],
			#misc_tag=tag_dct["misc"],
			#tag_dct=tag_dct,
		)
		self.__data_info = inp_data_info
		self.__FORMAL = FORMAL
		self.__OPT_INCLUDE_VALID_BUSY = OPT_INCLUDE_VALID_BUSY
		self.__OPT_INCLUDE_READY_BUSY = OPT_INCLUDE_READY_BUSY
		#self.__OPT_INCLUDE_BUSY = OPT_INCLUDE_BUSY
		#self.__OPT_INCLUDE_NEXT = OPT_INCLUDE_NEXT
		#self.__OPT_INCLUDE_PREV = OPT_INCLUDE_PREV
		self.__OPT_PASSTHROUGH = OPT_PASSTHROUGH
		self.__OPT_TIE_IFWD_VALID = OPT_TIE_IFWD_VALID
		#self.__OPT_TIE_IBAK_READY = OPT_TIE_IBAK_READY

	def bus(self):
		return self.__bus
	def FORMAL(self):
		return self.__FORMAL
	def OPT_INCLUDE_VALID_BUSY(self):
		return self.__OPT_INCLUDE_VALID_BUSY
	def OPT_INCLUDE_READY_BUSY(self):
		return self.__OPT_INCLUDE_READY_BUSY
	#def OPT_INCLUDE_BUSY(self):
	#	return self.__OPT_INCLUDE_BUSY
	#def OPT_INCLUDE_NEXT(self):
	#	return self.__OPT_INCLUDE_NEXT
	#def OPT_INCLUDE_PREV(self):
	#	return self.__OPT_INCLUDE_PREV
	def OPT_TIE_IFWD_VALID(self):
		return self.__OPT_TIE_IFWD_VALID
	#def OPT_TIE_IBAK_READY(self):
	#	return self.__OPT_TIE_IBAK_READY
	def OPT_PASSTHROUGH(self):
		return self.__OPT_PASSTHROUGH
	def data_info(self):
		return self.__data_info
	#def data_shape(self):
	#	return self.data_info().shape()
	#def sig_attrs(self):
	#	return self.bus().sig_attrs()
	# Adapted from
	# https://zipcpu.com/blog/2019/05/22/skidbuffer.html
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus().bus

		ifwd = bus.inp.fwd
		ibak = bus.inp.bak
		ofwd = bus.outp.fwd
		obak = bus.outp.bak
		misc = bus.misc

		data_info = self.data_info()
		FORMAL = self.FORMAL()
		OPT_INCLUDE_VALID_BUSY = self.OPT_INCLUDE_VALID_BUSY()
		OPT_INCLUDE_READY_BUSY = self.OPT_INCLUDE_READY_BUSY()
		#OPT_INCLUDE_BUSY = self.OPT_INCLUDE_BUSY()
		#OPT_INCLUDE_NEXT = self.OPT_INCLUDE_NEXT()
		#OPT_INCLUDE_PREV = self.OPT_INCLUDE_PREV()
		OPT_TIE_IFWD_VALID = self.OPT_TIE_IFWD_VALID()
		#OPT_TIE_IBAK_READY = self.OPT_TIE_IBAK_READY()
		#OPT_PASSTHROUGH = False
		OPT_PASSTHROUGH = self.OPT_PASSTHROUGH()
		#OPT_PASSTHROUGH = True
		#--------
		if OPT_PASSTHROUGH:
			#m.d.comb += 
			#m.d.sync += [
			#	ofwd.valid.eq(ifwd.valid),
			#	ofwd.data.eq(ifwd.data),
			#	obak.ready.eq(ibak.ready),
			#]
			m.d.comb += ofwd.valid.eq(ifwd.valid)
			if data_info is not None:
				#m.d.comb += [
				with m.If(ifwd.valid & ibak.ready):
					m.d.sync += ofwd.data.eq(ifwd.data)
				#]
			m.d.comb += obak.ready.eq(ibak.ready)
		else: # if not OPT_PASSTHROUGH:
		#if True:
			loc = Blank()
			# Individual `Splitrec` members can be driven by different
			# clock domains
			loc.r_shape = {
				#"valid_next": 1,
				"valid": 1,
				"ready": 1,
			}
			if data_info is not None:
				loc.r_shape["data"] = data_info

			loc.r = Splitrec(loc.r_shape, name="loc_r")
			#--------
			if FORMAL:
				loc.formal = Blank()
				loc.formal.past_valid = Signal(
					1, name="formal_past_valid", reset=0b0,
				)
			#--------
			with m.If(
				ResetSignal()
				| misc.clear

				# TODO: not sure if the below is going to work, so might
				# need to comment it out
				#| (
				#	# Use a Python "mux" instead of an Amaranth `Mux`
				#	0b0
				#	#if not OPT_INCLUDE_VALID_BUSY
				#	#else misc.valid_busy
				#	if not OPT_INCLUDE_BUSY
				#	else misc.busy
				#)
			):
				m.d.sync += loc.r.valid.eq(0b0)
				#m.d.comb += loc.r.valid_next.eq(0b0)
			with m.Elif(
				(ifwd.valid & obak.ready)
				& (ofwd.valid & ~ibak.ready)
			):
				m.d.sync += loc.r.valid.eq(0b1)
				#m.d.comb += loc.r.valid_next.eq(0b1)
			with m.Elif(ibak.ready):
				m.d.sync += loc.r.valid.eq(0b0)
				#m.d.comb += loc.r.valid_next.eq(0b0)
			#with m.Else():
			#	m.d.comb += loc.r.valid_next.eq(loc.r.valid)

			#m.d.sync += [
			#	loc.r.valid.eq(
			#		loc.r.valid_next
			#		#& (
			#		#	0b1
			#		#	if not OPT_INCLUDE_VALID_BUSY
			#		#	else ~misc.valid_busy
			#		#)
			#	),
			#	loc.r.ready.eq(
			#		~loc.r.valid_next
			#		#& (
			#		#	0b1
			#		#	if not OPT_INCLUDE_READY_BUSY
			#		#	else ~misc.ready_busy
			#		#)
			#	)
			#]

			if data_info is not None:
				with m.If(
					ResetSignal()
					| misc.clear
					#| (
					#	# Use a Python "mux" instead of an Amaranth `Mux`
					#	0b0
					#	if not OPT_INCLUDE_BUSY
					#	else misc.busy
					#)
				):
					m.d.sync += loc.r.data.eq(0x0)
				with m.If(obak.ready):
					m.d.sync += loc.r.data.eq(ifwd.data)

			with m.If(
				ResetSignal()
				| misc.clear
			):
				m.d.comb += [
					obak.ready.eq(0b0),
					ofwd.valid.eq(0b0),
				]
			with m.Else():
				m.d.comb += [
					obak.ready.eq(
						~loc.r.valid
						#loc.r.ready
						#& (
						#	0b1
						#	if not OPT_INCLUDE_READY_BUSY
						#	# Use a Python "mux" instead of an Amaranth
						#	# `Mux` AND the upstream `ready` with
						#	# ~`ready_busy`
						#	else ~misc.ready_busy
						#)
						##& (
						##	# Use a Python "mux" instead of an Amaranth
						##	# `Mux`
						##	0b1
						##	if not OPT_INCLUDE_BUSY
						##	else ~misc.busy
						##)
					),
					ofwd.valid.eq(
						# `ifwd.valid` will be registered in the general
						# case
						(ifwd.valid | loc.r.valid)
						#& (
						#	0b1
						#	if not OPT_INCLUDE_VALID_BUSY
						#	# Use a Python "mux" instead of an Amaranth
						#	# `Mux` AND the downstream `valid` with
						#	# ~`valid_busy`
						#	else ~misc.valid_busy
						#)
						##& (
						##	# Use a Python "mux" instead of an Amaranth
						##	# `Mux`
						##	0b1
						##	if not OPT_INCLUDE_BUSY
						##	else ~misc.busy
						##)
					),
				]

			if data_info is not None:
				with m.If(loc.r.valid):
					m.d.comb += ofwd.data.eq(loc.r.data)
				with m.Else(): # If(~loc.r.valid):
					m.d.comb += ofwd.data.eq(ifwd.data)
			#--------
			if FORMAL:
				m.d.sync += loc.formal.past_valid.eq(0b1)
				with m.If(
					loc.formal.past_valid
					&
					~misc.clear
					#& (
					#	0b1
					#	if not OPT_INCLUDE_BUSY
					#	else ~misc.busy
					#)
					& (
						0b1
						if not OPT_INCLUDE_VALID_BUSY
						else ~misc.valid_busy
					) & (
						0b1
						if not OPT_INCLUDE_READY_BUSY
						else ~misc.ready_busy
					)
				):
					with m.If(
						ResetSignal()
						#| misc.clear
					):
						if not OPT_TIE_IFWD_VALID:
							m.d.sync += [
								Assert(~ifwd.valid),
							]
						m.d.sync += [
							#Assert(~ifwd.valid),
							Assert(~loc.r.valid & ~ofwd.valid),
						]
					#with m.Elif(~misc.clear):
					with m.Else():
						with m.If(ifwd.valid & ~obak.ready):
							m.d.sync += [
								Assert(ifwd.valid),
								#Assert(Stable(ifwd.data)),
							]
							if (
								not isinstance(loc.r.data, Splitrec)
								and not isinstance(loc.r.data, Splitarr)
							):
								m.d.sync += [
									Assert(Stable(ifwd.data)),
								]
							else:
								m.d.sync += [
									Assert(Stable(flat_elem))
									for flat_elem in ifwd.data.flattened()
								]
						with m.If(ofwd.valid & ~ibak.ready):
							m.d.sync += [
								Assert(obak.ready),
								#Assert(Stable(ofwd.data)),
							]
							if (
								not isinstance(loc.r.data, Splitrec)
								and not isinstance(loc.r.data, Splitarr)
							):
								m.d.sync += [
									Assert(Stable(ofwd.data)),
								]
							else:
								m.d.sync += [
									Assert(Stable(flat_elem))
									for flat_elem in ofwd.data.flattened()
								]
						with m.If(
							ifwd.valid & obak.ready
							& ofwd.valid & ~ibak.ready
						):
							m.d.sync += [
								Assert(loc.r.valid),
								#Assert(loc.r.data == Past(ifwd.data)),
							]
							if (
								not isinstance(loc.r.data, Splitrec)
								and not isinstance(loc.r.data, Splitarr)
							):
								m.d.sync += [
									Assert(loc.r.data == Past(ifwd.data))
								]
							else:
								r_data_flat = loc.r.data.flattened()
								i_data_flat = ifwd.data.flattened()
								m.d.sync += [
									Assert(r_data_flat[i]
										== Past(i_data_flat[i]))
									for i in range(len(r_data_flat))
								]
						with m.If(~ifwd.valid & ~loc.r.valid & ibak.ready):
							m.d.sync += [
								Assert(ofwd.valid),
							]
						with m.If(loc.r.valid & ibak.ready):
							m.d.sync += [
								Assert(~loc.r.valid)
							]
		#--------
		return m
		#--------

	## Based on
	## https://github.com/iammituraj/skid_buffer/blob/main/pipe_skid_buffer.sv
	#@staticmethod
	#def STATE_WIDTH():
	#	return 1
	#def elaborate(self, platform: str) -> Module:
	#	#--------
	#	m = Module()
	#	#--------
	#	bus = self.bus().bus
	#	data_info = self.data_info()
	#	OPT_INCLUDE_VALID_BUSY = self.OPT_INCLUDE_VALID_BUSY()
	#	OPT_INCLUDE_READY_BUSY = self.OPT_INCLUDE_READY_BUSY()

	#	loc = Blank()
	#	#--------
	#	# // State encoding
	#	# localparam PIPE  = 1'b0 ;
	#	# localparam SKID = 1'b1 ;
	#	class State(enum.Enum, shape=PipeSkidBuf.STATE_WIDTH()):
	#		PIPE = 0b0
	#		SKID = 0b1

	#	# // State register
	#	# logic state_rg;

	#	# // Data buffer, Spare buffer
	#	# logic [DWIDTH-1 : 0] data_rg, sparebuff_rg; 

	#	# // Valid and Ready signals 
	#	# logic valid_rg, sparebuff_valid_rg, ready_rg;

	#	# // Pipeline ready signal
	#	# logic ready;
	#	sync_shape = {}
	#	comb_shape = {}

	#	sync_shape["state_rg"] = FieldInfo(
	#		State.as_shape(),
	#		#name="loc_state_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#		reset=State.PIPE
	#	)
	#	sync_shape["data_rg"] = FieldInfo(
	#		data_info,
	#		#name="loc_data_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#	)
	#	sync_shape["sparebuff_rg"] = FieldInfo(
	#		data_info,
	#		#name="loc_sparebuff_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#		use_parent_name=False,
	#	)

	#	sync_shape["valid_rg"] = FieldInfo(
	#		1,
	#		#name="loc_valid_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#	)
	#	sync_shape["sparebuff_valid_rg"] = FieldInfo(
	#		1,
	#		#name="loc_sparebuff_valid_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#	)
	#	sync_shape["ready_rg"] = FieldInfo(
	#		1,
	#		#name="loc_ready_rg", use_parent_name=False,
	#		attrs=sig_keep(),
	#	)
	#	loc.s = Splitrec(sync_shape)

	#	comb_shape["ready"] = FieldInfo(
	#		1,
	#		#name="loc_ready", use_parent_name=False,
	#		attrs=sig_keep(),
	#	)
	#	loc.c = Splitrec(comb_shape)

	#	# Synchronous logic
	#	with m.If(bus.misc.clear):
	#		m.d.sync += loc.s.eq(0x0)
	#	with m.Else(): # If(~bus.inp.clear):
	#		with m.Switch(loc.s.state_rg):
	#			# Stage where data is piped out or stored to spare buffer
	#			with m.Case(State.PIPE):
	#				# Pipe data out             
	#				# if (ready) begin
	#				# 	data_rg				<= i_data  ;
	#				# 	valid_rg			<= i_valid ;
	#				# 	ready_rg			<= 1'b1	 ;
	#				# end
	#				with m.If(loc.c.ready):
	#					m.d.sync += [
	#						loc.s.data_rg.eq(bus.inp.fwd.data),
	#						loc.s.valid_rg.eq(bus.inp.fwd.valid),
	#						loc.s.ready_rg.eq(0b1),
	#					]

	#				# Pipeline stall, store input data to spare buffer
	#				# (skid happened)
	#				# else begin
	#				# 	sparebuff_rg		  <= i_data  ;
	#				# 	sparebuff_valid_rg <= i_valid ;
	#				# 	ready_rg			  <= 1'b0	 ;
	#				# 	state_rg			  <= SKID	 ;
	#				# end
	#				with m.Else():
	#					m.d.sync += [
	#						loc.s.sparebuff_rg.eq(bus.inp.fwd.data),
	#						loc.s.sparebuff_valid_rg.eq(bus.inp.fwd.valid),
	#						loc.s.ready_rg.eq(0b0),
	#						loc.s.state_rg.eq(State.SKID),
	#					]
	#			# Stage to wait after data skid happened
	#			with m.Case(State.SKID):
	#				# Copy data from spare buffer to data buffer, resume
	#				# pipeline           
	#				# if (ready) begin
	#				# 	data_rg	<= sparebuff_rg		  ;
	#				# 	valid_rg <= sparebuff_valid_rg ;
	#				# 	ready_rg <= 1'b1				  ;
	#				# 	state_rg <= PIPE				  ;
	#				# end
	#				with m.If(loc.c.ready):
	#					m.d.sync += [
	#						loc.s.data_rg.eq(loc.s.sparebuff_rg),
	#						loc.s.valid_rg.eq(loc.s.sparebuff_valid_rg),
	#						loc.s.ready_rg.eq(0b1),
	#						loc.s.state_rg.eq(State.PIPE),
	#					]

	#	# Continuous assignments
	#	# assign ready   = i_ready || ~valid_rg ;
	#	# assign o_ready = ready_rg             ;
	#	# assign o_data  = data_rg              ;
	#	# assign o_valid = valid_rg ;
	#	m.d.comb += [
	#		loc.c.ready.eq(Mux(
	#			~bus.misc.clear
	#			& (
	#				# Use a Python "mux" instead of an Amaranth `Mux`
	#				# AND the upstream `ready` with ~`ready_busy`
	#				0b1
	#				if not OPT_INCLUDE_READY_BUSY
	#				else ~bus.inp.ready_busy
	#			),
	#			bus.inp.bak.ready | ~loc.s.valid_rg,
	#			0b0,
	#		)),
	#		bus.outp.bak.ready.eq(
	#			loc.s.ready_rg
	#			& (
	#				# Use a Python "mux" instead of an Amaranth `Mux`
	#				# AND the upstream `ready` with ~`ready_busy`
	#				0b1
	#				if not OPT_INCLUDE_READY_BUSY
	#				else ~bus.misc.ready_busy
	#			)
	#		),
	#		bus.outp.fwd.data.eq(loc.s.data_rg),
	#		bus.outp.fwd.valid.eq(
	#			loc.s.valid_rg
	#			& (
	#				# Use a Python "mux" instead of an Amaranth `Mux`
	#				# AND the downstream `valid` with ~`valid_busy`
	#				0b1
	#				if not OPT_INCLUDE_VALID_BUSY
	#				else ~bus.misc.valid_busy
	#			)
	#		),
	#	]

	#	#--------
	#	#--------
	#	return m
	#	#--------

	@staticmethod
	def connect_parallel(
		parent: Module,
		sb_bus_lst: list, # should be [Splitintf(PipeSkidBufIshape)]
		tie_first_inp_fwd_valid: bool=True,
		tie_last_inp_bak_ready: bool=True,
		*,
		lst_shrink=-1,
		other_lst_shrink=None,
	):
		assert len(sb_bus_lst) >= 2
		#print("testificate")

		for i in range(len(sb_bus_lst) - 1):
			sb_bus = sb_bus_lst[i]
			sb_bus_next = sb_bus_lst[i + 1]
			#sb_bus.connect(
			#	other=sb_bus_next,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel,
			#	#use_tag=True,
			#	#reduce_tag=False,
			#	lst_shrink=lst_shrink,
			#	other_lst_shrink=other_lst_shrink,
			#)
			sb_bus_next.inp.fwd.connect(
				other=sb_bus.outp.fwd,
				m=parent,
				kind=Splitintf.ConnKind.Parallel,
				lst_shrink=lst_shrink,
				other_lst_shrink=other_lst_shrink,
			)
			sb_bus.inp.bak.connect(
				other=sb_bus_next.outp.bak,
				m=parent,
				kind=Splitintf.ConnKind.Parallel,
				lst_shrink=lst_shrink,
				other_lst_shrink=other_lst_shrink,
			)

			#sb_bus_next.inp.fwd.connect(
			#	other=sb_bus.outp.fwd,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel,
			#	#f=f,
			#	use_tag=True,
			#	reduce_tag=False,
			#	#lst_shrink=-3,
			#	lst_shrink=lst_shrink,
			#	other_lst_shrink=other_lst_shrink,
			#)
			#sb_bus.inp.bak.connect(
			#	other=sb_bus_next.outp.bak,
			#	m=parent,
			#	kind=Splitintf.ConnKind.Parallel,
			#	#f=f,
			#	use_tag=True,
			#	reduce_tag=False,
			#	#lst_shrink=-3,
			#	lst_shrink=lst_shrink,
			#	other_lst_shrink=other_lst_shrink,
			#)
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
		#use_tag: bool=True,
		#reduce_tag: bool=True,
		*,
		lst_shrink=-1,
		other_lst_shrink=None,
	):
		"""
		This function is especially intended for when you have a
		`<YourModule>Bus` that has its own `PipeSkidBufBus` instance that
		you want to connect to the `PipeSkidBufBus` of an internal
		`PipeSkidBuf`.

		`parent_data` not being `None` provides the ability to do
		internal processing on the `PipeSkidBuf`'s fwd output.
		If you can't do all the needed processing on the `PipeSkidBuf`'s
		fwd output, then you'll need to assert
		`child_sb_bus.inp.valid_busy` until you're done with your
		processing.
		"""

		#parent.d.comb += [
		#	child_sb_bus.inp.fwd.eq(parent_sb_bus.inp.fwd),
		#	##parent_sb_bus.outp.fwd.eq(child_sb_bus.outp.fwd),
		#	#child_sb_bus.inp.eq(parent_sb_bus.inp),
		#	parent_sb_bus.outp.fwd.valid.eq(child_sb_bus.outp.fwd.valid),
		#	child_sb_bus.inp.bak.eq(parent_sb_bus.inp.bak),
		#	parent_sb_bus.outp.bak.eq(child_sb_bus.outp.bak),
		#]

		#parent_sb_bus.inp.fwd.connect(
		#	other=child_sb_bus.inp.fwd,
		#	m=parent,
		#	kind=Splitintf.ConnKind.Parent2Child,
		#	use_tag=use_tag,
		#	reduce_tag=reduce_tag,
		#)
		parent_sb_bus.inp.connect(
			other=child_sb_bus.inp,
			m=parent,
			kind=Splitintf.ConnKind.Parent2Child,
			#use_tag=use_tag,
			#reduce_tag=reduce_tag,
			lst_shrink=lst_shrink,
			other_lst_shrink=other_lst_shrink,
		)
		parent.d.comb += [
			#child_sb_bus.inp.fwd.valid.eq(parent_sb_bus.inp.fwd.valid),
			parent_sb_bus.outp.fwd.valid.eq(child_sb_bus.outp.fwd.valid),
		]
		#parent_sb_bus.inp.bak.connect(
		#	other=child_sb_bus.inp.bak,
		#	m=parent,
		#	kind=Splitintf.ConnKind.Parent2Child,
		#	use_tag=use_tag,
		#	reduce_tag=reduce_tag,
		#)
		parent_sb_bus.outp.bak.connect(
			other=child_sb_bus.outp.bak,
			m=parent,
			kind=Splitintf.ConnKind.Parent2Child,
			#use_tag=use_tag,
			#reduce_tag=reduce_tag,
			lst_shrink=lst_shrink,
			other_lst_shrink=other_lst_shrink,
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
			#use_tag=use_tag,
			#reduce_tag=reduce_tag,
			lst_shrink=lst_shrink,
			other_lst_shrink=other_lst_shrink,
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


