#!/usr/bin/env python3

#from enum import Enum, auto
import enum as pyenum

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum

#from libcheesevoyage import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.pipeline_mods import *
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
# TODO: convert these to use of `Splitintf`
## Write Splitrec layout (source (host) output, dest (device) input)
# Write Splitrec layout (host output, device input)
#class IntrcnLcvWriteH2dDataLayt(dict):
#	#--------
#	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
#		#--------
#		#self.__ADDR_WIDTH = ADDR_WIDTH
#		#self.__DATA_WIDTH = DATA_WIDTH
#		#--------
#		layt = {}
#		# Write address (host output, device input)
#		layt["addr"] = FieldInfo(
#			ADDR_WIDTH,
#			name="wh2d_addr", prefix=name_prefix,
#		)
#		# Write address handshake request (host output, device input)
#		layt["areq"] = FieldInfo(
#			1, name="wh2d_areq", prefix=name_prefix,
#		)
#
#		# Write burst width (host output, device input)
#		layt["awidth"] = FieldInfo(
#			#Shape.cast(IntrcnLcvBstWidth),
#			IntrcnLcvBstWidth.as_shape(),
#			name="wh2d_awidth", prefix=name_prefix,
#		)
#		# Write burst length (host output, device input)
#		layt["asize"] = FieldInfo(
#			#Shape.cast(IntrcnLcvBstSize),
#			IntrcnLcvBstSize.as_shape(),
#			name="wh2d_asize", prefix=name_prefix,
#		)
#		# Write burst type (host output, device input)
#		layt["atype"] = FieldInfo(
#			#Shape.cast(IntrcnLcvBstType),
#			IntrcnLcvBstType.as_shape(),
#			name="wh2d_atype", prefix=name_prefix,
#		)
#
#		# Write data (host output, device input)
#		layt["data"] = FieldInfo(
#			DATA_WIDTH,
#			name="wh2d_data", prefix=name_prefix,
#		)
#
#		# Write data handshake request (host output, device input)
#		layt["dreq"] = FieldInfo(
#			1, name="wh2d_dreq", prefix=name_prefix,
#		)
#
#		# Write response handshake grant (host output, device input)
#		layt["rgnt"] = FieldInfo(
#			1, name="wh2d_rgnt", prefix=name_prefix,
#		)
#		super().__init__(layt)
#		#--------
#	#--------
#	#def ADDR_WIDTH(self):
#	#	return self.__ADDR_WIDTH
#	#def DATA_WIDTH(self):
#	#	return self.__DATA_WIDTH
#	#--------
### Write Splitrec layout (source (host) input, dest (device) output)
## Write Splitrec layout (host input, device output)
##class IntrcnLcvWriteD2h(Splitrec):
#class IntrcnLcvWriteD2hDataLayt(dict):
#	#--------
#	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
#		#--------
#		self.__ADDR_WIDTH = ADDR_WIDTH
#		self.__DATA_WIDTH = DATA_WIDTH
#		#--------
#		layt = {}
#		# Write address handshake grant (host input, device output)
#		layt["agnt"] = FieldInfo(
#			1, name="wd2h_agnt", prefix=name_prefix,
#		)
#
#		# Write data handshake grant (host input, device output)
#		layt["dgnt"] = FieldInfo(
#			1, name="wd2h_dgnt", prefix=name_prefix,
#		)
#
#		# Write response (host input, device output)
#		layt["resp"] = FieldInfo(
#			#Shape.cast(IntrcnLcvResp),
#			IntrcnLcvResp.as_shape(),
#			name="wd2h_resp", prefix=name_prefix,
#		)
#
#		# Write response handshake request (host input, device out)
#		layt["rreq"] = FieldInfo(
#			1, name="wd2h_rreq", prefix=name_prefix,
#		)
#		super().__init__(layt)
#		#--------
#	#--------
#	def ADDR_WIDTH(self):
#		return self.__ADDR_WIDTH
#	def DATA_WIDTH(self):
#		return self.__DATA_WIDTH
#	#--------
#
### Read Splitrec layout (source (host) output, dest (device) input)
## Read Splitrec layout (host output, device input)
#class IntrcnLcvReadH2dLayt(dict):
#	#--------
#	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
#		#--------
#		self.__ADDR_WIDTH = ADDR_WIDTH
#		self.__DATA_WIDTH = DATA_WIDTH
#		#--------
#		layt = {}
#		# Read address (host output, device input)
#		layt["addr"] = FieldInfo(
#			self.ADDR_WIDTH(),
#			name="rh2d_addr", prefix=name_prefix,
#		)
#
#		# Read address handshake request (host output, device input)
#		layt["areq"] = FieldInfo(
#			1, name="rh2d_areq", prefix=name_prefix,
#		)
#
#		# Read burst width (host output, device input)
#		layt["awidth"] = FieldInfo(
#			#shape=Shape.cast(IntrcnLcvBstWidth),
#			IntrcnLcvBstWidth.as_shape(),
#			name="rh2d_awidth", prefix=name_prefix,
#		)
#		# Read burst length (host output, device input)
#		layt["asize"] = FieldInfo(
#			#shape=Shape.cast(IntrcnLcvBstSize),
#			IntrcnLcvBstSize.as_shape(),
#			name="rh2d_asize", prefix=name_prefix,
#		)
#		# Read burst type (host output, device input)
#		layt["atype"] = FieldInfo(
#			#shape=Shape.cast(IntrcnLcvBstType),
#			IntrcnLcvBstType.as_shape(),
#			name="rh2d_atype", prefix=name_prefix,
#		)
#
#		# Read data/response handshake request (host output, device input)
#		layt["drgnt"] = FieldInfo(
#			1, name="rh2d_drgnt", prefix=name_prefix,
#		)
#
#		super().__init__(layt)
#		#--------
#	#--------
#	def ADDR_WIDTH(self):
#		return self.__ADDR_WIDTH
#	def DATA_WIDTH(self):
#		return self.__DATA_WIDTH
#	#--------
## Read Splitrec layout (host input, device output)
#class IntrcnLcvReadD2hLayt(dict):
#	#--------
#	def __init__(self, ADDR_WIDTH, DATA_WIDTH, *, name_prefix=""):
#		#--------
#		self.__ADDR_WIDTH = ADDR_WIDTH
#		self.__DATA_WIDTH = DATA_WIDTH
#		#--------
#		layt = {}
#		# Read address handshake grant (host input, device output)
#		layt["agnt"] = FieldInfo(
#			1, name="rd2h_agnt", prefix=name_prefix,
#		)
#
#		# Read data (host input, device output)
#		layt["data"] = FieldInfo(
#			self.DATA_WIDTH(), name="rd2h_data", prefix=name_prefix,
#		)
#
#		# Read data/response handshake request (host input, device output)
#		layt["drreq"] = FieldInfo(
#			1, name="rd2h_drreq", prefix=name_prefix,
#		)
#
#		# Read response (host input, device output)
#		layt["resp"] = FieldInfo(
#			#shape=Shape.cast(IntrcnLcvResp),
#			IntrcnLcvResp.as_shape(),
#			name="rd2h_resp", prefix=name_prefix,
#		)
#		super().__init__(layt)
#		#--------
#	#--------
#	def ADDR_WIDTH(self):
#		return self.__ADDR_WIDTH
#	def DATA_WIDTH(self):
#		return self.__DATA_WIDTH
#	#--------
#--------
def _intrcn_lcv_check_widths(ADDR_WIDTH, DATA_WIDTH):
	if not isinstance(ADDR_WIDTH, int):
		raise TypeError("`ADDR_WIDTH`, `{!r}`, must be of type `int`"
			.format(ADDR_WIDTH))
	if (
		ADDR_WIDTH != 16 and ADDR_WIDTH != 32 and ADDR_WIDTH != 64
	):
		raise ValueError(("`ADDR_WIDTH`, `{!r}`, must be of value "
			+ "16, 32, or 64").format(ADDR_WIDTH))

	if not isinstance(DATA_WIDTH, int):
		raise TypeError("`DATA_WIDTH`, `{!r}`, must be of type `int`"
			.format(DATA_WIDTH))
	if (
		DATA_WIDTH != 8 and DATA_WIDTH != 16
		and DATA_WIDTH != 32 and DATA_WIDTH != 64
		and DATA_WIDTH != 128 and DATA_WIDTH != 256
		and DATA_WIDTH != 512 and DATA_WIDTH != 1024
	):
		raise ValueError(("When `DATA_WIDTH`, `{!r}`, must be one of "
			+ "the following values: 8, 16, 32, 64, 128, 256, 512, "
			+ "or 1024").format(DATA_WIDTH))
