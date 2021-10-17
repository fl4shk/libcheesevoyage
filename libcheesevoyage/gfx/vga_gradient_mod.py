#!/usr/bin/env python3

from nmigen import *

from ..misc_util import *

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
		col = dibus.col_in
		#--------
		# Gradient
		with m.If(drbus.buf.can_prep):
			m.d.sync \
			+= [
				drbus.buf.prep.eq(0b1),
				dibus.en.eq(0b1),
			]

			with m.If(dibus.next_pos.x == 0x0):
				m.d.sync += col.r.eq(0x0)
			with m.Else(): # If(dibus.next_pos.x > 0x0)
				m.d.sync += col.r.eq(col.r + 0x1)
			#m.d.sync += col.r.eq(col.r + 0x1)

			m.d.sync += col.g.eq(0x0)
			m.d.sync += col.b.eq(0x0)
		with m.Else(): # If(~drbus.buf.can_prep):
			m.d.sync \
			+= [
				#drbus.buf.prep.eq(0b0),
				dibus.en.eq(0b0),
			]
		#--------
		return m
		#--------
	#--------
