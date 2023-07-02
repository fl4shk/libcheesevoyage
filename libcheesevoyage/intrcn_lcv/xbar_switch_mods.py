#!/usr/bin/env python3

import enum as pyenum

from amaranth import *
from amaranth.lib.data import *
from amaranth.lib import enum

from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.misc_types import *
from libcheesevoyage.general.pipeline_mods import *

#from math import *
import math
#--------
class XbarSwitchIshape(IntfShape):
	def __init__(
		self,
		NUM_HOSTS, NUM_DEVS,
		*,
		h2d_shape=None,
		d2h_shape=None,
		tag_dct={
			"inp": None,
			"outp": None,
		},
	):
		#--------
		inp_shape = {}
		outp_shape = {}
		#--------
		# Inputs

		# Whether or not to enable forwarding the particular data
		inp_shape["sel_conn"] = FieldInfo(
			NUM_HOSTS, name="inp_sel_conn"
		)

		# Which hosts to connect to which devices
		inp_shape["sel"] = [
			FieldInfo(
				XbarSwitchIshape.SEL_WIDTH(NUM_DEVS), name=f"inp_sel_{i}"
			)
			for i in range(NUM_HOSTS)
		]

		if h2d_shape is not None:
			try:
				temp_h2d_shape = Shape.cast(h2d_shape)
			except Exception:
				temp_h2d_shape = h2d_shape

			inp_shape["h2d_data"] = [
				FieldInfo(
					temp_h2d_shape, #H2D_SIGNED,
					name=f"inp_h2d_data_{i}"
				)
				for i in range(NUM_HOSTS)
			]
		#if OPT_INCLUDE_D2H_DATA:
		if d2h_shape is not None:
			try:
				temp_d2h_shape = Shape.cast(d2h_shape)
			except Exception:
				temp_d2h_shape = d2h_shape

			inp_shape["d2h_data"] = [
				FieldInfo(
					temp_d2h_shape, #D2H_SIGNED,
					name=f"inp_d2h_data_{j}"
				)
				for j in range(NUM_DEVS)
			]
		#--------
		# Outputs

		# Which hosts are active
		outp_shape["hosts_active"] = FieldInfo(
			NUM_HOSTS,
			name="outp_hosts_active",
		)

		## Which hosts are active
		# Which devices are active
		outp_shape["devs_active"] = FieldInfo(
			NUM_DEVS,
			name="outp_devs_active",
		)

		if h2d_shape is not None:
			outp_shape["h2d_data"] = [
				FieldInfo(
					temp_h2d_shape, #H2D_SIGNED,
					name=f"outp_h2d_data_{j}"
				)
				for j in range(NUM_DEVS)
			]
		if d2h_shape is not None:
			outp_shape["d2h_data"] = [
				FieldInfo(
					temp_d2h_shape, #D2H_SIGNED,
					name=f"outp_d2h_data_{i}"
				)
				for i in range(NUM_HOSTS)
			]

		#outp_shape["d2h_found_arr"] = [
		#	FieldInfo(
		#		NUM_HOSTS,
		#		name=f"outp_h2d_found_arr_{j}"
		#	)
		#	for j in range(NUM_DEVS)
		#]
		#outp_shape["h2d_found_arr"] = [
		#	FieldInfo(
		#		NUM_DEVS,
		#		name=f"outp_d2h_found_arr_{j}"
		#	)
		#	for j in range(NUM_HOSTS)
		#]

		# Which hosts are connected to which devices
		outp_shape["h2d_conn_arr"] = [
			FieldInfo(
				#2 ** NUM_HOSTS,
				XbarSwitchIshape.SEL_WIDTH(NUM_HOSTS),
				name=f"outp_d2h_conn_arr_{j}",
			)
			for j in range(NUM_DEVS)
		]

		# Which devices are connected to which hosts
		outp_shape["d2h_conn_arr"] = [
			FieldInfo(
				#2 ** NUM_DEVS,
				XbarSwitchIshape.SEL_WIDTH(NUM_DEVS),
				name=f"outp_h2d_conn_arr_{i}",
			)
			for i in range(NUM_HOSTS)
		]
		#--------
		shape = IntfShape.mk_io_shape(
			shape_dct={
				"inp": inp_shape,
				"outp": outp_shape,
			},
			tag_dct=tag_dct,
			mk_modport_dct={
				"inp": True,
				"outp": True,
			},
		)
		super().__init__(shape)
		#--------
	@staticmethod
	def SEL_WIDTH(NUM_DEVS):
		return math.ceil(math.log2(NUM_DEVS))
