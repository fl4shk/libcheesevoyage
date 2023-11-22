#!/usr/bin/env python3

import sys
from math import log2, ceil

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
def clog2(x):
	return ceil(log2(x))

extraCycleWidth = 1

tileRshift = 0
#tilePxWidth = clog2(2)
tilePxWidth = clog2(4)
tileDblPxWidth = tilePxWidth + 1

gridIdxWidth = 1
#lineMemSize = 1 << 4
lineMemSize = 1 << 7
#mask = (1 << (tilePxWidth + gridIdxWidth)) - 1
#posX = 7
#posX = list(range(2, 5 + 1))
#posX = 2
posX = 0

#tilePxShift = 0
#tilePxMask = (1 << tilePxWidth) - 1
#tilePxShiftedMask = tilePxMask << tilePxShift
#tileX = 3

#gridIdxShift = tilePxShift + tilePxWidth
gridIdxShift = extraCycleWidth + tileRshift
gridIdxMask = (1 << gridIdxWidth) - 1
gridIdxShiftedMask = gridIdxMask << gridIdxShift

tileDblPxShift = gridIdxShift
tileDblPxMask = (1 << tileDblPxWidth) - 1
tileDblPxShiftedMask = tileDblPxMask << tileDblPxShift

##gridIdxShift = tilePxShift + tilePxWidth
#gridIdxShift = tileDblPxShift + tileDblPxWidth
#gridIdxMask = (1 << gridIdxWidth) - 1
#gridIdxShiftedMask = gridIdxMask << gridIdxShift

#subLineMemAddrShift = gridIdxShift + gridIdxWidth - 1
#subLineMemAddrMask = (lineMemSize - 1) >> subLineMemAddrShift
#subLineMemAddrShiftedMask = subLineMemAddrMask << subLineMemAddrShift

print(
	f"tilePxWidth:{tilePxWidth} "
	+ f"tileDblPxWidth:{tileDblPxWidth} "
	+ f"gridIdxWidth:{gridIdxWidth} "
	+ f"lineMemSize:{lineMemSize} "
	#+ f"mask:{mask} "
)
#print(
#	f"tilePxShift:{tilePxShift} "
#	+ f"tilePxMask:{hex(tilePxMask)[2:]} "
#	+ f"tilePxShiftedMask:{hex(tilePxShiftedMask)[2:]} "
#)
print(
	f"tileDblPxShift:{tileDblPxShift} "
	+ f"tileDblPxMask:{hex(tileDblPxMask)[2:]} "
	+ f"tileDblPxShiftedMask:{hex(tileDblPxShiftedMask)[2:]} "
)
print(
	f"gridIdxShift:{gridIdxShift} "
	+ f"gridIdxMask:{hex(gridIdxMask)[2:]} "
	+ f"gridIdxShiftedMask:{hex(gridIdxShiftedMask)[2:]} "
)
#print(
#	f"subLineMemAddrShift:{subLineMemAddrShift} "
#	+ f"subLineMemAddrMask:{hex(subLineMemAddrMask)[2:]} "
#	+ f"subLineMemAddrShiftedMask:{hex(subLineMemAddrShiftedMask)[2:]} "
#)

#for posX in range(lineMemSize):
#for i in range(1 << tilePxWidth):
#	##for gridIdx in range(1 << gridIdxWidth):
#	#maskVal = posX & mask
#	##maskValHex = hex(maskVal)
#	##posXHex = hex(posX)
#	#print(
#	#	f"x:{hex(posX)[2:]} m:{hex(maskVal)[2:]}"
#	#)
#	for gridIdx in range(1 << gridIdxWidth):
#		#maskVal = (poxX + i) & 
#		posXPlusIGridIdx = (
#			((posX + i) & gridIdxShiftedMask) >> gridIdxShift
#		)
#		posXPlusITilePx = (
#			((posX + i) & tilePxShiftedMask) >> tilePxShift
#		)
#		#posXPlusISubLineMemAddr = (
#		#	((posX + i) & subLineMemAddrShiftedMask) >> subLineMemAddrShift
#		#)
#		#print(
#		#	f"pos:{hex(posX + i)[2:]} "
#		#	+ f"gridIdx:{hex(gridIdx)[2:]} "
#		#	+ f"posGridIdx:{hex(posXPlusIGridIdx)[2:]} "
#		#	+ f"gridEq:{hex(posXPlusIGridIdx == gridIdx)} "
#		#	+ f"posTilePx:{hex(posXPlusITilePx)[2:]} "
#		#	+ f"posSubLineMemAddr:{hex(posXPlusISubLineMemAddr)[2:]} "
#		#)
