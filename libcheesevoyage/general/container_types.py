#!/usr/bin/env python3

#from enum import Enum, auto
import enum as pyenum
from collections import OrderedDict

from amaranth import *
from amaranth.lib.data import *
import amaranth.tracer as tracer
from amaranth.hdl.ast import ValueCastable, Slice
#from amaranth.hdl.rec import Record, Layout
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
#from libcheesevoyage.general.general_types import SigInfo
#import libcheesevoyage.general.general_types as general_types
#--------
#def dbg_printerr(s, obj):
#	printerr(s, ": ", type(obj), " \"", obj, "\"\n")
#--------
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
#@staticmethod
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

	#print("cast_shape(): ", repr(shape), type(shape))
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
		return cast_shape(
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
			(
				isinstance(shape.ObjKind(), type)
				and (
					issubclass(shape.ObjKind(), Splitrec)
					or issubclass(shape.ObjKind(), Splitarr)
				)
			) or (
				shape.ObjKind() == cast_shape
			)
		):
			temp_kwargs = {
				#"prefix": "",
				#"suffix": "",
				"use_parent_name": use_parent_name,
				#"parent_name": parent_name,
				#"parent": parent,
				"src_loc_at": src_loc_at + 1,
				"in_like": in_like,
			}
			#if shape.ObjKind() != cast_shape:
			return shape.mk_sig(
				#basenm=new_name,
				basenm=temp_name,
				**temp_kwargs,
			)
			#else:
			#	return shape.ObjKind()(
			#		#name=new_name,
			#		name=temp_name,
			#		shape=shape.shape(),
			#		**temp_kwargs,
			#	)
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
		#	"cast_shape(): making Splitrec: ",
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
		#	ret.__fields[key] = cast_shape(
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
		#	"cast_shape(): making Splitarr: ",
		#	new_name, " ",
		#	use_parent_name,
		#))
		#print(psconcat(
		#	"cast_shape(): making Splitarr: ",
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

class SigInfo:
	def __init__(
		self,
		basenm: str,
		shape,
		*,
		ObjKind=cast_shape,
		reset=0,
		attrs: str=sig_keep(),
		#prefix: str="",
		prefix: str="",
		suffix: str="",
		**kwargs
	):
		self.__basenm = basenm
		self.__shape = shape
		self.__ObjKind = ObjKind
		self.__reset = reset
		self.__attrs = attrs
		self.__prefix = prefix
		self.__suffix = suffix
		self.__kwargs = kwargs

	@staticmethod
	def like(other, **kwargs):
		kw = {
			"basenm": other.basenm(),
			"shape": other.shape(),
			"ObjKind": other.ObjKind(),
			"reset": other.reset(),
			"attrs": other.attrs(),
			"prefix": other.prefix(),
			"suffix": other.suffix(),
		}
		kw.update(kwargs)

		return SigInfo(**kw)
	@staticmethod
	def like_sig(sig, **kwargs):
		assert isinstance(sig, Signal)

		kw = {
			"basenm": sig.name,
			"shape": sig.shape(),
			"ObjKind": Signal,
			"reset": sig.reset,
			"attrs": sig.attrs,
		}
		kw.update(kwargs)

		return SigInfo(**kw)


	def basenm(self):
		return self.__basenm
	def shape(self):
		return self.__shape
	def ObjKind(self):
		return self.__ObjKind
	def reset(self):
		return self.__reset
	def attrs(self):
		return self.__attrs
	def prefix(self):
		return self.__prefix
	def suffix(self):
		return self.__suffix

	def fullnm(
		self,
		*,
		basenm=None,
		prefix=None,
		suffix=None,
	):
		temp_basenm = self.basenm() if basenm is None else basenm 
		temp_prefix = self.prefix() if prefix is None else prefix 
		temp_suffix = self.suffix() if suffix is None else suffix 
		return psconcat(temp_prefix, temp_basenm, temp_suffix)

	def mk_sig(
		self,
		*,
		basenm=None,
		prefix=None,
		suffix=None,
		**kwargs,
	):
		temp_kwargs = self.__kwargs if len(kwargs) == 0 else kwargs
		kw = {}
		if (
			isinstance(self.ObjKind(), type)
			and (
				issubclass(self.ObjKind(), Signal)
				or issubclass(self.ObjKind(), View)
			)
		):
			kw.update({
				"reset": self.reset(),
				"attrs": self.attrs(),
			})
		kw.update(temp_kwargs)
		return self.ObjKind()(
			self.shape(),
			name=self.fullnm(
				basenm=basenm, prefix=prefix, suffix=suffix
			),
			#reset=self.reset(),
			#attrs=self.attrs(),
			**kw
		)
		#if self.shape() is not None:
		#	return self.ObjKind()(
		#		self.shape(),
		#		name=self.fullnm(
		#			basenm=basenm, prefix=prefix, suffix=suffix
		#		),
		#		reset=self.reset(),
		#		attrs=self.attrs(),
		#		**temp_kwargs
		#	)
		#else:
		#	return self.ObjKind()(
		#		self.shape(),
		#		name=self.fullnm(
		#			basenm=basenm, prefix=prefix, suffix=suffix
		#		),
		#		reset=self.reset(),
		#		attrs=self.attrs(),
		#		**temp_kwargs
		#	)

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
#class Splitrec(ValueCastable):
class Splitrec:
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
			self.__fields[key] = cast_shape(
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

			#temp_val = cast_shape(
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
		if not (
			isinstance(other, Splitrec)
			or isinstance(other, Splitarr)
		):
			try:
				Value.cast(other)
			except Exception:
				raise TypeError(
					psconcat
					("Need to be able to cast `other`, {!r}, ".format(
						other
					),
					"to `Value")
				)
			return self.as_value().eq(Value.cast(other))
		else:
			return self.as_value().eq(other.as_value())
	#--------
	#@ValueCastable.lowermethod
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
#class Splitarr(ValueCastable):
class Splitarr:
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
			temp_elem = cast_shape(
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
		if not (
			isinstance(other, Splitrec)
			or isinstance(other, Splitarr)
		):
			try:
				Value.cast(other)
			except Exception:
				raise TypeError(psconcat
					("Need to be able to cast `other`, `{!r}`, ".format(
						other
					),
					"to `Value"))
			return self.as_value().eq(Value.cast(other))
		else:
			return self.as_value().eq(other.as_value())
	#--------
	#@ValueCastable.lowermethod
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
#class PortDir(pyenum.Enum):
#	Inp = 0
#	Outp = pyenum.auto()
#class Modport:
#	class Elem:
#		def __init__(
#			self,
#			name=
#			pdir: PortDir,
#			shape,
#		):
#			pdir
#	def __init__(
#		self,
#		shape: dict,
#	):
#		pass
#
#class Splitintf:
#	def __init__
#class Splitintfarr:
#	def __init__(
#		self,
#		shape,
#		*,
#		name=None,
#		use_parent_name=True,
#		src_loc_at=0,
#		in_like: bool=False,
#	):
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
		#self.x = cast_shape(shape=shape, name="Vec2_x")
		#self.y = cast_shape(shape=shape, name="Vec2_y")
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
