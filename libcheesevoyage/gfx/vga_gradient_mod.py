#!/usr/bin/env python3

from amaranth import *

from libcheesevoyage.misc_util import *

class VgaGradient(Elaboratable):
	#--------
	def __init__(self, vga_drbus, vga_dibus):
		self.__drbus, self.__dibus = vga_drbus, vga_dibus
	#--------
	def elaborate(self, platform: str):
		#--------
		m = Module()
		#--------
		drbus, dibus = self.__drbus, self.__dibus
		col = dibus.inp.col
		#--------
		# Gradient
		with m.If(drbus.outp.buf.can_prep):
			m.d.sync += [
				drbus.inp.buf.prep.eq(0b1),
				dibus.inp.en.eq(0b1),
			]

			with m.If(dibus.outp.next_pos.x == 0x0):
				m.d.sync += col.r.eq(0x0)
			with m.Else(): # If(dibus.outp.next_pos.x > 0x0)
				m.d.sync += col.r.eq(col.r + 0x1)
			#m.d.sync += col.r.eq(col.r + 0x1)

			m.d.sync += col.g.eq(0x0)
			m.d.sync += col.b.eq(0x0)
		with m.Else(): # If(~drbus.buf.can_prep):
			m.d.sync += [
				#drbus.buf.prep.eq(0b0),
				dibus.inp.en.eq(0b0),
			]
		#--------
		return m
		#--------
	#--------