# Write `Splitintf` layout (host output, device input)
class IntrcnLcvWriteH2dDataLayt(dict):
	def __init__(
		self, ADDR_WIDTH, DATA_WIDTH,
	):
		shape = {}

		# Write address (host output, device input)
		shape["addr"] = FieldInfo(
			ADDR_WIDTH, name="wh2d_addr",
		)
		shape["data"] = FieldInfo(
			DATA_WIDTH, name="wh2d_data",
		)
		shape["width"] = FieldInfo(
			IntrcnLcvBstWidth.as_shape(), name="wh2d_width",
		)
		shape["size"] = FieldInfo(
			IntrcnLcvBstSize.as_shape(), name="wh2d_size",
		)
		shape["type"] = FieldInfo(
			IntrcnLcvBstType.as_shape(), name="wh2d_type",
		)

		super().__init__(shape)
# Write `Splitintf` layout (host input, device output)
class IntrcnLcvWriteD2hDataLayt(dict):
	def __init__(
		self, #ADDR_WIDTH, DATA_WIDTH,
	):
		shape = {}
		shape["resp"] = FieldInfo(
			IntrcnLcvResp.as_shape(), name="w2dh_resp",
		)
		super().__init__(shape)
#def INTRCN_LCV_WRITE_DEF_TAG_DCT():
#	return {
#		"from": None,
#		"to": None,
#	}
class IntrcnLcvWriteIshape(IntfShape):
	def __init__(
		self, ADDR_WIDTH, DATA_WIDTH,
		*,
		in_device: bool, # `True` is considered `in_from`
		#name_dct: dict,
		tag_dct: dict,
	):
		#--------
		return IntrcnLcvWriteIshape.mk_fromto_shape(
			ADDR_WIDTH=ADDR_WIDTH, DATA_WIDTH=DATA_WIDTH,
			in_device=in_device,
			#name_dct=name_dct,
			tag_dct=tag_dct
		)
		#--------
	@staticmethod
	def mk_fromto_shape(
		ADDR_WIDTH, DATA_WIDTH,
		*,
		in_device: bool,
		#name_dct: dict,
		tag_dct: dict
	):
		_intrcn_lcv_check_widths(
			ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH
		)
		return PipeSkidBufIshape.mk_fromto_shape(
			FromDataLayt=IntrcnLcvWriteD2hDataLayt(),
			ToDataLayt=IntrcnLcvWriteH2dDataLayt(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH,
			),
			in_from=in_device,
			#name_dct=name_dct,
			name_dct={
				"from": "d2h",
				"to": "h2d",
			},
			tag_dct=tag_dct,
		)
	#@staticmethod
	#def def_tag_dct():
	#	return INTRCN_LCV_WRITE_DEF_TAG_DCT()
