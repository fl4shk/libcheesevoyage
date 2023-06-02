#!/usr/bin/env python3

from enum import Enum, auto
from collections import OrderedDict

from amaranth import *
from amaranth.lib.data import *
import amaranth.tracer as tracer
from amaranth.hdl.ast import ValueCastable, Slice
#from amaranth.hdl.rec import Record, Layout
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.general_types import SigInfo
#--------
#def dbg_printerr(s, obj):
#	printerr(s, ": ", type(obj), " \"", obj, "\"\n")
#--------
#class ElemRef(ValueCastable):
#	#--------
#	def __init__(self, obj, shape, key):
#		if not isinstance(obj, ElemRef):
#			try:
#				Value.cast(obj)
#			except Exception:
#				raise TypeError("`obj` {!r} is not castable to `Value`"
#					.format(obj)) from None
#
#		if isinstance(shape, Packrec.Layout):
#			if not isinstance(key, str):
#				raise TypeError(("`key` {!r} has invalid type: "
#					+ "should be `str`")
#					.format(key))
#
#			if not key in shape.fields():
#				raise AttributeError("`key` {} is not a member"
#					.format(key))
#
#		self.__obj = obj
#		self.__shape = shape
#		self.__key = key
#	#--------
#	def obj(self):
#		return self.__obj
#	def shape(self):
#		return self.__shape
#	def key(self):
#		return self.__key
#
#	def fields(self):
#		return self.shape().fields()
#
#	def ElemKindT(self):
#		#return self.shape().ElemKindT()
#		return self.shape().ElemKindT() \
#			if not isinstance(self.shape(), int) \
#			else self.shape()
#	def ELEM_WIDTH(self):
#		return self.shape().ELEM_WIDTH() \
#			if not isinstance(self.shape(), int) \
#			else self.shape()
#	def SIZE(self):
#		return self.shape().SIZE()
#	def SIG_WIDTH(self):
#		return self.shape().SIG_WIDTH()
#	def SIGNED(self):
#		return self.shape().SIGNED()
#	#--------
#	def eq(self, other):
#		return self.as_value().eq(other)
#	def word_select(self, index, elem_width):
#		return self.as_value().word_select(index, elem_width)
#	#--------
#	def __add__(self, other):
#		return (self.as_value() + Value.cast(other))
#	def __radd__(self, other):
#		return (Value.cast(other) + self.as_value())
#	def __sub__(self, other):
#		return (self.as_value() - Value.cast(other))
#	def __rsub__(self, other):
#		return (Value.cast(other) - self.as_value())
#	def __pos__(self):
#		return self.as_value()
#	def __neg__(self):
#		return (-self.as_value())
#
#	def __mul__(self, other):
#		return (self.as_value() * Value.cast(other))
#	def __rmul__(self, other):
#		return (Value.cast(other) * self.as_value())
#	def __floordiv__(self, other):
#		return (self.as_value() // Value.cast(other))
#	def __rfloordiv__(self, other):
#		return (Value.cast(other) // self.as_value())
#	def __mod__(self, other):
#		return (self.as_value() % Value.cast(other))
#	def __rmod__(self, other):
#		return (Value.cast(other) % self.as_value())
#
#	def __lshift__(self, other):
#		return (self.as_value() << Value.cast(other))
#	def __rlshift__(self, other):
#		return (Value.cast(other) << self.as_value())
#	def __rshift__(self, other):
#		return (self.as_value() >> Value.cast(other))
#	def __rrshift__(self, other):
#		return (Value.cast(other) >> self.as_value())
#
#	def __and__(self, other):
#		return (self.as_value() & Value.cast(other))
#	def __rand__(self, other):
#		return (Value.cast(other) & self.as_value())
#	def __or__(self, other):
#		return (self.as_value() | Value.cast(other))
#	def __ror__(self, other):
#		return (Value.cast(other) | self.as_value())
#	def __xor__(self, other):
#		return (self.as_value() ^ Value.cast(other))
#	def __rxor__(self, other):
#		return (Value.cast(other) ^ self.as_value())
#	def __invert__(self):
#		return (~self.as_value())
#
#	def __eq__(self, other):
#		return (self.as_value() == Value.cast(other))
#	def __ne__(self, other):
#		return (self.as_value() != Value.cast(other))
#	def __lt__(self, other):
#		return (self.as_value() < Value.cast(other))
#	def __gt__(self, other):
#		return (self.as_value() > Value.cast(other))
#	def __le__(self, other):
#		return (self.as_value() <= Value.cast(other))
#	def __ge__(self, other):
#		return (self.as_value() >= Value.cast(other))
#	#--------
#	@ValueCastable.lowermethod
#	def as_value(self):
#		# I believe that `obj` will always be a `Signal` or `Slice`, though
#		# `self.obj()` could be a `Signal`, `Slice`, `ElemRef`, `Packrec`,
#		# or `Packarr`. That's at least how it's supposed to work.
#		#obj = Value.cast(self.obj())
#		if isinstance(self.obj(), ValueCastable):
#			#obj = Value.cast(self.obj())
#			obj = self.obj().as_value()
#		else:
#			obj = self.obj()
#
#		if isinstance(self.shape(), Packrec.Layout):
#			if isinstance(self.key(), str):
#				if self.key() not in self.fields():
#					raise AttributeError(("`self.shape()` `{!r}` does "
#						+ "not have a field `{!r}`. Did you mean one "
#						+ "of `{!r}`?")
#						.format(self.shape(), self.key(),
#							", ".join(self.fields())))
#
#				# This is the `shape` of the field
#				shape = self.fields()[self.key()]
#				start_stop_pair = self.__calc_packrec_start_stop_pair \
#					(shape, self.key())
#				return obj[start_stop_pair[0]:start_stop_pair[1]]
#			elif isinstance(self.key(), int) \
#				or isinstance(self.key(), slice):
#				return obj[self.key]
#			else:
#				raise TypeError(("`self.key()` `{!r}` is not one of "
#					+ "`str`, `int`, or `slice`").format(self.key()))
#		elif isinstance(self.shape(), Packarr.Shape):
#			if isinstance(self.key(), slice):
#				return obj[self.key()]
#			else:
#				try:
#					Value.cast(self.key())
#				except Exception:
#					raise TypeError(("`self.key()` `{!r}` is neither "
#						+ "a `slice` nor castable to `Value`")
#						.format(self.key())) from None
#
#				shape = self.ElemKindT()
#				return obj.word_select(self.key(),
#					shape
#						if isinstance(shape, int)
#						else len(shape))
#		elif isinstance(self.shape(), int):
#			if isinstance(self.key(), int) \
#				or isinstance(self.key(), slice):
#				return obj[self.key()]
#			else:
#				raise TypeError(("`self.key()` `{!r}` must be an "
#					+ "`int` or `slice`.").format(self.key()))
#		else:
#			raise TypeError(("`self.shape()` `{!r}` must be a "
#				+ "`Packrec.Layout`, `Packarr.Shape`, or `int`")
#				.format(self.shape()))
#
#	def __len__(self):
#		return len(self.as_value())
#	def __repr__(self):
#		return "ElemRef([{!r}, {!r}, {!r}])" \
#			.format(self.obj(), self.shape(), self.key())
#	def __iter__(self):
#		if isinstance(self.obj(), ValueCastable):
#			obj = self.obj().as_value()
#		else:
#			obj = self.obj()
#
#		if isinstance(self.shape(), Packrec.Layout):
#			if isinstance(self.key(), str):
#				if self.key() not in self.fields():
#					raise AttributeError(("`self.shape()` `{!r}` does "
#						+ "not have a field `{!r}`. Did you mean one "
#						+ "of `{!r}`?")
#						.format(self.shape(), self.key(),
#							", ".join(self.fields())))
#				for name, shape in self.shape():
#					yield (ElemRef(self, shape, name), shape)
#			elif isinstance(self.key(), slice):
#				for elem in obj[self.key()]:
#					yield elem
#			else:
#				raise TypeError(("`self.key()` `{!r}` is not one of "
#					+ "`str` or `slice`").format(self.key()))
#		elif isinstance(self.shape(), Packarr.Shape):
#			if isinstance(self.key(), slice):
#				for elem in obj[self.key()]:
#					yield elem
#			else:
#				try:
#					Value.cast(self.key())
#				except Exception:
#					raise TypeError(("`self.key()` `{!r}` is neither a "
#						+ "`slice` nor castable to `Value`")
#						.format(self.key()))
#				for i in range(len(self.shape())):
#					yield ElemRef(self, self.ElemKindT(), i)
#		elif isinstance(self.shape(), int):
#			if isinstance(self.key(), slice):
#				for elem in obj[self.key()]:
#					yield elem
#			else:
#				raise TypeError("`self.key()` `{!r}` must be a `slice`"
#					.format(self.key()))
#		else:
#			raise TypeError(("`self.shape()` `{!r}` must be a "
#				+ "`Packrec.Layout`, `Packarr.Shape`, or `int`")
#				.format(self.shape()))
#	def __getattr__(self, key):
#		if key[0] == "_":
#			return self.__dict__[key]
#		else: # if key[0] != "_":
#			return self[key]
#	def __getitem__(self, key):
#		if isinstance(self.shape(), Packrec.Layout):
#			if isinstance(self.key(), str):
#				if self.key() not in self.fields():
#					raise AttributeError(("`{!r}` does not have a field "
#						+ "`{!r}`. Did you mean one of `{}`?")
#						.format(self.shape(), self.key(),
#							", ".join(self.fields())))
#				shape = self.fields()[self.key()]
#			elif isinstance(self.key(), int):
#				return self.as_value()[key]
#			else:
#				raise TypeError(("For a `Packrec`, `self.key()` `{!r}` "
#					+ "must be a `str` or an `int`")
#					.format(self.key())) from None
#		else: # if isinstance(self.shape(), Packarr.Shape):
#			try:
#				Value.cast(self.key())
#			except Exception:
#				raise TypeError(("For a `Packarr`, `self.key()` `{!r}` "
#					+ "must be castable to `Value`").format(self.key())) \
#					from None
#			shape = self.ElemKindT()
#			#printout("shape: {!r}\n".format(shape))
#
#		temp_ret = ElemRef(self, shape, key)
#
#		# If `shape` represents a vector, we return a slice into `obj`
#		#if isinstance(shape, int) or (isinstance(shape, Packarr.Shape) \
#		#	and isinstance(shape.ElemKindT(), int)):
#		if isinstance(shape, Packarr.Shape)  \
#			and isinstance(shape.ElemKindT(), int):
#			return temp_ret.as_value()
#		else:
#			return temp_ret
#	#--------
#	def __calc_packrec_start_stop_pair(self, shape, key):
#		start = 0
#
#		for field_key in self.fields():
#			field = self.fields()[field_key]
#			if field_key != key:
#				if isinstance(field, int):
#					start += field
#				else: # if not isinstance(field, int)
#					start += len(field)
#			else: # if field_key == key:
#				break
#
#		return (start,
#			start + (shape
#				if isinstance(shape, int)
#				else len(shape)))
#	#--------
#--------
#class Packrec(ValueCastable):
#	#--------
#	class Layout:
#		#--------
#		@staticmethod
#		def cast(obj, SIGNED=False, *, src_loc_at=0):
#			return obj \
#				if isinstance(obj, Packrec.Layout) \
#				else Packrec.Layout(obj, src_loc_at=src_loc_at + 1)
#			#if isinstance(obj, Packrec.Layout):
#			#	return obj
#			#return Packrec.Layout(obj, SIGNED, src_loc_at=src_loc_at + 1)
#		#--------
#		def __init__(self, fields, SIGNED=False, *, src_loc_at=0):
#			self.__fields = OrderedDict()
#			self.__SIGNED = SIGNED
#			self.__SIG_WIDTH = 0
#
#			for field in list(reversed(fields)):
#				if (not isinstance(field, tuple)) or (len(field) != 2):
#					raise TypeError(("Field {!r} has invalid layout: "
#						+ "should be (name, shape)").format(field))
#
#				name, shape = field
#
#				# Check for an internal `Packrec.Layout`
#				if isinstance(shape, list):
#					shape = Packrec.Layout.cast(shape)
#
#				if not isinstance(name, str):
#					raise TypeError(("Field {!r} has invalid name: "
#						+ "should be a string")
#						.format(field))
#
#				if (not isinstance(shape, Packrec.Layout)) \
#					and (not isinstance(shape, Packarr.Shape)) \
#					and (not isinstance(shape, int)):
#					raise TypeError(("`shape` {!r} has invalid type: "
#						+ "should be a `Packrec.Layout`, "
#						+ "a `Packarr.Shape`, or an `int`")
#						.format(shape))
#
#					#try:
#					#	# Check provided shape by calling `Shape.cast()`
#					#	# and checking for exception
#					#	Shape.cast(shape, src_loc_at=src_loc_at + 1)
#					#except Exception:
#					#	raise TypeError(("Field {!r} has invalid shape: "
#					#		+ "should be castable to `Shape` "
#					#		+ "or a list of fields of a nested `Packrec`")
#					#		.format(field)) from None
#
#				if isinstance(shape, int) and (shape <= 0):
#					raise ValueError(("`int` `shape` {!r} has invalid "
#						+ "value: should be > 0")
#						.format(shape))
#
#				if name in self.fields():
#					raise NameError(("Field {!r} has a name that is "
#						+ "already present in the layout"
#						.format(field)))
#
#				self.__SIG_WIDTH += shape \
#					if isinstance(shape, int) \
#					else len(shape)
#
#				self.fields()[name] = shape
#		#--------
#		def fields(self):
#			return self.__fields
#		def SIGNED(self):
#			return self.__SIGNED
#		def SIG_WIDTH(self):
#			return self.__SIG_WIDTH
#		#--------
#		def __len__(self):
#			return self.SIG_WIDTH()
#		def __getitem__(self, key):
#			return self.fields()[key]
#		def __iter__(self):
#			for name, shape in self.fields().items():
#				yield (name, shape)
#		def __eq__(self, other):
#			return (isinstance(other, Packrec.Layout)
#				and (self.fields() == other.fields()))
#		def __repr__(self):
#			field_reprs = []
#			for name, shape in self:
#				field_reprs.append("({!r}, {!r})".format(name, shape))
#			return "Packrec.Layout([{}])".format(", ".join(field_reprs))
#		#--------
#	#--------
#	@staticmethod
#	def like(other, SIGNED=None, *, name=None, name_suffix=None,
#		src_loc_at=0, **kwargs):
#		if name is not None:
#			new_name = str(name)
#		elif name_suffix is not None:
#			new_name = other.name() + str(name_suffix)
#		else:
#			new_name = tracer.get_var_name(depth=src_loc_at + 2,
#				default=None)
#
#		#return Packrec(other.layout(), name=new_name, src_loc_at=1)
#		if SIGNED is not None:
#			fields = list(reversed(other.layout().fields().items()))
#			layout = Packrec.Layout(fields=fields, SIGNED=SIGNED,
#				src_loc_at=src_loc_at)
#		else:
#			layout = other.layout()
#
#		kw \
#			= dict \
#			(
#				layout=layout,
#				name=new_name,
#				src_loc_at=src_loc_at + 1
#			)
#		if isinstance(other, Packrec):
#			kw.update \
#			(
#				reset=other.extra_args_reset(),
#				reset_less=other.extra_args_reset_less(),
#				attrs=other.extra_args_attrs(),
#				decoder=other.extra_args_decoder(),
#			)
#		kw.update(kwargs)
#
#		return Packrec(**kw)
#	#--------
#	def __init__(self, layout, *, name=None, reset=0, reset_less=False,
#		attrs=None, decoder=None, src_loc_at=0):
#		if name is None:
#			new_name = tracer.get_var_name(depth=src_loc_at + 2,
#				default=None)
#		else:
#			new_name = name
#
#		self.__extra_args_name = new_name
#		self.__extra_args_reset = reset
#		self.__extra_args_reset_less = reset_less
#		self.__extra_args_attrs = attrs
#		self.__extra_args_decoder = decoder
#		self.__extra_args_src_loc_at = src_loc_at
#
#		self.__layout = Packrec.Layout.cast(layout,
#			src_loc_at=src_loc_at + 1)
#
#		sig_shape = unsigned(self.SIG_WIDTH()) \
#			if not self.SIGNED() \
#			else signed(self.SIG_WIDTH())
#
#		self.__sig \
#			= Signal \
#			(
#				shape=sig_shape,
#				name=self.extra_args_name(),
#				reset=self.extra_args_reset(),
#				reset_less=self.extra_args_reset_less(),
#				attrs=self.extra_args_attrs(),
#				decoder=self.extra_args_decoder(),
#				src_loc_at=self.extra_args_src_loc_at(),
#			)
#	#--------
#	def layout(self):
#		return self.__layout
#	def SIGNED(self):
#		return self.layout().SIGNED()
#	def SIG_WIDTH(self):
#		return self.layout().SIG_WIDTH()
#	def sig(self):
#		return self.__sig
#
#	def extra_args_name(self):
#		return self.__extra_args_name
#	def extra_args_reset(self):
#		return self.__extra_args_reset
#	def extra_args_reset_less(self):
#		return self.__extra_args_reset_less
#	def extra_args_attrs(self):
#		return self.__extra_args_attrs
#	def extra_args_decoder(self):
#		return self.__extra_args_decoder
#	def extra_args_src_loc_at(self):
#		return self.__extra_args_src_loc_at
#	#--------
#	#@staticmethod
#	#def to_sig(val):
#	#	return Packarr.to_sig(val)
#	#--------
#	def eq(self, other):
#		return self.sig().eq(Value.cast(other))
#	def word_select(self, index, elem_width):
#		return self.sig().word_select(index, elem_width)
#	#--------
#	def __add__(self, other):
#		return (self.as_value() + Value.cast(other))
#	def __radd__(self, other):
#		return (Value.cast(other) + self.as_value())
#	def __sub__(self, other):
#		return (self.as_value() - Value.cast(other))
#	def __rsub__(self, other):
#		return (Value.cast(other) - self.as_value())
#	def __pos__(self):
#		return self.as_value()
#	def __neg__(self):
#		return (-self.as_value())
#
#	def __mul__(self, other):
#		return (self.as_value() * Value.cast(other))
#	def __rmul__(self, other):
#		return (Value.cast(other) * self.as_value())
#	def __floordiv__(self, other):
#		return (self.as_value() // Value.cast(other))
#	def __rfloordiv__(self, other):
#		return (Value.cast(other) // self.as_value())
#	def __mod__(self, other):
#		return (self.as_value() % Value.cast(other))
#	def __rmod__(self, other):
#		return (Value.cast(other) % self.as_value())
#
#	def __lshift__(self, other):
#		return (self.as_value() << Value.cast(other))
#	def __rlshift__(self, other):
#		return (Value.cast(other) << self.as_value())
#	def __rshift__(self, other):
#		return (self.as_value() >> Value.cast(other))
#	def __rrshift__(self, other):
#		return (Value.cast(other) >> self.as_value())
#
#	def __and__(self, other):
#		return (self.as_value() & Value.cast(other))
#	def __rand__(self, other):
#		return (Value.cast(other) & self.as_value())
#	def __or__(self, other):
#		return (self.as_value() | Value.cast(other))
#	def __ror__(self, other):
#		return (Value.cast(other) | self.as_value())
#	def __xor__(self, other):
#		return (self.as_value() ^ Value.cast(other))
#	def __rxor__(self, other):
#		return (Value.cast(other) ^ self.as_value())
#	def __invert__(self):
#		return (~self.as_value())
#
#	def __eq__(self, other):
#		return (self.as_value() == Value.cast(other))
#	def __ne__(self, other):
#		return (self.as_value() != Value.cast(other))
#	def __lt__(self, other):
#		return (self.as_value() < Value.cast(other))
#	def __gt__(self, other):
#		return (self.as_value() > Value.cast(other))
#	def __le__(self, other):
#		return (self.as_value() <= Value.cast(other))
#	def __ge__(self, other):
#		return (self.as_value() >= Value.cast(other))
#	#--------
#	@ValueCastable.lowermethod
#	def as_value(self):
#		return self.sig()
#	def __bool__(self):
#		return bool(self.sig())
#	def __len__(self):
#		return len(self.as_value())
#	def __repr__(self):
#		ret = "Packrec([{!r}, {!r},".format(self.sig(),
#			self.layout().SIGNED())
#
#		for name, shape in self.__layout:
#			ret += "({!r}, {!r})".format(name, shape)
#
#		ret += "])"
#
#		return ret
#	def __iter__(self):
#		for name, shape in self.layout():
#			yield (self[name], shape)
#	def __getattr__(self, key):
#		if key[0] == "_":
#			return self.__dict__[key]
#		else: # if key[0] != "_":
#			return self[key]
#	def __getitem__(self, key):
#		if isinstance(key, str):
#			return ElemRef(self.sig(), self.layout(), key)
#		elif isinstance(key, slice):
#			return self.sig().__getitem__(key)
#		else:
#			try:
#				Value.cast(key)
#			except Exception:
#				raise TypeError(psconcat
#					("Need to be able to `key`, `{!r}`, to ".format(key),
#					"`Value`, or `Value` must be a `str` or a `slice`"))
#			return ElemRef(self.sig(), self.layout(), key).as_value()
#	#--------
#--------
#class Packarr(ValueCastable):
#	#--------
#	class Shape:
#		#--------
#		@staticmethod
#		def cast(obj, *, src_loc_at=0):
#			if isinstance(obj, Packarr.Shape):
#				return obj
#			elif isinstance(obj, dict):
#				return Packarr.Shape(obj["ElemKindT"], obj["SIZE"],
#					obj["SIGNED"], src_loc_at=src_loc_at + 1)
#			else:
#				return Packarr.Shape(obj[0], obj[1], obj[2],
#					src_loc_at=src_loc_at + 1)
#		#--------
#		def __init__(self, ElemKindT, SIZE, SIGNED=False, *, src_loc_at=1):
#			self.__ElemKindT = ElemKindT
#			self.__SIZE = SIZE
#			self.__SIGNED = SIGNED
#			self.__ELEM_WIDTH = self.ElemKindT() \
#				if isinstance(self.ElemKindT(), int) \
#				else self.ElemKindT().SIG_WIDTH()
#			self.__SIG_WIDTH = (self.ELEM_WIDTH() * self.SIZE())
#
#			if (not isinstance(ElemKindT, Packrec.Layout)) \
#				and (not isinstance(ElemKindT, Packarr.Shape)) \
#				and (not isinstance(ElemKindT, int)):
#				raise TypeError(("`ElemKindT` {!r} has invalid type: "
#					+ "should be a `Packrec.Layout`, a `Packarr.Shape`, "
#					+ "or an `int`")
#					.format(ElemKindT))
#		#--------
#		def ElemKindT(self):
#			return self.__ElemKindT
#		def ELEM_WIDTH(self):
#			return self.__ELEM_WIDTH
#		def SIZE(self):
#			return self.__SIZE
#		def SIG_WIDTH(self):
#			#return (self.ELEM_WIDTH() * self.SIZE())
#			return self.__SIG_WIDTH
#		def SIGNED(self):
#			return self.__SIGNED
#		#--------
#		def __len__(self):
#			#return self.SIG_WIDTH()
#			return self.SIZE()
#		def __eq__(self, other):
#			return (isinstance(other, Packarr.Shape)
#				and (self.ElemKindT() == other.ElemKindT())
#				and (self.SIZE() == other.SIZE())
#				and (self.SIGNED() == other.SIGNED()))
#		def __repr__(self):
#			return "Packarr.Shape({!r}, {!r}, {!r})" \
#				.format(self.ElemKindT(), self.SIZE(), self.SIGNED())
#		#--------
#	#--------
#	@staticmethod
#	def like(other, name=None, name_suffix=None, src_loc_at=0, **kwargs):
#		if name is not None:
#			new_name = str(name)
#		elif name_suffix is not None:
#			new_name = other.name() + str(name_suffix)
#		else:
#			new_name = tracer.get_var_name(depth=src_loc_at + 2, 
#				default=None)
#
#		kw \
#			= dict \
#			(
#				#ElemKindT=other.ElemKindT(),
#				#SIZE=len(other),
#				#SIGNED=other.sig().shape.SIGNED,
#				shape=other.shape(),
#				name=new_name,
#				src_loc_at=src_loc_at + 1
#			)
#		if isinstance(other, Packarr):
#			kw.update \
#			(
#				reset=other.extra_args_reset(),
#				reset_less=other.extra_args_reset_less(),
#				attrs=other.extra_args_attrs(),
#				decoder=other.extra_args_decoder(),
#			)
#		kw.update(kwargs)
#
#		return Packarr(**kw)
#	#--------
#	@staticmethod
#	def build(ElemKindT, SIZE, SIGNED=False, *, name=None, reset=0,
#		reset_less=False, attrs=None, decoder=None, src_loc_at=0):
#
#		if name is None:
#			new_name = tracer.get_var_name(depth=src_loc_at + 2,
#				default=None)
#		else:
#			new_name = name
#
#		return Packarr(Packarr.Shape(ElemKindT, SIZE, SIGNED),
#			name=new_name, reset=reset, reset_less=reset_less, attrs=attrs,
#			decoder=decoder, src_loc_at=src_loc_at)
#	#--------
#	def __init__(self, shape, *, name=None, reset=0, reset_less=False,
#		attrs=None, decoder=None, src_loc_at=0):
#
#		if name is None:
#			new_name = tracer.get_var_name(depth=src_loc_at + 2,
#				default=None)
#		else:
#			new_name = name
#		#self.__ElemKindT = ElemKindT
#		#self.__SIZE = SIZE
#
#		self.__shape = Packarr.Shape.cast(shape)
#
#		self.__extra_args_name = new_name
#		self.__extra_args_reset = reset
#		self.__extra_args_reset_less = reset_less
#		self.__extra_args_attrs = attrs
#		self.__extra_args_decoder = decoder
#		self.__extra_args_src_loc_at = src_loc_at
#
#		sig_shape = unsigned(self.SIG_WIDTH()) \
#			if not self.SIGNED() \
#			else SIGNED(self.SIG_WIDTH())
#
#		self.__sig \
#			= Signal \
#			(
#				shape=sig_shape,
#				name=self.extra_args_name(),
#				reset=self.extra_args_reset(),
#				reset_less=self.extra_args_reset_less(),
#				attrs=self.extra_args_attrs(),
#				decoder=self.extra_args_decoder(),
#				src_loc_at=self.extra_args_src_loc_at(),
#			)
#	#--------
#	def shape(self):
#		return self.__shape
#	def ElemKindT(self):
#		return self.shape().ElemKindT()
#	def ELEM_WIDTH(self):
#		return self.shape().ELEM_WIDTH()
#	def SIZE(self):
#		return self.shape().SIZE()
#	def SIG_WIDTH(self):
#		return self.shape().SIG_WIDTH()
#	def SIGNED(self):
#		return self.shape().SIGNED()
#	def sig(self):
#		return self.__sig
#
#	#def sig_shape(self):
#	#	return self.sig().shape()
#
#	def extra_args_name(self):
#		return self.__extra_args_name
#	def extra_args_reset(self):
#		return self.__extra_args_reset
#	def extra_args_reset_less(self):
#		return self.__extra_args_reset_less
#	def extra_args_attrs(self):
#		return self.__extra_args_attrs
#	def extra_args_decoder(self):
#		return self.__extra_args_decoder
#	def extra_args_src_loc_at(self):
#		return self.__extra_args_src_loc_at
#	#--------
#	#@staticmethod
#	#def to_sig(val):
#	#	if not (isinstance(val, Signal) or isinstance(val, ElemRef)
#	#		or isinstance(val, Packrec) or isinstance(val, Packarr)):
#	#		raise TypeError(("`val` {!r} is not one of the following: "
#	#			+ "`Signal`, `ElemRef`, `Packrec`, `Packarr`")
#	#			.format(val))
#
#	#	if isinstance(val, Signal):
#	#		return val
#	#	elif isinstance(val, ElemRef):
#	#		return val.obj()
#	#	else:
#	#		return val.sig()
#	#--------
#	def eq(self, other):
#		return self.sig().eq(Value.cast(other))
#	def word_select(self, index, elem_width):
#		return self.sig().word_select(index, elem_width)
#	#--------
#	def __add__(self, other):
#		return (self.as_value() + Value.cast(other))
#	def __radd__(self, other):
#		return (Value.cast(other) + self.as_value())
#	def __sub__(self, other):
#		return (self.as_value() - Value.cast(other))
#	def __rsub__(self, other):
#		return (Value.cast(other) - self.as_value())
#	def __pos__(self):
#		return self.as_value()
#	def __neg__(self):
#		return (-self.as_value())
#
#	def __mul__(self, other):
#		return (self.as_value() * Value.cast(other))
#	def __rmul__(self, other):
#		return (Value.cast(other) * self.as_value())
#	def __floordiv__(self, other):
#		return (self.as_value() // Value.cast(other))
#	def __rfloordiv__(self, other):
#		return (Value.cast(other) // self.as_value())
#	def __mod__(self, other):
#		return (self.as_value() % Value.cast(other))
#	def __rmod__(self, other):
#		return (Value.cast(other) % self.as_value())
#
#	def __lshift__(self, other):
#		return (self.as_value() << Value.cast(other))
#	def __rlshift__(self, other):
#		return (Value.cast(other) << self.as_value())
#	def __rshift__(self, other):
#		return (self.as_value() >> Value.cast(other))
#	def __rrshift__(self, other):
#		return (Value.cast(other) >> self.as_value())
#
#	def __and__(self, other):
#		return (self.as_value() & Value.cast(other))
#	def __rand__(self, other):
#		return (Value.cast(other) & self.as_value())
#	def __or__(self, other):
#		return (self.as_value() | Value.cast(other))
#	def __ror__(self, other):
#		return (Value.cast(other) | self.as_value())
#	def __xor__(self, other):
#		return (self.as_value() ^ Value.cast(other))
#	def __rxor__(self, other):
#		return (Value.cast(other) ^ self.as_value())
#	def __invert__(self):
#		return (~self.as_value())
#
#	def __eq__(self, other):
#		return (self.as_value() == Value.cast(other))
#	def __ne__(self, other):
#		return (self.as_value() != Value.cast(other))
#	def __lt__(self, other):
#		return (self.as_value() < Value.cast(other))
#	def __gt__(self, other):
#		return (self.as_value() > Value.cast(other))
#	def __le__(self, other):
#		return (self.as_value() <= Value.cast(other))
#	def __ge__(self, other):
#		return (self.as_value() >= Value.cast(other))
#	#--------
#	@ValueCastable.lowermethod
#	def as_value(self):
#		return self.sig()
#	def __bool__(self):
#		return bool(self.sig())
#	def __len__(self):
#		#return len(self.as_value())
#		#return len(self.sig())
#		#printout("testificate len(): ", self.SIZE(), "\n")
#		return self.SIZE()
#	def __repr__(self):
#		#return repr(self.sig())
#		return "Packarr({!r}, [{!r}, {!r}])".format(self.sig(),
#			self.ElemKindT(), self.SIZE())
#	def __iter__(self):
#		for i in range(len(self)):
#			yield self[i]
#	def __getitem__(self, key):
#		temp_ret = ElemRef(self.sig(), self.shape(), key)
#		if isinstance(self.shape().ElemKindT(), int):
#			#return temp_ret.as_value()
#			return Value.cast(temp_ret)
#			#printout("testificate: ", self, " ", key, "\n")
#			#return self.as_value().word_select(key, self.ELEM_WIDTH())
#			#return self.sig()
#		else:
#			#printout("testificate 2: ", self, " ", key, "\n")
#			return temp_ret
#			#try:
#			#	Value.cast(key)
#			#except Exception:
#			#	raise TypeError(psconcat
#			#		("Need to be able to `key`, `{!r}`, to ".format(key)
#			#		"`Value`"))
#	#--------
#--------
#class Field:
#	def __init__(
#		self,
#		#name: str,
#		shape,
#		sig=None,
#		name: str="",
#		**kwargs,
#	):
#		#self.__name = name
#		self.__shape = shape
#		self.__name = name
#		self.__sig = sig
#		self.__kwargs = kwargs
#
#	def shape(self):
#		return self.__shape
#	def kwargs(self):
#		return self.__kwargs
#
#	@property
#	def name(self):
#		return self.__name
#	@name.setter
#	def name(self, n_name: str):
#		self.__name = n_name

