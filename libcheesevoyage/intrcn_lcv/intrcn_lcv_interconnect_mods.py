#!/usr/bin/env python3

import sys
import math
from enum import Enum, auto

from amaranth import *
from amaranth.sim import *

from amaranth.cli import main, main_parser, main_runner

from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.back import verilog

#from libcheesevoyage import *
from libcheesevoyage.general.container_types import *
from libcheesevoyage.general.pipeline_mods import *
from libcheesevoyage.intrcn_lcv.xbar_switch_mods import *
from libcheesevoyage.intrcn_lcv.intrcn_lcv_node_bus_types import *
#--------
class IntrcnLcvInterconnectBus:
	#--------
	def __init__(self, host_node_bus_lst, dev_node_cfg_lst):
		#--------
		# `host_node_bus` required format: `IntrcnLcvNodeBus`
		for host_node_bus in host_node_bus_lst:
			if not isinstance(host_node_bus, IntrcnLcvNodeBus):
				raise TypeError(psconcat(
					"`host_node_bus` `{!r}` must be an ",
					"`IntrcnLcvNodeBus`"
					).format(host_node_bus)
				)
		self.host_node_bus_lst = host_node_bus_lst
		#--------
		# `dev_node_cfg` required format: `[IntrcnLcvNodeBus, int, int]`,
		# `dev_node_cfg[1]` is the device address range's low value, and
		# `dev_node_cfg[2]` is the device address range's high value
		for dev_node_cfg in dev_node_cfg_lst:
			if (
				not isinstance(dev_node_cfg, list)
				or len(dev_node_cfg) != 3
				or not isinstance(dev_node_cfg[0], IntrcnLcvNodeBus)
				or not isinstance(dev_node_cfg[1], int)
				or not isinstance(dev_node_cfg[2], int)
			):
				raise TypeError(psconcat(
					"`dev_node_cfg` `{!r}` must be of the following ",
					"format: `[IntrcnLcvNodeBus, int, int]`"
					).format(dev_node_cfg)
				)
			if (
				dev_node_cfg[1] < 0
				or dev_node_cfg[1] > dev_node_cfg[2]
			):
				raise ValueError(psconcat(
					"`dev_node_cfg[1]` `{!r}` must be >= 0, and ",
					"`dev_node_cfg[2]` `{!r}` must be >= ",
					"`dev_node_cfg[1]`"
					).format(dev_node_cfg[1], dev_node_cfg[2])
				)
		self.dev_node_cfg_lst = dev_node_cfg_lst
		#--------
	#--------
#--------
class IntrcnLcvInterconnect:
	#--------
	def __init__(
		self,
		host_node_cfg_lst, dev_node_cfg_lst, 
		*, PRIO_LST_2D=None, FORMAL=False
	):
		#--------
		if (
			not isinstance(PRIO_LST_2D, list)
			and not isinstance(PRIO_LST_2D, type(None))
		):
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

				if (
					len(PRIO_LST) != len(temp)
					or temp != set(list(range(NUM_HOSTS)))
				):
					raise ValueError(psconcat(
						"`PRIO_LST` `{!r}` must ".format(PRIO_LST),
						"consist of all unique `int`s that are between 0 ",
						"and `NUM_HOSTS` `{!r}`".format(NUM_HOSTS)
					))

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
			self.__PRIO_LST_2D = [
				[i for i in range(NUM_HOSTS)]
					for j in range(NUM_DEVS)
			]
		#--------
		self.__bus = IntrcnLcvInterconnectBus(
			host_node_bus_lst=host_node_bus_lst,
			dev_node_cfg_lst=dev_node_cfg_lst,
		)
		#--------
	#--------
#--------