#--------
# Read `Splitintf` layout (host output, device input)
class IntrcnLcvReadH2dDataLayt(dict):
	def __init__(
		self, ADDR_WIDTH, #DATA_WIDTH,
	):
		shape = {}

		# Read address (host output, device input)
		shape["addr"] = FieldInfo(
			ADDR_WIDTH, name="wh2d_addr",
		)
		# Read burst width (host output, device input)
		shape["width"] = FieldInfo(
			IntrcnLcvBstWidth.as_shape(), name="wh2d_width",
		)

		# Read burst length (host output, device input)
		shape["size"] = FieldInfo(
			IntrcnLcvBstSize.as_shape(), name="wh2d_size",
		)

		# Read burst type (host output, device input)
		shape["type"] = FieldInfo(
			IntrcnLcvBstType.as_shape(), name="wh2d_type",
		)

		super().__init__(shape)
# Read `Splitintf` layout (host input, device output)
class IntrcnLcvReadD2hDataLayt(dict):
	def __init__(
		self, #ADDR_WIDTH,
		DATA_WIDTH,
	):
		shape = {}

		# Read data (host input, device output)
		shape["data"] = FieldInfo(
			DATA_WIDTH, name="wh2d_data",
		)

		# Read response (host input, device output)
		shape["resp"] = FieldInfo(
			IntrcnLcvResp.as_shape(), name="w2dh_resp",
		)
		super().__init__(shape)
class IntrcnLcvReadIshape(IntfShape):
	def __init__(
		self, ADDR_WIDTH, DATA_WIDTH,
		*,
		in_device: bool, # `True` is considered `in_from`
		#name_dct: dict,
		tag_dct: dict,
	):
		#--------
		return IntrcnLcvReadIshape.mk_fromto_shape(
			ADDR_WIDTH=ADDR_WIDTH, DATA_WIDTH=DATA_WIDTH,
			in_device=in_device,
			#name_dct=name_dct,
			tag_dct=tag_dct
		)
		#--------
	@staticmethod
	def mk_fromto_shape(
		ADDR_WIDTH, DATA_WIDTH,
		*,
		in_device: bool,
		#name_dct: dict,
		tag_dct: dict
	):
		_intrcn_lcv_check_widths(
			ADDR_WIDTH=ADDR_WIDTH,
			DATA_WIDTH=DATA_WIDTH
		)
		return PipeSkidBufIshape.mk_fromto_shape(
			FromDataLayt=IntrcnLcvReadD2hDataLayt(
				DATA_WIDTH=DATA_WIDTH,
			),
			ToDataLayt=IntrcnLcvReadH2dDataLayt(
				ADDR_WIDTH=ADDR_WIDTH,
			),
			in_from=in_device,
			#name_dct=name_dct,
			name_dct={
				"from": "d2h",
				"to": "h2d",
			},
			tag_dct=tag_dct,
		)
	#@staticmethod
	#def def_tag_dct():
	#	return INTRCN_LCV_WRITE_DEF_TAG_DCT()