#def field_name(parent, name: str):
#def field_name(parent_name: str, name: str, use_parent_name)
def field_name(
	parent_name: str,
	name: str,
	use_parent_name=None
) -> str:
	return (
		psconcat(parent_name, "_", name)
		if (
			parent_name is not None
			and use_parent_name
		)
		else name
	)
#def field_name_have_parent(parent) -> bool:
#	return (
#		(
#			isinstance(parent, Splitrec)
#			or isinstance(parent, Splitarr)
#		) and (
#			parent.extra_args_name() is not None
#		)
#	)
#def field_name_calc_upn(
#	parent, 
#	use_parent_name,
#) -> bool:
#	return (
#		(
#			field_name_have_parent(parent)
#			and parent.extra_args_use_parent_name()
#			and use_parent_name is None 
#		) or use_parent_name
#		##and use_parent_name
#		#or (
#		#	use_parent_name
#		#)
#	)
def field_name_calc_upn(
	other,
	use_parent_name,
	in_like: bool,
) -> bool:
	return (
		(
			use_parent_name
			and (
				in_like
				or other.extra_args_use_parent_name() is None
			)
		) or (
			other.extra_args_use_parent_name()
		)
		#or (
		#	use_parent_name
		#) or (
		#	use_parent_name is None
		#	and other.extra_args_use_parent_name
		#)

	)
