#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage import *
from libcheesevoyage.misc_util_running import *
#--------
if __name__ == "__main__":
	#formal_non_sync \
	#(
	#	dut_mod=XbarSwitch,
	#	ELEM_WIDTH=8,
	#	INP_SIZE=6,
	#	OUTP_SIZE=3,
	#)

	formal_non_sync \
	(
		dut_mod=XbarSwitch,
		ElemKindT=2,
		INP_SIZE=8,
		OUTP_SIZE=3,
		SIGNED=False,
	)

	#to_verilog_non_sync \
	#(
	#	dut_mod=XbarSwitch,
	#	ELEM_WIDTH=2,
	#	INP_SIZE=4,
	#	OUTP_SIZE=4,
	#)
#--------
