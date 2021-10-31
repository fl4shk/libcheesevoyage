#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.misc_types import *

from math import *
import functools
import operator
#--------
class XbarSwitchBus:
	#--------
	def __init__(self, ElemKindT, INP_SIZE, OUTP_SIZE, SIGNED=False, *,
		FORMAL=False):
		#--------
		self.__ElemKindT = ElemKindT
		self.__INP_SIZE = INP_SIZE
		self.__OUTP_SIZE = OUTP_SIZE
		self.__SIGNED = SIGNED
		self.__FORMAL = FORMAL
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		# Whether or not to enable forwarding the particular input
		self.inp.sel_conn = Signal(self.INP_SIZE(), name="inp_sel_conn")

		# Which inputs to forward to which outputs
		self.inp.sel \
			= Splitarr \
			([
				Signal(self.SEL_WIDTH(), name=f"inp_sel_{i}")
					for i in range(self.INP_SIZE())
			])

		#self.inp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.inp.data \
			= Splitarr \
			([
				Splitrec.cast_elem(self.ElemKindT(), self.SIGNED(),
					name=f"inp_data_{i}")
					for i in range(self.INP_SIZE())
			])

		#self.outp_data = Packarr.build(ElemKindT=self.ElemKindT(),
		#	SIZE=self.SIZE(), SIGNED=self.SIGNED())
		self.outp.data \
			= Splitarr \
			([
				Splitrec.cast_elem(self.ElemKindT(), self.SIGNED(),
					name=f"outp_data_{j}")
					for j in range(self.OUTP_SIZE())
			])
		#--------
	#--------
	def ElemKindT(self):
		return self.__ElemKindT
	def INP_SIZE(self):
		return self.__INP_SIZE
	def OUTP_SIZE(self):
		return self.__OUTP_SIZE
	def SIGNED(self):
		return self.__SIGNED
	def FORMAL(self):
		return self.__FORMAL
	def SEL_WIDTH(self):
		return math.ceil(math.log2(self.OUTP_SIZE()))
	#--------
#--------
# A crossbar switch
class XbarSwitch(Elaboratable):
	#--------
	# For `PRIO_LST_2D`, lower list indices mean higher priority

	#def __init__(self, ElemKindT, INP_SIZE, OUTP_SIZE, SIGNED=False, *,
	#	FORMAL=False):
	def __init__(self, ElemKindT, INP_SIZE, OUTP_SIZE, SIGNED=False, *,
		PRIO_LST_2D=None, DOMAIN=BasicDomain.COMB, FORMAL=False):
		#--------
		if (not isinstance(PRIO_LST_2D, list)) \
			and (not isinstance(PRIO_LST_2D, type(None))):
			raise TypeError(psconcat
				("`PRIO_LST_2d` `{!r}` must be a `list` or `None`"
				.format(PRIO_LST_2D)))

		if isinstance(PRIO_LST_2D, list):
			if len(PRIO_LST_2D) != OUTP_SIZE:
				raise ValueError(psconcat
					("`PRIO_LST_2D` `{!r}` must be of length `OUTP_SIZE` ",
					"`{!r}`".format(PRIO_LST_2D)))

			for PRIO_LST in PRIO_LST_2D:
				temp = set(PRIO_LST)

				if (len(PRIO_LST) != len(temp)) \
					or (temp != set(list(range(INP_SIZE)))):
					raise ValueError(psconcat
						("`PRIO_LST` `{!r}` must ".format(PRIO_LST),
						"consist of all unique `int`s that are between 0 ",
						"and `INP_SIZE` `{!r}`".format(INP_SIZE)))

			self.__PRIO_LST_2D = PRIO_LST_2D
		else: # if isinstance(PRIO_LST, None):
			self.__PRIO_LST_2D \
				= [
					[i for i in range(INP_SIZE)]
						for j in range(OUTP_SIZE)
				]
		#--------
		#if not isinstance(DOMAIN, BasicDomain):
		#	raise TypeError("`DOMAIN` `{!r}` must be a `BasicDomain`"
		#		.format(DOMAIN))
		self.__DOMAIN = DOMAIN
		#--------
		self.__bus \
			= XbarSwitchBus \
			(
				ElemKindT=ElemKindT,
				INP_SIZE=INP_SIZE,
				OUTP_SIZE=OUTP_SIZE,
				SIGNED=SIGNED,
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
		loc.found_arr \
			= Splitarr \
			([
				Signal(bus.INP_SIZE(), name=f"found_arr_{j}")
					for j in range(bus.OUTP_SIZE())
			])
		loc.dbg_sel \
			= Splitarr \
			([
				Signal(signed(bus.INP_SIZE() + 1), attrs=sig_keep(),
					name=f"dbg_sel_{j}")
					for j in range(bus.OUTP_SIZE())
			])

		md = basic_domain_to_actual_domain(m, self.DOMAIN())
		#--------
		for j in range(bus.OUTP_SIZE()):
			m.d.comb \
			+= [
				loc.found_arr[j][i].eq(inp.sel_conn[i]
					& (inp.sel[i] == j))
					for i in range(bus.INP_SIZE())
			]

			with m.Switch(loc.found_arr[j]):
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

					with m.Case("".join(list(reversed(CASE)))):
						md \
						+= [
							loc.dbg_sel.eq(PRIO_LST[i]),
							outp.data[j].eq(inp.data[PRIO_LST[i]])
						]

				with m.Default():
					md \
					+= [
						loc.dbg_sel.eq(-1),
						outp.data[j].eq(0x0),
					]
		#--------
		if bus.FORMAL():
			if self.DOMAIN() != BasicDomain.COMB:
				raise ValueError(("`self.DOMAIN()` `{!r}` must be "
					+ "`BasicDomain.COMB` when doing formal verification")
					.format(self.DOMAIN()))
			#if PRIO_LST_2D \
			#	!= [[i for i in range(bus.INP_SIZE()]
			#		for j in range(bus.OUTP_SIZE())]:
			#	raise ValueError(("`PRIO_LST_2D` `{!r}` must be the "
			#		+ "default when doing formal verification")
			#		.format(PRIO_LST_2D))

			for j in range(bus.OUTP_SIZE()):
				PRIO_LST = PRIO_LST_2D[j]

				with m.If(loc.found_arr[j][PRIO_LST[0]]):
					m.d.comb \
					+= [
						Assert(outp.data[j] == inp.data[PRIO_LST[0]])
					]
				for i in range(1, bus.INP_SIZE()):
					with m.Elif(loc.found_arr[j][PRIO_LST[i]]):
						m.d.comb \
						+= [
							Assert(outp.data[j] == inp.data[PRIO_LST[i]])
						]
				with m.Else():
					m.d.comb \
					+= [
						Assert(outp.data[j] == 0x0)
					]
		#--------
		return m
		#--------
	#--------
#--------