#def field_name_w_new_name(
#	parent,
#	new_name: str,
#	name: str,
#	use_parent_name,
#) -> str:
#	return (
#		field_name(
#			parent_name=new_name,
#			name=name,
#			use_parent_name=field_name_calc_upn(
#				parent=parent,
#				use_parent_name=use_parent_name,
#			)
#		) if field_name_have_parent(parent)
#		else name
#	)
#	#if field_name_have_parent(parent):
#	#	#temp_name = psconcat(new_name, "_", key)
#	#	#temp_name = field_name_w_parent
#	#	#return field_name_w_new_name(
#	#	#	parent=parent,
#	#	#	new_name=new_name,
#	#	#	name=key,
#	#	#	use_parent_name=use_parent_name,
#	#	#)
#	#	return (
#	#		field_name(
#	#			parent_name=new_name,
#	#			name=name,
#	#			use_parent_name=field_name_calc_upn(
#	#				parent=parent,
#	#				use_parent_name=use_parent_name,
#	#			)
#	#		) if field_name_have_parent(parent)
#	#		else name
#	#	)
#	#else:
#	#	#temp_name = psconcat(new_name, "_", key)
#	#	return field_name(
#	#		parent_name=new_name,
#	#		name=name,
#	#		use_parent_name=use_parent_name
#	#	)