#--------
class IntrcnLcvNodeIshape(IntfShape):
	#--------
	def __init__(
		self, ADDR_WIDTH, DATA_WIDTH,
		#*, name_prefix=""
		*,
		in_device: bool,
		#tag_dct={
		#	"from": None,
		#	"to": None,
		#},
		#wr_name_dct: dict,
		#rd_name_dct: dict,
		#tag_dct: dict={
		#	"wr": None,
		#	"rd": None,
		#},
		tag,
		wr_tag_dct: dict,
		rd_tag_dct: dict,
	):
		#--------
		#if len(name_prefix) != 0:
		#	name_prefix += "_"
		#--------
		#if not isinstance(ADDR_WIDTH, int):
		#	raise TypeError("`ADDR_WIDTH`, `{!r}`, must be of type `int`"
		#		.format(ADDR_WIDTH))
		#if (
		#	ADDR_WIDTH != 16 and ADDR_WIDTH != 32 and ADDR_WIDTH != 64
		#):
		#	raise ValueError(("`ADDR_WIDTH`, `{!r}`, must be of value "
		#		+ "16, 32, or 64").format(ADDR_WIDTH))
		#self.__ADDR_WIDTH = ADDR_WIDTH

		#if not isinstance(DATA_WIDTH, int):
		#	raise TypeError("`DATA_WIDTH`, `{!r}`, must be of type `int`"
		#		.format(DATA_WIDTH))
		#if (
		#	DATA_WIDTH != 8 and DATA_WIDTH != 16
		#	and DATA_WIDTH != 32 and DATA_WIDTH != 64
		#	and DATA_WIDTH != 128 and DATA_WIDTH != 256
		#	and DATA_WIDTH != 512 and DATA_WIDTH != 1024
		#):
		#	raise ValueError(("When `DATA_WIDTH`, `{!r}`, must be one of "
		#		+ "the following values: 8, 16, 32, 64, 128, 256, 512, "
		#		+ "or 1024").format(DATA_WIDTH))
		#self.__DATA_WIDTH = int(DATA_WIDTH)
		#_intrcn_lcv_check_widths(
		#	ADDR_WIDTH=ADDR_WIDTH, DATA_WIDTH=DATA_WIDTH,
		#)
		#--------
		#wh2d = IntrcnLcvWriteH2dDataLayt
		#self.wh2d = Splitrec(
		#	IntrcnLcvWriteH2dLayt(
		#		ADDR_WIDTH=ADDR_WIDTH, DATA_WIDTH=DATA_WIDTH,
		#		name_prefix=name_prefix
		#	),
		#	use_parent_name=False
		#)

		#self.wd2h = Splitrec(
		#	IntrcnLcvWriteD2hLayt(
		#		ADDR_WIDTH=ADDR_WIDTH,
		#		DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
		#	),
		#	use_parent_name=False
		#)

		#self.rh2d = Splitrec(
		#	IntrcnLcvReadH2dLayt(
		#		ADDR_WIDTH=ADDR_WIDTH,
		#		DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
		#	),
		#	use_parent_name=False
		#)

		#self.rd2h = Splitrec(
		#	IntrcnLcvReadD2hLayt(
		#		ADDR_WIDTH=ADDR_WIDTH,
		#		DATA_WIDTH=DATA_WIDTH, name_prefix=name_prefix
		#	),
		#	use_parent_name=False
		#)
		#--------
		shape = {
			"wr": IntrcnLcvWriteIshape(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH,
				in_device=in_device,
				tag_dct=wr_tag_dct,
			),
			"rd": IntrcnLcvReadIshape(
				ADDR_WIDTH=ADDR_WIDTH,
				DATA_WIDTH=DATA_WIDTH,
				in_device=in_device,
				tag_dct=rd_tag_dct,
			),
		}
		super().__init__(
			shape=shape,
			modport=None,
			tag=tag,
		)
		#--------
	#--------
	#def ADDR_WIDTH(self):
	#	return self.__ADDR_WIDTH
	#def DATA_WIDTH(self):
	#	return self.__DATA_WIDTH
	#--------
	@staticmethod
	def decode_bst_len(to_decode):
		return to_decode + 0x1
	#--------
