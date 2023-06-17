#!/usr/bin/env python3

from amaranth import *
from amaranth.lib.data import *

#from libcheesevoyage import *
from libcheesevoyage.misc_util_running import (
	to_verilog, formal,
)
from libcheesevoyage.general.container_types import (
	SigInfo
)
from libcheesevoyage.general.pipeline_mods import (
	PipeSkidBuf
)
#--------
if __name__ == "__main__":
	formal(
	#to_verilog
		#dut_mod=LongDivMultiCycle,
		dut_mod=PipeSkidBuf,
		data_info=SigInfo(
			basenm="formal",
			#shape=StructLayout({key: 5 for key in ["r", "g", "b"]})
			shape=1,
			#shape={key: 5 for key in ["r", "g", "b"]},
		),
		OPT_INCLUDE_VALID_BUSY=True,
		OPT_INCLUDE_READY_BUSY=True,
	)
#--------
