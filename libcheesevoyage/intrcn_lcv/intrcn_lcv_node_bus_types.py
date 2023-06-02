#!/usr/bin/env python3

#from enum import Enum, auto
import enum as pyenum

from amaranth import *
from amaranth.lib import enum

from libcheesevoyage import *
#--------
class IntrcnLcvBstWidth(enum.Enum, shape=3):
	BW_8 = 0b000
	BW_16 = 0b001
	BW_32 = 0b010
	BW_64 = 0b011
	BW_128 = 0b100
	BW_256 = 0b101
	BW_512 = 0b110
	BW_1024 = 0b111

	def __int__(self):
		return 8 << self.value
#--------
class IntrcnLcvBstSize(enum.Enum, shape=3):
	BS_1 = 0b000
	BS_2 = 0b001
	BS_4 = 0b010
	BS_8 = 0b011
	BS_16 = 0b100
	BS_32 = 0b101
	BS_64 = 0b110
	BS_128 = 0b111

	def __int__(self):
		return 1 << self.value
class IntrcnLcvBstType(enum.Enum, shape=1):
	FIXED = 0b0
	INCR = 0b1
#--------
class IntrcnLcvResp(enum.Enum, shape=2):
	OKAY = 0b00
	RESERVED = 0b01
	SLVERR = 0b10
	DECERR = 0b11
#--------
#def intrcn_lcv_asize_width():
#	return 8
#--------
# Write Splitrec layout (source (host) output, dest (device) input)
#class IntrcnLcvWriteS2d(Splitrec):
class IntrcnLcvWriteS2dLayt(dict):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		layt = {}
		# Write address (source output, dest input)
		layt["addr"] = FieldInfo(
			self.ADDR_WIDTH(),
			name="ws2d_addr", prefix=name_prefix,
		)
		# Write address handshake request (source output, dest input)
		layt["areq"] = FieldInfo(
			1, name="ws2d_areq", prefix=name_prefix,
		)

		# Write burst width (source output, dest input)
		layt["awidth"] = FieldInfo(
			#Shape.cast(IntrcnLcvBstWidth),
			IntrcnLcvBstWidth.as_shape(),
			name="ws2d_awidth", prefix=name_prefix,
		)
		# Write burst length (source output, dest input)
		layt["asize"] = FieldInfo(
			#Shape.cast(IntrcnLcvBstSize),
			IntrcnLcvBstSize.as_shape(),
			name="ws2d_asize", prefix=name_prefix,
		)
		# Write burst type (source output, dest input)
		layt["atype"] = FieldInfo(
			#Shape.cast(IntrcnLcvBstType),
			IntrcnLcvBstType.as_shape(),
			name="ws2d_atype", prefix=name_prefix,
		)

		# Write data (source output, dest input)
		layt["data"] = FieldInfo(
			self.DATA_WIDTH(),
			name="ws2d_data", prefix=name_prefix,
		)

		# Write data handshake request (source output, dest input)
		layt["dreq"] = FieldInfo(
			1, name="ws2d_dreq", prefix=name_prefix,
		)

		# Write response handshake grant (source output, dest input)
		layt["rgnt"] = FieldInfo(
			1, name="ws2d_rgnt", prefix=name_prefix,
		)
		super().__init__(layt)
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
# Write Splitrec layout (source (host) input, dest (device) output)
#class IntrcnLcvWriteD2s(Splitrec):
class IntrcnLcvWriteD2sLayt(dict):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		layt = {}
		# Write address handshake grant (source input, dest output)
		layt["agnt"] = FieldInfo(
			1, name="wd2s_agnt", prefix=name_prefix,
		)

		# Write data handshake grant (source input, dest output)
		layt["dgnt"] = FieldInfo(
			1, name="wd2s_dgnt", prefix=name_prefix,
		)

		# Write response (source input, dest output)
		layt["resp"] = FieldInfo(
			#Shape.cast(IntrcnLcvResp),
			IntrcnLcvResp.as_shape(),
			name="wd2s_resp", prefix=name_prefix,
		)

		# Write response handshake request (source input, device out)
		layt["rreq"] = FieldInfo(
			1, name="wd2s_rreq", prefix=name_prefix,
		)
		super().__init__(layt)
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------

