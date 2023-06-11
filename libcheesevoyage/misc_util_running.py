#!/usr/bin/env python3

from amaranth import *
from amaranth.sim import *
from amaranth.lib.data import *

from amaranth.cli import main, main_parser, main_runner

from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.back import verilog

from libcheesevoyage.general.container_types import (
	Splitrec, Splitarr, Splitintf, Splitintfarr
)

def inner_ports(bus):
	ret = []
	for key in bus.__dict__:
		val = bus.__dict__[key]
		if (
			(
				key[0] != "_"
			) and (
				key[0] not in type(bus).__dict__
			)
		):
			if isinstance(val, Signal) or isinstance(val, Record):
				ret += [Value.cast(val)]
			#elif isinstance(val, Packrec):
			#	ret += [Value.cast(val)]
			#elif isinstance(val, Packarr):
			#	ret += [Value.cast(val)]
			elif isinstance(val, View) \
				or isinstance(val, Struct) \
				or isinstance(val, Union):
				ret += [Value.cast(val)]
			elif (
				isinstance(val, Splitarr)
				or isinstance(val, Splitintfarr)
			):
				ret += list(val)
			#elif isinstance(val, Splitrec):
			#	ret += val.flattened()
			else:
				ret += inner_ports(val)
	return ret
def ports(bus):
	return ([ClockSignal(), ResetSignal()] + inner_ports(bus))

def to_verilog_non_sync(dut_mod, **kw_args):
	dut = dut_mod(**kw_args)
	# ./main.py generate -t v
	main(dut, ports=inner_ports(dut.bus()))
	#with open("dut.v.ignore", "w") as f:
	#	f.write(verilog.convert(dut, ports=ports(dut.bus())))
def to_verilog(dut_mod, **kw_args):
	dut = dut_mod(**kw_args)
	# ./main.py generate -t v
	main(dut, ports=ports(dut.bus()))
	#with open("dut.v.ignore", "w") as f:
	#	f.write(verilog.convert(dut, ports=ports(dut.bus())))

def formal_non_sync(dut_mod, **kw_args):
	parser = main_parser()
	args = parser.parse_args()

	m = Module()
	m.submodules.dut = dut = dut_mod(**kw_args, FORMAL=True)

	# python3 main.py generate -t il > toplevel.il
	# sby -f "$1"
	main_runner(parser, args, m, ports=inner_ports(dut.bus()))

def formal(dut_mod, **kw_args):
	parser = main_parser()
	args = parser.parse_args()

	m = Module()
	m.submodules.dut = dut = dut_mod(**kw_args, FORMAL=True)

	# python3 main.py generate -t il > toplevel.il
	# sby -f "$1"
	main_runner(parser, args, m, ports=ports(dut.bus()))

def verify(dut_mod, **kw_args):
	dut = dut_mod(**kw_args, DBG=True)

	sim = Simulator(dut)
	sim.add_clock(1e-6) # 1 MHz
	sim.add_process(dut.verify_process)
	with sim.write_vcd("test.vcd"):
		#sim.run_until(1e-3)
		sim.run()
