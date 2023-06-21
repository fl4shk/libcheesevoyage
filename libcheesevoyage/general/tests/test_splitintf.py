#!/usr/bin/env python3
#--------
from test_splitintf_mods import *
#from libcheesevoyage.misc_util import *
from libcheesevoyage.misc_util_running import (
	to_verilog, formal,
)
#--------
if __name__ == "__main__":
	#formal
	to_verilog(
		dut_mod=BitSerialAdder,
		WIDTH=4,
	)
#--------
