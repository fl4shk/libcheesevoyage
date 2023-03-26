#!/usr/bin/env python3

from amaranth import *
from amaranth import *

from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.misc_types import *

from math import *
import functools
import operator
#--------
class XbarSwitchBus:
	#--------
	def __init__(self, H2dShapeT, D2hShapeT, NUM_HOSTS, 
		NUM_DEVS,
		#H2D_SIGNED=False, D2H_SIGNED=False,
		*, FORMAL=False):
		#--------
		#self.__ElemKindT = ElemKindT

		self.__H2dShapeT = H2dShapeT
		self.__D2hShapeT = D2hShapeT

		self.__NUM_HOSTS = NUM_HOSTS
		self.__NUM_DEVS = NUM_DEVS
		#self.__H2D_SIGNED = H2D_SIGNED
		#self.__D2H_SIGNED = D2H_SIGNED
		self.__FORMAL = FORMAL
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		# Inputs

		# Whether or not to enable forwarding the particular data
		self.inp.sel_conn = Signal(self.NUM_HOSTS(), name="inp_sel_conn")

		# Which hosts to connect to which devices
		self.inp.sel = Splitarr([
			Signal(self.SEL_WIDTH(), name=f"inp_sel_{i}")
				for i in range(self.NUM_HOSTS())
		])

		#self.inp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.inp.h2d_data = Splitarr([
			Splitrec.cast_elem(self.H2dShapeT(), #self.H2D_SIGNED(),
				name=f"inp_h2d_data_{i}")
				for i in range(self.NUM_HOSTS())
		])
		self.inp.d2h_data = Splitarr([
			Splitrec.cast_elem(self.D2hShapeT(), #self.D2H_SIGNED(),
				name=f"inp_d2h_data_{j}")
				for j in range(self.NUM_DEVS())
		])
		#--------
		# Outputs

		# Which hosts are active
		self.outp.d2h_active = Signal(self.NUM_DEVS(),
			name="outp_d2h_active")

		#self.outp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.outp.h2d_data = Splitarr([
			Splitrec.cast_elem(self.H2dShapeT(), #self.H2D_SIGNED(),
				name=f"outp_h2d_data_{j}")
				for j in range(self.NUM_DEVS())
		])
		self.outp.d2h_data = Splitarr([
			Splitrec.cast_elem(self.D2hShapeT(), #self.D2H_SIGNED(),
				name=f"outp_d2h_data_{i}")
				for i in range(self.NUM_HOSTS())
		])
		#--------
	#--------
	def H2dShapeT(self):
		return self.__H2dShapeT
	def D2hShapeT(self):
		return self.__D2hShapeT
	def NUM_HOSTS(self):
		return self.__NUM_HOSTS
	def NUM_DEVS(self):
		return self.__NUM_DEVS
	#def H2D_SIGNED(self):
	#	return self.__H2D_SIGNED
	#def D2H_SIGNED(self):
	#	return self.__D2H_SIGNED
	def FORMAL(self):
		return self.__FORMAL
	def SEL_WIDTH(self):
		return math.ceil(math.log2(self.NUM_DEVS()))
	#--------
