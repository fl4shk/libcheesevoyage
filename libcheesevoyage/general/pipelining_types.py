#!/usr/bin/env python3

import enum as pyenum
from collections import OrderedDict

from amaranth import *
from amaranth.lib.data import *
#from libcheesevoyage.misc_util import psconcat, mk_keep_obj
from libcheesevoyage.misc_util import psconcat, sig_keep

class PsDir(pyenum.Enum):
	Fwd = 0
	Bak = pyenum.auto()

	#EdgeToNext = pyenum.auto()
	#EdgeToPrev = pyenum.auto()

	#Start2Next = pyenum.auto()
	#Middle2Next = pyenum.auto()
#class PsKind(pyenum.Enum):
#	Begin = 0
#	Middle = pyenum.auto()
#	End = pyenum.auto()

#def mk_ps_pair_constants(
#	base_name: str, shape, ps_dir: PsDir
#) -> OrderedDict:
#	return {
#		"base": base,
#		"next": base + "_next",
#		"prev": base + "_prev",
#		"shape": shape,
#		"ps_dir": ps_dir,
#	}

class PsSigInfo:
	def __init__(
		self,
		#prefix: str,
		basenm: str, ps_dir: PsDir, shapelayt,
		*, ObjKind=Signal, reset=None, attrs=sig_keep(),
		prefix: str="",
	):
		self.__basenm: basenm
		self.__outpnm: basenm + (
			"_next" if ps_dir == PsDir.Fwd else "_prev"
		)
		self.__inpnm: basenm + + (
			"_prev" if ps_dir == PsDir.Fwd else "_next"
		)
		self.__regnm: basenm + "_reg"
		self.__ps_dir: ps_dir
		self.__shapelayt: shapelayt
		self.__ObjKind = ObjKind
		self.__reset = reset
		self.__attrs = attrs
		self.__prefix = prefix

	@property
	def prefix(self):
		return self.__prefix
	@prefix.setter
	def prefix(self, n_prefix: str):
		self.__prefix = n_prefix

	def basenm(self):
		return self.__basenm
	def inpnm(self):
		return self.__inpnm
	def outpnm(self):
		return self.__outpnm
	def regnm(self):
		return self.__regnm
	def sig_name_lst(self):
		return [
			self.outpnm(), self.inpnm(), self.regnm(),
		]

	def ps_dir(self):
		return self.__ps_dir
	def shapelayt(self):
		return self.__shapelayt
	def ObjKind(self):
		return self.__ObjKind 
	def reset(self):
		return self.__reset
	def attrs(self):
		return self.__attrs

class PsBundle:
	def __init__(
		self,

		##stb_info: PsSigInfo,
		##busy_info: PsSigInfo
		#info_dct: OrderedDict,
		#*,
		#stb_attrs: str=sig_keep(),
		#busy_attrs: str=sig_keep(),
		#prefix: str="",
		bundle_box
	):
		info_dct = bundle_box.info_dct()
		stb_attrs = bundle_box.stb_attrs()
		busy_attrs = bundle_box.busy_attrs()
		prefix = bundle_box.prefix()

		#self.__prefix = prefix
		assert "stb" not in info_dct, psconcat(
			"\"stb\" must not be in `info_dct`"
		)
		assert "busy" not in info_dct, psconcat(
			"\"busy\" must not be in `info_dct`"
		)

		info_dct["stb"] = PsSigInfo(
			basenm="stb",
			ps_dir=PsDir.Fwd,
			shapelayt=1,
			attrs=stb_attrs,
			prefix=prefix,
		)
		info_dct["busy"] = PsSigInfo(
			basenm="busy",
			ps_dir=PsDir.Bak,
			shapelayt=1,
			attrs=busy_attrs,
			prefix=prefix,
		)
		for sig_info in info_dct:
			for name in sig_info.sig_name_lst():
				self.__dict__[name] = sig_info.ObjKind()(
					sig_info.shapelayt(),
					name=prefix + name,
					reset=sig_info.reset(),
					attrs=sig_info.attrs(),
				)

class PsBundleBox:
	def __init__(
		self,
		info_dct: OrderedDict,
		*,
		stb_attrs: str=sig_keep(),
		busy_attrs: str=sig_keep(),
		prefix: str="",
	):
		self.__info_dct = info_dct,
		self.__stb_attrs = stb_attrs
		self.__busy_attrs = busy_attrs
		self.__prefix = prefix

		self.__bundle = PsBundle(bundle_box=self)

	def info_dct(self):
		return self.__info_dct
	def stb_attrs(self):
		return self.__stb_attrs
	def busy_attrs(self):
		return self.__busy_attrs
	def prefix(self):
		return self.__prefix
	def bundle(self):
		return self.__bundle
	def inp(self, basenm):
		return self.bundle().__dict__[self.info_dct()[basenm].inpnm()]
	def outp(self, basenm):
		return self.bundle().__dict__[self.info_dct()[basenm].outpnm()]
	def reg(self, basenm):
		return self.bundle().__dict__[self.info_dct()[basenm].regnm()]
		

