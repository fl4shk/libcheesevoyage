#!/usr/bin/env python3
#--------
import math

from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
#--------
class FullAdderIshape(IntfShape):
	def __init__(
		self,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		inp_shape = {}
		outp_shape = {}

		inp_shape["a"] = 1
		inp_shape["b"] = 1
		inp_shape["carry"] = 1
		outp_shape["sum"] = 1
		outp_shape["carry"] = 1

		shape = IntfShape.mk_io_ishape(
			inp_shape=inp_shape,
			outp_shape=outp_shape,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)

		super().__init__(shape=shape)
class FullAdderBus:
	def __init__(
		self,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		ishape = FullAdderIshape(inp_tag=inp_tag, outp_tag=outp_tag)
		#super().__init__(ishape)
		self.bus = Splitintf(ishape)
	#@property
	#def bus(self):
	#	return self.__bus

class FullAdder(Elaboratable):
	def __init__(
		self,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		self.__bus = FullAdderBus(inp_tag=inp_tag, outp_tag=outp_tag)
	def bus(self):
		return self.__bus
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus().bus
		loc = Blank()

		loc.temp_inp_a = Signal(2)
		loc.temp_inp_b = Signal(2)
		loc.temp_inp_carry = Signal(2)
		loc.temp_outp_data = Signal(2)
		#--------
		m.d.comb += [
			loc.temp_inp_a.eq(bus.inp.a),
			loc.temp_inp_b.eq(bus.inp.b),
			loc.temp_inp_carry.eq(bus.inp.carry),
			loc.temp_outp_data.eq(
				loc.temp_inp_a + loc.temp_inp_b + loc.temp_inp_carry
			),
			Cat(bus.outp.carry, bus.outp.sum).eq(loc.temp_outp_data),
		]
		#--------
		return m
		#--------
#--------
class BitSerialAdderPstageIoLayt(dict):
	def __init__(self, WIDTH):
		super().__init__({
			"a": WIDTH,
			"b": WIDTH,
			"sum": WIDTH,
			"carry": 1,
		})
class BitSerialAdderPstageIshape(IntfShape):
	def __init__(
		self,
		#index: int,
		WIDTH: int,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		inp_shape = {}
		outp_shape = {}

		#inp_shape["a"] = WIDTH
		#inp_shape["b"] = WIDTH
		#inp_shape["carry"] = 1
		#inp_shape["prev"] = {
		#	"sum": WIDTH
		#},
		#outp_shape["next"] = {
		#	"a": WIDTH,
		#	"b": WIDTH
		#}
		#outp_shape["sum"] = WIDTH
		#outp_shape["carry"] = 1
		#inp_shape["nc"] = AdderNonCarryLayt(WIDTH=WIDTH)
		#inp_shape["carry"] = 1
		#outp_shape["nc"] = AdderNonCarryLayt(WIDTH=WIDTH)
		#outp_shape["carry"] = 1
		inp_shape = BitSerialAdderPstageIoLayt(WIDTH=WIDTH)
		outp_shape = BitSerialAdderPstageIoLayt(WIDTH=WIDTH)

		shape = IntfShape.mk_io_ishape(
			inp_shape=inp_shape,
			outp_shape=outp_shape,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)

		super().__init__(shape=shape)
class BitSerialAdderPstageBus:
	def __init__(
		self,
		WIDTH: int,
		INDEX: int,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		ishape = BitSerialAdderPstageIshape(
			WIDTH=WIDTH,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)
		self.bus = Splitintf(ishape)
		self.__WIDTH = WIDTH
		self.__INDEX = INDEX
		self.__inp_tag = inp_tag
		self.__outp_tag = outp_tag
	#@property
	#def bus(self):
	#	return self.__bus
	def WIDTH(self):
		return self.__WIDTH
	def INDEX(self):
		return self.__INDEX
	def inp_tag(self):
		return self.__inp_tag
	def outp_tag(self):
		return self.__outp_tag
class BitSerialAdderPstage(Elaboratable):
	def __init__(
		self,
		WIDTH: int,
		INDEX: int,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		self.__bus = BitSerialAdderPstageBus(
			WIDTH=WIDTH,
			INDEX=INDEX,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)
	def bus(self):
		return self.__bus
	def elaborate(self, platform: str) -> Module:
		m = Module()
		bus = self.bus().bus
		WIDTH = self.bus().WIDTH()
		INDEX = self.bus().INDEX()
		loc = Blank()

		loc.m = Blank()
		m.submodules.fa = loc.m.fa = fa = FullAdder(
			inp_tag=self.bus().inp_tag(),
			outp_tag=self.bus().outp_tag(),
		)
		fa_bus = fa.bus().bus

		m.d.comb += [
			fa_bus.inp.a.eq(bus.inp.a[INDEX]),
			fa_bus.inp.b.eq(bus.inp.b[INDEX]),
			fa_bus.inp.carry.eq(bus.inp.carry),
		]
		m.d.sync += [
			bus.outp.a.eq(bus.inp.a),
			bus.outp.b.eq(bus.inp.b),
			bus.outp.carry.eq(fa_bus.outp.carry),
		]
		for i in range(WIDTH):
			if i == INDEX:
				m.d.sync += bus.outp.sum[i].eq(fa_bus.outp.sum),
			else:
				m.d.sync += bus.outp.sum[i].eq(bus.inp.sum[i])

		return m

class BitSerialAdderIshape(IntfShape):
	def __init__(
		self,
		WIDTH: int,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		#shape = {
		#	"inp": {}
		#	"outp": {}
		#}
		inp_shape = {}
		outp_shape = {}

		inp_shape["a"] = WIDTH
		inp_shape["b"] = WIDTH
		inp_shape["carry"] = 1

		outp_shape["sum"] = WIDTH
		outp_shape["carry"] = 1

		shape = IntfShape.mk_io_ishape(
			inp_shape=inp_shape,
			outp_shape=outp_shape,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)

		super().__init__(shape=shape)
class BitSerialAdderBus:
	def __init__(
		self,
		WIDTH: int,
		*,
		inp_tag=None,
		outp_tag=None,
	):
		ishape = BitSerialAdderIshape(
			WIDTH=WIDTH,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)
		self.bus = Splitintf(ishape)
		self.__WIDTH = WIDTH
	#@property
	#def bus(self):
	#	return self.__bus
	def WIDTH(self):
		return self.__WIDTH

class BitSerialAdder(Elaboratable):
	def __init__(
		self,
		WIDTH: int,
		*,
		FORMAL: bool=False,
		inp_tag=None,
		outp_tag=None,
	):
		self.__bus = BitSerialAdderBus(
			WIDTH=WIDTH,
			inp_tag=inp_tag,
			outp_tag=outp_tag,
		)
		self.__WIDTH = WIDTH
		self.__FORMAL = FORMAL
	def bus(self):
		return self.__bus
	def WIDTH(self):
		return self.__WIDTH
	def FORMAL(self):
		return self.__FORMAL
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		#bus = self.bus()
		bus = self.bus().bus
		WIDTH = self.WIDTH()
		FORMAL = self.FORMAL()

		loc = Blank()
		loc.m = []
		f = open("elab.txt", "w")
		f.writelines([
			psconcat(
				"WIDTH:{!r}"
			).format(
				WIDTH
			),
			"\n",
		])
		for i in range(WIDTH):
			inp_tag = psconcat(i)
			outp_tag = psconcat(i + 1)
			f.writelines([
				psconcat(
					"i:{!r} inp_tag:{!r} outp_tag:{!r}"
				).format(
					i, inp_tag, outp_tag,
				),
				"\n"
			])
			loc.m.append(
				BitSerialAdderPstage(
					WIDTH=WIDTH,
					INDEX=i,
					inp_tag=inp_tag,
					outp_tag=outp_tag,
				)
			)
		f.close()
		m.submodules += loc.m

		if FORMAL:
			loc.formal = Blank()
			#loc.formal.oracle_outp_sum = Signal(
			#	WIDTH,
			#	name="formal_oracle_outp_sum", attrs=sig_keep(),
			#)
			#loc.formal.oracle_outp_carry = Signal(
			#	name="formal_oracle_outp_carry", attrs=sig_keep(),
			#)
			loc.formal.temp_arr = Splitarr(
				[
					{
						"a": WIDTH + 1,
						"b": WIDTH + 1,
						"carry_in": 2,
						"temp_sum": WIDTH + 1,
					}
					for i in range(WIDTH + 1)
				],
				name="formal_temp_arr",
				attrs=sig_keep(),
			)
			#loc.formal.oracle_outp_sum_arr = Splitarr(
			#	[WIDTH for i in range(WIDTH + 1)],
			#	name="formal_oracle_outp_sum_arr",
			#	attrs=sig_keep(),
			#)
			#loc.formal.oracle_outp_carry_vec = Signal(
			#	WIDTH + 1,
			#	name="formal_oracle_outp_carry_vec",
			#	attrs=sig_keep(),
			#)
		#--------
		for i in range(WIDTH - 1):
			f = open(psconcat("test_tag-", i, ".txt.ignore"), "w")
			ps_bus = loc.m[i].bus().bus
			ps_bus_next = loc.m[i + 1].bus().bus
			ps_bus.connect(
				other=ps_bus_next,
				m=m,
				kind=Splitintf.ConnKind.Parallel,
				#f=f,
				conn_lst_shrink=-2,
				use_tag=True,
			)
			#m.d.comb += Cat(ps_bus_next.inp.flattened()).eq(
			#	Cat(ps_bus.outp.flattened())
			#)
			f.close()
		ps_bus_first = loc.m[0].bus().bus
		ps_bus_last = loc.m[-1].bus().bus
		m.d.comb += [
			ps_bus_first.inp.a.eq(bus.inp.a),
			ps_bus_first.inp.b.eq(bus.inp.b),
			ps_bus_first.inp.sum.eq(0x0),
			ps_bus_first.inp.carry.eq(bus.inp.carry),

			bus.outp.sum.eq(ps_bus_last.outp.sum),
			bus.outp.carry.eq(ps_bus_last.outp.carry),
		]

		if FORMAL:
			m.d.comb += [
				#Cat(
				#	loc.formal.oracle_outp_carry_vec[0],
				#	loc.formal.oracle_outp_sum_arr[0],
				#).eq()
				loc.formal.temp_arr[0].a.eq(bus.inp.a),
				loc.formal.temp_arr[0].b.eq(bus.inp.b),
				loc.formal.temp_arr[0].sum.eq(0x0),
				loc.formal.temp_arr[0].carry.eq(bus.inp.carry),
			]

			for i in range(WIDTH):
				m.d.sync += [
					#loc.formal.oracle_outp_sum_arr[i + 1].eq(
					#	loc.formal.oracle_outp_sum_arr[i]
					#),
					#loc.formal.oracle_outp_carry_vec[i + 1].eq(
					#	loc.formal.oracle_outp_carry_vec[i]
					#),
					loc.formal.temp_arr[i + 1].eq(loc.formal.temp_arr[i])
				]
		#m.d.comb += [
		#	loc.m[0].bus()
		#]
		#--------
		return m
		#--------
