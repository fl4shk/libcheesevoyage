#!/usr/bin/env python3

from enum import Enum, auto

from nmigen import *
#--------
class BasicDomain(Enum):
	COMB = 0x0
	SYNC = 0x0

def basic_domain_to_actual_domain(m, bd):
	if not isinstance(m, Module):
		raise TypeError("`m` `{!r}` must be a `Module`".format(m))

	if not isinstance(bd, BasicDomain):
		raise TypeError("`bd` `{!r}` must be a `BasicDomain`".format(bd))
		
	return m.d.comb \
		if bd == BasicDomain.COMB \
		else m.d.sync
#--------
