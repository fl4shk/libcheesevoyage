#!/usr/bin/env python3

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum

#from libcheesevoyage import *
from libcheesevoyage.misc_util_running import *
from libcheesevoyage.general import *
#--------
if __name__ == "__main__":
	to_verilog(
		dut_mod=PipeSkidBuf,
		#data_info=PipeSigInfo(
		#	info=SigInfo(
		#		basenm="data",
		#		shape=unsigned(8),
		#	)
		#),
		#psig_dir=PipeSigDir.
		data_info=SigInfo(
			basenm="data",
			shape=unsigned(8),
		)
	)