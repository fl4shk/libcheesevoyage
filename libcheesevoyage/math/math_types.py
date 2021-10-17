#!/usr/bin/env python3

import math
from enum import Enum, auto

from nmigen import *
from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from nmigen.sim import Simulator, Delay, Tick

from libcheesevoyage.misc_util import *
#--------
class ArithShape(Enum):
	Unsigned = 0
	Signed = auto()
#--------
