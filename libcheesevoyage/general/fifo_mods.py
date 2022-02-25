#!/usr/bin/env python3

from amaranth import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from libcheesevoyage.misc_util import *
from libcheesevoyage.general.container_types import *
#--------
class FifoBus:
	def __init__(self, ShapeT, SIZE):
		self.__ShapeT, self.__SIZE = ShapeT, SIZE
		#--------
		self.inp = Splitrec()
		self.outp = Splitrec()
		#--------
		#self.clk = Signal()
		#self.rst = Signal()
		#--------
		# Inputs
		self.inp.wr_en = Signal()
		self.inp.wr_data = Signal(self.ShapeT())

		self.inp.rd_en = Signal()
		#--------
		# Outputs
		self.outp.rd_data = Signal(self.ShapeT())

		self.outp.empty = Signal()
		self.outp.full = Signal()
		#--------

	def ShapeT(self):
		return self.__ShapeT
	def SIZE(self):
		return self.__SIZE

	#def ports(self):
	#	#return [self.clk, self.rst, self.wr_en, self.wr_data, self.rd_en,
	#	#	self.rd_data, self.empty, self.full]
	#	return [ClockSignal(), ResetSignal(), self.wr_en, self.wr_data,
	#		self.rd_en, self.rd_data, self.empty, self.full]
