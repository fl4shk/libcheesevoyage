#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage import *
from libcheesevoyage.misc_util_running import *
#--------
if __name__ == "__main__":
	formal_non_sync \
	(
		dut_mod=XbarSwitchBus,
		ElemKindT=8,
		INP_SIZE=6,
		OUTP_SIZE=3
	)
#--------
