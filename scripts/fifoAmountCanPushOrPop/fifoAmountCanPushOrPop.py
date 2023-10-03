#!/usr/bin/env python3

import sys

#--------
class Blank:
	pass

def psconcat(*args):
	return str().join([str(arg) for arg in args])

def lsconcat(lst):
	#return str().join([str(elem) for elem in lst])
	return psconcat(*lst)

def fprintout(file, *args, flush=False):
	print(psconcat(*args), sep="", end="", file=file, flush=flush)

def printout(*args):
	fprintout(sys.stdout, *args)

def printerr(*args):
	fprintout(sys.stderr, *args)

def convert_enum_to_str(to_conv):
	return str(to_conv)[str(to_conv).find(".") + 1:]

def convert_str_to_enum_opt(to_conv, EnumT, STR_ENUM_MAP):
	if not (isinstance(to_conv, EnumT) or isinstance(to_conv, str)):
		raise TypeError(psconcat("convert_str_to_enum_opt() error: ",
			to_conv, " ", type(to_conv)))

	if isinstance(to_conv, EnumT):
		return to_conv
	else: # if isinstance(to_conv, str):
		if to_conv not in STR_ENUM_MAP:
			raise KeyError(to_conv)
		return STR_DIRECTION_MAP[to_conv]

def obj_err_str(obj, i=None, lst=None):
	if i is None:
		return psconcat("{!r}, {}".format(obj, type(obj)))
	else: # if i is not None:
		assert isinstance(i, int), \
			obj_err_str(i)
		assert lst is None or isinstance(lst, list), \
			obj_err_str(lst)

		if lst is None:
			return psconcat("{}, {!r}, {}".format(i, obj, type(obj)))
		else: # if isinstance(lst, list):
			return psconcat("{!r}, {}".format(lst, obj_err_str
				(obj, i, None)))
#--------
class NameDict:
	#--------
	def __init__(self, dct={}):
		self.__dct = dct
	#--------
	def dct(self):
		return self.__dct
	#--------
	def __getattr__(self, key):
		return self[key]
	def __getitem__(self, key):
		if NameDict.key_goes_in_dct(key):
			return self.__dct[key]
		else: # if not NameDict.key_goes_in_dct(key)
			return self.__dict__[key]

	def __setattr__(self, key, val):
		self[key] = val
	def __setitem__(self, key, val):
		if NameDict.key_goes_in_dct(key):
			self.__dct[key] = val
		else: # if not NameDict.key_goes_in_dct(key)
			self.__dict__[key] = val

	def __iadd__(self, val):
		if not (isinstance(val, list) or isinstance(val, tuple)):
			raise TypeError("`val`: type must be `list` or `tuple`: {}"
				.format(obj_err_str(val)))

		if isinstance(val, list):
			for item in val:
				if not isinstance(item, tuple):
					raise TypeError("`item`: type must be `tuple`: {}"
						.format(obj_err_str(item)))
				if not (len(item) == 2):
					raise ValueError("`item`: `len` must be 2: {}"
						.format(obj_err_str(item)))
				self[item[0]] = item[1]
		else: # if isinstance(val, tuple):
			if len(item) != 2:
				raise ValueError("`item`: `len` must be 2: {}"
					.format(len(item)))
			self[val[0]] = val[1]
	#--------
	@staticmethod
	def key_goes_in_dct(key):
		return isinstance(key, str) \
			and (len(key) > 0) \
			and key[0].isalpha()
	#--------
#--------
depth = 4
for head in range(depth):
	for tail in range(depth):
		depthMinus1 = depth - 1
		#someAmountCanPop = (
		#	head - tail
		#	if (head > tail)
		#	#else (depth - (tail - head))
		#	else (depth - (tail - head))
		#)

		#someAmountCanPush = (
		#	#abs(head - tail) % depth
		#	#if (head >= tail)
		#	#else (tail - head) % depth
		#	#(head - tail) % depth
		#	#head - tail
		#	(tail - head) % depth
		#	if (tail > head)
		#	else (head - tail) % depth
		#)
		#someAmountCanPop = (
		#	#U(f"$amountWidth'd$depthMinus1") - nextAmountCanPop
		#	#U(f"$amountWidth'd$depth")
		#	#depth
		#	#depth - someAmountCanPop
		#	-1
		#)

		#someAmountCanPop = ((head - tail) % depth)
		someAmountCanPop = (
			head - tail
			if (head >= tail)
			else (depth - (tail - head))
		)
		#someAmountCanPush = depth - someAmountCanPop
		someAmountCanPush = depth - 1 - someAmountCanPop

		#someAmountCanPush = (
		#	#(tail - head) % depth
		#	#(head - tail) % depth
		#)
		print("h:{} t:{} push:{} pop:{}".format(
			head, tail, someAmountCanPush, someAmountCanPop
		))
	print()