#--------
class Fifo(Elaboratable):
	def __init__(self, ShapeT, SIZE, *, FORMAL=False):
		self.__bus = FifoBus(ShapeT=ShapeT, SIZE=SIZE)
		self.__FORMAL = FORMAL

	def bus(self):
		return self.__bus
	def FORMAL(self):
		return self.__FORMAL

	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()

		#add_clk_domain(m, self.bus().clk)
		#add_clk_from_domain(m, self.bus.clk())
		#--------
		# Local variables
		bus = self.bus()
		inp = bus.inp
		outp = bus.outp

		loc = Blank()

		loc.arr = Array([Signal(bus.ShapeT()) for _ in range(bus.SIZE())])

		loc.PTR_WIDTH = width_from_arg(bus.SIZE())

		loc.tail = Signal(loc.PTR_WIDTH)
		loc.head = Signal(loc.PTR_WIDTH)

		loc.TAIL_PLUS_1 = loc.tail + 0x1
		loc.HEAD_PLUS_1 = loc.head + 0x1

		loc.incr_tail = Signal(loc.PTR_WIDTH)
		loc.incr_head = Signal(loc.PTR_WIDTH)

		loc.next_empty = Signal()
		loc.next_full = Signal()

		loc.next_tail = Signal(loc.PTR_WIDTH)
		loc.next_head = Signal(loc.PTR_WIDTH)

		loc.clk, loc.rst = ClockSignal(), ResetSignal()
		#loc.rst = bus.rst

		#loc.curr_en_cat = Signal(2)

		if self.FORMAL():
			loc.formal = Blank()

			loc.formal.last_tail_val = Signal(bus.ShapeT())
			loc.formal.test_head = Signal(loc.PTR_WIDTH)
			#loc.formal.empty = Signal()
			#loc.formal.full = Signal()
			loc.formal.wd_cnt = Signal(bus.ShapeT(), reset=0xa0)
		#--------
		if self.FORMAL():
			m.d.sync \
			+= [
				loc.formal.last_tail_val.eq(loc.arr[loc.tail]),
				loc.formal.wd_cnt.eq(loc.formal.wd_cnt - 0x10)
			]
			m.d.comb \
			+= [
				loc.formal.test_head.eq((loc.head + 0x1) % bus.SIZE()),
			]
		#--------
		# Combinational logic

		m.d.comb \
		+= [
			loc.incr_tail.eq(Mux(loc.TAIL_PLUS_1 < bus.SIZE(),
				(loc.tail + 0x1), 0x0)),
			loc.incr_head.eq(Mux(loc.HEAD_PLUS_1 < bus.SIZE(),
				(loc.head + 0x1), 0x0)),

			loc.next_empty.eq(loc.next_head == loc.next_tail),
			#loc.next_full.eq((loc.next_head + 0x1) == loc.next_tail),

			#loc.curr_en_cat.eq(Cat(inp.rd_en, inp.wr_en)),
		]

		with m.If(inp.rd_en & (~outp.empty)):
			m.d.comb += loc.next_tail.eq(loc.incr_tail)
		with m.Else():
			m.d.comb += loc.next_tail.eq(loc.tail)

		with m.If(inp.wr_en & (~outp.full)):
			m.d.comb \
			+= [
				loc.next_head.eq(loc.incr_head),
				loc.next_full.eq((loc.incr_head + 0x1) == loc.next_tail),
			]
		with m.Else():
			m.d.comb \
			+= [
				loc.next_head.eq(loc.head),
				loc.next_full.eq(loc.incr_head == loc.next_tail), 
			]

		if self.FORMAL():
			m.d.comb \
			+= [
				Cover(outp.empty),
				Cover(outp.full),
				Cover((~outp.empty) & (~outp.full)),
			]
		#--------
		# Clocked behavioral code
		if self.FORMAL():
			loc.formal.past_valid = Signal()

		with m.If(loc.rst):
			#for elem in loc.arr:
			#	m.d.sync += elem.eq(bus.ShapeT()())

			m.d.sync \
			+= [
				loc.tail.eq(0x0),
				loc.head.eq(0x0),

				#outp.rd_data.eq(bus.ShapeT()()),

				outp.empty.eq(0b1),
				outp.full.eq(0b0),
			]
			if self.FORMAL():
				m.d.sync += loc.formal.past_valid.eq(0b1)

		with m.Else(): # If(~loc.rst):
			#--------
			m.d.sync \
			+= [
				outp.empty.eq(loc.next_empty),
				outp.full.eq(loc.next_full),
				loc.tail.eq(loc.next_tail),
				loc.head.eq(loc.next_head),
			]

			with m.If(inp.rd_en & (~outp.empty)):
				m.d.sync += outp.rd_data.eq(loc.arr[loc.tail])
			with m.If(inp.wr_en & (~outp.full)):
				m.d.sync += loc.arr[loc.head].eq(inp.wr_data)
			#--------
			if self.FORMAL():
				with m.If(loc.formal.past_valid):
					m.d.sync \
					+= [
						Assert(outp.empty == Past(loc.next_empty)),
						Assert(outp.full == Past(loc.next_full)),
						Assert(loc.tail == Past(loc.next_tail)),
						Assert(loc.head == Past(loc.next_head)),
					]
					with m.If(Past(inp.rd_en)):
						with m.If(Past(outp.empty)):
							m.d.sync \
							+= [
								#Assert(Stable(outp.empty)),
								Assert(Stable(loc.tail)),
							]
						with m.Else(): # If(~Past(outp.empty)):
							#with m.If(~Past(inp.wr_en)):
							m.d.sync \
							+= [
								Assert(outp.rd_data
									== loc.arr[Past(loc.tail)])
							]
					with m.If(Past(inp.wr_en)):
						with m.If(Past(outp.full)):
							m.d.sync \
							+= [
								Assert(Stable(loc.head)),
							]
						#with m.Else(): # If(~Past(outp.full)):
						#	m.d.sync \
						#	+= [
						#		Assert(Past(inp.wr_data))
						#	]
					with m.Switch(Cat(outp.empty, outp.full)):
						with m.Case(0b00):
							m.d.sync \
							+= [
								Assume(loc.head != loc.tail),
								Assume(loc.formal.test_head != loc.tail),
							]
						with m.Case(0b01):
							m.d.sync \
							+= [
								Assert(loc.head == loc.tail)
							]
						with m.Case(0b10):
							m.d.sync \
							+= [
								Assert(loc.formal.test_head == loc.tail),
							]
					m.d.sync \
					+= [
						Assert(~(outp.empty & outp.full)),
						#Assume(~Stable(inp.wr_data)),
						#Assume(inp.wr_data == loc.formal.wd_cnt),
					]
			#--------
		#--------
		return m
		#--------
