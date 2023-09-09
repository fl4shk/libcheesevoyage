package libcheesevoyage.math.trySpinal

import spinal.core._
import spinal.lib._

object AluOp extends SpinalEnum(defaultEncoding=binarySequential) {
  val
    add,
    adc,
    sub,
    sbc,
    and,
    or,
    xor
    = newElement();
}

case class AluInp(width: Int) extends Bundle {
  val a = UInt(width bits)
  val b = UInt(width bits)
  val carry = UInt(1 bit)
  val op = AluOp()
}
case class AluOutp(width: Int) extends Bundle {
  val result = UInt(width bits)
  val carry = UInt(1 bit)
}

case class AluIo(width: Int) extends Bundle {
  val inp = in(AluInp(width=width))
  val outp = out(AluOutp(width=width))
}

case class Alu(width: Int) extends Component {
  val io = AluIo(width)

  val rcaArrIdxAdd = 0
  val rcaArrIdxAdc = 1
  val rcaArrIdxSub = 2
  val rcaArrIdxSbc = 3
  val rcaArrSize = 4

  val rcaArr = Array.fill(rcaArrSize)(RippleCarryAdder(width=width))
  for (idx <- 0 to rcaArrSize - 1) {
    rcaArr(idx).io.inp.a := io.inp.a
    idx match {
      case `rcaArrIdxAdd` => {
        rcaArr(idx).io.inp.b := io.inp.b
        rcaArr(idx).io.inp.carry := 0
      }
      case `rcaArrIdxAdc` => {
        rcaArr(idx).io.inp.b := io.inp.b
        rcaArr(idx).io.inp.carry := io.inp.carry
      }
      case `rcaArrIdxSub` => {
        rcaArr(idx).io.inp.b := ~io.inp.b
        rcaArr(idx).io.inp.carry := 0
      }
      case `rcaArrIdxSbc` => {
        rcaArr(idx).io.inp.b := ~io.inp.b
        rcaArr(idx).io.inp.carry := io.inp.carry
      }
      case _ => println("Invalid idx")
    }
  }

  switch(io.inp.op) {
    is(AluOp.add) {
      val idx = rcaArrIdxAdd
      val rca = rcaArr(idx)
      (io.outp.carry, io.outp.result) := Cat(
        rca.io.outp.carry, rca.io.outp.sum
      )
      //io.outp.carry := rca.io.outp.carry
      //io.outp.result := rca.io.outp.sum
    }
    is(AluOp.adc) {
      val idx = rcaArrIdxAdc
      val rca = rcaArr(idx)
      (io.outp.carry, io.outp.result) := Cat(
        rca.io.outp.carry, rca.io.outp.sum
      )
      //io.outp.carry := rca.io.outp.carry
      //io.outp.result := rca.io.outp.sum
    }
    is(AluOp.sub) {
      val idx = rcaArrIdxSub
      val rca = rcaArr(idx)
      //Cat(io.outp.carry, io.outp.result) := Cat(
      //  rca.io.outp.carry, rca.io.outp.sum
      //)
      io.outp.carry := rca.io.outp.carry
      io.outp.result := rca.io.outp.sum
    }
    is(AluOp.sbc) {
      val idx = rcaArrIdxSbc
      val rca = rcaArr(idx)
      (io.outp.carry, io.outp.result) := Cat(
        rca.io.outp.carry, rca.io.outp.sum
      )
      //io.outp.carry := rca.io.outp.carry
      //io.outp.result := rca.io.outp.sum
    }
    is(AluOp.and) {
      (io.outp.carry, io.outp.result) := Cat(B"1'b0", io.inp.a & io.inp.b)
      //io.outp.carry := B"1'b0" 
      //io.outp.result := io.inp.a & io.inp.b
    }
    is(AluOp.or) {
      (io.outp.carry, io.outp.result) := Cat(B"1'b0", io.inp.a | io.inp.b)
      //io.outp.carry := B"1'b0" 
      //io.outp.result := io.inp.a | io.inp.b
    }
    is(AluOp.xor) {
      (io.outp.carry, io.outp.result) := Cat(B"1'b0", io.inp.a ^ io.inp.b)
      //io.outp.carry := B"1'b0" 
      //io.outp.result := io.inp.a | io.inp.b
    }
    //default {
    //  //(io.outp.carry, io.outp.result) := 0
    //  //io.outp.carry := B"1'b0"
    //  io.outp.carry := 0
    //  io.outp.result := 0
    //}
  }
}
