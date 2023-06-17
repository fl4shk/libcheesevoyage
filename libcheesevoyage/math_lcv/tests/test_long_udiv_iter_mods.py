#!/usr/bin/env python3

from amaranth import *

#from libcheesevoyage import *
#from libcheesevoyage.misc_util_running import (
#	to_verilog, formal,
#)
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.pipeline_mods import *
from libcheesevoyage.math_lcv.long_div_mods import (
	LongDivMultiCycle, LongDivPipelined,
)
from libcheesevoyage.math_lcv.long_div_iter_mods import (
	LongDivConstants, LongUdivIterSyncIshape, LongUdivIterSync,
)
#--------
class WrapperLongUdivIterSyncBus:
	def __init__(
		self,
		constants: LongDivConstants,
		*,
		intf_tag_dct={
			"next": None,
			"prev": None,
		},
	):
		self.__bus = Splitintf(
			LongUdivIterSyncIshape(
				constants=constants,
				intf_tag_dct=intf_tag_dct,
				in_wrapper=True,
			)
		)
	@property
	def bus(self):
		return self.__bus
class WrapperLongUdivIterSync(Elaboratable):
	def __init__(
		self,
		constants: LongDivConstants,
		chunk_start_val: int,
		*,
		intf_tag_dct={
			"next": None,
			"prev": None,
		},
		FORMAL=None,
	):
		self.__constants = constants
		self.__chunk_start_val = chunk_start_val
		self.__intf_tag_dct = intf_tag_dct
		self.__bus = WrapperLongUdivIterSyncBus(
			constants=constants,
			intf_tag_dct=intf_tag_dct,
		)
	def bus(self):
		return self.__bus
	def elaborate(self, platform: str) -> Module:
		m = Module()
		bus = self.bus().bus

		m.submodules.udiv_its = udiv_its = LongUdivIterSync(
			constants=self.__constants,
			chunk_start_val=self.__chunk_start_val,
			intf_tag_dct=self.__intf_tag_dct,
			#intf_tag_dct={
			#	"next": "next",
			#	"prev": "prev",
			#},
		)
		f = open("debug-wrapper_udiv_iter_sync.txt.ignore", "w")
		bus.connect(
			other=udiv_its.bus().bus,
			m=m,
			kind=Splitintf.ConnKind.Parent2Child,
			f=f,
			use_tag=True,
			#use_tag=False,
			reduce_tag=False,
		)
		f.close()

		return m
#--------
class WrapperPipeSkidBufBus:
	def __init__(self, ishape):
		self.__bus = Splitintf(ishape, name="bus")
	@property
	def bus(self):
		return self.__bus
class WrapperPipeSkidBuf(Elaboratable):
	def __init__(self):
		ishape = PipeSkidBufIshape(
			inp_data_info=WrapperPipeSkidBuf.mk_data_info(PortDir.Inp),
			outp_data_info=WrapperPipeSkidBuf.mk_data_info(PortDir.Outp),
			OPT_INCLUDE_VALID_BUSY=(
				WrapperPipeSkidBuf.OPT_INCLUDE_VALID_BUSY()
			),
			OPT_INCLUDE_READY_BUSY=(
				WrapperPipeSkidBuf.OPT_INCLUDE_READY_BUSY()
			),
			tag_dct=WrapperPipeSkidBuf.mk_tag_dct(),
		)
		self.__bus = WrapperPipeSkidBufBus(ishape)
	def bus(self):
		return self.__bus
	@staticmethod
	def mk_data_info(pdir: PortDir):
		return SigInfo(
			basenm="wrap",
			suffix=(
				"_in"
				if pdir == PortDir.Inp
				else "_out"
			),
			#shape={
			#	key: FieldInfo(
			#		1, name=key, use_parent_name=True,
			#	)
			#	for key in ["r", "g", "b"]
			#},
			#shape=8,
			shape=FieldInfo(
				{
					"r": 8,
					"g": 8,
					"b": 8,
				},
				use_parent_name=True
			),
			use_parent_name=True,
		)
	@staticmethod
	def mk_tag_dct():
		return {
			"next": "next",
			"prev": "prev",
			"misc": "misc",
		}
	@staticmethod
	def OPT_INCLUDE_VALID_BUSY():
		return True
	@staticmethod
	def OPT_INCLUDE_READY_BUSY():
		return False
	def elaborate(self, platform: str) -> Module:
		m = Module()
		bus = self.bus().bus
		m.submodules.skid_buf = skid_buf = PipeSkidBuf(
			inp_data_info=WrapperPipeSkidBuf.mk_data_info(PortDir.Inp),
			outp_data_info=WrapperPipeSkidBuf.mk_data_info(PortDir.Outp),
			OPT_INCLUDE_VALID_BUSY=(
				WrapperPipeSkidBuf.OPT_INCLUDE_VALID_BUSY()
			),
			OPT_INCLUDE_READY_BUSY=(
				WrapperPipeSkidBuf.OPT_INCLUDE_READY_BUSY()
			),
			tag_dct=WrapperPipeSkidBuf.mk_tag_dct(),
		)
		#f = open("debug-wrapper_pipe_skid_buffer.txt.ignore", "w")
		#bus.connect(
		#	other=skid_buf.bus().bus,
		#	m=m,
		#	kind=Splitintf.ConnKind.Parent2Child,
		#	conn_lst_shrink=-1,
		#	#conn_lst_shrink=0,
		#	#f=f,
		#	use_tag=True,
		#	#use_tag=False,
		#	#reduce_tag=False,
		#)
		#f.close()
		PipeSkidBuf.connect_child(
			parent=m,
			parent_sb_bus=bus,
			child_sb_bus=skid_buf.bus().bus,
			use_tag=True,
			reduce_tag=True,
		)
		return m
#--------
