#!/usr/bin/env python3

from nmigen import *
import nmigen.tracer
from nmigen.hdl.rec import Record, Layout
from nmigen.asserts import Assert, Assume, Cover
from nmigen.asserts import Past, Rose, Fell, Stable

from enum import Enum, auto

from misc_util import *
