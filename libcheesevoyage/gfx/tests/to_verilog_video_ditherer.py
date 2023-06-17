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
		dut_mod=VideoDitherer,
		FB_SIZE_2D=ElabVec2(8, 8),
	)
#--------
