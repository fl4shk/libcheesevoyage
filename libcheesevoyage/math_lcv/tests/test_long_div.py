#!/usr/bin/env python3

from amaranth import *

from libcheesevoyage import *
from libcheesevoyage.misc_util_running import *
#--------
if __name__ == "__main__":
	formal(
		#dut_mod=LongDivMultiCycle,
		dut_mod=LongDivPipelined,
		MAIN_WIDTH=8,
		DENOM_WIDTH=8,
		CHUNK_WIDTH=2,
		signed_reset=0,
		#signed_reset=1,
	)
#--------