#--------
# Asynchronous Read FIFO
class AsyncReadFifo(Fifo):
	def __init__(self, ShapeT, SIZE, *, FORMAL=False):
		super().__init__(ShapeT, SIZE, FORMAL)

	def elaborate(self, platform: str) -> Module:
		#--------
		m = Module()
		#--------
		# Local variables
		bus = self.bus()
		inp = bus.inp
		outp = bus.outp

		loc = Blank()
		loc.arr = Array([Signal(bus.ShapeT()) for _ in range(bus.SIZE())])

		loc.PTR_WIDTH = width_from_arg(bus.SIZE())

		loc.head = Signal(loc.PTR_WIDTH)
		loc.tail = Signal(loc.PTR_WIDTH)

		loc.next_head = Signal(loc.PTR_WIDTH)
		loc.next_tail = Signal(loc.PTR_WIDTH)

		loc.incr_next_head = Signal(loc.PTR_WIDTH)

		loc.next_empty = Signal()
		loc.next_full = Signal()

		loc.clk, loc.rst = ClockSignal(), ResetSignal()

		if self.FORMAL():
			loc.formal = Blank()

			loc.formal.past_valid = Signal(reset=0b0)

			loc.formal.test_head = ((loc.head + 0x1) % bus.SIZE())
		#--------
		# Combinational logic

		#with m.If((~outp.empty) & inp.rd_en):
		#	m.d.comb += outp.rd_data.eq(loc.arr[loc.tail])

		m.d.comb += outp.rd_data.eq(loc.arr[loc.tail])

		# Compute `loc.next_head` and write into the FIFO if it's not full
		loc.HEAD_PLUS_1 = loc.head + 0x1
		with m.If(loc.rst):
			m.d.comb \
			+= [
				loc.next_head.eq(0x0),
				loc.next_tail.eq(0x0),
			]
		with m.Else(): # If(~loc.rst):
			with m.If((~outp.full) & inp.wr_en):
				#m.d.sync += loc.arr[loc.head].eq(inp.wr_data)

				with m.If(loc.HEAD_PLUS_1 >= bus.SIZE()):
					m.d.comb += loc.next_head.eq(0x0)
				with m.Else():
					m.d.comb += loc.next_head.eq(loc.HEAD_PLUS_1)
			with m.Else():
				m.d.comb += loc.next_head.eq(loc.head)

			# Compute `loc.next_tail`
			loc.TAIL_PLUS_1 = loc.tail + 0x1
			with m.If((~outp.empty) & inp.rd_en):
				with m.If(loc.TAIL_PLUS_1 >= bus.SIZE()):
					m.d.comb += loc.next_tail.eq(0x0)
				with m.Else():
					m.d.comb += loc.next_tail.eq(loc.TAIL_PLUS_1)
			with m.Else():
				m.d.comb += loc.next_tail.eq(loc.tail)

			# Compute `loc.next_empty` and `loc.next_full`
			loc.NEXT_HEAD_PLUS_1 = loc.next_head + 0x1
			with m.If(loc.NEXT_HEAD_PLUS_1 >= bus.SIZE()):
				m.d.comb += loc.incr_next_head.eq(0x0)
			with m.Else():
				m.d.comb += loc.incr_next_head.eq(loc.NEXT_HEAD_PLUS_1)

			with m.If(loc.incr_next_head == loc.next_tail):
				m.d.comb \
				+= [
					loc.next_empty.eq(0b0),
					loc.next_full.eq(0b1),
				]
			with m.Elif(loc.next_head == loc.next_tail):
				m.d.comb \
				+= [
					loc.next_empty.eq(0b1),
					loc.next_full.eq(0b0),
				]
			with m.Else():
				m.d.comb \
				+= [
					loc.next_empty.eq(0b0),
					loc.next_full.eq(0b0),
				]
		#if self.FORMAL():
		#	m.d.comb \
		#	+= [
		#		Cover(loc.formal.past_valid)
		#		#Cover(loc.formal.past_valid & outp.empty),
		#		#Cover(loc.formal.past_valid & outp.full),
		#		#Cover(loc.formal.past_valid & (~outp.empty)
		#		#	& (~outp.full)),
		#	]
		#--------
		# Sequential logic
		#with m.If(loc.rst):
		#	if self.FORMAL():
		#		m.d.sync += loc.formal.past_valid.eq(0b1)
		if self.FORMAL():
			m.d.sync += loc.formal.past_valid.eq(0b1)

		m.d.sync \
		+= [
			loc.head.eq(loc.next_head),
			loc.tail.eq(loc.next_tail),
			outp.empty.eq(loc.next_empty),
			outp.full.eq(loc.next_full),
		]
		#with m.If(loc.rst):
		#	#--------
		#	m.d.sync \
		#	+= [
		#		loc.head.eq(0x0),
		#		loc.tail.eq(0x0),

		#		outp.empty.eq(0b1),
		#		outp.full.eq(0b0),
		#	]
		#	#--------
		#with m.Else(): # If(~loc.rst):
		with m.If(~loc.rst):
			#--------

			# Write into the FIFO
			with m.If((~outp.full) & inp.wr_en):
				m.d.sync += loc.arr[loc.head].eq(inp.wr_data)
			#--------
			if self.FORMAL():
				#m.d.comb += Cover(loc.formal.past_valid)
				with m.If(loc.formal.past_valid):
					m.d.sync \
					+= [
						Cover(outp.empty),
						Cover(outp.full),
						Cover(~(outp.empty & outp.full)),
					]

					m.d.sync \
					+= [
						#Assert(loc.head == Past(loc.next_head)),
						#Assert(loc.tail == Past(loc.next_tail)),
						#Assert(outp.empty == Past(loc.next_empty)),
						#Assert(outp.full == Past(loc.next_full)),
						Assert(~(outp.empty & outp.full)),

						Assert(outp.rd_data == loc.arr[loc.tail]),
					]

					with m.If(Past(inp.rd_en)):
						with m.If(Past(outp.empty)):
							m.d.sync += Assert(Stable(loc.tail)),
						with m.Else(): # If(~Past(outp.empty)):
							m.d.sync += Assert(loc.tail
								== ((Past(loc.tail) + 1) % bus.SIZE()))
					with m.Else(): # If(~Past(inp.rd_en)):
						m.d.sync \
						+= [
							#Assert(Stable(outp.empty)),
							Assert(Stable(loc.tail)),
						]

					with m.If(Past(inp.wr_en)):
						with m.If(Past(outp.full)):
							m.d.sync += Assert(Stable(loc.head))
						with m.Else(): # If(~Past(outp.full)):
							m.d.sync += Assert(loc.head
								== ((Past(loc.head) + 1) % bus.SIZE()))
					with m.Else(): # If(~Past(inp.wr_en)):
						m.d.sync \
						+= [
							#Assert(Stable(outp.full)),
							Assert(Stable(loc.head)),
						]


					with m.Switch(Cat(outp.empty, outp.full)):
						# neither full nor empty
						with m.Case(0b00):
							m.d.sync \
							+= [
								Assert(loc.head != loc.tail),
								Assert(loc.formal.test_head != loc.tail),
							]

						# empty
						with m.Case(0b01):
							m.d.sync \
							+= [
								Assert(loc.head == loc.tail)
							]

						# full
						with m.Case(0b10):
							m.d.sync \
							+= [
								Assert(loc.formal.test_head == loc.tail)
							]

					# empty
					with m.If(loc.head == loc.tail):
						m.d.sync += Assert(outp.empty
							& (~outp.full))
					with m.Elif(loc.formal.test_head == loc.tail):
						m.d.sync += Assert((~outp.empty)
							& outp.full)
					with m.Else():
						m.d.sync += Assert((~outp.empty)
							& (~outp.full))
			#--------
		#--------
		return m
		#--------
#--------