# Read Splitrec layout (source (host) output, dest (device) input)
#class IntrcnLcvReadS2d(Splitrec):
class IntrcnLcvReadS2dLayt(dict):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		layt = {}
		# Read address (source output, dest input)
		layt["addr"] = FieldInfo(
			self.ADDR_WIDTH(),
			name="rs2d_addr", prefix=name_prefix,
		)

		# Read address handshake request (source output, dest input)
		layt["areq"] = FieldInfo(
			1, name="rs2d_areq", prefix=name_prefix,
		)

		# Read burst width (source output, dest input)
		layt["awidth"] = FieldInfo(
			#shape=Shape.cast(IntrcnLcvBstWidth),
			IntrcnLcvBstWidth.as_shape(),
			name="rs2d_awidth", prefix=name_prefix,
		)
		# Read burst length (source output, dest input)
		layt["asize"] = FieldInfo(
			#shape=Shape.cast(IntrcnLcvBstSize),
			IntrcnLcvBstSize.as_shape(),
			name="rs2d_asize", prefix=name_prefix,
		)
		# Read burst type (source output, dest input)
		layt["atype"] = FieldInfo(
			#shape=Shape.cast(IntrcnLcvBstType),
			IntrcnLcvBstType.as_shape(),
			name="rs2d_atype", prefix=name_prefix,
		)

		# Read data/response handshake request (source output, dest input)
		layt["drgnt"] = FieldInfo(
			1, name="rs2d_drgnt", prefix=name_prefix,
		)

		super().__init__(layt)
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
# Read Splitrec layout (source input, dest output)
#class IntrcnLcvReadD2s(Splitrec):
class IntrcnLcvReadD2sLayt(dict):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		layt = {}
		# Read address handshake grant (source input, dest output)
		layt["agnt"] = FieldInfo(
			1, name="rd2s_agnt", prefix=name_prefix,
		)

		# Read data (source input, dest output)
		layt["data"] = FieldInfo(
			self.DATA_WIDTH(), name="rd2s_data", prefix=name_prefix,
		)

		# Read data/response handshake request (source input, dest output)
		layt["drreq"] = FieldInfo(
			1, name="rd2s_drreq", prefix=name_prefix,
		)

		# Read response (source input, dest output)
		layt["resp"] = FieldInfo(
			#shape=Shape.cast(IntrcnLcvResp),
			IntrcnLcvResp.as_shape(),
			name="rd2s_resp", prefix=name_prefix,
		)
		super().__init__(layt)
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
#--------
class IntrcnLcvNodeBus:
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		if len(name_prefix) != 0:
			name_prefix += "_"
		#--------
		if not isinstance(ADDR_WIDTH, int):
			raise TypeError("`ADDR_WIDTH`, `{!r}`, must be of type `int`"
				.format(ADDR_WIDTH))
		if (ADDR_WIDTH != 16) and (ADDR_WIDTH != 32) \
			and (ADDR_WIDTH != 64):
			raise ValueError(("`ADDR_WIDTH`, `{!r}`, must be of value "
				+ "16, 32, or 64").format(ADDR_WIDTH))
		self.__ADDR_WIDTH = ADDR_WIDTH

		if (not isinstance(DATA_WIDTH, int)):
			raise TypeError("`DATA_WIDTH`, `{!r}`, must be of type `int`"
				.format(DATA_WIDTH))
		if (DATA_WIDTH != 8) and (DATA_WIDTH != 16) \
			and (DATA_WIDTH != 32) and (DATA_WIDTH != 64) \
			and (DATA_WIDTH != 128) and (DATA_WIDTH != 256) \
			and (DATA_WIDTH != 512) and (DATA_WIDTH != 1024):
			raise ValueError(("When `DATA_WIDTH`, `{!r}`, must be one of "
				+ "the following values: 8, 16, 32, 64, 128, 256, 512, "
				+ "or 1024").format(DATA_WIDTH))
		self.__DATA_WIDTH = int(DATA_WIDTH)
		#--------
		self.ws2d = Splitrec(
			IntrcnLcvWriteS2dLayt(
				ADDR_WIDTH=ADDR_WIDTH, DATA_WIDTH=DATA_WIDTH,
				name_prefix=name_prefix
			),
			use_parent_name=False
		)

		self.wd2s = Splitrec(
			IntrcnLcvWriteD2sLayt(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
			),
			use_parent_name=False
		)

		self.rs2d = Splitrec(
			IntrcnLcvReadS2dLayt(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
			),
			use_parent_name=False
		)

		self.rd2s = Splitrec(
			IntrcnLcvReadD2sLayt(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
			),
			use_parent_name=False
		)
		#--------
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
	def decode_bst_len(self, to_decode):
		return to_decode + 0x1
	#--------

