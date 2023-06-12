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
#class ObjConstants:
#	def __init__(
#		#shape,
#		*,
#		name=None,
#		use_parent_name=False,
#		parent_name=None,
#		reset=None,
#		reset_less=None,
#		attrs=None,
#		decoder=None,
#		src_loc_at=0,
#		in_like: bool=False
#	):
#		self.__name = name
#		self.__use_parent_name = use_parent_name
#		self.__parent_name = parent_name
#		self.__reset = reset
#		self.__reset_less = reset_less
#		self.__attrs = attrs
#		self.__decoder = decoder
#		self.__src_loc_at  src_loc_at
#		self.__in_like :  in_like=Fals

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
	do_except: bool=True,
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
		#with open("cast_shape-FieldInfo.txt.ignore", "w") as f:
		#	f.writelines([
		#		"shape, field: {!r} {!r}\n".format(shape, shape.field())
		#	])
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
		#temp = Splitrec(dct=shape, name=new_name,
		#	src_loc_at=src_loc_at + 1)
		#return Splitrec.like(other=temp, name=None,
		#	name_suffix=None, src_loc_at=src_loc_at + 1)

		#print("testificate 2")
		#dct = OrderedDict()

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
			#dct=OrderedDict(),
			#dct=shape,
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
		#	#dct[key]
		#	ret.__dct[key] = cast_shape(
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
		#print(dct)
		#print("testificate 5: ", ret.dct())
		return ret

		#return Splitrec(
		#	#dct=dct,
		#	dct=shape,
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
		if do_except:
			#raise TypeError(psconcat
			#	("Need one of the following types for `shape`, {!r}, "
			#		.format(shape),
			#	": `int`, `Packrec.Layout`, `list`, or `Packarr.Shape`"))
			#with open("cast_shape-exception.txt.ignore", "w") as f:
			#	f.writelines([
			#		"shape: {!r} {!r}\n".format(shape, type(shape)),
			#		"isinstance: {!r}\n".format(
			#			isinstance(shape, FieldInfo)
			#		),
			#	])
			raise TypeError(psconcat(
				"Invaild type for `shape`, `{!r}`".format(shape)
			))
		else:
			return None

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

def do_psconcat_flattened(obj, *, use_repr: bool=True, spaces=0):
	def mk_spaces(spaces):
		return str().join(["\t" for i in range(spaces)])
	def inner_psconcat(some_obj, use_repr):
		if not use_repr:
			return psconcat(some_obj)
		else: # if use_repr:
			return psconcat("{!r}".format(some_obj))

	#ret = mk_spaces(spaces)
	ret = ""

	if (
		isinstance(obj, Splitrec)
		or isinstance(obj, Splitintf)
	):
		ret += mk_spaces(spaces)
		ret += inner_psconcat(obj.extra_args_name(), use_repr=True)
		ret += "\n"
		for field in obj.dct().values():
			ret += do_psconcat_flattened(
				field, use_repr=use_repr, spaces=spaces + 1
			)
		#ret += "\n"
		#print()
	elif (
		isinstance(obj, Splitarr)
		or isinstance(obj, Splitintfarr)
	):
		ret += mk_spaces(spaces)
		ret += inner_psconcat(obj.extra_args_name(), use_repr=True)
		ret += "\n"
		for i in range(len(list(obj))):
			ret += do_psconcat_flattened(
				obj[i], use_repr=use_repr, spaces=spaces + 1
			)
		#ret += "\n"
		#print()
	elif isinstance(obj, IntfShape):
		ret += mk_spaces(spaces)
		ret += inner_psconcat("shape", use_repr=True)
		ret += "\n"
		ret += do_psconcat_flattened(
			obj.shape(), use_repr=use_repr, spaces=spaces + 1
		)
		ret += mk_spaces(spaces)
		if obj.modport() is not None:
			ret += inner_psconcat("modport.dct()", use_repr=True)
			ret += "\n"
			ret += do_psconcat_flattened(
				obj.modport().dct(), use_repr=use_repr, spaces=spaces + 1
			)
		else:
			ret += inner_psconcat("modport is None", use_repr=True)
			ret += "\n"
		ret += mk_spaces(spaces)
		ret += inner_psconcat("tag", use_repr=True)
		ret += "\n"
		ret += do_psconcat_flattened(
			obj.tag(), use_repr=use_repr, spaces=spaces + 1
		)
	elif isinstance(obj, IntfarrShape):
		ret += mk_spaces(spaces)
		ret += inner_psconcat("shape", use_repr=True)
		ret += "\n"
		ret += do_psconcat_flattened(
			obj.shape(), use_repr=use_repr, spaces=spaces + 1
		)
		ret += mk_spaces(spaces)
		ret += inner_psconcat("tag", use_repr=True)
		ret += "\n"
		ret += do_psconcat_flattened(
			obj.tag(), use_repr=use_repr, spaces=spaces + 1
		)
	elif (
		isinstance(obj, Signal)
		or isinstance(obj, View)
	):
		ret += mk_spaces(spaces)
		ret += inner_psconcat(
			#obj.name,
			obj,
			use_repr=True,
		)
		ret += " "
		ret += inner_psconcat(
			(
				obj.shape()
				if isinstance(obj, Signal)
				#else "\"other shape\""
				#else obj._View__layout
				else Layout.of(obj)
			),
			use_repr=use_repr,
		)
		ret += "\n"
	elif isinstance(obj, list) or isinstance(obj, tuple):
		for i in range(len(obj)):
			ret += do_psconcat_flattened(
				obj[i], use_repr=use_repr, spaces=spaces + 1
			)
	elif isinstance(obj, dict):
		for key in obj:
			ret += mk_spaces(spaces)
			ret += inner_psconcat(key, use_repr=True)
			#ret += ":"
			ret += "\n"
			ret += do_psconcat_flattened(
				obj[key], use_repr=use_repr, spaces=spaces + 1
			)
		#ret += "\n"
	elif (
		#hasattr(obj, "__dict__")
		hasattr(obj, "extra_args_conn_name")
	):
		ret += mk_spaces(spaces)
		#for key in obj.__dict__:
		#	ret += inner_psconcat(
		#		key, use_repr=True
		#	)
		#	#ret += ":"
		#	ret += "\n"
		#	ret += do_psconcat_flattened(
		#		getattr(obj, key), use_repr=use_repr, spaces=spaces + 1
		#	)
		ret += inner_psconcat(
			obj.extra_args_conn_name(), use_repr=True
		)
		ret += "\n"
		for field in obj.__dict__.values():
			ret += do_psconcat_flattened(
				field, use_repr=use_repr, spaces=spaces + 1
			)
		#ret += "\n"
	else:
		ret += mk_spaces(spaces)
		ret += inner_psconcat(obj, use_repr=use_repr)
		ret += "\n"
	#ret += "\n"
	return ret
