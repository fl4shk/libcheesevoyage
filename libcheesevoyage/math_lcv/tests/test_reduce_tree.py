#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage import *
#--------
if name == "__main__":
	dut \
		= ReduceTree \
		(
			INP_DATA_WIDTH=8,
			INP_SIZE=8,
			BINOP=ReduceTreeBinop.ADD,
			FORMAL=True,
		)
#--------
