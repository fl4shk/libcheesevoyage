#!/usr/bin/env python3

from enum import Enum, auto

from nmigen import *

from libcheesevoyage import *
#--------
class IntrcnLcvBstWidth(Enum):
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
class IntrcnLcvBstSize(Enum):
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
class IntrcnLcvBstType(Enum):
	FIXED = 0b0
	INCR = 0b1
#--------
class IntrcnLcvResp(Enum):
	OKAY = 0b00
	RESERVED = 0b01
	SLVERR = 0b10
	DECERR = 0b11
#--------
def intrcn_lcv_blen_width():
	return 8

# Write Splitrec (host output, device input)
class IntrcnLcvWriteH2d(Splitrec):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		# Write address (host output, device input)
		self.addr = Signal(self.ADDR_WIDTH(),
			name=psconcat(name_prefix, "wh2d_addr"))
		# Write address handshake request (host output, device input)
		self.areq = Signal(name=psconcat(name_prefix, "wh2d_areq"))

		# Write data (host output, device input)
		self.data = Signal(self.DATA_WIDTH(),
			name=psconcat(name_prefix, "wh2d_data"))

		# Write data handshake request (host output, device input)
		self.dreq = Signal(name=psconcat(name_prefix, "wh2d_dreq"))

		# Write burst width (host output, device input)
		self.bwidth = Signal(shape=Shape.cast(IntrcnLcvBstWidth),
			name=psconcat(name_prefix, "wh2d_bwidth"))
		# Write burst length (host output, device input)
		self.blen = Signal(intrcn_lcv_blen_width(),
			name=psconcat(name_prefix, "wh2d_blen"))
		# Write burst type (host output, device input)
		self.btype = Signal(shape=Shape.cast(IntrcnLcvBstType),
			name=psconcat(name_prefix, "wh2d_btype"))

		# Write response handshake grant (host output, device input)
		self.rgnt = Signal(name=psconcat(name_prefix, "wh2d_rgnt"))
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
# Write Splitrec (host input, device output)
class IntrcnLcvWriteD2h(Splitrec):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		# Write address handshake grant (host input, device output)
		self.agnt = Signal(name=psconcat(name_prefix, "wd2h_agnt"))

		# Write data handshake grant (host input, device output)
		self.dgnt = Signal(name=psconcat(name_prefix, "wd2h_dgnt"))

		# Write response (host input, device output)
		self.resp = Signal(shape=Shape.cast(IntrcnLcvResp),
			name=psconcat(name_prefix, "wd2h_resp"))

		# Write response handshake request (host input, device out)
		self.rreq = Signal(name=psconcat(name_prefix, "wd2h_rreq"))
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------

# Read Splitrec (host output, device input)
class IntrcnLcvReadH2d(Splitrec):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		# Read address (host output, device input)
		self.addr = Signal(self.ADDR_WIDTH(),
			name=psconcat(name_prefix, "rh2d_addr"))

		# Read address handshake request (host output, device input)
		self.areq = Signal(name=psconcat(name_prefix, "rh2d_areq"))

		# Read burst width (host output, device input)
		self.bwidth = Signal(shape=Shape.cast(IntrcnLcvBstWidth),
			name=psconcat(name_prefix, "rh2d_bwidth"))
		# Read burst length (host output, device input)
		self.blen = Signal(intrcn_lcv_blen_width(),
			name=psconcat(name_prefix, "rh2d_blen"))
		# Read burst type (host output, device input)
		self.btype = Signal(shape=Shape.cast(IntrcnLcvBstType),
			name=psconcat(name_prefix, "rh2d_btype"))

		# Read data/response handshake request (host output, device input)
		self.drgnt = Signal(name=psconcat(name_prefix, "rh2d_drgnt"))
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	#--------
# Read Splitrec (host input, device output)
class IntrcnLcvReadD2h(Splitrec):
	#--------
	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
		#--------
		self.__ADDR_WIDTH = ADDR_WIDTH
		self.__DATA_WIDTH = DATA_WIDTH
		#--------
		# Read address handshake grant (host input, device output)
		self.agnt = Signal(name=psconcat(name_prefix, "rd2h_agnt"))

		# Read data (host input, device output)
		self.data = Signal(self.DATA_WIDTH(),
			name=psconcat(name_prefix, "rd2h_data"))

		# Read data/response handshake request (host input, device output)
		self.drreq = Signal(name=psconcat(name_prefix, "rd2h_drreq"))

		# Read response (host input, device output)
		self.resp = Signal(shape=Shape.cast(IntrcnLcvResp),
			name=psconcat(name_prefix, "rd2h_resp"))
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
		self.wh2d = IntrcnLcvWriteH2d(ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix)

		self.wd2h = IntrcnLcvWriteD2h(ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix)

		self.rh2d = IntrcnLcvReadH2d(ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix)

		self.rd2h = IntrcnLcvReadD2h(ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix)
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

