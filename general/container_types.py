#!/usr/bin/env python3

from nmigen import *
import nmigen.tracer as tracer
from nmigen.hdl.ast import ValueCastable, Slice
#from nmigen.hdl.rec import Record, Layout
from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from enum import Enum, auto
from collections import OrderedDict

from misc_util import *
#--------
#def dbg_printerr(s, obj):
#	printerr(s, ": ", type(obj), " \"", obj, "\"\n")
#--------
class ElemRef(ValueCastable):
	#--------
	def __init__(self, obj, shape, key):
		if not isinstance(obj, ElemRef):
			try:
				Value.cast(obj)
			except Exception:
				raise TypeError("`obj` {!r} is not castable to `Value`"
					.format(obj)) from None

		if isinstance(shape, Packrec.Layout):
			if not isinstance(key, str):
				raise TypeError(("`key` {!r} has invalid type: "
					+ "should be `str`")
					.format(key))

			if not key in shape.fields():
				raise AttributeError("`key` {} is not a member"
					.format(key))

		self.__obj = obj
		self.__shape = shape
		self.__key = key
	#--------
	def obj(self):
		return self.__obj
	def shape(self):
		return self.__shape
	def key(self):
		return self.__key

	def fields(self):
		return self.shape().fields()

	def ElemKindT(self):
		return self.shape().ElemKindT()
	def ELEM_WIDTH(self):
		return self.shape().ELEM_WIDTH()
	def SIZE(self):
		return self.shape().SIZE()
	def SIG_WIDTH(self):
		return self.shape().SIG_WIDTH()
	def signed(self):
		return self.shape().signed()
	#--------
	def eq(self, other):
		return self.as_value().eq(other)
	def word_select(self, index, elem_width):
		return self.as_value().word_select(index, elem_width)
	#--------
	def __add__(self, other):
		return (self.as_value() + Value.cast(other))
	def __radd__(self, other):
		return (Value.cast(other) + self.as_value())
	def __sub__(self, other):
		return (self.as_value() - Value.cast(other))
	def __rsub__(self, other):
		return (Value.cast(other) - self.as_value())
	def __pos__(self):
		return self.as_value()
	def __neg__(self):
		return (-self.as_value())

	def __mul__(self, other):
		return (self.as_value() * Value.cast(other))
	def __rmul__(self, other):
		return (Value.cast(other) * self.as_value())
	def __floordiv__(self, other):
		return (self.as_value() // Value.cast(other))
	def __rfloordiv__(self, other):
		return (Value.cast(other) // self.as_value())
	def __mod__(self, other):
		return (self.as_value() % Value.cast(other))
	def __rmod__(self, other):
		return (Value.cast(other) % self.as_value())

	def __lshift__(self, other):
		return (self.as_value() << Value.cast(other))
	def __rlshift__(self, other):
		return (Value.cast(other) << self.as_value())
	def __rshift__(self, other):
		return (self.as_value() >> Value.cast(other))
	def __rrshift__(self, other):
		return (Value.cast(other) >> self.as_value())

	def __and__(self, other):
		return (self.as_value() & Value.cast(other))
	def __rand__(self, other):
		return (Value.cast(other) & self.as_value())
	def __or__(self, other):
		return (self.as_value() | Value.cast(other))
	def __ror__(self, other):
		return (Value.cast(other) | self.as_value())
	def __xor__(self, other):
		return (self.as_value() ^ Value.cast(other))
	def __rxor__(self, other):
		return (Value.cast(other) ^ self.as_value())
	def __invert__(self):
		return (~self.as_value())

	def __eq__(self, other):
		return (self.as_value() == Value.cast(other))
	def __ne__(self, other):
		return (self.as_value() != Value.cast(other))
	def __lt__(self, other):
		return (self.as_value() < Value.cast(other))
	def __gt__(self, other):
		return (self.as_value() > Value.cast(other))
	def __le__(self, other):
		return (self.as_value() <= Value.cast(other))
	def __ge__(self, other):
		return (self.as_value() >= Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		# I believe that `obj` will always be a `Signal` or `Slice`, though
		# `self.obj()` could be a `Signal`, `Slice`, `ElemRef`, `Packrec`,
		# or `Packarr`. That's at least how it's supposed to work.
		obj = Value.cast(self.obj())

		if isinstance(self.shape(), Packrec.Layout):
			if isinstance(self.key(), str):
				if self.key() not in self.fields():
					raise AttributeError(("`self.shape()` `{}` does not "
						+ "have a field `{}. Did you mean one of `{}`?")
						.format(self.shape(), self.key(),
							", ".join(self.fields())))

				# This is the `shape` of the field
				shape = self.fields()[self.key()]
				start_stop_pair = self.__calc_packrec_start_stop_pair \
					(shape, self.key())
				return obj[start_stop_pair[0]:start_stop_pair[1]]
			elif isinstance(self.key(), slice):
				return obj[self.key]
			else:
				try:
					Value.cast(self.key())
				except Exception:
					raise TypeError(("`self.key()` `{!r}` is not one of "
						+ "`str`, `slice`, or castable to `Value`")
						.format(self.key())) from None
		else: # if isinstance(self.shape(), Packarr.Shape):
			if isinstance(self.key(), slice):
				return obj[self.key()]
			else:
				try:
					Value.cast(self.key())
				except Exception:
					raise TypeError(("`self.key()` `{!r}` is neither "
						+ "a `slice` nor castable to `Value`")
						.format(self.key())) from None

				shape = self.ElemKindT()
				return obj.word_select(self.key(),
					shape
						if isinstance(shape, int)
						else len(shape))
	def __len__(self):
		return len(self.as_value())
	def __repr__(self):
		return "ElemRef([{}, {}, {}])" \
			.format(self.obj(), self.shape(), self.key())
	def __getattr__(self, key):
		if key[0] == "_":
			return self.__dict__[key]
		else: # if key[0] != "_":
			return self[key]
	def __getitem__(self, key):
		if isinstance(self.shape(), Packrec.Layout):
			if isinstance(self.key(), str):
				if self.key() not in self.fields():
					raise AttributeError(("`{}` does not have a field `{}. "
						+ "Did you mean one of `{}`?")
						.format(self.shape(), self.key(),
							", ".join(self.fields())))
				shape = self.fields()[self.key()]
			elif isinstance(self.key(), slice):
				return self.as_value()[self.key()]
			else:
				try:
					Value.cast(self.key())
				except Exception:
					raise TypeError(("`self.key()` {!r} is not one of "
						+ "`str`, `slice`, or castable to `Value`")
						.format(self.key())) from None
				return self.as_value()[self.key()]
		else: # if isinstance(self.shape(), Packarr.Shape):
			shape = self.ElemKindT()
		return ElemRef(self, shape, key)
	#--------
	def __calc_packrec_start_stop_pair(self, shape, key):
		start = 0

		for field_key in self.fields():
			field = self.fields()[field_key]
			if field_key != key:
				if isinstance(field, int):
					start += field
				else: # if not isinstance(field, int)
					start += len(field)
			else: # if field_key == key:
				break

		return (start,
			start + (shape
				if isinstance(shape, int)
				else len(shape)))
	#--------
#--------
class Packrec(ValueCastable):
	#--------
	class Layout:
		#--------
		@staticmethod
		def cast(obj, signed=False, *, src_loc_at=0):
			return obj \
				if isinstance(obj, Packrec.Layout) \
				else Packrec.Layout(obj, src_loc_at=src_loc_at + 1)
			#if isinstance(obj, Packrec.Layout):
			#	return obj
			#return Packrec.Layout(obj, signed, src_loc_at=src_loc_at + 1)
		#--------
		def __init__(self, fields, signed=False, *, src_loc_at=0):
			self.__fields = OrderedDict()
			self.__signed = signed
			self.__SIG_WIDTH = 0

			for field in list(reversed(fields)):
				if (not isinstance(field, tuple)) or (len(field) != 2):
					raise TypeError(("Field {!r} has invalid layout: "
						+ "should be (name, shape)").format(field))

				name, shape = field

				# Check for an internal `Packrec.Layout`
				if isinstance(shape, list):
					shape = Packrec.Layout.cast(shape)

				if not isinstance(name, str):
					raise TypeError(("Field {!r} has invalid name: "
						+ "should be a string")
						.format(field))

				if (not isinstance(shape, Packrec.Layout)) \
					and (not isinstance(shape, Packarr.Shape)) \
					and (not isinstance(shape, int)):
					raise TypeError(("`shape` {!r} has invalid type: "
						+ "should be a `Packrec.Layout`, "
						+ "a `Packarr.Shape`, or an `int`")
						.format(shape))

					#try:
					#	# Check provided shape by calling `Shape.cast()`
					#	# and checking for exception
					#	Shape.cast(shape, src_loc_at=src_loc_at + 1)
					#except Exception:
					#	raise TypeError(("Field {!r} has invalid shape: "
					#		+ "should be castable to `Shape` "
					#		+ "or a list of fields of a nested `Packrec`")
					#		.format(field)) from None

				if isinstance(shape, int) and (shape <= 0):
					raise ValueError(("`int` `shape` {!r} has invalid "
						+ "value: should be > 0")
						.format(shape))

				if name in self.fields():
					raise NameError(("Field {!r} has a name that is "
						+ "already present in the layout"
						.format(field)))

				self.__SIG_WIDTH += shape \
					if isinstance(shape, int) \
					else len(shape)

				self.fields()[name] = shape
		#--------
		def fields(self):
			return self.__fields
		def signed(self):
			return self.__signed
		def SIG_WIDTH(self):
			return self.__SIG_WIDTH
		#--------
		def __len__(self):
			return self.SIG_WIDTH()
		def __getitem__(self, key):
			return self.fields()[key]
		def __iter__(self):
			for name, shape in self.fields().items():
				yield (name, shape)
		def __eq__(self, other):
			return (isinstance(other, Packrec.Layout)
				and (self.fields() == other.fields()))
		def __repr__(self):
			field_reprs = []
			for name, shape in self:
				field_reprs.append("({!r}, {!r})".format(name, shape))
			return "Packrec.Layout([{}])".format(", ".join(field_reprs))
		#--------
	#--------
	@staticmethod
	def like(other, *, name=None, name_suffix=None, src_loc_at=0,
		**kwargs):
		if name is not None:
			new_name = str(name)
		elif name_suffix is not None:
			new_name = other.name() + str(name_suffix)
		else:
			new_name = tracer.get_var_name(depth=src_loc_at + 2,
				default=None)

		#return Packrec(other.layout(), name=new_name, src_loc_at=1)
		kw \
			= dict \
			(
				layout=other.layout(),
				name=new_name,
				src_loc_at=src_loc_at + 1
			)
		if isinstance(other, Packrec):
			kw.update \
			(
				reset=other.extra_args_reset(),
				reset_less=other.extra_args_reset_less(),
				attrs=other.extra_args_attrs(),
				decode=other.extra_args_decoder(),
			)
		kw.update(kwargs)

		return Packrec(**kw)
	#--------
	def __init__(self, layout, *, name=None, reset=0, reset_less=False,
		attrs=None, decoder=None, src_loc_at=0):
		if name is None:
			name = tracer.get_var_name(depth=src_loc_at + 2, default=None)

		self.__extra_args_name = name
		self.__extra_args_reset = reset
		self.__extra_args_reset_less = reset_less
		self.__extra_args_attrs = attrs
		self.__extra_args_decoder = decoder
		self.__extra_args_src_loc_at = src_loc_at

		self.__layout = Packrec.Layout.cast(layout,
			src_loc_at=src_loc_at + 1)

		sig_shape = unsigned(self.SIG_WIDTH()) \
			if not self.signed() \
			else signed(self.SIG_WIDTH())

		self.__sig \
			= Signal \
			(
				shape=sig_shape,
				name=self.extra_args_name(),
				reset=self.extra_args_reset(),
				reset_less=self.extra_args_reset_less(),
				attrs=self.extra_args_attrs(),
				decoder=self.extra_args_decoder(),
				src_loc_at=self.extra_args_src_loc_at(),
			)
	#--------
	def layout(self):
		return self.__layout
	def signed(self):
		return self.layout().signed()
	def SIG_WIDTH(self):
		return self.layout().SIG_WIDTH()
	def sig(self):
		return self.__sig

	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_reset(self):
		return self.__extra_args_reset
	def extra_args_reset_less(self):
		return self.__extra_args_reset_less
	def extra_args_attrs(self):
		return self.__extra_args_attrs
	def extra_args_decoder(self):
		return self.__extra_args_decoder
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
	#@staticmethod
	#def to_sig(val):
	#	return Packarr.to_sig(val)
	#--------
	def eq(self, other):
		return self.sig().eq(Value.cast(other))
	def word_select(self, index, elem_width):
		return self.sig().word_select(index, elem_width)
	#--------
	def __add__(self, other):
		return (self.as_value() + Value.cast(other))
	def __radd__(self, other):
		return (Value.cast(other) + self.as_value())
	def __sub__(self, other):
		return (self.as_value() - Value.cast(other))
	def __rsub__(self, other):
		return (Value.cast(other) - self.as_value())
	def __pos__(self):
		return self.as_value()
	def __neg__(self):
		return (-self.as_value())

	def __mul__(self, other):
		return (self.as_value() * Value.cast(other))
	def __rmul__(self, other):
		return (Value.cast(other) * self.as_value())
	def __floordiv__(self, other):
		return (self.as_value() // Value.cast(other))
	def __rfloordiv__(self, other):
		return (Value.cast(other) // self.as_value())
	def __mod__(self, other):
		return (self.as_value() % Value.cast(other))
	def __rmod__(self, other):
		return (Value.cast(other) % self.as_value())

	def __lshift__(self, other):
		return (self.as_value() << Value.cast(other))
	def __rlshift__(self, other):
		return (Value.cast(other) << self.as_value())
	def __rshift__(self, other):
		return (self.as_value() >> Value.cast(other))
	def __rrshift__(self, other):
		return (Value.cast(other) >> self.as_value())

	def __and__(self, other):
		return (self.as_value() & Value.cast(other))
	def __rand__(self, other):
		return (Value.cast(other) & self.as_value())
	def __or__(self, other):
		return (self.as_value() | Value.cast(other))
	def __ror__(self, other):
		return (Value.cast(other) | self.as_value())
	def __xor__(self, other):
		return (self.as_value() ^ Value.cast(other))
	def __rxor__(self, other):
		return (Value.cast(other) ^ self.as_value())
	def __invert__(self):
		return (~self.as_value())

	def __eq__(self, other):
		return (self.as_value() == Value.cast(other))
	def __ne__(self, other):
		return (self.as_value() != Value.cast(other))
	def __lt__(self, other):
		return (self.as_value() < Value.cast(other))
	def __gt__(self, other):
		return (self.as_value() > Value.cast(other))
	def __le__(self, other):
		return (self.as_value() <= Value.cast(other))
	def __ge__(self, other):
		return (self.as_value() >= Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		return self.sig()
	def __bool__(self):
		return bool(self.sig())
	def __len__(self):
		return len(self.as_value())
	def __getattr__(self, key):
		if key[0] == "_":
			return self.__dict__[key]
		else: # if key[0] != "_":
			return self[key]
	def __getitem__(self, key):
		if isinstance(key, str):
			return ElemRef(self.sig(), self.layout(), key)
	#--------
#--------
class Packarr(ValueCastable):
	#--------
	class Shape:
		#--------
		@staticmethod
		def cast(obj, *, src_loc_at=0):
			return obj \
				if isinstance(obj, Packarr.Shape) \
				else Packarr.Shape(obj, src_loc_at=src_loc_at + 1)
		#--------
		def __init__(self, ElemKindT, SIZE, signed=False, *, src_loc_at=1):
			self.__ElemKindT = ElemKindT
			self.__SIZE = SIZE
			self.__signed = signed
			self.__ELEM_WIDTH = self.ElemKindT() \
				if isinstance(self.ElemKindT(), int) \
				else len(self.ElemKindT())
			self.__SIG_WIDTH = (self.ELEM_WIDTH() * self.SIZE())

			if (not isinstance(ElemKindT, Packrec.Layout)) \
				and (not isinstance(ElemKindT, Packarr.Shape)) \
				and (not isinstance(ElemKindT, int)):
				raise TypeError(("`ElemKindT` {!r} has invalid type: "
					+ "should be a `Packrec.Layout`, a `Packarr.Shape`, "
					+ "or an `int`")
					.format(ElemKindT))
		#--------
		def ElemKindT(self):
			return self.__ElemKindT
		def ELEM_WIDTH(self):
			return self.__ELEM_WIDTH
		def SIZE(self):
			return self.__SIZE
		def SIG_WIDTH(self):
			#return (self.ELEM_WIDTH() * self.SIZE())
			return self.__SIG_WIDTH
		def signed(self):
			return self.__signed
		#--------
		def __len__(self):
			return self.SIG_WIDTH()
		def __eq__(self, other):
			return (isinstance(other, Packarr.Shape)
				and (self.ElemKindT() == other.ElemKindT())
				and (self.SIZE() == other.SIZE())
				and (self.signed() == other.signed()))
		def __repr__(self):
			return "Packarr.Shape({}, {}, {})" \
				.format(self.ElemKindT(), self.SIZE(), self.signed())
		#--------
	#--------
	@staticmethod
	def like(other, name=None, name_suffix=None, src_loc_at=0, **kwargs):
		if name is not None:
			new_name = str(name)
		elif name_suffix is not None:
			new_name = other.name() + str(name_suffix)
		else:
			new_name = tracer.get_var_name(depth=src_loc_at + 2, 
				default=None)

		kw \
			= dict \
			(
				#ElemKindT=other.ElemKindT(),
				#SIZE=len(other),
				#signed=other.sig().shape.signed,
				shape=other.shape(),
				name=new_name,
				src_loc_at=src_loc_at + 1
			)
		if isinstance(other, Packarr):
			kw.update \
			(
				reset=other.extra_args_reset(),
				reset_less=other.extra_args_reset_less(),
				attrs=other.extra_args_attrs(),
				decoder=other.extra_args_decoder(),
			)
		kw.update(kwargs)

		return Packarr(**kw)
	#--------
	def __init__(self, shape, *, name=None, reset=0, reset_less=False,
		attrs=None, decoder=None, src_loc_at=0):
		#self.__ElemKindT = ElemKindT
		#self.__SIZE = SIZE

		self.__shape = shape

		self.__extra_args_name = name
		self.__extra_args_reset = reset
		self.__extra_args_reset_less = reset_less
		self.__extra_args_attrs = attrs
		self.__extra_args_decoder = decoder
		self.__extra_args_src_loc_at = src_loc_at

		sig_shape = unsigned(self.SIG_WIDTH()) \
			if not self.signed() \
			else signed(self.SIG_WIDTH())

		self.__sig \
			= Signal \
			(
				shape=sig_shape,
				name=self.extra_args_name(),
				reset=self.extra_args_reset(),
				reset_less=self.extra_args_reset_less(),
				attrs=self.extra_args_attrs(),
				decoder=self.extra_args_decoder(),
				src_loc_at=self.extra_args_src_loc_at(),
			)
	#--------
	def shape(self):
		return self.__shape
	def ElemKindT(self):
		return self.shape().ElemKindT()
	def ELEM_WIDTH(self):
		return self.shape().ELEM_WIDTH()
	def SIZE(self):
		return self.shape().SIZE()
	def SIG_WIDTH(self):
		return self.shape().SIG_WIDTH()
	def signed(self):
		return self.shape().signed()
	def sig(self):
		return self.__sig

	#def sig_shape(self):
	#	return self.sig().shape()

	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_reset(self):
		return self.__extra_args_reset
	def extra_args_reset_less(self):
		return self.__extra_args_reset_less
	def extra_args_attrs(self):
		return self.__extra_args_attrs
	def extra_args_decoder(self):
		return self.__extra_args_decoder
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
	#@staticmethod
	#def to_sig(val):
	#	if not (isinstance(val, Signal) or isinstance(val, ElemRef)
	#		or isinstance(val, Packrec) or isinstance(val, Packarr)):
	#		raise TypeError(("`val` {!r} is not one of the following: "
	#			+ "`Signal`, `ElemRef`, `Packrec`, `Packarr`")
	#			.format(val))

	#	if isinstance(val, Signal):
	#		return val
	#	elif isinstance(val, ElemRef):
	#		return val.obj()
	#	else:
	#		return val.sig()
	#--------
	def eq(self, other):
		return self.as_value().eq(Value.cast(other))
	def word_select(self, index, elem_width):
		return self.sig().word_select(index, elem_width)
	#--------
	def __add__(self, other):
		return (self.as_value() + Value.cast(other))
	def __radd__(self, other):
		return (Value.cast(other) + self.as_value())
	def __sub__(self, other):
		return (self.as_value() - Value.cast(other))
	def __rsub__(self, other):
		return (Value.cast(other) - self.as_value())
	def __pos__(self):
		return self.as_value()
	def __neg__(self):
		return (-self.as_value())

	def __mul__(self, other):
		return (self.as_value() * Value.cast(other))
	def __rmul__(self, other):
		return (Value.cast(other) * self.as_value())
	def __floordiv__(self, other):
		return (self.as_value() // Value.cast(other))
	def __rfloordiv__(self, other):
		return (Value.cast(other) // self.as_value())
	def __mod__(self, other):
		return (self.as_value() % Value.cast(other))
	def __rmod__(self, other):
		return (Value.cast(other) % self.as_value())

	def __lshift__(self, other):
		return (self.as_value() << Value.cast(other))
	def __rlshift__(self, other):
		return (Value.cast(other) << self.as_value())
	def __rshift__(self, other):
		return (self.as_value() >> Value.cast(other))
	def __rrshift__(self, other):
		return (Value.cast(other) >> self.as_value())

	def __and__(self, other):
		return (self.as_value() & Value.cast(other))
	def __rand__(self, other):
		return (Value.cast(other) & self.as_value())
	def __or__(self, other):
		return (self.as_value() | Value.cast(other))
	def __ror__(self, other):
		return (Value.cast(other) | self.as_value())
	def __xor__(self, other):
		return (self.as_value() ^ Value.cast(other))
	def __rxor__(self, other):
		return (Value.cast(other) ^ self.as_value())
	def __invert__(self):
		return (~self.as_value())

	def __eq__(self, other):
		return (self.as_value() == Value.cast(other))
	def __ne__(self, other):
		return (self.as_value() != Value.cast(other))
	def __lt__(self, other):
		return (self.as_value() < Value.cast(other))
	def __gt__(self, other):
		return (self.as_value() > Value.cast(other))
	def __le__(self, other):
		return (self.as_value() <= Value.cast(other))
	def __ge__(self, other):
		return (self.as_value() >= Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		return self.sig()
	def __bool__(self):
		return bool(self.sig())
	def __len__(self):
		return len(self.as_value())
		#return len(self.sig())
		#return self.SIZE()
	def __repr__(self):
		#return repr(self.sig())
		return "Packarr([{}, {}])".format(self.ElemKindT(), self.SIZE())
	def __getitem__(self, key):
		return ElemRef(self.sig(), self.shape(), key)
	#--------
#--------
# A record type is composed of separate signals.  This allows setting the
# attributes of every signal.
class Splitrec(ValueCastable):
	#--------
	#@staticmethod
	#def like(other, name=None, name_suffix=None, src_loc_at=0, **kwargs):
	#	if name is not None:
	#		new_name = str(name)
	#	elif name_suffix is not None:
	#		new_name = other.name() + str(name_suffix)
	#	else:
	#		new_name = tracer.get_var_name(depth=src_loc_at + 2, 
	#			default=None)

	#	fields = {}

	#	for name, field in other.fields():
	#		if isinstance(field, Signal):
	#			fields[name] = Signal.like(field, name=)
	#		elif isinstance(field, Packrec):
	#			fields[name] = Packrec.like(field)

	#	kw \
	#		= dict \
	#		(
	#			fields=fields
	#		)

	#	kw.update(kwargs)

	#	return Splitrec(**kw)
	
	def __init__(self, fields: dict={}, *, name=None, src_loc_at=0):
		#self.__dict__["__fields"] = fields
		#self.__dict__["_Splitrec__fields"] = fields

		#self.__fields = fields
		for name, field in fields:
			self.__setattr__(name, field)

		self.__extra_args_name = name
		self.__extra_args_src_loc_at = src_loc_at
	#--------
	def fields(self):
		#return self.__fields
		ret = {}
		for name in self.__dict__:
			if name[0] != "_":
				ret[name] = self.__dict__[name]
		return ret

	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
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
			raise TypeError(psconcat
				("Need to be able to cast `other`, {!r}, ".format(other),
				"to `Value"))
		return self.as_value().eq(Value.cast(other))
	#--------
	@ValueCastable.lowermethod
	def as_value(self):
		#printout("Splitrec.as_value(): ", self.flattened(), "\n")
		return Cat(*self.flattened())
	def __len__(self):
		return len(self.as_value())
	#def __repr__(self):
	#--------
	def __check_val_type(self, prefix_str, val):
		#assert (isinstance(val, Signal) or isinstance(val, Packarr) 
		#	or isinstance(val, Packrec) or isinstance(val, Splitrec)), \
		#	psconcat(prefix_str, " Error:  Need a `Signal`, `Packarr`, ",
		#		"`Packrec`, or `Splitrec` for `val`, and `val`'s type ",
		#		"is \"", type(val), "\". Also, `val` is \"", val, "\".")
		if (not isinstance(val, Signal)) \
			and (not isinstance(val, Packrec)) \
			and (not isinstance(val, Packarr)) \
			and (not isinstance(val, Splitrec)):
			raise TypeError(psconcat(prefix_str, " ",
				"Need a `Signal`, `Packrec`, `Packarr`, or `Splitrec` ",
				"for `val` {!r}".format(val)))
	def flattened(self):
		ret = []
		for val in self.fields().values():
			self.__check_val_type("Splitrec.flattened()", val)

			if isinstance(val, Signal) or isinstance(val, Packrec) \
				or isinstance(val, Packarr):
				#ret.append(SigContrBase.get_nmigen_val(val))
				#printout("Splitrec.flattened(): ",
				#	Value.cast(val).name, "\n")
				ret.append(val)
			else: # if isinstance(val, Splitrec):
				ret.append(val.flattened())
		#printout("\n")
		return ret
	#def cat(self):
	#	return eval(psconcat("Cat(" + ",".join(self.flattened()) + ")"))
	#--------
#--------
class Vec2Layout(Packrec.Layout):
	def __init__(self, ElemKindT, signed=False):
		self.__ElemKindT = ElemKindT
		self.__signed = signed
		super().__init__ \
		(
			[
				("x", self.ElemKindT()),
				("y", self.ElemKindT()),
			],
			signed=signed
		)
	def ElemKindT(self):
		return self.__ElemKindT
	def signed(self):
		return self.__signed
class Vec2(Packrec):
	def __init__(self, ElemKindT, signed):
		super().__init__(Vec2Layout(ElemKindT=ElemKindT))
	def ElemKindT(self):
		return self.layout().ElemKindT()
	def signed(self):
		return self.layout().signed()
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
