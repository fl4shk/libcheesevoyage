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

	#formal_non_sync \
	formal \
	(
		dut_mod=XbarSwitch,
		H2dElemKindT=2,
		D2hElemKindT=2,
		NUM_HOSTS=2,
		NUM_DEVS=2,
		H2D_SIGNED=False,
		D2H_SIGNED=False,
	)

	#to_verilog \
	#(
	#	dut_mod=XbarSwitch,
	#	H2dElemKindT=2,
	#	D2hElemKindT=2,
	#	NUM_HOSTS=2,
	#	NUM_DEVS=2,
	#	H2D_SIGNED=False,
	#	D2H_SIGNED=False,
	#)

	#to_verilog_non_sync \
	#(
	#	dut_mod=XbarSwitch,
	#	ELEM_WIDTH=2,
	#	INP_SIZE=4,
	#	OUTP_SIZE=4,
	#)
#--------
