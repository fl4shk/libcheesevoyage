#!/usr/bin/env python3

import sys
import math
from enum import Enum, auto

from nmigen import *
from nmigen.sim import *

from nmigen.cli import main, main_parser, main_runner
from nmigen_boards.de0_cv import *

from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from nmigen.back import verilog

from libcheesevoyage import *
#--------
class IntrcnLcvBus:
	#--------
	def __init__(self, host_node_bus_lst, dev_node_cfg_lst, *,
		PRIO_LST_2D=None, FORMAL=False):
		#--------
		if (not isinstance(PRIO_LST_2D, list)) \
			and (not isinstance(PRIO_LST_2D, type(None))):
			raise TypeError(psconcat
				("`PRIO_LST_2D` `{!r}` must be a `list` or `None`"
				.format(PRIO_LST_2D)))

		if isinstance(PRIO_LST_2D, list):
			if len(PRIO_LST_2D) != NUM_DEVS:
				raise ValueError(psconcat
					("`PRIO_LST_2D` `{!r}` must be of length `NUM_DEVS` ",
					"`{!r}`".format(PRIO_LST_2D)))

			#self.__PRIO_LST_2D = []

			for PRIO_LST in PRIO_LST_2D:
				temp = set(PRIO_LST)

				if (len(PRIO_LST) != len(temp)) \
					or (temp != set(list(range(NUM_HOSTS)))):
					raise ValueError(psconcat
						("`PRIO_LST` `{!r}` must ".format(PRIO_LST),
						"consist of all unique `int`s that are between 0 ",
						"and `NUM_HOSTS` `{!r}`".format(NUM_HOSTS)))

				#self.__PRIO_LST_2D.append(list(reversed(PRIO_LST)))

			self.__PRIO_LST_2D = PRIO_LST_2D
		else: # if isinstance(PRIO_LST, None):
			#self.__PRIO_LST_2D \
			#	= [
			#		[NUM_HOSTS - (i + 1) for i in range(NUM_HOSTS)]
			#			for j in range(NUM_DEVS)
			#	]
			#self.__PRIO_LST_2D \
			#	= [
			#		list(reversed([i for i in range(NUM_HOSTS)]))
			#			for j in range(NUM_DEVS)
			#	]
			self.__PRIO_LST_2D \
				= [
					[i for i in range(NUM_HOSTS)]
						for j in range(NUM_DEVS)
				]
		#--------
		#--------
	#--------
#--------