class XbarSwitchBus:
	#--------
	def __init__(
		self,
		NUM_HOSTS, NUM_DEVS,
		*,
		FORMAL=False,
		h2d_shape=None,
		d2h_shape=None,
		tag_dct={
			"inp": None,
			"outp": None,
		},
	):
		#--------
		self.__NUM_HOSTS = NUM_HOSTS
		self.__NUM_DEVS = NUM_DEVS
		self.__FORMAL = FORMAL

		self.__h2d_shape = h2d_shape
		self.__d2h_shape = d2h_shape

		self.__tag_dct = tag_dct
		#--------
		#--------
		shape = XbarSwitchIshape(
			#h2d_shape=h2d_shape, d2h_shape=d2h_shape,
			NUM_HOSTS=NUM_HOSTS, NUM_DEVS=NUM_DEVS,
			h2d_shape=h2d_shape, d2h_shape=d2h_shape,
			#H2D_SIGNED=H2D_SIGNED, D2H_SIGNED=D2H_SIGNED,
			tag_dct=tag_dct,
		)
		self.__bus = Splitintf(shape, name="bus", use_parent_name=False)
		#--------
	#--------
	@property
	def bus(self):
		return self.__bus
	def NUM_HOSTS(self):
		return self.__NUM_HOSTS
	def NUM_DEVS(self):
		return self.__NUM_DEVS
	def FORMAL(self):
		return self.__FORMAL
	def h2d_shape(self):
		return self.__h2d_shape
	def d2h_shape(self):
		return self.__d2h_shape
	#def OPT_INCLUDE_H2D_DATA(self):
	#	return self.__OPT_INCLUDE_H2D_DATA
	#def OPT_INCLUDE_D2H_DATA(self):
	#	return self.__OPT_INCLUDE_D2H_DATA
	def SEL_WIDTH(self):
		#return math.ceil(math.log2(self.NUM_DEVS()))
		return XbarSwitchIshape.SEL_WIDTH(self.NUM_DEVS())
	def tag_dct(self):
		return self.__tag_dct
	def inp_tag(self):
		#return self.__inp_tag
		return self.tag_dct()["inp"]
	def outp_tag(self):
		#return self.__outp_tag
		return self.tag_dct()["outp"]
	#--------