#--------
# A crossbar switch
class XbarSwitch(Elaboratable):
	#--------
	# For `PRIO_LST_2D`, lower list indices mean higher priority

	def __init__(self, H2dShapeT, D2hShapeT, NUM_HOSTS, NUM_DEVS,
		#H2D_SIGNED=False, D2H_SIGNED=False,
		*, PRIO_LST_2D=None,
		DOMAIN=BasicDomain.COMB, FORMAL=False):
	#def __init__(self, H2dShapeT, D2hShapeT, NUM_HOSTS, NUM_DEVS,
	#	H2D_SIGNED=False, D2H_SIGNED=False, *, PRIO_LST_2D=None,
	#	FORMAL=False):
		#--------
		if (not isinstance(PRIO_LST_2D, list)) \
			and (not isinstance(PRIO_LST_2D, type(None))):
			raise TypeError(psconcat(
				"`PRIO_LST_2D` `{!r}` must be a `list` or `None`"
				.format(PRIO_LST_2D)
			))

		if isinstance(PRIO_LST_2D, list):
			if len(PRIO_LST_2D) != NUM_DEVS:
				raise ValueError(psconcat(
					"`PRIO_LST_2D` `{!r}` must be of length `NUM_DEVS` ",
					"`{!r}`".format(PRIO_LST_2D)
				))

			#self.__PRIO_LST_2D = []

			for PRIO_LST in PRIO_LST_2D:
				temp = set(PRIO_LST)

				if (len(PRIO_LST) != len(temp)) \
					or (temp != set(list(range(NUM_HOSTS)))):
					raise ValueError(psconcat(
						"`PRIO_LST` `{!r}` must ".format(PRIO_LST),
						"consist of all unique `int`s that are between 0 ",
						"and `NUM_HOSTS` `{!r}`".format(NUM_HOSTS)
					))

				#self.__PRIO_LST_2D.append(list(reversed(PRIO_LST)))

			self.__PRIO_LST_2D = PRIO_LST_2D
		else: # if isinstance(PRIO_LST, None):
			#self.__PRIO_LST_2D = [
			#	[NUM_HOSTS - (i + 1) for i in range(NUM_HOSTS)]
			#		for j in range(NUM_DEVS)
			#]
			#self.__PRIO_LST_2D = [
			#	list(reversed([i for i in range(NUM_HOSTS)]))
			#		for j in range(NUM_DEVS)
			#]
			self.__PRIO_LST_2D = [
				[i for i in range(NUM_HOSTS)]
					for j in range(NUM_DEVS)
			]
		#--------
		if not isinstance(DOMAIN, BasicDomain):
			raise TypeError("`DOMAIN` `{!r}` must be a `BasicDomain`"
				.format(DOMAIN))
		self.__DOMAIN = DOMAIN
		#--------
		self.__bus = XbarSwitchBus(
			H2dShapeT=H2dShapeT,
			D2hShapeT=D2hShapeT,
			NUM_HOSTS=NUM_HOSTS,
			NUM_DEVS=NUM_DEVS,
			#H2D_SIGNED=H2D_SIGNED,
			#D2H_SIGNED=D2H_SIGNED,
			FORMAL=FORMAL
		)
		#--------
	#--------
	def PRIO_LST_2D(self):
		return self.__PRIO_LST_2D
	def DOMAIN(self):
		return self.__DOMAIN
	def bus(self):
		return self.__bus
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		inp = bus.inp
		outp = bus.outp

		PRIO_LST_2D = self.PRIO_LST_2D()

		loc = Blank()
		loc.found_arr = Splitarr([
			Signal(bus.NUM_HOSTS(), name=f"found_arr_{j}")
				for j in range(bus.NUM_DEVS())
		])
		#if bus.FORMAL():
		#	loc.formal = Blank()
		#	loc.formal.past_valid = Signal(name="formal_past_valid")

		#loc.dbg_sel = Splitarr([
		#	Signal(signed(bus.NUM_HOSTS() + 1), attrs=sig_keep(),
		#		name=f"dbg_sel_{j}")
		#		for j in range(bus.NUM_DEVS())
		#])

		md = basic_domain_to_actual_domain(m, self.DOMAIN())
		#--------
		for j in range(bus.NUM_DEVS()):
			m.d.comb += [
				loc.found_arr[j][i].eq(inp.sel_conn[i]
					& (inp.sel[i] == j))
					for i in range(bus.NUM_HOSTS())
			]

			with m.Switch(loc.found_arr[j]):
				#PRIO_LST = list(reversed(PRIO_LST_2D[j]))
				PRIO_LST = PRIO_LST_2D[j]

				for i in range(len(PRIO_LST)):
					CASE = ["-"] * len(PRIO_LST)
					CASE[PRIO_LST[i]] = "1";

					k = 0
					while k < i:
						CASE[PRIO_LST[k]] = "0"
						k += 1
					k += 1
					while k < len(PRIO_LST):
						CASE[PRIO_LST[k]] = "-"
						k += 1

					# The `"".join(...)` is correct. I tested it by
					# examining the Verilog output
					with m.Case("".join(list(reversed(CASE)))):
						md += [
							#loc.dbg_sel.eq(PRIO_LST[i]),
							outp.d2h_active[j].eq(0b1),
							outp.h2d_data[j].eq(inp.h2d_data[PRIO_LST[i]]),

							# I'm not sure this is correct.
							# Here's how I see it.
							# We are mapping from the selected *master*,
							# which is what has priority, 
							outp.d2h_data[PRIO_LST[i]].eq(inp.d2h_data[j]),
						]
				with m.Default():
					md += [
						#loc.dbg_sel.eq(-1),
						outp.d2h_active[j].eq(0b0),
						#outp.d2h_active.eq(0x0),
						#outp.h2d_data[j].eq(0x0),
						#outp.h2d_data[j].eq(0x0),
					]
		#--------
		if bus.FORMAL():
			if self.DOMAIN() != BasicDomain.COMB:
				raise ValueError(("`self.DOMAIN()` `{!r}` must be "
					+ "`BasicDomain.COMB` when doing formal verification")
					.format(self.DOMAIN()))
			#if PRIO_LST_2D != [
			#		#[
			#		#	bus.NUM_HOSTS() - (i + 1)
			#		#	for i in range(bus.NUM_HOSTS())
			#		#]
			#		[i for i in range(bus.NUM_HOSTS())]
			#		for j in range(bus.NUM_DEVS())
			#]:
			#	raise ValueError(("`PRIO_LST_2D` `{!r}` must be the "
			#		+ "default when doing formal verification")
			#		.format(PRIO_LST_2D))
			#m.d.sync += loc.formal.past_valid.eq(0b1)

			#with m.If((~ResetSignal()) & loc.formal.past_valid):
			for j in range(bus.NUM_DEVS()):
				PRIO_LST = PRIO_LST_2D[j]
				#PRIO_LST = list(reversed(PRIO_LST_2D[j]))

				if isinstance(bus.D2hShapeT(), int):
					for i in range(bus.NUM_HOSTS()):
						for k in range(2 ** len(outp.d2h_data[0])):
							with m.If(outp.d2h_data[PRIO_LST[i]] == k):
								m.d.comb += [
									Cover(inp.d2h_data[j] == k)
								]

				with m.If(loc.found_arr[j][PRIO_LST[0]]):
					m.d.comb += [
						Assert(outp.d2h_active[j]),
						Assert(outp.h2d_data[j]
							== inp.h2d_data[PRIO_LST[0]]),
						Assert(outp.d2h_data[PRIO_LST[0]]
							== inp.d2h_data[j]),
					]
				for i in range(1, bus.NUM_HOSTS()):
					with m.Elif(loc.found_arr[j][PRIO_LST[i]]):
						m.d.comb += [
							Assert(outp.d2h_active[j]),
							Assert(outp.h2d_data[j]
								== inp.h2d_data[PRIO_LST[i]]),
							Assert(outp.d2h_data[PRIO_LST[i]]
								== inp.d2h_data[j]),
						]
				with m.Else():
					m.d.comb += [
						Assert(~outp.d2h_active[j]),
						#Assert(outp.d2h_active == 0x0)
						#Assert(outp.h2d_data[j] == 0x0),
					]
		#--------
		return m
		#--------
	#--------
#--------
