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
		shapelayt,
		*,
		ObjKind=Signal,
		reset=0,
		attrs: str=sig_keep(),
		#prefix: str="",
	):
		self.__basenm = basenm
		self.__shapelayt = shapelayt
		self.__ObjKind = ObjKind
		self.__reset = reset
		self.__attrs = attrs
	@staticmethod
	def like(other, **kwargs):
		kw = {
			"basenm": other.basenm(),
			"shapelayt": other.shapelayt(),
			"ObjKind": other.ObjKind(),
			"reset": other.reset(),
			"attrs": other.attrs(),
		}
		kw.update(kwargs)

		return SigInfo(**kw)
	@staticmethod
	def like_sig(sig, **kwargs):
		assert isinstance(sig, Signal)

		kw = {
			"basenm": sig.name,
			"shapelayt": sig.shape(),
			"ObjKind": Signal,
			"reset": sig.reset,
			"attrs": sig.attrs,
		}
		kw.update(kwargs)

		return SigInfo(**kw)


	def basenm(self):
		return self.__name
	def shapelayt(self):
		return self.__shapelayt
	def ObjKind(self):
		return self.__ObjKind
	def reset(self):
		return self.__reset
	def attrs(self):
		return self.__attrs

	def mk_sig(
		self,
		*,
		basenm=None,
		prefix="",
		suffix=""
	):
		temp_basenm = self.basenm() if basenm == None else basenm 
		return self.ObjKind()(
			self.shapelayt(),
			name=psconcat(prefix, temp_basenm, suffix),
			reset=self.reset(),
			attrs=self.attrs(),
		)

#class SigCfgDict:
#	def __init__(
#		self,
#		dct: OrderedDict,
#	):
#		self.__dct = dct

