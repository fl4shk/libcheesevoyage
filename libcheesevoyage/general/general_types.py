#!/usr/bin/env python3

from amaranth import *
import amaranth.tracer
from amaranth.hdl.rec import Record, Layout
from amaranth.asserts import Assert, Assume, Cover
from amaranth.asserts import Past, Rose, Fell, Stable

from enum import Enum, auto

from libcheesevoyage.misc_util import *