def do_print_flattened(obj, *, use_repr: bool=True):
	print(do_psconcat_flattened(obj, use_repr=use_repr))

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
		shape: dict,
		*,
		name=None,
		use_parent_name=True,
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

		self.__extra_args_name = new_name
		self.__extra_args_use_parent_name = use_parent_name
		self.__extra_args_src_loc_at = src_loc_at

		self.__dct = {}
		for key, field_shape in shape.items():
			#if isinstance(field, View):
			#	raise TypeError(psconcat
			#		("Need `Splitrec.View` for `field` implementing, ",
			#		"`{!r}`")
			#		.format(field))
			#self.__setattr__(key, field)
			#self.__dct[key] = field
			self.__dct[key] = cast_shape(
				field_shape,
				name=key,
				use_parent_name=use_parent_name,
				parent_name=new_name,
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

		kw = {
			#"dct": dct,
			"shape": other.shape(),
			"name": new_name,
			#"name_suffix": name_suffix,
			#"use_parent_name": use_parent_name,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
			#"parent": parent,
			"src_loc_at": src_loc_at,
			"in_like": True,
		}

		kw.update(kwargs)

		return Splitrec(**kw)
	
	#--------
	def dct(self):
		return self.__dct
		##ret = OrderedDict()
		#ret = dict()
		##for name in self.__dict__:
		#for name in self.__dct:
		#	if name[0] != "_":
		#		#ret[name] = self.__dict__[name]
		#		#ret[name] = self.__dct[name]
		#		ret[name] = self.__dct[name]
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
	#--------
	#def __getitem__(self, key):
	#	return self.__dct[key]
	#def __getattr__(self, key):
	#	#ret = self.__dict__[key]
	#	ret = self.__dct[key]
	#	#ret = self[key]
	#	if isinstance(ret, Splitrec.View):
	#		print("testificate")
	#		return ret.view
	#	else:
	#		return ret
	def __getattribute__(self, key):
		if (
			key[0] == "_"
			or key in Splitrec.__dict__
		):
			return super().__getattribute__(key)
		else:
			return self.__dct[key]
	def __setattr__(self, key, val):
		#if isinstance(val, View):
		#	raise TypeError(psconcat
		#		("Need `Splitrec.View` containing the layout when ",
		#		"`val` (`{!r}`), is a `View`")
		#		.format(val))

		#if key not in self.__dct:
		#	self.__dct[key] = val
		#object.__setattr__(self, key, val)

		if (
			key[0] == "_"
			or key in Splitrec.__dict__
		):
			#print(psconcat(
			#	"Splitrec.__setattr__(): not __dct: ", key, " ", val
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
					"add in members only with the `dct` argument to ",
					"`Splitrec.__init__()`. "
				)
			)

	#def __getattr__(self, key):
	#	return self[key]
	#def __getitem__(self, key):
	#	if key[0] == "_":
	#		return self.__dict__[temp_key]
	#	else: # if key[0] != "_":
	#		val = self.dct()[key]
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
	#		self.dct()[key] = val
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
		for name in self.__dct:
			yield (name, self.__dct[name])
		#for item in self.__dct:
		#	yield item
		#for value in self.__dct.values():
		#	yield value
	#def __repr__(self):
	#--------
	#@staticmethod
	#def check_val_type(prefix_str, val):
	#	try:
	#		Value.cast(val)
	#	except Exception:
	#		raise TypeError(psconcat(prefix_str, " `val` `{!r}` must be ",
	#			"possible to cast to `Value`").format(val)) from None

	def flattened(self):
		ret = []
		for val in self.dct().values():
			#Splitrec.check_val_type("Splitrec.flattened()", val)
			if (
				isinstance(val, Splitrec)
				or isinstance(val, Splitarr)
			):
				temp = val.flattened()
				for elem in temp:
					ret.append(elem)
			else:
				ret.append(val)
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
		shape: list,
		*,
		name=None,
		#name_suffix=None,
		use_parent_name=True,
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
		self.__extra_args_src_loc_at = src_loc_at

		#self.__lst = lst
		self.__lst = []
		#for elem in lst:
		for i in range(len(shape)):
			#elem = lst[i]
			elem = shape[i]
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
				#src_loc_at=self.extra_args_src_loc_at(),
				src_loc_at=self.extra_args_src_loc_at() + 1,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				in_like=in_like,
			)
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

		kw = {
			#"lst": lst,
			"shape": other.shape(),
			"name": new_name,
			#"name_suffix": name_suffix,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
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
class PortDir(pyenum.Enum):
	Inp = 0
	Outp = pyenum.auto()
	Noconn = pyenum.auto()
#def cast_intf_mbr_shape(
#	shape: dict,
#	*,
#	name=None,
#	use_parent_name=False,
#	parent_name=None,
#
#	reset=None,
#	reset_less=None,
#	attrs=None,
#	decoder=None,
#
#	src_loc_at=0,
#	in_like: bool=False,
#	in_intfarr: bool=False,
#):
#	if name is None:
#		new_name = tracer.get_var_name(
#			depth=src_loc_at + 2,
#			default=None
#		)
#	else:
#		new_name = name
#
#	temp_name = field_name(
#		parent_name=parent_name,
#		name=new_name,
#		use_parent_name=use_parent_name,
#	)
#	#temp_shape = IntfShape.cast(shape, do_except=False)
#	#if temp_shape is not None:
#	if isinstance(shape, IntfShape):
#		return Splitintf(
#			#shape=temp_shape,
#			shape=shape,
#			name=temp_name,
#			use_parent_name=use_parent_name,
#			src_loc_at=src_loc_at + 1,
#			in_like=in_like,
#		)
#	#temp_shape = IntfarrShape.cast(shape, do_except=False)
#	#if temp_shape is not None:
#	if isinstance(shape, IntfarrShape):
#		return Splitintfarr(
#			shape=temp_shape,
#			name=temp_name,
#			use_parent_name=use_parent_name,
#			src_loc_at=src_loc_at + 1,
#			in_like=in_like,
#		)
#	if not in_intfarr:
#		return cast_shape(
#			#--------
#			shape=shape,
#			name=temp_name,
#			use_parent_name=use_parent_name,
#			# NOTE: I'm pretty sure there's no need for `parent_name` to be
#			# set to anything here.
#			#--------
#			reset=reset,
#			reset_less=reset_less,
#			attrs=attrs,
#			decoder=decoder,
#			#--------
#			src_loc_at=src_loc_at + 1,
#			in_like=in_like,
#			#--------
#		)
#	else: # if in_intfarr:
#		raise TypeError(psconcat(
#			"Invalid `shape` `{!r}`".format(shape)
#		))

class Modport:
	def __init__(
		self,
		#name2pdir_dct: dict, # expected form: [str, PortDir]
		dct: dict, # expected form: [str, PortDir];
					# this should map port names to `PortDir`s
	):
		#self.__name2pdir_dct = name2pdir_dct
		for item in dct.items():
			if not (
				isinstance(item[1], PortDir)
				#and item[1] != PortDir.Noconn
				#or item[1] is None
			):
				raise TypeError(psconcat(
					"Invalid type for value in `item`: {!r}".format(item)
				))
		self.__dct = dct
	#def name2pdir_dct(self):
	#	return self.__name2pdir_dct
	def dct(self):
		return self.__dct

	#def mk_mp_dct(self, name: str, mp_dct=None):
	#	temp = {
	#		name: self,
	#	}
	#	if mp_dct is None:
	#		return temp
	#	else:
	#		mp_dct.update(temp)
	#		return mp_dct
	#def mk_mp_dct_w_rev(self, name: str, rev_name: str, mp_dct=None):
	#	#temp = {
	#	#	name: self,
	#	#	rev_name: reversed(self),
	#	#}
	#	#if mp_dct is not None:
	#	#	return temp
	#	#else:
	#	#	mp_dct.update(temp)
	#	#	return mp_dct
	#	temp = self.mk_mp_dct(name, mp_dct)
	#	return reversed(self).mk_mp_dct(rev_name, temp)
	def __reversed__(self):
		#temp_dct = {
		#	name: (
		#		PortDir.Inp
		#		if getattr(self, name) == PortDir.Outp
		#		else PortDir.Outp
		#	)
		#	#for name in self.name2pdir_dct()
		#	for name in self.dct()
		#}
		temp_dct = {}
		#for name in self.dct():
		for item in self.dct().items():
			if not (
				isinstance(item[1], PortDir)
				#or item[1] is None
			):
				raise TypeError(psconcat(
					"Invalid type for `item`: {!r}".format(item)
				))
			#temp_attr = getattr(self, item[0])
			#if temp_attr is None:
			#	temp_dct[name] = temp_attr
			#el
			if item[1] == PortDir.Inp:
				temp_dct[item[0]] = PortDir.Outp
			elif item[1] == PortDir.Outp:
				temp_dct[item[0]] = PortDir.Inp
			else:
				temp_dct[item[0]] = PortDir.Noconn
			#else:
			#	assert False
		return Modport(temp_dct)
	def __getattribute__(self, key: str):
		if (
			key[0] == "_"
			or key in Modport.__dict__
		):
			return super().__getattribute__(key)
		else:
			#return self.__name2pdir_dct[key]
			return self.__dct[key]
class IntfShape:
	def __init__(
		self,
		#mp_dct: dict, # expected form: [str, Splitintf.Modport]
		shape: dict,
		modport=None, # make this a `Modport` instance for non-`IntfShape`,
					# non-`IntfarrShape` values of `shape`
		tag=None, # tag for `Splitintf.connect()`
	):
		#self.__name2mp_dct = name2mp_dct
		#assert set(modport.dct().keys()) == set(shape.keys())
		#with open("IntfShape.txt.ignore", "w") as f:
		#	f.writelines([
		#		psconcat("modport.dct(): ", modport.dct(), "\n"),
		#		psconcat("shape: ", shape, "\n"),
		#		psconcat(
		#			"modport.dct().keys(): ", modport.dct().keys(), "\n"
		#		),
		#		psconcat("shape.keys(): ", shape.keys(), "\n"),
		#	])
		if modport is not None:
			for key in modport.dct():
				if key not in shape:
					if (
						not isinstance(shape[key], IntfShape)
						and not isinstance(shape[key], IntfarrShape)
					):
						raise AttributeError(psconcat(
							"Name mismatch between ",
							"`modport` and `shape` found: ",
							"{!r} in `modport` but not `shape`".format(
								key
							),
						))
				else:
					if (
						isinstance(shape[key], IntfShape)
						or isinstance(shape[key], IntfarrShape)
					):
						raise AttributeError(psconcat(
							"Can't have `shape[key]` {!r} ".format(
								shape[key]
							),
							"of type `IntfShape` or `IntfarrShape` if ",
							"`key` {!r} is in `modport.dct()`; ".format(
								key
							),
							"`shape`: {!r}; `modport.dct()`: {!r}".format(
								shape, modport.dct(),
							),
						))
			for key in shape:
				if key not in modport.dct():
					if (
						not isinstance(shape[key], IntfShape)
						and not isinstance(shape[key], IntfarrShape)
					):
						raise AttributeError(psconcat(
							"Name mismatch between ",
							"`shape` and `modport` found: ",
							"{!r} in `shape` but not `modport`".format(
								key
							),
						))
				else: # if key in modport.dct():
					if (
						isinstance(shape[key], IntfShape)
						or isinstance(shape[key], IntfarrShape)
					):
						raise AttributeError(psconcat(
							"Can't have `shape[key]` {!r}".format(
								shape[key]
							),
							"of type `IntfShape` or `IntfarrShape` if ",
							"`key` {!r} is in `modport.dct()`".format(
								key
							),
						))
		else: # if modport is None:
			for key in shape:
				if (
					not isinstance(shape[key], IntfShape)
					and not isinstance(shape[key], IntfarrShape)
				):
					raise AttributeError(psconcat(
						"Invalid `shape[key]` {!r} ".format(shape[key]),
						"With `modport=None`, all elements of `shape` ",
						"must be of type `IntfShape` or `IntfarrShape`"
					))
		self.__shape = shape
		self.__modport = modport
		self.__tag = tag
	@staticmethod
	def mk_single_pdir_shape(
		name: str,
		shape,
		pdir: PortDir,
		*,
		tag=None,
		mk_modport: bool=True,
	):
		return {
			name: IntfShape(
				shape=shape,
				modport=(
					Modport({
						key: pdir
						for key in shape
					}) if mk_modport
					else None
				),
				tag=tag,
			)
		}

	@staticmethod
	def mk_io_shape(
		inp_shape, outp_shape,
		*,
		inp_tag=None, outp_tag=None,
		mk_inp_modport: bool=True, mk_outp_modport: bool=True,
		#formal_shape=None, formal_tag=None
	):
		#ret = {
		#	"inp": IntfShape(
		#		shape=inp_shape,
		#		modport=Modport({
		#			inp_key: PortDir.Inp
		#			for inp_key in inp_shape
		#		}),
		#		tag=inp_tag
		#	),
		#	"outp": IntfShape(
		#		shape=outp_shape,
		#		modport=Modport({
		#			outp_key: PortDir.Outp
		#			for outp_key in outp_shape
		#		}),
		#		tag=outp_tag
		#	),
		#}
		ret = {}
		ret.update(
			IntfShape.mk_single_pdir_shape(
				name="inp",
				shape=inp_shape,
				pdir=PortDir.Inp,
				tag=inp_tag,
				mk_modport=mk_inp_modport,
			)
		)
		ret.update(
			IntfShape.mk_single_pdir_shape(
				name="outp",
				shape=outp_shape,
				pdir=PortDir.Outp,
				tag=outp_tag,
				mk_modport=mk_outp_modport,
			)
		)
		#if (
		#	formal_shape is not None
		#	and formal_tag is not None
		#):
		#	ret["formal"] 
		return ret
	@staticmethod
	def cast(other, do_except: bool=True):
		if isinstance(other, IntfShape):
			return other
		elif isinstance(other, dict):
			return IntfShape(**other)
		elif (
			(
				isinstance(other, list)
				or isinstance(other, tuple)
			) and (
				len(other) == 2
				or len(other) == 3
			)
		):
			kw = {
				"shape": other[0],
				"modport": other[1],
			}
			if len(other) == 3:
				kw["tag"] = other[2]
			return IntfShape(**kw)
		else:
			if do_except:
				raise TypeError(psconcat(
					"Invalid type for `other`: {!r}".format(other)
				))
			else:
				return None
	#def name2mp_dct(self):
	#	return self.__name2mp_dct
	def shape(self):
		return self.__shape
	def modport(self):
		return self.__modport
	def tag(self):
		return self.__tag
class Splitintf:
	def __init__(
		self,
		#shape: dict[str, Splitintf.Modport],
		#shape: Modport,
		#modport_db: dict, # expected form: [str, Splitintf.Modport]
		#shape: dict, 
		shape,
		*,
		name=None,
		conn_name=None,
		use_parent_name=True,
		#parent_name=None,
		parent=None,
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

		if conn_name is None:
			temp_conn_name = new_name
		else:
			temp_conn_name = conn_name

		temp_shape = IntfShape.cast(shape)

		self.__extra_args_name = new_name
		self.__extra_args_conn_name = temp_conn_name
		self.__extra_args_use_parent_name = use_parent_name
		#self.__extra_args_parent_name = parent_name
		self.__extra_args_parent = parent
		self.__extra_args_src_loc_at = src_loc_at

		self.__dct = {}
		for key, field_shape in temp_shape.shape().items():
			#with open("key-field_shape.txt.ignore", "w") as f:
			#	f.writelines([
			#		"key, field_shape: {!r} {!r}".format(key, field_shape)
			#	])
			self.__dct[key] = Splitintf.cast_mbr_shape(
				field_shape,
				name=key,
				conn_name=key,
				use_parent_name=use_parent_name,
				parent_name=new_name,
				parent=self,
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)

		self.__shape = temp_shape
		#self.__shape = shape
	#--------
	def dct(self):
		return self.__dct
	def shape(self):
		return self.__shape
	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_conn_name(self):
		return self.__extra_args_conn_name
	def extra_args_use_parent_name(self):
		return self.__extra_args_use_parent_name
	#def extra_args_parent_name(self):
	#	return self.__extra_args_parent_name
	def extra_args_parent(self):
		return self.__extra_args_parent
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
	@staticmethod
	def cast_mbr_shape(
		shape,
		**kwargs
	):
		return Splitintf._inner_cast_mbr_shape(shape, **kwargs)
	@staticmethod
	def _inner_cast_mbr_shape(
		shape,
		*,
		name=None,
		conn_name=None,
		use_parent_name=False,
		parent_name=None,
		parent=None,
	
		reset=None,
		reset_less=None,
		attrs=None,
		decoder=None,
	
		src_loc_at=0,
		in_like: bool=False,
		in_intfarr: bool=False,
	):
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name

		if conn_name is None:
			temp_conn_name = new_name
		else:
			temp_conn_name = conn_name
	
		temp_name = field_name(
			parent_name=parent_name,
			name=new_name,
			use_parent_name=use_parent_name,
		)
		#temp_shape = IntfShape.cast(shape, do_except=False)
		#if temp_shape is not None:
		if isinstance(shape, IntfShape):
			return Splitintf(
				#shape=temp_shape,
				shape=shape,
				name=temp_name,
				conn_name=temp_conn_name,
				use_parent_name=use_parent_name,
				parent=parent,
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)
		#temp_shape = IntfarrShape.cast(shape, do_except=False)
		#if temp_shape is not None:
		elif isinstance(shape, IntfarrShape):
			return Splitintfarr(
				#shape=temp_shape,
				shape=shape,
				name=temp_name,
				conn_name=temp_conn_name,
				use_parent_name=use_parent_name,
				parent=parent,
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
			)
		if not in_intfarr:
			#with open("not-in_intfarr.txt.ignore", "w") as f:
			#	f.writelines([
			#		"shape: {!r}\n".format(shape)
			#	])
			return cast_shape(
				#--------
				shape=shape,
				name=temp_name,
				use_parent_name=use_parent_name,
				# NOTE: I'm pretty sure there's no need to set
				# `parent_name` to anything here.
				#--------
				reset=reset,
				reset_less=reset_less,
				attrs=attrs,
				decoder=decoder,
				#--------
				src_loc_at=src_loc_at + 1,
				in_like=in_like,
				#--------
			)
		else: # if in_intfarr:
			raise TypeError(psconcat(
				"Invalid `shape` `{!r}`".format(shape)
			))
	@staticmethod
	def like(
		other,
		name=None,
		use_parent_name=None,
		src_loc_at=0,
		**kwargs
	):
		if name is None:
			new_name = tracer.get_var_name(
				depth=src_loc_at + 2,
				default=None
			)
		else:
			new_name = name

		kw = {
			"shape": other.shape(),
			"name": new_name,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
			"src_loc_at": src_loc_at,
			"in_like": True,
		}

		kw.update(kwargs)

		return Splitintf(**kw)
	#--------
	def __getattribute__(self, key):
		if (
			key[0] == "_"
			or key in Splitintf.__dict__
		):
			return super().__getattribute__(key)
		else:
			return self.__dct[key]
	#--------
	def __iter__(self):
		#for name in self.__dict__:
		#	if name[0] != "_":
		#		yield (name, self.__dict__[name])
		#		#yield self.__dict__[name]
		#for name in self.__dct:
		#	yield (name, self.__dct[name])
		for name in self.__dct:
			yield (name, self.__dct[name])
		#for item in self.__dct:
		#	yield item
		#for value in self.__dct.values():
		#	yield value
	#--------
	class ConnKind(pyenum.Enum):
		# `Module` to one of its `submodule`s
		Parent2Child = 0

		# Two child `submodules` being connected in parallel
		Parallel = pyenum.auto()

		#Ident = pyenum.auto()

	def connect(
		#self, mp_name_dct: dict, parent_m: Module,
		#other, other_mp_name_dct: dict, child_m=None,
		self, other,
		m: Module,
		kind, #: Splitintf.ConnKind,
		*,
		#f=None,
		conn_lst_shrink: int=-1,
		other_conn_lst_shrink=None,
		self_pdir=None, # `None`, `PortDir.Inp`, or `PortDir.Outp`
		use_tag: bool=False,
		#reduce_tag: bool=False,
		reduce_tag: bool=True,
	):
		if not isinstance(kind, Splitintf.ConnKind):
			raise TypeError(psconcat(
				"Invalid type for `kind` {!r}".format(kind)
			))
		# This matches by shared object names
		lst = self.flattened(w_pdir=True)
		other_lst = other.flattened(w_pdir=True)
		if other_conn_lst_shrink is not None:
			temp_other_conn_lst_shrink = other_conn_lst_shrink
		else:
			temp_other_conn_lst_shrink = conn_lst_shrink

		dct = {}
		for elem in lst:
			temp = {
				"elem": elem,
				"conn_name_lst": [],
				"tag_lst": [],
			}
			for inner_elem in (
				elem.conn_lst()[:conn_lst_shrink]
			):
				temp["conn_name_lst"].append(
					inner_elem.extra_args_conn_name()
				)
			#if f is not None:
			#	f.writelines([
			#		psconcat("debug: ",
			#			[
			#				[
			#					conn.extra_args_conn_name(),
			#					type(conn),
			#				]
			#				for conn in elem.conn_lst()[1:]
			#			]
			#		),
			#		"\n"
			#	])
			# We are (nearly 100%) guaranteed to have at least two
			# elements in `elem.conn_list()` due `Splitintf` and
			# `Splitintfarr` being composed of real Amaranth objects at
			# the end of the day
			#for inner_elem in elem.conn_lst()[:-1]:
			#for i in range(len(elem.conn_lst()[:-1])):
			for i in range(len(elem.conn_lst()[1:])):
				#if i > 0:
				inner_elem = elem.conn_lst()[i + 1]
				#if f is not None:
				#	f.writelines([
				#		psconcat(
				#			"debug inner_elem: ",
				#			do_psconcat_flattened(inner_elem)
				#		)
				#	])
				#if len(temp["conn_name_lst"]) > 1:
				temp["tag_lst"].append(
					inner_elem.shape().tag()
				)
			#dct["_".join(list(reversed(temp["conn_name_lst"])))] = temp
			temp_lst = list(reversed(temp["conn_name_lst"]))
			dct[(
				(
					temp["elem"].pdir()
					#if kind == Splitintf.ConnKind.Parent2Child
					#else (
					#	#"inp"
					#	PortDir.Inp
					#	if temp["elem"].pdir() == PortDir.Inp
					#	else PortDir.Outp #"outp"
					#)
				),
				("_".join(temp_lst))
			)] = temp
			#dct["_".join(list(reversed(elem.conn_lst()[:-1])))] = temp
		other_dct = {}
		for other_elem in other_lst:
			other_temp = {
				"elem": other_elem,
				"conn_name_lst": [],
				"tag_lst": [],
			}
			for other_inner_elem in (
				other_elem.conn_lst()[:temp_other_conn_lst_shrink]
			):
				other_temp["conn_name_lst"].append(
					other_inner_elem.extra_args_conn_name()
				)
			#for other_inner_elem in other_elem.conn_lst()[:-1]:
			for i in range(len(other_elem.conn_lst()[:-1])):
			#for i in range(len(other_elem.conn_lst()[1:])):
				#if i > 0:
				other_inner_elem = other_elem.conn_lst()[i + 1]
				#if len(other_temp["conn_name_lst"]) > 1:
				other_temp["tag_lst"].append(
					other_inner_elem.shape().tag()
				)
			other_temp_lst = list(reversed(other_temp["conn_name_lst"]))
			other_dct[(
				(
					other_temp["elem"].pdir()
					#if kind == Splitintf.ConnKind.Parent2Child
					#else (
					#	#"inp"
					#	PortDir.Inp
					#	if other_temp["elem"].pdir() == PortDir.Inp
					#	else PortDir.Outp #"outp"
					#)
				), ("_".join(other_temp_lst))
			)] = other_temp
			#other_dct["_".join(
			#	list(reversed(other.conn_lst()[:-1]))
			#)] = other_temp

		#f = open("test_tag.txt.ignore", "w")
		#if f is not None:
		#	f.writelines([
		#		#"dct:{!r}\n".format(dct),
		#		#"other_dct:{!r}\n".format(other_dct),
		#		do_psconcat_flattened({
		#			"dct": dct,
		#			"other_dct": other_dct,
		#		})
		#	])
		for key in dct:
			other_key = (
				key 
				if kind == Splitintf.ConnKind.Parent2Child
				else (
					(
						PortDir.Inp
						if key[0] == PortDir.Outp
						else PortDir.Outp
					),
					key[1]
				)
			)
			#if f is not None:
			#	f.writelines([
			#		do_psconcat_flattened(
			#			{
			#				"key": key,
			#				"other_key": other_key,
			#				"in-other": other_key in other_dct,
			#			},
			#			use_repr=True,
			#		),
			#		#"\n",
			#	])
			#if kind == Splitintf.ConnKind.Parent2Child:
			if other_key in other_dct:
				outer_elem = dct[key]
				other_outer_elem = other_dct[other_key]

				test_tag = (
					not use_tag
					or (
						(
							not reduce_tag
							#and (
							#	elem.extra_args_parent().shape().tag()
							#	== other_elem.extra_args_parent().shape()
							#		.tag()
							#)
							and (
								outer_elem["tag_lst"][0]
								== other_outer_elem["tag_lst"][0]
							)
						) or (
							reduce_tag
							and (
								outer_elem["tag_lst"]
								== other_outer_elem["tag_lst"]
							)
						)
					)
				)
				#if f is not None:
				#	f.writelines([
				#		do_psconcat_flattened(
				#			{
				#				(key, other_key): {
				#					"pdir": outer_elem["elem"].pdir(),
				#					"other-pdir": (
				#						other_outer_elem["elem"].pdir()
				#					),
				#					"conn_name_lst": 
				#						outer_elem["conn_name_lst"],
				#					"other-conn_name_lst":
				#						other_outer_elem["conn_name_lst"],
				#					"cmp": (
				#						outer_elem["conn_name_lst"]
				#						!= other_outer_elem
				#							["conn_name_lst"]
				#					),
				#					"test_tag": test_tag,
				#					"key": key,
				#					"other_key": other_key,
				#					"tag": (
				#						elem.extra_args_parent().shape()
				#							.tag()
				#					),
				#					"other-tag": (
				#						other_elem.extra_args_parent()
				#							.shape().tag()
				#					),
				#				}
				#			},
				#			use_repr=True,
				#		),
				#		#"\n",
				#	])

				if (
					(
						outer_elem["conn_name_lst"]
						!= other_outer_elem["conn_name_lst"]
					) or (
						not test_tag
					)
				):
					#if f is not None:
					#	f.writelines([
					#		psconcat("continuing\n")
					#	])
					continue

				elem = outer_elem["elem"]
				other_elem = other_outer_elem["elem"]

				err_msg = psconcat(
					"With `kind` {!r}, invalid combination of ",
					"`elem.pdir()` {!r} and `other_elem.pdir()` {!r}"
				).format(kind, elem.pdir(), other_elem.pdir())

				if kind == Splitintf.ConnKind.Parent2Child:
					if (
						elem.pdir() == other_elem.pdir()
						and elem.pdir() != PortDir.Noconn
						#and other_elem.pdir() != PortDir.Noconn
						and (
							self_pdir is None
							or elem.pdir() == self_pdir
						)
					):
						if elem.pdir() == PortDir.Inp:
							m.d.comb += other.obj().eq(elem.obj())
						else: # if elem.pdir() == PortDir.Outp:
							m.d.comb += elem.obj().eq(other.obj())
					else:
						raise ValueError(err_msg)
				else: # if kind == Splitintf.ConnKind.Parallel:
					if (
						elem.pdir() != other_elem.pdir()
						and elem.pdir() != PortDir.Noconn
						and other_elem.pdir() != PortDir.Noconn
						and (
							self_pdir is None
							or elem.pdir() == self_pdir
							or other_elem.pdir() == self_pdir
						)
					):
						if elem.pdir() == PortDir.Inp:
							m.d.comb += elem.obj().eq(other_elem.obj())
						else: # if other_elem.pdir() == PortDir.Inp:
							m.d.comb += other_elem.obj().eq(elem.obj())
					else:
						raise ValueError(err_msg)
		#f.close()

	# Flattened With `PortDir`
	class FlatWPdir:
		def __init__(
			self,
			key,
			parent,
			obj,
			pdir: PortDir,
		):
			#self.__key = key
			self.__extra_args_conn_name = key
			self.__extra_args_parent = parent
			self.__obj = obj
			self.__pdir = pdir
		#def key(self):
		#	return self.__key
		def extra_args_conn_name(self):
			return self.__extra_args_conn_name
			#return self.__key
		def extra_args_parent(self):
			return self.__extra_args_parent
		def obj(self):
			return self.__obj
		def pdir(self):
			return self.__pdir
		#def cmp_other_name(self, other):
		#	pass
		def __repr__(self):
			#return " ".join([
			#	psconcat(
			#		key, ":", repr(self.__dict__[key])
			#	)
			#	for key in self.__dict__
			#])
			return do_psconcat_flattened(self, use_repr=True)
		def conn_lst(self):
			def get_conn(ret: list):
				parent = ret[-1].extra_args_parent()
				if parent is None:
					#return list(reversed(ret))
					return ret
				else: # if parent is not None:
					#temp = ret[-1].extra_args_parent()
					#ret += get_conn(ret, temp)
					#ret.append(temp)
					#temp = ret[-1].extra_args_parent()
					#ret.append(parent.extra_args_parent())
					#return get_conn(ret, ret[-1])
					ret.append(parent)
					return get_conn(ret)
			return get_conn([self])
	def flattened(
		self,
		*,
		#conn_dct: dict,
		w_pdir: bool=False # with `PortDir`
	):
		ret = []

		for item in self.dct().items():
			key, val = item
			if isinstance(val, Splitintf):
				#temp = val.flattened()
				temp = val.flattened(w_pdir=w_pdir)
				for elem in temp:
					ret.append(elem)
			elif isinstance(val, Splitintfarr):
				#temp = val.flattened()
				temp = val.flattened(w_pdir=w_pdir)
				for elem in temp:
					ret.append(elem)
			else:
				def one_iter(
					self,
					ret: list,
					w_pdir: bool,
					key, obj,
				):
					if not w_pdir:
						ret.append(obj)
					else: # if w_pdir:
						ret.append(Splitintf.FlatWPdir(
							key=key,
							parent=self,
							obj=obj,
							pdir=getattr(self.shape().modport(), key),
						))

				if (
					isinstance(val, Splitrec)
					or isinstance(val, Splitarr)
				):
					temp = val.flattened()
					for elem in temp:
						#ret.append(elem)
						one_iter(
							self=self,
							ret=ret,
							w_pdir=w_pdir,
							key=key,
							obj=elem,
						)
				else:
					#print(val)
					#temp = val
					#ret.append(temp)
					one_iter(
						self=self,
						ret=ret,
						w_pdir=w_pdir,
						key=key, obj=val
					)

		return ret
	#--------
class IntfarrShape:
	def __init__(
		self,
		shape: list,
		tag=None,
	):
		self.__shape = shape
		self.__tag = tag
	def shape(self):
		return self.__shape
	def tag(self):
		return self.__tag
	#def __getattribute__(self, key):
	#	if (
	#		key[0] == "_"
	#		or key in IntfarrShape.__dict__
	#	):
	#		return super().__getattribute__(key)
	#	else:
	#		return self.__dct[key]
	@staticmethod
	def cast(other, do_except: bool=True):
		if isinstance(other, IntfarrShape):
			return other
		#elif isinstance(other, list):
		#	return IntfarrShape(other)
		elif (
			(
				isinstance(other, list)
				or isinstance(other, tuple)
			) and (
				len(other) == 1
				or len(other) == 2
			)
		):
			kw = {
				"shape": other[0]
			}
			if len(other) == 2:
				kw["tag"] = other[2]
			return IntfarrShape(**kw)
		else:
			if do_except:
				raise TypeError(psconcat(
					"Invalid type for `other`: {!r}".format(other)
				))
			else:
				return None
	#def __getitem__(self, key):
	#	return self.shape()[key]
class Splitintfarr:
	def __init__(
		self,
		shape,
		*,
		name=None,
		conn_name=None,
		use_parent_name=True,
		parent=None,
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

		if conn_name is None:
			temp_conn_name = new_name
		else:
			temp_conn_name = conn_name

		temp_shape = IntfarrShape.cast(shape)

		self.__extra_args_name = new_name
		self.__extra_args_conn_name = temp_conn_name
		self.__extra_args_use_parent_name = use_parent_name
		self.__extra_args_parent = parent
		self.__extra_args_src_loc_at = src_loc_at

		self.__lst = []
		for i in range(len(temp_shape.shape())):
			elem = temp_shape.shape()[i]
			temp_elem = Splitintfarr.cast_mbr_shape(
				elem,
				name=psconcat(i),
				conn_name=psconcat(i),
				use_parent_name=use_parent_name,
				parent_name=new_name,
				parent=self,
				#src_loc_at=self.extra_args_src_loc_at(),
				src_loc_at=src_loc_at + 1,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				#src_loc_at=self.extra_args_src_loc_at() + 3,
				in_like=in_like,
				in_intfarr=True,
			)
			self.__lst.append(temp_elem)
		self.__shape = temp_shape
	#--------
	@staticmethod
	def cast_mbr_shape(
		shape,
		**kwargs
	):
		return Splitintf._inner_cast_mbr_shape(
			shape,
			#in_intfarr=True,
			**kwargs
		)
	@staticmethod
	def like(
		other,
		name=None,
		use_parent_name=None,
		src_loc_at=0,
		**kwargs
	):
		# `Splitarr.like()`
		if name is None:
			new_name = tracer.get_var_name(depth=src_loc_at + 2,
				default=None)
		else:
			new_name = name

		kw = {
			"shape": other.shape(),
			"name": new_name,
			"use_parent_name": field_name_calc_upn(
				other=other,
				use_parent_name=use_parent_name,
				in_like=True,
			),
			"src_loc_at": src_loc_at,
			"in_like": True
		}

		kw.update(kwargs)

		return Splitintfarr(**kw)
	#--------
	def lst(self):
		return self.__lst
	def shape(self):
		return self.__shape
	def extra_args_name(self):
		return self.__extra_args_name
	def extra_args_use_parent_name(self):
		return self.__extra_args_use_parent_name
	def extra_args_src_loc_at(self):
		return self.__extra_args_src_loc_at
	#--------
	def __getitem__(self, key):
		return self.lst()[key]
	#--------
	#def __len__(self):
	#	return len(self.as_value())
	def __iter__(self):
		for item in self.lst():
			yield item
	def __list__(self):
		return self.lst()
	#--------
	def flattened(
		self,
		*,
		w_pdir: bool=False, # with `PortDir`
	):
		ret = []

		for val in self:
			if (
				# Elements of `Splitintfarr` can only be a
				# `Splitintf` or `Splitintfarr`, but not regular
				# `Value`/`ValueCastable` objects
				isinstance(val, Splitintf)
				or isinstance(val, Splitintfarr)
			):
				temp = val.flattened(w_pdir=w_pdir)
				for elem in temp:
					ret.append(elem)
			else:
				#ret.append(val)
				# NOTE: Hopefully this won't happen!
				raise TypeError(psconcat(
					"Invalid `val` `{!r}`".format(val)
				))

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
