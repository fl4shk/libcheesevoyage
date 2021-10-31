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
		# Write Splitrec (host output, device input)
		self.who = self.wdi = Splitrec()

		# Write Splitrec (host input, device output)
		self.whi = self.wdo = Splitrec()

		# Read Splitrec (host output, device input)
		self.rho = self.rdi = Splitrec()

		# Read Splitrec (host input, device input)
		self.rhi = self.rdo = Splitrec()
		#--------
		# Write address (host output, device input)
		self.who.addr = Signal(self.ADDR_WIDTH(),
			name=psconcat(name_prefix, "who_addr"))

		# Write address handshake request (host output, device input)
		self.who.areq = Signal(name=psconcat(name_prefix, "who_areq"))
		# Write address handshake grant (host input, device output)
		self.whi.agnt = Signal(name=psconcat(name_prefix, "whi_agnt"))


		# Write data (host output, device input)
		self.who.data = Signal(self.DATA_WIDTH(),
			name=psconcat(name_prefix, "who_data"))

		# Write data handshake request (host output, device input)
		self.who.dreq = Signal(name=psconcat(name_prefix, "who_dreq"))
		# Write data handshake grant (host input, device output)
		self.whi.dgnt = Signal(name=psconcat(name_prefix, "whi_dgnt"))

		# Write burst width (host output, device input)
		self.who.bwidth = Signal(shape=Shape.cast(IntrcnLcvBstWidth),
			name=psconcat(name_prefix, "who_bwidth"))
		# Write burst length (host output, device input)
		self.who.blen = Signal(self.BLEN_WIDTH(),
			name=psconcat(name_prefix, "who_blen"))
		# Write burst type (host output, device input)
		self.who.btype = Signal(shape=Shape.cast(IntrcnLcvBstType),
			name=psconcat(name_prefix, "who_btype"))


		# Write response (host input, device output)
		self.whi.resp = Signal(shape=Shape.cast(IntrcnLcvResp),
			name=psconcat(name_prefix, "whi_resp"))

		# Write response handshake request (host input, device out)
		self.whi.rreq = Signal(name=psconcat(name_prefix, "whi_rreq"))
		# Write response handshake grant (host output, device input)
		self.who.rgnt = Signal(name=psconcat(name_prefix, "who_rgnt"))
		#--------
		# Read address (host output, device input)
		self.rho.addr = Signal(self.ADDR_WIDTH(),
			name=psconcat(name_prefix, "rho_addr"))

		# Read address handshake request (host output, device input)
		self.rho.areq = Signal(name=psconcat(name_prefix, "rho_areq"))
		# Read address handshake grant (host input, device output)
		self.rhi.agnt = Signal(name=psconcat(name_prefix, "rhi_agnt"))


		# Read data (host input, device output)
		self.rhi.data = Signal(self.DATA_WIDTH(),
			name=psconcat(name_prefix, "rhi_data"))

		# Read burst width (host output, device input)
		self.rho.bwidth = Signal(shape=Shape.cast(IntrcnLcvBstWidth),
			name=psconcat(name_prefix, "rho_bwidth"))
		# Read burst length (host output, device input)
		self.rho.blen = Signal(self.BLEN_WIDTH(),
			name=psconcat(name_prefix, "rho_blen"))
		# Read burst type (host output, device input)
		self.rho.btype = Signal(shape=Shape.cast(IntrcnLcvBstType),
			name=psconcat(name_prefix, "rho_btype"))

		# Read data/response handshake request (host input, device output)
		self.rhi.drreq = Signal(name=psconcat(name_prefix, "rhi_drreq"))
		# Read data/response handshake request (host output, device input)
		self.rho.drgnt = Signal(name=psconcat(name_prefix, "rho_drgnt"))

		# Read response (host input, device output)
		self.rhi.resp = Signal(shape=Shape.cast(IntrcnLcvResp),
			name=psconcat(name_prefix, "rhi_resp"))
		#--------
	#--------
	def ADDR_WIDTH(self):
		return self.__ADDR_WIDTH
	def DATA_WIDTH(self):
		return self.__DATA_WIDTH
	def BLEN_WIDTH():
		return 8
	#--------
	def decode_bst_len(self, to_decode):
		return to_decode + 0x1
	#--------