#def field_name_w_parent(
#	parent,
#	name: str,
#	use_parent_name,
#) -> str:
#	return (
#		field_name(
#			parent_name=parent.extra_args_name(),
#			name=name,
#			use_parent_name=field_name_calc_upn(
#				parent=parent,
#				use_parent_name=use_parent_name,
#			)
#		) if field_name_have_parent(parent)
#		else name
#	)

class FieldInfo:
	def __init__(
		self,
		field,
		use_parent_name=None,
		*,
		name=None,
		#parent_name=None,
		reset=None,
		reset_less=None,
		attrs=None,
		decoder=None,
		#src_loc_at=0,
	):
		#if name is None:
		#	new_name = tracer.get_var_name(
		#		depth=src_loc_at + 2,
		#		default=None
		#	)
		#else:
		#	new_name = name

		self.__field = field
		#self.__extra_args_name = new_name
		self.__extra_args_name = name
		self.__extra_args_use_parent_name = use_parent_name
		#self.__extra_args_parent_name = parent_name
		#self.__extra_args_src_loc_at = src_loc_at

		self.__extra_args_reset = reset
		self.__extra_args_reset_less = reset_less
		self.__extra_args_attrs = attrs
		self.__extra_args_decoder = decoder
	def field(self):
		return self.__field
	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_use_parent_name(self):
		return self.__extra_args_use_parent_name
	#def extra_args_parent_name(self):
	#	return self.__extra_args_parent_name
	#def extra_args_src_loc_at(self):
	#	return self.__extra_args_src_loc_at
	def extra_args_reset(self):
		return self.__extra_args_reset
	def extra_args_reset_less(self):
		return self.__extra_args_reset_less
	def extra_args_attrs(self):
		return self.__extra_args_attrs
	def extra_args_decoder(self):
		return self.__extra_args_decoder

