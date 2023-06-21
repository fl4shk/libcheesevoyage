#!/usr/bin/env python3

from amaranth import *

#from libcheesevoyage import *
from libcheesevoyage.misc_util_running import (
	to_verilog, formal,
)
#from libcheesevoyage.math_lcv.long_div_mods import (
#	LongDivMultiCycle, LongDivPipelined,
#)
#from libcheesevoyage.math_lcv.long_div_iter_mods import (
#	LongDivConstants, LongUdivIterSync,
#)
from test_long_udiv_iter_mods import *
#--------
if __name__ == "__main__":
	formal(
	#to_verilog(
		#dut_mod=LongDivMultiCycle,
		dut_mod=LongDivPipelined,
		MAIN_WIDTH=6,
		DENOM_WIDTH=6,
		CHUNK_WIDTH=2,
		#MAIN_WIDTH=2,
		#DENOM_WIDTH=2,
		#CHUNK_WIDTH=1,
		signed_reset=0,
		#signed_reset=1,
		USE_PIPE_SKID_BUF=True,
		#USE_PIPE_SKID_BUF=False,
		#FORMAL=True,

		#dut_mod=WrapperLongUdivIterSync,
		##dut_mod=LongUdivIterSync,
		#constants=LongDivConstants(
		#	MAIN_WIDTH=2,
		#	DENOM_WIDTH=2,
		#	CHUNK_WIDTH=1,
		#	TAG_WIDTH=1,
		#	PIPELINED=True,
		#	USE_PIPE_SKID_BUF=True,
		#	#USE_PIPE_SKID_BUF=False,
		#	#FORMAL=True,
		#),
		#chunk_start_val=0,
		#intf_tag_dct={
		#	"next": "next",
		#	"prev": "prev",
		#},

		#dut_mod=WrapperPipeSkidBuf
	)
	#)
#--------
