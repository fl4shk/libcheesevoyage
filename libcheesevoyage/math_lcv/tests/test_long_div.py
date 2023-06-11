#!/usr/bin/env python3

from amaranth import *

#from libcheesevoyage import *
from libcheesevoyage.misc_util_running import (
	to_verilog, formal,
)
from libcheesevoyage.math_lcv.long_div_mods import LongDivPipelined
#--------
if __name__ == "__main__":
	formal(
	#to_verilog
		#dut_mod=LongDivMultiCycle,
		dut_mod=LongDivPipelined,
		MAIN_WIDTH=8,
		DENOM_WIDTH=8,
		CHUNK_WIDTH=2,
		signed_reset=0,
		#signed_reset=1,
		USE_PIPE_SKID_BUF=True,
		#USE_PIPE_SKID_BUF=False,
	)
#--------