def do_print_flattened(obj, spaces=0):
	print(str().join([" " for i in range(spaces)]), end="")
	if isinstance(obj, Splitrec):
		print(obj.extra_args_name())
		for field in obj.fields().values():
			do_print_flattened(field, spaces=spaces + 1)
		#print()
	elif isinstance(obj, Splitarr):
		print(obj.extra_args_name())
		for i in range(len(list(obj))):
			do_print_flattened(obj[i], spaces=spaces + 1)
		#print()
	else:
		print(
			#obj.name,
			obj,
			(
				obj.shape()
				if isinstance(obj, Signal)
				#else "\"other shape\""
				else obj._View__layout
			)
		)
# A record type which is composed of separate signals.  This allows setting
# the attributes of every signal.
class Splitrec(ValueCastable):
	# Temporary class for until `Signal.like()` works with a `View`
	#class View:
	#	def __init__(self, shape, **kwargs):
	#		self.view = View(shape, **kwargs)
	#		self.shape = shape
	#--------
	def __init__(
		self,
		#shape: OrderedDict=OrderedDict(),
		shape: dict=dict(),
		*,
		name=None,
		#use_parent_name: bool=True,
		#use_parent_name: bool=False,
		#use_parent_name=None,
		use_parent_name=True,
		#parent_name=None,
		#parent=None,
		src_loc_at=0,
		in_like: bool=False,
	):
		#self.__dict__["__fields"] = fields
		#self.__dict__["_Splitrec__fields"] = fields
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name

		#if parent_name is None:
		#	new_parent_name = 

		#self.__extra_args_name = name
		self.__extra_args_name = new_name
		self.__extra_args_use_parent_name = use_parent_name
		#self.__extra_args_parent = parent
		#self.__extra_args_parent_name = parent_name
		self.__extra_args_src_loc_at = src_loc_at

		#self.__fields = fields
		#self.__fields = dict()
		#self.__fields = fields
		#self.__fields = OrderedDict()
		self.__fields = dict()
		for key, field_shape in shape.items():
			#if isinstance(field, View):
			#	raise TypeError(psconcat
			#		("Need `Splitrec.View` for `field` implementing, ",
			#		"`{!r}`")
			#		.format(field))
			#self.__setattr__(key, field)
			#self.__fields[key] = field
			self.__fields[key] = Splitrec.cast_shape(
				field_shape,
				name=key,
				#name=field_name(
				#	parent_name=new_name,
				#	name=key,
				#	use_parent_name=use_parent_name
				#)
				use_parent_name=use_parent_name,
				parent_name=new_name,
				#parent_name=parent_name,
				#parent=self,
				#reset=reset
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)
		self.__shape = shape
	@staticmethod
	#def cast_shape(ElemKindT, SIGNED=False, *, name=None, reset=0x0,
	#	reset_less=False, attrs=None, decoder=None, src_loc_at=0):
	def cast_shape(
		shape,
		*,
		name=None,
		#name_suffix=None,
		#use_parent_name: bool=False,
		use_parent_name=False,
		parent_name=None,
		#parent=None,

		reset=None,
		reset_less=None,
		attrs=None,
		decoder=None,

		#key=None,
		#target=None
		src_loc_at=0,
		in_like: bool=False,
	):
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name
		#if name is not None:
		#	new_name = str(name)
		#elif name_suffix is not None:
		#	new_name = other.extra_args_name() + str(name_suffix)
		#else:
		#	new_name = tracer.get_var_name(
		#		depth=src_loc_at + 2, 
		#		default=None
		#	)

		#print("Splitrec.cast_shape(): ", repr(shape), type(shape))
		#temp_name = field_name_w_parent(
		#)
		#temp_name = field_name(
		#	#parent_name=new_name,
		#	name=key,
		#	use_parent_name=(
		#		field_name_calc_upn(
		#			parent=parent,
		#			use_parent_name=use_parent_name,
		#		)
		#	)
		#)
		#temp_name = field_name_w_parent(
		#	parent=parent,
		#	name=new_name,
		#	use_parent_name=use_parent_name,
		#)
		temp_name = field_name(
			parent_name=parent_name,
			name=new_name,
			use_parent_name=use_parent_name,
		)

		if isinstance(shape, FieldInfo):
			assert shape != shape.field(), psconcat(
				"Can't have `shape` the same as ",
				"`shape.field()`: {!r}".format(shape.field()),
			)
			#temp_field_info_name = field_name(
			#	parent_name=parent_name,
			#	name=(
			#		shape.extra_args_name()
			#		if shape.extra_args_name() is not None
			#		else new_name
			#	),
			#	use_parent_name=use_parent_name,
			#)
			temp_field_info_name = (
				field_name(
					parent_name=parent_name,
					name=shape.extra_args_name(),
					#in_like=in_like,
					use_parent_name=field_name_calc_upn(
						other=shape,
						use_parent_name=use_parent_name,
						in_like=in_like
					)

				) if shape.extra_args_name() is not None
				else new_name
			)
			temp_use_parent_name = (
				shape.extra_args_name() is None
				and (
					#(
					#	use_parent_name
					#	and shape.extra_args_use_parent_name() is None
					#) or shape.extra_args_use_parent_name()
					field_name_calc_upn(
						other=shape,
						use_parent_name=use_parent_name,
						in_like=in_like
					)
				)
			)
			#print(psconcat(
			#	"testificate: ", temp_use_parent_name,
			#))
			return Splitrec.cast_shape(
				shape.field(),
				#name=new_name,
				name=temp_field_info_name,
				use_parent_name=temp_use_parent_name,
				parent_name=parent_name,
				reset=shape.extra_args_reset(),
				reset_less=shape.extra_args_reset_less(),
				attrs=shape.extra_args_attrs(),
				decoder=shape.extra_args_decoder(),
				#src_loc_at=0,
				#src_loc_at=src_loc_at, # idk if this is correct...
				src_loc_at=src_loc_at + 1, # this might be correct?
				in_like=in_like,
			)
		elif isinstance(shape, SigInfo):
			if (
				issubclass(shape.ObjKind(), Splitrec)
				or issubclass(shape.ObjKind(), Splitarr)
			):
				return shape.mk_sig(
					#basenm=new_name,
					basenm=temp_name,
					#prefix="",
					#suffix="",
					use_parent_name=use_parent_name,
					parent_name=parent_name,
					#parent=parent,
					src_loc_at=src_loc_at + 1,
					in_like=in_like,
				)
			else:
				return shape.mk_sig(
					#basenm=new_name,
					basenm=temp_name,
					src_loc_at=src_loc_at + 1,
				)
		elif (
			isinstance(shape, int)
			or isinstance(shape, Shape)
		):
			#shape = unsigned(shape) \
			#	if not SIGNED \
			#	else SIGNED(shape)
			#print("testificate")
			return Signal(
				shape=shape,
				#name=new_name,
				name=temp_name,
				reset=reset,
				reset_less=reset_less,
				attrs=attrs,
				decoder=decoder,
				src_loc_at=src_loc_at + 1
			)
		elif (
			isinstance(shape, OrderedDict)
			or isinstance(shape, dict)
		):
			#temp = Splitrec(fields=shape, name=new_name,
			#	src_loc_at=src_loc_at + 1)
			#return Splitrec.like(other=temp, name=None,
			#	name_suffix=None, src_loc_at=src_loc_at + 1)

			#print("testificate 2")
			#fields = OrderedDict()

			#print(
			#	"testificate 4: ",
			#	new_name,
			#	#field_name(ret, "testificate")
			#)

			#print(psconcat(
			#	"Splitrec.cast_shape(): making Splitrec: ",
			#	repr(new_name), " ",
			#	repr(temp_name), " ",
			#	repr(use_parent_name),
			#))

			ret = Splitrec(
				#fields=OrderedDict(),
				#fields=shape,
				shape=shape,
				#name=new_name,
				name=temp_name,
				#name_suffix=name_suffix,
				use_parent_name=use_parent_name,
				#use_parent_name=(
				#	False
				#	if (
				#		not isinstance(parent, Splitrec)
				#		and not isinstance(parent, Splitarr)
				#	)
				#	else parent.extra_args_use_parent_name(),
				#),
				#parent_name=parent_name,
				#parent=parent,
				#src_loc_at=src_loc_at + 2
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)
			#print(type(ret))
			#print(
			#	"testificate: ",
			#	new_name,
			#	field_name(ret, "testificate")
			#)
			#print(
			#	"testificate 3: ",
			#	new_name,
			#	field_name(ret, "testificate")
			#)

			#for key, field in shape.items():
			#	#fields[key]
			#	ret.__fields[key] = Splitrec.cast_shape(
			#		field,
			#		#name=psconcat(new_name, "_", key),
			#		#name=psconcat(new_name, key),
			#		#name=field_name(ret, key, use_parent_name),
			#		name=field_name(ret, key),
			#		#name=key,
			#		#use_parent_name=use_parent_name,
			#		parent=ret,
			#		#name=key,
			#		#reset=reset,
			#		#reset_less=reset_less,
			#		attrs=attrs, decoder=decoder,
			#		#src_loc_at=src_loc_at + 2,
			#		src_loc_at=ret.extra_args_src_loc_at(),
			#		#src_loc_at=ret.extra_args_src_loc_at() + 1,
			#		#target=target
			#	)
			#print(fields)
			#print("testificate 5: ", ret.fields())
			return ret

			#return Splitrec(
			#	#fields=fields,
			#	fields=shape,
			#	name=new_name, reset=reset,
			#	reset_less=reset_less,
			#	attrs=attrs, decoder=decoder,
			#	src_loc_at=src_loc_at + 1,
			#	in_like=in_like,
			#)
		elif (
			isinstance(shape, list)
			and (
				len(shape) == 0
				or (
					len(shape) > 0
					and not isinstance(shape[0], tuple)
				)
			)
		):
			#print(psconcat(
			#	"Splitrec.cast_shape(): making Splitarr: ",
			#	new_name, " ",
			#	use_parent_name,
			#))
			#print(psconcat(
			#	"Splitrec.cast_shape(): making Splitarr: ",
			#	repr(new_name), " ",
			#	repr(temp_name), " ",
			#	repr(use_parent_name),
			#))
			#temp = Splitarr(lst=shape, name=new_name,
			#	src_loc_at=src_loc_at + 1)
			#return Splitarr.like(other=temp, name=None,
			#	name_suffix=None, src_loc_at=src_loc_at + 1)
			return Splitarr(
				#lst=shape,
				shape=shape,
				#name=new_name,
				name=temp_name,
				#name_suffix=name_suffix,
				#use_parent_name=use_parent_name,
				use_parent_name=use_parent_name,
				#use_parent_name=(
				#	False
				#	if (
				#		not isinstance(parent, Splitrec)
				#		and not isinstance(parent, Splitarr)
				#	)
				#	else parent.extra_args_use_parent_name(),
				#),
				#parent=parent,
				#parent_name=parent_name,
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)

		#elif isinstance(shape, Packrec.Layout) \
		#	or (isinstance(shape, list) and (len(shape) > 0) \
		#		and isinstance(shape[0], tuple)):
		#	return Packrec(shape, name=new_name, reset=reset,
		#		reset_less=reset_less, attrs=attrs, decoder=decoder,
		#		src_loc_at=src_loc_at + 1)
		#elif isinstance(shape, Packarr.Shape):
		#	return Packarr(shape, name=new_name, reset=reset,
		#		reset_less=reset_less, attrs=attrs, decoder=decoder,
		#		src_loc_at=src_loc_at + 1)
		elif (
			isinstance(shape, ArrayLayout)
			or isinstance(shape, StructLayout)
			or isinstance(shape, UnionLayout)
		):
			#return Splitrec.View(
			#	view=View(shape, target=target,
			#		name=new_name, reset=reset,
			#		reset_less=reset_less, attrs=attrs, decoder=decoder,
			#		src_loc_at=src_loc_at + 1),
			#	shape=shape
			#)
			return View(
				shape, #target=target,
				#name=new_name,
				name=temp_name,
				reset=reset,
				reset_less=reset_less, attrs=attrs, decoder=decoder,
				src_loc_at=src_loc_at + 1
			)
		#elif isinstance(shape, UnionLayout):
		#	return View(shape, name=new_name, reset=reset,
		#		reset_less=reset_less, attrs=attrs, decoder=decoder,
		#		src_loc_at=src_loc_at + 1)
		else:
			#raise TypeError(psconcat
			#	("Need one of the following types for `shape`, {!r}, "
			#		.format(shape),
			#	": `int`, `Packrec.Layout`, `list`, or `Packarr.Shape`"))
			raise TypeError(psconcat
				("Invaild type for `shape`, `{!r}`".format(shape)))
			#return None
	#--------
	@staticmethod
	def like(
		other,
		name=None,
		#name_suffix=None,
		use_parent_name=None,
		#parent_name=None,
		#parent=None,
		src_loc_at=0,
		**kwargs
	):
		# `Splitrec.like()`
		##if name is not None:
		##	new_name = str(name)
		##elif name_suffix is not None:
		##	new_name = other.extra_args_name() + str(name_suffix)
		##else:
		##	new_name = tracer.get_var_name(
		##		depth=src_loc_at + 2, 
		##		default=None
		##	)
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name

		#new_name = field_name_w_parent(
		#	parent=parent,
		#	name=new_name,
		#	use_parent_name=use_parent_name,
		#)
		#new_name = (
		#	psconcat(parent.extra_args_name(), "_", new_name)
		#	if (
		#		field_name_have_parent(parent)
		#		and field_name_calc_upn(
		#			parent=parent,
		#			use_parent_name=use_parent_name,
		#			in_like=in_like,
		#		)
		#	)
		#	else new_name
		#)
		#print(psconcat("Splitrec.like(): ", new_name))

		#fields = OrderedDict()

		#for key, field in other.fields().items():
		#	try:
		#		Value.cast(field)
		#	except Exception:
		#		raise TypeError(("`field` `{!r}` must be castable to "
		#			+ "`Value`").format(field)) from None

		#	temp_src_loc_at = src_loc_at + 1

		#	like_func = (
		#		type(field).like
		#		if not isinstance(field, View)
		#		else Signal.like
		#	)
		#	if (
		#		isinstance(field, Splitrec)
		#		or isinstance(field, Splitarr)
		#	):
		#		fields[key] = like_func(
		#			field,
		#			#name=temp_name,
		#			name=new_name,
		#			use_parent_name=use_parent_name,
		#			#parent=parent,
		#			src_loc_at=temp_src_loc_at
		#		)
		#	else:
		#		#temp_name = field_name(
		#		#)

		#		#temp_name = field_name_w_parent(
		#		#	parent=parent,
		#		#	name=temp_name,
		#		#	use_parent_name=use_parent_name,
		#		#)

		#		fields[key] = like_func(
		#			field,
		#			name=new_name,
		#			#name=temp_name,
		#			src_loc_at=temp_src_loc_at
		#		)

		kw = {
			#"fields": fields,
			"shape": other.shape(),
			"name": new_name,
			#"name": name,
			#"name_suffix": name_suffix,
			#"use_parent_name": use_parent_name,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
			#"use_parent_name":(
			#	False
			#	if (
			#		not isinstance(parent, Splitrec)
			#		and not isinstance(parent, Splitarr)
			#	)
			#	else parent.extra_args_use_parent_name(),
			#),
			#"parent": parent,
			"src_loc_at": src_loc_at,
			"in_like": True
		}

		kw.update(kwargs)

		return Splitrec(**kw)
	
	#--------
	def fields(self):
		return self.__fields
		##ret = OrderedDict()
		#ret = dict()
		##for name in self.__dict__:
		#for name in self.__fields:
		#	if name[0] != "_":
		#		#ret[name] = self.__dict__[name]
		#		#ret[name] = self.__fields[name]
		#		ret[name] = self.__fields[name]
		#return ret
	def shape(self):
		return self.__shape

	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_use_parent_name(self):
		return self.__extra_args_use_parent_name
	#def extra_args_parent(self):
	#	return self.__extra_args_parent
	#def extra_args_parent_name(self):
	#	return self.__extra_args_parent_name
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#def field_name(self, name: str):
	#	return (
	#		name
	#		if (
	#			self.extra_args_name() is None
	#			or not self.extra_args_use_parent_name()
	#		)
	#		else psconcat(self.extra_args_name(), "_", name)
	#	)
	#--------
	#def __getitem__(self, key):
	#	return self.__fields[key]
	#def __getattr__(self, key):
	#	#ret = self.__dict__[key]
	#	ret = self.__fields[key]
	#	#ret = self[key]
	#	if isinstance(ret, Splitrec.View):
	#		print("testificate")
	#		return ret.view
	#	else:
	#		return ret
	def __getattribute__(self, key):
		##ret = object.__getattribute__(self, key)
		#ret = super().__getattribute__(key)
		##if isinstance(ret, Splitrec.View):
		##	return ret.view
		##else:
		##	return ret
		#return ret
		#print(psconcat(
		#	"Splitrec.__getattribute__(): ",
		#	key
		#))
		if (
			key[0] == "_"
			or key in Splitrec.__dict__
		):
			return super().__getattribute__(key)
		else:
			return self.__fields[key]
	def __setattr__(self, key, val):
		#if isinstance(val, View):
		#	raise TypeError(psconcat
		#		("Need `Splitrec.View` containing the layout when ",
		#		"`val` (`{!r}`), is a `View`")
		#		.format(val))

		#if key not in self.__fields:
		#	self.__fields[key] = val
		#object.__setattr__(self, key, val)

		if (
			key[0] == "_"
			or key in Splitrec.__dict__
		):
			#print(psconcat(
			#	"Splitrec.__setattr__(): not __fields: ", key, " ", val
			#))
			super().__setattr__(key, val)
			#return
		else:
			raise AttributeError(
				psconcat(
					"`Splitrec.__setattr__()` ",
					"cannot be used to add members to a `Splitrec`. ",
					"(key, val)=({!r}, {!r})".format(key, val),
					"NOTE: This may be an indication of old code in ",
					"libcheesevoyage that has not been updated for the ",
					"new (and now only) way to use `Splitrec`, ",
					"where it is intended that the user of `Splitrec` ",
					"add in members only with the `fields` argument to ",
					"`Splitrec.__init__()`. "
				)
			)
			##print(psconcat(
			##	"Splitrec.__setattr__(): `try`: ",
			##	val, " ",
			##	field_name(self, key), " ",
			##	self.extra_args_use_parent_name(),
			##))
			#parent=self.extra_args_parent()
			#use_parent_name = self.extra_args_use_parent_name()
			##temp_name = field_name_w_parent(
			##)

			##temp_name = field_name(
			##	self.extra_args_name(),
			##	key,
			##	use_parent_name
			##)

			##print(psconcat(
			##	"Splitrec.__setattr__(): begin: ",
			##	val, " ",
			##	temp_name, " ",
			##	use_parent_name
			##))
			##new_name = field_name_w_parent(
			##	parent=parent,
			##	name=self.extra_args_name(),
			##	#use_parent_name=self.extra_args_use_parent_name(),
			##	use_parent_name=use_parent_name,
			##)
			##temp_name = key
			##temp_name = field_name_w_new_name(
			##	parent=parent,
			##	new_name=new_name,
			##	name=key,
			##	use_parent_name=use_parent_name,
			##)
			##temp_name = field_name_w_parent(
			##	parent=parent,
			##	name=self.extra_args_name(),
			##	#use_parent_name=self.extra_args_use_parent_name(),
			##	use_parent_name=use_parent_name,
			##)
			##temp_name = field_name_w_new_name(
			##	parent=parent,
			##	new_name=new_name,
			##	name=key,
			##	use_parent_name=use_parent_name,
			##)

			##try:
			#	#super().__setattr__
			##use_parent_name

			##if (
			##	#key == "c"
			##	#temp_name[0] == "c"
			##	#and
			##	not isinstance(val, Signal)
			##	and not isinstance(val, View)
			##	and not isinstance(val, Splitrec)
			##	and not isinstance(val, Splitarr)
			##):
			##	print(psconcat(
			##		"testificate: ",
			##		#"new_name=", new_name, " ",
			##		"temp_name=", temp_name,
			##	))

			##if isinstance(val, Splitrec):
			##	print(psconcat(
			##		"Splitrec.__setattr__(): val Splitrec: ",
			##		"name=", val.extra_args_name(), " ",
			##		#"key=", key, " ",
			##		#"flattened=", val.flattened,
			##		"fields=", val.fields(),
			##	))
			##elif isinstance(val, Splitarr):
			##	print(psconcat(
			##		"Splitrec.__setattr__(): val Splitarr: ",
			##		"name=", val.extra_args_name(), " ",
			##		#"key=", key, " ",
			##		#"flattened=", val.flattened,
			##		"lst=", val.lst(),
			##	))
			##elif (
			##	isinstance(val, OrderedDict)
			##	or isinstance(val, dict)
			##):
			##	print(psconcat(
			##		"Splitrec.__setattr__(): val dict: ",
			##		"key=", key, " ",
			##		"val=", dict(val),
			##	))
			##elif isinstance(val, list):
			##	print(psconcat(
			##		"Splitrec.__setattr__(): val list: ",
			##		"key=", key, " ",
			##		"val=", val,
			##	))

			#temp_val = Splitrec.cast_shape(
			#	val,
			#	#name=field_name(self, key),
			#	name=key,
			#	#name=temp_name,
			#	#name=self.extra_args_name(),
			#	#use_parent_name=self.extra_args_use_parent_name(),
			#	use_parent_name=use_parent_name,
			#	parent=self,
			#)
			#did_cast = True
			##except Exception as exc:
			#if temp_val is None:
			#	#print(str(exc))
			#	#super().__setattr__(key, val)
			#	#self.__fields[key] = val
			#	temp_val = val
			#	did_cast = False
			##print(psconcat(
			##	"Splitrec.__setattr__(): __fields: ",
			##	key, " ", val, " ", temp_val, " ", did_cast
			##))
			#self.__fields[key] = temp_val

	#def __getattr__(self, key):
	#	return self[key]
	#def __getitem__(self, key):
	#	if key[0] == "_":
	#		return self.__dict__[temp_key]
	#	else: # if key[0] != "_":
	#		val = self.fields()[key]
	#		#printout("Splitrec.__getitem__(): ",
	#		#	key, " ", val, " ", Value.cast(val).name, "\n")
	#		return val

	#def __setattr__(self, key, val):
	#	self[key] = val
	#def __setitem__(self, key, val):
	#	if key[0] == "_":
	#		self.__dict__[key] = val
	#	else: # if key[0] != "_":
	#		self.__check_val_type("Splitrec.__setitem___()", val)
	#		#printout("Splitrec.__setitem__(): ",
	#		#	key, " ", val, " ", Value.cast(val).name, "\n")
	#		self.fields()[key] = val
	#--------
	def eq(self, other):
		#printout("Splitrec.eq(): ", type(other), ", ", other, "\n")
		try:
			Value.cast(other)
		except Exception:
			raise TypeError(
				psconcat
				("Need to be able to cast `other`, {!r}, ".format(other),
				"to `Value")
			)
		return self.as_value().eq(Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		#printout("Splitrec.as_value(): ", self.flattened(), "\n")
		return Cat(*self.flattened())
		#return Cat(self.flattened())
	def __len__(self):
		return len(self.as_value())
	def __iter__(self):
		#for name in self.__dict__:
		#	if name[0] != "_":
		#		yield (name, self.__dict__[name])
		#		#yield self.__dict__[name]
		for name in self.__fields:
			yield (name, self.__fields[name])
	#def __repr__(self):
	#--------
	@staticmethod
	def check_val_type(prefix_str, val):
		#if (not isinstance(val, Signal)) \
		#	and (not isinstance(val, Packrec)) \
		#	and (not isinstance(val, Packarr)) \
		#	and (not isinstance(val, Splitarr)) \
		#	and (not isinstance(val, Splitrec)):
		#	raise TypeError(psconcat(prefix_str, " ",
		#		"Need a `Signal`, `Packrec`, `Packarr`, `Splitarr`, or "
		#		"`Splitrec` for `val` {!r}".format(val)))

		try:
			Value.cast(val)
		except Exception:
			raise TypeError(psconcat(prefix_str, " `val` `{!r}` must be ",
				"possible to cast to `Value`").format(val)) from None

	def flattened(self):
		ret = []
		for val in self.fields().values():
			#Splitrec.check_val_type("Splitrec.flattened()", val)

			#if isinstance(val, Signal) or isinstance(val, Packrec) \
			#	or isinstance(val, Packarr):
			#	ret.append(val)
			#else: # if isinstance(val, Splitarr) \
			#	# or isinstance(val, Splitrec):
			#	ret.append(val.flattened())
			if (
				isinstance(val, Splitrec)
				or isinstance(val, Splitarr)
			):
				#ret.append(*val.flattened())
				#ret.append(val.as_value())
				#ret += val.flattened()
				temp = val.flattened()
				for elem in temp:
					ret.append(elem)
				#print(val)
				#ret += val.flattened()
			else:
				ret.append(val)
		#printout("\n")
		return ret
	#def cat(self):
	#	return eval(psconcat("Cat(" + ",".join(self.flattened()) + ")"))
	#--------
#--------
class Splitarr(ValueCastable):
	#--------
	def __init__(
		self,
		#lst: list=[],
		shape: list=[],
		*,
		name=None,
		#name_suffix=None,
		#use_parent_name=None,
		use_parent_name=True,
		#parent_name=None,
		#parent=None,
		#parent_name=None,
		#reset=0x0, reset_less=False, attrs=None, decoder=None,
		src_loc_at=0,
		in_like: bool=False,
	):
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name
		#if name is not None:
		#	new_name = str(name)
		#elif name_suffix is not None:
		#	new_name = other.extra_args_name() + str(name_suffix)
		#else:
		#	new_name = tracer.get_var_name(
		#		depth=src_loc_at + 2, 
		#		default=None
		#	)


		self.__extra_args_name = new_name
		self.__extra_args_use_parent_name = use_parent_name
		#self.__extra_args_parent = parent
		#self.__extra_args_parent_name = parent_name
		self.__extra_args_src_loc_at = src_loc_at

		#self.__lst = lst
		self.__lst = []
		#for elem in lst:
		for i in range(len(shape)):
			#elem = lst[i]
			elem = shape[i]
			#temp_name = psconcat(self.extra_args_name(), "_", i)
			#temp_name = psconcat(new_name, "_", i)
			#print(psconcat(
			#	"Splitarr.__init__(): `try`: ",
			#	elem, " ",
			#	#psconcat(new_name, "_", i), " ",
			#	temp_name, " ",
			#	self.extra_args_use_parent_name(),
			#))
			#try:
			temp_elem = Splitrec.cast_shape(
				elem,
				#name=psconcat(self.extra_args_name(), "_", i),
				#name=temp_name,
				name=psconcat(i),
				use_parent_name=use_parent_name,
				parent_name=new_name,
				#parent_name=parent_name,
				#parent=parent,
				#parent=self,
				#reset=reset,
				#reset_less=reset_less,
				#attrs=attrs,
				#decoder=decoder,
				src_loc_at=self.extra_args_src_loc_at(),
				#src_loc_at=self.extra_args_src_loc_at() + 1,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				in_like=in_like,
			)
			#did_cast = True
			##except Exception as exc:
			#if temp_elem is None:
			#	#print(str(exc))
			#	temp_elem = elem
			#	did_cast = False

			#print(psconcat(
			#	"Splitarr.__init__(): past `try`: ",
			#	elem, " ",
			#	temp_elem, " ",
			#	did_cast,
			#))
			self.__lst.append(temp_elem)
		self.__shape = shape
	#--------
	@staticmethod
	def like(
		other,
		name=None,
		#name_suffix=None,
		use_parent_name=None,
		#use_parent_name=True,
		#parent_name=None,
		#parent=None,
		src_loc_at=0,
		**kwargs
	):
		# `Splitarr.like()`
		if name is None:
			new_name = tracer.get_var_name(depth=src_loc_at + 2,
				default=None)
		else:
			new_name = name
		##if name is not None:
		##	new_name = str(name)
		##elif name_suffix is not None:
		##	new_name = other.extra_args_name() + str(name_suffix)
		##else:
		##	new_name = tracer.get_var_name(depth=src_loc_at + 2, 
		##		default=None)

		#new_name = field_name_w_parent(
		#	parent=parent,
		#	name=new_name,
		#	use_parent_name=use_parent_name,
		#)
		#print(psconcat("Splitarr.like(): ", new_name))

		#lst = []

		#for i in range(len(list(other))):
		#	elem = other[i]

		#	try:
		#		Value.cast(elem)
		#	except Exception:
		#		raise TypeError(("`elem` `{!r}` must be castable to "
		#			+ "`Value`").format(field)) from None

		#	# The naming is a heuristic
		#	#lst.append(type(elem).like(elem,
		#	#	name=psconcat(new_name, "_", i),
		#	#	src_loc_at=src_loc_at + 1))
		#	temp_src_loc_at = src_loc_at + 1
		#	#temp_name = field_name(new_name, i)

		#	# this is different from `Splitrec.like()`

		#	temp_name = psconcat(new_name, "_", i)

		#	#temp_name = field_name(
		#	#	parent_name=new_name,
		#	#	name=i,
		#	#	use_parent_name=(
		#	#		field_name_calc_upn(
		#	#			parent=parent,
		#	#			use_parent_name=use_parent_name,
		#	#			in_like=in_like,
		#	#		)
		#	#	)
		#	#)

		#	#temp_name = field_name_w_new_name(
		#	#	parent=parent,
		#	#	new_name=new_name,
		#	#	name=i,
		#	#	use_parent_name=use_parent_name,
		#	#)

		#	#temp_name = field_name_w_new_name(
		#	#	parent=parent,
		#	#	new_name=new_name,
		#	#	name=key,
		#	#	use_parent_name=use_parent_name,
		#	#)

		#	#temp_name = new_name
		#	#if (
		#	#	use_parent_name
		#	#	and (
		#	#		isinstance(parent, Splitrec)
		#	#		or isinstance(parent, Splitarr)
		#	#	)
		#	#):
		#	#	#temp_name = psconcat(
		#	#	#	other.extra_args_name(), "_", temp_name
		#	#	#)
		#	#	temp_name = field_name(other, temp_name)
		#	#temp_name = field_name(other, temp_name, use_parent_name)
		#	#temp_name = field_name(other, temp_name)
		#	#temp_name = field_name(parent, temp_name, use_parent_name)
		#	#temp_name = field_name(parent, temp_name)
		#	like_func = (
		#		type(elem).like
		#		if not isinstance(elem, View)
		#		else Signal.like
		#	)
		#	#if (
		#	#	like_func == Splitrec.like
		#	#	or like_func == Splitarr.like
		#	#):
		#	if (
		#		isinstance(elem, Splitrec)
		#		or isinstance(elem, Splitarr)
		#	):
		#		lst.append(
		#			like_func(
		#				elem,
		#				name=temp_name,
		#				use_parent_name=use_parent_name,
		#				#parent=parent,
		#				src_loc_at=temp_src_loc_at
		#			)
		#		)
		#	else:
		#		#temp_name = field_name_w_parent(
		#		#	parent=parent,
		#		#	name=temp_name,
		#		#	use_parent_name=use_parent_name,
		#		#)
		#		lst.append(
		#			like_func(
		#				elem,
		#				#name=field_name_w_parent(
		#				#	parent=parent,
		#				#	temp_name,
		#				#	use_parent_name=use_parent_name
		#				#),
		#				name=temp_name,
		#				src_loc_at=temp_src_loc_at
		#			)
		#		)
		#	#lst.append(type(elem).like(elem,
		#	#	name=psconcat(new_name, "_",
		#	#		elem.name
		#	#			if hasattr(elem, "name")
		#	#			else elem.extra_args_name()),
		#	#	src_loc_at=src_loc_at + 1))

		kw = {
			#"lst": lst,
			"shape": other.shape(),
			"name": new_name,
			#"name_suffix": name_suffix,
			#"use_parent_name": use_parent_name,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
			#"use_parent_name": (
			#	False
			#	if (
			#		not isinstance(parent, Splitrec)
			#		and not isinstance(parent, Splitarr)
			#	)
			#	else parent.extra_args_use_parent_name(),
			#),
			#"parent": parent,
			"src_loc_at": src_loc_at,
			"in_like": True
		}

		kw.update(kwargs)

		return Splitarr(**kw)
	#--------
	def lst(self):
		return self.__lst
	def shape(self):
		return self.__shape
	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_use_parent_name(self):
		return self.__extra_args_use_parent_name
	#def extra_args_parent(self):
	#	return self.__extra_args_parent
	#def extra_args_parent_name(self):
	#	return self.__extra_args_parent_name
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
	#def elem_name(self, index):
	#	# Idea borrowed from `field_name()`, but I'm not sure how
	#	# relevant this idea is to `Splitarr`? I think it makes sense to
	#	# leave out `extra_args_use_parent_name()` for this function.
	#	#return (
	#	#	psconcat(index)
	#	#	if (
	#	#		self.extra_args_name() is None
	#	#		or not self.extra_args_use_parent_name()
	#	#	)
	#	#	else psconcat(self.extra_args_name(), "_", index)
	#	#)
	#	return psconcat(self.extra_args_name(), "_", index)
	#--------
	def __getitem__(self, key):
		return self.lst()[key]
	#def __setitem__(self, key, val):
	#	# Not sure this method is necessary, or even makes sense, so I
	#	commented it out.
	#	self.lst()[key] = val
	#--------
	def eq(self, other):
		try:
			Value.cast(other)
		except Exception:
			raise TypeError(psconcat
				("Need to be able to cast `other`, `{!r}`, ".format(other),
				"to `Value"))
		return self.as_value().eq(Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		return Cat(*self.flattened())
		#return Cat(self.flattened())
	def __len__(self):
		return len(self.as_value())
	def __iter__(self):
		for item in self.lst():
			yield item
	def __list__(self):
		return self.lst()
	#--------
	def flattened(self):
		ret = []

		for val in self:
			#Splitrec.check_val_type("Splitarr.flattened()", val)

			#if isinstance(val, Signal) or isinstance(val, Packrec) \
			#	or isinstance(val, Packarr):
			#	ret.append(val)
			#else:
			if (
				isinstance(val, Splitarr)
				or isinstance(val, Splitrec)
			):
				#ret.append(*val.flattened())
				#ret.append(val.as_value())
				#ret += val.flattened()

				temp = val.flattened()
				for elem in temp:
					ret.append(elem)
				#print(val)
				#ret += val.flattened()
			else:
				ret.append(val)

		return ret
	#--------
#--------
#class Vec2Layt(Packrec.Layout):
#	def __init__(self, ElemKindT, SIGNED=False):
#		self.__ElemKindT = ElemKindT
#		self.__SIGNED = SIGNED
#		super().__init__ \
#		(
#			[
#				("x", self.ElemKindT()),
#				("y", self.ElemKindT()),
#			],
#			SIGNED=SIGNED
#		)
#	def ElemKindT(self):
#		return self.__ElemKindT
#	def SIGNED(self):
#		return self.__SIGNED
class Vec2Layt(dict):
	def __init__(self, shape):
		super().__init__({"x": shape, "y": shape})
class Vec2(Splitrec):
	#def __init__(self, shape, SIGNED=False):
	def __init__(self, shape):
		#self.x = Splitrec.cast_shape(shape=shape, name="Vec2_x")
		#self.y = Splitrec.cast_shape(shape=shape, name="Vec2_y")
		#super().__init__({"x": shape, "y": shape})
		super().__init__(Vec2Layt(shape=shape))
#--------
class PrevCurrPair:
	def __init__(self, curr=None):
		self.__prev, self.__curr = None, curr

	def prev(self):
		return self.__prev
	def curr(self):
		return self.__curr

	def back_up(self):
		self.__prev = self.curr()

	def back_up_and_update(self, curr):
		self.__prev = self.curr()
		self.__curr = curr
#--------
