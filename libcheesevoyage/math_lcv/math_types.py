#!/usr/bin/env python3

import math
from enum import Enum, auto

from amaranth import *
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from amaranth.sim import Simulator, Delay, Tick

from libcheesevoyage.misc_util import *
#--------
class ArithShape(Enum):
	Unsigned = 0
	Signed = auto()
#--------
