package libcheesevoyage

import spinal.core._
import spinal.lib._

case class FullAdderInp() extends Bundle {
  val a = in port UInt(1 bit)
  val b = in port UInt(1 bit)
  val carry = in port UInt(1 bit)
}
case class FullAdderOutp() extends Bundle {
  val sum = out port UInt(1 bit)
  val carry = out port UInt(1 bit)
}

case class FullAdderIo() extends Bundle {
  val inp = in(FullAdderInp())
  val outp = out(FullAdderOutp())
}

case class FullAdder() extends Component {
  val io = FullAdderIo()

  //val zero = Bool(False)
  //val zero = UInt(1 bit)
  //zero := 0

  (io.outp.carry, io.outp.sum) := (
    Cat(B"1'b0", io.inp.a).asUInt
    + Cat(B"1'b0", io.inp.b).asUInt
    + Cat(B"1'b0", io.inp.carry).asUInt
  ).asBits
  //val half_sum = io.inp.a ^ io.inp.b
  //io.outp.sum := half_sum ^ io.inp.carry
  //io.outp.carry := (io.inp.a & io.inp.b) | (half_sum & io.inp.carry)
}
