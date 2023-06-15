#!/usr/bin/env python3

from libcheesevoyage.general import *

#class HostLayt(dict):
def DATA_WIDTH():
	return 8

class BusFwdLayt(dict):
	def __init__(self):
		shape = {}
		shape["valid"] = 1
		shape["data"] = DATA_WIDTH()
		super().__init__(shape)
class BusBakLayt(dict):
	def __init__(self):
		shape = {}
		shape["ready"] = 1
		super().__init__(shape)
#class BusOneSideShape(IntfShape):
class BusOneSideLayt(dict):
	def __init__(self):
		shape = {}
		shape["fwd"] = BusFwdLayt()
		shape["bak"] = BusBakLayt()
		super().__init__(shape)

		#super().__init__(
		#	#mp_dct={
		#	#	"inp": Modport({"fwd": PortDir.Inp, "bak": PortDir.Outp})
		#	#}
		#	mp_dct={
		#		"fwd": Modport({"fwd": PortDir.Inp, "bak": PortDir.Outp}),
		#		"bak": Modport({"fwd": PortDir.Outp, "bak": PortDir.Inp}),
		#	},
		#	shape=layt,
		#)
#fwd_bus = Splitintf(BusOneSideShape())
#bak_bus = Splitintf(BusOneSideShape())
class BusIshape(IntfShape):
	def __init__(self):
		fwd_ishape = IntfShape()
		bak_ishape = IntfShape()
#bus_mp_dct = Modport({"fwd": })
#fwd_ishape = IntfShape
#bak_ishape
#bus = Splitintf(
#)
