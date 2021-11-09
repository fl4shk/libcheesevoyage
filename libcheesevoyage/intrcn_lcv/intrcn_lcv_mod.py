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
	def __init__(self):
		pass
	#--------
#--------