#--------
# A crossbar switch
class XbarSwitch(Elaboratable):
	#--------
	# For `H2D_PRIO_LST_2D`, lower list indices mean higher priority
	def __init__(
		self,
		#h2d_shape, d2h_shape,
		NUM_HOSTS, NUM_DEVS,
		#H2D_SIGNED=False, D2H_SIGNED=False,
		*,
		FORMAL: bool=False,
		h2d_shape=None,
		d2h_shape=None,
		#OPT_INCLUDE_H2D_DATA: bool=True,
		#OPT_INCLUDE_D2H_DATA: bool=True,
		H2D_PRIO_LST_2D=None,
		DOMAIN=BasicDomain.COMB,
		tag_dct: dict={
			"inp": None,
			"outp": None,
		},
	):
	#def __init__(self, h2d_shape, d2h_shape, NUM_HOSTS, NUM_DEVS,
	#	H2D_SIGNED=False, D2H_SIGNED=False, *, H2D_PRIO_LST_2D=None,
	#	FORMAL=False):
		#--------
		self.__H2D_PRIO_LST_2D = XbarSwitch.mk_h2d_prio_lst_2d(
			NUM_HOSTS=NUM_HOSTS,
			NUM_DEVS=NUM_DEVS,
			SOME_H2D_PRIO_LST_2D=H2D_PRIO_LST_2D,
		)
		self.__D2H_PRIO_LST_2D = XbarSwitch.mk_d2h_prio_lst_2d(
			NUM_HOSTS=NUM_HOSTS,
			NUM_DEVS=NUM_DEVS,
			SOME_H2D_PRIO_LST_2D=self.H2D_PRIO_LST_2D(),
			#SOME_H2D_PRIOCASE_LST_2D=self.H2D_PRIO_LST_2D(),
		)
		self.__H2D_CASE_LST_2D = XbarSwitch.mk_h2d_case_lst_2d(
			NUM_DEVS=NUM_DEVS,
			SOME_H2D_PRIO_LST_2D=self.H2D_PRIO_LST_2D(),
		)
		#self.__D2H_CASE_LST_2D = XbarSwitch.mk_d2h_case_lst_2d(
		#	NUM_HOSTS=NUM_HOSTS,
		#	#NUM_DEVS=NUM_DEVS,
		#	SOME_D2H_PRIO_LST_2D=self.D2H_PRIO_LST_2D(),
		#	#SOME_H2D_CASE_LST_2D=self.H2D_CASE_LST_2D()
		#)
		#--------
		if not isinstance(DOMAIN, BasicDomain):
			raise TypeError(
				"`DOMAIN` `{!r}` must be a `BasicDomain`".format(DOMAIN)
			)
		self.__DOMAIN = DOMAIN
		#--------
		self.__bus = XbarSwitchBus(
			#h2d_shape=h2d_shape,
			#d2h_shape=d2h_shape,
			NUM_HOSTS=NUM_HOSTS,
			NUM_DEVS=NUM_DEVS,
			#H2D_SIGNED=H2D_SIGNED,
			#D2H_SIGNED=D2H_SIGNED,
			FORMAL=FORMAL,
			h2d_shape=h2d_shape,
			d2h_shape=d2h_shape,
			#OPT_INCLUDE_H2D_DATA=OPT_INCLUDE_H2D_DATA,
			#OPT_INCLUDE_D2H_DATA=OPT_INCLUDE_D2H_DATA,
			#inp_tag=inp_tag,
			#outp_tag=outp_tag,
			tag_dct=tag_dct,
		)
		#--------
	#--------
	def H2D_PRIO_LST_2D(self):
		return self.__H2D_PRIO_LST_2D
	def D2H_PRIO_LST_2D(self):
		return self.__D2H_PRIO_LST_2D
	def H2D_CASE_LST_2D(self):
		return self.__H2D_CASE_LST_2D
	#def D2H_CASE_LST_2D(self):
	#	return self.__D2H_CASE_LST_2D
	def DOMAIN(self):
		return self.__DOMAIN
	def bus(self):
		return self.__bus
	#--------
	@staticmethod
	def mk_h2d_prio_lst_2d(
		NUM_HOSTS,
		NUM_DEVS,
		SOME_H2D_PRIO_LST_2D,
	):
		if (
			not isinstance(SOME_H2D_PRIO_LST_2D, list)
			and not isinstance(SOME_H2D_PRIO_LST_2D, type(None))
		):
			raise TypeError(psconcat(
				"`SOME_H2D_PRIO_LST_2D` `{!r}` must be a `list` or `None`"
				.format(SOME_H2D_PRIO_LST_2D)
			))

		if isinstance(SOME_H2D_PRIO_LST_2D, list):
			if len(SOME_H2D_PRIO_LST_2D) != NUM_DEVS:
				raise ValueError(psconcat(
					"`SOME_H2D_PRIO_LST_2D` `{!r}` must be of length "
					"`NUM_DEVS` `{!r}`".format(H2D_PRIO_LST_2D)
				))

			#self.__H2D_PRIO_LST_2D = []

			for SOME_H2D_PRIO_LST in SOME_H2D_PRIO_LST_2D:
				temp = set(SOME_H2D_PRIO_LST)

				if (
					len(SOME_H2D_PRIO_LST) != len(temp)
					or temp != set(list(range(NUM_HOSTS)))
				):
					raise ValueError(psconcat(
						"`H2D_PRIO_LST` `{!r}` must ".format(
							SOME_H2D_PRIO_LST
						),
						"consist of all unique `int`s that are between 0 ",
						"and `NUM_HOSTS` `{!r}`".format(NUM_HOSTS)
					))

				#self.__H2D_PRIO_LST_2D.append(list(reversed(H2D_PRIO_LST)))

			#self.__H2D_PRIO_LST_2D = H2D_PRIO_LST_2D
			return SOME_H2D_PRIO_LST_2D
		else: # if isinstance(SOME_H2D_PRIO_LST, None):
			#self.__H2D_PRIO_LST_2D = [
			#	[NUM_HOSTS - (i + 1) for i in range(NUM_HOSTS)]
			#		for j in range(NUM_DEVS)
			#]
			#self.__H2D_PRIO_LST_2D = [
			#	list(reversed([i for i in range(NUM_HOSTS)]))
			#		for j in range(NUM_DEVS)
			#]
			#self.__H2D_PRIO_LST_2D = [
			#	[i for i in range(NUM_HOSTS)]
			#	for j in range(NUM_DEVS)
			#]
			return [
				[i for i in range(NUM_HOSTS)]
				for j in range(NUM_DEVS)
			]
	@staticmethod
	def mk_d2h_prio_lst_2d(
	#def mk_d2h_priocase_lst_2d
		NUM_HOSTS,
		NUM_DEVS,
		SOME_H2D_PRIO_LST_2D,
		#SOME_H2D_PRIOCASE_LST_2D,
	):
		return [
			[SOME_H2D_PRIO_LST_2D[j][i] for j in range(NUM_DEVS)]
			for i in range(NUM_HOSTS)
		]
		#return [
		#	[SOME_H2D_PRIOCASE_LST_2D[j][i] for j in range(NUM_DEVS)]
		#	for i in range(NUM_HOSTS)
		#]
	@staticmethod
	def mk_h2d_case_lst_2d(
		NUM_DEVS,
		SOME_H2D_PRIO_LST_2D,
	):
		ret = []
		for j in range(NUM_DEVS):
			SOME_H2D_PRIO_LST = SOME_H2D_PRIO_LST_2D[j]

			ret.append([])

			for i in range(len(SOME_H2D_PRIO_LST)):
				CASE = ["-"] * len(SOME_H2D_PRIO_LST)
				CASE[SOME_H2D_PRIO_LST[i]] = "1";

				k = 0
				while k < i:
					CASE[SOME_H2D_PRIO_LST[k]] = "0"
					k += 1
				k += 1
				while k < len(SOME_H2D_PRIO_LST):
					CASE[SOME_H2D_PRIO_LST[k]] = "-"
					k += 1
				ret[-1].append(CASE)
		return ret
	#@staticmethod
	#def mk_d2h_case_lst_2d(
	#	NUM_HOSTS,
	#	#NUM_DEVS,
	#	SOME_H2D_CASE_LST_2D,
	#	#SOME_D2H_PRIO_LST_2D,
	#):
	#	#return [
	#	#	[SOME_H2D_CASE_LST_2D[j][i] for j in range(NUM_DEVS)]
	#	#	for i in range(NUM_HOSTS)
	#	#]
	#	#return XbarSwitch.mk_h2d_case_lst_2d(
	#	#	NUM_DEVS=NUM_HOSTS,
	#	#	SOME_H2D_PRIO_LST_2D=SOME_D2H_PRIO_LST_2D
	#	#),
	#	f = open("test-XbarSwitch-mk_d2h_prio_lst_2d.txt.ignore", "w")
	#	f.close()
	#--------
	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		bus = self.bus()
		inp = bus.bus.inp
		outp = bus.bus.outp

		H2D_PRIO_LST_2D = self.H2D_PRIO_LST_2D()
		H2D_CASE_LST_2D = self.H2D_CASE_LST_2D()
		D2H_PRIO_LST_2D = self.D2H_PRIO_LST_2D()
		#D2H_CASE_LST_2D = self.D2H_CASE_LST_2D()
		#with open("test-XbarSwitch-PRIO_CASE.txt.ignore", "w") as f:
		#	f.writelines([
		#		"H2D_PRIO_LST_2D\n",
		#		#do_psconcat_flattened(H2D_PRIO_LST_2D),
		#	])
		#	temp = ""
		#	for H2D_PRIO_LST in H2D_PRIO_LST_2D:
		#		#temp += "".join(H2D_PRIO_LST)
		#		for PRIO in H2D_PRIO_LST:
		#			temp += psconcat(PRIO, " ")
		#		temp += "\n"
		#	f.writelines([
		#		temp, "\n"
		#	])
		#	f.writelines([
		#		"H2D_CASE_LST_2D\n",
		#		#do_psconcat_flattened("".join(H2D_CASE_LST_2D)),
		#		#"".join(H2D_CASE_LST_2D),
		#	])
		#	temp = ""
		#	for H2D_CASE_LST in H2D_CASE_LST_2D:
		#		#temp += "".join(H2D_CASE_LST)
		#		for CASE in H2D_CASE_LST:
		#			temp += psconcat(CASE, " ")
		#		temp += "\n"
		#	#f.writelines([
		#	#	temp
		#	#])
		#	f.writelines([
		#		temp, "\n"
		#	])
		#	f.writelines([
		#		"D2H_PRIO_LST_2D\n",
		#		#do_psconcat_flattened(D2H_PRIO_LST_2D),
		#	])
		#	temp = ""
		#	for D2H_PRIO_LST in D2H_PRIO_LST_2D:
		#		#temp += "".join(D2H_PRIO_LST)
		#		for PRIO in D2H_PRIO_LST:
		#			temp += psconcat(PRIO, " ")
		#		temp += "\n"
		#	f.writelines([
		#		temp, "\n"
		#	])
		#	#f.writelines([
		#	#	"D2H_CASE_LST_2D\n",
		#	#	#do_psconcat_flattened("".join(D2H_CASE_LST_2D)),
		#	#	#"".join(D2H_CASE_LST_2D),
		#	#])
		#	#temp = ""
		#	#for D2H_CASE_LST in D2H_CASE_LST_2D:
		#	#	#temp += "".join(D2H_CASE_LST)
		#	#	for CASE in D2H_CASE_LST:
		#	#		temp += psconcat(CASE, " ")
		#	#	temp += "\n"
		#	#f.writelines([
		#	#	temp
		#	#])

		loc = Blank()
		#loc.found_arr = Splitarr([
		#	FieldInfo(bus.NUM_HOSTS(), name=f"found_arr_{j}")
		#		for j in range(bus.NUM_DEVS())
		#])

		loc.h2d_found_arr = cast_shape([
			FieldInfo(
				bus.NUM_DEVS(),
				name=f"loc_d2h_found_arr_{i}"
			)
			for i in range(bus.NUM_HOSTS())
		])
		loc.d2h_found_arr = cast_shape([
			FieldInfo(
				bus.NUM_HOSTS(),
				name=f"loc_h2d_found_arr_{j}"
			)
			for j in range(bus.NUM_DEVS())
		])
		#loc.d2h_conn_arr = View(
		#	ArrayLayout(
		#		XbarSwitchIshape.SEL_WIDTH(NUM_DEVS), NUM_HOSTS,
		#	),
		#	name="loc_d2h_conn_arr",
		#)
		#loc.h2d_conn_arr = View(
		#	ArrayLayout(
		#		XbarSwitchIshape.SEL_WIDTH(NUM_HOSTS), NUM_DEVS,
		#	),
		#	name="loc_d2h_conn_arr",
		#)

		md = basic_domain_to_actual_domain(m, self.DOMAIN())
		#--------
		#for i in range(len(H2D_PRIO_LST)):
		#	#with m.Else():
		#	with m.If(loc.match_arr_z[i].any()):
		#		md += [
		#			outp.d2h_data[H2D_PRIO_LST[i]].eq(0x0)
		#		]
		#for i in range(bus.NUM_HOSTS()):
		#	m.d.sync += [
		#		loc.saved_h2d_data[i].eq(outp.h2d_data[i]),
		#	]
		for j in range(bus.NUM_DEVS()):
			for i in range(bus.NUM_HOSTS()):
				m.d.comb += [
					loc.d2h_found_arr[j][i].eq(
						inp.sel_conn[i] & (inp.sel[i] == j)
					),
					loc.h2d_found_arr[i][j].eq(loc.d2h_found_arr[j][i]),
					#outp.hosts_active[i][j].eq(out.devs_active[j][i]),
				]
				#with m.If(d2h_found_arr[j][i]):
				#	md += [
				#		outp.d2h_conn_arr[i].eq(j)
				#	]
			#H2D_PRIO_LST = list(reversed(H2D_PRIO_LST_2D[j]))

			#m.d.sync += [
			#	loc.saved_d2h_data[j].eq(outp.d2h_data[j]),
			#]

			H2D_PRIO_LST = H2D_PRIO_LST_2D[j]
			H2D_CASE_LST = H2D_CASE_LST_2D[j]

			with m.Switch(loc.d2h_found_arr[j]):
				for i in range(len(H2D_PRIO_LST)):
					PRIO = H2D_PRIO_LST[i]
					#CASE = "".join(list(reversed(H2D_CASE_LST[i])))
					#CASE = H2D_CASE_LST[i]
					CASE = list(reversed(H2D_CASE_LST[i]))

					with m.Case("".join(CASE)):
						md += [
							outp.d2h_conn_arr[PRIO].eq(j),
							outp.h2d_conn_arr[j].eq(PRIO),
							#outp.d2h_conn_arr[i].eq(j),
							#outp.h2d_conn_arr[j].eq(i),
						]
						if bus.h2d_shape() is not None:
							md += [
								outp.h2d_data[j].eq(
									inp.h2d_data[PRIO]
									#inp.h2d_data[i]
								),
							]
						if bus.d2h_shape() is not None:
							md += [
								outp.d2h_data[PRIO].eq(
									inp.d2h_data[j]
								),
								#outp.d2h_data[i].eq(inp.d2h_data[j]),
							]
				#with m.Default():
				#	for i in range(len(H2D_PRIO_LST)):
				#		m.d.comb += [
				#			outp.hosts_active[i].eq(0b0),
				#		]

		for j in range(bus.NUM_DEVS()):
			md += [
				#outp.devs_active[j].eq(loc.match_arr[j].any()),
				outp.devs_active[j].eq(loc.d2h_found_arr[j].any()),
			]
		for i in range(bus.NUM_HOSTS()):
			md += [
				outp.hosts_active[i].eq(loc.h2d_found_arr[i].any()),
			]
		#--------
		if bus.FORMAL():
		#if FORMAL:
			if self.DOMAIN() != BasicDomain.COMB:
				raise ValueError(("`self.DOMAIN()` `{!r}` must be "
					+ "`BasicDomain.COMB` when doing formal verification")
					.format(self.DOMAIN()))
			#if H2D_PRIO_LST_2D != [
			#		#[
			#		#	bus.NUM_HOSTS() - (i + 1)
			#		#	for i in range(bus.NUM_HOSTS())
			#		#]
			#		[i for i in range(bus.NUM_HOSTS())]
			#		for j in range(bus.NUM_DEVS())
			#]:
			#	raise ValueError(("`H2D_PRIO_LST_2D` `{!r}` must be the "
			#		+ "default when doing formal verification")
			#		.format(H2D_PRIO_LST_2D))
			#m.d.sync += loc.formal.past_valid.eq(0b1)

			#with m.If((~ResetSignal()) & loc.formal.past_valid):
			for j in range(bus.NUM_DEVS()):
				H2D_PRIO_LST = H2D_PRIO_LST_2D[j]
				#H2D_PRIO_LST = list(reversed(H2D_PRIO_LST_2D[j]))

				if (
					#bus.OPT_INCLUDE_D2H_DATA()
					is_builtin_shape(bus.d2h_shape())
				):
					#d2h_shape = Shape.cast(bus.d2h_shape())
					#value_out_0 = Value.cast(outp.d2h_data[0])
					for i in range(bus.NUM_HOSTS()):
						PRIO = H2D_PRIO_LST[i]
						value_out = Value.cast(
							outp.d2h_data[PRIO]
							#outp.d2h_data[i]
						)
						value_in = Value.cast(inp.d2h_data[j])
						#temp_d2h_found = loc.d2h_found_arr[j][PRIO]
						temp_d2h_found = loc.d2h_found_arr[j][i]
						temp_eq_found = (
							(value_in == value_out)
							& temp_d2h_found
						)
						temp_ne_found = (
							(value_in != value_out)
							& temp_d2h_found
						)
						temp_eq_not_found = (
							(value_in == value_out)
							& ~temp_d2h_found
						)
						temp_ne_not_found = (
							(value_in != value_out)
							& ~temp_d2h_found
						)

						for k in range(2 ** len(value_out)):
							#with m.If(value_out == k):
							temp_eq_k = (
								(value_out == k)
								& (value_in == k)
							)
							m.d.comb += [
								Cover(
									temp_eq_k
									& temp_eq_found
								),
								Cover(
									temp_eq_k
									#& temp_eq_not_found
									& ~temp_d2h_found
								),
							]

						m.d.comb += [
							Cover(temp_eq_found),
							Cover(temp_ne_found),
							Cover(temp_eq_not_found),
							Cover(temp_ne_not_found),
							#Cover(
							#	(value_in != value_out)
							#	^ temp_d2h_found
							#),
							#Cover(
							#	outp.devs_active[j]
							#	& temp_d2h_found
							#),
							#Cover(
							#	outp.hosts_active[PRIO]
							#	& temp_d2h_found
							#),
							#Cover(
							#	(value_in == value_out)
							#	& temp_d2h_found
							#),
							Cover(
								temp_eq_found
								& outp.devs_active[j]
								& outp.hosts_active[i]
							),
							Cover(
								temp_ne_found
								& outp.devs_active[j]
								& outp.hosts_active[i]
							),
							Cover(
								temp_ne_not_found
								& ~outp.devs_active[j]
								& ~outp.hosts_active[i]
							),
						]

				PRIO = H2D_PRIO_LST[0]
				with m.If(loc.d2h_found_arr[j][PRIO]):
				#with m.If(loc.d2h_found_arr[j][0]):
					m.d.comb += [
						Assert(outp.devs_active[j]),
						Assert(outp.hosts_active[PRIO]),
					]
					m.d.comb += [
						#Assert(outp.h2d_conn_arr[PRIO] == j),
						#Assert(outp.h2d_conn_arr[PRIO] == j),
						Assert(outp.h2d_conn_arr[j] == PRIO),
						#Assert(outp.h2d_conn_arr[j] == 0),
					]
					m.d.comb += [
						#Assert(outp.d2h_conn_arr[j] == PRIO),
						#Assert(outp.d2h_conn_arr[0] == H2D_PRIO_LST[j]),
						Assert(outp.d2h_conn_arr[PRIO] == j),
						#Assert(outp.d2h_conn_arr[0] == j),
					]
					if bus.h2d_shape() is not None:
						m.d.comb += [
							Assert(
								outp.h2d_data[j]
								== inp.h2d_data[PRIO]
								#outp.h2d_data[j]
								#== inp.h2d_data[0]
							)
						]
					if bus.d2h_shape() is not None:
						m.d.comb += [
							Assert(
								outp.d2h_data[PRIO]
									== inp.d2h_data[j]
								#outp.d2h_data[0]
								#	== inp.d2h_data[j]
							)
						]
				#for i in range(1, bus.NUM_HOSTS()):
				for i in range(1, len(H2D_PRIO_LST)):
					PRIO = H2D_PRIO_LST[i]
					with m.Elif(loc.d2h_found_arr[j][PRIO]):
					#with m.Elif(loc.d2h_found_arr[j][i]):
						m.d.comb += [
							Assert(outp.devs_active[j]),
							Assert(outp.hosts_active[PRIO]),
						]
						m.d.comb += [
							Assert(
								outp.h2d_conn_arr[j] == PRIO
								#outp.h2d_conn_arr[j] == i
							),
						]
						m.d.comb += [
							Assert(
								outp.d2h_conn_arr[PRIO] == j
								#outp.d2h_conn_arr[i] == j
							),
						]
						if bus.h2d_shape() is not None:
							m.d.comb += [
								Assert(
									outp.h2d_data[j] == inp.h2d_data[PRIO]
									#outp.h2d_data[j] == inp.h2d_data[i]
								)
							]
						if bus.d2h_shape() is not None:
							m.d.comb += [
								Assert(
									outp.d2h_data[PRIO] == inp.d2h_data[j]
									#outp.d2h_data[i] == inp.d2h_data[j]
								)
							]
				with m.Else():
					m.d.comb += [
						Assert(~outp.devs_active[j]),
						#Assert(outp.devs_active == 0x0)
						#Assert(outp.h2d_conn_arr[j] == 0x0),
					]
			for i in range(len(H2D_PRIO_LST)):
				#PRIO = H2D_PRIO_LST[i]
				#with m.If(outp.hosts_active[PRIO])
				with m.If(~loc.h2d_found_arr[i].any()):
					#with m.If(~loc.d2h_found_arr[j][i]):
					m.d.comb += [
						Assert(~outp.hosts_active[i])
					]
		#--------
		return m
		#--------
	#--------
