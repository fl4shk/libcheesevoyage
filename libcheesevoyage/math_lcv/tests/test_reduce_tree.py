#!/usr/bin/env python3

from amaranth import *
from amaranth import *

from libcheesevoyage import *
from libcheesevoyage.misc_util_running import *
#--------
if __name__ == "__main__":
	formal_non_sync(
		dut_mod=ReduceTree,
		INP_DATA_WIDTH=8,
		INP_SIZE=8,
		BINOP=ReduceTreeBinop.ADD,
	)
#--------
