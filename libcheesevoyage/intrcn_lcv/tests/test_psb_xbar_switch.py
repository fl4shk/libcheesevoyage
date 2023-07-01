#!/usr/bin/env python3

from amaranth import *
#from amaranth import *

from libcheesevoyage.misc_util_running import *
from libcheesevoyage.intrcn_lcv import *
#--------
if __name__ == "__main__":
	#formal_non_sync(
	#	dut_mod=XbarSwitch,
	#	ELEM_WIDTH=8,
	#	INP_SIZE=6,
	#	OUTP_SIZE=3,
	#)

	#formal(
	to_verilog(
		dut_mod=PsbXbarSwitch,
		h2d_data_shape={"h2d_data": 2},
		d2h_data_shape={"d2h_data": 2},
		#NUM_HOSTS=4,
		#NUM_DEVS=4,
		NUM_HOSTS=2,
		NUM_DEVS=2,
		#H2D_SIGNED=False,
		#D2H_SIGNED=False,
	#)
	)

	#to_verilog_non_sync(
	#	dut_mod=XbarSwitch,
	#	H2dElemKindT=2,
	#	D2hElemKindT=2,
	#	NUM_HOSTS=3,
	#	NUM_DEVS=2,
	#	H2D_SIGNED=False,
	#	D2H_SIGNED=False,
	#	#PRIO_LST_2D = [[0, 2, 1], [0, 2, 1]],
	#	DOMAIN=BasicDomain.COMB
	#)

	#to_verilog_non_sync(
	#	dut_mod=XbarSwitch,
	#	ELEM_WIDTH=2,
	#	INP_SIZE=4,
	#	OUTP_SIZE=4,
	#)
#--------