#--------
#def PSB_XBAR_SWITCH_DEF_TAG_DCT():
#	return {
#		#"xbar": {
#		#	"inp": "xbar_inp",
#		#	"outp": "xbar_outp",
#		#},
#		"misc": "misc",
#		"h2d": "h2d",
#		"d2h": "d2h",
#	}
## "Psb" is short for `PipeSkidBuf`
#class PsbXbarSwitchIshape(IntfShape):
#	def __init__(
#		self,
#		h2d_data_shape, d2h_data_shape,
#		NUM_HOSTS, NUM_DEVS,
#		*,
#		#in_from: bool,
#		tag=None,
#		tag_dct: dict=PSB_XBAR_SWITCH_DEF_TAG_DCT(),
#		#tag_prefix_dct: dict={
#		#	"h2d": "h2d",
#		#	"d2h": "d2h",
#		#},
#	):
#		#--------
#		#xbar_shape = XbarSwitchIshape(
#		#	h2d_shape=None, d2h_shape=None,
#		#	NUM_HOSTS=NUM_HOSTS, NUM_DEVS=NUM_DEVS,
#		#	#OPT_INCLUDE_DATA=False,
#		#	tag_dct=tag_dct["xbar"],
#		#)
#		misc_shape = IntfShape(
#			shape={
#				"sel": [
#					FieldInfo(
#						XbarSwitchIshape.SEL_WIDTH(NUM_DEVS),
#						name=f"misc_sel_{i}"
#					)
#					for i in range(NUM_HOSTS)
#				],
#			},
#			modport=Modport({"sel": PortDir.Inp}),
#			tag=tag_dct["misc"],
#		)
#		h2d_shape_lst = [
#			PsbXbarSwitchIshape.mk_single_node_shape_info(
#				data_shape=h2d_data_shape,
#				#index=i,
#				in_d2h=False,
#				#tag_prefix=tag_prefix_dct["h2d"],
#				tag=tag_dct["h2d"],
#			)["shape"]
#			for i in range(NUM_HOSTS)
#		]
#		d2h_shape_lst = [
#			PsbXbarSwitchIshape.mk_single_node_shape_info(
#				data_shape=d2h_data_shape,
#				#index=i,
#				in_d2h=True,
#				#tag_prefix=tag_prefix_dct["d2h"],
#				tag=tag_dct["d2h"],
#			)["shape"]
#			for i in range(NUM_DEVS)
#		]
#		#h2d_shape_lst = [elem["shape"] for elem in h2d_shape_info_lst]
#		#d2h_shape_lst = [elem["shape"] for elem in d2h_shape_info_lst]
#		h2d_iarr_shape = IntfarrShape(h2d_shape_lst)
#		d2h_iarr_shape = IntfarrShape(d2h_shape_lst)
#		shape = {
#			"misc": misc_shape,
#			"h2d": h2d_iarr_shape,
#			"d2h": d2h_iarr_shape,
#		}
#		super().__init__(shape=shape, tag=tag)
#		#--------
#	@staticmethod
#	def mk_single_node_shape_info(
#		data_shape,
#		#index: int,
#		*,
#		in_d2h: bool,
#		#tag_prefix: str,
#		tag: str,
#		OPT_INCLUDE_VALID_BUSY: bool=True,
#		OPT_INCLUDE_READY_BUSY: bool=True,
#	):
#		#name = "h2d" if not in_d2h else "d2h"
#		name = "data"
#		#name = "arr"
#		#tag = psconcat(tag_prefix, "_", index)
#
#		return PipeSkidBufIshape.mk_fromto_shape_info(
#			data_layt_dct={
#				"from": data_shape if in_d2h else None,
#				"to": data_shape if not in_d2h else None,
#			},
#			in_from=in_d2h,
#			name_dct={
#				"from": name if in_d2h else None,
#				"to": name if not in_d2h else None,
#			},
#			tag_dct={
#				"from": tag if in_d2h else None,
#				"to": tag if not in_d2h else None,
#			},
#			OPT_IN_FROM_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
#			OPT_IN_FROM_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
#			OPT_NOT_IN_FROM_INCLUDE_VALID_BUSY=OPT_INCLUDE_VALID_BUSY,
#			OPT_NOT_IN_FROM_INCLUDE_READY_BUSY=OPT_INCLUDE_READY_BUSY,
#		)
#	@staticmethod
#	def def_tag_dct():
#		return PSB_XBAR_SWITCH_DEF_TAG_DCT()
#class PsbXbarSwitchBus:
#	def __init__(
#		self,
#		h2d_data_shape, d2h_data_shape,
#		NUM_HOSTS, NUM_DEVS,
#		*,
#		FORMAL: bool=False,
#		tag=None,
#		tag_dct: dict=PSB_XBAR_SWITCH_DEF_TAG_DCT(),
#		#tag_prefix_dct: dict={
#		#	"h2d": "h2d",
#		#	"d2h": "d2h",
#		#},
#	):
#		self.__h2d_data_shape = h2d_data_shape
#		self.__d2h_data_shape = d2h_data_shape
#		self.__NUM_HOSTS = NUM_HOSTS
#		self.__NUM_DEVS = NUM_DEVS
#		self.__FORMAL = FORMAL
#		self.__tag = tag
#		self.__tag_dct = tag_dct
#		#self.__tag_prefix_dct = tag_prefix_dct 
#		shape = PsbXbarSwitchIshape(
#			h2d_data_shape=h2d_data_shape,
#			d2h_data_shape=d2h_data_shape,
#			NUM_HOSTS=NUM_HOSTS,
#			NUM_DEVS=NUM_DEVS,
#			tag=tag,
#			tag_dct=tag_dct,
#		)
#		self.__bus = Splitintf(shape, name="bus", use_parent_name=True)
#	@property
#	def bus(self):
#		return self.__bus
#	def h2d_data_shape(self):
#		return self.__h2d_data_shape
#	def d2h_data_shape(self):
#		return self.__d2h_data_shape
#	def NUM_HOSTS(self):
#		return self.__NUM_HOSTS
#	def NUM_DEVS(self):
#		return self.__NUM_DEVS
#	def FORMAL(self):
#		return self.__FORMAL
#	def tag(self):
#		return self.__tag
#	def tag_dct(self):
#		return self.__tag_dct
#	#def tag_prefix_dct(self):
#	#	return self.__tag_prefix_dct
#class PsbXbarSwitch(Elaboratable):
#	def __init__(
#		self,
#		h2d_data_shape, d2h_data_shape,
#		NUM_HOSTS, NUM_DEVS,
#		*,
#		FORMAL: bool=False,
#		H2D_PRIO_LST_2D=None,
#		tag=None,
#		tag_dct: dict={
#			"h2d": "h2d",
#			"d2h": "d2h",
#		},
#	):
#		self.__bus = PsbXbarSwitchBus(
#			h2d_data_shape=h2d_data_shape,
#			d2h_data_shape=d2h_data_shape,
#			NUM_HOSTS=NUM_HOSTS,
#			NUM_DEVS=NUM_DEVS,
#			FORMAL=FORMAL,
#			tag=tag,
#			tag_dct=tag_dct,
#		)
#		self.__FORMAL = FORMAL
#		#self.__H2D_PRIO_LST_2D = H2D_PRIO_LST_2D
#		self.__H2D_PRIO_LST_2D = XbarSwitch.mk_h2d_prio_lst_2d(
#			NUM_HOSTS=NUM_HOSTS,
#			NUM_DEVS=NUM_DEVS,
#			H2D_PRIO_LST_2D=H2D_PRIO_LST_2D,
#		)
#	def bus(self):
#		return self.__bus
#	#def FORMAL(self):
#	#	return self.__FORMAL
#	def H2D_PRIO_LST_2D(self):
#		return self.__H2D_PRIO_LST_2D
#	def elaborate(self, platform: str) -> Module:
#		#--------
#		m = Module()
#		#--------
#		bus = self.bus()
#		h2d = bus.bus.h2d
#		d2h = bus.bus.d2h
#		h2d_data_shape = bus.h2d_data_shape()
#		d2h_data_shape = bus.d2h_data_shape()
#		FORMAL = bus.FORMAL()
#
#		loc = Blank()
#		#loc.DOMAIN = BasicDomain.COMB
#		loc.m = Blank()
#		#--------
#		loc.h2d_shape_info = PsbXbarSwitchIshape.mk_single_node_shape_info(
#			data_shape=h2d_data_shape,
#			in_d2h=False,
#			tag=bus.tag_dct()["h2d"],
#			OPT_INCLUDE_VALID_BUSY=True,
#			OPT_INCLUDE_READY_BUSY=True,
#		)
#		loc.d2h_shape_info = PsbXbarSwitchIshape.mk_single_node_shape_info(
#			data_shape=d2h_data_shape,
#			in_d2h=True,
#			tag=bus.tag_dct()["d2h"],
#			OPT_INCLUDE_VALID_BUSY=True,
#			OPT_INCLUDE_READY_BUSY=True,
#		)
#		loc.m.h2d_sb = [
#			PipeSkidBuf(**loc.h2d_shape_info["to"])
#			for i in range(bus.NUM_HOSTS())
#		]
#		loc.m.d2h_sb = [
#			PipeSkidBuf(**loc.d2h_shape_info["from"])
#			for i in range(bus.NUM_DEVS())
#		]
#		loc.h2d_sb_bus = [
#			loc.m.h2d_sb.bus().bus
#			for i in range(len(loc.m.h2d_sb))
#		]
#		loc.d2h_sb_bus = [
#			loc.m.d2h_sb.bus().bus
#			for i in range(len(loc.m.d2h_sb))
#		]
#		#m.submodules.xbar = loc.m.xbar = XbarSwitch(
#		#	self,
#		#	#h2d_shape=h2d[0].shape().  d2h_shape=d2h[0].shape(),
#		#	NUM_HOSTS=bus.NUM_HOSTS(), NUM_DEVS=bus.NUM_DEVS(),
#		#	FORMAL=FORMAL,
#		#	H2D_PRIO_LST_2D=self.H2D_PRIO_LST_2D()
#		#	DOMAIN=BasicDomain.COMB,
#		#	tag_dct={
#		#		"inp": 
#		#	}
#		#)
#		#--------
#		return m
#		#--------
##--------
