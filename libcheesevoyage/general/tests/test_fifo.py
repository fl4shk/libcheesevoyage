#!/usr/bin/env python3

from amaranth import *

#from libcheesevoyage import *
from libcheesevoyage.misc_util_running import (
	to_verilog, formal,
)
from libcheesevoyage.general.fifo_mods import (
	Fifo, AsyncReadFifo,
)
#--------
if __name__ == "__main__":
	formal(
		dut_mod=Fifo,
		#dut_mod=AsyncReadFifo,
		shape=4,
		SIZE=4,
		inp_tag=None,
		outp_tag=None,
	)
