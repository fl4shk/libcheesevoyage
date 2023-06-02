#!/usr/bin/env python3

from enum import Enum, auto
from collections import OrderedDict
from amaranth import *
#import amaranth.tracer
#from amaranth.hdl.rec import Record, Layout
from amaranth.lib.data import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

#from libcheesevoyage.misc_util import *
from libcheesevoyage.misc_util import psconcat, sig_keep, Blank

class SigInfo:
	def __init__(
		self,
		basenm: str,
		shape,
		*,
		ObjKind=Signal,
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
		if (
			issubclass(self.ObjKind(), Signal)
			or issubclass(self.ObjKind(), View)
		):
			kw = {
				"reset": self.reset(),
				"attrs": self.attrs(),
			}
		else:
			kw = {}
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

#class Field(SigInfo):
#	def __init__(
#		self,
#		#info: SigInfo,
#		basenm: str,
#		shape,
#		*,
#		ObjKind=Signal,
#		reset=0,
#		attrs: str=sig_keep(),
#		prefix: str="",
#		suffix: str="",
#		**kwargs
#	):
#		super().__init__(
#			basenm=basenm,
#			shape=shape,
#			ObjKind=ObjKind,
#			reset=reset,
#			attrs=attrs,
#			**kwargs
#		)
#		#self.__info = info
#		#self.__basenm = basenm
#		self.__prefix = prefix
#		self.__suffix = suffix
#		#self.__kwargs = kwargs
#		#self.__sig = self.info().mk_sig(
#		#	basenm=basenm,
#		#	prefix=prefix,
#		#	suffix=suffix,
#		#	kwargs=kwargs,
#		#)
#	#def info(self):
#	#	return self.__info
#	#def basenm(self):
#	#	return self.__basenm
#	def prefix(self):
#		return self.__prefix
#	def suffix(self):
#		return self.__suffix
#	#def kwargs(self):
#	#	return self.__kwargs
#	#def sig(self):
#	def mk_sig(self, **kwargs):
#		#return self.info().mk_sig
#		return self.mk_sig(
#			basenm=self.basenm(),
#			prefix=self.prefix(),
#			suffix=self.suffix(),
#			#**self.kwargs(),
#			**kwargs
#		)

#class SigCfgDict:
#	def __init__(
#		self,
#		dct: OrderedDict,
#	):
#		self.__dct = dct

