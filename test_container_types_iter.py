#!/usr/bin/env python3

from nmigen import *
from nmigen_boards import *

from libcheesevoyage import *

import functools
import operator
#--------
#a = Packarr.build(2, 4)
#printout(functools.reduce(operator.or_, a), "\n")
a = Packarr.build(Packarr.Shape(2, 4), 2)
#b = Signal(8)
#c = Signal.like(b)
#d = Signal.like(c)
printout(functools.reduce(operator.or_, a[0]), "\n")
##for elem in a:
##	printout(functools.reduce(operator.or_, elem), "\n")
#printout("{!r}".format(a), "\n")
#printout("{!r}".format(a[b]), "\n")
#printout("{!r}".format(a[b][c]), "\n")
#printout("{!r}".format(a[b][c][1]), "\n")
#printout("{!r}".format(a[b][c][0:2]), "\n")
#--------