#class SkidBufPstageBus:
#	def __init__(
#		self,
#		#m: Module,
#		#logic_func, # should take `m` as its first argument
#		info_dct: OrderedDict
#	):
#		self.__info_dct = info_dct
#		self.__strc = PsBundle(self.info_dct())
#
#	def info_dct(self):
#		return self.__info_dct
#	def bundle(self):
#		return self.__strc

# This probably could have been an `Elaboratable`, but doing things this
# way allows for not having another nesting level of `Module`s.
class SkidBufPstageGen:
	def __init__(
		self,
		parent: Module,
		#info_dct: OrderedDict,
		box: PsBundleBox,

		# `logic_func` takes this `SkidBufPstageGen` (`self`) as its
		# first argument.
		# The second argument is either
		# `self.box().outp` or `self.box().regp`,
		# so you can call that to select which signals to drive in this
		# pipeline stage.
		# "stb" and "busy" should definitely not be driven by
		# `logic_func()`, and there is probably no reason to read from
		# `stb` or `busy`, either, as that logic is taken care of in
		# `SkidBufPstageGen.gen()`
		logic_func,
	):
		self.__parent = parent
		#self.__bus = SkidBufPstageBus(info_dct)
		self.__box = box
		#self.__info_dct = bundle_box.info_dct()
		#self.__bundle = PsBundle(self.info_dct())
		#self.__bundle = bundle_box.bundle()
		self.__logic_func = logic_func

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
	def gen(self):
		# Translated logic from here:
		# https://zipcpu.com/blog/2017/08/14/strategies-for-pipelining.html
		m = self.parent()
		box = self.box()
		info_dct = self.info_dct()
		logic_func = self.logic_func()

		with m.If(ResetSignal()):
			pass
		# If the next stage is not busy
		# if (!i_busy)
		with m.Elif(~box.inp("busy")):
			# if (!r_stb)
			with m.If(~box.reg("stb")):
				# Nothing is in the buffer, so send the input directly
				# to the output.
				# o_stb <= i_stb;
				m.d.sync += box.outp("stb").eq(box.inp("stb"))

				# This `logic_func()` function is arbitrary, and
				# specific to what this stage is supposed to do.
				# o_data <= logic(i_data);
				logic_func(self, box.outp)
			# else
			with m.Else(): # If(box.reg("stb")):
				# `outp("busy")` is true and something is in our
				# buffer.
				# Flush the buffer to the output port.

				# o_stb <= 1'b1;
				m.d.sync += box.outp("stb").eq(0b1)

				# o_data <= r_data;
				m.d.sync += [
					box.outp(info_dct[key].basenm())
						.eq(box.reg(info_dct[key].basenm()))
					for key in info_dct.keys()
				]

				# We can ignore the input in this case, since we'll
				# only be here if `outp("busy")` is also true

			m.d.sync += [
				# We can also clear any stall condition.
				# o_busy <= 1'b0;
				box.outp("busy").eq(0b0),

				# And declare the register to be empty.
				# r_stb <= 1'b0;
				box.reg("stb").eq(0b0),
			]
		# Else, the next stage is busy
		# else if (!o_stb)
		with m.Elif(~box.outp("stb")):
			m.d.sync += [
				# o_stb <= i_stb;
				box.outp("stb").eq(box.inp("stb")),

				# o_busy <= 1'b0;
				box.outp("busy").eq(0b0),

				# Keep the buffer empty
				# r_stb <= 1'b0;
				box.reg("stb").eq(0b0),
			]
			# Apply the logic to the input data, and set the output
			# data
			# o_data <= logic(i_data);
			logic_func(self, box.outp)
		# i_busy and o_stb are both true
		# else if ((i_stb) && (!o_busy))
		with m.Elif(box.inp("stb") & ~box.outp("busy")):
			# If the next stage *is* busy, though, and we haven't stalled
			# yet, then we need to accept the requested value from the
			# input. We'll place it into a temporary location.
			m.d.sync += [
				# r_stb <= (i_stb) && (o_stb);
				box.reg("stb").eq(box.inp("stb") & box.outp("stb")),

				# o_busy <= (i_stb) && (o_stb);
				box.outp("busy").eq(box.inp("stb") & box.outp("stb")),
			]
		# if (!o_busy)
		with m.If(~ResetSignal() & ~box.outp("busy")):
			# r_data <= logic(i_data);
			logic_func(self, bus.reg)


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
