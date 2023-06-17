#!/usr/bin/env python3

from libcheesevoyage.general import *

def MAIN_WIDTH():
	return 32
def CACHE_LINE_WIDTH():
	return 64 * 8


class IcacheMetadataLayt(dict):
	def __init__(self):
		shape = {}
		#shape["tag"] = constants.ICACHE_SZ_PWR_INSN()
		shape["tag"] = MAIN_WIDTH()
		shape["dirty"] = 1
		super().__init__(shape)
class IcacheOutpLayt(dict):
	def __init__(self):
		shape = {}
		shape["data"] = CACHE_LINE_WIDTH()
		shape["metadt"] = IcacheMetadataLayt()
		super().__init__(shape)
class IcacheFromIfLayt(dict):
	def __init__(self):
		shape = {}
		shape["pc"] = MAIN_WIDTH()
		super().__init__(shape)
class IcacheIfIshape(IntfShape):
	def __init__(
		self,
		*,
		in_icache: bool=True,
		from_if_tag=None,
		to_if_tag=None,
	):
		shape = IntfShape.mk_fromto_shape(
			from_name="from_if", to_name="to_if",
			from_shape=IcacheFromIfLayt(), to_shape=IcacheOutpLayt(),
			in_from=in_icache,
			from_tag=from_if_tag, to_tag=to_if_tag,
			mk_from_modport=True, mk_to_modport=True,
		)
		super().__init__(shape)

#class IcacheFromIdLayt(dict):
#	def __init__(self):
#		shape = {}
#		super().__init__(shape)

class IcacheIdIshape(IntfShape):
	def __init__(
		self,
		*,
		in_icache: bool=True,
		#from_id_tag=None,
		to_id_tag=None,
	):
		shape = IntfShape.mk_fromto_shape(
			from_name=None, to_name="to_id",
			from_shape=None, to_shape=IcacheOutpLayt(),
			in_from=in_icache,
			from_tag=None, to_tag=to_id_tag,
		)
		super().__init__(shape)
class IcacheIshape(IntfShape):
	def __init__(
		self,
		*,
		#in_icache: bool=True,
		from_if_tag=None,
		to_if_tag=None,
		#from_id_tag=None,
		to_id_tag=None,
		#from_mem_tag=None,
		#to_mem_tag=None,
	):
		shape = {
			"if_bus": IcacheIfIshape(
				#in_icache=in_icache,
				in_icache=True,
				from_if_tag=from_if_tag,
				to_if_tag=to_if_tag,
			),
			"id_bus": IcacheIdIshape(
				#in_icache=in_icache,
				in_icache=True,
				#from_id_tag=from_id_tag,
				to_id_tag=to_id_tag,
			),
			#"mem_bus": MemIshape(
			#	in_mem=False,
			#	from_mem_tag=from_mem_tag,
			#	to_mem_tag=to_mem_tag,
			#),
		}
		super().__init__(shape)

intf = Splitintf(IcacheIshape())
print(
	repr(intf.id_bus.to_id), "\n",
	type(intf.id_bus.to_id), "\n",
	do_psconcat_flattened(intf.id_bus.to_id),
	do_psconcat_flattened(intf.id_bus.to_id.metadt),
)
