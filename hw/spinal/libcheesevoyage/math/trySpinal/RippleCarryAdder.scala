package libcheesevoyage.math.trySpinal

import spinal.core._
import spinal.lib._

case class RippleCarryAdderInp(width: Int) extends Bundle {
  val a = UInt(width bits)
  val b = UInt(width bits)
  val carry = UInt(1 bit)
}
case class RippleCarryAdderOutp(width: Int) extends Bundle {
  val sum = UInt(width bits)
  val carry = UInt(1 bit)
}
case class RippleCarryAdderIo(width: Int) extends Bundle {
  val inp = in(RippleCarryAdderInp(width=width))
  val outp = out(RippleCarryAdderOutp(width=width))
}

case class RippleCarryAdder(width: Int) extends Component {
  val io = RippleCarryAdderIo(width)

  val faArr = Array.fill(width)(FullAdder())

  faArr(0).io.inp.carry := io.inp.carry
  io.outp.carry := faArr(width - 1).io.outp.carry

  for (idx <- 0 to width - 1) {
    faArr(idx).io.inp.a(0) := io.inp.a(idx)
    faArr(idx).io.inp.b(0) := io.inp.b(idx)
    io.outp.sum(idx) := faArr(idx).io.outp.sum(0)

    if (idx > 0) {
      faArr(idx).io.inp.carry := faArr(idx - 1).io.outp.carry
    }
  }
}
