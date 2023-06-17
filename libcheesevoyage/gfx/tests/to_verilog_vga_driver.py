#!/usr/bin/env python3

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum

from libcheesevoyage.misc_util_running import *
from libcheesevoyage.general import *
from libcheesevoyage.gfx import *
#--------
if __name__ == "__main__":
	to_verilog(
		dut_mod=VgaDriver,
		CLK_RATE=100,
		TIMING_INFO=VGA_TIMING_INFO_DICT["640x480@60"],
		#FIFO_SIZE=640,
		FIFO_SIZE=64,
	)
#--------
